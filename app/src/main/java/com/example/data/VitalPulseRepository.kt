package com.example.data

import com.example.data.local.AppSettingsEntity
import com.example.data.local.ProfileEntity
import com.example.data.local.VitalPulseDao
import com.example.data.local.WeightEntryEntity
import com.example.domain.ActivityLevel
import com.example.domain.AppSettingsState
import com.example.domain.BiologicalSex
import com.example.domain.BiometricEngine
import com.example.domain.ProfileData
import com.example.domain.UnitSystem
import com.example.domain.WeightEntry
import com.example.domain.WeightUnit
import com.example.ui.theme.AccentPreset
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VitalPulseRepository(private val dao: VitalPulseDao) {

  val profilesFlow: Flow<List<ProfileData>> = dao.observeProfiles().map { list ->
    list.map { it.toDomain() }
  }

  val allEntriesFlow: Flow<List<WeightEntry>> = dao.observeAllEntries().map { list ->
    list.map { it.toDomain() }
  }

  val settingsFlow: Flow<AppSettingsState> = dao.observeSettings().map { entity ->
    entity?.toDomain() ?: AppSettingsState()
  }

  suspend fun ensureDefaultSeeded() {
    val existingProfiles = dao.getProfilesSnapshot()
    if (existingProfiles.isEmpty()) {
      dao.upsertProfile(ProfileData(name = "Me").toEntity())
    }
    val existingSettings = dao.getSettingsSnapshot()
    if (existingSettings == null) {
      val firstProfile = existingProfiles.firstOrNull()?.name ?: "Me"
      dao.upsertSettings(AppSettingsState(activeProfileName = firstProfile).toEntity())
    }
  }

  suspend fun saveProfile(profile: ProfileData) {
    dao.upsertProfile(profile.toEntity())
  }

  suspend fun saveSettings(settings: AppSettingsState) {
    dao.upsertSettings(settings.toEntity())
  }

  suspend fun addProfile(newName: String): Boolean {
    val trimmed = newName.trim().take(20)
    if (trimmed.isEmpty()) return false
    val current = dao.getProfilesSnapshot()
    if (current.any { it.name.equals(trimmed, ignoreCase = false) }) return false
    dao.upsertProfile(ProfileData(name = trimmed).toEntity())
    val settings = dao.getSettingsSnapshot()?.toDomain() ?: AppSettingsState()
    dao.upsertSettings(settings.copy(activeProfileName = trimmed).toEntity())
    return true
  }

  suspend fun deleteProfile(name: String): Boolean {
    val current = dao.getProfilesSnapshot()
    if (current.size < 2) return false
    dao.deleteProfile(name)
    dao.deleteEntriesForProfile(name)
    val remaining = dao.getProfilesSnapshot()
    val nextActive = remaining.firstOrNull()?.name ?: "Me"
    val settings = dao.getSettingsSnapshot()?.toDomain() ?: AppSettingsState()
    dao.upsertSettings(settings.copy(activeProfileName = nextActive).toEntity())
    return true
  }

  suspend fun upsertWeightEntry(entry: WeightEntry) {
    dao.upsertEntry(entry.toEntity())
  }

  suspend fun deleteWeightEntry(id: Long) {
    dao.deleteEntryById(id)
  }

  suspend fun clearAllData() {
    val currentSettings = dao.getSettingsSnapshot()?.toDomain() ?: AppSettingsState()
    dao.deleteAllEntries()
    dao.deleteAllProfiles()
    dao.upsertProfile(ProfileData(name = "Me").toEntity())
    dao.upsertSettings(currentSettings.copy(activeProfileName = "Me").toEntity())
  }

  suspend fun restoreBackup(restored: BiometricEngine.RestoredBackup) {
    val currentSettings = dao.getSettingsSnapshot()?.toDomain() ?: AppSettingsState()
    dao.deleteAllEntries()
    dao.deleteAllProfiles()
    for (p in restored.profiles) {
      dao.upsertProfile(p.toEntity())
    }
    for (e in restored.entries) {
      dao.upsertEntry(e.copy(id = 0L).toEntity())
    }
    dao.upsertSettings(
      currentSettings.copy(
        activeProfileName = restored.activeProfile,
        useAsianCutoffs = restored.useAsianCutoffs
      ).toEntity()
    )
  }

  private fun ProfileEntity.toDomain(): ProfileData = ProfileData(
    name = name,
    unitSystem = UnitSystem.fromCode(unitSystemCode),
    heightCm = heightCm,
    heightFt = heightFt,
    heightIn = heightIn,
    weightDisplay = weightDisplay,
    weightUnit = WeightUnit.fromCode(weightUnitCode),
    age = age,
    sex = BiologicalSex.fromCode(sexCode),
    activityLevel = ActivityLevel.fromIndex(activityLevelIndex),
    waistInput = waistInput,
    hipInput = hipInput,
    goalWeightDisplay = goalWeightDisplay,
    weeklyRateKg = weeklyRateKg,
    startWeightKg = startWeightKg
  )

  private fun ProfileData.toEntity(): ProfileEntity = ProfileEntity(
    name = name,
    unitSystemCode = unitSystem.code,
    heightCm = heightCm,
    heightFt = heightFt,
    heightIn = heightIn,
    weightDisplay = weightDisplay,
    weightUnitCode = weightUnit.code,
    age = age,
    sexCode = sex.code,
    activityLevelIndex = activityLevel.index,
    waistInput = waistInput,
    hipInput = hipInput,
    goalWeightDisplay = goalWeightDisplay,
    weeklyRateKg = weeklyRateKg,
    startWeightKg = startWeightKg
  )

  private fun WeightEntryEntity.toDomain(): WeightEntry = WeightEntry(
    id = id,
    profileName = profileName,
    dateIso = dateIso,
    weightKg = weightKg,
    bmi = bmi,
    note = note
  )

  private fun WeightEntry.toEntity(): WeightEntryEntity = WeightEntryEntity(
    id = id,
    profileName = profileName,
    dateIso = dateIso,
    weightKg = weightKg,
    bmi = bmi,
    note = note
  )

  private fun AppSettingsEntity.toDomain(): AppSettingsState = AppSettingsState(
    activeProfileName = activeProfileName,
    themeMode = runCatching { ThemeMode.valueOf(themeModeName) }.getOrDefault(ThemeMode.SYSTEM),
    accentPreset = runCatching { AccentPreset.valueOf(accentPresetName) }.getOrDefault(AccentPreset.KINETIC_INDIGO),
    useDynamicColor = useDynamicColor,
    useAsianCutoffs = useAsianCutoffs
  )

  private fun AppSettingsState.toEntity(): AppSettingsEntity = AppSettingsEntity(
    id = 1,
    activeProfileName = activeProfileName,
    themeModeName = themeMode.name,
    accentPresetName = accentPreset.name,
    useDynamicColor = useDynamicColor,
    useAsianCutoffs = useAsianCutoffs
  )
}
