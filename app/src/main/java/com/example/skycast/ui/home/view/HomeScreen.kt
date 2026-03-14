package com.example.skycast.ui.home.view

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.skycast.R
import com.example.skycast.data.model.ForecastItem
import com.example.skycast.data.model.WeatherResponse
import com.example.skycast.ui.home.viewModel.HomeViewModel
import com.example.skycast.ui.theme.*
import com.example.skycast.utils.Resource
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import com.example.skycast.ui.home.viewModel.AiState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import com.example.skycast.utils.ConnectivityObserver
import com.example.skycast.ui.home.view.components.*

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()
    val networkStatus by viewModel.networkStatus.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SkyDeepNavy, DarkSurface, SkyNavy)
                )
            )
    ) {
        when (weatherState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = SkyBlueBright, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Fetching weather…",
                            color = CloudGrey,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            is Resource.Success -> {
                val data = (weatherState as Resource.Success).data
                if (data != null) {
                    WeatherContent(data, viewModel)
                }
            }

            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text("⚠️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = (weatherState as Resource.Error).message ?: "Something went wrong",
                            color = StormRed,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // ── Offline Banner ────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = networkStatus == ConnectivityObserver.Status.Unavailable || networkStatus == ConnectivityObserver.Status.Lost,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Red.copy(alpha = 0.8f))
                    .padding(WindowInsets.statusBars.asPaddingValues())
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Offline Mode - Showing Cached Data",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WeatherContent(data: WeatherResponse, viewModel: HomeViewModel) {
    val currentWeather = data.forecastList.firstOrNull() ?: return
    val aiState by viewModel.aiSummaryState.collectAsStateWithLifecycle()

    // Group forecast by day for the 5-day section
    val dailyForecasts = data.forecastList
        .groupBy { it.dateText.substring(0, 10) }
        .entries.take(5)
        .map { it.value.first() } // take first forecast item per day

    // Today's hourly items
    val todayDate = data.forecastList.firstOrNull()?.dateText?.substring(0, 10) ?: ""
    val hourlyToday = data.forecastList.filter { it.dateText.startsWith(todayDate) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
            bottom = 100.dp
        )
    ) {
        // ── Hero Section ──────────────────────────────────────────────────────
        item {
            HeroSection(data = data, currentWeather = currentWeather)
        }

        // ── Details Card ─────────────────────────────────────────────────────
        item {
            WeatherDetailsCard(currentWeather = currentWeather)
        }

        // ── AI Brief Section ─────────────────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = aiState !is AiState.Idle,
                enter = slideInVertically(initialOffsetY = { 50 }) + androidx.compose.animation.fadeIn(),
                exit = slideOutVertically() + androidx.compose.animation.fadeOut()
            ) {
                AiBriefCard(aiState = aiState, currentWeather = currentWeather, cityName = data.city.name)
            }
        }

        // ── Today's Hourly Forecast ───────────────────────────────────────────
        item {
            SectionHeader(title = "Today's Forecast")
        }
        item {
            HourlyForecastRow(hourlyItems = hourlyToday)
        }

        // ── 5-Day Forecast ────────────────────────────────────────────────────
        item {
            SectionHeader(title = "5-Day Forecast")
        }
        items(dailyForecasts) { day ->
            DailyForecastRow(forecast = day)
        }

        // Bottom spacing
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}