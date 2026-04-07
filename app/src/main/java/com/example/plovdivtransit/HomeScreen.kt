package com.example.plovdivtransit

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onPlanRoute: () -> Unit,
    onUserLocationChanged: (UserLocation) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        Text(
            text = "Home",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Live buses around you",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(20.dp))

        SearchCard(
            onClick = onPlanRoute
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickDestinationChip(
                title = "Home",
                eta = "12 min",
                modifier = Modifier.weight(1f)
            )
            QuickDestinationChip(
                title = "Work",
                eta = "18 min",
                modifier = Modifier.weight(1f)
            )
            QuickDestinationChip(
                title = "School",
                eta = "9 min",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        MapPreviewCard(
            onUserLocationChanged = onUserLocationChanged
        )

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Nearby buses",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(12.dp))

        NearbyBusCard(
            line = "19",
            destination = "Central Station",
            stopsAway = "3 stops away",
            eta = "2 min",
            status = "On time",
            statusColor = Color(0xFFD1FAE5),
            statusTextColor = Color(0xFF065F46)
        )

        Spacer(modifier = Modifier.height(12.dp))

        NearbyBusCard(
            line = "26",
            destination = "Maritsa District",
            stopsAway = "5 stops away",
            eta = "5 min",
            status = "On time",
            statusColor = Color(0xFFD1FAE5),
            statusTextColor = Color(0xFF065F46)
        )

        Spacer(modifier = Modifier.height(12.dp))

        NearbyBusCard(
            line = "102",
            destination = "Airport",
            stopsAway = "7 stops away",
            eta = "8 min",
            status = "Delayed",
            statusColor = Color(0xFFFFEDD5),
            statusTextColor = Color(0xFFEA580C)
        )
    }
}

@Composable
fun SearchCard(
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(Color(0xFFF1F5F9), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Where to?",
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Outlined.ArrowForward,
                contentDescription = "Open",
                tint = Color(0xFF64748B)
            )
        }
    }
}

@Composable
fun QuickDestinationChip(
    title: String,
    eta: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = eta,
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun MapPreviewCard(
    onUserLocationChanged: (UserLocation) -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .height(205.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                HomeMapScreen(
                    onUserLocationChanged = onUserLocationChanged
                )
            }

            Text(
                text = "Your location: Central Plovdiv",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun NearbyBusCard(
    line: String,
    destination: String,
    stopsAway: String,
    eta: String,
    status: String,
    statusColor: Color,
    statusTextColor: Color
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF1E3A5F), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = line,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = destination,
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = stopsAway,
                    color = Color(0xFF64748B),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = eta,
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .background(statusColor, RoundedCornerShape(999.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = status,
                            color = statusTextColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}