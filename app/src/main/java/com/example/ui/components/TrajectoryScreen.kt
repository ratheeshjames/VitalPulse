package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.BiometricEngine
import com.example.domain.GoalProjection
import com.example.domain.ProfileData
import com.example.domain.WhatIfSimulation

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrajectoryScreen(
  profile: ProfileData,
  goalProjection: GoalProjection?,
  whatIfWeightKg: Float,
  whatIfSimulation: WhatIfSimulation?,
  onGoalWeightChange: (Double) -> Unit,
  onWeeklyRateChange: (Double) -> Unit,
  onLockStartWeight: () -> Unit,
  onWhatIfSliderChange: (Float) -> Unit,
  modifier: Modifier = Modifier
) {
  var goalWeightText by remember(profile.name, profile.weightUnit, profile.goalWeightDisplay) {
    mutableStateOf(BiometricEngine.formatNum(profile.goalWeightDisplay, 1).removeSuffix(".0"))
  }

  val rateOptions = listOf(0.25, 0.5, 0.75, 1.0)

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("trajectory_screen_list"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Target Weight & Weekly Pace Card
    item {
      ElevatedCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("goal_planner_card"),
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
              imageVector = Icons.Outlined.Flag,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Goal Trajectory Planner",
              style = MaterialTheme.typography.titleLarge
            )
          }

          OutlinedTextField(
            value = goalWeightText,
            onValueChange = { text ->
              goalWeightText = text
              text.toDoubleOrNull()?.let { onGoalWeightChange(it) }
            },
            label = { Text("Target Goal Weight (${profile.weightUnit.label})") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("goal_weight_input")
          )

          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "Target Weekly Pace",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              rateOptions.forEach { rate ->
                FilterChip(
                  selected = profile.weeklyRateKg == rate,
                  onClick = { onWeeklyRateChange(rate) },
                  label = { Text("$rate kg / wk") },
                  modifier = Modifier.testTag("rate_chip_$rate")
                )
              }
            }
          }

          FilledTonalButton(
            onClick = onLockStartWeight,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("set_start_weight_button")
          ) {
            Icon(
              imageVector = Icons.Outlined.LockClock,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Set Current Weight as Start Baseline")
          }

          if (goalProjection != null) {
            val animatedProgress by animateFloatAsState(
              targetValue = (goalProjection.progressPercent / 100f).coerceIn(0f, 1f),
              label = "goal_progress_anim"
            )

            Surface(
              color = MaterialTheme.colorScheme.surfaceContainerHigh,
              shape = MaterialTheme.shapes.medium,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = goalProjection.progressSummaryText,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.testTag("goal_progress_text")
                  )
                }
                LinearProgressIndicator(
                  progress = { animatedProgress },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .testTag("goal_progress_bar")
                )
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              MetricTile(
                label = goalProjection.directionLabel,
                value = goalProjection.differenceFormatted,
                modifier = Modifier.weight(1f)
              )
              MetricTile(
                label = "Time at This Rate",
                value = goalProjection.weeksFormatted,
                modifier = Modifier.weight(1f)
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              MetricTile(
                label = "Projected Date",
                value = goalProjection.projectedDateFormatted,
                modifier = Modifier.weight(1f)
              )
              MetricTile(
                label = "BMI at Goal",
                value = BiometricEngine.formatNum(goalProjection.bmiAtGoal, 1),
                subtitle = goalProjection.categoryAtGoal.label,
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }
    }

    // 2. Interactive "What-If" Weight Explorer Card
    if (whatIfSimulation != null) {
      item {
        ElevatedCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("what_if_simulator_card"),
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
                imageVector = Icons.Outlined.Tune,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "What-If Weight Simulator",
                style = MaterialTheme.typography.titleLarge
              )
            }

            Text(
              text = "Scrub the slider to preview how hypothetical body weights affect your BMI and WHO classification.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Try a weight:",
                style = MaterialTheme.typography.titleSmall
              )
              Text(
                text = whatIfSimulation.formattedWeight,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("what_if_weight_value")
              )
            }

            Slider(
              value = whatIfWeightKg,
              onValueChange = onWhatIfSliderChange,
              valueRange = 30f..200f,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("what_if_slider")
            )

            Surface(
              color = whatIfSimulation.category.color.copy(alpha = 0.14f),
              shape = MaterialTheme.shapes.medium,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(12.dp)
                      .clip(CircleShape)
                      .background(whatIfSimulation.category.color)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = "BMI ${BiometricEngine.formatNum(whatIfSimulation.bmi, 1)} · ${whatIfSimulation.category.label}",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = whatIfSimulation.category.color,
                      modifier = Modifier.testTag("what_if_result_text")
                    )
                    Text(
                      text = whatIfSimulation.deltaFromCurrentFormatted,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
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
}
