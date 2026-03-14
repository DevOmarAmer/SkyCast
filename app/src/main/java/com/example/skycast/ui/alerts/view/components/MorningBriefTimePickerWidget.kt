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
import androidx.compose.ui.window.Dialog
import com.example.skycast.R
import com.example.skycast.ui.theme.*

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
