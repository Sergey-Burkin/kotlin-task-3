package org.example

data class RegressionResult(
    val slope: Double,
    val intercept: Double
)

fun calculateSlopeAndIntercept(data: List<PriceData>): RegressionResult {
    val n = data.size
    require(n > 1) { "Not enough data points for linear regression." }
    val firstTimestamp = data.first().timestamp.toDouble()
    val x = data.map{(it.timestamp.toDouble() - firstTimestamp) / TIME_STAMP_NORMALIZATOR}
    val y = data.map{it.price / PRICE_NORMALIZATOR}
    val sumX = x.sum()
    val sumY = y.sum()
    val sumXY = x.zip(y).sumOf { (x, y) -> x * y }
    val sumX2 = x.sumOf { it * it }

    val slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
    val intercept = (sumY - slope * sumX) / n

    return RegressionResult(slope, intercept)
}


enum class ApproximationType {
    LINEAR,
    AVERAGE
}

fun approximate(
    data: List<PriceData>,
    type: ApproximationType
): (Long) -> Double = when (type) {
    ApproximationType.LINEAR -> linearRegression(data)
    ApproximationType.AVERAGE -> movingAverage(data)
}

private fun linearRegression(data: List<PriceData>): (Long) -> Double {
    require(data.size >= 2) { "Need at least 2 points for regression" }

    val regressionResult = calculateSlopeAndIntercept(data)
    val firstTimestamp = data.first().timestamp.toDouble()

    return { timestamp ->
        val normalizedTime = (timestamp.toDouble() - firstTimestamp) / TIME_STAMP_NORMALIZATOR
        (regressionResult.slope * normalizedTime + regressionResult.intercept) * PRICE_NORMALIZATOR
    }
}


private fun movingAverage(data: List<PriceData>): (Long) -> Double {
    require(data.isNotEmpty()) { "Need at least one point for moving average" }
    val averagePrice = data.sumOf { it.price } / data.size.toDouble()
    return { _ ->
        averagePrice }
}
