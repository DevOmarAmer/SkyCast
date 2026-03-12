package com.example.skycast.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp


@Immutable
data class WeatherColors(
    /** Two-stop vertical background gradient  */
    val bgTop: Color,
    val bgBottom: Color,
    /** Glow / hero accent color behind the weather icon */
    val heroGlow: Color,
    /** Accent used in pill labels, hourly cards, section highlights */
    val accent: Color,
    /** Card surface (glassmorphism tint) */
    val cardSurface: Color,
    /** Card border */
    val cardBorder: Color = FrostStrong
)

val DefaultWeatherColors = WeatherColors(
    bgTop        = SkyDeepNavy,
    bgBottom     = SkyNavy,
    heroGlow     = SkyBlueBright,
    accent       = SkyBlueBright,
    cardSurface  = Frost
)


private object Palettes {

    // 🔥 Hot — temp ≥ 35 °C  (desert orange → deep amber)
    val hot = WeatherColors(
        bgTop        = Color(0xFF2D1200),
        bgBottom     = Color(0xFF1A0A00),
        heroGlow     = Color(0xFFFF7043),
        accent       = Color(0xFFFF8F00),
        cardSurface  = Color(0x1AFF7043)
    )

    // 🌅 Warm — temp 25–34 °C  (golden / sunset)
    val warm = WeatherColors(
        bgTop        = Color(0xFF211600),
        bgBottom     = Color(0xFF120D00),
        heroGlow     = Color(0xFFFFB300),
        accent       = Color(0xFFFFD54F),
        cardSurface  = Color(0x1AFFB300)
    )

    // ☁️ Cloudy — clouds ≥ 75 % (slate grey)
    val cloudy = WeatherColors(
        bgTop        = Color(0xFF1A1F2E),
        bgBottom     = Color(0xFF0E1420),
        heroGlow     = Color(0xFF78909C),
        accent       = Color(0xFFB0BEC5),
        cardSurface  = Color(0x1A78909C)
    )

    // 🌧️ Rainy  — rain/drizzle/thunderstorm condition
    val rainy = WeatherColors(
        bgTop        = Color(0xFF0D1B2A),
        bgBottom     = Color(0xFF051020),
        heroGlow     = Color(0xFF29B6F6),
        accent       = Color(0xFF29B6F6),
        cardSurface  = Color(0x1A1565C0)
    )

    // ❄️ Cold — temp ≤ 5 °C (icy blue / white)
    val cold = WeatherColors(
        bgTop        = Color(0xFF0A1525),
        bgBottom     = Color(0xFF091122),
        heroGlow     = Color(0xFF80DEEA),
        accent       = Color(0xFFB2EBF2),
        cardSurface  = Color(0x1A80DEEA)
    )

    // 🌨️ Snow — snow/sleet/freezing condition
    val snowy = WeatherColors(
        bgTop        = Color(0xFF0E1822),
        bgBottom     = Color(0xFF0A1220),
        heroGlow     = Color(0xFFE0F7FA),
        accent       = Color(0xFFB2EBF2),
        cardSurface  = Color(0x1AE0F7FA)
    )

    // 🌤 Mild — temp 15–24 °C (original palette, slight warmth)
    val mild = WeatherColors(
        bgTop        = SkyDeepNavy,
        bgBottom     = DarkSurface,
        heroGlow     = SkyBlueBright,
        accent       = SkyBlueBright,
        cardSurface  = Frost
    )
}

private fun classifyCondition(conditionId: Int): String = when (conditionId) {
    in 200..299 -> "thunderstorm"
    in 300..399 -> "drizzle"
    in 500..599 -> "rain"
    in 600..699 -> "snow"
    in 700..799 -> "atmosphere"   // mist/fog/haze
    800         -> "clear"
    in 801..804 -> "clouds"
    else        -> "unknown"
}


fun deriveWeatherColors(
    tempCelsius: Double,
    cloudPct: Int,
    conditionId: Int
): WeatherColors {
    val condition = classifyCondition(conditionId)

    // Precipitation overrides temperature palettes
    if (condition == "snow") return Palettes.snowy
    if (condition in setOf("thunderstorm", "rain", "drizzle")) return Palettes.rainy

    // Then apply temperature bands
    return when {
        tempCelsius >= 35  -> Palettes.hot
        tempCelsius >= 25  -> Palettes.warm
        tempCelsius <= 0   -> Palettes.snowy
        tempCelsius <= 5   -> Palettes.cold
        cloudPct >= 75     -> Palettes.cloudy
        else               -> Palettes.mild
    }
}

val LocalWeatherColors = compositionLocalOf { DefaultWeatherColors }
