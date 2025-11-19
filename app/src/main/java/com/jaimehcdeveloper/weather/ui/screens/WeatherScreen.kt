package com.jaimehcdeveloper.weather.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jaimehcdeveloper.weather.WeatherViewModel

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            state.isLoading -> {
                CircularProgressIndicator()
            }
            state.data != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "¡HILT FUNCIONA!", fontSize = 30.sp, color = Color.Green)
                    Text(text = "Ubicación: ${state.data?.locationName}")
                    Text(text = "Temp: ${state.data?.temperature}°C")
                    Button(onClick = { viewModel.loadMockData() }) {
                        Text("Recargar")
                    }
                }
            }
            else -> {
                Text(text = "Algo falló: ${state.errorMessage}")
            }
        }
    }
}