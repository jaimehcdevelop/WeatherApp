package com.jaimehcdeveloper.weather.domain.model


data class WeatherInfo(
    val locationName: String,
    val temperature: Int,
    val description: String,
    val iconUrl: String,
    val humidity: Int,
    val pressure: Int,
    val windSpeed: Double,
    val hourlyForecast: List<HourlyWeatherInfo>,
    // NUEVO: Lista de pronóstico diario
    val dailyForecast: List<DailyWeatherInfo>
)

data class HourlyWeatherInfo(
    val time: String,
    val temperature: Int,
    val iconUrl: String
)

// NUEVO: Modelo para un día específico
data class DailyWeatherInfo(
    val time: String,       // Ej: "Lunes", "Martes"
    val maxTemperature: Int,
    val minTemperature: Int,
    val iconUrl: String
)