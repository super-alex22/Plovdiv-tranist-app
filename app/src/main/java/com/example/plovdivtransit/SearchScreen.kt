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

fun normalizeSearchText(text: String): String {
    return text
        .lowercase()
        .trim()
        .replace(Regex("\\s+"), " ")
}

fun transliterateBgToLatin(text: String): String {
    return buildString {
        for (ch in text.lowercase()) {
            append(
                when (ch) {
                    'а' -> "a"
                    'б' -> "b"
                    'в' -> "v"
                    'г' -> "g"
                    'д' -> "d"
                    'е' -> "e"
                    'ж' -> "zh"
                    'з' -> "z"
                    'и' -> "i"
                    'й' -> "y"
                    'к' -> "k"
                    'л' -> "l"
                    'м' -> "m"
                    'н' -> "n"
                    'о' -> "o"
                    'п' -> "p"
                    'р' -> "r"
                    'с' -> "s"
                    'т' -> "t"
                    'у' -> "u"
                    'ф' -> "f"
                    'х' -> "h"
                    'ц' -> "ts"
                    'ч' -> "ch"
                    'ш' -> "sh"
                    'щ' -> "sht"
                    'ъ' -> "a"
                    'ь' -> ""
                    'ю' -> "yu"
                    'я' -> "ya"
                    else -> ch.toString()
                }
            )
        }
    }
}

fun transliterateLatinToBgLike(text: String): String {
    return text
        .lowercase()
        .replace("sht", "щ")
        .replace("sh", "ш")
        .replace("ch", "ч")
        .replace("zh", "ж")
        .replace("ts", "ц")
        .replace("yu", "ю")
        .replace("ya", "я")
        .replace("a", "а")
        .replace("b", "б")
        .replace("v", "в")
        .replace("g", "г")
        .replace("d", "д")
        .replace("e", "е")
        .replace("z", "з")
        .replace("i", "и")
        .replace("y", "й")
        .replace("k", "к")
        .replace("l", "л")
        .replace("m", "м")
        .replace("n", "н")
        .replace("o", "о")
        .replace("p", "п")
        .replace("r", "р")
        .replace("s", "с")
        .replace("t", "т")
        .replace("u", "у")
        .replace("f", "ф")
        .replace("h", "х")
}

fun matchesSearch(source: String, query: String): Boolean {
    val normalizedSource = normalizeSearchText(source)
    val normalizedQuery = normalizeSearchText(query)

    val sourceLatin = transliterateBgToLatin(normalizedSource)
    val queryLatin = transliterateBgToLatin(normalizedQuery)

    val sourceBgLike = transliterateLatinToBgLike(normalizedSource)
    val queryBgLike = transliterateLatinToBgLike(normalizedQuery)

    return normalizedSource.contains(normalizedQuery) ||
            sourceLatin.contains(queryLatin) ||
            sourceBgLike.contains(queryBgLike)
}

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
            android.util.Log.d("SEARCH_UI", "Starting search block")
            suggestions = withContext(Dispatchers.IO) {
                android.util.Log.d("SEARCH_UI", "Inside withContext(IO)")
                
                val localPopular = try {
                    PopularPlaces.items
                        .filter {
                            matchesSearch(it.name, query)    ||
                                    it.keywords.any { keyword ->
                                        matchesSearch(keyword, query)
                                    }
                        }
                        .map {
                            PlaceSearchResult(
                                name = it.name,
                                lat = it.lat,
                                lon = it.lon
                            )
                        }.also {
                            android.util.Log.d("SEARCH_UI", "localPopular count = ${it.size}")
                        }
                } catch (e: Exception) {
                    android.util.Log.e("SEARCH_UI", "localPopular failed", e)
                    emptyList<PlaceSearchResult>()
                }

                val localStops = try {
                    gtfsRepository.loadStops()
                        .filter { matchesSearch(it.stopName, query) }
                        .map {
                            PlaceSearchResult(
                                name = it.stopName,
                                lat = it.stopLat,
                                lon = it.stopLon
                            )
                        }.also {
                            android.util.Log.d("SEARCH_UI", "localStops count = ${it.size}")
                        }
                } catch (e: Exception) {
                    android.util.Log.e("SEARCH_UI", "localStops failed", e)
                    emptyList<PlaceSearchResult>()
                }

                // Temporary: Disable Geoapify completely
                val remoteResults = emptyList<PlaceSearchResult>()
                android.util.Log.d("SEARCH_UI", "remoteResults count = 0 (disabled)")

                val combined = (localPopular + localStops + remoteResults)
                    .distinctBy { normalizeSearchText(transliterateBgToLatin(it.name)) }

                android.util.Log.d("SEARCH_UI", "combined count = ${combined.size}")

                val result = if (userLat != null && userLon != null) {
                    combined.sortedBy { distanceMeters(userLat, userLon, it.lat, it.lon) }
                } else {
                    combined
                }
                android.util.Log.d("SEARCH_UI", "Final result size: ${result.size}")
                result
            }
            android.util.Log.d("SEARCH_UI", "Suggestions received: ${suggestions.size}")
        } catch (e: Exception) {
            android.util.Log.e("SEARCH_UI", "Search outer block failed", e)
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