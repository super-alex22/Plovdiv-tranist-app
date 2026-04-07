package com.example.plovdivtransit

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class OsrmResponse(
    val routes: List<OsrmRoute>
)

data class OsrmRoute(
    val geometry: OsrmGeometry,
    val distance: Double,
    val duration: Double
)

data class OsrmGeometry(
    val coordinates: List<List<Double>>
)

interface RoutingApi {
    @GET("route/v1/driving/{coordinates}")
    suspend fun getRoute(
        @Path("coordinates") coordinates: String,
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "geojson"
    ): OsrmResponse
}