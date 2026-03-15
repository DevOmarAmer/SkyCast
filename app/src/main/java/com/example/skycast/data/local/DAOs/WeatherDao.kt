package com.example.skycast.data.local.DAOs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.skycast.data.local.entity.CachedWeather

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(cachedWeather: CachedWeather)

    @Query("SELECT * FROM cached_weather WHERE id = 1")
    suspend fun getCachedWeather(): CachedWeather?
}
