package com.example.ui

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.domain.BiometricEngine
import com.example.ui.components.AppearanceSettingsSheet
import com.example.ui.components.JournalScreen
import com.example.ui.components.MetabolismScreen
import com.example.ui.components.ProfileManagerSheet
import com.example.ui.components.PulseScreen
import com.example.ui.components.ReferenceGuideSheet
import com.example.ui.components.TrajectoryScreen
import kotlinx.coroutines.launch

enum class VitalPulseDestination(
  val route: String,
  val labelRes: Int,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector
) {
  PULSE("pulse", R.string.nav_pulse, Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
  METABOLISM("metabolism", R.string.nav_metabolism, Icons.Filled.LocalFireDepartment, Icons.Outlined.LocalFireDepartment),
  TRAJECTORY("trajectory", R.string.nav_trajectory, Icons.Filled.Flag, Icons.Outlined.Flag),
  JOURNAL("journal", R.string.nav_journal, Icons.Filled.Timeline, Icons.Outlined.Timeline)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitalPulseApp(
  viewModel: VitalPulseViewModel
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var currentDestination by rememberSaveable { mutableStateOf(VitalPulseDestination.PULSE) }
  var showProfileSheet by rememberSaveable { mutableStateOf(false) }
  var showAppearanceSheet by rememberSaveable { mutableStateOf(false) }
  var showReferenceSheet by rememberSaveable { mutableStateOf(false) }

  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current

  BackHandler(enabled = currentDestination != VitalPulseDestination.PULSE) {
    currentDestination = VitalPulseDestination.PULSE
  }

  LaunchedEffect(uiState.snackbarMessage) {
    uiState.snackbarMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      viewModel.clearSnackbar()
    }
  }

  val shareBiometricSummary: () -> Unit = {
    val bio = uiState.biometrics
    if (bio.isValid) {
      val weightStr = BiometricEngine.formatWeight(bio.weightKg, uiState.activeProfile.weightUnit)
      val heightStr = "${BiometricEngine.formatNum(bio.heightCm, 0)} cm"
      val summary = "My BMI is ${BiometricEngine.formatNum(bio.bmi, 1)} (${bio.category.label}) at $weightStr, $heightStr."
      clipboardManager.setText(AnnotatedString(summary))
      runCatching {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_SUBJECT, "VitalPulse Biometric Summary")
          putExtra(Intent.EXTRA_TEXT, summary)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share BMI Summary"))
      }.onFailure {
        coroutineScope.launch {
          snackbarHostState.showSnackbar("Summary copied to clipboard")
        }
      }
    }
  }

  val printBiometricReport: () -> Unit = {
    val bio = uiState.biometrics
    if (bio.isValid) {
      printHtmlReport(context, uiState)
    }
  }

  if (showProfileSheet) {
    ProfileManagerSheet(
      profiles = uiState.allProfiles,
      activeProfileName = uiState.activeProfile.name,
      onSelectProfile = {
        viewModel.selectProfile(it)
        showProfileSheet = false
      },
      onCreateProfile = { viewModel.createNewProfile(it) },
      onDeleteActiveProfile = { viewModel.deleteCurrentProfile() },
      onDismiss = { showProfileSheet = false }
    )
  }

  if (showAppearanceSheet) {
    AppearanceSettingsSheet(
      settings = uiState.settings,
      onThemeModeChange = viewModel::updateThemeMode,
      onAccentPresetChange = viewModel::updateAccentPreset,
      onDynamicColorToggle = viewModel::toggleDynamicColor,
      onAsianCutoffsToggle = viewModel::toggleAsianCutoffs,
      onDismiss = { showAppearanceSheet = false }
    )
  }

  if (showReferenceSheet) {
    ReferenceGuideSheet(
      referenceRows = uiState.referenceRows,
      useAsianCutoffs = uiState.settings.useAsianCutoffs,
      onDismiss = { showReferenceSheet = false }
    )
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val isExpanded = maxWidth >= 700.dp

    Scaffold(
      contentWindowInsets = WindowInsets.safeDrawing,
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      topBar = {
        TopAppBar(
          title = {
            Text(
              text = stringResource(R.string.app_name),
              style = MaterialTheme.typography.headlineSmall
            )
          },
          actions = {
            FilledTonalButton(
              onClick = { showProfileSheet = true },
              modifier = Modifier
                .padding(end = 4.dp)
                .testTag("profile_switcher_button")
            ) {
              Icon(
                imageVector = Icons.Outlined.PersonOutline,
                contentDescription = stringResource(R.string.cd_profile_switcher),
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(uiState.activeProfile.name)
            }

            IconButton(
              onClick = { showReferenceSheet = true },
              modifier = Modifier.testTag("reference_guide_button")
            ) {
              Icon(
                imageVector = Icons.Outlined.HelpOutline,
                contentDescription = stringResource(R.string.cd_reference_guide)
              )
            }

            IconButton(
              onClick = { showAppearanceSheet = true },
              modifier = Modifier.testTag("appearance_settings_button")
            ) {
              Icon(
                imageVector = Icons.Outlined.Palette,
                contentDescription = stringResource(R.string.cd_theme_settings)
              )
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
          )
        )
      },
      bottomBar = {
        if (!isExpanded) {
          NavigationBar(modifier = Modifier.testTag("bottom_navigation_bar")) {
            VitalPulseDestination.entries.forEach { dest ->
              val selected = currentDestination == dest
              NavigationBarItem(
                selected = selected,
                onClick = { currentDestination = dest },
                icon = {
                  Icon(
                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                    contentDescription = stringResource(dest.labelRes)
                  )
                },
                label = { Text(stringResource(dest.labelRes)) },
                modifier = Modifier.testTag("nav_tab_${dest.route}")
              )
            }
          }
        }
      }
    ) { innerPadding ->
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        if (isExpanded) {
          NavigationRail(
            modifier = Modifier
              .fillMaxHeight()
              .testTag("side_navigation_rail")
          ) {
            VitalPulseDestination.entries.forEach { dest ->
              val selected = currentDestination == dest
              NavigationRailItem(
                selected = selected,
                onClick = { currentDestination = dest },
                icon = {
                  Icon(
                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                    contentDescription = stringResource(dest.labelRes)
                  )
                },
                label = { Text(stringResource(dest.labelRes)) },
                modifier = Modifier.testTag("rail_tab_${dest.route}")
              )
            }
          }
        }

        Box(modifier = Modifier.weight(1f)) {
          Crossfade(
            targetState = currentDestination,
            label = "destination_crossfade"
          ) { dest ->
            when (dest) {
              VitalPulseDestination.PULSE -> PulseScreen(
                profile = uiState.activeProfile,
                biometrics = uiState.biometrics,
                useAsianCutoffs = uiState.settings.useAsianCutoffs,
                onUnitSystemChange = viewModel::switchUnitSystem,
                onWeightUnitChange = viewModel::switchWeightUnit,
                onHeightCmChange = viewModel::updateHeightCm,
                onHeightImperialChange = viewModel::updateHeightImperial,
                onWeightChange = viewModel::updateWeightDisplay,
                onAgeChange = viewModel::updateAge,
                onSexChange = viewModel::updateSex,
                onAsianCutoffsToggle = viewModel::toggleAsianCutoffs,
                onResetInputs = viewModel::resetActiveProfileInputs,
                onShareClick = shareBiometricSummary,
                onPrintClick = printBiometricReport
              )

              VitalPulseDestination.METABOLISM -> MetabolismScreen(
                profile = uiState.activeProfile,
                metabolism = uiState.metabolism,
                onWaistHipChange = viewModel::updateWaistHip,
                onActivityLevelChange = viewModel::updateActivityLevel
              )

              VitalPulseDestination.TRAJECTORY -> TrajectoryScreen(
                profile = uiState.activeProfile,
                goalProjection = uiState.goalProjection,
                whatIfWeightKg = uiState.whatIfWeightKg,
                whatIfSimulation = uiState.whatIfSimulation,
                onGoalWeightChange = viewModel::updateGoalWeight,
                onWeeklyRateChange = viewModel::updateWeeklyRate,
                onLockStartWeight = viewModel::lockCurrentWeightAsStart,
                onWhatIfSliderChange = viewModel::updateWhatIfWeightKg
              )

              VitalPulseDestination.JOURNAL -> JournalScreen(
                weightUnit = uiState.activeProfile.weightUnit,
                trendMetricMode = uiState.trendMetricMode,
                trendSummary = uiState.trendSummary,
                editingEntry = uiState.editingEntry,
                entryDateInput = uiState.entryDateInput,
                entryNoteInput = uiState.entryNoteInput,
                dataPortableBoxText = uiState.dataPortableBoxText,
                onDateChange = viewModel::updateEntryDateInput,
                onNoteChange = viewModel::updateEntryNoteInput,
                onSaveEntry = viewModel::saveWeightEntry,
                onCancelEdit = viewModel::cancelEditingEntry,
                onEditEntry = { entry ->
                  viewModel.startEditingEntry(entry)
                  currentDestination = VitalPulseDestination.JOURNAL
                },
                onDeleteEntry = viewModel::deleteWeightEntry,
                onTrendMetricModeChange = viewModel::setTrendMetricMode,
                onExportCsv = { viewModel.generateCsvExport() },
                onExportJson = { viewModel.generateJsonBackup() },
                onRestoreBackup = viewModel::restoreFromDataBox,
                onCopyBox = {
                  val txt = uiState.dataPortableBoxText
                  if (txt.isNotBlank()) {
                    clipboardManager.setText(AnnotatedString(txt))
                    coroutineScope.launch {
                      snackbarHostState.showSnackbar("Copied to clipboard")
                    }
                  } else {
                    coroutineScope.launch {
                      snackbarHostState.showSnackbar("Export CSV or JSON first to copy")
                    }
                  }
                },
                onDataBoxChange = viewModel::updateDataBoxText,
                onClearAllData = viewModel::clearAllAppData
              )
            }
          }
        }
      }
    }
  }
}

private fun printHtmlReport(context: Context, state: VitalPulseUiState) {
  runCatching {
    val bio = state.biometrics
    val met = state.metabolism
    val prof = state.activeProfile
    val html = """
      <html>
      <head>
        <meta charset="utf-8"/>
        <style>
          body { font-family: sans-serif; padding: 24px; color: #111827; }
          h1 { margin-bottom: 4px; }
          .sub { color: #475569; margin-bottom: 20px; }
          .box { border: 1px solid #cbd5e1; border-radius: 12px; padding: 16px; margin-bottom: 16px; }
          table { width: 100%; border-collapse: collapse; }
          td, th { padding: 8px; border-bottom: 1px solid #e2e8f0; text-align: left; }
        </style>
      </head>
      <body>
        <h1>VitalPulse Biometric Report — ${prof.name}</h1>
        <div class="sub">Height: ${BiometricEngine.formatNum(bio.heightCm, 1)} cm · Weight: ${BiometricEngine.formatWeight(bio.weightKg, prof.weightUnit)} · Age: ${prof.age}</div>
        <div class="box">
          <h2>BMI: ${BiometricEngine.formatNum(bio.bmi, 1)} (${bio.category.label})</h2>
          <p>Healthy Weight Range: ${bio.healthyWeightRangeFormatted} (${bio.deltaToHealthyFormatted})</p>
          <p>BMI Prime: ${BiometricEngine.formatNum(bio.bmiPrime, 2)} · Ponderal Index: ${BiometricEngine.formatNum(bio.ponderalIndex, 1)} · Est. Body Fat: ${BiometricEngine.formatNum(bio.deurenbergBodyFatPct, 1)}%</p>
        </div>
        ${if (met != null) """
        <div class="box">
          <h3>Metabolic Summary</h3>
          <p>BMR (Mifflin-St Jeor): ${met.bmrMifflinStJeor} kcal · TDEE: ${met.tdeeDailyBurn} kcal · Lean Body Mass: ${met.leanBodyMassFormatted}</p>
          <p>Goal Calories: ${met.goalRateCalories} kcal (${met.goalMacros.formattedSummary})</p>
        </div>
        """ else ""}
      </body>
      </html>
    """.trimIndent()

    val webView = WebView(context)
    webView.webViewClient = object : WebViewClient() {
      override fun onPageFinished(view: WebView, url: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val adapter = view.createPrintDocumentAdapter("VitalPulse_Report_${prof.name}")
        printManager.print("VitalPulse Biometric Report", adapter, PrintAttributes.Builder().build())
      }
    }
    webView.loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null)
  }
}
