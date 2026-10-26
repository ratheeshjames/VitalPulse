package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.BiometricAnalysis
import com.example.domain.BiometricEngine
import com.example.ui.theme.BmiSpectrumColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BiometricGaugeCard(
  biometrics: BiometricAnalysis,
  onShareClick: () -> Unit,
  onPrintClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val cdGauge = stringResource(R.string.cd_bmi_gauge)

  ElevatedCard(
    modifier = modifier
      .fillMaxWidth()
      .testTag("bmi_gauge_hero_card"),
    shape = MaterialTheme.shapes.extraLarge,
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
  ) {
    if (!biometrics.isValid) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Surface(
          color = MaterialTheme.colorScheme.errorContainer,
          shape = MaterialTheme.shapes.large
        ) {
          Text(
            text = biometrics.validationError ?: stringResource(R.string.validation_error_bounds),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier
              .padding(16.dp)
              .testTag("validation_error_text")
          )
        }
      }
      return@ElevatedCard
    }

    val clampedBmi = biometrics.bmi.coerceIn(10.0, 40.0).toFloat()
    val animatedBmi by animateFloatAsState(
      targetValue = clampedBmi,
      animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
      ),
      label = "bmi_needle_anim"
    )

    val limits = listOf(10.0) + biometrics.cutoffs + listOf(40.0)
    val needleColor = MaterialTheme.colorScheme.onSurface

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 22.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Custom Radial Segmented Arc Gauge + Center Readout
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(190.dp),
        contentAlignment = Alignment.BottomCenter
      ) {
        Canvas(
          modifier = Modifier
            .size(width = 280.dp, height = 175.dp)
            .align(Alignment.TopCenter)
            .semantics { contentDescription = cdGauge }
            .testTag("bmi_radial_canvas")
        ) {
          val strokeWidthPx = 18.dp.toPx()
          val diameter = size.width - strokeWidthPx * 1.6f
          val topLeft = Offset((size.width - diameter) / 2f, strokeWidthPx)
          val arcSize = Size(diameter, diameter)
          val center = Offset(size.width / 2f, strokeWidthPx + diameter / 2f)
          val radius = diameter / 2f

          // Draw each BMI category segment across 180 degrees (start at 180 deg, total 180 deg)
          for (i in 0 until 8) {
            val startVal = limits[i].toFloat()
            val endVal = limits[i + 1].toFloat()
            val startAngle = 180f + ((startVal - 10f) / 30f) * 180f
            val sweepAngle = ((endVal - startVal) / 30f) * 180f
            drawArc(
              color = BmiSpectrumColors.all[i],
              startAngle = startAngle + 1.2f,
              sweepAngle = (sweepAngle - 2.4f).coerceAtLeast(1f),
              useCenter = false,
              topLeft = topLeft,
              size = arcSize,
              style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
            )
          }

          // Needle angle in radians (180 degrees at BMI 10 -> 360 degrees at BMI 40)
          val fraction = (animatedBmi - 10f) / 30f
          val angleDeg = 180.0 + fraction * 180.0
          val angleRad = Math.toRadians(angleDeg)

          // Indicator dot on the arc + needle line from pivot
          val indicatorRadius = radius - strokeWidthPx * 0.85f
          val tipX = center.x + (indicatorRadius * cos(angleRad)).toFloat()
          val tipY = center.y + (indicatorRadius * sin(angleRad)).toFloat()

          val innerBaseRadius = radius * 0.42f
          val baseX = center.x + (innerBaseRadius * cos(angleRad)).toFloat()
          val baseY = center.y + (innerBaseRadius * sin(angleRad)).toFloat()

          drawLine(
            color = needleColor,
            start = Offset(baseX, baseY),
            end = Offset(tipX, tipY),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
          )

          drawCircle(
            color = biometrics.category.color,
            radius = 7.dp.toPx(),
            center = Offset(tipX, tipY)
          )
          drawCircle(
            color = needleColor,
            radius = 3.dp.toPx(),
            center = Offset(tipX, tipY)
          )
        }

        // Center Score & Category Badge
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(bottom = 2.dp)
        ) {
          Text(
            text = BiometricEngine.formatNum(biometrics.bmi, 1),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag("bmi_score_text")
          )
          Surface(
            color = biometrics.category.color.copy(alpha = 0.16f),
            shape = CircleShape
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(CircleShape)
                  .background(biometrics.category.color)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = biometrics.category.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = biometrics.category.color,
                modifier = Modifier.testTag("bmi_category_badge")
              )
            }
          }
        }
      }

      if (biometrics.contextualNote.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = biometrics.contextualNote,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .testTag("bmi_contextual_note")
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Proportional Spectrum Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(12.dp)
          .clip(RoundedCornerShape(6.dp))
      ) {
        for (i in 0 until 8) {
          val span = (limits[i + 1] - limits[i]).toFloat().coerceAtLeast(0.5f)
          Box(
            modifier = Modifier
              .weight(span)
              .height(12.dp)
              .background(BmiSpectrumColors.all[i])
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Quick Share & Print Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
      ) {
        FilledTonalButton(
          onClick = onShareClick,
          modifier = Modifier
            .weight(1f)
            .testTag("share_result_button")
        ) {
          Icon(
            imageVector = Icons.Outlined.Share,
            contentDescription = stringResource(R.string.cd_share_report),
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("Share Report")
        }

        OutlinedButton(
          onClick = onPrintClick,
          modifier = Modifier
            .weight(1f)
            .testTag("print_report_button")
        ) {
          Icon(
            imageVector = Icons.Outlined.Print,
            contentDescription = stringResource(R.string.cd_print_report),
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("Print / PDF")
        }
      }
    }
  }
}
