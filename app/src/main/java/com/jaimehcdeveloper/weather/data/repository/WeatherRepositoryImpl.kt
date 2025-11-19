package com.jaime.weatherapp.data.repository


import android.content.Context
import android.location.Geocoder
import com.jaimehcdeveloper.weather.data.remote.WeatherApiService
import com.jaimehcdeveloper.weather.data.remote.dto.WeatherApiResponse
import com.jaimehcdeveloper.weather.data.repository.WeatherRepository
import com.jaimehcdeveloper.weather.domain.model.DailyWeatherInfo
import com.jaimehcdeveloper.weather.domain.model.HourlyWeatherInfo
import com.jaimehcdeveloper.weather.domain.model.LocationSearchResult
import com.jaimehcdeveloper.weather.domain.model.WeatherInfo

import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

class WeatherRepositoryImpl @Inject constructor(
    private val apiService: WeatherApiService,
    @ApplicationContext private val context: Context
) : WeatherRepository {

    // --- Implementación existente getWeather ... ---
    override suspend fun getWeather(lat: Double, lon: Double): Result<WeatherInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getWeatherByLocation(lat = lat, lon = lon)
                val locationNames = getLocationName(lat, lon)
                Result.success(response.toDomain(locationNames.first, locationNames.second))
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    // --- NUEVA Implementación searchCity ---
    override suspend fun searchCity(query: String): Result<List<LocationSearchResult>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchCity(query = query)
                val results = response.results?.map { dto ->
                    LocationSearchResult(
                        name = dto.name,
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        country = dto.country ?: "",
                        region = dto.admin1 ?: ""
                    )
                } ?: emptyList()

                Result.success(results)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }


    // ... (Resto de funciones privadas getLocationName, toDomain, etc. permanecen IGUALES) ...
    @Suppress("DEPRECATION")
    private fun getLocationName(lat: Double, lon: Double): Pair<String, String> {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val city = address.locality ?: address.subAdminArea ?: address.adminArea ?: "Ubicación"
                val country = address.countryName ?: ""
                Pair(city, country)
            } else {
                Pair("Ubicación desconocida", "Lat: $lat")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair("Coordenadas", "$lat, $lon")
        }
    }

    private fun WeatherApiResponse.toDomain(cityName: String, countryName: String): WeatherInfo {
        val currentCode = this.currentWeather.weatherCode
        val currentWeatherType = parseWeatherCode(currentCode)

        val currentHourIndex = getCurrentHourIndex(this.hourly.time)
        val next24Hours = if (currentHourIndex != -1) {
            val endIndex = minOf(currentHourIndex + 24, this.hourly.time.size)
            (currentHourIndex until endIndex).map { index ->
                val timeStr = this.hourly.time[index]
                val temp = this.hourly.temperatures[index]
                val code = this.hourly.weatherCodes[index]
                val hourlyWeatherInfo: HourlyWeatherInfo = HourlyWeatherInfo(
                    time = parseTime(timeStr),
                    temperature = temp.roundToInt(),
                    iconUrl = parseWeatherCode(code).iconUrl
                )
                hourlyWeatherInfo
            }
        } else {
            emptyList()
        }

        val dailyList = this.daily.time.indices.map { index ->
            val dateStr = this.daily.time[index]
            val maxTemp = this.daily.maxTemperatures[index]
            val minTemp = this.daily.minTemperatures[index]
            val code = this.daily.weatherCodes[index]

            DailyWeatherInfo(
                time = parseDateToDay(dateStr),
                maxTemperature = maxTemp.roundToInt(),
                minTemperature = minTemp.roundToInt(),
                iconUrl = parseWeatherCode(code).iconUrl
            )
        }

        val currentHumidity = this.hourly.humidities.getOrNull(currentHourIndex) ?: 0
        val currentPressure = this.hourly.pressures.getOrNull(currentHourIndex)?.roundToInt() ?: 1013

        return WeatherInfo(
            locationName = cityName,
            temperature = this.currentWeather.temperature.roundToInt(),
            description = currentWeatherType.description,
            iconUrl = currentWeatherType.iconUrl,
            humidity = currentHumidity,
            pressure = currentPressure,
            windSpeed = this.currentWeather.windSpeed,
            hourlyForecast = next24Hours,
            dailyForecast = dailyList
        )
    }

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

    private fun parseDateToDay(isoDate: String): String {
        return try {
            val date = LocalDate.parse(isoDate)
            val today = LocalDate.now()
            when (date) {
                today -> "Hoy"
                today.plusDays(1) -> "Mañana"
                else -> date.format(DateTimeFormatter.ofPattern("EEEE", Locale("es", "ES")))
                    .replaceFirstChar { it.uppercase() }
            }
        } catch (e: Exception) {
            isoDate
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