package com.jaimehcdeveloper.weather.data.remote

import com.jaimehcdeveloper.weather.data.remote.dto.WeatherApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("v1/forecast")
    suspend fun getWeatherByLocation(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current_weather") currentWeather: Boolean = true,
        // NUEVO: Pedimos datos horarios de temperatura, clima, humedad y presión
        @Query("hourly") hourly: String = "temperature_2m,weathercode,relativehumidity_2m,pressure_msl"
    ): WeatherApiResponse
}