package com.jaimehcdeveloper.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaimehcdeveloper.weather.data.repository.WeatherRepository
import com.jaimehcdeveloper.weather.domain.model.LocationSearchResult
import com.jaimehcdeveloper.weather.ui.weather.WeatherUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private var searchJob: Job? = null

    fun loadWeatherInfo(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isSearching = false) }

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

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        searchJob?.cancel()

        if (query.length < 3) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            // Si la interfaz (Paso 1) está bien, repository.searchCity ya no dará error rojo
            repository.searchCity(query)
                .onSuccess { results ->
                    _uiState.update { it.copy(searchResults = results) }
                }
                .onFailure {
                    // Error silencioso o log
                }
        }
    }

    fun onToggleSearch() {
        _uiState.update { it.copy(isSearching = !it.isSearching, searchQuery = "", searchResults = emptyList()) }
    }

    fun onCitySelected(location: LocationSearchResult) {
        loadWeatherInfo(location.latitude, location.longitude)
    }
}