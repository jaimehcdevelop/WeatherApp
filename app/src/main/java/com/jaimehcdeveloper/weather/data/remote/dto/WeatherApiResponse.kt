package com.jaimehcdeveloper.weather.data.remote.dto

import com.squareup.moshi.Json


data class WeatherApiResponse(
    @Json(name = "current_weather") val currentWeather: CurrentWeatherDto,
    @Json(name = "hourly") val hourly: HourlyDto,
    // IMPORTANTE: Este campo 'daily' es el que faltaba y causa el error en el Repo
    @Json(name = "daily") val daily: DailyDto
)

data class CurrentWeatherDto(
    @Json(name = "temperature") val temperature: Double,
    @Json(name = "windspeed") val windSpeed: Double,
    @Json(name = "weathercode") val weatherCode: Int
)

data class HourlyDto(
    @Json(name = "time") val time: List<String>,
    @Json(name = "temperature_2m") val temperatures: List<Double>,
    @Json(name = "weathercode") val weatherCodes: List<Int>,
    @Json(name = "relativehumidity_2m") val humidities: List<Int>,
    @Json(name = "pressure_msl") val pressures: List<Double>
)

// IMPORTANTE: Esta clase también debe existir
data class DailyDto(
    @Json(name = "time") val time: List<String>,
    @Json(name = "weathercode") val weatherCodes: List<Int>,
    @Json(name = "temperature_2m_max") val maxTemperatures: List<Double>,
    @Json(name = "temperature_2m_min") val minTemperatures: List<Double>
)
