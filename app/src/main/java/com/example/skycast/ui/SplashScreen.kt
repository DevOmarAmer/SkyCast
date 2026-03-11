package com.example.skycast.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skycast.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

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
        ParticleCanvas()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Logo area ─────────────────────────────────────────────────────
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
                        .offset(y = float.dp)
                ) {
                    Text(
                        text = "⛅",
                        fontSize = 52.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── App name ──────────────────────────────────────────────────────
            Text(
                text = "SkyCast",
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
                text = "Your sky, your forecast",
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
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
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

// ── Ambient background particles ─────────────────────────────────────────────
@Composable
private fun ParticleCanvas() {
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
