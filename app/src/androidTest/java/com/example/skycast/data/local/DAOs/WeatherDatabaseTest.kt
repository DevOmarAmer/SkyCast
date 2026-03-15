package com.example.skycast.data.local.DAOs

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.skycast.data.local.db.WeatherDatabase
import com.example.skycast.data.model.FavoriteLocation
import com.example.skycast.data.model.WeatherAlert
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class WeatherDatabaseTest {

    private lateinit var db: WeatherDatabase
    private lateinit var favoriteDao: FavoriteLocationDao
    private lateinit var alertDao: AlertDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, WeatherDatabase::class.java).build()
        favoriteDao = db.favoriteLocationDao()
        alertDao = db.alertDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertFavoriteLocation_andRead_returnsIt() = runBlocking {
        val location = FavoriteLocation(cityName = "Cairo", latitude = 30.0, longitude = 31.0)
        favoriteDao.insertLocation(location)
        
        val favoriteList = favoriteDao.getAllFavoriteLocations().first()
        assertEquals(1, favoriteList.size)
        assertEquals("Cairo", favoriteList[0].cityName)
    }

    @Test
    @Throws(Exception::class)
    fun deleteFavoriteLocation_andRead_listIsEmpty() = runBlocking {
        val location = FavoriteLocation(id = 1, cityName = "Cairo", latitude = 30.0, longitude = 31.0)
        favoriteDao.insertLocation(location)
        favoriteDao.deleteLocation(location)
        
        val favoriteList = favoriteDao.getAllFavoriteLocations().first()
        assertTrue(favoriteList.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun insertAlert_andRead_returnsCorrectConditionType() = runBlocking {
        val alert = WeatherAlert(
            conditionType = "TEMP_ABOVE",
            threshold = 35.0,
            label = "High Heat",
            alertType = "notification",
            workerId = "test-worker-1"
        )
        alertDao.insertAlert(alert)
        
        val alerts = alertDao.getAlerts().first()
        assertEquals(1, alerts.size)
        assertEquals("TEMP_ABOVE", alerts[0].conditionType)
    }
}
