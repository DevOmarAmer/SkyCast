package com.example.skycast.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.skycast.data.local.Converters
import com.example.skycast.data.local.DAOs.AlertDao
import com.example.skycast.data.local.entity.CachedWeather
import com.example.skycast.data.local.DAOs.FavoriteLocationDao
import com.example.skycast.data.local.DAOs.WeatherDao
import com.example.skycast.data.local.entity.FavoriteLocation
import com.example.skycast.data.local.entity.WeatherAlert

@Database(entities = [FavoriteLocation::class, WeatherAlert::class, CachedWeather::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun weatherDao(): WeatherDao
    abstract fun favoriteLocationDao(): FavoriteLocationDao
    abstract fun alertDao(): AlertDao

    companion object {
        @Volatile
        private var INSTANCE: WeatherDatabase? = null

        fun getDatabase(context: Context): WeatherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDatabase::class.java,
                    "weather_database"
                ).fallbackToDestructiveMigration( dropAllTables = true)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}