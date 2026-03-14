package com.example.skycast.ui.splash.view.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.example.skycast.ui.theme.SkyBlueBright

@Composable
fun SplashBrandDotsWidget(tagAlpha: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    
    Row(
        modifier = Modifier
            .padding(bottom = 56.dp)
            .alpha(tagAlpha),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) { index ->
            val dotPulse by infiniteTransition.animateFloat(
                initialValue = 0.5f, targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(600, delayMillis = index * 180, easing = FastOutSlowInEasing),
                    RepeatMode.Reverse
                ),
                label = "dot$index"
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .alpha(dotPulse)
                    .background(SkyBlueBright, CircleShape)
            )
        }
    }
}
