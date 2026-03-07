package com.example.skycast.data.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("list") val forecastList: List<ForecastItem>,
    @SerializedName("city") val city: City
)

data class ForecastItem(
    @SerializedName("dt") val date: Long,
    @SerializedName("main") val main: MainTemp,
    @SerializedName("weather") val weatherInfo: List<WeatherDescription>,
    @SerializedName("wind") val wind: Wind,
    @SerializedName("dt_txt") val dateText: String
)

data class MainTemp(
    @SerializedName("temp") val temp: Double,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("pressure") val pressure: Int
)

data class WeatherDescription(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class Wind(
    @SerializedName("speed") val speed: Double
)

data class City(
    @SerializedName("name") val name: String,
    @SerializedName("coord") val coordinates: Coordinates
)

data class Coordinates(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double
)