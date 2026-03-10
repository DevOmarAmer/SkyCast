package com.example.skycast.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.skycast.data.model.ForecastItem
import com.example.skycast.data.model.WeatherResponse
import com.example.skycast.utils.Resource
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val weatherState by viewModel.weatherState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (weatherState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                val data = (weatherState as Resource.Success).data
                if (data != null) {
                    WeatherContent(data)
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = (weatherState as Resource.Error).message ?: "Error",
                        color = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherContent(data: WeatherResponse) {
    val currentWeather = data.forecastList.firstOrNull() ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = data.city.name, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text(text = formatDate(currentWeather.dateText), fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            val iconCode = currentWeather.weatherInfo.firstOrNull()?.icon
            AsyncImage(
                model = "https://openweathermap.org/img/wn/${iconCode}@2x.png",
                contentDescription = "Weather Icon",
                modifier = Modifier.size(100.dp)
            )
            Column {
                Text(text = "${currentWeather.main.temp.toInt()}°C", fontSize = 48.sp, fontWeight = FontWeight.Bold)
                Text(text = currentWeather.weatherInfo.firstOrNull()?.description ?: "", fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                WeatherDetailItem(title = "Humidity", value = "${currentWeather.main.humidity}%")
                WeatherDetailItem(title = "Wind", value = "${currentWeather.wind.speed} m/s")
                WeatherDetailItem(title = "Pressure", value = "${currentWeather.main.pressure} hPa")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Upcoming Forecast",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(data.forecastList.take(8)) { forecast ->
                ForecastCard(forecast)
            }
        }
    }
}

@Composable
fun WeatherDetailItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, fontSize = 14.sp, color = Color.Gray)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ForecastCard(forecast: ForecastItem) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // عرض الوقت فقط من النص (مثال: 15:00)
            Text(text = forecast.dateText.substring(11, 16), fontSize = 14.sp)

            val iconCode = forecast.weatherInfo.firstOrNull()?.icon
            AsyncImage(
                model = "https://openweathermap.org/img/wn/${iconCode}.png",
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Text(text = "${forecast.main.temp.toInt()}°", fontWeight = FontWeight.Bold)
        }
    }
}

fun formatDate(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("EEEE, dd MMM - hh:mm a", Locale.getDefault())
        val date = parser.parse(dateString)
        if (date != null) formatter.format(date) else dateString
    } catch (e: Exception) {
        dateString
    }
}