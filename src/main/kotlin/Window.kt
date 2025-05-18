package org.example

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


class TimeWindowProcessor {

    private val windowDuration: Long = WINDOW_SIZE_IN_SECONDS * 1000
    private val dataPoints = ArrayDeque<PriceData>()
    private val mutex = Mutex()

    private fun removeExpiredData() {
        if (windowDuration <= 0) return // Если окно не ограничено по времени

        val currentTime = System.currentTimeMillis()
        while (dataPoints.isNotEmpty() &&
            currentTime - dataPoints.first().timestamp > windowDuration) {
            dataPoints.removeFirst()
        }
    }

    suspend fun addData(point: PriceData) = mutex.withLock {
        // Добавляем новую точку
        dataPoints.addLast(point)
    }

    suspend fun getWindowData(): List<PriceData> = mutex.withLock {
        // Удаляем старые данные, которые вышли за пределы окна
        removeExpiredData()
        // Возвращаем копию данных в окне
        dataPoints.toList()
    }

    suspend fun clear() = mutex.withLock {
        dataPoints.clear()
    }

    // Constants for formatting
    private val PRICE_FORMAT = "%.2f"
    private val PREDICTION_FORMAT = "%.4f"
    private val TIME_FORMAT = "HH:mm:ss.SSS"

    suspend fun displayData() {
        val data = getWindowData()
        val currentTime = System.currentTimeMillis()

        // Format the current price
        val currentPrice = data.lastOrNull()?.price?.let { PRICE_FORMAT.format(it) } ?: "N/A"

        // Prepare predictions if enough data is available
        val predictions = if (data.size >= MINIMAL_WINDOW_SIZE) {
            val linearPrediction = approximate(data, ApproximationType.Linear)(currentTime + PREDICTION_TIME_IN_SECONDS * 1000)
            val averagePrediction = approximate(data, ApproximationType.Average)(currentTime + PREDICTION_TIME_IN_SECONDS * 1000)
            """        |   Linear Prediction: ${PREDICTION_FORMAT.format(linearPrediction)}
        |   Average Prediction: ${PREDICTION_FORMAT.format(averagePrediction)}
        """
        } else {
            "|   Not enough data for predictions (min. $MINIMAL_WINDOW_SIZE points required)"
        }

        // Format the output with aligned columns
        val output = """
        | Time: ${java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern(TIME_FORMAT))}
        | Current Price: $currentPrice
        | Predictions:
        $predictions
        """.trimMargin()

        println(output)
    }
    
}