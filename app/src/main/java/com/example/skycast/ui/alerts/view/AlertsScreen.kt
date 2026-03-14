package com.example.skycast.ui.alerts.view

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.skycast.BuildConfig
import com.example.skycast.R
import com.example.skycast.data.model.AlertCondition
import com.example.skycast.data.model.WeatherAlert
import com.example.skycast.ui.alerts.viewModel.AlertsViewModel
import com.example.skycast.ui.home.viewModel.HomeViewModel
import com.example.skycast.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ── Entry Point ────────────────────────────────────────────────────────────────
@Composable
fun AlertsScreen(viewModel: AlertsViewModel, homeViewModel: HomeViewModel) {
    val context       = LocalContext.current
    val alerts        by viewModel.alertsList.collectAsStateWithLifecycle()
    val briefEnabled  by viewModel.morningBriefEnabled.collectAsStateWithLifecycle()
    val briefHour     by viewModel.morningBriefHour.collectAsStateWithLifecycle()
    val briefMinute   by viewModel.morningBriefMinute.collectAsStateWithLifecycle()
    val location      by homeViewModel.currentLocation.collectAsStateWithLifecycle()
    val apiKey        by homeViewModel.exposedApiKey.collectAsStateWithLifecycle()

    var showAddDialog   by remember { mutableStateOf(false) }
    var showTimePicker  by remember { mutableStateOf(false) }

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
                                onDelete = {
                                    viewModel.deleteAlert(alert)
                                    Toast.makeText(context, context.getString(R.string.alert_removed), Toast.LENGTH_SHORT).show()
                                }
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
}

// ── Morning Brief Card ────────────────────────────────────────────────────────
@Composable
fun MorningBriefCard(
    enabled: Boolean,
    hour: Int,
    minute: Int,
    onToggle: (Boolean) -> Unit,
    onTimeClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = Frost),
        border   = BorderStroke(1.dp, FrostStrong)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "☕ " + stringResource(R.string.morning_brief_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.W700,
                        color = CloudWhite
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.morning_brief_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = CloudGrey
                    )
                }
                Switch(
                    checked  = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor  = CloudWhite,
                        checkedTrackColor  = SkyBlueBright,
                        uncheckedThumbColor = CloudGrey,
                        uncheckedTrackColor = FrostStrong
                    )
                )
            }

            if (enabled) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = FrostStrong)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.morning_brief_time_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CloudGrey
                    )
                    OutlinedButton(
                        onClick = onTimeClick,
                        shape  = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SkyBlueBright),
                        border = BorderStroke(1.dp, SkyBlueBright)
                    ) {
                        val label = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.W700,
                            color = SkyBlueBright
                        )
                    }
                }
            }
        }
    }
}

// ── Condition Alert Card ──────────────────────────────────────────────────────
@Composable
fun ConditionAlertCard(alert: WeatherAlert, onDelete: () -> Unit) {
    val (emoji, bgColor) = conditionStyle(alert.conditionType)
    val typeLabel = if (alert.alertType == "notification")
        stringResource(R.string.notification_type) else stringResource(R.string.alarm_type)

    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val startStr = dateFormat.format(alert.startDateTime)
    val endStr   = dateFormat.format(alert.endDateTime)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = Frost),
        border   = BorderStroke(1.dp, FrostStrong)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) { Text(emoji, fontSize = 22.sp) }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alert.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.W600,
                        color = CloudWhite
                    )
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = CloudGrey
                    )
                }

                IconButton(onClick = onDelete) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(StormRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = StormRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, null, tint = SkyBluePale, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "$startStr  ${stringResource(R.string.until)}  $endStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = SkyBluePale
                )
            }
        }
    }
}

// ── Add Condition Alert Dialog ────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddConditionAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: (conditionType: String, threshold: Double, alertType: String, label: String, start: Long, end: Long) -> Unit
) {
    val context = LocalContext.current
    val conditions = listOf(
        Triple(AlertCondition.TEMP_ABOVE,    stringResource(R.string.cond_temp_above),    true),
        Triple(AlertCondition.TEMP_BELOW,    stringResource(R.string.cond_temp_below),    true),
        Triple(AlertCondition.WIND_ABOVE,    stringResource(R.string.cond_wind_above),    true),
        Triple(AlertCondition.RAIN_EXPECTED, stringResource(R.string.cond_rain_expected), false),
        Triple(AlertCondition.VERY_CLOUDY,   stringResource(R.string.cond_very_cloudy),   false),
    )

    var selectedCondition by remember { mutableStateOf(conditions[0]) }
    var sliderValue       by remember { mutableFloatStateOf(30f) }
    var selectedType      by remember { mutableStateOf("notification") }

    // Time states
    val now = Calendar.getInstance()
    var startCal by remember { mutableStateOf(now.clone() as Calendar) }
    var endCal   by remember {
        mutableStateOf((now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) })
    }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker   by remember { mutableStateOf(false) }
    var showEndTimePicker   by remember { mutableStateOf(false) }

    val (sliderMin, sliderMax, sliderUnit) = when (selectedCondition.first) {
        AlertCondition.TEMP_ABOVE, AlertCondition.TEMP_BELOW -> Triple(-20f, 50f, "°C")
        AlertCondition.WIND_ABOVE                            -> Triple(0f, 40f, " m/s")
        else                                                 -> Triple(0f, 100f, "%")
    }

    LaunchedEffect(selectedCondition) {
        sliderValue = when (selectedCondition.first) {
            AlertCondition.TEMP_ABOVE -> 35f
            AlertCondition.TEMP_BELOW -> 5f
            AlertCondition.WIND_ABOVE -> 15f
            else                      -> 0f
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape  = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SkyNavy),
            border = BorderStroke(1.dp, FrostStrong)
        ) {
            LazyColumn(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.new_alert),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.W700,
                        color = CloudWhite
                    )
                }

                // ── Condition Type ─────────────────────────────────────────────
                item {
                    Text(stringResource(R.string.condition_type_label), style = MaterialTheme.typography.labelLarge, color = SkyBluePale)
                    Spacer(Modifier.height(8.dp))
                    conditions.chunked(2).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { cond ->
                                val isSelected = selectedCondition.first == cond.first
                                FilterChip(
                                    selected = isSelected,
                                    onClick  = { selectedCondition = cond },
                                    label    = { Text(cond.second, maxLines = 1) },
                                    modifier = Modifier.weight(1f),
                                    colors   = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SkyBlueBright,
                                        selectedLabelColor     = CloudWhite,
                                        containerColor         = Frost,
                                        labelColor             = CloudGrey
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled             = true,
                                        selected            = isSelected,
                                        borderColor         = FrostStrong,
                                        selectedBorderColor = SkyBlueBright
                                    )
                                )
                            }
                            if (row.size == 1) Box(Modifier.weight(1f))
                        }
                    }
                }

                // ── Threshold Slider ───────────────────────────────────────────
                if (selectedCondition.third) {
                    item {
                        Text(stringResource(R.string.threshold_label), style = MaterialTheme.typography.labelLarge, color = SkyBluePale)
                        Text(
                            text = "${sliderValue.toInt()}$sliderUnit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.W700,
                            color = SkyBlueBright
                        )
                        Slider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it },
                            valueRange = sliderMin..sliderMax,
                            colors = SliderDefaults.colors(
                                thumbColor        = SkyBlueBright,
                                activeTrackColor  = SkyBlueBright,
                                inactiveTrackColor = FrostStrong
                            )
                        )
                    }
                }

                // ── Time Range Selection ───────────────────────────────────────
                item {
                    Text(stringResource(R.string.time_range), style = MaterialTheme.typography.labelLarge, color = SkyBluePale)
                    Spacer(Modifier.height(8.dp))

                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                    // Start Time Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Frost)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.start_time), color = CloudWhite, style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = dateFormat.format(startCal.time),
                                color = SkyBlueBright,
                                modifier = Modifier.clickable { showStartDatePicker = true }
                            )
                            Text(
                                text = timeFormat.format(startCal.time),
                                color = SkyBlueBright,
                                modifier = Modifier.clickable { showStartTimePicker = true }
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // End Time Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Frost)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.end_time), color = CloudWhite, style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = dateFormat.format(endCal.time),
                                color = SkyBlueBright,
                                modifier = Modifier.clickable { showEndDatePicker = true }
                            )
                            Text(
                                text = timeFormat.format(endCal.time),
                                color = SkyBlueBright,
                                modifier = Modifier.clickable { showEndTimePicker = true }
                            )
                        }
                    }
                }

                // ── Alert Type ─────────────────────────────────────────────────
                item {
                    Text(stringResource(R.string.alert_type), style = MaterialTheme.typography.labelLarge, color = SkyBluePale)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "notification" to "🔔 ${stringResource(R.string.notification)}",
                            "alarm"        to "⏰ ${stringResource(R.string.alarm)}"
                        ).forEach { (value, label) ->
                            val isSelected = selectedType == value
                            FilterChip(
                                selected = isSelected,
                                onClick  = { selectedType = value },
                                label    = { Text(label) },
                                modifier = Modifier.weight(1f),
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SkyBlueBright,
                                    selectedLabelColor     = CloudWhite,
                                    containerColor         = Frost,
                                    labelColor             = CloudGrey
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled             = true,
                                    selected            = isSelected,
                                    borderColor         = FrostStrong,
                                    selectedBorderColor = SkyBlueBright
                                )
                            )
                        }
                    }
                }

                // ── Action Buttons ─────────────────────────────────────────────
                item {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick  = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = CloudGrey)
                        ) { Text(stringResource(R.string.cancel)) }

                        Button(
                            onClick = {
                                if (endCal.timeInMillis <= startCal.timeInMillis) {
                                    Toast.makeText(context, "End time must be after start time", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val condType  = selectedCondition.first
                                val threshold = if (selectedCondition.third) sliderValue.toDouble() else 0.0
                                val label = buildLabel(context, condType, threshold, selectedCondition.second)
                                onConfirm(condType, threshold, selectedType, label, startCal.timeInMillis, endCal.timeInMillis)
                            },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = SkyBlueBright)
                        ) { Text(stringResource(R.string.save_alert)) }
                    }
                }
            }
        }
    }

    // Handlers for Pickers
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startCal.timeInMillis)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { startCal.timeInMillis = it }
                    showStartDatePicker = false
                }) { Text("OK", color = SkyBlueBright) }
            }
        ) { DatePicker(state = datePickerState) }
    }
    if (showStartTimePicker) {
        val timePickerState = rememberTimePickerState(initialHour = startCal.get(Calendar.HOUR_OF_DAY), initialMinute = startCal.get(Calendar.MINUTE), is24Hour = false)
        Dialog(onDismissRequest = { showStartTimePicker = false }) {
            Card(colors = CardDefaults.cardColors(containerColor = Frost)) {
                Column(Modifier.padding(16.dp)) {
                    TimePicker(state = timePickerState)
                    Button(onClick = {
                        startCal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        startCal.set(Calendar.MINUTE, timePickerState.minute)
                        showStartTimePicker = false
                    }, colors = ButtonDefaults.buttonColors(containerColor = SkyBlueBright)) { Text("OK") }
                }
            }
        }
    }
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endCal.timeInMillis)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { endCal.timeInMillis = it }
                    showEndDatePicker = false
                }) { Text("OK", color = SkyBlueBright) }
            }
        ) { DatePicker(state = datePickerState) }
    }
    if (showEndTimePicker) {
        val timePickerState = rememberTimePickerState(initialHour = endCal.get(Calendar.HOUR_OF_DAY), initialMinute = endCal.get(Calendar.MINUTE), is24Hour = false)
        Dialog(onDismissRequest = { showEndTimePicker = false }) {
            Card(colors = CardDefaults.cardColors(containerColor = Frost)) {
                Column(Modifier.padding(16.dp)) {
                    TimePicker(state = timePickerState)
                    Button(onClick = {
                        endCal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        endCal.set(Calendar.MINUTE, timePickerState.minute)
                        showEndTimePicker = false
                    }, colors = ButtonDefaults.buttonColors(containerColor = SkyBlueBright)) { Text("OK") }
                }
            }
        }
    }
}

private fun buildLabel(context: android.content.Context, condType: String, threshold: Double, condName: String): String = when (condType) {
    AlertCondition.TEMP_ABOVE    -> "${context.getString(R.string.cond_temp_above)}: ${threshold.toInt()}°C"
    AlertCondition.TEMP_BELOW    -> "${context.getString(R.string.cond_temp_below)}: ${threshold.toInt()}°C"
    AlertCondition.WIND_ABOVE    -> "${context.getString(R.string.cond_wind_above)}: ${threshold.toInt()} m/s"
    AlertCondition.RAIN_EXPECTED -> context.getString(R.string.cond_rain_expected)
    AlertCondition.VERY_CLOUDY   -> context.getString(R.string.cond_very_cloudy)
    else                         -> condName
}

// ── Morning Brief Time Picker ─────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorningBriefTimePicker(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = false)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape  = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SkyNavy),
            border = BorderStroke(1.dp, FrostStrong)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.pick_brief_time),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.W700,
                    color = CloudWhite
                )
                Spacer(Modifier.height(16.dp))
                TimePicker(
                    state  = state,
                    colors = TimePickerDefaults.colors(
                        clockDialColor          = Frost,
                        selectorColor           = SkyBlueBright,
                        containerColor          = SkyNavy,
                        clockDialSelectedContentColor = CloudWhite,
                        clockDialUnselectedContentColor = CloudGrey,
                        periodSelectorBorderColor = FrostStrong,
                        timeSelectorSelectedContainerColor = SkyBlueBright,
                        timeSelectorUnselectedContainerColor = Frost,
                        timeSelectorSelectedContentColor = CloudWhite,
                        timeSelectorUnselectedContentColor = CloudGrey
                    )
                )
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = CloudGrey)
                    ) { Text(stringResource(R.string.cancel)) }

                    Button(
                        onClick  = { onConfirm(state.hour, state.minute) },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = SkyBlueBright)
                    ) { Text(stringResource(R.string.confirm)) }
                }
            }
        }
    }
}

// ── Helper – condition style ──────────────────────────────────────────────────
@Composable
private fun conditionStyle(conditionType: String): Pair<String, androidx.compose.ui.graphics.Color> = when (conditionType) {
    AlertCondition.TEMP_ABOVE    -> "🌡️" to SunGold.copy(alpha = 0.2f)
    AlertCondition.TEMP_BELOW    -> "🥶" to RainBlue.copy(alpha = 0.2f)
    AlertCondition.WIND_ABOVE    -> "💨" to SkyBluePale.copy(alpha = 0.2f)
    AlertCondition.RAIN_EXPECTED -> "🌧️" to RainBlue.copy(alpha = 0.2f)
    AlertCondition.VERY_CLOUDY   -> "☁️" to CloudGrey.copy(alpha = 0.2f)
    else                         -> "🔔" to FrostStrong.copy(alpha = 0.2f)
}
