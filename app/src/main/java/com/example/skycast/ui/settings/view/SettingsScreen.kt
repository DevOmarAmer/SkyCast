package com.example.skycast.ui.settings.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skycast.R
import com.example.skycast.ui.settings.viewModel.SettingsViewModel
import com.example.skycast.ui.theme.*
import com.example.skycast.ui.settings.view.components.SegmentedOptions
import com.example.skycast.ui.settings.view.components.SettingsSection

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onOpenMap: () -> Unit = {}) {
    val currentTempUnit    by viewModel.tempUnit.collectAsStateWithLifecycle()
    val currentWindUnit    by viewModel.windUnit.collectAsStateWithLifecycle()
    val currentLang        by viewModel.language.collectAsStateWithLifecycle()
    val currentLocMethod   by viewModel.locationMethod.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyDeepNavy, DarkSurface)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
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
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.W600,
                    color = CloudWhite
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SettingsSection(
                icon = Icons.Outlined.LocationOn,
                title = stringResource(R.string.location_method)
            ) {
                SegmentedOptions(
                    options = listOf(stringResource(R.string.gps) to "gps", stringResource(R.string.map) to "map"),
                    selected = currentLocMethod,
                    onSelect = { viewModel.saveLocationMethod(it) }
                )

                // When map mode is selected, show the open-map button
                AnimatedVisibility(
                    visible = currentLocMethod == "map",
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(200))
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onOpenMap,
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBlueBright)
                        ) {
                            Icon(
                                Icons.Outlined.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Open Map to Set Location →",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.W600,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            SettingsSection(
                icon = Icons.Outlined.Thermostat,
                title = stringResource(R.string.temperature_unit)
            ) {
                SegmentedOptions(
                    options = listOf(stringResource(R.string.celsius) to "metric", stringResource(R.string.fahrenheit) to "imperial", stringResource(R.string.kelvin) to "standard"),
                    selected = currentTempUnit,
                    onSelect = { viewModel.saveTempUnit(it) }
                )
            }

            SettingsSection(
                icon = Icons.Outlined.Air,
                title = stringResource(R.string.wind_speed_unit)
            ) {
                SegmentedOptions(
                    options = listOf(stringResource(R.string.meter_sec) to "m/s", stringResource(R.string.miles_hour) to "mph"),
                    selected = currentWindUnit,
                    onSelect = { viewModel.saveWindUnit(it) }
                )
            }

            SettingsSection(
                icon = Icons.Outlined.Language,
                title = stringResource(R.string.language)
            ) {
                SegmentedOptions(
                    options = listOf(stringResource(R.string.english) to "en", stringResource(R.string.arabic) to "ar"),
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
