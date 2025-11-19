package com.jaimehcdeveloper.weather.domain.model


// Modelo puro de Kotlin, sin dependencias de Android o librerías de JSON
data class WeatherInfo(
    val locationName: String,
    val country: String,
    val temperature: Int, // Redondeamos para la UI
    val description: String,
    val iconUrl: String,
    val humidity: Int,
    val pressure: Int
)