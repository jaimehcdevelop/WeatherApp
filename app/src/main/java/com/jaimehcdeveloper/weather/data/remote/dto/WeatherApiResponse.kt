package com.jaimehcdeveloper.weather.data.remote.dto

import com.squareup.moshi.Json


data class WeatherApiResponse(
    @Json(name = "current_weather") val currentWeather: CurrentWeatherDto,
    @Json(name = "hourly") val hourly: HourlyDto
)

data class CurrentWeatherDto(
    @Json(name = "temperature") val temperature: Double,
    @Json(name = "windspeed") val windSpeed: Double,
    @Json(name = "weathercode") val weatherCode: Int
)

// Open-Meteo devuelve "Arrays paralelos" (Columnar data)
data class HourlyDto(
    @Json(name = "time") val time: List<String>,
    @Json(name = "temperature_2m") val temperatures: List<Double>,
    @Json(name = "weathercode") val weatherCodes: List<Int>,
    @Json(name = "relativehumidity_2m") val humidities: List<Int>, // Opcional
    @Json(name = "pressure_msl") val pressures: List<Double> // Opcional
)