package org.example

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class DeribitRequest(
    val jsonrpc: String = "2.0",
    val id: Int,
    val method: String,
    val params: SubscribeParams
)

@Serializable
data class SubscribeParams(
    val channels: List<String>
)

@Serializable
data class DeribitResponse(
    val params: DeribitParams? = null,
)

@Serializable
data class DeribitParams(
    val data: PriceData
)

@Serializable
data class PriceData(
    val timestamp: Long,
    @SerialName(PRICE_TAG) val price: Double
)