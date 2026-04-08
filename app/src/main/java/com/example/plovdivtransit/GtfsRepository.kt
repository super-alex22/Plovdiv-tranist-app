package com.example.plovdivtransit

import android.content.Context
import android.util.Log
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

        val content = readShapesFile()
        if (content.isBlank()) return emptyMap()
        val lines = content.lineSequence().filter { it.isNotBlank() }.toList()
        if (lines.isEmpty()) return emptyMap()

        val headerMap = csvHeaderMap(lines[0])

        val loaded = lines.drop(1)
            .mapNotNull { line ->
                val parts = parseCsv(line)
                val shapeId = value(parts, headerMap, "shape_id") ?: return@mapNotNull null
                val lat = value(parts, headerMap, "shape_pt_lat")?.toDoubleOrNull() ?: return@mapNotNull null
                val lon = value(parts, headerMap, "shape_pt_lon")?.toDoubleOrNull() ?: return@mapNotNull null
                val sequence = value(parts, headerMap, "shape_pt_sequence")?.toIntOrNull() ?: 0

                shapeId to (sequence to GeoPoint(lat, lon))
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { entry ->
                entry.value.sortedBy { it.first }.map { it.second }
            }

        Log.d("GTFS", "loaded shapes count: ${loaded.size}")
        cachedShapes = loaded
        return loaded
    }

    fun loadTrips(): List<GtfsTrip> {
        cachedTrips?.let { return it }

        val content = readTripsFile()
        if (content.isBlank()) return emptyList()
        val lines = content.lineSequence().filter { it.isNotBlank() }.toList()
        if (lines.isEmpty()) return emptyList()

        val headerMap = csvHeaderMap(lines[0])

        val loaded = lines.drop(1)
            .mapNotNull { line ->
                val parts = parseCsv(line)
                GtfsTrip(
                    routeId = value(parts, headerMap, "route_id") ?: return@mapNotNull null,
                    tripId = value(parts, headerMap, "trip_id") ?: return@mapNotNull null,
                    shapeId = value(parts, headerMap, "shape_id")
                )
            }
            .toList()

        Log.d("GTFS", "loaded trips count: ${loaded.size}")
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

        val content = readRoutesFile()
        if (content.isBlank()) return emptyList()
        val lines = content.lineSequence().filter { it.isNotBlank() }.toList()
        if (lines.isEmpty()) return emptyList()

        val headerMap = csvHeaderMap(lines[0])

        val loaded = lines.drop(1)
            .mapNotNull { line ->
                val parts = parseCsv(line)
                GtfsRoute(
                    routeId = value(parts, headerMap, "route_id") ?: return@mapNotNull null,
                    routeShortName = value(parts, headerMap, "route_short_name") ?: ""
                )
            }
            .toList()

        Log.d("GTFS", "loaded routes count: ${loaded.size}")
        cachedRoutes = loaded
        return loaded
    }

    fun loadStopTimes(): List<GtfsStopTime> {
        cachedStopTimes?.let { return it }

        val content = readStopTimesFile()
        if (content.isBlank()) return emptyList()
        val lines = content.lineSequence().filter { it.isNotBlank() }.toList()
        if (lines.isEmpty()) return emptyList()

        val headerMap = csvHeaderMap(lines[0])

        val loaded = lines.drop(1)
            .mapNotNull { line ->
                val parts = parseCsv(line)
                val tripId = value(parts, headerMap, "trip_id") ?: return@mapNotNull null
                val stopId = value(parts, headerMap, "stop_id") ?: return@mapNotNull null
                val arrivalTime = value(parts, headerMap, "arrival_time") ?: return@mapNotNull null
                val departureTime = value(parts, headerMap, "departure_time") ?: return@mapNotNull null
                val stopSequence = value(parts, headerMap, "stop_sequence")?.toIntOrNull() ?: return@mapNotNull null

                GtfsStopTime(
                    tripId = tripId,
                    arrivalTime = arrivalTime,
                    departureTime = departureTime,
                    stopId = stopId,
                    stopSequence = stopSequence
                )
            }
            .toList()

        Log.d("GTFS", "loaded stop_times count: ${loaded.size}")
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

        val content = readStopsFile()
        if (content.isBlank()) return emptyList()
        val lines = content.lineSequence().filter { it.isNotBlank() }.toList()
        if (lines.isEmpty()) return emptyList()

        val headerMap = csvHeaderMap(lines[0])

        val loaded = lines.drop(1)
            .mapNotNull { line ->
                val parts = parseCsv(line)
                val stopId = value(parts, headerMap, "stop_id") ?: return@mapNotNull null
                val stopCode = value(parts, headerMap, "stop_code")?.ifBlank { null }
                val stopName = value(parts, headerMap, "stop_name") ?: return@mapNotNull null
                val stopLat = value(parts, headerMap, "stop_lat")?.toDoubleOrNull() ?: return@mapNotNull null
                val stopLon = value(parts, headerMap, "stop_lon")?.toDoubleOrNull() ?: return@mapNotNull null

                GtfsStop(
                    stopId = stopId,
                    stopCode = stopCode,
                    stopName = stopName,
                    stopLat = stopLat,
                    stopLon = stopLon
                )
            }
            .toList()

        Log.d("GTFS", "loaded stops count: ${loaded.size}")
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

    private fun parseCsv(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when (char) {
                '\"' -> inQuotes = !inQuotes
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

    private fun csvHeaderMap(headerLine: String): Map<String, Int> {
        return parseCsv(headerLine)
            .mapIndexed { index, s -> s.replace("\"", "").trim() to index }
            .toMap()
    }

    private fun value(parts: List<String>, headerMap: Map<String, Int>, columnName: String): String? {
        val index = headerMap[columnName] ?: return null
        if (index >= parts.size) return null
        return parts[index].replace("\"", "").trim()
    }

    private fun cleanStopName(name: String): String {
        return name
            .split(" - ")
            .first()
            .trim()
    }

    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371e3
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaPhi = Math.toRadians(lat2 - lat1)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
                Math.cos(phi1) * Math.cos(phi2) *
                Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return r * c
    }

    suspend fun findBestRouteOptions(
        startLat: Double,
        startLon: Double,
        destLat: Double,
        destLon: Double
    ): List<GtfsRouteOption> = withContext(Dispatchers.IO) {
        Log.d("GTFS_ROUTE", "Planning route from ($startLat, $startLon) to ($destLat, $destLon)")
        val allStops = loadStops()
        Log.d("GTFS_ROUTE", "Total stops loaded: ${allStops.size}")

        val startStops = allStops
            .map { it to distanceMeters(startLat, startLon, it.stopLat, it.stopLon) }
            .sortedBy { it.second }
            .take(15)
        
        Log.d("GTFS_ROUTE", "Nearby start stops found: ${startStops.size}")

        val endStops = allStops
            .map { it to distanceMeters(destLat, destLon, it.stopLat, it.stopLon) }
            .sortedBy { it.second }
            .take(15)
            
        Log.d("GTFS_ROUTE", "Nearby destination stops found: ${endStops.size}")

        val options = mutableListOf<GtfsRouteOption>()
        var pairsChecked = 0
        var directTripsFound = 0

        for ((sStop, sDist) in startStops) {
            for ((eStop, eDist) in endStops) {
                pairsChecked++
                // Log.v("GTFS_ROUTE", "Checking pair: ${sStop.stopName} -> ${eStop.stopName}")
                val direct = findDirectTripBetweenStops(sStop.stopId, eStop.stopId)

                if (direct != null) {
                    directTripsFound++
                    Log.d("GTFS_ROUTE", "Found direct trip: Bus ${direct.routeShortName} from ${sStop.stopName} to ${eStop.stopName}")
                    val walkTo = (sDist / 80.0).toInt().coerceAtLeast(1)

                    val rawBusMin = minutesBetweenTimes(direct.startDepartureTime, direct.endArrivalTime)
                    val busMin = if (rawBusMin > 0) rawBusMin else 15

                    val walkFrom = (eDist / 80.0).toInt().coerceAtLeast(1)

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

        Log.d("GTFS_ROUTE", "Search completed. Pairs checked: $pairsChecked, Direct trips found: $directTripsFound")

        val result = options
            .distinctBy { "${it.routeShortName}_${it.startStop.stopId}_${it.endStop.stopId}" }
            .sortedBy { it.totalMinutes + (it.walkToStopMinutes + it.walkToDestMinutes) * 0.5 }
            .take(3)
        
        Log.d("GTFS_ROUTE", "Final route options count: ${result.size}")
        result
    }
}
