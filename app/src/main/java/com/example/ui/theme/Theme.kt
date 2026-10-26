package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

enum class ThemeMode(val label: String) {
  SYSTEM("System Auto"),
  LIGHT("Light Studio"),
  DARK("Dark OLED")
}

val VitalPulseShapes = Shapes(
  extraSmall = RoundedCornerShape(8.dp),
  small = RoundedCornerShape(12.dp),
  medium = RoundedCornerShape(18.dp),
  large = RoundedCornerShape(24.dp),
  extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun VitalPulseTheme(
  themeMode: ThemeMode = ThemeMode.SYSTEM,
  accentPreset: AccentPreset = AccentPreset.KINETIC_INDIGO,
  useDynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  val darkTheme = when (themeMode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
  }

  val colorScheme = when {
    useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> darkColorScheme(
      primary = accentPreset.primaryDark,
      onPrimary = accentPreset.onPrimaryDark,
      primaryContainer = accentPreset.primaryContainerDark,
      onPrimaryContainer = accentPreset.onPrimaryContainerDark,
      secondary = accentPreset.secondaryDark,
      tertiary = accentPreset.tertiaryDark,
      background = Color(0xFF0B0F17),
      onBackground = Color(0xFFE6EAF2),
      surface = Color(0xFF111723),
      onSurface = Color(0xFFE6EAF2),
      surfaceVariant = Color(0xFF1D2536),
      onSurfaceVariant = Color(0xFFB5C0D4),
      surfaceContainerLowest = Color(0xFF070A10),
      surfaceContainerLow = Color(0xFF131926),
      surfaceContainer = Color(0xFF182030),
      surfaceContainerHigh = Color(0xFF212B3F),
      surfaceContainerHighest = Color(0xFF2B374F),
      outline = Color(0xFF424E66),
      outlineVariant = Color(0xFF293246)
    )
    else -> lightColorScheme(
      primary = accentPreset.primaryLight,
      onPrimary = accentPreset.onPrimaryLight,
      primaryContainer = accentPreset.primaryContainerLight,
      onPrimaryContainer = accentPreset.onPrimaryContainerLight,
      secondary = accentPreset.secondaryLight,
      tertiary = accentPreset.tertiaryLight,
      background = Color(0xFFF6F8FC),
      onBackground = Color(0xFF111827),
      surface = Color(0xFFFFFFFF),
      onSurface = Color(0xFF111827),
      surfaceVariant = Color(0xFFE9EEF7),
      onSurfaceVariant = Color(0xFF475569),
      surfaceContainerLowest = Color(0xFFFFFFFF),
      surfaceContainerLow = Color(0xFFF1F5FB),
      surfaceContainer = Color(0xFFEBF0F9),
      surfaceContainerHigh = Color(0xFFE3EAF5),
      surfaceContainerHighest = Color(0xFFDCE4F2),
      outline = Color(0xFF94A3B8),
      outlineVariant = Color(0xFFCBD5E1)
    )
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = VitalPulseTypography,
    shapes = VitalPulseShapes,
    content = content
  )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  VitalPulseTheme(
    themeMode = if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT,
    content = content
  )
}
