package com.example.skycast.ui.favorites.viewModel

import app.cash.turbine.test
import com.example.skycast.data.local.entity.FavoriteLocation
import com.example.skycast.data.repository.IWeatherRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

import com.example.skycast.utils.SettingsManager

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private lateinit var viewModel: FavoritesViewModel
    private val repository: IWeatherRepository = mockk()
    private val settingsManager: SettingsManager = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        coEvery { repository.getFavoriteLocations() } returns flowOf(emptyList())
        every { settingsManager.windUnitFlow } returns flowOf("m/s")
        every { settingsManager.tempUnitFlow } returns flowOf("metric")
        every { settingsManager.langFlow } returns flowOf("en")

        viewModel = FavoritesViewModel(repository, settingsManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addLocation_callsInsertFavoriteLocation() = runTest {
        val location = FavoriteLocation(cityName = "Paris", latitude = 48.8, longitude = 2.3)
        coEvery { repository.insertFavoriteLocation(location) } returns Unit

        viewModel.addLocation(location)

        coVerify(exactly = 1) { repository.insertFavoriteLocation(location) }
    }

    @Test
    fun deleteLocation_callsDeleteFavoriteLocation() = runTest {
        val location = FavoriteLocation(id = 3, cityName = "Paris", latitude = 48.8, longitude = 2.3)
        coEvery { repository.deleteFavoriteLocation(location) } returns Unit

        viewModel.deleteLocation(location)

        coVerify(exactly = 1) { repository.deleteFavoriteLocation(location) }
    }

    @Test
    fun favoritesList_exposesFlowFromRepository() = runTest {
        val list = listOf(FavoriteLocation(cityName = "Tokyo", latitude = 35.6, longitude = 139.6))
        coEvery { repository.getFavoriteLocations() } returns flowOf(list)

        val newViewModel = FavoritesViewModel(repository, settingsManager)
        
        newViewModel.favoritesList.test {
            assertEquals(list, awaitItem())
        }
    }
}
