package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.VitalPulseRepository
import com.example.domain.ActivityLevel
import com.example.domain.AppSettingsState
import com.example.domain.BiologicalSex
import com.example.domain.BiometricAnalysis
import com.example.domain.BiometricEngine
import com.example.domain.CategoryThresholdRow
import com.example.domain.GoalProjection
import com.example.domain.MetabolicAnalysis
import com.example.domain.ProfileData
import com.example.domain.TrendSummary
import com.example.domain.UnitSystem
import com.example.domain.WeightEntry
import com.example.domain.WeightUnit
import com.example.domain.WhatIfSimulation
import com.example.ui.theme.AccentPreset
import com.example.ui.theme.ThemeMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TrendMetricMode(val label: String) {
  BMI("BMI Index"),
  WEIGHT("Body Weight")
}

data class VitalPulseUiState(
  val settings: AppSettingsState = AppSettingsState(),
  val allProfiles: List<ProfileData> = listOf(ProfileData()),
  val activeProfile: ProfileData = ProfileData(),
  val biometrics: BiometricAnalysis = BiometricEngine.analyzeBiometrics(ProfileData(), false),
  val metabolism: MetabolicAnalysis? = null,
  val goalProjection: GoalProjection? = null,
  val whatIfWeightKg: Float = 70f,
  val whatIfSimulation: WhatIfSimulation? = null,
  val trendMetricMode: TrendMetricMode = TrendMetricMode.BMI,
  val trendSummary: TrendSummary = BiometricEngine.summarizeTrends(emptyList(), WeightUnit.KG),
  val editingEntry: WeightEntry? = null,
  val entryDateInput: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
  val entryNoteInput: String = "",
  val dataPortableBoxText: String = "",
  val referenceRows: List<CategoryThresholdRow> = BiometricEngine.getReferenceTableRows(false),
  val snackbarMessage: String? = null
)

private data class TransientUiControls(
  val whatIfWeightKg: Float? = null,
  val trendMetricMode: TrendMetricMode = TrendMetricMode.BMI,
  val editingEntry: WeightEntry? = null,
  val entryDateInput: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
  val entryNoteInput: String = "",
  val dataPortableBoxText: String = "",
  val snackbarMessage: String? = null
)

class VitalPulseViewModel(
  private val repository: VitalPulseRepository
) : ViewModel() {

  private val transientFlow = MutableStateFlow(TransientUiControls())

  init {
    viewModelScope.launch {
      repository.ensureDefaultSeeded()
    }
  }

  val uiState: StateFlow<VitalPulseUiState> = combine(
    repository.settingsFlow,
    repository.profilesFlow,
    repository.allEntriesFlow,
    transientFlow
  ) { settings, profilesList, allEntries, transient ->
    val safeProfiles = if (profilesList.isEmpty()) listOf(ProfileData(name = "Me")) else profilesList
    val activeProfile = safeProfiles.find { it.name == settings.activeProfileName } ?: safeProfiles.first()
    val profileEntries = allEntries.filter { it.profileName == activeProfile.name }

    val biometrics = BiometricEngine.analyzeBiometrics(
      profile = activeProfile,
      useAsianCutoffs = settings.useAsianCutoffs
    )
    val metabolism = BiometricEngine.analyzeMetabolism(activeProfile, biometrics)
    val trendSummary = BiometricEngine.summarizeTrends(profileEntries, activeProfile.weightUnit)
    val goalProjection = BiometricEngine.analyzeGoalProjection(
      profile = activeProfile,
      biometrics = biometrics,
      chronologicalEntries = trendSummary.entriesChronological,
      useAsianCutoffs = settings.useAsianCutoffs
    )

    val defaultWhatIfKg = if (biometrics.isValid) {
      ((biometrics.weightKg * 2.0).roundToInt() / 2.0).toFloat().coerceIn(30f, 200f)
    } else 70f
    val currentWhatIfKg = transient.whatIfWeightKg ?: defaultWhatIfKg
    val whatIfSim = BiometricEngine.simulateWhatIf(
      whatIfWeightKg = currentWhatIfKg.toDouble(),
      profile = activeProfile,
      biometrics = biometrics,
      useAsianCutoffs = settings.useAsianCutoffs
    )

    VitalPulseUiState(
      settings = settings,
      allProfiles = safeProfiles,
      activeProfile = activeProfile,
      biometrics = biometrics,
      metabolism = metabolism,
      goalProjection = goalProjection,
      whatIfWeightKg = currentWhatIfKg,
      whatIfSimulation = whatIfSim,
      trendMetricMode = transient.trendMetricMode,
      trendSummary = trendSummary,
      editingEntry = transient.editingEntry,
      entryDateInput = transient.entryDateInput,
      entryNoteInput = transient.entryNoteInput,
      dataPortableBoxText = transient.dataPortableBoxText,
      referenceRows = BiometricEngine.getReferenceTableRows(settings.useAsianCutoffs),
      snackbarMessage = transient.snackbarMessage
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = VitalPulseUiState()
  )

  fun clearSnackbar() {
    transientFlow.value = transientFlow.value.copy(snackbarMessage = null)
  }

  private fun showMessage(msg: String) {
    transientFlow.value = transientFlow.value.copy(snackbarMessage = msg)
  }

  fun updateProfile(transform: (ProfileData) -> ProfileData) {
    val current = uiState.value.activeProfile
    val updated = transform(current)
    // If weight changed, sync whatIf slider to new weight
    val weightChanged = updated.weightDisplay != current.weightDisplay || updated.weightUnit != current.weightUnit
    if (weightChanged) {
      val newKg = BiometricEngine.toKg(updated.weightDisplay, updated.weightUnit)
      if (newKg.isFinite() && newKg > 0) {
        val snapped = ((newKg * 2.0).roundToInt() / 2.0).toFloat().coerceIn(30f, 200f)
        transientFlow.value = transientFlow.value.copy(whatIfWeightKg = snapped)
      }
    }
    viewModelScope.launch {
      repository.saveProfile(updated)
    }
  }

  fun switchUnitSystem(targetSystem: UnitSystem) {
    val current = uiState.value.activeProfile
    val converted = BiometricEngine.convertProfileUnitSystem(current, targetSystem)
    viewModelScope.launch {
      repository.saveProfile(converted)
    }
  }

  fun switchWeightUnit(targetUnit: WeightUnit) {
    val current = uiState.value.activeProfile
    val converted = BiometricEngine.convertProfileWeightUnit(current, targetUnit)
    viewModelScope.launch {
      repository.saveProfile(converted)
    }
  }

  fun updateHeightCm(cm: Double) = updateProfile { it.copy(heightCm = cm) }
  fun updateHeightImperial(ft: Int, inches: Double) = updateProfile { it.copy(heightFt = ft, heightIn = inches) }
  fun updateWeightDisplay(weight: Double) = updateProfile { it.copy(weightDisplay = weight) }
  fun updateAge(age: Int) = updateProfile { it.copy(age = age) }
  fun updateSex(sex: BiologicalSex) = updateProfile { it.copy(sex = sex) }
  fun updateActivityLevel(level: ActivityLevel) = updateProfile { it.copy(activityLevel = level) }
  fun updateWaistHip(waist: String, hip: String) = updateProfile { it.copy(waistInput = waist, hipInput = hip) }
  fun updateGoalWeight(goal: Double) = updateProfile { it.copy(goalWeightDisplay = goal) }
  fun updateWeeklyRate(rate: Double) = updateProfile { it.copy(weeklyRateKg = rate) }

  fun lockCurrentWeightAsStart() {
    val bio = uiState.value.biometrics
    if (!bio.isValid) {
      showMessage("Fix your height, weight, and age inputs first")
      return
    }
    updateProfile { it.copy(startWeightKg = bio.weightKg) }
    showMessage("Start weight locked at ${BiometricEngine.formatWeight(bio.weightKg, uiState.value.activeProfile.weightUnit)}")
  }

  fun resetActiveProfileInputs() {
    val currentName = uiState.value.activeProfile.name
    val currentStart = uiState.value.activeProfile.startWeightKg
    val resetProfile = ProfileData(name = currentName, startWeightKg = currentStart)
    transientFlow.value = transientFlow.value.copy(whatIfWeightKg = 70f)
    viewModelScope.launch {
      repository.saveProfile(resetProfile)
      showMessage("Profile measurements reset to defaults")
    }
  }

  fun updateWhatIfWeightKg(kg: Float) {
    transientFlow.value = transientFlow.value.copy(
      whatIfWeightKg = ((kg * 2f).roundToInt() / 2f).coerceIn(30f, 200f)
    )
  }

  fun toggleAsianCutoffs(enabled: Boolean) {
    val s = uiState.value.settings
    viewModelScope.launch {
      repository.saveSettings(s.copy(useAsianCutoffs = enabled))
    }
  }

  fun updateThemeMode(mode: ThemeMode) {
    val s = uiState.value.settings
    viewModelScope.launch {
      repository.saveSettings(s.copy(themeMode = mode))
    }
  }

  fun updateAccentPreset(preset: AccentPreset) {
    val s = uiState.value.settings
    viewModelScope.launch {
      repository.saveSettings(s.copy(accentPreset = preset, useDynamicColor = false))
    }
  }

  fun toggleDynamicColor(enabled: Boolean) {
    val s = uiState.value.settings
    viewModelScope.launch {
      repository.saveSettings(s.copy(useDynamicColor = enabled))
    }
  }

  fun selectProfile(profileName: String) {
    val s = uiState.value.settings
    transientFlow.value = transientFlow.value.copy(whatIfWeightKg = null, editingEntry = null)
    viewModelScope.launch {
      repository.saveSettings(s.copy(activeProfileName = profileName))
      showMessage("Switched to profile \"$profileName\"")
    }
  }

  fun createNewProfile(name: String) {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) {
      showMessage("Enter a profile name")
      return
    }
    viewModelScope.launch {
      val added = repository.addProfile(trimmed)
      if (added) {
        transientFlow.value = transientFlow.value.copy(whatIfWeightKg = 70f, editingEntry = null)
        showMessage("Profile \"$trimmed\" added")
      } else {
        showMessage("That profile already exists")
      }
    }
  }

  fun deleteCurrentProfile() {
    val currentName = uiState.value.activeProfile.name
    viewModelScope.launch {
      val deleted = repository.deleteProfile(currentName)
      if (deleted) {
        transientFlow.value = transientFlow.value.copy(whatIfWeightKg = null, editingEntry = null)
        showMessage("Profile \"$currentName\" deleted")
      } else {
        showMessage("Keep at least one profile")
      }
    }
  }

  fun updateEntryDateInput(dateIso: String) {
    transientFlow.value = transientFlow.value.copy(entryDateInput = dateIso)
  }

  fun updateEntryNoteInput(note: String) {
    transientFlow.value = transientFlow.value.copy(entryNoteInput = note.take(80))
  }

  fun setTrendMetricMode(mode: TrendMetricMode) {
    transientFlow.value = transientFlow.value.copy(trendMetricMode = mode)
  }

  fun startEditingEntry(entry: WeightEntry) {
    val prof = uiState.value.activeProfile
    val convertedWeight = BiometricEngine.formatNum(
      BiometricEngine.fromKg(entry.weightKg, prof.weightUnit),
      2
    ).toDoubleOrNull() ?: prof.weightDisplay
    updateWeightDisplay(convertedWeight)
    transientFlow.value = transientFlow.value.copy(
      editingEntry = entry,
      entryDateInput = entry.dateIso,
      entryNoteInput = entry.note
    )
  }

  fun cancelEditingEntry() {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    transientFlow.value = transientFlow.value.copy(
      editingEntry = null,
      entryDateInput = today,
      entryNoteInput = ""
    )
  }

  fun saveWeightEntry() {
    val state = uiState.value
    if (!state.biometrics.isValid) {
      showMessage("Fix your height, weight, and age inputs first")
      return
    }
    val dateStr = state.entryDateInput.trim()
    if (dateStr.isEmpty()) {
      showMessage("Pick a valid date")
      return
    }
    val editing = state.editingEntry
    val entryToSave = WeightEntry(
      id = editing?.id ?: 0L,
      profileName = state.activeProfile.name,
      dateIso = dateStr,
      weightKg = state.biometrics.weightKg,
      bmi = state.biometrics.bmi,
      note = state.entryNoteInput.trim()
    )
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    transientFlow.value = transientFlow.value.copy(
      editingEntry = null,
      entryDateInput = today,
      entryNoteInput = "",
      snackbarMessage = if (editing != null) "Entry updated" else "Entry saved to journal"
    )
    viewModelScope.launch {
      repository.upsertWeightEntry(entryToSave)
    }
  }

  fun deleteWeightEntry(id: Long) {
    if (uiState.value.editingEntry?.id == id) {
      cancelEditingEntry()
    }
    viewModelScope.launch {
      repository.deleteWeightEntry(id)
      showMessage("Entry deleted")
    }
  }

  fun updateDataBoxText(text: String) {
    transientFlow.value = transientFlow.value.copy(dataPortableBoxText = text)
  }

  fun generateCsvExport(): String {
    val csv = BiometricEngine.exportCsv(uiState.value.trendSummary.entriesChronological)
    transientFlow.value = transientFlow.value.copy(
      dataPortableBoxText = csv,
      snackbarMessage = "CSV generated in portability box"
    )
    return csv
  }

  fun generateJsonBackup(allEntriesSnapshot: List<WeightEntry>? = null) {
    viewModelScope.launch {
      val state = uiState.value
      val entriesByProf = mapOf(state.activeProfile.name to state.trendSummary.entriesChronological)
      val json = BiometricEngine.exportBackupJson(
        settings = state.settings,
        profiles = state.allProfiles,
        entriesByProfile = entriesByProf
      )
      transientFlow.value = transientFlow.value.copy(
        dataPortableBoxText = json,
        snackbarMessage = "JSON backup generated in portability box"
      )
    }
  }

  fun restoreFromDataBox() {
    val raw = uiState.value.dataPortableBoxText
    val parsed = BiometricEngine.parseBackupJson(raw)
    if (parsed == null) {
      showMessage("That isn’t a valid VitalPulse JSON backup")
      return
    }
    viewModelScope.launch {
      repository.restoreBackup(parsed)
      transientFlow.value = transientFlow.value.copy(
        whatIfWeightKg = null,
        editingEntry = null,
        snackbarMessage = "Backup restored (${parsed.profiles.size} profile(s))"
      )
    }
  }

  fun clearAllAppData() {
    viewModelScope.launch {
      repository.clearAllData()
      transientFlow.value = transientFlow.value.copy(
        whatIfWeightKg = 70f,
        editingEntry = null,
        dataPortableBoxText = "",
        snackbarMessage = "All data cleared and reset"
      )
    }
  }

  class Factory(private val repository: VitalPulseRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return VitalPulseViewModel(repository) as T
    }
  }
}
