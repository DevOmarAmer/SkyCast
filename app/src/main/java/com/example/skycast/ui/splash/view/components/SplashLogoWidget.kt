package com.example.skycast.ui.splash.view.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skycast.ui.theme.SkyBlue
import com.example.skycast.ui.theme.SkyBlueBright
import com.example.skycast.ui.theme.SkyBluePale
import com.example.skycast.ui.theme.SkyNavy
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashLogoWidget(
    ringRotation: Float,
    pulse: Float,
    logoScale: Float,
    logoAlpha: Float,
    floatOffset: Float
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(180.dp)
    ) {
        // Outer rotating dashed ring
        Canvas(modifier = Modifier.size(180.dp)) {
            drawRotatingRing(ringRotation, SkyBluePale.copy(alpha = 0.35f), 6f, size.width / 2)
        }

        // Pulsing glow circle
        Box(
            modifier = Modifier
                .size((130 * pulse).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            SkyBlueBright.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        // Logo container (frosted circle)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(120.dp)
                .scale(logoScale)
                .alpha(logoAlpha)
                .background(
                    Brush.radialGradient(
                        colors = listOf(SkyBlue.copy(alpha = 0.7f), SkyNavy)
                    ),
                    CircleShape
                )
                .drawBehind {
                    drawCircle(
                        color = SkyBluePale.copy(alpha = 0.4f),
                        radius = size.minDimension / 2,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                .offset(y = floatOffset.dp)
        ) {
            Text(
                text = "⛅",
                fontSize = 52.sp
            )
        }
    }
}

// ── Rotating dashed ring ──────────────────────────────────────────────────────
private fun DrawScope.drawRotatingRing(
    rotationDeg: Float,
    color: Color,
    strokeWidth: Float,
    radius: Float
) {
    val dashCount = 20
    val dashAngle = 360f / dashCount
    val dashLength = dashAngle * 0.5f
    for (i in 0 until dashCount) {
        val startAngle = rotationDeg + i * dashAngle
        val endAngle = startAngle + dashLength
        val startRad = Math.toRadians(startAngle.toDouble())
        val endRad = Math.toRadians(endAngle.toDouble())
        drawLine(
            color = color,
            start = Offset(
                x = center.x + (radius * cos(startRad)).toFloat(),
                y = center.y + (radius * sin(startRad)).toFloat()
            ),
            end = Offset(
                x = center.x + (radius * cos(endRad)).toFloat(),
                y = center.y + (radius * sin(endRad)).toFloat()
            ),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}
