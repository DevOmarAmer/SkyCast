package com.example.skycast.ui.alerts

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
import androidx.compose.material.icons.filled.Delete

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.work.*
import com.example.skycast.R
import com.example.skycast.ui.theme.*
import com.example.skycast.utils.WeatherWorker
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// ── Simple in-memory alert model (will be replaced by Room entity in Phase 3) ─
data class AlertItem(
    val id: String = UUID.randomUUID().toString(),
    val delayMinutes: Long,
    val type: String,         // "notification" | "alarm"
    val createdAt: Long = System.currentTimeMillis()
)

@Composable
fun AlertsScreen() {
    val context = LocalContext.current
    val alerts = remember { mutableStateListOf<AlertItem>() }
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyDeepNavy, DarkSurface)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Bar ───────────────────────────────────────────────────────
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

            // ── List or Empty State ───────────────────────────────────────────
            if (alerts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Text("🔔", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.no_alerts),
                            style = MaterialTheme.typography.titleLarge,
                            color = CloudWhite, fontWeight = FontWeight.W600
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.add_alert_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CloudGrey, textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    itemsIndexed(alerts, key = { _, a -> a.id }) { index, alert ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { visible = true }

                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(300 + index * 60)) +
                                    slideInVertically(tween(300 + index * 60)) { it / 2 }
                        ) {
                            AlertCard(
                                alert = alert,
                                onDelete = {
                                    WorkManager.getInstance(context).cancelWorkById(UUID.fromString(alert.id))
                                    alerts.remove(alert)
                                    val alertRemovedMsg = context.getString(R.string.alert_removed)
                                    Toast.makeText(context, alertRemovedMsg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }

        // ── FAB ───────────────────────────────────────────────────────────────
        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp, 24.dp,24.dp, 120.dp),
            containerColor = SkyBlueBright,
            contentColor = CloudWhite,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Alert")
        }
    }

    // ── Add Alert Dialog ──────────────────────────────────────────────────────
    if (showDialog) {
        AddAlertDialog(
            onDismiss = { showDialog = false },
            onConfirm = { delayMinutes, type ->
                val inputData = Data.Builder()
                    .putString("alert_type", type)
                    .build()

                val request = OneTimeWorkRequestBuilder<WeatherWorker>()
                    .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                    .setInputData(inputData)
                    .build()

                WorkManager.getInstance(context).enqueue(request)

                val item = AlertItem(
                    id = request.id.toString(),
                    delayMinutes = delayMinutes,
                    type = type
                )
                alerts.add(item)
                showDialog = false
                val scheduledMsg = context.getString(R.string.alert_scheduled)
                Toast.makeText(context, scheduledMsg, Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// ── Alert Card ────────────────────────────────────────────────────────────────
@Composable
fun AlertCard(alert: AlertItem, onDelete: () -> Unit) {
    val emoji = if (alert.type == "notification") "🔔" else "⏰"
    val typeLabel = if (alert.type == "notification") stringResource(R.string.notification_type) else stringResource(R.string.alarm_type)
    val timeLabel = when {
        alert.delayMinutes < 60 -> "In ${alert.delayMinutes} min"
        alert.delayMinutes == 60L -> "In 1 hour"
        else -> "In ${alert.delayMinutes / 60}h ${alert.delayMinutes % 60}m"
    }
    val createdLabel = SimpleDateFormat("HH:mm · dd MMM", Locale.getDefault())
        .format(Date(alert.createdAt))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Frost),
        border = androidx.compose.foundation.BorderStroke(1.dp, FrostStrong)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (alert.type == "notification") RainBlue.copy(alpha = 0.2f)
                        else SunGold.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.W600,
                    color = CloudWhite
                )
                Text(
                    text = "$typeLabel · ${stringResource(R.string.set_at)} $createdLabel",
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
                        contentDescription = "Delete",
                        tint = StormRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ── Add Alert Dialog ──────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long, String) -> Unit
) {
    val durationOptions = listOf(
        15L to stringResource(R.string.min_15),
        30L to stringResource(R.string.min_30),
        60L to stringResource(R.string.hour_1),
        120L to stringResource(R.string.hour_2)
    )
    var selectedDuration by remember { mutableStateOf(15L) }
    var selectedType by remember { mutableStateOf("notification") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SkyNavy),
            border = androidx.compose.foundation.BorderStroke(1.dp, FrostStrong)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.new_alert),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.W700,
                    color = CloudWhite
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Duration
                Text(stringResource(R.string.notify_after), style = MaterialTheme.typography.labelLarge, color = SkyBluePale)
                Spacer(modifier = Modifier.height(10.dp))

                // 2x2 grid of duration chips
                val chunked = durationOptions.chunked(2)
                chunked.forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (min, label) ->
                            val isSelected = selectedDuration == min
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDuration = min },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SkyBlueBright,
                                    selectedLabelColor = CloudWhite,
                                    containerColor = Frost,
                                    labelColor = CloudGrey
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = FrostStrong,
                                    selectedBorderColor = SkyBlueBright
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Alert type
                Text(stringResource(R.string.alert_type), style = MaterialTheme.typography.labelLarge, color = SkyBluePale)
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("notification" to "🔔 ${stringResource(R.string.notification)}", "alarm" to "⏰ ${stringResource(R.string.alarm)}").forEach { (value, label) ->
                        val isSelected = selectedType == value
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedType = value },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SkyBlueBright,
                                selectedLabelColor = CloudWhite,
                                containerColor = Frost,
                                labelColor = CloudGrey
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = FrostStrong,
                                selectedBorderColor = SkyBlueBright
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CloudGrey)
                    ) { Text(stringResource(R.string.cancel)) }

                    Button(
                        onClick = { onConfirm(selectedDuration, selectedType) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBlueBright)
                    ) { Text(stringResource(R.string.save_alert)) }
                }
            }
        }
    }
}