package org.example


import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import java.util.concurrent.atomic.AtomicInteger
import io.ktor.client.plugins.contentnegotiation.*
import kotlinx.serialization.encodeToString
import kotlinx.coroutines.channels.consumeEach


val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

val requestId = AtomicInteger(1)

fun main() = runBlocking {

    val client = HttpClient(CIO) {
        install(WebSockets) { contentConverter = KotlinxWebsocketSerializationConverter(Json) }
        install(ContentNegotiation) {
            json(json)
        }
    }
    val windowProcessor = TimeWindowProcessor()
    try {
        client.webSocket(DERIBIT_TEST_WS_URL) {
            println("Connected to Deribit WebSocket.")

            val subscribeRequest = DeribitRequest(
                id = requestId.getAndIncrement(),
                method = "public/subscribe",
                params = SubscribeParams(channels = listOf("ticker.$INSTRUMENT_NAME.$TICKER_SUBSCRIPTION_INTERVAL"))
            )
            send(Frame.Text(json.encodeToString(subscribeRequest)))
            println("Sent subscription request: ${json.encodeToString(subscribeRequest)}")
            incoming.consumeEach {
                if (it is Frame.Text) {
                    delay(100)
                    val response = json.decodeFromString<DeribitResponse>(it.readText())
                    if (response.params != null) {
                        windowProcessor.addData(response.params.data)
                        windowProcessor.displayData()
                    }
                }
            }
        }
    } catch (e: Exception) {
        println("Connection error: ${e.message}.")
        delay(5000)
    } finally {
        client.close()
    }

}
