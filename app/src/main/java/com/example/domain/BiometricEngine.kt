package com.example.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt
import org.json.JSONArray
import org.json.JSONObject

object BiometricEngine {

  private val STANDARD_LIMITS = listOf(16.0, 17.0, 18.5, 25.0, 30.0, 35.0, 40.0)
  private val ASIAN_LIMITS = listOf(16.0, 17.0, 18.5, 23.0, 27.5, 32.5, 37.5)

  private val TIPS_BY_TIER = listOf(
    listOf(
      "Add nutrient-dense foods and quality protein at each meal.",
      "Progressive strength training helps build lean, healthy body mass.",
      "Consider a clinical check-up to rule out underlying metabolic causes."
    ),
    listOf(
      "Keep up regular cardiovascular and strength activity: ~150 minutes/week.",
      "Prioritize colorful vegetables, whole fruits, lean proteins, and whole grains.",
      "Maintain 7–9 hours of restorative sleep and steady daily hydration."
    ),
    listOf(
      "Aim for small, sustainable habits such as a brisk 30-minute daily walk.",
      "Cut back on liquid calories, sugary drinks, and ultra-processed snacks.",
      "Monitor waist circumference alongside scale weight for visceral health."
    ),
    listOf(
      "Partner with a healthcare professional on a personalized, safe weight plan.",
      "Start with joint-friendly activity such as walking, cycling, or swimming.",
      "Focus on sustainable behavioral routines rather than crash dieting."
    )
  )

  fun getCutoffs(useAsianCutoffs: Boolean): List<Double> =
    if (useAsianCutoffs) ASIAN_LIMITS else STANDARD_LIMITS

  fun classifyBmi(bmi: Double, useAsianCutoffs: Boolean): BmiCategory {
    val limits = getCutoffs(useAsianCutoffs)
    var idx = 0
    while (idx < 7 && bmi >= limits[idx]) {
      idx++
    }
    return BmiCategory.fromIndex(idx)
  }

  fun toKg(value: Double, unit: WeightUnit): Double = value / unit.factorFromKg

  fun fromKg(kg: Double, unit: WeightUnit): Double = kg * unit.factorFromKg

  fun formatNum(value: Double, decimals: Int = 1): String {
    if (!value.isFinite()) return "–"
    return String.format(Locale.US, "%.${decimals}f", value)
  }

  fun formatWeight(kg: Double, unit: WeightUnit, decimals: Int = 1): String {
    return "${formatNum(fromKg(kg, unit), decimals)} ${unit.code}"
  }

  fun getHeightCm(profile: ProfileData): Double {
    return if (profile.unitSystem == UnitSystem.METRIC) {
      profile.heightCm
    } else {
      profile.heightFt * 30.48 + profile.heightIn * 2.54
    }
  }

  // Error function approximation matching the reference implementation
  fun erf(xInput: Double): Double {
    val s = if (xInput < 0) -1.0 else if (xInput > 0) 1.0 else 0.0
    val x = abs(xInput)
    val t = 1.0 / (1.0 + 0.3275911 * x)
    val poly = (((((1.061405429 * t - 1.453152027) * t) + 1.421413741) * t - 0.284496736) * t + 0.254829592) * t
    return s * (1.0 - poly * exp(-x * x))
  }

  // Child/Teen BMI-for-age percentile estimation (ages 2-19)
  fun estimateChildPercentile(bmi: Double, age: Int, sex: BiologicalSex): Double {
    val table = listOf(
      2.0 to 16.4,
      4.0 to 15.6,
      6.0 to 15.3,
      8.0 to 15.8,
      10.0 to 16.8,
      12.0 to 18.0,
      14.0 to 19.4,
      16.0 to 20.5,
      18.0 to 21.5,
      19.0 to 22.0
    )
    var median = table.last().second
    val a = age.toDouble()
    for (j in 0 until table.size - 1) {
      val (a0, m0) = table[j]
      val (a1, m1) = table[j + 1]
      if (a >= a0 && a <= a1) {
        median = m0 + (m1 - m0) * (a - a0) / (a1 - a0)
        break
      }
    }
    if (sex == BiologicalSex.FEMALE) {
      median *= 1.01
    }
    val z = ln(bmi / median) / 0.13 / sqrt(2.0)
    return (50.0 * (1.0 + erf(z))).coerceIn(0.1, 99.9)
  }

  fun analyzeBiometrics(profile: ProfileData, useAsianCutoffs: Boolean): BiometricAnalysis {
    val kg = toKg(profile.weightDisplay, profile.weightUnit)
    val cm = getHeightCm(profile)
    val age = profile.age
    val cutoffs = getCutoffs(useAsianCutoffs)

    val isValid = kg in 2.0..500.0 && cm in 50.0..272.0 && age in 2..120
    if (!isValid) {
      return BiometricAnalysis(
        isValid = false,
        validationError = "Enter a height between 50–272 cm, a weight of 2–500 kg, and an age of 2–120.",
        cutoffs = cutoffs
      )
    }

    val m = cm / 100.0
    val bmi = kg / (m * m)
    val category = classifyBmi(bmi, useAsianCutoffs)
    val isChild = age < 20
    val isMale = profile.sex == BiologicalSex.MALE

    val upperNormalBmi = if (useAsianCutoffs) 23.0 else 25.0
    val healthyMinKg = 18.5 * m * m
    val healthyMaxKg = upperNormalBmi * m * m

    val deltaFormatted = when {
      kg < healthyMinKg -> "Gain ${formatWeight(healthyMinKg - kg, profile.weightUnit)}"
      kg > healthyMaxKg -> "Lose ${formatWeight(kg - healthyMaxKg, profile.weightUnit)}"
      else -> "In healthy range"
    }

    val bmiPrime = bmi / upperNormalBmi
    val ponderalIndex = kg / m.pow(3.0)
    val trefethenNewBmi = 1.3 * kg / m.pow(2.5)
    val bodyFat = 1.2 * bmi + 0.23 * age - 10.8 * (if (isMale) 1.0 else 0.0) - 5.4

    var childPct: Double? = null
    var childPctCat: String? = null
    val noteBuilder = StringBuilder()
    if (useAsianCutoffs) {
      noteBuilder.append("Asian-population WHO cutoffs applied (overweight from 23.0). ")
    }
    if (isChild) {
      val p = estimateChildPercentile(bmi, age, profile.sex)
      childPct = p
      val status = when {
        p < 5.0 -> "underweight"
        p < 85.0 -> "healthy weight"
        p < 95.0 -> "overweight"
        else -> "obese"
      }
      childPctCat = status
      noteBuilder.append(
        "Estimated percentile for age $age: ${formatNum(p, 0)}th ($status). Simplified estimate."
      )
    } else if (age >= 65) {
      noteBuilder.append("For adults 65+, a slightly higher BMI (about 25–27) can be protective.")
    }

    return BiometricAnalysis(
      isValid = true,
      weightKg = kg,
      heightCm = cm,
      heightMeters = m,
      bmi = bmi,
      category = category,
      healthyWeightMinKg = healthyMinKg,
      healthyWeightMaxKg = healthyMaxKg,
      healthyWeightRangeFormatted = "${formatWeight(healthyMinKg, profile.weightUnit)} – ${
        formatWeight(healthyMaxKg, profile.weightUnit)
      }",
      deltaToHealthyFormatted = deltaFormatted,
      bmiPrime = bmiPrime,
      ponderalIndex = ponderalIndex,
      trefethenNewBmi = trefethenNewBmi,
      deurenbergBodyFatPct = bodyFat,
      childPercentile = childPct,
      childPercentileCategory = childPctCat,
      contextualNote = noteBuilder.toString().trim(),
      personalizedTips = TIPS_BY_TIER[category.tipTier],
      cutoffs = cutoffs
    )
  }

  fun analyzeMetabolism(
    profile: ProfileData,
    biometrics: BiometricAnalysis
  ): MetabolicAnalysis? {
    if (!biometrics.isValid) return null
    val kg = biometrics.weightKg
    val cm = biometrics.heightCm
    val age = profile.age
    val isMale = profile.sex == BiologicalSex.MALE

    val bodyFatClamped = max(biometrics.deurenbergBodyFatPct, 0.0)
    val leanBodyMassKg = kg * (1.0 - bodyFatClamped / 100.0)

    val bmrMifflin = 10.0 * kg + 6.25 * cm - 5.0 * age + (if (isMale) 5.0 else -161.0)
    val bmrHarris = if (isMale) {
      88.362 + 13.397 * kg + 4.799 * cm - 5.677 * age
    } else {
      447.593 + 9.247 * kg + 3.098 * cm - 4.33 * age
    }
    val tdee = bmrMifflin * profile.activityLevel.multiplier

    val bsa = sqrt(cm * kg / 3600.0)
    val waterLiters = kg * 0.033

    val waistVal = profile.waistInput.toDoubleOrNull()?.takeIf { it > 0.0 }
    val hipVal = profile.hipInput.toDoubleOrNull()?.takeIf { it > 0.0 }
    val unitRatioToCm = if (profile.unitSystem == UnitSystem.METRIC) 1.0 else 2.54

    val whr = if (waistVal != null && hipVal != null) waistVal / hipVal else null
    val whrStatus = whr?.let { ratio ->
      val threshold = if (isMale) 0.90 else 0.85
      if (ratio <= threshold) "Low cardiovascular risk" else "Elevated central risk"
    }

    val whtr = if (waistVal != null) (waistVal * unitRatioToCm) / cm else null
    val whtrStatus = whtr?.let { ratio ->
      when {
        ratio < 0.40 -> "Slender central frame"
        ratio < 0.50 -> "Optimal (< 0.50)"
        ratio < 0.60 -> "Elevated visceral adiposity"
        else -> "High central risk"
      }
    }

    val rateKcalDelta = profile.weeklyRateKg * 1100.0
    val goalKg = toKg(profile.goalWeightDisplay, profile.weightUnit)
    val targetGoalCalories = when {
      goalKg < kg - 0.05 -> tdee - rateKcalDelta
      goalKg > kg + 0.05 -> tdee + rateKcalDelta
      else -> tdee
    }.coerceAtLeast(1000.0)

    val pGrams = (targetGoalCalories * 0.30 / 4.0).roundToInt()
    val cGrams = (targetGoalCalories * 0.40 / 4.0).roundToInt()
    val fGrams = (targetGoalCalories * 0.30 / 9.0).roundToInt()

    val inchesOver5Ft = max(cm / 2.54 - 60.0, 0.0)
    val formulas = listOf(
      IdealWeightFormulaResult(
        formulaName = "Devine",
        subtitle = "Clinical dosing standard (1974)",
        weightKg = if (isMale) 50.0 + 2.3 * inchesOver5Ft else 45.5 + 2.3 * inchesOver5Ft,
        formattedWeight = formatWeight(
          if (isMale) 50.0 + 2.3 * inchesOver5Ft else 45.5 + 2.3 * inchesOver5Ft,
          profile.weightUnit
        )
      ),
      IdealWeightFormulaResult(
        formulaName = "Robinson",
        subtitle = "Actuarial refinement (1983)",
        weightKg = if (isMale) 52.0 + 1.9 * inchesOver5Ft else 49.0 + 1.7 * inchesOver5Ft,
        formattedWeight = formatWeight(
          if (isMale) 52.0 + 1.9 * inchesOver5Ft else 49.0 + 1.7 * inchesOver5Ft,
          profile.weightUnit
        )
      ),
      IdealWeightFormulaResult(
        formulaName = "Miller",
        subtitle = "Median frame curve (1983)",
        weightKg = if (isMale) 56.2 + 1.41 * inchesOver5Ft else 53.1 + 1.36 * inchesOver5Ft,
        formattedWeight = formatWeight(
          if (isMale) 56.2 + 1.41 * inchesOver5Ft else 53.1 + 1.36 * inchesOver5Ft,
          profile.weightUnit
        )
      ),
      IdealWeightFormulaResult(
        formulaName = "Hamwi",
        subtitle = "Dietetic baseline (1964)",
        weightKg = if (isMale) 48.0 + 2.7 * inchesOver5Ft else 45.4 + 2.27 * inchesOver5Ft,
        formattedWeight = formatWeight(
          if (isMale) 48.0 + 2.7 * inchesOver5Ft else 45.4 + 2.27 * inchesOver5Ft,
          profile.weightUnit
        )
      )
    )

    return MetabolicAnalysis(
      bmrMifflinStJeor = bmrMifflin.roundToInt(),
      bmrHarrisBenedict = bmrHarris.roundToInt(),
      tdeeDailyBurn = tdee.roundToInt(),
      leanBodyMassKg = leanBodyMassKg,
      leanBodyMassFormatted = formatWeight(leanBodyMassKg, profile.weightUnit),
      bodySurfaceAreaM2 = bsa,
      dailyWaterLiters = waterLiters,
      waistToHipRatio = whr,
      waistToHipStatus = whrStatus,
      waistToHeightRatio = whtr,
      waistToHeightStatus = whtrStatus,
      loseCalories = max((tdee - 500.0).roundToInt(), 1000),
      maintainCalories = tdee.roundToInt(),
      gainCalories = (tdee + 300.0).roundToInt(),
      goalRateCalories = targetGoalCalories.roundToInt(),
      goalMacros = MacroBreakdown(
        calories = targetGoalCalories.roundToInt(),
        proteinGrams = pGrams,
        carbsGrams = cGrams,
        fatGrams = fGrams,
        formattedSummary = "P ${pGrams}g · C ${cGrams}g · F ${fGrams}g"
      ),
      idealWeightFormulas = formulas
    )
  }

  fun analyzeGoalProjection(
    profile: ProfileData,
    biometrics: BiometricAnalysis,
    chronologicalEntries: List<WeightEntry>,
    useAsianCutoffs: Boolean
  ): GoalProjection? {
    if (!biometrics.isValid) return null
    val currentKg = biometrics.weightKg
    val goalKg = toKg(profile.goalWeightDisplay, profile.weightUnit).coerceIn(2.0, 500.0)
    val startKg = profile.startWeightKg ?: chronologicalEntries.firstOrNull()?.weightKg ?: currentKg
    val rate = profile.weeklyRateKg.coerceAtLeast(0.1)

    val diffKg = abs(currentKg - goalKg)
    val isReached = diffKg < 0.05
    val weeks = if (isReached) 0.0 else diffKg / rate

    val progressPct = if (abs(startKg - goalKg) < 0.01) {
      100f
    } else {
      (((startKg - currentKg) / (startKg - goalKg)) * 100.0).toFloat().coerceIn(0f, 100f)
    }

    val projectedMillis = System.currentTimeMillis() + (weeks * 7.0 * 24.0 * 3600.0 * 1000.0).toLong()
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
    val projectedDateStr = if (isReached) "Reached" else dateFormat.format(Date(projectedMillis))

    val bmiAtGoal = goalKg / (biometrics.heightMeters * biometrics.heightMeters)
    val categoryAtGoal = classifyBmi(bmiAtGoal, useAsianCutoffs)

    val startStr = formatWeight(startKg, profile.weightUnit)
    val goalStr = formatWeight(goalKg, profile.weightUnit)

    return GoalProjection(
      startWeightKg = startKg,
      currentWeightKg = currentKg,
      goalWeightKg = goalKg,
      startFormatted = startStr,
      goalFormatted = goalStr,
      differenceFormatted = if (isReached) "Reached" else formatWeight(diffKg, profile.weightUnit),
      directionLabel = if (goalKg < currentKg) "To lose" else "To gain",
      isGoalReached = isReached,
      progressPercent = progressPct,
      progressSummaryText = "${formatNum(progressPct.toDouble(), 0)}% of the way from $startStr to $goalStr",
      weeksRemaining = weeks,
      weeksFormatted = "${formatNum(weeks, 0)} weeks",
      projectedDateFormatted = projectedDateStr,
      bmiAtGoal = bmiAtGoal,
      categoryAtGoal = categoryAtGoal
    )
  }

  fun simulateWhatIf(
    whatIfWeightKg: Double,
    profile: ProfileData,
    biometrics: BiometricAnalysis,
    useAsianCutoffs: Boolean
  ): WhatIfSimulation? {
    if (!biometrics.isValid) return null
    val clampedKg = whatIfWeightKg.coerceIn(30.0, 200.0)
    val bmi = clampedKg / (biometrics.heightMeters * biometrics.heightMeters)
    val cat = classifyBmi(bmi, useAsianCutoffs)
    val delta = clampedKg - biometrics.weightKg
    val signPrefix = if (delta > 0.05) "+" else if (delta < -0.05) "−" else "±"
    val deltaStr = "$signPrefix${formatWeight(abs(delta), profile.weightUnit)} from current"
    return WhatIfSimulation(
      weightKg = clampedKg,
      formattedWeight = formatWeight(clampedKg, profile.weightUnit),
      bmi = bmi,
      category = cat,
      deltaFromCurrentFormatted = deltaStr
    )
  }

  fun summarizeTrends(
    entries: List<WeightEntry>,
    weightUnit: WeightUnit
  ): TrendSummary {
    val chronological = entries.sortedWith(compareBy<WeightEntry> { it.dateIso }.thenBy { it.id })
    val reverse = chronological.reversed()
    if (chronological.isEmpty()) {
      return TrendSummary(
        entriesChronological = emptyList(),
        entriesReverseChronological = emptyList(),
        lowestBmi = null,
        highestBmi = null,
        averageBmi = null,
        totalWeightChangeKg = null,
        totalWeightChangeFormatted = null
      )
    }
    val bmis = chronological.map { it.bmi }
    val weights = chronological.map { it.weightKg }
    val netChangeKg = weights.last() - weights.first()
    val prefix = if (netChangeKg >= 0.0) "+" else "−"
    return TrendSummary(
      entriesChronological = chronological,
      entriesReverseChronological = reverse,
      lowestBmi = bmis.minOrNull(),
      highestBmi = bmis.maxOrNull(),
      averageBmi = bmis.average(),
      totalWeightChangeKg = netChangeKg,
      totalWeightChangeFormatted = "$prefix${formatWeight(abs(netChangeKg), weightUnit)}"
    )
  }

  fun getReferenceTableRows(useAsianCutoffs: Boolean): List<CategoryThresholdRow> {
    val l = getCutoffs(useAsianCutoffs)
    return BmiCategory.entries.mapIndexed { i, category ->
      val rangeText = when (i) {
        0 -> "Below ${formatNum(l[0], 1)}"
        7 -> "${formatNum(l[6], 1)} and above"
        else -> "${formatNum(l[i - 1], 1)} – ${formatNum(l[i], 1)}"
      }
      CategoryThresholdRow(category, rangeText)
    }
  }

  fun convertProfileUnitSystem(profile: ProfileData, targetSystem: UnitSystem): ProfileData {
    if (profile.unitSystem == targetSystem) return profile
    var updated = profile
    if (targetSystem == UnitSystem.IMPERIAL) {
      val totalInches = profile.heightCm / 2.54
      val ft = floor(totalInches / 12.0).toInt()
      val remIn = formatNum(totalInches % 12.0, 1).toDoubleOrNull() ?: 0.0
      val newWaist = profile.waistInput.toDoubleOrNull()?.let { formatNum(it / 2.54, 1) } ?: ""
      val newHip = profile.hipInput.toDoubleOrNull()?.let { formatNum(it / 2.54, 1) } ?: ""
      updated = updated.copy(
        heightFt = ft,
        heightIn = remIn,
        waistInput = newWaist,
        hipInput = newHip
      )
    } else {
      val cm = formatNum(profile.heightFt * 30.48 + profile.heightIn * 2.54, 1).toDoubleOrNull() ?: 170.0
      val newWaist = profile.waistInput.toDoubleOrNull()?.let { formatNum(it * 2.54, 1) } ?: ""
      val newHip = profile.hipInput.toDoubleOrNull()?.let { formatNum(it * 2.54, 1) } ?: ""
      updated = updated.copy(
        heightCm = cm,
        waistInput = newWaist,
        hipInput = newHip
      )
    }

    val targetWeightUnit = if (targetSystem == UnitSystem.IMPERIAL) WeightUnit.LB else WeightUnit.KG
    if (targetWeightUnit != profile.weightUnit) {
      val currentKg = toKg(profile.weightDisplay, profile.weightUnit)
      val goalKg = toKg(profile.goalWeightDisplay, profile.weightUnit)
      updated = updated.copy(
        weightUnit = targetWeightUnit,
        weightDisplay = formatNum(fromKg(currentKg, targetWeightUnit), 1).toDoubleOrNull() ?: 70.0,
        goalWeightDisplay = formatNum(fromKg(goalKg, targetWeightUnit), 1).toDoubleOrNull() ?: 65.0
      )
    }

    return updated.copy(unitSystem = targetSystem)
  }

  fun convertProfileWeightUnit(profile: ProfileData, targetWeightUnit: WeightUnit): ProfileData {
    if (profile.weightUnit == targetWeightUnit) return profile
    val currentKg = toKg(profile.weightDisplay, profile.weightUnit)
    val goalKg = toKg(profile.goalWeightDisplay, profile.weightUnit)
    return profile.copy(
      weightUnit = targetWeightUnit,
      weightDisplay = formatNum(fromKg(currentKg, targetWeightUnit), 2).toDoubleOrNull() ?: 70.0,
      goalWeightDisplay = formatNum(fromKg(goalKg, targetWeightUnit), 2).toDoubleOrNull() ?: 65.0
    )
  }

  fun exportCsv(entries: List<WeightEntry>): String {
    val sorted = entries.sortedBy { it.dateIso }
    val sb = StringBuilder("Date,Weight(kg),BMI,Note\n")
    for (entry in sorted) {
      val escapedNote = entry.note.replace("\"", "\"\"")
      sb.append("${entry.dateIso},${formatNum(entry.weightKg, 2)},${formatNum(entry.bmi, 2)},\"$escapedNote\"\n")
    }
    return sb.toString().trimEnd()
  }

  fun exportBackupJson(
    settings: AppSettingsState,
    profiles: List<ProfileData>,
    entriesByProfile: Map<String, List<WeightEntry>>
  ): String {
    val root = JSONObject()
    root.put("cur", settings.activeProfileName)
    val setObj = JSONObject().apply {
      put("theme", when (settings.themeMode) {
        com.example.ui.theme.ThemeMode.SYSTEM -> "auto"
        com.example.ui.theme.ThemeMode.LIGHT -> "light"
        com.example.ui.theme.ThemeMode.DARK -> "dark"
      })
      put("ac", settings.accentPreset.name)
      put("dynamicColor", settings.useDynamicColor)
      put("asian", settings.useAsianCutoffs)
    }
    root.put("set", setObj)

    val pObj = JSONObject()
    for (prof in profiles) {
      val pEntry = JSONObject()
      val iObj = JSONObject().apply {
        put("u", prof.unitSystem.code)
        put("cm", prof.heightCm)
        put("ft", prof.heightFt)
        put("inn", prof.heightIn)
        put("w", prof.weightDisplay)
        put("wu", prof.weightUnit.code)
        put("age", prof.age)
        put("sex", prof.sex.code)
        put("act", prof.activityLevel.index)
        put("waist", prof.waistInput)
        put("hip", prof.hipInput)
        put("goal", prof.goalWeightDisplay)
        put("rate", prof.weeklyRateKg)
      }
      pEntry.put("i", iObj)
      if (prof.startWeightKg != null) {
        pEntry.put("start", prof.startWeightKg)
      } else {
        pEntry.put("start", JSONObject.NULL)
      }

      val eArr = JSONArray()
      val list = entriesByProfile[prof.name].orEmpty()
      for (item in list) {
        eArr.put(
          JSONObject().apply {
            put("id", item.id)
            put("d", item.dateIso)
            put("kg", item.weightKg)
            put("bmi", item.bmi)
            put("n", item.note)
          }
        )
      }
      pEntry.put("e", eArr)
      pObj.put(prof.name, pEntry)
    }
    root.put("p", pObj)
    return root.toString(2)
  }

  data class RestoredBackup(
    val activeProfile: String,
    val useAsianCutoffs: Boolean,
    val profiles: List<ProfileData>,
    val entries: List<WeightEntry>
  )

  fun parseBackupJson(jsonText: String): RestoredBackup? {
    return try {
      val root = JSONObject(jsonText.trim())
      val cur = root.optString("cur", "").takeIf { it.isNotBlank() } ?: return null
      val pObj = root.optJSONObject("p") ?: return null
      if (!pObj.has(cur)) return null

      val setObj = root.optJSONObject("set")
      val asian = setObj?.optBoolean("asian", false) ?: false

      val profiles = mutableListOf<ProfileData>()
      val entries = mutableListOf<WeightEntry>()

      val keys = pObj.keys()
      while (keys.hasNext()) {
        val profName = keys.next()
        val profNode = pObj.optJSONObject(profName) ?: continue
        val iNode = profNode.optJSONObject("i") ?: JSONObject()
        val startVal = if (profNode.has("start") && !profNode.isNull("start")) {
          profNode.optDouble("start").takeIf { it.isFinite() && it > 0 }
        } else null

        val profileData = ProfileData(
          name = profName,
          unitSystem = UnitSystem.fromCode(iNode.optString("u", "m")),
          heightCm = iNode.optDouble("cm", 170.0),
          heightFt = iNode.optInt("ft", 5),
          heightIn = iNode.optDouble("inn", 7.0),
          weightDisplay = iNode.optDouble("w", 70.0),
          weightUnit = WeightUnit.fromCode(iNode.optString("wu", "kg")),
          age = iNode.optInt("age", 30),
          sex = BiologicalSex.fromCode(iNode.optString("sex", "m")),
          activityLevel = ActivityLevel.fromIndex(iNode.optInt("act", 1)),
          waistInput = iNode.optString("waist", ""),
          hipInput = iNode.optString("hip", ""),
          goalWeightDisplay = iNode.optDouble("goal", 65.0),
          weeklyRateKg = iNode.optDouble("rate", 0.5),
          startWeightKg = startVal
        )
        profiles.add(profileData)

        val eArr = profNode.optJSONArray("e")
        if (eArr != null) {
          for (idx in 0 until eArr.length()) {
            val eNode = eArr.optJSONObject(idx) ?: continue
            val d = eNode.optString("d", "")
            val kg = eNode.optDouble("kg", Double.NaN)
            val bmi = eNode.optDouble("bmi", Double.NaN)
            if (d.isNotBlank() && kg.isFinite() && bmi.isFinite()) {
              entries.add(
                WeightEntry(
                  id = 0L,
                  profileName = profName,
                  dateIso = d,
                  weightKg = kg,
                  bmi = bmi,
                  note = eNode.optString("n", "")
                )
              )
            }
          }
        }
      }
      if (profiles.isEmpty()) null else RestoredBackup(cur, asian, profiles, entries)
    } catch (_: Exception) {
      null
    }
  }
}
