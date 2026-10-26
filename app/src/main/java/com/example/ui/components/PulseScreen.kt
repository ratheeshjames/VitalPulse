package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.BiologicalSex
import com.example.domain.BiometricAnalysis
import com.example.domain.BiometricEngine
import com.example.domain.ProfileData
import com.example.domain.UnitSystem
import com.example.domain.WeightUnit
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PulseScreen(
  profile: ProfileData,
  biometrics: BiometricAnalysis,
  useAsianCutoffs: Boolean,
  onUnitSystemChange: (UnitSystem) -> Unit,
  onWeightUnitChange: (WeightUnit) -> Unit,
  onHeightCmChange: (Double) -> Unit,
  onHeightImperialChange: (Int, Double) -> Unit,
  onWeightChange: (Double) -> Unit,
  onAgeChange: (Int) -> Unit,
  onSexChange: (BiologicalSex) -> Unit,
  onAsianCutoffsToggle: (Boolean) -> Unit,
  onResetInputs: () -> Unit,
  onShareClick: () -> Unit,
  onPrintClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var cmText by remember(profile.name, profile.unitSystem, profile.heightCm) {
    mutableStateOf(BiometricEngine.formatNum(profile.heightCm, 1).removeSuffix(".0"))
  }
  var ftText by remember(profile.name, profile.unitSystem, profile.heightFt) {
    mutableStateOf(profile.heightFt.toString())
  }
  var inText by remember(profile.name, profile.unitSystem, profile.heightIn) {
    mutableStateOf(BiometricEngine.formatNum(profile.heightIn, 1).removeSuffix(".0"))
  }
  var weightText by remember(profile.name, profile.weightUnit, profile.weightDisplay) {
    mutableStateOf(BiometricEngine.formatNum(profile.weightDisplay, 1).removeSuffix(".0"))
  }
  var ageText by remember(profile.name, profile.age) {
    mutableStateOf(profile.age.toString())
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("pulse_screen_list"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Hero Radial Gauge Card
    item {
      BiometricGaugeCard(
        biometrics = biometrics,
        onShareClick = onShareClick,
        onPrintClick = onPrintClick
      )
    }

    // 2. Interactive Biometric Studio Controls Card
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
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Outlined.Straighten,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Biometric Parameters",
                style = MaterialTheme.typography.titleLarge
              )
            }
            OutlinedButton(
              onClick = onResetInputs,
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              modifier = Modifier.testTag("reset_inputs_button")
            ) {
              Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = stringResource(R.string.cd_reset_inputs),
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Reset", style = MaterialTheme.typography.labelMedium)
            }
          }

          // Unit System Segmented Button Row
          SingleChoiceSegmentedButtonRow(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("unit_system_selector")
          ) {
            UnitSystem.entries.forEachIndexed { idx, system ->
              SegmentedButton(
                selected = profile.unitSystem == system,
                onClick = { onUnitSystemChange(system) },
                shape = SegmentedButtonDefaults.itemShape(index = idx, count = UnitSystem.entries.size),
                modifier = Modifier.testTag("unit_${system.code}")
              ) {
                Text(system.label, style = MaterialTheme.typography.labelLarge)
              }
            }
          }

          // Height Control (Metric cm vs Imperial ft/in)
          if (profile.unitSystem == UnitSystem.METRIC) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Height (cm)",
                  style = MaterialTheme.typography.labelLarge,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "${BiometricEngine.formatNum(profile.heightCm, 1)} cm",
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.primary
                )
              }
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Slider(
                  value = profile.heightCm.toFloat().coerceIn(80f, 230f),
                  onValueChange = {
                    val v = (it * 2f).roundToInt() / 2.0
                    cmText = BiometricEngine.formatNum(v, 1).removeSuffix(".0")
                    onHeightCmChange(v)
                  },
                  valueRange = 80f..230f,
                  modifier = Modifier
                    .weight(1f)
                    .testTag("height_cm_slider")
                )
                OutlinedTextField(
                  value = cmText,
                  onValueChange = { text ->
                    cmText = text
                    text.toDoubleOrNull()?.let { onHeightCmChange(it) }
                  },
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  modifier = Modifier
                    .width(96.dp)
                    .testTag("height_cm_input")
                )
              }
            }
          } else {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              OutlinedTextField(
                value = ftText,
                onValueChange = { text ->
                  ftText = text
                  val ftVal = text.toIntOrNull() ?: 0
                  onHeightImperialChange(ftVal, profile.heightIn)
                },
                label = { Text("Height (ft)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                  .weight(1f)
                  .testTag("height_ft_input")
              )
              OutlinedTextField(
                value = inText,
                onValueChange = { text ->
                  inText = text
                  val inVal = text.toDoubleOrNull() ?: 0.0
                  onHeightImperialChange(profile.heightFt, inVal)
                },
                label = { Text("Height (in)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                  .weight(1f)
                  .testTag("height_in_input")
              )
            }
          }

          // Weight & Weight Unit Chips
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Body Weight (${profile.weightUnit.label})",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                WeightUnit.entries.forEach { wu ->
                  FilterChip(
                    selected = profile.weightUnit == wu,
                    onClick = { onWeightUnitChange(wu) },
                    label = { Text(wu.label) },
                    modifier = Modifier.testTag("weight_unit_chip_${wu.code}")
                  )
                }
              }
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              FilledIconButton(
                onClick = {
                  val next = (profile.weightDisplay - 0.5).coerceAtLeast(2.0)
                  weightText = BiometricEngine.formatNum(next, 1).removeSuffix(".0")
                  onWeightChange(next)
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                  containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                modifier = Modifier.size(48.dp)
              ) {
                Icon(Icons.Outlined.Remove, contentDescription = "Decrease weight")
              }

              OutlinedTextField(
                value = weightText,
                onValueChange = { text ->
                  weightText = text
                  text.toDoubleOrNull()?.let { onWeightChange(it) }
                },
                label = { Text("Weight (${profile.weightUnit.label})") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                  .weight(1f)
                  .testTag("weight_input")
              )

              FilledIconButton(
                onClick = {
                  val next = (profile.weightDisplay + 0.5).coerceAtMost(500.0)
                  weightText = BiometricEngine.formatNum(next, 1).removeSuffix(".0")
                  onWeightChange(next)
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                  containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                modifier = Modifier.size(48.dp)
              ) {
                Icon(Icons.Outlined.Add, contentDescription = "Increase weight")
              }
            }
          }

          // Age & Biological Sex Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = ageText,
              onValueChange = { text ->
                ageText = text
                text.toIntOrNull()?.let { onAgeChange(it) }
              },
              label = { Text("Age (yrs)") },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier
                .weight(0.45f)
                .testTag("age_input")
            )

            SingleChoiceSegmentedButtonRow(
              modifier = Modifier
                .weight(0.55f)
                .padding(top = 6.dp)
                .testTag("sex_selector")
            ) {
              BiologicalSex.entries.forEachIndexed { idx, sex ->
                SegmentedButton(
                  selected = profile.sex == sex,
                  onClick = { onSexChange(sex) },
                  shape = SegmentedButtonDefaults.itemShape(index = idx, count = BiologicalSex.entries.size),
                  modifier = Modifier.testTag("sex_${sex.code}")
                ) {
                  Text(sex.label)
                }
              }
            }
          }

          // Asian Population Cutoffs Toggle Card
          OutlinedCard(
            onClick = { onAsianCutoffsToggle(!useAsianCutoffs) },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("asian_cutoffs_card")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Icon(
                  imageVector = Icons.Outlined.Public,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = "Asian-Population WHO Cutoffs",
                    style = MaterialTheme.typography.titleSmall
                  )
                  Text(
                    text = "Screens overweight from BMI 23.0 and obesity from 27.5",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
              Switch(
                checked = useAsianCutoffs,
                onCheckedChange = onAsianCutoffsToggle,
                modifier = Modifier.testTag("asian_cutoffs_switch")
              )
            }
          }
        }
      }
    }

    // 3. Anthropometric Analysis Bento Grid
    if (biometrics.isValid) {
      item {
        ElevatedCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("analysis_bento_card"),
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
                imageVector = Icons.Outlined.Analytics,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Anthropometric Breakdown",
                style = MaterialTheme.typography.titleLarge
              )
            }

            val metrics = buildList {
              add("Healthy Weight Range" to biometrics.healthyWeightRangeFormatted)
              add("To Reach Healthy Range" to biometrics.deltaToHealthyFormatted)
              add("BMI Prime" to BiometricEngine.formatNum(biometrics.bmiPrime, 2))
              add("Ponderal Index" to "${BiometricEngine.formatNum(biometrics.ponderalIndex, 1)} kg/m³")
              add("New BMI (Trefethen)" to BiometricEngine.formatNum(biometrics.trefethenNewBmi, 1))
              add("Body Fat (Deurenberg est.)" to "${BiometricEngine.formatNum(biometrics.deurenbergBodyFatPct, 1)}%")
              if (biometrics.childPercentile != null) {
                add("Child Percentile (est.)" to "${BiometricEngine.formatNum(biometrics.childPercentile, 0)}th")
              }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              metrics.chunked(2).forEach { pair ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  pair.forEach { (label, value) ->
                    MetricTile(
                      label = label,
                      value = value,
                      modifier = Modifier.weight(1f)
                    )
                  }
                  if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                  }
                }
              }
            }
          }
        }
      }

      // 4. Personalized Actionable Wellness Tips
      item {
        ElevatedCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("wellness_tips_card"),
          shape = MaterialTheme.shapes.large,
          colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
          )
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Guidance for You",
                style = MaterialTheme.typography.titleLarge
              )
            }

            biometrics.personalizedTips.forEach { tip ->
              Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = biometrics.category.color,
                    modifier = Modifier.size(20.dp)
                  )
                  Spacer(modifier = Modifier.width(12.dp))
                  Text(
                    text = tip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
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
fun MetricTile(
  label: String,
  value: String,
  subtitle: String? = null,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
    shape = MaterialTheme.shapes.medium
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Text(
        text = value,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      if (subtitle != null) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary
        )
      }
    }
  }
}
