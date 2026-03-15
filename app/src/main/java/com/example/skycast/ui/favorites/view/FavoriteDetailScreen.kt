package com.example.skycast.ui.favorites.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.skycast.R
import com.example.skycast.data.local.entity.FavoriteLocation
import com.example.skycast.data.model.WeatherResponse
import com.example.skycast.ui.favorites.viewModel.FavoritesViewModel
import com.example.skycast.ui.theme.*
import com.example.skycast.utils.Resource
import java.util.*
import com.example.skycast.ui.favorites.view.components.DetailDailyCard
import com.example.skycast.ui.favorites.view.components.DetailErrorState
import com.example.skycast.ui.favorites.view.components.DetailHourlyCard
import com.example.skycast.ui.favorites.view.components.DetailLoadingState
import com.example.skycast.ui.favorites.view.components.DetailSectionHeader
import com.example.skycast.ui.favorites.view.components.DetailStatsRow
import com.example.skycast.ui.favorites.view.components.TemperaturePill
@Composable
fun FavoriteDetailScreen(
    location: FavoriteLocation,
    viewModel: FavoritesViewModel,
    apiKey: String,
    onNavigateBack: () -> Unit
) {
    val weatherState by viewModel.selectedFavoriteWeather.collectAsStateWithLifecycle()

    LaunchedEffect(location) {
        viewModel.loadFavoriteWeather(location.latitude, location.longitude, apiKey)
    }

    val rawColors: WeatherColors = when (weatherState) {
        is Resource.Success -> {
            val first = (weatherState as Resource.Success<WeatherResponse>).data
                ?.forecastList?.firstOrNull()
            if (first != null) {
                deriveWeatherColors(
                    tempCelsius  = first.main.temp,
                    cloudPct     = first.clouds.all,
                    conditionId  = first.weatherInfo.firstOrNull()?.id ?: 800
                )
            } else DefaultWeatherColors
        }
        else -> DefaultWeatherColors
    }

    val animSpec = tween<Color>(durationMillis = 1200, easing = FastOutSlowInEasing)
    val bgTop     by animateColorAsState(rawColors.bgTop,       animSpec, label = "bgTop")
    val bgBottom  by animateColorAsState(rawColors.bgBottom,    animSpec, label = "bgBottom")
    val heroGlow  by animateColorAsState(rawColors.heroGlow,    animSpec, label = "heroGlow")
    val accent    by animateColorAsState(rawColors.accent,      animSpec, label = "accent")
    val surface   by animateColorAsState(rawColors.cardSurface, animSpec, label = "surface")

    val animatedColors = WeatherColors(
        bgTop       = bgTop,
        bgBottom    = bgBottom,
        heroGlow    = heroGlow,
        accent      = accent,
        cardSurface = surface
    )

    CompositionLocalProvider(LocalWeatherColors provides animatedColors) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(bgTop, bgBottom)))
        ) {
            when (weatherState) {
                is Resource.Loading -> DetailLoadingState(location, onNavigateBack)
                is Resource.Success -> {
                    val data = (weatherState as Resource.Success<WeatherResponse>).data
                    if (data != null) {
                        DetailSuccessContent(data, location, onNavigateBack)
                    }
                }
                is Resource.Error -> DetailErrorState(
                    message = (weatherState as Resource.Error).message,
                    onNavigateBack = onNavigateBack
                )
            }
        }
    }
}

@Composable
private fun DetailSuccessContent(
    data: WeatherResponse,
    location: FavoriteLocation,
    onNavigateBack: () -> Unit
) {
    val current = data.forecastList.firstOrNull() ?: return
    val todayDate = current.dateText.substring(0, 10)
    val hourlyToday = data.forecastList.filter { it.dateText.startsWith(todayDate) }
    val dailyForecasts = data.forecastList
        .groupBy { it.dateText.substring(0, 10) }
        .entries.take(5)
        .map { it.value.first() }

    val wc = LocalWeatherColors.current

    // Pulsing glow behind the hero icon
    val infiniteTransition = rememberInfiniteTransition(label = "detail")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    val float by infiniteTransition.animateFloat(
        initialValue = -6f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(wc.heroGlow.copy(alpha = 0.22f), Color.Transparent)
                        )
                    )
                    .statusBarsPadding()
            ) {
                // Back button
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(44.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(FrostStrong, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = CloudWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Center: city + temperature hero
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp, bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Location badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(FrostStrong, RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = SkyBluePale,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = location.cityName,
                            style = MaterialTheme.typography.labelMedium,
                            color = SkyBluePale,
                            fontWeight = FontWeight.W600
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Glowing icon
                    Box(contentAlignment = Alignment.Center) {
                        // Ambient glow
                        Box(
                            modifier = Modifier
                                .size((120 * glowScale).dp)
                                .scale(glowScale)
                                .background(
                                    Brush.radialGradient(
                                        listOf(wc.heroGlow.copy(alpha = 0.35f), Color.Transparent)
                                    ),
                                    CircleShape
                                )
                                .blur(20.dp)
                        )
                        val iconCode = current.weatherInfo.firstOrNull()?.icon
                        AsyncImage(
                            model = "https://openweathermap.org/img/wn/${iconCode}@4x.png",
                            contentDescription = stringResource(R.string.weather_icon),
                            modifier = Modifier
                                .size(130.dp)
                                .offset(y = float.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Temperature
                    Text(
                        text = "${current.main.temp.toInt()}°",
                        fontSize = 80.sp,
                        fontWeight = FontWeight.W200,
                        color = CloudWhite,
                        lineHeight = 80.sp
                    )

                    Text(
                        text = current.weatherInfo.firstOrNull()?.description
                            ?.replaceFirstChar { it.titlecase(Locale.getDefault()) } ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        color = wc.accent.copy(alpha = 0.9f),
                        fontWeight = FontWeight.W400
                    )

                    Spacer(Modifier.height(8.dp))

                    // Min / Max pill
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TemperaturePill(label = stringResource(R.string.feels_like_label), value = "${current.main.feelsLike.toInt()}°", color = wc.accent)
                        Box(Modifier.size(4.dp).background(wc.accent.copy(alpha=0.3f), CircleShape))
                        TemperaturePill(label = stringResource(R.string.min), value = "${current.main.tempMin.toInt()}°", color = RainBlue)
                        Box(Modifier.size(4.dp).background(wc.accent.copy(alpha=0.3f), CircleShape))
                        TemperaturePill(label = stringResource(R.string.max), value = "${current.main.tempMax.toInt()}°", color = StormRed)
                    }
                }
            }
        }

        item {
            DetailStatsRow(current)
        }

        item { DetailSectionHeader(stringResource(R.string.today_forecast)) }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(hourlyToday) { forecast ->
                    DetailHourlyCard(forecast)
                }
            }
        }

        item { DetailSectionHeader(stringResource(R.string.five_day_forecast)) }
        items(dailyForecasts) { day ->
            DetailDailyCard(day)
        }
    }
}


