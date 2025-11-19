package com.jaimehcdeveloper.weather.data.repository

import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.jaimehcdeveloper.weather.data.remote.WeatherApiService
import com.jaimehcdeveloper.weather.data.remote.dto.WeatherApiResponse
import com.jaimehcdeveloper.weather.data.repository.WeatherRepository
import com.jaimehcdeveloper.weather.domain.model.HourlyWeatherInfo
import com.jaimehcdeveloper.weather.domain.model.WeatherInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

class WeatherRepositoryImpl @Inject constructor(
    private val apiService: WeatherApiService,
    // Inyectamos el contexto de la aplicación para usar el Geocoder
    @ApplicationContext private val context: Context
) : WeatherRepository {

    override suspend fun getWeather(lat: Double, lon: Double): Result<WeatherInfo> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Llamada a la API del clima
                val response = apiService.getWeatherByLocation(lat = lat, lon = lon)

                // 2. Llamada al Geocoder para obtener el nombre de la ciudad
                // (Esto convierte coordenadas -> "Madrid, España")
                val locationNames = getLocationName(lat, lon)

                // 3. Unimos todo
                Result.success(response.toDomain(locationNames.first, locationNames.second))
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    // --- NUEVA FUNCIÓN: Geocoding Inverso ---
    @Suppress("DEPRECATION") // Usamos la versión sincrónica porque ya estamos en Dispatchers.IO
    private fun getLocationName(lat: Double, lon: Double): Pair<String, String> {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            // Obtenemos 1 resultado máximo
            val addresses = geocoder.getFromLocation(lat, lon, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                // Priorizamos "Locality" (Ciudad), si es null usamos "SubAdminArea" o "AdminArea"
                val city = address.locality ?: address.subAdminArea ?: address.adminArea ?: "Ubicación"
                val country = address.countryName ?: ""
                Pair(city, country)
            } else {
                Pair("Ubicación desconocida", "Lat: $lat")
            }
        } catch (e: Exception) {
            // Si falla el Geocoder (ej: sin internet o servicio no disponible), mostramos coordenadas
            e.printStackTrace()
            Pair("Coordenadas", "$lat, $lon")
        }
    }

    private fun WeatherApiResponse.toDomain(cityName: String, countryName: String): WeatherInfo {
        val currentCode = this.currentWeather.weatherCode
        val currentWeatherType = parseWeatherCode(currentCode)

        // Lógica para procesar el horario (Igual que antes)
        val currentHourIndex = getCurrentHourIndex(this.hourly.time)
        val next24Hours = if (currentHourIndex != -1) {
            val endIndex = minOf(currentHourIndex + 24, this.hourly.time.size)
            (currentHourIndex until endIndex).map { index ->
                val timeStr = this.hourly.time[index]
                val temp = this.hourly.temperatures[index]
                val code = this.hourly.weatherCodes[index]

                HourlyWeatherInfo(
                    time = parseTime(timeStr),
                    temperature = temp.roundToInt(),
                    iconUrl = parseWeatherCode(code).iconUrl
                )
            }
        } else {
            emptyList()
        }

        val currentHumidity = this.hourly.humidities.getOrNull(currentHourIndex) ?: 0
        val currentPressure = this.hourly.pressures.getOrNull(currentHourIndex)?.roundToInt() ?: 1013

        return WeatherInfo(
            locationName = cityName, // ¡Ahora usamos el nombre real!
            temperature = this.currentWeather.temperature.roundToInt(),
            description = currentWeatherType.description,
            iconUrl = currentWeatherType.iconUrl,
            humidity = currentHumidity,
            pressure = currentPressure,
            windSpeed = this.currentWeather.windSpeed,
            hourlyForecast = next24Hours
        )
    }

    // ... Resto de funciones auxiliares (getCurrentHourIndex, parseTime, parseWeatherCode) igual que antes ...

    private fun getCurrentHourIndex(times: List<String>): Int {
        val now = LocalDateTime.now()
        return times.indexOfFirst {
            val time = LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
            time.hour == now.hour && time.dayOfMonth == now.dayOfMonth
        }.coerceAtLeast(0)
    }

    private fun parseTime(isoString: String): String {
        return try {
            val date = LocalDateTime.parse(isoString, DateTimeFormatter.ISO_DATE_TIME)
            date.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            isoString
        }
    }

    private fun parseWeatherCode(code: Int): WeatherType {
        return when (code) {
            0 -> WeatherType("Despejado", "01d")
            1, 2, 3 -> WeatherType("Nublado", "03d")
            45, 48 -> WeatherType("Niebla", "50d")
            51, 53, 55 -> WeatherType("Llovizna", "09d")
            61, 63, 65 -> WeatherType("Lluvia", "10d")
            71, 73, 75 -> WeatherType("Nieve", "13d")
            95, 96, 99 -> WeatherType("Tormenta", "11d")
            else -> WeatherType("Desconocido", "03d")
        }
    }

    private data class WeatherType(val description: String, val iconCode: String) {
        val iconUrl: String get() = "https://openweathermap.org/img/wn/$iconCode@4x.png"
    }
}