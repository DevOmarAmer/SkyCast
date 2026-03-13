package com.example.skycast.ui.settings.view

import androidx.compose.runtime.Composable
import com.example.skycast.ui.map.view.MapPickerScreen
import com.example.skycast.ui.settings.viewModel.SettingsViewModel

@Composable
fun MapLocationPickerScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onLocationSet: (lat: Double, lon: Double, cityName: String) -> Unit
) {
    MapPickerScreen(
        screenTitle = "Set My Location",
        confirmLabel = "Set as My Location",
        onBack = onNavigateBack,
        onConfirm = onLocationSet
    )
}
