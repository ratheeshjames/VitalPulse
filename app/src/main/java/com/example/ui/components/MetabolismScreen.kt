package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.ActivityLevel
import com.example.domain.BiometricEngine
import com.example.domain.MetabolicAnalysis
import com.example.domain.ProfileData
import com.example.domain.UnitSystem

@Composable
fun MetabolismScreen(
  profile: ProfileData,
  metabolism: MetabolicAnalysis?,
  onWaistHipChange: (String, String) -> Unit,
  onActivityLevelChange: (ActivityLevel) -> Unit,
  modifier: Modifier = Modifier
) {
  val lengthUnitLabel = if (profile.unitSystem == UnitSystem.METRIC) "cm" else "in"
  var waistText by remember(profile.name, profile.unitSystem, profile.waistInput) {
    mutableStateOf(profile.waistInput)
  }
  var hipText by remember(profile.name, profile.unitSystem, profile.hipInput) {
    mutableStateOf(profile.hipInput)
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("metabolism_screen_list"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Circumference & Activity Level Card
    item {
      ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Outlined.Straighten,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Body Circumference & Activity",
              style = MaterialTheme.typography.titleLarge
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            OutlinedTextField(
              value = waistText,
              onValueChange = { text ->
                waistText = text
                onWaistHipChange(text, hipText)
              },
              label = { Text("Waist ($lengthUnitLabel)") },
              placeholder = { Text("Optional") },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              modifier = Modifier
                .weight(1f)
                .testTag("waist_input")
            )
            OutlinedTextField(
              value = hipText,
              onValueChange = { text ->
                hipText = text
                onWaistHipChange(waistText, text)
              },
              label = { Text("Hip ($lengthUnitLabel)") },
              placeholder = { Text("Optional") },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              modifier = Modifier
                .weight(1f)
                .testTag("hip_input")
            )
          }

          Text(
            text = "Weekly Activity Level",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ActivityLevel.entries.forEach { level ->
              val selected = profile.activityLevel == level
              OutlinedCard(
                onClick = { onActivityLevelChange(level) },
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.outlinedCardColors(
                  containerColor = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                  } else {
                    MaterialTheme.colorScheme.surface
                  }
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("activity_level_${level.index}")
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  RadioButton(
                    selected = selected,
                    onClick = { onActivityLevelChange(level) }
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = level.title,
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.SemiBold
                    )
                    Text(
                      text = level.subtitle,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                  Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CircleShape
                  ) {
                    Text(
                      text = "×${level.multiplier}",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSecondaryContainer,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    if (metabolism != null) {
      // 2. Body Metrics & Metabolic Output Bento Card
      item {
        ElevatedCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("body_metrics_card"),
          shape = MaterialTheme.shapes.large,
          colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
          )
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Outlined.LocalFireDepartment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Metabolic & Physiological Metrics",
                style = MaterialTheme.typography.titleLarge
              )
            }

            val bodyItems = buildList {
              add(Triple("BMR (Mifflin-St Jeor)", "${metabolism.bmrMifflinStJeor} kcal", "Resting baseline"))
              add(Triple("BMR (Harris-Benedict)", "${metabolism.bmrHarrisBenedict} kcal", "Classic equation"))
              add(Triple("TDEE (Daily Burn)", "${metabolism.tdeeDailyBurn} kcal", profile.activityLevel.title))
              add(Triple("Lean Body Mass (est.)", metabolism.leanBodyMassFormatted, "Fat-free mass"))
              add(Triple("Body Surface Area", "${BiometricEngine.formatNum(metabolism.bodySurfaceAreaM2, 2)} m²", "Mosteller BSA"))
              add(Triple("Daily Hydration", "${BiometricEngine.formatNum(metabolism.dailyWaterLiters, 1)} L", "33 ml / kg target"))
              if (metabolism.waistToHipRatio != null) {
                add(
                  Triple(
                    "Waist-to-Hip Ratio",
                    BiometricEngine.formatNum(metabolism.waistToHipRatio, 2),
                    metabolism.waistToHipStatus
                  )
                )
              }
              if (metabolism.waistToHeightRatio != null) {
                add(
                  Triple(
                    "Waist-to-Height Ratio",
                    BiometricEngine.formatNum(metabolism.waistToHeightRatio, 2),
                    metabolism.waistToHeightStatus
                  )
                )
              }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              bodyItems.chunked(2).forEach { rowItems ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  rowItems.forEach { (label, value, sub) ->
                    MetricTile(
                      label = label,
                      value = value,
                      subtitle = sub,
                      modifier = Modifier.weight(1f)
                    )
                  }
                  if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                  }
                }
              }
            }
          }
        }
      }

      // 3. Calories & Macros Studio Card
      item {
        ElevatedCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("calories_macros_card"),
          shape = MaterialTheme.shapes.large,
          colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
          )
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Outlined.Restaurant,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Daily Energy & Macro Split",
                style = MaterialTheme.typography.titleLarge
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              MetricTile(
                label = "Lose (−500)",
                value = "${metabolism.loseCalories} kcal",
                modifier = Modifier.weight(1f)
              )
              MetricTile(
                label = "Maintain",
                value = "${metabolism.maintainCalories} kcal",
                modifier = Modifier.weight(1f)
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              MetricTile(
                label = "Gain (+300)",
                value = "${metabolism.gainCalories} kcal",
                modifier = Modifier.weight(1f)
              )
              MetricTile(
                label = "For Goal & Rate",
                value = "${metabolism.goalRateCalories} kcal",
                subtitle = "${profile.weeklyRateKg} kg/wk pace",
                modifier = Modifier.weight(1f)
              )
            }

            // 30 / 40 / 30 Macro Breakdown Card
            Surface(
              color = MaterialTheme.colorScheme.surfaceContainerHigh,
              shape = MaterialTheme.shapes.medium,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Macros at Goal Calories (30/40/30)",
                    style = MaterialTheme.typography.titleSmall
                  )
                  Text(
                    text = metabolism.goalMacros.formattedSummary,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("macros_summary_text")
                  )
                }

                // Visual 30/40/30 bar
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                ) {
                  Box(
                    modifier = Modifier
                      .weight(0.3f)
                      .height(10.dp)
                      .background(Color(0xFF3B82F6))
                  )
                  Box(
                    modifier = Modifier
                      .weight(0.4f)
                      .height(10.dp)
                      .background(Color(0xFF10B981))
                  )
                  Box(
                    modifier = Modifier
                      .weight(0.3f)
                      .height(10.dp)
                      .background(Color(0xFFF59E0B))
                  )
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  MacroLegendDot("Protein 30%: ${metabolism.goalMacros.proteinGrams}g", Color(0xFF3B82F6))
                  MacroLegendDot("Carbs 40%: ${metabolism.goalMacros.carbsGrams}g", Color(0xFF10B981))
                  MacroLegendDot("Fat 30%: ${metabolism.goalMacros.fatGrams}g", Color(0xFFF59E0B))
                }
              }
            }
          }
        }
      }

      // 4. Ideal Weight Formulas Comparison Card
      item {
        ElevatedCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("ideal_weight_formulas_card"),
          shape = MaterialTheme.shapes.large,
          colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
          )
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Outlined.Balance,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Ideal Weight Reference Formulas",
                style = MaterialTheme.typography.titleLarge
              )
            }

            val maxFormulaKg = (metabolism.idealWeightFormulas.maxOfOrNull { it.weightKg } ?: 80.0).coerceAtLeast(1.0)
            metabolism.idealWeightFormulas.forEach { item ->
              Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier.padding(12.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text(
                        text = item.formulaName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                      )
                      Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                    Text(
                      text = item.formattedWeight,
                      style = MaterialTheme.typography.titleMedium,
                      color = MaterialTheme.colorScheme.primary,
                      fontWeight = FontWeight.Bold
                    )
                  }
                  LinearProgressIndicator(
                    progress = { (item.weightKg / maxFormulaKg).toFloat().coerceIn(0.1f, 1f) },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(6.dp)
                      .clip(RoundedCornerShape(3.dp))
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun MacroLegendDot(text: String, color: Color) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .clip(CircleShape)
        .background(color)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
