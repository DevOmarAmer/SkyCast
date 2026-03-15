package com.example.skycast.ui.home.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skycast.data.model.ForecastItem
import com.example.skycast.data.repository.IAIAssistantRepository
import com.example.skycast.utils.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class MorningAnalysisState {
    object Loading : MorningAnalysisState()
    data class Success(val markdownContent: String) : MorningAnalysisState()
    data class Error(val message: String) : MorningAnalysisState()
}

class MorningAnalysisViewModel(
    private val aiRepository: IAIAssistantRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _analysisState = MutableStateFlow<MorningAnalysisState>(MorningAnalysisState.Loading)
    val analysisState: StateFlow<MorningAnalysisState> = _analysisState.asStateFlow()

    val language = settingsManager.langFlow

    fun fetchDetailedAnalysis(cityName: String, forecastList: List<ForecastItem>) {
        viewModelScope.launch {
            _analysisState.value = MorningAnalysisState.Loading
            try {
                val lang = settingsManager.langFlow.first()
                val result = aiRepository.getDetailedMorningAnalysis(cityName, forecastList, lang)
                
                if (result != null) {
                    _analysisState.value = MorningAnalysisState.Success(result)
                } else {
                    _analysisState.value = MorningAnalysisState.Error("Failed to generate analysis")
                }
            } catch (e: Exception) {
                _analysisState.value = MorningAnalysisState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}
