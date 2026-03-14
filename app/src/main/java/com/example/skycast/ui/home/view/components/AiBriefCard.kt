package com.example.skycast.ui.home.view.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skycast.R
import com.example.skycast.data.model.ForecastItem
import com.example.skycast.ui.home.viewModel.AiState
import com.example.skycast.ui.theme.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection

// ── AI Brief Card ─────────────────────────────────────────────────────────────
@Composable
fun AiBriefCard(aiState: AiState, currentWeather: ForecastItem, cityName: String, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ai_alpha"
    )

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, Brush.linearGradient(listOf(SkyBluePale.copy(alpha=0.5f), CloudGrey.copy(alpha=0.1f))))
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = stringResource(R.string.ai),
                        tint = SunGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SkyCast AI Brief",
                        style = MaterialTheme.typography.titleSmall,
                        color = SkyBluePale,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                when (aiState) {
                    is AiState.Loading -> {
                        Text(
                            text = "Generating your personalized weather brief...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CloudGrey.copy(alpha = alphaAnim)
                        )
                    }
                    is AiState.Success -> {
                        Text(
                            text = aiState.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CloudWhite,
                            lineHeight = 22.sp,
                            textAlign = if (isRtl) TextAlign.Right else TextAlign.Left
                        )
                    }
                    is AiState.Error -> {
                        // Fallback to static text
                        val tempC = currentWeather.main.temp.toInt()
                        val desc = currentWeather.weatherInfo.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "Clear"
                        val humidity = currentWeather.main.humidity
                        val wind = currentWeather.wind.speed.toInt()

                        val outfit = when {
                            tempC >= 35 -> stringResource(R.string.outfit_hot)
                            tempC >= 25 -> stringResource(R.string.outfit_warm)
                            tempC >= 15 -> stringResource(R.string.outfit_mild)
                            tempC >= 5  -> stringResource(R.string.outfit_cool)
                            else        -> stringResource(R.string.outfit_cold)
                        }
                        val msg = stringResource(R.string.morning_brief_message_format, cityName, tempC, desc, humidity, wind, outfit)
                        val disclaimer = stringResource(R.string.ai_error_disclaimer)

                        Text(
                            text = disclaimer + msg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CloudWhite,
                            lineHeight = 22.sp,
                            textAlign = if (isRtl) TextAlign.Right else TextAlign.Left
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}
