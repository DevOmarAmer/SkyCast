package com.example.skycast.ui.favorites

import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.skycast.data.model.FavoriteLocation
import com.example.skycast.ui.theme.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFavoriteScreen(
    viewModel: FavoritesViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var cityName by remember { mutableStateOf("") }
    var searchError by remember { mutableStateOf("") }

    val defaultLocation = LatLng(30.0444, 31.2357)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 5f)
    }

    fun geocodeAndMove(query: String) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(query, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val newLatLng = LatLng(address.latitude, address.longitude)
                selectedLocation = newLatLng
                cityName = address.locality ?: address.subAdminArea ?: query
                searchError = ""
                cameraPositionState.position = CameraPosition.fromLatLngZoom(newLatLng, 10f)
            } else {
                searchError = "Location not found"
            }
        } catch (e: Exception) {
            searchError = "Search failed – check your connection"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyDeepNavy, DarkSurface)))
    ) {
        // ── Full Screen Map ────────────────────────────────────────────────────
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                selectedLocation = latLng
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                    cityName = if (!addresses.isNullOrEmpty())
                        addresses[0].locality ?: addresses[0].subAdminArea ?: "Selected location"
                    else "Selected location"
                } catch (e: Exception) {
                    cityName = "Selected location"
                }
            }
        ) {
            selectedLocation?.let {
                Marker(state = MarkerState(position = it), title = cityName)
            }
        }

        // ── Floating search bar on top of map ─────────────────────────────────
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Search field with glass effect
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SkyNavy.copy(alpha = 0.95f)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = CloudWhite
                        )
                    }
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it; searchError = "" },
                        placeholder = {
                            Text("Search city…", color = CloudGrey)
                        },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            focusedTextColor = CloudWhite,
                            unfocusedTextColor = CloudWhite,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            cursorColor = SkyBlueBright
                        ),
                        singleLine = true
                    )
                    IconButton(onClick = { geocodeAndMove(searchQuery) }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = SkyBlueBright
                        )
                    }
                }

                if (searchError.isNotEmpty()) {
                    Text(
                        text = searchError,
                        color = StormRed,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(top = 56.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Save Bottom Panel ──────────────────────────────────────────────
            if (selectedLocation != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = SkyNavy),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FrostStrong)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "📍 $cityName",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.W600,
                            color = CloudWhite
                        )
                        Text(
                            text = "${"%.4f".format(selectedLocation!!.latitude)}°, ${"%.4f".format(selectedLocation!!.longitude)}°",
                            style = MaterialTheme.typography.labelSmall,
                            color = CloudGrey
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val newFavorite = FavoriteLocation(
                                    cityName = cityName,
                                    latitude = selectedLocation!!.latitude,
                                    longitude = selectedLocation!!.longitude
                                )
                                viewModel.addLocation(newFavorite)
                                onNavigateBack()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlueBright)
                        ) {
                            Text(
                                "Save to Favorites",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.W600,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}