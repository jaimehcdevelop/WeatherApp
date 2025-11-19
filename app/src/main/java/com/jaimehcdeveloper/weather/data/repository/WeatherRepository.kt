package com.jaimehcdeveloper.weather.data.repository

import com.jaimehcdeveloper.weather.domain.model.LocationSearchResult
import com.jaimehcdeveloper.weather.domain.model.WeatherInfo


interface WeatherRepository {
    suspend fun getWeather(lat: Double, lon: Double): Result<WeatherInfo>

    suspend fun searchCity(query: String): Result<List<LocationSearchResult>>
}