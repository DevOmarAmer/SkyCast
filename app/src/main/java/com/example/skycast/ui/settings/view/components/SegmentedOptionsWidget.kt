package com.example.skycast.ui.settings.view.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.skycast.ui.theme.*

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
