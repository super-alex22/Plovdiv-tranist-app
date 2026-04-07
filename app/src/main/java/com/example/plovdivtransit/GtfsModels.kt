package com.example.plovdivtransit

data class GtfsStop(
    val stopId: String,
    val stopCode: String?,
    val stopName: String,
    val stopLat: Double,
    val stopLon: Double
)

data class GtfsTrip(
    val routeId: String,
    val tripId: String,
    val shapeId: String? = null
)

data class GtfsRoute(
    val routeId: String,
    val routeShortName: String
)

data class DirectTripResult(
    val tripId: String,
    val routeId: String,
    val routeShortName: String,
    val startStopId: String,
    val endStopId: String,
    val startDepartureTime: String,
    val endArrivalTime: String,
    val stopCount: Int,
    val shapeId: String? = null
)

data class GtfsStopTime(
    val tripId: String,
    val arrivalTime: String,
    val departureTime: String,
    val stopId: String,
    val stopSequence: Int
)

data class UserStopMatch(
    val stop: GtfsStop,
    val distanceMeters: Double
)

data class GtfsRouteOption(
    val routeShortName: String,
    val startStop: GtfsStop,
    val endStop: GtfsStop,
    val walkToStopMinutes: Int,
    val busMinutes: Int,
    val walkToDestMinutes: Int,
    val totalMinutes: Int,
    val tripId: String? = null,
    val shapeId: String? = null
)