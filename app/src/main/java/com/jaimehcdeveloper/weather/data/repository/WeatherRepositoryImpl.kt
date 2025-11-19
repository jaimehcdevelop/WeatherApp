package com.jaimehcdeveloper.weather.data.repository

import com.jaimehcdeveloper.weather.data.remote.WeatherApiService
import com.jaimehcdeveloper.weather.domain.model.WeatherInfo


import kotlinx.coroutines.delay
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    // Mantenemos la inyección del servicio para probar que Hilt
    // está conectando todo correctamente en el AppModule.
    private val apiService: WeatherApiService
) : WeatherRepository {

    override suspend fun getWeather(lat: Double, lon: Double): Result<WeatherInfo> {
        // Simulamos un pequeño retraso de red para ver el ProgressBar
        delay(1500)

        // Devolvemos datos "Hardcodeados" para probar la UI
        return Result.success(
            WeatherInfo(
                locationName = "Ciudad de Prueba",
                country = "ES",
                temperature = 25,
                description = "Funciona Correctamente",
                iconUrl = "https://openweathermap.org/img/wn/01d@4x.png",
                humidity = 50,
                pressure = 1013
            )
        )
    }
}