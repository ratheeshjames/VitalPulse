package com.example.domain

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AccentPreset
import com.example.ui.theme.BmiSpectrumColors
import com.example.ui.theme.ThemeMode

enum class UnitSystem(val code: String, val label: String) {
  METRIC("m", "Metric (cm, kg)"),
  IMPERIAL("i", "Imperial (ft/in, lb)");

  companion object {
    fun fromCode(code: String): UnitSystem = entries.find { it.code == code } ?: METRIC
  }
}

enum class WeightUnit(val code: String, val label: String, val factorFromKg: Double) {
  KG("kg", "kg", 1.0),
  LB("lb", "lb", 2.20462),
  ST("st", "st", 1.0 / 6.35029);

  companion object {
    fun fromCode(code: String): WeightUnit = entries.find { it.code == code } ?: KG
  }
}

enum class BiologicalSex(val code: String, val label: String) {
  MALE("m", "Male"),
  FEMALE("f", "Female");

  companion object {
    fun fromCode(code: String): BiologicalSex = entries.find { it.code == code } ?: MALE
  }
}

enum class ActivityLevel(
  val index: Int,
  val title: String,
  val subtitle: String,
  val multiplier: Double
) {
  SEDENTARY(0, "Sedentary", "Desk job, minimal planned exercise", 1.2),
  LIGHT(1, "Lightly Active", "Light exercise 1–3 days / week", 1.375),
  MODERATE(2, "Moderately Active", "Moderate training 3–5 days / week", 1.55),
  VERY_ACTIVE(3, "Very Active", "Hard workouts 6–7 days / week", 1.725),
  ATHLETE(4, "Athlete / Physical", "Intense daily training or labor job", 1.9);

  companion object {
    fun fromIndex(index: Int): ActivityLevel = entries.find { it.index == index } ?: LIGHT
  }
}

enum class BmiCategory(
  val index: Int,
  val label: String,
  val shortLabel: String,
  val color: Color,
  val tipTier: Int
) {
  SEVERE_THINNESS(0, "Severe thinness", "Severe Thin", BmiSpectrumColors.SevereThinness, 0),
  MODERATE_THINNESS(1, "Moderate thinness", "Mod Thin", BmiSpectrumColors.ModerateThinness, 0),
  MILD_THINNESS(2, "Mild thinness", "Underweight", BmiSpectrumColors.MildThinness, 0),
  NORMAL_WEIGHT(3, "Normal weight", "Healthy", BmiSpectrumColors.NormalWeight, 1),
  OVERWEIGHT(4, "Overweight", "Overweight", BmiSpectrumColors.Overweight, 2),
  OBESE_CLASS_I(5, "Obese class I", "Obese I", BmiSpectrumColors.ObeseClassI, 3),
  OBESE_CLASS_II(6, "Obese class II", "Obese II", BmiSpectrumColors.ObeseClassII, 3),
  OBESE_CLASS_III(7, "Obese class III", "Obese III", BmiSpectrumColors.ObeseClassIII, 3);

  companion object {
    fun fromIndex(index: Int): BmiCategory =
      entries.getOrElse(index.coerceIn(0, 7)) { NORMAL_WEIGHT }
  }
}

data class ProfileData(
  val name: String = "Me",
  val unitSystem: UnitSystem = UnitSystem.METRIC,
  val heightCm: Double = 170.0,
  val heightFt: Int = 5,
  val heightIn: Double = 7.0,
  val weightDisplay: Double = 70.0,
  val weightUnit: WeightUnit = WeightUnit.KG,
  val age: Int = 30,
  val sex: BiologicalSex = BiologicalSex.MALE,
  val activityLevel: ActivityLevel = ActivityLevel.LIGHT,
  val waistInput: String = "",
  val hipInput: String = "",
  val goalWeightDisplay: Double = 65.0,
  val weeklyRateKg: Double = 0.5,
  val startWeightKg: Double? = null
)

data class WeightEntry(
  val id: Long = 0L,
  val profileName: String = "Me",
  val dateIso: String, // YYYY-MM-DD
  val weightKg: Double,
  val bmi: Double,
  val note: String = ""
)

data class AppSettingsState(
  val activeProfileName: String = "Me",
  val themeMode: ThemeMode = ThemeMode.SYSTEM,
  val accentPreset: AccentPreset = AccentPreset.KINETIC_INDIGO,
  val useDynamicColor: Boolean = false,
  val useAsianCutoffs: Boolean = false
)

data class BiometricAnalysis(
  val isValid: Boolean,
  val validationError: String? = null,
  val weightKg: Double = 0.0,
  val heightCm: Double = 0.0,
  val heightMeters: Double = 0.0,
  val bmi: Double = 0.0,
  val category: BmiCategory = BmiCategory.NORMAL_WEIGHT,
  val healthyWeightMinKg: Double = 0.0,
  val healthyWeightMaxKg: Double = 0.0,
  val healthyWeightRangeFormatted: String = "",
  val deltaToHealthyFormatted: String = "",
  val bmiPrime: Double = 0.0,
  val ponderalIndex: Double = 0.0,
  val trefethenNewBmi: Double = 0.0,
  val deurenbergBodyFatPct: Double = 0.0,
  val childPercentile: Double? = null,
  val childPercentileCategory: String? = null,
  val contextualNote: String = "",
  val personalizedTips: List<String> = emptyList(),
  val cutoffs: List<Double> = emptyList()
)

data class IdealWeightFormulaResult(
  val formulaName: String,
  val subtitle: String,
  val weightKg: Double,
  val formattedWeight: String
)

data class MacroBreakdown(
  val calories: Int,
  val proteinGrams: Int,
  val carbsGrams: Int,
  val fatGrams: Int,
  val formattedSummary: String
)

data class MetabolicAnalysis(
  val bmrMifflinStJeor: Int,
  val bmrHarrisBenedict: Int,
  val tdeeDailyBurn: Int,
  val leanBodyMassKg: Double,
  val leanBodyMassFormatted: String,
  val bodySurfaceAreaM2: Double,
  val dailyWaterLiters: Double,
  val waistToHipRatio: Double? = null,
  val waistToHipStatus: String? = null,
  val waistToHeightRatio: Double? = null,
  val waistToHeightStatus: String? = null,
  val loseCalories: Int,
  val maintainCalories: Int,
  val gainCalories: Int,
  val goalRateCalories: Int,
  val goalMacros: MacroBreakdown,
  val idealWeightFormulas: List<IdealWeightFormulaResult>
)

data class GoalProjection(
  val startWeightKg: Double,
  val currentWeightKg: Double,
  val goalWeightKg: Double,
  val startFormatted: String,
  val goalFormatted: String,
  val differenceFormatted: String,
  val directionLabel: String,
  val isGoalReached: Boolean,
  val progressPercent: Float,
  val progressSummaryText: String,
  val weeksRemaining: Double,
  val weeksFormatted: String,
  val projectedDateFormatted: String,
  val bmiAtGoal: Double,
  val categoryAtGoal: BmiCategory
)

data class WhatIfSimulation(
  val weightKg: Double,
  val formattedWeight: String,
  val bmi: Double,
  val category: BmiCategory,
  val deltaFromCurrentFormatted: String
)

data class TrendSummary(
  val entriesChronological: List<WeightEntry>,
  val entriesReverseChronological: List<WeightEntry>,
  val lowestBmi: Double?,
  val highestBmi: Double?,
  val averageBmi: Double?,
  val totalWeightChangeKg: Double?,
  val totalWeightChangeFormatted: String?
)

data class CategoryThresholdRow(
  val category: BmiCategory,
  val rangeText: String
)
