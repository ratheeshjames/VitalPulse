package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val SpaceGroteskFamily = FontFamily(
  Font(R.font.space_grotesk, FontWeight.Normal),
  Font(R.font.space_grotesk, FontWeight.Medium),
  Font(R.font.space_grotesk, FontWeight.Bold)
)

val PlusJakartaSansFamily = FontFamily(
  Font(R.font.plus_jakarta_sans, FontWeight.Normal),
  Font(R.font.plus_jakarta_sans, FontWeight.Medium),
  Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
  Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

val VitalPulseTypography = Typography(
  displayLarge = TextStyle(
    fontFamily = SpaceGroteskFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 54.sp,
    lineHeight = 60.sp,
    letterSpacing = (-1.2).sp
  ),
  displayMedium = TextStyle(
    fontFamily = SpaceGroteskFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 42.sp,
    lineHeight = 48.sp,
    letterSpacing = (-0.8).sp
  ),
  displaySmall = TextStyle(
    fontFamily = SpaceGroteskFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 34.sp,
    lineHeight = 40.sp,
    letterSpacing = (-0.5).sp
  ),
  headlineLarge = TextStyle(
    fontFamily = SpaceGroteskFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 34.sp,
    letterSpacing = (-0.3).sp
  ),
  headlineMedium = TextStyle(
    fontFamily = SpaceGroteskFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    lineHeight = 30.sp,
    letterSpacing = (-0.2).sp
  ),
  headlineSmall = TextStyle(
    fontFamily = SpaceGroteskFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 26.sp
  ),
  titleLarge = TextStyle(
    fontFamily = SpaceGroteskFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 19.sp,
    lineHeight = 25.sp
  ),
  titleMedium = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.1.sp
  ),
  titleSmall = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp
  ),
  bodyLarge = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.15.sp
  ),
  bodyMedium = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.2.sp
  ),
  bodySmall = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.3.sp
  ),
  labelLarge = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp
  ),
  labelMedium = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.3.sp
  ),
  labelSmall = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 15.sp,
    letterSpacing = 0.4.sp
  )
)
