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
// В GtfsModels.kt замените соответствующие классы:

data class RouteSegment(
    val routeId: String,        // ID маршрута из GTFS
    val routeShortName: String, // Номер автобуса (напр. "12")
    val tripId: String,
    val fromStop: GtfsStop,
    val toStop: GtfsStop,
    val departureTime: String,
    val arrivalTime: String,
    val stopCount: Int,
    val shapeId: String? = null
)

data class RouteOption(
    val segments: List<RouteSegment>,
    val walkToFirstStopMinutes: Int,
    val walkToFinalDestMinutes: Int,
    val totalMinutes: Int
) {
    val isDirect: Boolean get() = segments.size == 1

    // Уникальный ключ: маршрут1:старт1->финиш1 | маршрут2:старт2->финиш2
    val uniqueKey: String
        get() = segments.joinToString("|") {
            "${it.routeId}:${it.fromStop.stopId}->${it.toStop.stopId}"
        }
}

data class LiveVehicle(
    val vehicleId: String,
    val type: String,
    val line: String,
    val variant: String?,
    val destinationBg: String?,
    val destinationEn: String?,
    val delayMs: Long?,
    val lat: Double,
    val lon: Double,
    val bearing: Int?,
    val extra: Int?,
    val timestamp: Long?
)

