package com.example.plovdivtransit

import android.content.Context
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class GeoapifySuggestion(
    val name: String,
    val lat: Double,
    val lon: Double
)

class GeoapifyRepository(private val context: Context) {

    fun searchPlaces(
        query: String,
        userLat: Double? = null,
        userLon: Double? = null
    ): List<GeoapifySuggestion> {
        android.util.Log.d("GEOAPIFY", "searchPlaces called, query=$query")

        if (query.isBlank()) return emptyList()

        val apiKey = context.getString(R.string.geoapify_api_key)
        android.util.Log.d("GEOAPIFY", "API key blank: ${apiKey.isBlank()}")

        if (apiKey.isBlank()) return emptyList()

        val encodedQuery = URLEncoder.encode(query, "UTF-8")

        val biasLon = userLon ?: 24.7453
        val biasLat = userLat ?: 42.1354

        val urlString =
            "https://api.geoapify.com/v1/geocode/autocomplete" +
                    "?text=$encodedQuery" +
                    "&limit=8" +
                    "&filter=countrycode:bg" +
                    "&bias=proximity:$biasLon,$biasLat" +
                    "&apiKey=$apiKey"

        android.util.Log.d("GEOAPIFY", "Request URL: $urlString")

        val connection = URL(urlString).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        return try {
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

            android.util.Log.d("GEOAPIFY", "Response code: $responseCode")
            android.util.Log.d("GEOAPIFY", "Response body: $response")

            if (responseCode !in 200..299) {
                emptyList()
            } else {
                val parsed = parseSuggestions(response)

                val sorted = if (userLat != null && userLon != null) {
                    parsed.sortedWith(
                        compareBy<GeoapifySuggestion> {
                            distanceMeters(userLat, userLon, it.lat, it.lon)
                        }.thenBy { it.name }
                    )
                } else {
                    parsed.sortedWith(
                        compareByDescending<GeoapifySuggestion> {
                            val lower = it.name.lowercase()
                            lower.contains("plovdiv") || lower.contains("пловдив")
                        }.thenBy { it.name }
                    )
                }

                android.util.Log.d("GEOAPIFY", "Parsed suggestions count: ${sorted.size}")
                sorted
            }
        } catch (e: Exception) {
            android.util.Log.e("GEOAPIFY", "Search failed", e)
            emptyList()
        } finally {
            connection.disconnect()
        }
    }

    private fun parseSuggestions(json: String): List<GeoapifySuggestion> {
        val result = mutableListOf<GeoapifySuggestion>()

        return try {
            val root = org.json.JSONObject(json)
            val features = root.optJSONArray("features") ?: return emptyList()

            for (i in 0 until features.length()) {
                val feature = features.optJSONObject(i) ?: continue
                val properties = feature.optJSONObject("properties") ?: continue

                val name = properties.optString("formatted")
                val lat = properties.optDouble("lat")
                val lon = properties.optDouble("lon")

                if (name.isNotBlank()) {
                    result.add(
                        GeoapifySuggestion(
                            name = name,
                            lat = lat,
                            lon = lon
                        )
                    )
                }
            }

            result
        } catch (e: Exception) {
            android.util.Log.e("GEOAPIFY", "Parse failed", e)
            emptyList()
        }
    }
}