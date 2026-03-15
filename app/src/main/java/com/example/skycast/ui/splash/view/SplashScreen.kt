package com.example.skycast.ui.splash.view

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skycast.R
import com.example.skycast.ui.theme.*
import kotlinx.coroutines.delay
import com.example.skycast.ui.splash.view.components.ParticleCanvasWidget
import com.example.skycast.ui.splash.view.components.SplashBrandDotsWidget
import com.example.skycast.ui.splash.view.components.SplashLogoWidget

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {

    // ── Master timeline: fires onSplashComplete after 3s ─────────────────────
    LaunchedEffect(Unit) {
        delay(3000)
        onSplashComplete()
    }

    // ── Animation states ──────────────────────────────────────────────────────

    // Background gradient reveal
    val bgAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "bgAlpha"
    )

    // Outer shimmer ring – infinite rotation
    val infiniteTransition = rememberInfiniteTransition(label = "global")
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "ringRotation"
    )

    // Pulsing inner glow
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    // Logo scale + fade in
    var logoVisible by remember { mutableStateOf(false) }
    val logoScale by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0.4f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0f,
        animationSpec = tween(600),
        label = "logoAlpha"
    )

    // App name slide-up
    var nameVisible by remember { mutableStateOf(false) }
    val nameOffsetY by animateFloatAsState(
        targetValue = if (nameVisible) 0f else 60f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "nameOffsetY"
    )
    val nameAlpha by animateFloatAsState(
        targetValue = if (nameVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "nameAlpha"
    )

    // Tagline fade-up (delayed)
    var tagVisible by remember { mutableStateOf(false) }
    val tagAlpha by animateFloatAsState(
        targetValue = if (tagVisible) 1f else 0f,
        animationSpec = tween(600),
        label = "tagAlpha"
    )
    val tagOffsetY by animateFloatAsState(
        targetValue = if (tagVisible) 0f else 30f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "tagOffsetY"
    )

    // Stagger triggers
    LaunchedEffect(Unit) {
        delay(300);  logoVisible = true
        delay(400);  nameVisible = true
        delay(500);  tagVisible  = true
    }

    // Floating weather icon
    val float by infiniteTransition.animateFloat(
        initialValue = -8f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float"
    )

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SkyDeepNavy, DarkSurface, SkyNavy),
                )
            )
            .alpha(bgAlpha),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background particles
        ParticleCanvasWidget()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Logo area ─────────────────────────────────────────────────────
            SplashLogoWidget(
                ringRotation = ringRotation,
                pulse = pulse,
                logoScale = logoScale,
                logoAlpha = logoAlpha,
                floatOffset = float
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── App name ──────────────────────────────────────────────────────
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 42.sp,
                fontWeight = FontWeight.W700,
                letterSpacing = 3.sp,
                color = CloudWhite,
                modifier = Modifier
                    .alpha(nameAlpha)
                    .offset(y = nameOffsetY.dp),
                style = MaterialTheme.typography.displaySmall
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Accent divider line
            Box(
                modifier = Modifier
                    .alpha(nameAlpha)
                    .width(120.dp)
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, SkyBlueBright, SunGold, Color.Transparent)
                        )
                    )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Tagline ───────────────────────────────────────────────────────
            Text(
                text = stringResource(R.string.splash_tagline),
                fontSize = 15.sp,
                fontWeight = FontWeight.W300,
                letterSpacing = 1.5.sp,
                color = CloudGrey,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(tagAlpha)
                    .offset(y = tagOffsetY.dp)
                    .padding(horizontal = 32.dp)
            )
        }

        // ── Bottom brand dots ─────────────────────────────────────────────────
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            SplashBrandDotsWidget(tagAlpha = tagAlpha)
        }
    }
}
