package com.example.plovdivtransit
import kotlin.math.*

enum class GuidancePhase {
    NoRoute,             // 0
    WalkingToFirstStop,  // 1
    WaitingForVehicle,   // 2
    RidingVehicle,       // 3
    WalkingTransfer,     // 4
    WaitingForTransferVehicle, // 5
    FinalWalk,           // 6
    Arrived              // 7
}

data class GuidanceStep(
    val phase: GuidancePhase,
    val title: String,
    val subtitle: String,
    val mainIcon: String = "walk",
    val progress: Float = 0f,
    val segmentIndex: Int = 0 // Добавляем индекс текущего сегмента
) {
    // Вычисляем абсолютный вес прогресса для сравнения
    fun getProgressWeight(): Int {
        if (phase == GuidancePhase.Arrived) return Int.MAX_VALUE
        if (phase == GuidancePhase.FinalWalk) return Int.MAX_VALUE - 1
        if (phase == GuidancePhase.NoRoute) return 0

        // Каждый сегмент весит 10 единиц + порядковый номер фазы внутри него
        return (segmentIndex * 10) + phase.ordinal
    }
}

object GuidanceEngine {
    private const val ARRIVED_RADIUS_METERS = 50.0
    private const val WAIT_RADIUS_METERS = 50.0
    private const val RETURN_TO_WALK_RADIUS_METERS = 90.0
    private const val FINAL_WALK_TRIGGER_METERS = 70.0
    private const val RIDE_CORRIDOR_RADIUS_METERS = 100.0
    fun resolve(
        route: RouteOption?,
        userLoc: UserLocation?,
        destination: PlaceSearchResult?,
        previousStep: GuidanceStep? = null,
        previousUserLoc: UserLocation? = null
    ): GuidanceStep {
        if (route == null || destination == null || userLoc == null) {
            return previousStep ?: GuidanceStep(
                GuidancePhase.NoRoute,
                "Waiting...",
                "GPS/Route needed"
            )
        }

        val segments = route.segments
        if (segments.isEmpty()) {
            return GuidanceStep(GuidancePhase.NoRoute, "Error", "No segments")
        }

        val potentialStep = findBestMatchForLocation(
            route = route,
            userLoc = userLoc,
            destination = destination,
            previousStep = previousStep,
            previousUserLoc = previousUserLoc
        )

        if (previousStep == null || previousStep.phase == GuidancePhase.NoRoute) {
            return potentialStep
        }

        val previousSegment = segments[previousStep.segmentIndex.coerceIn(0, segments.lastIndex)]

        val distToPreviousStart = distanceBetween(
            userLoc.lat,
            userLoc.lon,
            previousSegment.fromStop.stopLat,
            previousSegment.fromStop.stopLon
        )

        val distToPreviousLine = distanceToSegment(
            userLoc.lat,
            userLoc.lon,
            previousSegment.fromStop.stopLat,
            previousSegment.fromStop.stopLon,
            previousSegment.toStop.stopLat,
            previousSegment.toStop.stopLon
        )

        val allowBackToWalkFromWait =
            previousStep.phase == GuidancePhase.WaitingForVehicle &&
                    distToPreviousStart > RETURN_TO_WALK_RADIUS_METERS &&
                    distToPreviousLine > RIDE_CORRIDOR_RADIUS_METERS &&
                    potentialStep.phase == GuidancePhase.WalkingToFirstStop

        val allowBackToTransferWalk =
            previousStep.phase == GuidancePhase.WaitingForTransferVehicle &&
                    distToPreviousStart > RETURN_TO_WALK_RADIUS_METERS &&
                    distToPreviousLine > RIDE_CORRIDOR_RADIUS_METERS &&
                    potentialStep.phase == GuidancePhase.WalkingTransfer

        return when {
            allowBackToWalkFromWait -> potentialStep
            allowBackToTransferWalk -> potentialStep
            potentialStep.getProgressWeight() >= previousStep.getProgressWeight() -> potentialStep
            else -> previousStep
        }
    }

    private fun findBestMatchForLocation(
        route: RouteOption,
        userLoc: UserLocation,
        destination: PlaceSearchResult,
        previousStep: GuidanceStep? = null,
        previousUserLoc: UserLocation? = null
    ): GuidanceStep {
        val segments = route.segments

        val distToDest = distanceBetween(
            userLoc.lat,
            userLoc.lon,
            destination.lat,
            destination.lon
        )

        if (distToDest < ARRIVED_RADIUS_METERS) {
            return GuidanceStep(
                GuidancePhase.Arrived,
                "You have arrived!",
                destination.name,
                "flag",
                1f,
                segments.lastIndex.coerceAtLeast(0)
            )
        }

        val currentIndex = when {
            previousStep == null || previousStep.phase == GuidancePhase.NoRoute -> 0

            previousStep.phase == GuidancePhase.WalkingTransfer ||
                    previousStep.phase == GuidancePhase.WaitingForTransferVehicle -> {
                (previousStep.segmentIndex + 1).coerceIn(0, segments.lastIndex)
            }

            else -> previousStep.segmentIndex.coerceIn(0, segments.lastIndex)
        }

        val candidateIndices = buildList {
            add(currentIndex)
            if (currentIndex + 1 <= segments.lastIndex) add(currentIndex + 1)
        }.distinct()

        for (i in candidateIndices) {
            val seg = segments[i]

            val distToStart = distanceBetween(
                userLoc.lat,
                userLoc.lon,
                seg.fromStop.stopLat,
                seg.fromStop.stopLon
            )

            val distToEnd = distanceBetween(
                userLoc.lat,
                userLoc.lon,
                seg.toStop.stopLat,
                seg.toStop.stopLon
            )

            val distToLine = distanceToSegment(
                userLoc.lat,
                userLoc.lon,
                seg.fromStop.stopLat,
                seg.fromStop.stopLon,
                seg.toStop.stopLat,
                seg.toStop.stopLon
            )

            val movedMeters = if (previousUserLoc != null) {
                distanceBetween(
                    previousUserLoc.lat,
                    previousUserLoc.lon,
                    userLoc.lat,
                    userLoc.lon
                )
            } else {
                0.0
            }

            if (distToEnd < FINAL_WALK_TRIGGER_METERS) {
                return if (i < segments.lastIndex) {
                    GuidanceStep(
                        GuidancePhase.WalkingTransfer,
                        "Transfer stop",
                        seg.toStop.stopName,
                        "walk",
                        0.8f,
                        i
                    )
                } else {
                    GuidanceStep(
                        GuidancePhase.FinalWalk,
                        "Final walk",
                        "To destination",
                        "walk",
                        0.9f,
                        i
                    )
                }
            }

            val waitAllowed = when {
                previousStep == null || previousStep.phase == GuidancePhase.NoRoute -> i == 0
                previousStep.phase == GuidancePhase.WaitingForVehicle ||
                        previousStep.phase == GuidancePhase.WalkingToFirstStop -> i == 0
                previousStep.phase == GuidancePhase.WalkingTransfer ||
                        previousStep.phase == GuidancePhase.WaitingForTransferVehicle -> {
                    i == previousStep.segmentIndex.coerceIn(0, segments.lastIndex)
                }
                else -> false
            }

            if (waitAllowed && distToStart <= WAIT_RADIUS_METERS) {
                val phase = if (i == 0) {
                    GuidancePhase.WaitingForVehicle
                } else {
                    GuidancePhase.WaitingForTransferVehicle
                }

                return GuidanceStep(
                    phase,
                    "Wait for Bus ${seg.routeShortName}",
                    "At ${seg.fromStop.stopName}",
                    "wait",
                    0.3f,
                    i
                )
            }

            val isNearRouteLine = distToLine <= RIDE_CORRIDOR_RADIUS_METERS
            val movedEnough = movedMeters >= 25.0

            val canStartRiding =
                (
                        previousStep?.phase == GuidancePhase.WaitingForVehicle ||
                                previousStep?.phase == GuidancePhase.WaitingForTransferVehicle ||
                                (
                                        previousStep?.phase == GuidancePhase.WalkingTransfer &&
                                                distToStart > WAIT_RADIUS_METERS
                                        )
                        ) &&
                        movedEnough &&
                        isNearRouteLine &&
                        distToStart > WAIT_RADIUS_METERS + 10.0 &&
                        distToEnd > 30.0

            val stayRiding =
                previousStep?.phase == GuidancePhase.RidingVehicle &&
                        isNearRouteLine &&
                        distToEnd > 30.0 &&
                        distToStart > WAIT_RADIUS_METERS + 10.0 &&
                        movedMeters >= 8.0
            val leftRouteWhileRiding =
                previousStep?.phase == GuidancePhase.RidingVehicle &&
                        (!isNearRouteLine || distToStart <= WAIT_RADIUS_METERS)

            if (leftRouteWhileRiding) {
                return GuidanceStep(
                    GuidancePhase.WalkingToFirstStop,
                    "Walk to stop",
                    seg.fromStop.stopName,
                    "walk",
                    0.15f,
                    i
                )
            }
            if (canStartRiding || stayRiding) {
                return GuidanceStep(
                    GuidancePhase.RidingVehicle,
                    "Riding Bus ${seg.routeShortName}",
                    "To ${seg.toStop.stopName}",
                    "bus",
                    0.6f,
                    i
                )
            }
        }

        return if (
            previousStep?.phase == GuidancePhase.WalkingTransfer ||
            previousStep?.phase == GuidancePhase.WaitingForTransferVehicle
        ) {
            val nextIdx = (previousStep.segmentIndex + 1).coerceIn(0, segments.lastIndex)

            GuidanceStep(
                GuidancePhase.WalkingTransfer,
                "Walk to transfer stop",
                segments[nextIdx].fromStop.stopName,
                "walk",
                0.5f,
                nextIdx
            )
        } else {
            GuidanceStep(
                GuidancePhase.WalkingToFirstStop,
                "Walk to stop",
                segments[0].fromStop.stopName,
                "walk",
                0.15f,
                0
            )
        }
    }
    private fun distanceToSegment(
        lat: Double, lon: Double,
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {

        val k = 0.74

        val px = lon * k
        val py = lat

        val x1 = lon1 * k
        val y1 = lat1
        val x2 = lon2 * k
        val y2 = lat2

        val dx = x2 - x1
        val dy = y2 - y1
        val lenSq = dx * dx + dy * dy

        if (lenSq == 0.0) return distanceBetween(lat, lon, y1, x1 / k)

        val t = ((px - x1) * dx + (py - y1) * dy) / lenSq

        val (closestX, closestY) = when {
            t < 0 -> Pair(x1, y1)
            t > 1 -> Pair(x2, y2)
            else -> Pair(x1 + t * dx, y1 + t * dy)
        }

        return distanceBetween(lat, lon, closestY, closestX / k)
    }


    private fun walkingMinutes(distanceMeters: Double): Int {
        return max(1, (distanceMeters / 75.0).roundToInt())
    }

    private fun distanceBetween(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadius = 6371000.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}