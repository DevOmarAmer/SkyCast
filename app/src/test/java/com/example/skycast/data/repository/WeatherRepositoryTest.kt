package com.example.skycast.data.repository

import app.cash.turbine.test
import com.example.skycast.data.local.ILocalDataSource
import com.example.skycast.data.local.entity.FavoriteLocation
import com.example.skycast.data.model.WeatherResponse
import com.example.skycast.data.remote.IRemoteDataSource
import com.example.skycast.utils.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class WeatherRepositoryTest {

    private lateinit var repository: WeatherRepository
    private val remoteDataSource: IRemoteDataSource = mockk()
    private val localDataSource: ILocalDataSource = mockk()

    @Before
    fun setup() {
        repository = WeatherRepository(remoteDataSource, localDataSource)
    }

    @Test
    fun getWeatherForecast_onSuccess_emitsLoadingThenSuccess() = runBlocking {
        val weatherData = mockk<WeatherResponse>()
        val response = Response.success(weatherData)
        
        coEvery { remoteDataSource.getWeatherForecast(any(), any(), any(), any(), any()) } returns response
        coEvery { localDataSource.insertCachedWeather(weatherData) } returns Unit

        repository.getWeatherForecast(30.0, 31.0, "key", "metric", "en").test {
            assertTrue(awaitItem() is Resource.Loading)
            val success = awaitItem() as Resource.Success
            assertEquals(weatherData, success.data)
            awaitComplete()
        }

        coVerify(exactly = 1) { localDataSource.insertCachedWeather(weatherData) }
    }

    @Test
    fun getWeatherForecast_onIOException_emitsLocalCache() = runBlocking {
        val cachedData = mockk<WeatherResponse>()
        
        coEvery { remoteDataSource.getWeatherForecast(any(), any(), any(), any(), any()) } throws IOException("No internet")
        coEvery { localDataSource.getCachedWeather() } returns cachedData

        repository.getWeatherForecast(30.0, 31.0, "key", "metric", "en").test {
            assertTrue(awaitItem() is Resource.Loading)
            val success = awaitItem() as Resource.Success
            assertEquals(cachedData, success.data)
            awaitComplete()
        }
    }

    @Test
    fun insertFavoriteLocation_delegatesToLocalDataSource() = runBlocking {
        val location = FavoriteLocation(cityName = "London", latitude = 51.5, longitude = -0.1)
        coEvery { localDataSource.insertFavoriteLocation(location) } returns Unit

        repository.insertFavoriteLocation(location)

        coVerify(exactly = 1) { localDataSource.insertFavoriteLocation(location) }
    }
}
