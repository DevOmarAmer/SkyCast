package com.example.skycast.ui.settings

import androidx.compose.runtime.Composable
import com.example.skycast.ui.MapPickerScreen

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
