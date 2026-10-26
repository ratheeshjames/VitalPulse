package com.example

import com.example.domain.BiologicalSex
import com.example.domain.BiometricEngine
import com.example.domain.BmiCategory
import com.example.domain.ProfileData
import com.example.domain.UnitSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun biometricCalculations_standardAndAsianCutoffs_areAccurate() {
    val profile = ProfileData(
      name = "Me",
      unitSystem = UnitSystem.METRIC,
      heightCm = 170.0,
      weightDisplay = 70.0,
      age = 30,
      sex = BiologicalSex.MALE
    )
    // 70 / (1.7 * 1.7) = 24.22145
    val standardResult = BiometricEngine.analyzeBiometrics(profile, useAsianCutoffs = false)
    assertTrue(standardResult.isValid)
    assertEquals(24.22, standardResult.bmi, 0.02)
    assertEquals(BmiCategory.NORMAL_WEIGHT, standardResult.category)

    // With Asian cutoffs (overweight >= 23.0), BMI 24.22 is Overweight
    val asianResult = BiometricEngine.analyzeBiometrics(profile, useAsianCutoffs = true)
    assertEquals(BmiCategory.OVERWEIGHT, asianResult.category)

    val metabolism = BiometricEngine.analyzeMetabolism(profile, standardResult)
    assertNotNull(metabolism)
    // Mifflin-St Jeor for 70kg, 170cm, 30yo Male: 10*70 + 6.25*170 - 5*30 + 5 = 700 + 1062.5 - 150 + 5 = 1618
    assertEquals(1618, metabolism!!.bmrMifflinStJeor)
  }
}
