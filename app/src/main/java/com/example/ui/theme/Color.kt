package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Curated Material 3 Accent Palettes
enum class AccentPreset(
  val label: String,
  val primaryLight: Color,
  val onPrimaryLight: Color,
  val primaryContainerLight: Color,
  val onPrimaryContainerLight: Color,
  val secondaryLight: Color,
  val tertiaryLight: Color,
  val primaryDark: Color,
  val onPrimaryDark: Color,
  val primaryContainerDark: Color,
  val onPrimaryContainerDark: Color,
  val secondaryDark: Color,
  val tertiaryDark: Color,
  val previewHex: Color
) {
  KINETIC_INDIGO(
    label = "Kinetic Indigo",
    primaryLight = Color(0xFF3B49DF),
    onPrimaryLight = Color(0xFFFFFFFF),
    primaryContainerLight = Color(0xFFE0E2FF),
    onPrimaryContainerLight = Color(0xFF00105C),
    secondaryLight = Color(0xFF006874),
    tertiaryLight = Color(0xFF8B418F),
    primaryDark = Color(0xFFBEC2FF),
    onPrimaryDark = Color(0xFF061994),
    primaryContainerDark = Color(0xFF2330B8),
    onPrimaryContainerDark = Color(0xFFE0E2FF),
    secondaryDark = Color(0xFF4FD8EB),
    tertiaryDark = Color(0xFFFFA9FE),
    previewHex = Color(0xFF4F46E5)
  ),
  EMERALD_PULSE(
    label = "Emerald Pulse",
    primaryLight = Color(0xFF006C51),
    onPrimaryLight = Color(0xFFFFFFFF),
    primaryContainerLight = Color(0xFF84F8CE),
    onPrimaryContainerLight = Color(0xFF002116),
    secondaryLight = Color(0xFF4C6359),
    tertiaryLight = Color(0xFF3E6374),
    primaryDark = Color(0xFF67DBB3),
    onPrimaryDark = Color(0xFF003828),
    primaryContainerDark = Color(0xFF00513C),
    onPrimaryContainerDark = Color(0xFF84F8CE),
    secondaryDark = Color(0xFFB3CCBF),
    tertiaryDark = Color(0xFFA6CCE0),
    previewHex = Color(0xFF10B981)
  ),
  CORAL_VITALITY(
    label = "Coral Vitality",
    primaryLight = Color(0xFFB91D47),
    onPrimaryLight = Color(0xFFFFFFFF),
    primaryContainerLight = Color(0xFFFFD9DE),
    onPrimaryContainerLight = Color(0xFF400012),
    secondaryLight = Color(0xFF76565B),
    tertiaryLight = Color(0xFF795831),
    primaryDark = Color(0xFFFFB2BE),
    onPrimaryDark = Color(0xFF660022),
    primaryContainerDark = Color(0xFF8F0033),
    onPrimaryContainerDark = Color(0xFFFFD9DE),
    secondaryDark = Color(0xFFE5BDC2),
    tertiaryDark = Color(0xFFEBBF90),
    previewHex = Color(0xFFF43F5E)
  ),
  AMBER_METABOLIC(
    label = "Amber Metabolic",
    primaryLight = Color(0xFF855300),
    onPrimaryLight = Color(0xFFFFFFFF),
    primaryContainerLight = Color(0xFFFFDDB8),
    onPrimaryContainerLight = Color(0xFF2A1700),
    secondaryLight = Color(0xFF705B40),
    tertiaryLight = Color(0xFF53643E),
    primaryDark = Color(0xFFFFB95C),
    onPrimaryDark = Color(0xFF462A00),
    primaryContainerDark = Color(0xFF653E00),
    onPrimaryContainerDark = Color(0xFFFFDDB8),
    secondaryDark = Color(0xFFDFC2A2),
    tertiaryDark = Color(0xFFBACD9F),
    previewHex = Color(0xFFF59E0B)
  )
}

// Semantic BMI Category Spectrum Colors
object BmiSpectrumColors {
  val SevereThinness = Color(0xFF7C3AED)
  val ModerateThinness = Color(0xFF6366F1)
  val MildThinness = Color(0xFF3B82F6)
  val NormalWeight = Color(0xFF10B981)
  val Overweight = Color(0xFFEAB308)
  val ObeseClassI = Color(0xFFF97316)
  val ObeseClassII = Color(0xFFEF4444)
  val ObeseClassIII = Color(0xFFB91C1C)

  val all = listOf(
    SevereThinness,
    ModerateThinness,
    MildThinness,
    NormalWeight,
    Overweight,
    ObeseClassI,
    ObeseClassII,
    ObeseClassIII
  )
}
