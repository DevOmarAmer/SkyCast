package com.example.skycast.ui.map.view

import android.annotation.SuppressLint
import android.location.Geocoder
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.skycast.BuildConfig
import com.example.skycast.data.remote.RetrofitClient
import com.example.skycast.ui.theme.*
import com.example.skycast.utils.LocationHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Reusable full-screen map location picker.
 * Assembles [MapSearchBar] + [MapLocationBottomSheet] on top of a GoogleMap.
 *
 * @param confirmLabel  Button label ("Save to Favorites" / "Set as My Location")
 * @param onBack        Back-arrow callback
 * @param onConfirm     Emits (lat, lon, cityName) when user confirms
 */
@SuppressLint("MissingPermission")
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
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var cityName by remember { mutableStateOf("") }
    var countryName by remember { mutableStateOf("") }
    var isLocatingMe by remember { mutableStateOf(true) }
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
                    resp.body()?.forecastList?.firstOrNull()?.let { first ->
                        weatherPreview = WeatherPreview(
                            temp = first.main.temp.toInt(),
                            description = first.weatherInfo.firstOrNull()?.description
                                ?.replaceFirstChar { it.titlecase(Locale.getDefault()) } ?: "",
                            icon = first.weatherInfo.firstOrNull()?.icon ?: "",
                            humidity = first.main.humidity,
                            windSpeed = first.wind.speed,
                            conditionId = first.weatherInfo.firstOrNull()?.id ?: 800,
                            cloudsPct = first.clouds.all
                        )
                    }
                }
            } catch (_: Exception) { }
            isWeatherLoading = false
        }
    }

    fun reverseGeocode(latLng: LatLng) {
        scope.launch {
            val (city, country) = withContext(Dispatchers.IO) {
                try {
                    val a = Geocoder(context, Locale.getDefault())
                        .getFromLocation(latLng.latitude, latLng.longitude, 1)
                        ?.firstOrNull()
                    (a?.locality ?: a?.subAdminArea ?: a?.adminArea ?: "Unknown") to (a?.countryName ?: "")
                } catch (_: Exception) { "Selected Location" to "" }
            }
            cityName = city
            countryName = country
        }
    }

    fun pickLocation(latLng: LatLng) {
        selectedLocation = latLng
        reverseGeocode(latLng)
        fetchWeatherPreview(latLng)
    }

    fun animateTo(latLng: LatLng, zoom: Float = 13f, durationMs: Int = 900) {
        scope.launch {
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(latLng, zoom)),
                durationMs = durationMs
            )
        }
    }

    /* ── On launch: fly to GPS ─────────────────────────────────────────────── */
    var isMapReady by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // Defer map inflation slightly so the Compose Navigation animation 
        // doesn't stutter because of heavy GoogleMap View initialization on main thread.
        delay(400)
        isMapReady = true
        
        LocationHelper.getCurrentLocation(context) { loc ->
            if (loc != null) animateTo(LatLng(loc.latitude, loc.longitude), zoom = 13f, durationMs = 1200)
            isLocatingMe = false
        }
    }

    /* ── Derive dynamic colors ─────────────────────────────────────────────── */
    val rawColors = if (weatherPreview != null) {
        deriveWeatherColors(
            tempCelsius = weatherPreview!!.temp.toDouble(),
            cloudPct = weatherPreview!!.cloudsPct,
            conditionId = weatherPreview!!.conditionId
        )
    } else {
        DefaultWeatherColors
    }

    val animSpec = tween<Color>(durationMillis = 800, easing = FastOutSlowInEasing)
    val bgTop by animateColorAsState(rawColors.bgTop, animSpec, label = "bgTop")
    val bgBottom by animateColorAsState(rawColors.bgBottom, animSpec, label = "bgBottom")
    val heroGlow by animateColorAsState(rawColors.heroGlow, animSpec, label = "heroGlow")
    val accent by animateColorAsState(rawColors.accent, animSpec, label = "accent")
    val surface by animateColorAsState(rawColors.cardSurface, animSpec, label = "surface")

    val animatedColors = WeatherColors(
        bgTop = bgTop, bgBottom = bgBottom, heroGlow = heroGlow, accent = accent, cardSurface = surface
    )

    /* ── UI ────────────────────────────────────────────────────────────────── */
    CompositionLocalProvider(LocalWeatherColors provides animatedColors) {
        val wc = LocalWeatherColors.current
        Box(modifier = Modifier.fillMaxSize().background(wc.bgTop)) {

            if (isMapReady) {
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
                    selectedLocation?.let { Marker(state = MarkerState(position = it), title = cityName) }
                }
            } else {
                // Placeholder map skeleton while navigation transitioning
                Box(
                    modifier = Modifier.fillMaxSize().background(wc.bgTop),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = wc.accent)
                }
            }

        // ── Search bar (autocomplete) ──────────────────────────────────────────
        MapSearchBar(
            onBack = onBack,
            onSuggestionPicked = { place: PlaceSuggestion ->
                pickLocation(place.latLng)
                animateTo(place.latLng, zoom = 12f)
            }
        )

        // ── My Location FAB ────────────────────────────────────────────────────
        FloatingActionButton(
            onClick = {
                isLocatingMe = true
                LocationHelper.getCurrentLocation(context) { loc ->
                    if (loc != null) {
                        val latLng = LatLng(loc.latitude, loc.longitude)
                        pickLocation(latLng)
                        animateTo(latLng, zoom = 14f)
                    }
                    isLocatingMe = false
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 16.dp,
                    bottom = if (selectedLocation != null) 290.dp else 110.dp
                )
                .size(52.dp),
            containerColor = wc.bgBottom.copy(alpha = 0.95f),
            contentColor = wc.accent,
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isLocatingMe) {
                CircularProgressIndicator(
                    color = wc.accent,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Icon(Icons.Default.MyLocation, contentDescription = "My Location")
            }
        }

        // ── Bottom sheet ───────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = selectedLocation != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(tween(350, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(250)),
            exit  = slideOutVertically(tween(250)) { it } + fadeOut(tween(200))
        ) {
            selectedLocation?.let { latLng ->
                MapLocationBottomSheet(
                    cityName = cityName,
                    countryName = countryName,
                    latLng = latLng,
                    weatherPreview = weatherPreview,
                    isWeatherLoading = isWeatherLoading,
                    confirmLabel = confirmLabel,
                    onConfirm = {
                        onConfirm(latLng.latitude, latLng.longitude, cityName.ifEmpty { "Unknown" })
                    }
                )
            }
            }
        }
    }
}
