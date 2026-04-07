package com.example.plovdivtransit

import org.osmdroid.util.GeoPoint
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class RouteData(
    val points: List<GeoPoint>,
    val distanceMeters: Double,
    val durationSeconds: Double
)
class RoutingRepository {

    private val api = Retrofit.Builder()
        .baseUrl("https://router.project-osrm.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RoutingApi::class.java)

    suspend fun getRouteData(
        fromLat: Double,
        fromLon: Double,
        toLat: Double,
        toLon: Double
    ): RouteData {
        val coordinates = "$fromLon,$fromLat;$toLon,$toLat"
        val response = api.getRoute(coordinates)

        val route = response.routes.firstOrNull()
            ?: return RouteData(
                points = emptyList(),
                distanceMeters = 0.0,
                durationSeconds = 0.0
            )

        val points = route.geometry.coordinates.map { coord ->
            GeoPoint(coord[1], coord[0])
        }

        return RouteData(
            points = points,
            distanceMeters = route.distance,
            durationSeconds = route.duration
        )
    }
}