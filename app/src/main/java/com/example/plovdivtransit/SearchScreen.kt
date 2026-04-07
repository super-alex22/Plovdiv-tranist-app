package com.example.plovdivtransit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

data class PlaceSearchResult(
    val name: String,
    val lat: Double,
    val lon: Double
)

@Composable
fun SearchDestinationScreen(
    userLat: Double? = null,
    userLon: Double? = null,
    onBack: () -> Unit,
    onPlaceSelected: (PlaceSearchResult) -> Unit
) {
    val context = LocalContext.current
    val geoapifyRepository = remember { GeoapifyRepository(context) }
    val gtfsRepository = remember { GtfsRepository(context) }
    var query by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<PlaceSearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(query, userLat, userLon) {
        android.util.Log.d("SEARCH_UI", "Query changed: $query")

        if (query.length < 3) {
            suggestions = emptyList()
            isSearching = false
            return@LaunchedEffect
        }

        delay(500)
        isSearching = true

        try {
            android.util.Log.d("SEARCH_UI", "Starting Geoapify search")
            suggestions = withContext(Dispatchers.IO) {
                val localPopular = PopularPlaces.items
                    .filter {
                        it.name.contains(query, ignoreCase = true) ||
                                it.keywords.any { keyword ->
                                    keyword.contains(query, ignoreCase = true)
                                }
                    }
                    .map {
                        PlaceSearchResult(
                            name = it.name,
                            lat = it.lat,
                            lon = it.lon
                        )
                    }

                val localStops = gtfsRepository.loadStops()
                    .filter { it.stopName.contains(query, ignoreCase = true) }
                    .map {
                        PlaceSearchResult(
                            name = it.stopName,
                            lat = it.stopLat,
                            lon = it.stopLon
                        )
                    }

                val remoteResults = geoapifyRepository.searchPlaces(
                    query = query,
                    userLat = userLat,
                    userLon = userLon
                ).map {
                    PlaceSearchResult(
                        name = it.name,
                        lat = it.lat,
                        lon = it.lon
                    )
                }

                val combined = (localPopular + localStops + remoteResults)
                    .distinctBy { it.name.lowercase() }

                if (userLat != null && userLon != null) {
                    combined.sortedBy { distanceMeters(userLat, userLon, it.lat, it.lon) }
                } else {
                    combined
                }
            }
            android.util.Log.d("SEARCH_UI", "Suggestions received: ${suggestions.size}")
        } catch (e: Exception) {
            android.util.Log.e("SEARCH_UI", "Search failed", e)
            suggestions = emptyList()
        } finally {
            isSearching = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp)
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

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Search destination",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Where do you want to go?")
            },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isSearching) {
            Text(
                text = "Searching...",
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            suggestions.forEach { suggestion ->
                Card(
                    onClick = {
                        onPlaceSelected(suggestion)
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = suggestion.name,
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "${suggestion.lat}, ${suggestion.lon}",
                            color = Color(0xFF64748B),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}