package com.jaimehcdeveloper.weather.data.remote

import com.jaimehcdeveloper.weather.data.remote.dto.WeatherApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    // CAMBIO 2: Endpoint de Open-Meteo
    // Docs: https://open-meteo.com/en/docs
    @GET("v1/forecast")
    suspend fun getWeatherByLocation(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current_weather") currentWeather: Boolean = true
    ): WeatherApiResponse
}