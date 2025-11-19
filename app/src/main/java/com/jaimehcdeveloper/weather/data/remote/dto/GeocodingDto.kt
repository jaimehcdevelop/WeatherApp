package com.jaimehcdeveloper.weather.data.remote.dto

import com.squareup.moshi.Json

data class GeocodingResponse(
    @Json(name = "results") val results: List<GeocodingResultDto>?
)

data class GeocodingResultDto(
    @Json(name = "name") val name: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "country") val country: String?,
    @Json(name = "admin1") val admin1: String? // Región/Estado (opcional)
)