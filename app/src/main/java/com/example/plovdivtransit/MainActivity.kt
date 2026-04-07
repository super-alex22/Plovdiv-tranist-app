package com.example.plovdivtransit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.plovdivtransit.ui.theme.PlovdivTransitTheme
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import android.annotation.SuppressLint
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.CustomZoomButtonsController
import android.view.ViewGroup
import androidx.compose.ui.platform.LocalContext
import org.osmdroid.views.overlay.Marker
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import org.osmdroid.views.overlay.Polyline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
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
    onUserLocationChanged: (UserLocation) -> Unit
) {
    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var currentLocation by remember {
        mutableStateOf(GeoPoint(42.1354, 24.7453))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = GeoPoint(location.latitude, location.longitude)
                    onUserLocationChanged(
                        UserLocation(
                            lat = location.latitude,
                            lon = location.longitude
                        )
                    )
                }
            }
        }
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
            mapView.controller.setCenter(currentLocation)

            mapView.overlays.clear()

            val marker = Marker(mapView)
            marker.position = currentLocation
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = if (hasLocationPermission) "Your location" else "Test location"

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

                    AppScaffold(
                        currentScreen = currentScreen,
                        authManager = authManager,
                        selectedPlace = selectedPlace,
                        onPlaceSelected = { selectedPlace = it },
                        userLocation = userLocation,
                        onUserLocationChanged = { userLocation = it },
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
    onScreenSelected: (AppScreen) -> Unit,
    onLogout: () -> Unit
) {
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
                    onPlanRoute = { onScreenSelected(AppScreen.Search) },
                    onUserLocationChanged = onUserLocationChanged
                )
                AppScreen.Routes -> SuggestedRoutesScreen(
                    destination = selectedPlace,
                    userLocation = userLocation,
                    onBack = { onScreenSelected(AppScreen.Home) },
                    onGo = { onScreenSelected(AppScreen.Go) }
                )
                AppScreen.Go -> GoScreen(
                    onBack = { onScreenSelected(AppScreen.Routes) }
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
    selectedRoute: GtfsRouteOption? = null,
    gtfsRepository: GtfsRepository? = null
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp) // Slightly taller for better visibility
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            RouteMapScreen(
                destination = destination,
                userLocation = userLocation,
                routeData = routeData,
                selectedRoute = selectedRoute,
                gtfsRepository = gtfsRepository
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
    selectedRoute: GtfsRouteOption? = null,
    gtfsRepository: GtfsRepository? = null
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

            if (selectedRoute != null) {
                val startStop = GeoPoint(selectedRoute.startStop.stopLat, selectedRoute.startStop.stopLon)
                val endStop = GeoPoint(selectedRoute.endStop.stopLat, selectedRoute.endStop.stopLon)

                // 1. Walk to start stop
                val walk1 = Polyline().apply {
                    setPoints(listOf(fromPoint, startStop))
                    outlinePaint.color = android.graphics.Color.GRAY
                    outlinePaint.strokeWidth = 4f
                    outlinePaint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f)
                }
                
                // 2. Bus segment
                val busPoints = if (gtfsRepository != null && selectedRoute.shapeId != null) {
                    gtfsRepository.getShapePoints(selectedRoute.shapeId, selectedRoute.startStop, selectedRoute.endStop)
                } else {
                    emptyList()
                }

                val busLine = Polyline().apply {
                    setPoints(
                        if (busPoints.isNotEmpty()) busPoints
                        else listOf(startStop, endStop)
                    )
                    outlinePaint.color = android.graphics.Color.parseColor("#3B82F6") // Blue
                    outlinePaint.strokeWidth = 10f
                }

                // 3. Walk to destination
                val walk2 = Polyline().apply {
                    setPoints(listOf(endStop, toPoint))
                    outlinePaint.color = android.graphics.Color.GRAY
                    outlinePaint.strokeWidth = 4f
                    outlinePaint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f)
                }

                mapView.overlays.add(walk1)
                mapView.overlays.add(busLine)
                mapView.overlays.add(walk2)
            } else {
                // Fallback to direct route if no GTFS option selected
                val routeLine = Polyline().apply {
                    setPoints(
                        if (routeData?.points?.isNotEmpty() == true) {
                            routeData.points
                        } else {
                            listOf(fromPoint, toPoint)
                        }
                    )
                    outlinePaint.strokeWidth = 8f
                }
                mapView.overlays.add(routeLine)
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

            val centerPoint = GeoPoint(
                (fromPoint.latitude + toPoint.latitude) / 2.0,
                (fromPoint.longitude + toPoint.longitude) / 2.0
            )
            mapView.controller.setZoom(14.0)
            mapView.controller.setCenter(centerPoint)
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
    onGo: () -> Unit

) {

    val repository = remember { RoutingRepository() }
    val context = LocalContext.current
    val gtfsRepository = remember { GtfsRepository(context) }
    var routeOptions by remember { mutableStateOf<List<GtfsRouteOption>>(emptyList()) }
    var selectedRouteIndex by remember { mutableStateOf(0) }
    var isLoadingRoutes by remember { mutableStateOf(false) }
    var routeData by remember { mutableStateOf<RouteData?>(null) }

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

    LaunchedEffect(destination, userLocation) {
        if (destination == null || userLocation == null) {
            routeOptions = emptyList()
            return@LaunchedEffect
        }

        isLoadingRoutes = true

        routeOptions = gtfsRepository.findBestRouteOptions(
            startLat = userLocation.lat,
            startLon = userLocation.lon,
            destLat = destination.lat,
            destLon = destination.lon
        )

        isLoadingRoutes = false
    }
    LaunchedEffect(fromPoint, toPoint) {
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
            from = "Your location",
            to = destination?.name ?: "Central Station"
        )

        Spacer(modifier = Modifier.height(16.dp))

        RouteMapPreview(
            destination = destination,
            userLocation = userLocation,
            routeData = routeData,
            selectedRoute = routeOptions.getOrNull(selectedRouteIndex),
            gtfsRepository = gtfsRepository
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
                text = "No direct routes found nearby.",
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            val fastestIndex = routeOptions.indices.minByOrNull { routeOptions[it].totalMinutes } ?: -1
            val lessWalkingIndex = routeOptions.indices.minByOrNull {
                routeOptions[it].walkToStopMinutes + routeOptions[it].walkToDestMinutes
            } ?: -1
            routeOptions.forEachIndexed { index, option ->
                val isSelected = index == selectedRouteIndex
                val badge = when {
                    index == fastestIndex -> "Fastest"
                    index == lessWalkingIndex && lessWalkingIndex != fastestIndex -> "Less walking"
                    else -> "Alternative"
                }

                RouteOptionCard(
                    badge = badge,
                    duration = "${option.totalMinutes} min",
                    isSelected = isSelected,
                    steps = listOf(
                        RouteStep.Walk(
                            text = "Walk to ${option.startStop.stopName}",
                            time = "${option.walkToStopMinutes} min"
                        ),
                        RouteStep.Bus(
                            text = "Bus ${option.routeShortName} to ${option.endStop.stopName}",
                            time = "${option.busMinutes} min",
                            line = option.routeShortName
                        ),
                        RouteStep.Walk(
                            text = "Walk to destination",
                            time = "${option.walkToDestMinutes} min"
                        )
                    ),
                    onGo = {
                        selectedRouteIndex = index
                    }
                )

                if (index != routeOptions.lastIndex) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
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
    onGo: () -> Unit
) {
    Card(
        onClick = onGo,
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
    onBack: () -> Unit = {}
) {
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

        LiveGuidanceMapCard()

        Spacer(modifier = Modifier.height(18.dp))

        CurrentStepCard()

        Spacer(modifier = Modifier.height(16.dp))

        NextStepCard()

        Spacer(modifier = Modifier.height(16.dp))

        SmartAlertCard()
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
fun CurrentStepCard() {
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
                        text = "Step 1 of 3",
                        color = Color(0xFFCBD5E1),
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Walk to the stop",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Head to Bus Stop Opera",
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
                            text = "240m",
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
                            text = "3 min",
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
fun NextStepCard() {
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
                        text = "19",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Take Bus 19 to Central Station",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Bus arrives in 5 min",
                        color = Color(0xFFCBD5E1),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun SmartAlertCard() {
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
                    text = "Smart alert active",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "We'll notify you when Bus 19 is approaching and when it's time to get off",
                    color = Color(0xFFD1FAE5),
                    style = MaterialTheme.typography.bodyMedium
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