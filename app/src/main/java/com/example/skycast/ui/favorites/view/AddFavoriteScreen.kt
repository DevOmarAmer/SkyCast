package com.example.skycast.ui.favorites.view

import androidx.compose.runtime.Composable
import com.example.skycast.data.model.FavoriteLocation
import com.example.skycast.ui.map.view.MapPickerScreen
import com.example.skycast.ui.favorites.viewModel.FavoritesViewModel

@Composable
fun AddFavoriteScreen(
    viewModel: FavoritesViewModel,
    onNavigateBack: () -> Unit
) {
    MapPickerScreen(
        screenTitle = "Add Favorite",
        confirmLabel = "Save to Favorites",
        onBack = onNavigateBack,
        onConfirm = { lat, lon, cityName ->
            viewModel.addLocation(
                FavoriteLocation(
                    cityName = cityName,
                    latitude = lat,
                    longitude = lon
                )
            )
            onNavigateBack()
        }
    )
}