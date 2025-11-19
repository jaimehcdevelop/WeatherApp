package com.jaimehcdeveloper.weather.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.jaimehcdeveloper.weather.WeatherViewModel
import com.jaimehcdeveloper.weather.domain.model.DailyWeatherInfo
import com.jaimehcdeveloper.weather.domain.model.HourlyWeatherInfo
import com.jaimehcdeveloper.weather.domain.model.LocationSearchResult
import com.jaimehcdeveloper.weather.domain.model.WeatherInfo



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val pullRefreshState = rememberPullToRefreshState()

    fun getCurrentLocationAndFetchWeather() {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.loadWeatherInfo(location.latitude, location.longitude)
                    } else {
                        viewModel.loadWeatherInfo(40.4165, -3.70256)
                    }
                }
            } else {
                viewModel.loadWeatherInfo(40.4165, -3.70256)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            getCurrentLocationAndFetchWeather()
        } else {
            viewModel.loadWeatherInfo(40.4165, -3.70256)
        }
    }

    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) { getCurrentLocationAndFetchWeather() }
    }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) pullRefreshState.endRefresh()
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocationAndFetchWeather()
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    Scaffold(
        topBar = {
            WeatherTopBar(
                isSearching = state.isSearching,
                query = state.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onSearchToggle = viewModel::onToggleSearch,
                onLocationClick = { getCurrentLocationAndFetchWeather() },
                searchResults = state.searchResults,
                onResultClick = viewModel::onCitySelected
            )
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
                .nestedScroll(pullRefreshState.nestedScrollConnection)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when {
                    state.isLoading && state.data == null -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
                    }
                    state.errorMessage != null -> {
                        ErrorView(
                            message = state.errorMessage!!,
                            onRetry = { getCurrentLocationAndFetchWeather() },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    state.data != null -> {
                        Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            WeatherContent(
                                info = state.data!!,
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        }
                    }
                }
            }
            PullToRefreshContainer(
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = Color.White,
                contentColor = Color(0xFF3949AB)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherTopBar(
    isSearching: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchToggle: () -> Unit,
    onLocationClick: () -> Unit,
    searchResults: List<LocationSearchResult>,
    onResultClick: (LocationSearchResult) -> Unit
) {
    Column {
        if (isSearching) {
            // Barra de Búsqueda Activa
            SearchBar(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = {},
                active = true,
                onActiveChange = { if (!it) onSearchToggle() },
                placeholder = { Text("Buscar ciudad...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = onSearchToggle) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                LazyColumn {
                    items(searchResults) { result ->
                        ListItem(
                            headlineContent = { Text(result.name) },
                            supportingContent = { Text("${result.region}, ${result.country}") },
                            leadingContent = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            modifier = Modifier.clickable { onResultClick(result) }
                        )
                    }
                }
            }
        } else {
            // Barra Normal
            CenterAlignedTopAppBar(
                title = { Text("Weather App", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(onClick = onSearchToggle) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.White)
                    }
                    IconButton(onClick = onLocationClick) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Mi Ubicación", tint = Color.White)
                    }
                }
            )
        }
    }
}

// ... (Las funciones WeatherContent, DailyForecastItem, etc. se mantienen idénticas al código anterior) ...

@Composable
fun WeatherContent(info: WeatherInfo, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Parte Superior (Actual) ---
        Text(
            text = info.locationName,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(8.dp))

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            WeatherDetailItem(label = "Viento", value = "${info.windSpeed} km/h")
            WeatherDetailItem(label = "Humedad", value = "${info.humidity}%")
            WeatherDetailItem(label = "Presión", value = "${info.pressure} hPa")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Sección: Por Horas ---
        Text(
            text = "Hoy",
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

        // --- Sección: 7 Días (NUEVO) ---
        Text(
            text = "Próximos 7 Días",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.Start).padding(start = 8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            info.dailyForecast.forEach { daily ->
                DailyForecastItem(daily)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun DailyForecastItem(daily: DailyWeatherInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = daily.time,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        AsyncImage(
            model = daily.iconUrl,
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "${daily.maxTemperature}°",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${daily.minTemperature}°",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 18.sp
            )
        }
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