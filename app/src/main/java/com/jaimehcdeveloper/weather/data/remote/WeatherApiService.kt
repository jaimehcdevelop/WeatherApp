package com.jaimehcdeveloper.weather.data.remote

import com.jaimehcdeveloper.weather.data.remote.dto.GeocodingResponse
import com.jaimehcdeveloper.weather.data.remote.dto.WeatherApiResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface WeatherApiService {

    @GET("v1/forecast")
    suspend fun getWeatherByLocation(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current_weather") currentWeather: Boolean = true,
        @Query("hourly") hourly: String = "temperature_2m,weathercode,relativehumidity_2m,pressure_msl",
        // NUEVO: Pedimos datos diarios y la zona horaria automática
        @Query("daily") daily: String = "weathercode,temperature_2m_max,temperature_2m_min",
        @Query("timezone") timezone: String = "auto"
    ): WeatherApiResponse
    @GET
    suspend fun searchCity(
        @Url url: String = "https://geocoding-api.open-meteo.com/v1/search",
        @Query("name") query: String,
        @Query("count") count: Int = 5,
        @Query("language") language: String = "es",
        @Query("format") format: String = "json"
    ): GeocodingResponse
}