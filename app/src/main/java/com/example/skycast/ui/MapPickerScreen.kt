package com.example.skycast.ui

import android.annotation.SuppressLint
import android.location.Geocoder
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.skycast.BuildConfig
import com.example.skycast.data.remote.RetrofitClient
import com.example.skycast.ui.theme.*
import com.example.skycast.utils.LocationHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Reusable full-screen map location picker.
 *
 * @param screenTitle      Title shown nowhere (nav-less screen) – kept for semantics
 * @param confirmLabel     Text on the action button, e.g. "Save to Favorites" or "Set as My Location"
 * @param onBack           Called when the back arrow is tapped
 * @param onConfirm        Called when the user confirms with (lat, lon, cityName)
 */
@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    screenTitle: String = "",
    confirmLabel: String,
    onBack: () -> Unit,
    onConfirm: (lat: Double, lon: Double, cityName: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    /* ── State ─────────────────────────────────────────────────────────────── */
    var searchQuery by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var cityName by remember { mutableStateOf("") }
    var countryName by remember { mutableStateOf("") }
    var searchError by remember { mutableStateOf("") }
    var isLocatingMe by remember { mutableStateOf(true) }

    // Weather preview
    data class WeatherPreview(val temp: Int, val description: String, val icon: String, val humidity: Int, val windSpeed: Double)
    var weatherPreview by remember { mutableStateOf<WeatherPreview?>(null) }
    var isWeatherLoading by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(30.0444, 31.2357), 5f)
    }

    /* ── Helpers ───────────────────────────────────────────────────────────── */
    fun fetchWeatherPreview(latLng: LatLng) {
        scope.launch {
            isWeatherLoading = true
            weatherPreview = null
            try {
                val resp = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getWeatherForecast(
                        lat = latLng.latitude,
                        lon = latLng.longitude,
                        apiKey = BuildConfig.API_KEY
                    )
                }
                if (resp.isSuccessful) {
                    val first = resp.body()?.forecastList?.firstOrNull()
                    if (first != null) {
                        weatherPreview = WeatherPreview(
                            temp = first.main.temp.toInt(),
                            description = first.weatherInfo.firstOrNull()?.description
                                ?.replaceFirstChar { it.titlecase(Locale.getDefault()) } ?: "",
                            icon = first.weatherInfo.firstOrNull()?.icon ?: "",
                            humidity = first.main.humidity,
                            windSpeed = first.wind.speed
                        )
                    }
                }
            } catch (_: Exception) { }
            isWeatherLoading = false
        }
    }

    fun reverseGeocode(latLng: LatLng) {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val addresses = Geocoder(context, Locale.getDefault())
                        .getFromLocation(latLng.latitude, latLng.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val a = addresses[0]
                        (a.locality ?: a.subAdminArea ?: a.adminArea ?: "Unknown") to (a.countryName ?: "")
                    } else "Selected Location" to ""
                } catch (_: Exception) { "Selected Location" to "" }
            }
            cityName = result.first
            countryName = result.second
        }
    }

    fun pickLocation(latLng: LatLng) {
        selectedLocation = latLng
        reverseGeocode(latLng)
        fetchWeatherPreview(latLng)
    }

    /* ── Launch: fly to GPS ────────────────────────────────────────────────── */
    LaunchedEffect(Unit) {
        LocationHelper.getCurrentLocation(context) { loc ->
            if (loc != null) {
                val latLng = LatLng(loc.latitude, loc.longitude)
                scope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.fromLatLngZoom(latLng, 13f)
                        ), durationMs = 1200
                    )
                }
            }
            isLocatingMe = false
        }
    }

    /* ── UI ────────────────────────────────────────────────────────────────── */
    Box(modifier = Modifier.fillMaxSize()) {

        // Full-screen map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { pickLocation(it) },
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                compassEnabled = true
            )
        ) {
            selectedLocation?.let {
                Marker(state = MarkerState(position = it), title = cityName)
            }
        }

        // ── Floating search bar ────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SkyNavy.copy(alpha = 0.97f)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = CloudWhite)
                }
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it; searchError = "" },
                    placeholder = { Text("Search city…", color = CloudGrey) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = CloudWhite,
                        unfocusedTextColor = CloudWhite,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = SkyBlueBright
                    )
                )
                IconButton(onClick = {
                    scope.launch {
                        val results = withContext(Dispatchers.IO) {
                            try { Geocoder(context, Locale.getDefault()).getFromLocationName(searchQuery, 1) }
                            catch (_: Exception) { null }
                        }
                        if (!results.isNullOrEmpty()) {
                            val a = results[0]
                            val latLng = LatLng(a.latitude, a.longitude)
                            pickLocation(latLng)
                            searchError = ""
                            cameraPositionState.animate(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.fromLatLngZoom(latLng, 12f)
                                ), durationMs = 800
                            )
                        } else { searchError = "City not found" }
                    }
                }) {
                    Icon(Icons.Default.Search, "Search", tint = SkyBlueBright)
                }
            }
            if (searchError.isNotEmpty()) {
                Text(
                    searchError,
                    color = StormRed,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }

        // ── My Location FAB ────────────────────────────────────────────────
        FloatingActionButton(
            onClick = {
                isLocatingMe = true
                LocationHelper.getCurrentLocation(context) { loc ->
                    if (loc != null) {
                        val latLng = LatLng(loc.latitude, loc.longitude)
                        pickLocation(latLng)
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.fromLatLngZoom(latLng, 14f)
                                ), durationMs = 800
                            )
                        }
                    }
                    isLocatingMe = false
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 16.dp,
                    bottom = if (selectedLocation != null) 300.dp else 120.dp
                )
                .size(52.dp),
            containerColor = SkyNavy,
            contentColor = SkyBlueBright,
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isLocatingMe) {
                CircularProgressIndicator(color = SkyBlueBright, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            } else {
                Icon(Icons.Default.MyLocation, "My Location")
            }
        }

        // ── Bottom info + confirm panel ────────────────────────────────────
        AnimatedVisibility(
            visible = selectedLocation != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(tween(350, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(250)),
            exit  = slideOutVertically(tween(250)) { it } + fadeOut(tween(200))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = SkyNavy),
                border = androidx.compose.foundation.BorderStroke(1.dp, FrostStrong)
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {

                    // Drag handle
                    Box(
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(40.dp).height(4.dp)
                            .background(FrostStrong, CircleShape)
                    )

                    Spacer(Modifier.height(16.dp))

                    // ── Location header ────────────────────────────────────
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(SkyBlueBright.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) { Text("📍", fontSize = 20.sp) }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                cityName.ifEmpty { "Selected Location" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.W700,
                                color = CloudWhite
                            )
                            if (countryName.isNotEmpty()) {
                                Text(countryName, style = MaterialTheme.typography.bodySmall, color = SkyBluePale)
                            }
                        }
                        // Coordinates chip
                        selectedLocation?.let { latLng ->
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "${"%.3f".format(latLng.latitude)}°",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CloudGrey
                                )
                                Text(
                                    "${"%.3f".format(latLng.longitude)}°",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CloudGrey
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = FrostStrong)
                    Spacer(Modifier.height(14.dp))

                    // ── Weather preview ────────────────────────────────────
                    when {
                        isWeatherLoading -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Frost, RoundedCornerShape(16.dp))
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = SkyBlueBright,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text("Fetching weather…", style = MaterialTheme.typography.bodySmall, color = CloudGrey)
                            }
                        }
                        weatherPreview != null -> {
                            val wp = weatherPreview!!
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(listOf(SkyBlue.copy(alpha = 0.3f), SkyNavy)),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Weather icon
                                AsyncImage(
                                    model = "https://openweathermap.org/img/wn/${wp.icon}@2x.png",
                                    contentDescription = null,
                                    modifier = Modifier.size(52.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                // Temp + description
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "${wp.temp}°C",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.W700,
                                        color = CloudWhite
                                    )
                                    Text(
                                        wp.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SkyBluePale
                                    )
                                }
                                // Stats column
                                Column(horizontalAlignment = Alignment.End) {
                                    WeatherStatChip(emoji = "💧", value = "${wp.humidity}%")
                                    Spacer(Modifier.height(4.dp))
                                    WeatherStatChip(emoji = "💨", value = "${wp.windSpeed} m/s")
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Confirm button ─────────────────────────────────────
                    Button(
                        onClick = {
                            selectedLocation?.let { latLng ->
                                onConfirm(latLng.latitude, latLng.longitude, cityName.ifEmpty { "Unknown" })
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBlueBright)
                    ) {
                        Text(confirmLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.W700)
                    }
                }
            }
        }
    }
}

/** Small inline stat chip used inside the weather preview card */
@Composable
private fun WeatherStatChip(emoji: String, value: String) {
    Row(
        modifier = Modifier
            .background(FrostStrong, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(emoji, fontSize = 12.sp)
        Text(value, style = MaterialTheme.typography.labelSmall, color = CloudGrey)
    }
}
