package com.example.skycast.ui.alerts.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.skycast.R
import com.example.skycast.ui.theme.*
import java.util.Locale

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
