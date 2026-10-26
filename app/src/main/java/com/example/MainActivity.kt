package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.VitalPulseRepository
import com.example.data.local.VitalPulseDatabase
import com.example.ui.VitalPulseApp
import com.example.ui.VitalPulseViewModel
import com.example.ui.theme.VitalPulseTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = VitalPulseDatabase.getInstance(applicationContext)
    val repository = VitalPulseRepository(database.dao())

    setContent {
      val viewModel: VitalPulseViewModel = viewModel(
        factory = VitalPulseViewModel.Factory(repository)
      )
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()

      VitalPulseTheme(
        themeMode = uiState.settings.themeMode,
        accentPreset = uiState.settings.accentPreset,
        useDynamicColor = uiState.settings.useDynamicColor
      ) {
        VitalPulseApp(viewModel = viewModel)
      }
    }
  }
}
