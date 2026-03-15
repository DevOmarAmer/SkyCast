package com.example.skycast.ui.alerts.viewModel

import app.cash.turbine.test
import com.example.skycast.data.local.entity.WeatherAlert
import com.example.skycast.data.repository.IWeatherRepository
import com.example.skycast.utils.IAlertScheduler
import com.example.skycast.utils.SettingsManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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

@OptIn(ExperimentalCoroutinesApi::class)
class AlertsViewModelTest {

    private lateinit var viewModel: AlertsViewModel
    private val repository: IWeatherRepository = mockk()
    private val alertScheduler: IAlertScheduler = mockk()
    private val settingsManager: SettingsManager = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        coEvery { repository.getAlerts() } returns flowOf(emptyList())
        every { settingsManager.morningBriefEnabledFlow } returns flowOf(false)
        every { settingsManager.morningBriefHourFlow } returns flowOf(8)
        every { settingsManager.morningBriefMinuteFlow } returns flowOf(0)
        
        viewModel = AlertsViewModel(repository, alertScheduler, settingsManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun scheduleConditionAlert_callsInsertAlert() = runTest {
        val alert = WeatherAlert(
            conditionType = "RAIN_EXPECTED",
            label = "Rain Alert",
            alertType = "notification",
            workerId = "worker-123"
        )
        coEvery {
            alertScheduler.scheduleConditionAlert(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns alert
        
        coEvery { repository.insertAlert(alert) } returns Unit

        viewModel.scheduleConditionAlert("RAIN_EXPECTED", 0.0, "notification", "Rain Alert", 30.0, 31.0, "key", 1000L, 2000L)

        coVerify(exactly = 1) { repository.insertAlert(alert) }
    }

    @Test
    fun deleteAlert_callsCancelAndDeleteAlert() = runTest {
        val alert = WeatherAlert(id = 5, conditionType = "WIND", label = "Wind", alertType = "notif", workerId = "w-5")
        coEvery { alertScheduler.cancel(alert.workerId) } returns Unit
        coEvery { repository.deleteAlert(alert) } returns Unit

        viewModel.deleteAlert(alert)

        coVerify(exactly = 1) { alertScheduler.cancel(alert.workerId) }
        coVerify(exactly = 1) { repository.deleteAlert(alert) }
    }

    @Test
    fun alertsList_exposesFlowFromRepository() = runTest {
        val list = listOf(WeatherAlert(id = 1, conditionType = "SUN", label = "Sun", alertType = "notif", workerId = "w-1"))
        coEvery { repository.getAlerts() } returns flowOf(list)
        
        val newViewModel = AlertsViewModel(repository, alertScheduler, settingsManager)
        
        newViewModel.alertsList.test {
            assertEquals(list, awaitItem())
        }
    }
}
