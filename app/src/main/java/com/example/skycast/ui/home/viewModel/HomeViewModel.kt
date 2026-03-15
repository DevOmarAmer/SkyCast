package com.example.skycast.ui.home.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skycast.data.model.WeatherResponse
import com.example.skycast.data.repository.IAIAssistantRepository
import com.example.skycast.data.repository.IWeatherRepository
import com.example.skycast.utils.Resource
import com.example.skycast.utils.SettingsManager
import com.example.skycast.utils.ConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: IWeatherRepository,
    private val aiRepository: IAIAssistantRepository,
    private val settingsManager: SettingsManager,
    private val widgetUpdaterService: com.example.skycast.utils.IWidgetUpdaterService,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _weatherState = MutableStateFlow<Resource<WeatherResponse>>(Resource.Loading())
    val weatherState: StateFlow<Resource<WeatherResponse>> = _weatherState.asStateFlow()

    private val _aiSummaryState = MutableStateFlow<AiState>(AiState.Idle)
    val aiSummaryState: StateFlow<AiState> = _aiSummaryState.asStateFlow()

    val windUnit: StateFlow<String> = settingsManager.windUnitFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "m/s")

    val tempUnit: StateFlow<String> = settingsManager.tempUnitFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "metric")

    private val _networkStatus = MutableStateFlow(ConnectivityObserver.Status.Available)
    val networkStatus: StateFlow<ConnectivityObserver.Status> = _networkStatus.asStateFlow()

    // Cache location to auto-refresh on settings change
    private var currentLat: Double? = null
    private var currentLon: Double? = null
    private var currentApiKey: String? = null

    private val _currentLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val currentLocation: StateFlow<Pair<Double, Double>?> = _currentLocation.asStateFlow()

    private val _currentApiKey = MutableStateFlow("")
    val exposedApiKey: StateFlow<String> = _currentApiKey.asStateFlow()

    // Cache to prevent redundant AI calls and save quota
    private var lastFetchedWeatherKey: String? = null
    private var lastFetchedLang: String? = null
    private var aiJob: kotlinx.coroutines.Job? = null


    init {
        // Observe network changes
        viewModelScope.launch {
            connectivityObserver.observe().collect { status ->
                val wasOffline = _networkStatus.value == ConnectivityObserver.Status.Unavailable || _networkStatus.value == ConnectivityObserver.Status.Lost
                _networkStatus.value = status
                val isOnline = status == ConnectivityObserver.Status.Available
                
                // Fetch if we just came back online
                if (wasOffline && isOnline) {
                    if (currentLat != null && currentLon != null && currentApiKey != null) {
                        launch {
                            val unit = settingsManager.tempUnitFlow.first()
                            val lang = settingsManager.langFlow.first()
                            fetchWeatherInternal(currentLat!!, currentLon!!, currentApiKey!!, unit, lang)
                        }
                    }
                }
            }
        }

        // Observe settings changes
        viewModelScope.launch {
            combine(
                settingsManager.tempUnitFlow,
                settingsManager.langFlow,
                settingsManager.locationMethodFlow,
                settingsManager.mapLatFlow,
                settingsManager.mapLonFlow,
                settingsManager.windUnitFlow
            ) { args: Array<Any?> ->
                SettingsData(
                    unit = args[0] as String,
                    lang = args[1] as String,
                    locMode = args[2] as String,
                    mapLat = args[3] as Double,
                    mapLon = args[4] as Double,
                    windUnit = args[5] as String
                )
            }.collectLatest { settings ->
                if (settings.locMode == "map") {
                    // Mode is Map: Fetch immediately using saved coordinates
                    if (settings.mapLat != 0.0 && settings.mapLon != 0.0 && currentApiKey != null) {
                        fetchWeatherInternal(
                            settings.mapLat,
                            settings.mapLon,
                            currentApiKey!!,
                            settings.unit,
                            settings.lang
                        )
                    }
                } else {
                    // Mode is GPS: Fetch only if we received GPS coordinates from UI
                    if (currentLat != null && currentLon != null && currentApiKey != null) {
                        fetchWeatherInternal(
                            currentLat!!,
                            currentLon!!,
                            currentApiKey!!,
                            settings.unit,
                            settings.lang
                        )
                    }
                }
            }
        }
    }
    fun getWeatherData(lat: Double, lon: Double, apiKey: String) {
        currentLat = lat
        currentLon = lon
        currentApiKey = apiKey
        _currentLocation.value = Pair(lat, lon)
        _currentApiKey.value = apiKey

        viewModelScope.launch {
            val unit = settingsManager.tempUnitFlow.first()
            val lang = settingsManager.langFlow.first()
            fetchWeatherInternal(lat, lon, apiKey, unit, lang)
        }
    }

    private suspend fun fetchWeatherInternal(
        lat: Double,
        lon: Double,
        apiKey: String,
        unit: String,
        lang: String
    ) {
        repository.getWeatherForecast(lat, lon, apiKey, unit, lang)
            .onStart { _weatherState.value = Resource.Loading() }
            .collect { result ->
                _weatherState.value = result
                if (result is Resource.Success) {
                    result.data?.let { data ->
                        val firstItem = data.forecastList.firstOrNull()
                        val tempValue = firstItem?.main?.temp?.toInt()?.toString()?.plus("°") ?: "--°"
                        val cityValue = data.city.name
                        val descValue = firstItem?.weatherInfo?.firstOrNull()?.description ?: ""
                        
                        widgetUpdaterService.updateWidget(
                            tempValue, cityValue, descValue
                        )
                        fetchAiSummary(firstItem, lang)
                    }
                }
            }
    }

    private fun fetchAiSummary(firstItem: com.example.skycast.data.model.ForecastItem?, lang: String) {
        if (firstItem == null) return
        
        if (_aiSummaryState.value == AiState.Loading) return

        val weatherKey = "${firstItem.main.temp.toInt()}_${firstItem.weatherInfo.firstOrNull()?.description}_${firstItem.wind.speed.toInt()}"
        if (weatherKey == lastFetchedWeatherKey && lang == lastFetchedLang && _aiSummaryState.value is AiState.Success) {
            return 
        }

        aiJob?.cancel()
        
        aiJob = viewModelScope.launch {
            _aiSummaryState.value = AiState.Loading
            
            val tempC = firstItem.main.temp.toInt()
            val desc = firstItem.weatherInfo.firstOrNull()?.description ?: "Clear"
            val windSpeed = firstItem.wind.speed.toInt()
            val isRainy = desc.contains("Rain", true) || desc.contains("Drizzle", true) || desc.contains("Thunderstorm", true)

            val summary = aiRepository.getWeatherSummary(tempC, desc, windSpeed, isRainy, lang)
            if (!summary.isNullOrBlank()) {
                val trimmedSummary = summary.trim()
                lastFetchedWeatherKey = weatherKey
                lastFetchedLang = lang
                _aiSummaryState.value = AiState.Success(trimmedSummary)
                widgetUpdaterService.updateAiBrief(trimmedSummary)
            } else {
                _aiSummaryState.value = AiState.Error
            }
        }
    }
}

sealed class AiState {
    object Idle : AiState()
    object Loading : AiState()
    data class Success(val text: String) : AiState()
    object Error : AiState()
}

data class SettingsData(
    val unit: String,
    val lang: String,
    val locMode: String,
    val mapLat: Double,
    val mapLon: Double,
    val windUnit: String
)
