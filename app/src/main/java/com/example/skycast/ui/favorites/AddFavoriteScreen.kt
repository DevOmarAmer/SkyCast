package com.example.skycast.ui.favorites

import android.location.Geocoder
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.skycast.data.model.FavoriteLocation
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.util.Locale

@Composable
fun AddFavoriteScreen(
    viewModel: FavoritesViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var cityName by remember { mutableStateOf("") }

    val defaultLocation = LatLng(30.0444, 31.2357)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 6f)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("ابحث عن مدينة...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            trailingIcon = {
                Button(onClick = {
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocationName(searchQuery, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0]
                            val newLatLong = LatLng(address.latitude, address.longitude)
                            selectedLocation = newLatLong
                            cityName = address.locality ?: address.subAdminArea ?: searchQuery
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(newLatLong, 10f)
                        }
                    } catch (e: Exception) {


                    }
                }) {
                    Text("بحث")
                }
            }
        )

        Box(modifier = Modifier.weight(1f)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    selectedLocation = latLng
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            cityName = addresses[0].locality ?: addresses[0].subAdminArea ?: "مكان محدد"
                        } else {
                            cityName = "مكان محدد"
                        }
                    } catch (e: Exception) {
                        cityName = "مكان محدد"
                    }
                }
            ) {
                selectedLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = cityName
                    )
                }
            }
        }

        Button(
            onClick = {
                selectedLocation?.let {
                    val newFavorite = FavoriteLocation(
                        cityName = cityName,
                        latitude = it.latitude,
                        longitude = it.longitude
                    )
                    viewModel.addLocation(newFavorite)
                    onNavigateBack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = selectedLocation != null
        ) {
            Text("حفظ في المفضلة")
        }
    }
}