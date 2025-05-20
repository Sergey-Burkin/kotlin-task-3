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


    
}