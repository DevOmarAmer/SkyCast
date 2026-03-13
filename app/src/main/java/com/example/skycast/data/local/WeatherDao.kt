package com.example.skycast.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(cachedWeather: CachedWeather)

    @Query("SELECT * FROM cached_weather WHERE id = 1")
    suspend fun getCachedWeather(): CachedWeather?
}
