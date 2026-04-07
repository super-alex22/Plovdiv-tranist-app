package com.example.plovdivtransit

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint

class GtfsRepository(private val context: Context) {
    private var cachedTripsById: Map<String, GtfsTrip>? = null
    private var cachedRoutesById: Map<String, GtfsRoute>? = null
    private var cachedStopTimesByTrip: Map<String, List<GtfsStopTime>>? = null
    private var cachedStops: List<GtfsStop>? = null
    private var cachedStopTimes: List<GtfsStopTime>? = null
    private var cachedTrips: List<GtfsTrip>? = null
    private var cachedRoutes: List<GtfsRoute>? = null
    private var cachedShapes: Map<String, List<GeoPoint>>? = null

    fun readStopTimesFile(): String {
        return context.assets.open("plovdiv/stop_times.txt")
            .bufferedReader()
            .use { it.readText() }
    }

    fun readTripsFile(): String {
        return context.assets.open("plovdiv/trips.txt")
            .bufferedReader()
            .use { it.readText() }
    }

    fun readShapesFile(): String {
        return context.assets.open("plovdiv/shapes.txt")
            .bufferedReader()
            .use { it.readText() }
    }

    fun loadShapes(): Map<String, List<GeoPoint>> {
        cachedShapes?.let { return it }

        val loaded = readShapesFile()
            .lineSequence()
            .drop(1)
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = parseCsvLine(line)
                if (parts.size < 4) return@mapNotNull null

                val shapeId = parts[0].replace("\"", "")
                val lat = parts[1].toDoubleOrNull() ?: return@mapNotNull null
                val lon = parts[2].toDoubleOrNull() ?: return@mapNotNull null
                val sequence = parts[3].toIntOrNull() ?: 0

                shapeId to (sequence to GeoPoint(lat, lon))
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { entry ->
                entry.value.sortedBy { it.first }.map { it.second }
            }

        cachedShapes = loaded
        return loaded
    }

    fun loadTrips(): List<GtfsTrip> {
        cachedTrips?.let { return it }

        val loaded = readTripsFile()
            .lineSequence()
            .drop(1)
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = parseCsvLine(line)
                if (parts.size < 3) return@mapNotNull null

                GtfsTrip(
                    routeId = parts[0].replace("\"", ""),
                    tripId = parts[2].replace("\"", ""),
                    shapeId = if (parts.size > 4) parts[4].replace("\"", "") else null
                )
            }
            .toList()

        cachedTrips = loaded
        return loaded
    }

    fun readRoutesFile(): String {
        return context.assets.open("plovdiv/routes.txt")
            .bufferedReader()
            .use { it.readText() }
    }

    fun loadRoutes(): List<GtfsRoute> {
        cachedRoutes?.let { return it }

        val loaded = readRoutesFile()
            .lineSequence()
            .drop(1)
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = parseCsvLine(line)
                if (parts.size < 5) return@mapNotNull null

                GtfsRoute(
                    routeId = parts[0].replace("\"", ""),
                    routeShortName = parts[4].replace("\"", "")
                )
            }
            .toList()

        cachedRoutes = loaded
        return loaded
    }

    fun loadStopTimes(): List<GtfsStopTime> {
        cachedStopTimes?.let { return it }

        val loaded = readStopTimesFile()
            .lineSequence()
            .drop(1)
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = parseCsvLine(line)
                if (parts.size < 5) return@mapNotNull null

                val tripId = parts[0].replace("\"", "")
                val stopId = parts[1].replace("\"", "")
                val arrivalTime = parts[2].replace("\"", "")
                val departureTime = parts[3].replace("\"", "")
                val stopSequence = parts[4].replace("\"", "").toIntOrNull() ?: return@mapNotNull null

                GtfsStopTime(
                    tripId = tripId,
                    arrivalTime = arrivalTime,
                    departureTime = departureTime,
                    stopId = stopId,
                    stopSequence = stopSequence
                )
            }
            .toList()

        cachedStopTimes = loaded
        return loaded
    }

    fun readStopsFile(): String {
        return context.assets.open("plovdiv/stops.txt")
            .bufferedReader()
            .use { it.readText() }
    }

    fun loadStops(): List<GtfsStop> {
        cachedStops?.let { return it }

        val loaded = readStopsFile()
            .lineSequence()
            .drop(1)
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = parseCsvLine(line)
                if (parts.size < 5) return@mapNotNull null

                val stopId = parts[0].replace("\"", "")
                val stopCode = parts[1].replace("\"", "").ifBlank { null }
                val stopName = parts[2].replace("\"", "")
                val stopLat = parts[3].replace("\"", "").toDoubleOrNull() ?: return@mapNotNull null
                val stopLon = parts[4].replace("\"", "").toDoubleOrNull() ?: return@mapNotNull null

                GtfsStop(
                    stopId = stopId,
                    stopCode = stopCode,
                    stopName = stopName,
                    stopLat = stopLat,
                    stopLon = stopLon
                )
            }
            .toList()

        cachedStops = loaded
        return loaded
    }

    fun findDirectTripBetweenStops(
        startStopId: String,
        endStopId: String
    ): DirectTripResult? {
        val tripsById = getTripsById()
        val routesById = getRoutesById()
        val stopTimesByTrip = getStopTimesByTrip()

        for ((tripId, sortedStopTimes) in stopTimesByTrip) {
            val start = sortedStopTimes.firstOrNull { it.stopId == startStopId } ?: continue
            val end = sortedStopTimes.firstOrNull { it.stopId == endStopId } ?: continue

            if (start.stopSequence >= end.stopSequence) continue

            val trip = tripsById[tripId] ?: continue
            val route = routesById[trip.routeId] ?: continue

            return DirectTripResult(
                tripId = tripId,
                routeId = trip.routeId,
                routeShortName = route.routeShortName,
                startStopId = startStopId,
                endStopId = endStopId,
                startDepartureTime = start.departureTime,
                endArrivalTime = end.arrivalTime,
                stopCount = end.stopSequence - start.stopSequence,
                shapeId = trip.shapeId
            )
        }

        return null
    }

    private fun getTripsById(): Map<String, GtfsTrip> {
        cachedTripsById?.let { return it }

        val built = loadTrips().associateBy { it.tripId }
        cachedTripsById = built
        return built
    }

    private fun getRoutesById(): Map<String, GtfsRoute> {
        cachedRoutesById?.let { return it }

        val built = loadRoutes().associateBy { it.routeId }
        cachedRoutesById = built
        return built
    }

    private fun getStopTimesByTrip(): Map<String, List<GtfsStopTime>> {
        cachedStopTimesByTrip?.let { return it }

        val built = loadStopTimes()
            .groupBy { it.tripId }
            .mapValues { (_, stopTimes) -> stopTimes.sortedBy { it.stopSequence } }

        cachedStopTimesByTrip = built
        return built
    }

    fun findStopById(stopId: String): GtfsStop? {
        return loadStops().firstOrNull { it.stopId == stopId }
    }

    fun getShapePoints(shapeId: String?, startStop: GtfsStop, endStop: GtfsStop): List<GeoPoint> {
        if (shapeId == null) return emptyList()
        val allShapes = loadShapes()
        val shape = allShapes[shapeId] ?: return emptyList()

        // Find indices of points closest to our stops to clip the shape
        var startIndex = -1
        var minStartDist = Double.MAX_VALUE
        var endIndex = -1
        var minEndDist = Double.MAX_VALUE

        shape.forEachIndexed { index, pt ->
            val dStart = distanceMeters(pt.latitude, pt.longitude, startStop.stopLat, startStop.stopLon)
            if (dStart < minStartDist) {
                minStartDist = dStart
                startIndex = index
            }
            val dEnd = distanceMeters(pt.latitude, pt.longitude, endStop.stopLat, endStop.stopLon)
            if (dEnd < minEndDist) {
                minEndDist = dEnd
                endIndex = index
            }
        }

        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return shape.subList(startIndex, endIndex + 1)
        }

        return emptyList()
    }

    fun minutesBetweenTimes(startTime: String, endTime: String): Int {
        fun toMinutes(value: String): Int {
            val parts = value.split(":")
            if (parts.size < 2) return 0
            val hours = parts[0].toIntOrNull() ?: 0
            val minutes = parts[1].toIntOrNull() ?: 0
            return hours * 60 + minutes
        }

        return toMinutes(endTime) - toMinutes(startTime)
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when (char) {
                '"' -> inQuotes = !inQuotes
                ',' -> {
                    if (inQuotes) {
                        current.append(char)
                    } else {
                        result.add(current.toString())
                        current.clear()
                    }
                }
                else -> current.append(char)
            }
        }

        result.add(current.toString())
        return result
    }

    private fun cleanStopName(name: String): String {
        return name
            .split(" - ")
            .first()
            .trim()
    }

    suspend fun findBestRouteOptions(
        startLat: Double,
        startLon: Double,
        destLat: Double,
        destLon: Double
    ): List<GtfsRouteOption> = withContext(Dispatchers.IO) {
        val allStops = loadStops()

        val startStops = allStops
            .sortedBy { distanceMeters(startLat, startLon, it.stopLat, it.stopLon) }
            .take(15)

        val endStops = allStops
            .sortedBy { distanceMeters(destLat, destLon, it.stopLat, it.stopLon) }
            .take(15)

        val options = mutableListOf<GtfsRouteOption>()

        for (sStop in startStops) {
            for (eStop in endStops) {
                val direct = findDirectTripBetweenStops(sStop.stopId, eStop.stopId)

                if (direct != null) {
                    val walkTo = (distanceMeters(startLat, startLon, sStop.stopLat, sStop.stopLon) / 80.0)
                        .toInt()
                        .coerceAtLeast(1)

                    val rawBusMin = minutesBetweenTimes(direct.startDepartureTime, direct.endArrivalTime)
                    val busMin = if (rawBusMin > 0) rawBusMin else 15

                    val walkFrom = (distanceMeters(eStop.stopLat, eStop.stopLon, destLat, destLon) / 80.0)
                        .toInt()
                        .coerceAtLeast(1)

                    options.add(
                        GtfsRouteOption(
                            routeShortName = direct.routeShortName,
                            startStop = sStop.copy(stopName = cleanStopName(sStop.stopName)),
                            endStop = eStop.copy(stopName = cleanStopName(eStop.stopName)),
                            walkToStopMinutes = walkTo,
                            busMinutes = busMin,
                            walkToDestMinutes = walkFrom,
                            totalMinutes = walkTo + busMin + walkFrom,
                            tripId = direct.tripId,
                            shapeId = direct.shapeId
                        )
                    )
                }
            }
        }

        options
            .distinctBy { "${it.routeShortName}_${it.startStop.stopId}_${it.endStop.stopId}" }
            .sortedBy { it.totalMinutes + (it.walkToStopMinutes + it.walkToDestMinutes) * 0.5 }
            .take(3)
    }
}
