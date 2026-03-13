package com.example.skycast.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// App-wide DataStore instance
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(val context: Context) {

    companion object {
        val TEMP_UNIT_KEY = stringPreferencesKey("temp_unit") // celsius, fahrenheit , kelvin
        val WIND_UNIT_KEY = stringPreferencesKey("wind_unit") // m/s, mph
        val LANG_KEY = stringPreferencesKey("language")       // ar, en
        val LOCATION_METHOD_KEY = stringPreferencesKey("location_method") // gps, map

        val MAP_LAT_KEY = doublePreferencesKey("map_lat")
        val MAP_LON_KEY = doublePreferencesKey("map_lon")
    }

    val tempUnitFlow: Flow<String> = context.dataStore.data.map { it[TEMP_UNIT_KEY] ?: "metric" }
    val windUnitFlow: Flow<String> = context.dataStore.data.map { it[WIND_UNIT_KEY] ?: "m/s" }
    val langFlow: Flow<String> = context.dataStore.data.map { it[LANG_KEY] ?: "en" }
    val locationMethodFlow: Flow<String> = context.dataStore.data.map { it[LOCATION_METHOD_KEY] ?: "gps" }
    val mapLatFlow: Flow<Double> = context.dataStore.data.map { it[MAP_LAT_KEY] ?: 0.0 }
    val mapLonFlow: Flow<Double> = context.dataStore.data.map { it[MAP_LON_KEY] ?: 0.0 }

    suspend fun saveTempUnit(unit: String) {
        context.dataStore.edit { it[TEMP_UNIT_KEY] = unit }
    }

    suspend fun saveWindUnit(unit: String) {
        context.dataStore.edit { it[WIND_UNIT_KEY] = unit }
    }

    suspend fun saveLanguage(lang: String) {
        context.dataStore.edit { it[LANG_KEY] = lang }
    }

    suspend fun saveLocationMethod(method: String) {
        context.dataStore.edit { it[LOCATION_METHOD_KEY] = method }
    }
    // Save map coordinates and auto-switch mode to map
    suspend fun saveMapLocation(lat: Double, lon: Double) {
        context.dataStore.edit {
            it[MAP_LAT_KEY] = lat
            it[MAP_LON_KEY] = lon
            it[LOCATION_METHOD_KEY] = "map"
        }
    }
}