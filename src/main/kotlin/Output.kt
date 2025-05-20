package org.example

// Constants for formatting
private val PRICE_FORMAT = "%.2f"
private val PREDICTION_FORMAT = "%.4f"
private val TIME_FORMAT = "HH:mm:ss.SSS"

suspend fun displayData(window: TimeWindowProcessor) {
    val data = window.getWindowData()
    val currentTime = System.currentTimeMillis()

    // Format the current price
    val currentPrice = data.lastOrNull()?.price?.let { PRICE_FORMAT.format(it) } ?: "N/A"

    // Prepare predictions if enough data is available
    val predictions = if (data.size >= MINIMAL_WINDOW_SIZE) {
        val linearPrediction = approximate(data, ApproximationType.LINEAR)(currentTime + PREDICTION_TIME_IN_SECONDS * 1000)
        val averagePrediction = approximate(data, ApproximationType.AVERAGE)(currentTime + PREDICTION_TIME_IN_SECONDS * 1000)
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