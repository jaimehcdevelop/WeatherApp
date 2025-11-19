package com.example.weatherapp.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaimehcdeveloper.weather.data.repository.WeatherRepository
import com.jaimehcdeveloper.weather.ui.weather.WeatherUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        // Cargar Madrid por defecto al iniciar la app
        loadWeatherInfo()
    }

    // Por defecto usamos coordenadas de Madrid, pero se pueden pasar otras
    fun loadWeatherInfo(lat: Double = 40.4165, lon: Double = -3.70256) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            repository.getWeather(lat, lon)
                .onSuccess { weatherInfo ->
                    _uiState.update {
                        it.copy(isLoading = false, data = weatherInfo, errorMessage = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            data = null,
                            errorMessage = error.message ?: "Error desconocido de red"
                        )
                    }
                }
        }
    }
}