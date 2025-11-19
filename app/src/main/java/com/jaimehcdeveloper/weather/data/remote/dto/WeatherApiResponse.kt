package com.jaimehcdeveloper.weather.data.remote.dto


import com.squareup.moshi.Json

// DTO: Refleja exactamente la estructura del JSON
data class WeatherApiResponse(
    @Json(name = "weather") val weatherDescriptions: List<WeatherDescriptionDto>,
    @Json(name = "main") val main: MainWeatherDto,
    @Json(name = "name") val locationName: String,
    @Json(name = "sys") val sys: SysDto // Añadido para obtener país si hace falta
)

data class WeatherDescriptionDto(
    @Json(name = "description") val description: String,
    @Json(name = "icon") val icon: String
)

data class MainWeatherDto(
    @Json(name = "temp") val temperature: Double,
    @Json(name = "humidity") val humidity: Int,
    @Json(name = "pressure") val pressure: Int
)

data class SysDto(
    @Json(name = "country") val country: String
)