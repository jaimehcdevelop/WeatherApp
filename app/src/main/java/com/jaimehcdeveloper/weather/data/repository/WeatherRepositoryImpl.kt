package com.jaimehcdeveloper.weather.data.repository

import com.jaimehcdeveloper.weather.data.remote.WeatherApiService
import com.jaimehcdeveloper.weather.data.remote.dto.WeatherApiResponse
import com.jaimehcdeveloper.weather.domain.model.WeatherInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.roundToInt

class WeatherRepositoryImpl @Inject constructor(
    private val apiService: WeatherApiService
) : WeatherRepository {


    override suspend fun getWeather(lat: Double, lon: Double): Result<WeatherInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getWeatherByLocation(lat = lat, lon = lon)
                Result.success(response.toDomain(lat, lon))
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    private fun WeatherApiResponse.toDomain(lat: Double, lon: Double): WeatherInfo {
        val code = this.currentWeather.weatherCode
        val weatherType = parseWeatherCode(code)

        return WeatherInfo(
            // Open-Meteo no devuelve nombre de ciudad, así que mostramos coordenadas o un texto genérico
            locationName = "Ubicación Detectada",
            country = "Lat: ${String.format("%.2f", lat)}",
            temperature = this.currentWeather.temperature.roundToInt(),
            description = weatherType.description,
            iconUrl = weatherType.iconUrl,
            humidity = 0, // Open-Meteo requiere otra llamada para humedad, lo dejamos en 0 por simplicidad
            pressure = 1013
        )
    }

    // Helper para traducir códigos numéricos WMO a descripciones e iconos legibles
    private fun parseWeatherCode(code: Int): WeatherType {
        return when (code) {
            0 -> WeatherType("Cielo despejado", "01d")
            1, 2, 3 -> WeatherType("Parcialmente nublado", "03d")
            45, 48 -> WeatherType("Niebla", "50d")
            51, 53, 55 -> WeatherType("Llovizna", "09d")
            61, 63, 65 -> WeatherType("Lluvia", "10d")
            71, 73, 75 -> WeatherType("Nieve", "13d")
            95, 96, 99 -> WeatherType("Tormenta eléctrica", "11d")
            else -> WeatherType("Desconocido", "03d")
        }
    }

    // Clase auxiliar privada para mantener limpio el código
    private data class WeatherType(val description: String, val iconCode: String) {
        // Reutilizamos los iconos de OpenWeatherMap porque son bonitos y fáciles de cargar
        val iconUrl: String
            get() = "https://openweathermap.org/img/wn/$iconCode@4x.png"
    }
}