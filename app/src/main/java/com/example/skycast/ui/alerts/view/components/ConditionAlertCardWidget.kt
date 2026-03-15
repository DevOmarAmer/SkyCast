package com.example.skycast.ui.alerts.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skycast.R
import com.example.skycast.data.local.entity.AlertCondition
import com.example.skycast.data.local.entity.WeatherAlert
import com.example.skycast.ui.theme.*
import com.example.skycast.utils.AlertUtils
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ConditionAlertCard(alert: WeatherAlert, onDelete: () -> Unit) {
    val context = LocalContext.current
    val localizedLabel = AlertUtils.buildLocalizedLabel(context, alert.conditionType, alert.threshold)
    val style = conditionStyle(alert.conditionType)
    val emoji = style.first
    val bgColor = style.second
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
                        text = localizedLabel,
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

@Composable
internal fun conditionStyle(conditionType: String): Pair<String, Color> = when (conditionType) {
    AlertCondition.TEMP_ABOVE    -> "🌡️" to SunGold.copy(alpha = 0.2f)
    AlertCondition.TEMP_BELOW    -> "🥶" to RainBlue.copy(alpha = 0.2f)
    AlertCondition.WIND_ABOVE    -> "💨" to SkyBluePale.copy(alpha = 0.2f)
    AlertCondition.RAIN_EXPECTED -> "🌧️" to RainBlue.copy(alpha = 0.2f)
    AlertCondition.VERY_CLOUDY   -> "☁️" to CloudGrey.copy(alpha = 0.2f)
    else                         -> "🔔" to FrostStrong.copy(alpha = 0.2f)
}
