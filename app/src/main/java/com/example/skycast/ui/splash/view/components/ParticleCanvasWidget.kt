package com.example.skycast.ui.splash.view.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.skycast.ui.theme.*

@Composable
fun ParticleCanvasWidget() {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val particles = remember {
        listOf(
            ParticleData(0.15f, 0.20f, 28f, 5000, SkyBluePale, 3200),
            ParticleData(0.80f, 0.15f, 18f, 6000, SunGold,     2800),
            ParticleData(0.10f, 0.75f, 22f, 4500, RainBlue,    3500),
            ParticleData(0.85f, 0.65f, 14f, 7000, SkyBlueBright, 4000),
            ParticleData(0.45f, 0.88f, 20f, 5500, SkyBluePale, 3000),
            ParticleData(0.70f, 0.42f, 10f, 6500, SunGoldDark, 2500),
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Soft radial gradient overlay
        drawRect(
            Brush.radialGradient(
                colors = listOf(SkyBlue.copy(alpha = 0.08f), Color.Transparent),
                center = Offset(size.width * 0.5f, size.height * 0.35f),
                radius = size.width * 0.7f
            )
        )
    }

    particles.forEach { p ->
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.05f,
            targetValue = p.maxAlpha,
            animationSpec = infiniteRepeatable(
                tween(p.duration, delayMillis = p.delay, easing = FastOutSlowInEasing),
                RepeatMode.Reverse
            ),
            label = "pa${p.hashCode()}"
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = p.color.copy(alpha = alpha),
                radius = p.radius,
                center = Offset(size.width * p.x, size.height * p.y)
            )
        }
    }
}

private data class ParticleData(
    val x: Float,
    val y: Float,
    val radius: Float,
    val duration: Int,
    val color: Color,
    val delay: Int,
    val maxAlpha: Float = 0.35f
)
