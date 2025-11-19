package com.example.weatherapp.ui.weather


import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.jaimehcdeveloper.weather.domain.model.HourlyWeatherInfo
import com.jaimehcdeveloper.weather.domain.model.WeatherInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Cliente de ubicación de Google
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Función auxiliar para pedir datos
    fun getCurrentLocationAndFetchWeather() {
        try {
            // Verificamos permiso antes de llamar (aunque ya lo habremos pedido)
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.loadWeatherInfo(location.latitude, location.longitude)
                    } else {
                        // Si el GPS está apagado o no hay última ubicación, cargamos Madrid por defecto
                        viewModel.loadWeatherInfo(40.4165, -3.70256)
                    }
                }
            } else {
                // Fallback sin permisos
                viewModel.loadWeatherInfo(40.4165, -3.70256)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // Launcher para pedir permisos del sistema
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Si se concede ubicación precisa o aproximada
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            getCurrentLocationAndFetchWeather()
        } else {
            // Si deniega, cargamos Madrid por defecto
            viewModel.loadWeatherInfo(40.4165, -3.70256)
        }
    }

    // Efecto inicial: Pedir permisos al abrir la pantalla
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocationAndFetchWeather()
        } else {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Weather App", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { getCurrentLocationAndFetchWeather() },
                containerColor = Color.White.copy(alpha = 0.2f),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Recargar Ubicación")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1A237E), Color(0xFF3949AB), Color(0xFF5C6BC0))
                    )
                )
                .padding(padding)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center), color = Color.White
                )
                state.errorMessage != null -> ErrorView(
                    message = state.errorMessage!!,
                    onRetry = { getCurrentLocationAndFetchWeather() },
                    modifier = Modifier.align(Alignment.Center)
                )
                state.data != null -> WeatherContent(
                    info = state.data!!,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
                else -> {
                    // Estado inicial vacío mientras carga el GPS
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center), color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherContent(info: WeatherInfo, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Cabecera Principal
        Text(
            text = info.locationName,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Icono y Temperatura gigante
        AsyncImage(
            model = info.iconUrl,
            contentDescription = null,
            modifier = Modifier.size(160.dp),
            contentScale = ContentScale.Fit
        )
        Text(
            text = "${info.temperature}°",
            fontSize = 100.sp,
            fontWeight = FontWeight.Light,
            color = Color.White
        )
        Text(
            text = info.description,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Grid de Detalles (Viento, Humedad, Presión)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            WeatherDetailItem(label = "Viento", value = "${info.windSpeed} km/h")
            WeatherDetailItem(label = "Humedad", value = "${info.humidity}%")
            WeatherDetailItem(label = "Presión", value = "${info.pressure} hPa")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Pronóstico por Horas (Carrusel)
        Text(
            text = "Pronóstico Hoy",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.Start).padding(start = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(info.hourlyForecast) { hourly ->
                HourlyForecastItem(hourly)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun WeatherDetailItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .width(85.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun HourlyForecastItem(hourly: HourlyWeatherInfo) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(text = hourly.time, color = Color.White, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        AsyncImage(
            model = hourly.iconUrl,
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "${hourly.temperature}°", color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("¡Ups!", style = MaterialTheme.typography.titleLarge, color = Color.White)
        Text(message, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
            Text("Reintentar", color = Color.Black)
        }
    }
}