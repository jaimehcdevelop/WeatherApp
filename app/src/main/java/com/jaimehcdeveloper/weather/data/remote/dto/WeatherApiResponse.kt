package com.jaimehcdeveloper.weather.data.remote.dto

import com.squareup.moshi.Json


data class WeatherApiResponse(
    @Json(name = "current_weather") val currentWeather: CurrentWeatherDto
)

data class CurrentWeatherDto(
    @Json(name = "temperature") val temperature: Double,
    @Json(name = "windspeed") val windSpeed: Double,
    @Json(name = "weathercode") val weatherCode: Int
)