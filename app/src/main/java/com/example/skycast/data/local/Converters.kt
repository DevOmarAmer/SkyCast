package com.example.skycast.data.local

import androidx.room.TypeConverter
import com.example.skycast.data.model.City
import com.example.skycast.data.model.ForecastItem
import com.example.skycast.data.model.WeatherResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromForecastList(list: List<ForecastItem>?): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toForecastList(json: String?): List<ForecastItem>? {
        if (json == null) return null
        val type = object : TypeToken<List<ForecastItem>>() {}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun fromCity(city: City?): String {
        return gson.toJson(city)
    }

    @TypeConverter
    fun toCity(json: String?): City? {
        if (json == null) return null
        val type = object : TypeToken<City>() {}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun fromWeatherResponse(weatherResponse: WeatherResponse?): String {
        return gson.toJson(weatherResponse)
    }

    @TypeConverter
    fun toWeatherResponse(json: String?): WeatherResponse? {
        if (json == null) return null
        val type = object : TypeToken<WeatherResponse>() {}.type
        return gson.fromJson(json, type)
    }
}
