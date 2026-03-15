package com.example.skycast.ui.alerts.view.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.skycast.R
import com.example.skycast.data.local.entity.AlertCondition
import com.example.skycast.ui.theme.*
import com.example.skycast.utils.AlertUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

    val range = when (selectedCondition.first) {
        AlertCondition.TEMP_ABOVE, AlertCondition.TEMP_BELOW -> Triple(-20f, 50f, "°C")
        AlertCondition.WIND_ABOVE                            -> Triple(0f, 40f, " m/s")
        else                                                 -> Triple(0f, 100f, "%")
    }
    val sliderMin = range.first
    val sliderMax = range.second
    val sliderUnit = range.third

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
                                val label = AlertUtils.buildLocalizedLabel(context, condType, threshold)
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
                    datePickerState.selectedDateMillis?.let { mills ->
                        val utcCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = mills }
                        val newCal = startCal.clone() as Calendar
                        newCal.set(
                            utcCal.get(Calendar.YEAR),
                            utcCal.get(Calendar.MONTH),
                            utcCal.get(Calendar.DAY_OF_MONTH)
                        )
                        startCal = newCal
                    }
                    showStartDatePicker = false
                }) { Text(stringResource(R.string.ok_button), color = SkyBlueBright) }
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
                        val newCal = startCal.clone() as Calendar
                        newCal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        newCal.set(Calendar.MINUTE, timePickerState.minute)
                        startCal = newCal
                        showStartTimePicker = false
                    }, colors = ButtonDefaults.buttonColors(containerColor = SkyBlueBright)) { Text(stringResource(R.string.ok_button)) }
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
                    datePickerState.selectedDateMillis?.let { mills ->
                        val utcCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = mills }
                        val newCal = endCal.clone() as Calendar
                        newCal.set(
                            utcCal.get(Calendar.YEAR),
                            utcCal.get(Calendar.MONTH),
                            utcCal.get(Calendar.DAY_OF_MONTH)
                        )
                        endCal = newCal
                    }
                    showEndDatePicker = false
                }) { Text(stringResource(R.string.ok_button), color = SkyBlueBright) }
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
                        val newCal = endCal.clone() as Calendar
                        newCal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        newCal.set(Calendar.MINUTE, timePickerState.minute)
                        endCal = newCal
                        showEndTimePicker = false
                    }, colors = ButtonDefaults.buttonColors(containerColor = SkyBlueBright)) { Text(stringResource(R.string.ok_button)) }
                }
            }
        }
    }
}
