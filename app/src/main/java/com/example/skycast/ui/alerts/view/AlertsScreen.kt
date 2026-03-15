package com.example.skycast.ui.alerts.view

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.skycast.BuildConfig
import com.example.skycast.R
import com.example.skycast.ui.alerts.view.components.AddConditionAlertDialog
import com.example.skycast.ui.alerts.view.components.ConditionAlertCard
import com.example.skycast.ui.alerts.view.components.MorningBriefCard
import com.example.skycast.ui.alerts.view.components.MorningBriefTimePicker
import com.example.skycast.ui.alerts.viewModel.AlertsViewModel
import com.example.skycast.ui.theme.*

@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel,
    location: Pair<Double, Double>?,  // ← plain data, injected by nav host
    apiKey: String                    // ← plain data, injected by nav host
) {
    val context       = LocalContext.current
    val alerts        by viewModel.alertsList.collectAsStateWithLifecycle()
    val briefEnabled  by viewModel.morningBriefEnabled.collectAsStateWithLifecycle()
    val briefHour     by viewModel.morningBriefHour.collectAsStateWithLifecycle()
    val briefMinute   by viewModel.morningBriefMinute.collectAsStateWithLifecycle()

    var showAddDialog   by remember { mutableStateOf(false) }
    var showTimePicker  by remember { mutableStateOf(false) }
    var alertToDelete   by remember { mutableStateOf<com.example.skycast.data.model.WeatherAlert?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyDeepNavy, DarkSurface)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(SkyBlue.copy(alpha = 0.25f), SkyDeepNavy.copy(alpha = 0f))))
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(
                    text = stringResource(R.string.alerts_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.W600,
                    color = CloudWhite
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {

                // ── Morning Brief Card ─────────────────────────────────────────
                item {
                    MorningBriefCard(
                        enabled   = briefEnabled,
                        hour      = briefHour,
                        minute    = briefMinute,
                        onToggle  = { nowEnabled ->
                            val loc = location ?: Pair(30.0444, 31.2357)
                            viewModel.setMorningBriefEnabled(
                                enabled = nowEnabled,
                                hour    = briefHour,
                                minute  = briefMinute,
                                lat     = loc.first,
                                lon     = loc.second,
                                apiKey  = apiKey.ifEmpty { BuildConfig.API_KEY }
                            )
                            val msg = if (nowEnabled) context.getString(R.string.brief_enabled) else context.getString(R.string.brief_disabled)
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        onTimeClick = { showTimePicker = true }
                    )
                }

                // ── Section header for condition alerts ────────────────────────
                item {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.condition_alerts_title),
                            style = MaterialTheme.typography.labelLarge,
                            color = SkyBluePale,
                            fontWeight = FontWeight.W600
                        )
                        Spacer(Modifier.width(8.dp))
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = FrostStrong
                        )
                    }
                }

                // ── Empty state for alerts ─────────────────────────────────────
                if (alerts.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🔔", fontSize = 56.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.no_alerts),
                                style = MaterialTheme.typography.titleMedium,
                                color = CloudWhite, fontWeight = FontWeight.W600
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.add_alert_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = CloudGrey, textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    itemsIndexed(alerts, key = { _, a -> a.id }) { index, alert ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { visible = true }
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(300 + index * 60)) +
                                    slideInVertically(tween(300 + index * 60)) { it / 2 }
                        ) {
                            ConditionAlertCard(
                                alert    = alert,
                                onDelete = { alertToDelete = alert }
                            )
                        }
                    }
                }
            }
        }

        // ── FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp, 24.dp, 24.dp, 120.dp),
            containerColor = SkyBlueBright,
            contentColor   = CloudWhite,
            shape          = CircleShape,
            elevation      = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_alert))
        }
    }

    // ── Add Condition Alert Dialog ────────────────────────────────────────────
    if (showAddDialog) {
        val loc = location ?: Pair(30.0444, 31.2357)
        AddConditionAlertDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { condType, threshold, alertType, label, startDt, endDt ->
                viewModel.scheduleConditionAlert(
                    conditionType = condType,
                    threshold     = threshold,
                    alertType     = alertType,
                    label         = label,
                    lat           = loc.first,
                    lon           = loc.second,
                    apiKey        = apiKey.ifEmpty { BuildConfig.API_KEY },
                    startDateTime = startDt,
                    endDateTime   = endDt
                )
                showAddDialog = false
                Toast.makeText(context, context.getString(R.string.alert_scheduled), Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ── Time Picker Dialog ────────────────────────────────────────────────────
    if (showTimePicker) {
        MorningBriefTimePicker(
            initialHour   = briefHour,
            initialMinute = briefMinute,
            onDismiss     = { showTimePicker = false },
            onConfirm     = { h, m ->
                showTimePicker = false
                val loc = location ?: Pair(30.0444, 31.2357)
                viewModel.updateMorningBriefTime(
                    hour   = h,
                    minute = m,
                    lat    = loc.first,
                    lon    = loc.second,
                    apiKey = apiKey.ifEmpty { BuildConfig.API_KEY }
                )
                Toast.makeText(context, context.getString(R.string.brief_time_updated), Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ── Delete Confirmation Dialog ───────────────────────────────────────────
    if (alertToDelete != null) {
        AlertDialog(
            onDismissRequest = { alertToDelete = null },
            title = { Text(stringResource(R.string.delete_alert_title), color = CloudWhite, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.delete_alert_message), color = CloudGrey) },
            confirmButton = {
                Button(
                    onClick = {
                        alertToDelete?.let {
                            viewModel.deleteAlert(it)
                            Toast.makeText(context, context.getString(R.string.alert_removed), Toast.LENGTH_SHORT).show()
                        }
                        alertToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StormRed)
                ) {
                    Text(stringResource(R.string.delete), color = CloudWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { alertToDelete = null }) {
                    Text(stringResource(R.string.cancel), color = SkyBlueBright)
                }
            },
            containerColor = SkyNavy,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
