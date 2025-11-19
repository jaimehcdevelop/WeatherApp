package com.jaimehcdeveloper.weather


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
        // Cargamos los datos automáticamente al iniciar
        loadMockData()
    }

    fun loadMockData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.getWeather(0.0, 0.0)
                .onSuccess { info ->
                    _uiState.update { it.copy(isLoading = false, data = info) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Error en Mock") }
                }
        }
    }
}