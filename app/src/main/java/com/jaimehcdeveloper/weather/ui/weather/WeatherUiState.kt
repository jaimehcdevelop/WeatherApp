package com.jaimehcdeveloper.weather.ui.weather

import com.jaimehcdeveloper.weather.domain.model.WeatherInfo


// Estado inmutable de la UI
data class WeatherUiState(
    val isLoading: Boolean = false,
    val data: WeatherInfo? = null,
    val errorMessage: String? = null
)