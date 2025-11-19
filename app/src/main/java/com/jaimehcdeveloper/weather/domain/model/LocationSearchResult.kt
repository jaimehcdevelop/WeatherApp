package com.jaimehcdeveloper.weather.domain.model

data class LocationSearchResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    val region: String
)