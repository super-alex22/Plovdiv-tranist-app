package com.example.plovdivtransit

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit



data class LiveTripInfo(
    val nextStop: Int?,
    val delayMs: Long?
)

class LiveTransportRepository {

    private val client = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private var webSocket: WebSocket? = null

    fun connectPlovdiv(
        onVehiclesUpdated: (List<LiveVehicle>) -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        disconnect()

        val request = Request.Builder()
            .url("wss://api.livetransport.eu/plovdiv")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("LIVE_WS", "Connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val vehicles = parseVehiclesMessage(text)
                    onVehiclesUpdated(vehicles)
                } catch (t: Throwable) {
                    Log.e("LIVE_WS", "Parse error", t)
                    onError(t)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("LIVE_WS", "Closing: $code / $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("LIVE_WS", "Closed: $code / $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("LIVE_WS", "Failure", t)
                onError(t)
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "bye")
        webSocket = null
    }

    suspend fun getTripInfo(
        vehicleId: String,
        tripId: String
    ): LiveTripInfo? {
        return try {
            val encodedVehicleId = java.net.URLEncoder.encode(vehicleId, "UTF-8")
            val encodedTripId = java.net.URLEncoder.encode(tripId, "UTF-8")

            val request = Request.Builder()
                .url("https://api.livetransport.eu/plovdiv/vehicle/$encodedVehicleId/trip?trip=$encodedTripId")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null

                val body = response.body?.string() ?: return null
                val json = JSONObject(body)

                LiveTripInfo(
                    nextStop = json.optInt("nextStop").let { if (it == 0 && !json.has("nextStop")) null else it },
                    delayMs = if (json.has("delay")) json.optLong("delay") else null
                )
            }
        } catch (t: Throwable) {
            Log.e("LIVE_API", "Trip info error", t)
            null
        }
    }

    private fun parseVehiclesMessage(text: String): List<LiveVehicle> {
        val root = JSONArray(text)
        val result = mutableListOf<LiveVehicle>()

        for (i in 0 until root.length()) {
            val item = root.optJSONArray(i) ?: continue
            val vehicle = parseVehicle(item) ?: continue
            result.add(vehicle)
        }

        return result
    }

    private fun parseVehicle(item: JSONArray): LiveVehicle? {
        if (item.length() < 10) return null

        val vehicleId = item.optString(0, "")
        val type = item.optString(1, "")
        val line = item.optString(2, "")
        val variant = item.optStringOrNull(3)

        val destinationObj = item.optJSONObject(4)
        val destinationBg = destinationObj?.optString("bg")
        val destinationEn = destinationObj?.optString("en")

        val delayMs = item.optLongOrNull(5)

        val coords = item.optJSONArray(6) ?: return null
        val lat = coords.optDouble(0, Double.NaN)
        val lon = coords.optDouble(1, Double.NaN)
        if (lat.isNaN() || lon.isNaN()) return null

        val bearing = item.optIntOrNull(7)
        val extra = item.optIntOrNull(8)
        val timestamp = item.optLongOrNull(9)

        return LiveVehicle(
            vehicleId = vehicleId,
            type = type,
            line = line,
            variant = variant,
            destinationBg = destinationBg,
            destinationEn = destinationEn,
            delayMs = delayMs,
            lat = lat,
            lon = lon,
            bearing = bearing,
            extra = extra,
            timestamp = timestamp
        )
    }
}

private fun JSONArray.optStringOrNull(index: Int): String? {
    if (isNull(index)) return null
    val value = optString(index, "")
    return value.ifBlank { null }
}

private fun JSONArray.optIntOrNull(index: Int): Int? {
    if (isNull(index)) return null
    return try { getInt(index) } catch (_: Throwable) { null }
}

private fun JSONArray.optLongOrNull(index: Int): Long? {
    if (isNull(index)) return null
    return try { getLong(index) } catch (_: Throwable) { null }
}