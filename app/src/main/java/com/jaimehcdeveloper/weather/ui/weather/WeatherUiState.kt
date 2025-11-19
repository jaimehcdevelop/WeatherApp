package com.jaimehcdeveloper.weather.ui.weather

import com.jaimehcdeveloper.weather.domain.model.LocationSearchResult
import com.jaimehcdeveloper.weather.domain.model.WeatherInfo


// Estado inmutable de la UI
data class WeatherUiState(
    val isLoading: Boolean = false,
    val data: WeatherInfo? = null,
    val errorMessage: String? = null,

    val isSearching: Boolean = false, // Si la barra de búsqueda está activa
    val searchQuery: String = "",
    val searchResults: List<LocationSearchResult> = emptyList()
)

