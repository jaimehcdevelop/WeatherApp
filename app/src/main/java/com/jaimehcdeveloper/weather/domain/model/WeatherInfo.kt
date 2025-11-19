package com.jaimehcdeveloper.weather.domain.model


data class WeatherInfo(
    val locationName: String,
    val temperature: Int,
    val description: String,
    val iconUrl: String,
    val humidity: Int,
    val pressure: Int,
    val windSpeed: Double,
    // NUEVO: Lista de pronóstico para las próximas horas
    val hourlyForecast: List<HourlyWeatherInfo>
)

data class HourlyWeatherInfo(
    val time: String, // Ej: "14:00"
    val temperature: Int,
    val iconUrl: String
)