package com.example.skycast.ui.map

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.skycast.ui.theme.*
import com.google.android.gms.maps.model.LatLng

data class WeatherPreview(
    val temp: Int,
    val description: String,
    val icon: String,
    val humidity: Int,
    val windSpeed: Double,
    val conditionId: Int,
    val cloudsPct: Int
)


@Composable
fun MapLocationBottomSheet(
    cityName: String,
    countryName: String,
    latLng: LatLng,
    weatherPreview: WeatherPreview?,
    isWeatherLoading: Boolean,
    confirmLabel: String,
    onConfirm: () -> Unit
) {
        val wc = LocalWeatherColors.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        colors = CardDefaults.cardColors(containerColor = wc.bgBottom),
        border = androidx.compose.foundation.BorderStroke(1.dp, wc.accent.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {

            // ── Drag handle ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .background(FrostStrong, CircleShape)
            )

            Spacer(Modifier.height(18.dp))

            // ── Location header ────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Pin icon box
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(wc.heroGlow.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📍", fontSize = 20.sp)
                }

                Spacer(Modifier.width(12.dp))

                // City + country
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cityName.ifEmpty { "Selected Location" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.W700,
                        color = CloudWhite
                    )
                    if (countryName.isNotEmpty()) {
                        Text(
                            text = countryName,
                            style = MaterialTheme.typography.bodySmall,
                            color = wc.accent.copy(alpha = 0.8f)
                        )
                    }
                }

                // Coordinates
                Column(horizontalAlignment = Alignment.End) {
                    CoordLabel("${"%.4f".format(latLng.latitude)}°N")
                    CoordLabel("${"%.4f".format(latLng.longitude)}°E")
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = FrostStrong, thickness = 0.5.dp)
            Spacer(Modifier.height(16.dp))

            // ── Weather preview ────────────────────────────────────────────────
            WeatherPreviewSection(
                isLoading = isWeatherLoading,
                preview = weatherPreview
            )

            Spacer(Modifier.height(18.dp))

            // ── Confirm button ─────────────────────────────────────────────────
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = wc.accent)
            ) {
                Text(
                    text = confirmLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.W700
                )
            }
        }
    }
}

// ── Private sub-composables ───────────────────────────────────────────────────

@Composable
private fun CoordLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = CloudGrey
    )
}

@Composable
private fun WeatherPreviewSection(isLoading: Boolean, preview: WeatherPreview?) {
    when {
        isLoading -> {
            val wc = LocalWeatherColors.current
            // Skeleton / loading row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(wc.cardSurface, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = wc.accent,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Fetching weather…",
                    style = MaterialTheme.typography.bodySmall,
                    color = CloudGrey
                )
            }
        }

        preview != null -> {
            val wc = LocalWeatherColors.current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        wc.cardSurface,
                        RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                AsyncImage(
                    model = "https://openweathermap.org/img/wn/${preview.icon}@2x.png",
                    contentDescription = null,
                    modifier = Modifier.size(56.dp)
                )

                Spacer(Modifier.width(8.dp))

                // Temp + description
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${preview.temp}°C",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.W700,
                        color = CloudWhite
                    )
                    Text(
                        text = preview.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = wc.accent.copy(alpha = 0.9f)
                    )
                }

                // Stat chips
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    WeatherChip(emoji = "💧", value = "${preview.humidity}%")
                    WeatherChip(emoji = "💨", value = "${preview.windSpeed} m/s")
                }
            }
        }

        else -> {
            val wc = LocalWeatherColors.current
            // No selection yet — empty state
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(wc.cardSurface, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Tap the map to see weather details",
                    style = MaterialTheme.typography.bodySmall,
                    color = CloudGrey
                )
            }
        }
    }
}

@Composable
private fun WeatherChip(emoji: String, value: String) {
    Row(
        modifier = Modifier
            .background(FrostStrong, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(emoji, fontSize = 12.sp)
        Text(value, style = MaterialTheme.typography.labelSmall, color = CloudGrey)
    }
}
