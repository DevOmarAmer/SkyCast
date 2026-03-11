package com.example.skycast.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skycast.ui.theme.*

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val currentTempUnit    by viewModel.tempUnit.collectAsState()
    val currentWindUnit    by viewModel.windUnit.collectAsState()
    val currentLang        by viewModel.language.collectAsState()
    val currentLocMethod   by viewModel.locationMethod.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyDeepNavy, DarkSurface)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Top Bar ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(SkyBlue.copy(alpha = 0.25f), SkyDeepNavy.copy(alpha = 0f))))
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.W600,
                    color = CloudWhite
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Location Method ───────────────────────────────────────────────
            SettingsSection(
                icon = Icons.Outlined.LocationOn,
                title = "Location Method"
            ) {
                SegmentedOptions(
                    options = listOf("GPS" to "gps", "Map" to "map"),
                    selected = currentLocMethod,
                    onSelect = { viewModel.saveLocationMethod(it) }
                )
            }

            // ── Temperature Unit ──────────────────────────────────────────────
            SettingsSection(
                icon = Icons.Outlined.Thermostat,
                title = "Temperature Unit"
            ) {
                SegmentedOptions(
                    options = listOf("°C" to "metric", "°F" to "imperial", "K" to "standard"),
                    selected = currentTempUnit,
                    onSelect = { viewModel.saveTempUnit(it) }
                )
            }

            // ── Wind Speed Unit ───────────────────────────────────────────────
            SettingsSection(
                icon = Icons.Outlined.Air,
                title = "Wind Speed"
            ) {
                SegmentedOptions(
                    options = listOf("m/s" to "m/s", "mph" to "mph"),
                    selected = currentWindUnit,
                    onSelect = { viewModel.saveWindUnit(it) }
                )
            }

            // ── Language ──────────────────────────────────────────────────────
            SettingsSection(
                icon = Icons.Outlined.Language,
                title = "Language"
            ) {
                SegmentedOptions(
                    options = listOf("English" to "en", "العربية" to "ar"),
                    selected = currentLang,
                    onSelect = { viewModel.saveLanguage(it) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App info footer
            Text(
                text = "SkyCast v1.0  ·  Powered by OpenWeatherMap",
                style = MaterialTheme.typography.labelSmall,
                color = CloudGrey.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 32.dp)
            )
        }
    }
}

@Composable
fun SettingsSection(
    icon: ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Section label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SkyBlueBright,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = SkyBluePale,
                fontWeight = FontWeight.W600,
                letterSpacing = 0.8.sp
            )
        }
        // Card container
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Frost),
            border = androidx.compose.foundation.BorderStroke(1.dp, FrostStrong)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SegmentedOptions(
    options: List<Pair<String, String>>,  // label -> value
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SkyDeepNavy.copy(alpha = 0.5f)),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        options.forEachIndexed { index, (label, value) ->
            val isSelected = selected == value
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) SkyBlueBright else Color.Transparent,
                animationSpec = tween(250),
                label = "segBg_$index"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) CloudWhite else CloudGrey,
                animationSpec = tween(250),
                label = "segText_$index"
            )

            TextButton(
                onClick = { onSelect(value) },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor),
                colors = ButtonDefaults.textButtonColors(contentColor = textColor)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.W600 else FontWeight.W400
                )
            }
        }
    }
}