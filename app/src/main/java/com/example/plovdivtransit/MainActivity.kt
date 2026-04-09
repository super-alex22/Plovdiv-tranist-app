package com.example.plovdivtransit

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.plovdivtransit.ui.theme.PlovdivTransitTheme
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline


enum class AppScreen {
    Welcome,
    Login,
    Register,
    Home,
    Search,
    Routes,
    Go,
    Profile
}
data class UserLocation(
    val lat: Double,
    val lon: Double
)
private val GRAND_HOTEL_PLOVDIV = UserLocation(42.1557, 24.7461)
data class BusStop(
    val name: String,
    val lat: Double,
    val lon: Double
)

val sampleBusStops = listOf(
    BusStop("Central Station Stop", 42.1418, 24.7314),
    BusStop("Mall Plovdiv Stop", 42.1360, 24.7198),
    BusStop("Opera Stop", 42.1432, 24.7488),
    BusStop("Main Square Stop", 42.1467, 24.7495),
    BusStop("Old Town Stop", 42.1495, 24.7523)
)
fun distanceBetween(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val result = FloatArray(1)
    android.location.Location.distanceBetween(
        lat1, lon1, lat2, lon2, result
    )
    return result[0].toDouble()
}
fun walkingMinutes(distanceMeters: Double): Int {
    val metersPerMinute = 80.0
    return kotlin.math.ceil(distanceMeters / metersPerMinute).toInt().coerceAtLeast(1)
}
fun minutesBetweenTimes(startTime: String, endTime: String): Int {
    fun parseToMinutes(value: String): Int {
        val parts = value.split(":")
        if (parts.size < 2) return 0

        val hours = parts[0].toIntOrNull() ?: 0
        val minutes = parts[1].toIntOrNull() ?: 0

        return hours * 60 + minutes
    }

    val start = parseToMinutes(startTime)
    val end = parseToMinutes(endTime)

    val diff = end - start

    return when {
        diff <= 0 -> 2
        else -> diff
    }
}
fun formatDelayMinutes(delayMs: Long?): String {
    if (delayMs == null) return "Live"
    val min = kotlin.math.abs(delayMs) / 60000
    return when {
        delayMs > 0 -> "Delay ${min} min"
        delayMs < 0 -> "Early ${min} min"
        else -> "On time"
    }
}
fun findNearestGtfsStops(
    lat: Double,
    lon: Double,
    stops: List<GtfsStop>,
    limit: Int = 5
): List<GtfsStop> {
    return stops
        .sortedBy { stop ->
            distanceBetween(lat, lon, stop.stopLat, stop.stopLon)
        }
        .take(limit)
}
fun findNearestGtfsStop(
    lat: Double,
    lon: Double,
    stops: List<GtfsStop>
): GtfsStop? {
    return stops.minByOrNull { stop ->
        distanceBetween(lat, lon, stop.stopLat, stop.stopLon)
    }
}
fun findNearestStop(
    lat: Double,
    lon: Double,
    stops: List<BusStop>
): BusStop? {
    return stops.minByOrNull { stop ->
        distanceBetween(lat, lon, stop.lat, stop.lon)
    }
}
@SuppressLint("MissingPermission", "UseKtx")
@Composable
fun HomeMapScreen(
    userLocation: UserLocation?,
    onUserLocationChanged: (UserLocation) -> Unit
) {
    val context = LocalContext.current
    val displayLocation = remember(userLocation) {
        userLocation?.let { GeoPoint(it.lat, it.lon) }
            ?: GeoPoint(42.1354, 24.7453)
    }

    DisposableEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
        onDispose { }
    }

    AndroidView(
        factory = {
            MapView(it).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            }
        },
        update = { mapView ->
            mapView.controller.setZoom(15.5)

            mapView.overlays.clear()

            mapView.controller.setCenter(displayLocation)

            val marker = Marker(mapView)
            marker.position = displayLocation
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = if (userLocation != null) "Your location" else "Default location"
            mapView.overlays.add(marker)
            mapView.invalidate()
        },
        modifier = Modifier.fillMaxSize()
    )
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PlovdivTransitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authManager = remember { AuthManager() }

                    var currentScreen by remember {
                        mutableStateOf(
                            if (authManager.isUserLoggedIn()) AppScreen.Home else AppScreen.Welcome
                        )
                    }
                    var selectedPlace by remember {
                        mutableStateOf<PlaceSearchResult?>(null)
                    }
                    var userLocation by remember {
                        mutableStateOf<UserLocation?>(null)
                    }
                    var activeRoute by remember {
                        mutableStateOf<RouteOption?>(null)
                    }

                    AppScaffold(
                        currentScreen = currentScreen,
                        authManager = authManager,
                        selectedPlace = selectedPlace,
                        onPlaceSelected = { selectedPlace = it },
                        userLocation = userLocation,
                        onUserLocationChanged = { userLocation = it },
                        activeRoute = activeRoute,
                        onRouteSelected = { activeRoute = it },
                        onScreenSelected = { currentScreen = it },
                        onLogout = {
                            authManager.logout()
                            currentScreen = AppScreen.Welcome
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppScaffold(
    currentScreen: AppScreen,
    authManager: AuthManager,
    selectedPlace: PlaceSearchResult?,
    onPlaceSelected: (PlaceSearchResult) -> Unit,
    userLocation: UserLocation?,
    onUserLocationChanged: (UserLocation) -> Unit,
    activeRoute: RouteOption?,
    onRouteSelected: (RouteOption?) -> Unit,
    onScreenSelected: (AppScreen) -> Unit,
    onLogout: () -> Unit
) {

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // 1. Управление разрешениями на уровне Scaffold
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { hasLocationPermission = it }
    val locationRequest = remember {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateIntervalMillis(2000L)
            .build()
    }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    onUserLocationChanged(
                        UserLocation(location.latitude, location.longitude)
                    )
                }
            }
        }
    }

    DisposableEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    android.os.Looper.getMainLooper()
                )
            } catch (e: SecurityException) { }
        }

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            when (currentScreen) {
                AppScreen.Search -> SearchDestinationScreen(
                    userLat = userLocation?.lat,
                    userLon = userLocation?.lon,
                    onBack = { onScreenSelected(AppScreen.Home) },
                    onPlaceSelected = { place ->
                        onPlaceSelected(place)
                        onScreenSelected(AppScreen.Routes)
                    }
                )
                AppScreen.Home -> HomeScreen(
                    userLocation = userLocation,
                    onPlanRoute = { onScreenSelected(AppScreen.Search) },
                    onUserLocationChanged = onUserLocationChanged
                )
                AppScreen.Routes -> SuggestedRoutesScreen(
                    destination = selectedPlace,
                    userLocation = userLocation,
                    onBack = { onScreenSelected(AppScreen.Home) },
                    onRouteSelected = { onRouteSelected(it) },
                    onGo = { onScreenSelected(AppScreen.Go) }
                )
                AppScreen.Go -> GoScreen(
                    selectedRoute = activeRoute,
                    destination = selectedPlace,
                    userLocation = userLocation,
                    onBack = {
                        onRouteSelected(null)
                        onScreenSelected(AppScreen.Home)
                    },
                    onCancelNavigation = {
                        onRouteSelected(null)
                        onScreenSelected(AppScreen.Home)
                    }
                )
                AppScreen.Profile -> ProfileScreen(
                    authManager = authManager,
                    onSignIn = { onScreenSelected(AppScreen.Login) },
                    onCreateAccount = { onScreenSelected(AppScreen.Register) },
                    onLogout = onLogout
                )
                AppScreen.Welcome,
                AppScreen.Login,
                AppScreen.Register -> {}
            }
        }

        BottomNavBar(
            currentScreen = currentScreen,
            onScreenSelected = onScreenSelected
        )
    }
}
@Composable
fun BottomNavBar(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = Color.White
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.Home,
            onClick = { onScreenSelected(AppScreen.Home) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.Routes,
            onClick = { onScreenSelected(AppScreen.Routes) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Route,
                    contentDescription = "Routes"
                )
            },
            label = { Text("Routes") }
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.Go,
            onClick = { onScreenSelected(AppScreen.Go) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.DirectionsBus,
                    contentDescription = "Go"
                )
            },
            label = { Text("Go") }
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.Profile,
            onClick = { onScreenSelected(AppScreen.Profile) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile") }
        )
    }
}

@Composable
fun WelcomeScreen(
    onContinueWithoutAccount: () -> Unit,
    onSignIn: () -> Unit,
    onCreateAccount: () -> Unit
) {
    val background = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF8FAFC),
            Color(0xFFF8FAFC),
            Color(0xFFF1F5F9)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Plovdiv Transit",
                color = Color(0xFF0F172A),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Smart public transport for Plovdiv",
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Move through Plovdiv without guessing",
                color = Color(0xFF0F172A),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Live buses, smart routes, and simple trip guidance from your location to the right stop.",
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(28.dp))

            AnimatedWelcomePreviewCard()

            Spacer(modifier = Modifier.height(24.dp))

            BenefitRow("Real buses near you")
            Spacer(modifier = Modifier.height(10.dp))
            BenefitRow("Suggested routes on map and as a list")
            Spacer(modifier = Modifier.height(10.dp))
            BenefitRow("Smart alerts during your trip")
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = onCreateAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Get started")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onContinueWithoutAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Continue without account")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onSignIn,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Sign in",
                    color = Color(0xFF0F172A)
                )
            }
        }
    }
}
private enum class WelcomePreviewPage {
    LIVE_BUSES,
    ROUTES,
    ALERTS
}

@Composable
fun AnimatedWelcomePreviewCard() {
    var currentPage by remember { mutableStateOf(WelcomePreviewPage.LIVE_BUSES) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2200)
            currentPage = when (currentPage) {
                WelcomePreviewPage.LIVE_BUSES -> WelcomePreviewPage.ROUTES
                WelcomePreviewPage.ROUTES -> WelcomePreviewPage.ALERTS
                WelcomePreviewPage.ALERTS -> WelcomePreviewPage.LIVE_BUSES
            }
        }
    }

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "What the app helps with",
                color = Color(0xFF0F172A),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = currentPage,
                label = "welcome_preview"
            ) { page ->
                when (page) {
                    WelcomePreviewPage.LIVE_BUSES -> LiveBusesPreview()
                    WelcomePreviewPage.ROUTES -> RoutesPreview()
                    WelcomePreviewPage.ALERTS -> AlertsPreview()
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DotIndicator(currentPage == WelcomePreviewPage.LIVE_BUSES)
                DotIndicator(currentPage == WelcomePreviewPage.ROUTES)
                DotIndicator(currentPage == WelcomePreviewPage.ALERTS)
            }
        }
    }
}

@Composable
fun LiveBusesPreview() {
    val infiniteTransition = rememberInfiniteTransition(label = "bus_animation")
    val busOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 220f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bus_offset"
    )

    Column {
        Text(
            text = "See buses moving live on the map",
            color = Color(0xFF0F172A),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Quickly understand what is near you and what is arriving soon.",
            color = Color(0xFFCBD5E1),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFF1F5F9))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val y = size.height * 0.55f

                drawRoundRect(
                    color = androidx.compose.ui.graphics.Color(0xFFE2E8F0),
                    cornerRadius = CornerRadius(24f, 24f)
                )

                drawLine(
                    color = androidx.compose.ui.graphics.Color(0xFF94A3B8),
                    start = Offset(24f, y),
                    end = Offset(size.width - 24f, y),
                    strokeWidth = 10f,
                    cap = StrokeCap.Round
                )

                listOf(40f, size.width * 0.45f, size.width - 60f).forEach { stopX ->
                    drawCircle(
                        color = androidx.compose.ui.graphics.Color.White,
                        radius = 12f,
                        center = Offset(stopX, y)
                    )
                    drawCircle(
                        color = androidx.compose.ui.graphics.Color(0xFF64748B),
                        radius = 6f,
                        center = Offset(stopX, y)
                    )
                }

                drawRoundRect(
                    color = androidx.compose.ui.graphics.Color(0xFF0F172A),
                    topLeft = Offset(16f + busOffset.coerceAtMost(size.width - 90f), y - 20f),
                    size = Size(60f, 40f),
                    cornerRadius = CornerRadius(14f, 14f)
                )
            }
        }
    }
}

@Composable
fun RoutesPreview() {
    Column {
        Text(
            text = "Compare route options instantly",
            color = Color(0xFF0F172A),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Choose the fastest trip or the option with less walking.",
            color = Color(0xFFCBD5E1),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(18.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PreviewRouteCard(
                title = "Fastest",
                duration = "18 min",
                isHighlighted = true
            )
            PreviewRouteCard(
                title = "Less walking",
                duration = "21 min",
                isHighlighted = false
            )
        }
    }
}
fun formatDurationMinutes(durationSeconds: Double): String {
    val minutes = (durationSeconds / 60.0).toInt()
    return "$minutes min"
}
@Composable
fun AlertsPreview() {
    val infiniteTransition = rememberInfiniteTransition(label = "alerts_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alert_alpha"
    )

    Column {
        Text(
            text = "Get smart trip alerts",
            color = Color(0xFF0F172A),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "The app can guide you step by step during the ride.",
            color = Color(0xFFCBD5E1),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(18.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AlertMiniCard(
                text = "Walk to Bus Stop Opera",
                background = Color(0x22FFFFFF),
                textColor = Color(0xFF0F172A),
                alpha = 1f
            )
            AlertMiniCard(
                text = "Bus 19 is arriving",
                background = Color(0xFFFFEDD5),
                textColor = Color(0xFF9A3412),
                alpha = alpha
            )
            AlertMiniCard(
                text = "Get off in 2 stops",
                background = Color(0xFFDCFCE7),
                textColor = Color(0xFF166534),
                alpha = 1f
            )
        }
    }
}

@Composable
fun PreviewRouteCard(
    title: String,
    duration: String,
    isHighlighted: Boolean
) {
    val containerColor = if (isHighlighted) Color.White else Color(0x22FFFFFF)
    val titleColor = if (isHighlighted) Color(0xFF0F172A) else Color.White
    val subColor = if (isHighlighted) Color(0xFF64748B) else Color(0xFFCBD5E1)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    color = titleColor,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Bus + walking",
                    color = subColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = duration,
                color = titleColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RouteMapPreview(
    destination: PlaceSearchResult?,
    userLocation: UserLocation?,
    routeData: RouteData?,
    selectedRoute: RouteOption? = null,
    gtfsRepository: GtfsRepository? = null,
    liveVehicles: List<LiveVehicle> = emptyList()
){
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            RouteMapScreen(
                destination = destination,
                userLocation = userLocation,
                routeData = routeData,
                selectedRoute = selectedRoute,
                gtfsRepository = gtfsRepository,
                liveVehicles = liveVehicles
            )
        }
    }
}

@SuppressLint("UseKtx")
@Composable
fun RouteMapScreen(
    destination: PlaceSearchResult?,
    userLocation: UserLocation?,
    routeData: RouteData?,
    selectedRoute: RouteOption? = null,
    gtfsRepository: GtfsRepository? = null,
    liveVehicles: List<LiveVehicle> = emptyList(),
    focusOnUser: Boolean = false
) {
    val context = LocalContext.current

    val fromPoint = if (userLocation != null) {
        GeoPoint(userLocation.lat, userLocation.lon)
    } else {
        GeoPoint(42.1354, 24.7453)
    }

    val toPoint = if (destination != null) {
        GeoPoint(destination.lat, destination.lon)
    } else {
        GeoPoint(42.1419, 24.7316)
    }

    DisposableEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
        onDispose { }
    }

    AndroidView(
        factory = {
            MapView(it).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()

            if (fromPoint == null) return@AndroidView

            val segments = selectedRoute?.segments ?: emptyList()

            if (segments.isNotEmpty()) {
                var currentPoint = fromPoint

                segments.forEach { segment ->
                    val startStop = GeoPoint(
                        segment.fromStop.stopLat,
                        segment.fromStop.stopLon
                    )

                    val endStop = GeoPoint(
                        segment.toStop.stopLat,
                        segment.toStop.stopLon
                    )

                    val walkToStop = Polyline().apply {
                        setPoints(listOf(currentPoint, startStop))
                        outlinePaint.color = android.graphics.Color.GRAY
                        outlinePaint.strokeWidth = 4f
                        outlinePaint.pathEffect =
                            android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f)
                    }
                    mapView.overlays.add(walkToStop)

                    val busPoints =
                        if (gtfsRepository != null && segment.shapeId != null) {
                            gtfsRepository.getShapePoints(
                                segment.shapeId,
                                segment.fromStop,
                                segment.toStop
                            )
                        } else {
                            emptyList()
                        }

                    val busLine = Polyline().apply {
                        setPoints(if (busPoints.isNotEmpty()) busPoints else listOf(startStop, endStop))
                        outlinePaint.color = android.graphics.Color.parseColor("#3B82F6")
                        outlinePaint.strokeWidth = 10f
                    }
                    mapView.overlays.add(busLine)

                    val pickupMarker = Marker(mapView).apply {
                        position = startStop
                        title = "Pickup: ${segment.fromStop.stopName} (Bus ${segment.routeShortName})"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }

                    val dropoffMarker = Marker(mapView).apply {
                        position = endStop
                        title = "Dropoff: ${segment.toStop.stopName}"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }

                    mapView.overlays.add(pickupMarker)
                    mapView.overlays.add(dropoffMarker)

                    currentPoint = endStop
                }

                val finalWalk = Polyline().apply {
                    setPoints(listOf(currentPoint, toPoint))
                    outlinePaint.color = android.graphics.Color.GRAY
                    outlinePaint.strokeWidth = 4f
                    outlinePaint.pathEffect =
                        android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f)
                }
                mapView.overlays.add(finalWalk)

            } else {
                android.util.Log.d(
                    "ROUTING",
                    "No selected GTFS route. Drawing only markers."
                )
            }

            val fromMarker = Marker(mapView).apply {
                position = fromPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Your location"
            }

            val toMarker = Marker(mapView).apply {
                position = toPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = destination?.name ?: "Central Station"
            }

            mapView.overlays.add(fromMarker)
            mapView.overlays.add(toMarker)

            liveVehicles.forEach { vehicle ->
                android.util.Log.d(
                    "LIVE_MAP",
                    "Draw vehicle ${vehicle.line} at ${vehicle.lat}, ${vehicle.lon}"
                )
                val marker = Marker(mapView).apply {
                    position = GeoPoint(vehicle.lat, vehicle.lon)
                    title = buildString {
                        append("Bus ${vehicle.line}")
                        vehicle.destinationEn?.let { append(" → $it") }
                        append("\n")
                        append(formatDelayMinutes(vehicle.delayMs))
                    }
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                }
                mapView.overlays.add(marker)
            }

            if (focusOnUser && userLocation != null) {
                mapView.controller.setZoom(17.0)
                mapView.controller.setCenter(GeoPoint(userLocation.lat, userLocation.lon))
            } else {
                val centerPoint = GeoPoint(
                    (fromPoint.latitude + toPoint.latitude) / 2.0,
                    (fromPoint.longitude + toPoint.longitude) / 2.0
                )
                mapView.controller.setZoom(14.0)
                mapView.controller.setCenter(centerPoint)
            }

            mapView.invalidate()
        },
        modifier = Modifier.fillMaxSize()
    )
}
@Composable
fun AlertMiniCard(
    text: String,
    background: Color,
    textColor: Color,
    alpha: Float
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun BenefitRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(0xFF38BDF8))
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text,
            color = Color(0xFF0F172A),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun DotIndicator(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .size(if (isSelected) 18.dp else 8.dp, 8.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(
                if (isSelected) Color.White else Color(0x55FFFFFF)
            )
    )
}



@Composable
fun BusBadge(line: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Text(
            text = line,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BusCard(
    line: String,
    destination: String,
    eta: String,
    status: String
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Text(
                        text = line,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Bus $line → $destination",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = status,
                        color = Color(0xFF64748B),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = eta,
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "nearby",
                    color = Color(0xFF64748B),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
@Composable
fun SuggestedRoutesScreen(
    destination: PlaceSearchResult?,
    userLocation: UserLocation?,
    onBack: () -> Unit = {},
    onRouteSelected: (RouteOption?) -> Unit,
    onGo: () -> Unit
) {

    val repository = remember { RoutingRepository() }
    val context = LocalContext.current
    val gtfsRepository = remember { GtfsRepository(context) }
    var routeOptions by remember { mutableStateOf<List<RouteOption>>(emptyList()) }
    var selectedRouteIndex by remember { mutableStateOf(0) }
    var isLoadingRoutes by remember { mutableStateOf(false) }
    var routeData by remember { mutableStateOf<RouteData?>(null) }
    val liveRepository = remember { LiveTransportRepository() }
    var liveVehicles by remember { mutableStateOf<List<LiveVehicle>>(emptyList()) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var usedTestLocation by remember { mutableStateOf<UserLocation?>(null) }

    val effectiveLocation = userLocation ?: usedTestLocation
    val fromPoint = effectiveLocation?.let {
        GeoPoint(it.lat, it.lon)
    }

    val toPoint = if (destination != null) {
        GeoPoint(destination.lat, destination.lon)
    } else {
        GeoPoint(42.1419, 24.7316)
    }
    DisposableEffect(Unit) {
        liveRepository.connectPlovdiv(
            onVehiclesUpdated = { vehicles ->
                liveVehicles = vehicles
                android.util.Log.d("LIVE_WS", "Vehicles received: ${vehicles.size}")
            },
            onError = { error ->
                android.util.Log.e("LIVE_WS", "Socket error", error)
            }
        )

        onDispose {
            liveRepository.disconnect()
        }
    }


    LaunchedEffect(destination, effectiveLocation) {
        val currentLocation = effectiveLocation

        if (destination == null || currentLocation == null) {
            android.util.Log.d("ROUTING", "No destination or location, skipping search.")
            routeOptions = emptyList()
            return@LaunchedEffect
        }

        isLoadingRoutes = true
        android.util.Log.d(
            "ROUTING",
            "Searching for GTFS routes from (${currentLocation.lat}, ${currentLocation.lon}) to (${destination.lat}, ${destination.lon})"
        )

        try {
            routeOptions = gtfsRepository.findComplexRouteOptions(
                startLat = currentLocation.lat,
                startLon = currentLocation.lon,
                destLat = destination.lat,
                destLon = destination.lon
            )
            android.util.Log.d("ROUTING", "Found ${routeOptions.size} route options.")
            routeOptions.forEachIndexed { i, opt ->
                android.util.Log.d(
                    "ROUTING",
                    "Option $i: ${opt.segments.size} segment(s), from ${opt.segments.first().fromStop.stopName} to ${opt.segments.last().toStop.stopName} (${opt.totalMinutes} min)"
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("ROUTING", "Error finding GTFS routes", e)
            routeOptions = emptyList()
        } finally {
            isLoadingRoutes = false
        }
    }
    LaunchedEffect(routeOptions) {
        if (routeOptions.isNotEmpty() && selectedRouteIndex !in routeOptions.indices) {
            selectedRouteIndex = 0
        }
    }
    LaunchedEffect(fromPoint, toPoint) {
        if (fromPoint == null) {
            routeData = null
            return@LaunchedEffect
        }

        routeData = try {
            repository.getRouteData(
                fromLat = fromPoint.latitude,
                fromLon = fromPoint.longitude,
                toLat = toPoint.latitude,
                toLon = toPoint.longitude
            )
        } catch (e: Exception) {
            null
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF0F172A)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "Suggested routes",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        FromToCard(
            from = if (userLocation != null) "Your location"
            else if (usedTestLocation != null) "Grand Hotel Plovdiv (test)"
            else "Location not found",
            to = destination?.name ?: "Central Station"
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (effectiveLocation == null) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Location not found",
                        color = Color(0xFF0F172A),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Use Grand Hotel Plovdiv as a test starting point to load route options.",
                        color = Color(0xFF64748B),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            usedTestLocation = GRAND_HOTEL_PLOVDIV
                        },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Use Grand Hotel for testing")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        val selectedLine = routeOptions
            .getOrNull(selectedRouteIndex)
            ?.segments
            ?.firstOrNull()
            ?.routeShortName
            ?.trim()

        val selectedLiveVehicles = if (!selectedLine.isNullOrBlank()) {
            val normalizedSelected = selectedLine.filter { it.isDigit() }

            liveVehicles.filter { vehicle ->
                val normalizedVehicleLine = vehicle.line.filter { it.isDigit() }
                normalizedVehicleLine == normalizedSelected
            }
        } else {
            emptyList()
        }

        LaunchedEffect(routeOptions, selectedRouteIndex, liveVehicles) {
            android.util.Log.d(
                "LIVE_FILTER",
                "routeOptions=${routeOptions.size}, selectedIndex=$selectedRouteIndex, selectedLine=$selectedLine, matched=${selectedLiveVehicles.size}, liveTotal=${liveVehicles.size}"
            )
            android.util.Log.d(
                "LIVE_FILTER",
                "live lines sample=${liveVehicles.map { it.line }.distinct().sorted().take(50)}"
            )
            android.util.Log.d(
                "LIVE_FILTER",
                "live vehicles sample=${liveVehicles.take(20).map { "${it.vehicleId}:${it.line}" }}"
            )
            android.util.Log.d(
                "LIVE_FILTER",
                "selected line raw='$selectedLine'"
            )

            android.util.Log.d(
                "LIVE_FILTER",
                "route option lines=    ${routeOptions.mapNotNull { it.segments.firstOrNull()?.routeShortName }}"
            )

            android.util.Log.d(
                "LIVE_FILTER",
                "live lines sample=${liveVehicles.map { it.line }.distinct().take(20)}"
            )
        }
        RouteMapPreview(
            destination = destination,
            userLocation = effectiveLocation,
            routeData = routeData,
            selectedRoute = routeOptions.getOrNull(selectedRouteIndex),
            gtfsRepository = gtfsRepository,
            liveVehicles = selectedLiveVehicles
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Route options",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (isLoadingRoutes) {
            Text(
                text = "Loading routes...",
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodyMedium
            )
        } else if (routeOptions.isEmpty()) {
            Text(
                text = "No route options found nearby.",
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            val fastestIndex = routeOptions.indices.minByOrNull { routeOptions[it].totalMinutes } ?: -1
            val lessWalkingIndex = routeOptions.indices.minByOrNull {
                routeOptions[it].walkToFirstStopMinutes + routeOptions[it].walkToFinalDestMinutes            } ?: -1
            routeOptions.forEachIndexed { index, option ->
                val isSelected = index == selectedRouteIndex
                val badge = when {
                    index == fastestIndex -> "Fastest"
                    index == lessWalkingIndex && lessWalkingIndex != fastestIndex -> "Less walking"
                    else -> "Alternative"
                }

                val steps = mutableListOf<RouteStep>()

                steps.add(
                    RouteStep.Walk(
                        text = "Walk to ${option.segments.first().fromStop.stopName}",
                        time = "${option.walkToFirstStopMinutes} мин"
                    )
                )

                option.segments.forEachIndexed { segmentIndex, segment ->
                    steps.add(
                        RouteStep.Bus(
                            text = "Bus ${segment.routeShortName} to ${segment.toStop.stopName}",
                            time = "${minutesBetweenTimes(segment.departureTime, segment.arrivalTime).coerceAtLeast(2)} min",
                            line = segment.routeShortName
                        )
                    )

                    if (segmentIndex < option.segments.size - 1) {
                        steps.add(
                            RouteStep.Walk(
                                text = "Transfer at ${segment.toStop.stopName}",
                                time = "Wait ~10 min"
                            )
                        )
                    }
                }

                steps.add(
                    RouteStep.Walk(
                        text = "Walk to destination",
                        time = "${option.walkToFinalDestMinutes} min"
                    )
                )

                RouteOptionCard(
                    badge = badge,
                    duration = "${option.totalMinutes} min",
                    steps = steps,
                    isSelected = isSelected,
                    onSelect = {
                        selectedRouteIndex = index
                    },
                    onGo = {
                        selectedRouteIndex = index
                        onRouteSelected(option)
                        onGo()
                    }
                )
            }
        }
    }
    if (showLocationDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Location not found") },
            text = {
                Text(
                    "Your current location could not be determined. Start the route from Grand Hotel Plovdiv for testing?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        usedTestLocation = GRAND_HOTEL_PLOVDIV
                        showLocationDialog = false
                        onGo()
                    }
                ) {
                    Text("Start from Grand Hotel")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLocationDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FromToCard(
    from: String,
    to: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            FromToRow(
                iconBg = Color(0xFFE7F8F0),
                iconTint = Color(0xFF10B981),
                label = "From",
                value = from,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.MyLocation,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            FromToRow(
                iconBg = Color(0xFFFFECDD),
                iconTint = Color(0xFFF97316),
                label = "To",
                value = to,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@Composable
fun FromToRow(
    iconBg: Color,
    iconTint: Color,
    label: String,
    value: String,
    icon: @Composable () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = label,
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = value,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}



sealed class RouteStep {
    data class Walk(val text: String, val time: String) : RouteStep()
    data class Bus(val text: String, val time: String, val line: String) : RouteStep()
}

@Composable
fun RouteOptionCard(
    badge: String,
    duration: String,
    steps: List<RouteStep>,
    isSelected: Boolean = false,
    onSelect: () -> Unit,
    onGo: () -> Unit
){
    Card(
        onClick = onSelect,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) Modifier.border(2.dp, Color(0xFF3B82F6), RoundedCornerShape(18.dp))
                else Modifier
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEFF3F8), RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badge,
                            color = Color(0xFF1E3A5F),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = duration,
                            color = Color(0xFF0F172A),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = onGo,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Go")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                steps.forEach { step ->
                    when (step) {
                        is RouteStep.Walk -> StepRowWalk(
                            text = step.text,
                            time = step.time
                        )

                        is RouteStep.Bus -> StepRowBus(
                            text = step.text,
                            time = step.time,
                            line = step.line
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StepRowWalk(
    text: String,
    time: String
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.DirectionsWalk,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = text,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = time,
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
@Composable
fun StepRowBus(
    text: String,
    time: String,
    line: String
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8EEF8)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = line,
                color = Color(0xFF1E3A5F),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = text,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = time,
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun GoScreen(
    selectedRoute: RouteOption?,
    destination: PlaceSearchResult?,
    userLocation: UserLocation?,
    onBack: () -> Unit = {},
    onCancelNavigation: () -> Unit = {}
) {


    var currentGuidanceState by remember(selectedRoute) {
        mutableStateOf<GuidanceStep?>(null)
    }
    var lastUserLocation by remember(selectedRoute) {
        mutableStateOf<UserLocation?>(null)
    }

    LaunchedEffect(userLocation, selectedRoute, destination) {
        if (userLocation != null) {
            currentGuidanceState = GuidanceEngine.resolve(
                route = selectedRoute,
                userLoc = userLocation,
                destination = destination,
                previousStep = currentGuidanceState,
                previousUserLoc = lastUserLocation
            )
            lastUserLocation = userLocation
        }
    }

    val step = currentGuidanceState
        ?: GuidanceStep(GuidancePhase.NoRoute, "Initializing", "Locating...")

    val segments = selectedRoute?.segments ?: emptyList()
    val totalSteps = if (selectedRoute == null) 0 else selectedRoute.segments.size + 2
    val firstSegment = segments.firstOrNull()
    val walkTargetName = firstSegment?.fromStop?.stopName ?: "Stop not available"
    val busLine = firstSegment?.routeShortName ?: "-"
    val busDestination = firstSegment?.toStop?.stopName ?: (destination?.name ?: "Destination")

    val walkDistanceMeters = if (userLocation != null && firstSegment != null) {
        distanceBetween(
            userLocation.lat,
            userLocation.lon,
            firstSegment.fromStop.stopLat,
            firstSegment.fromStop.stopLon
        )
    } else {
        0.0
    }

    val walkMinutesValue = walkingMinutes(walkDistanceMeters)
    val rideMinutes = if (firstSegment != null) {
        minutesBetweenTimes(firstSegment.departureTime, firstSegment.arrivalTime).coerceAtLeast(2)
    } else {
        0
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF111827),
                        Color(0xFF162033),
                        Color(0xFF0F172A)
                    )
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF2C374B), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Live guidance",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        if (selectedRoute == null) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "No active route",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Go back to Suggested routes and choose a route first.",
                        color = Color(0xFFCBD5E1),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            return@Column
        }

        LiveGuidanceRouteMap(
            destination = destination,
            userLocation = userLocation,
            selectedRoute = selectedRoute,
            liveVehicles = emptyList()
        )

        Spacer(modifier = Modifier.height(18.dp))

        GuidanceStatusCard(currentStep = step)
    }
}
@Composable
fun LiveGuidanceRouteMap(
    destination: PlaceSearchResult?,
    userLocation: UserLocation?,
    selectedRoute: RouteOption?,
    liveVehicles: List<LiveVehicle>
) {
    val context = LocalContext.current
    val gtfsRepository = remember { GtfsRepository(context) }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .height(214.dp)
    ) {
        RouteMapScreen(
            destination = destination,
            userLocation = userLocation,
            routeData = null,
            selectedRoute = selectedRoute,
            gtfsRepository = gtfsRepository,
            liveVehicles = liveVehicles,
            focusOnUser = true
        )
    }
}
@Composable
fun LiveGuidanceMapCard() {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF24528A)),
        modifier = Modifier
            .fillMaxWidth()
            .height(214.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val gridColor = Color(0x22FFFFFF)

                val stepX = size.width / 14f
                val stepY = size.height / 10f

                for (i in 0..14) {
                    drawLine(
                        color = gridColor,
                        start = Offset(i * stepX, 0f),
                        end = Offset(i * stepX, size.height),
                        strokeWidth = 1f
                    )
                }

                for (i in 0..10) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, i * stepY),
                        end = Offset(size.width, i * stepY),
                        strokeWidth = 1f
                    )
                }

                val start = Offset(size.width * 0.13f, size.height * 0.78f)
                val mid1 = Offset(size.width * 0.35f, size.height * 0.58f)
                val mid2 = Offset(size.width * 0.58f, size.height * 0.78f)

                val path = Path().apply {
                    moveTo(start.x, start.y)
                    quadraticTo(
                        size.width * 0.30f,
                        size.height * 0.60f,
                        mid1.x,
                        mid1.y
                    )
                    quadraticTo(
                        size.width * 0.48f,
                        size.height * 0.50f,
                        mid2.x,
                        mid2.y
                    )
                }

                drawPath(
                    path = path,
                    color = Color(0xFF19D3A2),
                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                )

                drawCircle(
                    color = Color(0xFF19D3A2),
                    radius = 12f,
                    center = start
                )

                drawCircle(
                    color = Color(0xFFFF8A1F),
                    radius = 12f,
                    center = mid2
                )
            }

            Icon(
                imageVector = Icons.Outlined.Navigation,
                contentDescription = "Navigation",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(42.dp)
            )
        }
    }
}

@Composable
fun CurrentStepCard(
    stepLabel: String,
    title: String,
    subtitle: String,
    distanceText: String,
    timeText: String
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color(0xFF19D3A2), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = stepLabel,
                        color = Color(0xFFCBD5E1),
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = title,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = subtitle,
                        color = Color(0xFFE2E8F0),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Distance",
                            color = Color(0xFF94A3B8),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = distanceText,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Time",
                            color = Color(0xFF94A3B8),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = timeText,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NextStepCard(
    line: String,
    title: String,
    subtitle: String
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Next step",
                color = Color(0xFFCBD5E1),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF1E3A5F), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = line,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = subtitle,
                        color = Color(0xFFCBD5E1),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun SmartAlertCard(
    text: String
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B3B3C)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = Color(0xFF19D3A2),
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Trip status",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = text,
                    color = Color(0xFFD1FAE5),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun GuidanceStatusCard(
    currentStep: GuidanceStep
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF334155))
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = when (currentStep.mainIcon) {
                    "bus" -> Icons.Outlined.DirectionsBus
                    "wait" -> Icons.Outlined.AccessTime
                    "flag" -> Icons.Outlined.Place
                    else -> Icons.Outlined.DirectionsWalk
                }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color(0xFF19D3A2), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    val phaseLabel = when (currentStep.phase) {
                        GuidancePhase.WalkingToFirstStop -> "Walk"
                        GuidancePhase.WaitingForVehicle -> "Wait"
                        GuidancePhase.RidingVehicle -> "On board"
                        GuidancePhase.WalkingTransfer -> "Transfer"
                        GuidancePhase.WaitingForTransferVehicle -> "Wait for transfer"
                        GuidancePhase.FinalWalk -> "Final walk"
                        GuidancePhase.Arrived -> "Arrived"
                        GuidancePhase.NoRoute -> "No route"
                    }

                    Text(
                        text = phaseLabel,
                        color = Color(0xFFCBD5E1),
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentStep.title,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = currentStep.subtitle,
                        color = Color(0xFFE2E8F0),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            if (currentStep.phase != GuidancePhase.NoRoute) {
                Spacer(modifier = Modifier.height(18.dp))

                LinearProgressIndicator(
                    progress = { currentStep.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = Color(0xFF19D3A2),
                    trackColor = Color(0xFF0F172A)
                )
            }
        }
    }
}

@Composable
fun ProfileScreen(
    authManager: AuthManager,
    onSignIn: () -> Unit,
    onCreateAccount: () -> Unit,
    onLogout: () -> Unit
) {
    val isLoggedIn = authManager.isUserLoggedIn()
    val email = authManager.getCurrentUserEmail()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isLoggedIn) "Manage your account and settings" else "Manage your account and settings",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(22.dp))

        if (isLoggedIn) {
            SignedInCard(
                email = email ?: "Unknown",
                onLogout = onLogout
            )
        } else {
            SignedOutCard(
                onSignIn = onSignIn,
                onCreateAccount = onCreateAccount
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "PREFERENCES",
            color = Color(0xFF64748B),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        PreferencesCard()

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Plovdiv Transit v1.0.0",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = Color(0xFF64748B),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "© 2026 Plovdiv Transit. All rights reserved.",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = Color(0xFF94A3B8),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
@Composable
fun SignedOutCard(
    onSignIn: () -> Unit,
    onCreateAccount: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Sign in to save your data",
                        color = Color(0xFF0F172A),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Access saved routes, stops, and preferences",
                        color = Color(0xFF64748B),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Sign in")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onCreateAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Create account")
            }
        }
    }
}

@Composable
fun SignedInCard(
    email: String,
    onLogout: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Signed in",
                        color = Color(0xFF0F172A),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = email,
                        color = Color(0xFF64748B),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Logout")
            }
        }
    }
}

@Composable
fun PreferencesCard() {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            PreferenceRow(
                icon = Icons.Outlined.Place,
                title = "Saved stops",
                subtitle = "Quick access to your favorite stops"
            )

            DividerLine()

            PreferenceRow(
                icon = Icons.Outlined.Notifications,
                title = "Notifications",
                subtitle = "Manage alert preferences"
            )

            DividerLine()

            PreferenceRow(
                icon = Icons.Outlined.Settings,
                title = "Settings",
                subtitle = "App preferences and privacy"
            )
        }
    }
}

@Composable
fun PreferenceRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(Color(0xFFF1F5F9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF475569),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = title,
                color = Color(0xFF0F172A),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFE2E8F0))
    )
}