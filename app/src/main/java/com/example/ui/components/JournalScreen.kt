package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
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
import com.example.domain.BiometricEngine
import com.example.domain.TrendSummary
import com.example.domain.WeightEntry
import com.example.domain.WeightUnit
import com.example.ui.TrendMetricMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JournalScreen(
  weightUnit: WeightUnit,
  trendMetricMode: TrendMetricMode,
  trendSummary: TrendSummary,
  editingEntry: WeightEntry?,
  entryDateInput: String,
  entryNoteInput: String,
  dataPortableBoxText: String,
  onDateChange: (String) -> Unit,
  onNoteChange: (String) -> Unit,
  onSaveEntry: () -> Unit,
  onCancelEdit: () -> Unit,
  onEditEntry: (WeightEntry) -> Unit,
  onDeleteEntry: (Long) -> Unit,
  onTrendMetricModeChange: (TrendMetricMode) -> Unit,
  onExportCsv: () -> Unit,
  onExportJson: () -> Unit,
  onRestoreBackup: () -> Unit,
  onCopyBox: () -> Unit,
  onDataBoxChange: (String) -> Unit,
  onClearAllData: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showDatePicker by remember { mutableStateOf(false) }
  var showClearConfirmDialog by remember { mutableStateOf(false) }

  if (showDatePicker) {
    val datePickerState = rememberDatePickerState(
      initialSelectedDateMillis = System.currentTimeMillis()
    )
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let { millis ->
              val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
              }
              onDateChange(sdf.format(Date(millis)))
            }
            showDatePicker = false
          }
        ) {
          Text("Select")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDatePicker = false }) {
          Text("Cancel")
        }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }

  if (showClearConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showClearConfirmDialog = false },
      icon = { Icon(Icons.Outlined.WarningAmber, contentDescription = null) },
      title = { Text("Clear All Profiles & History?") },
      text = {
        Text("This permanently removes all saved weight logs and custom profiles on this device. Export a JSON backup first if you want to keep your history.")
      },
      confirmButton = {
        Button(
          onClick = {
            showClearConfirmDialog = false
            onClearAllData()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
          ),
          modifier = Modifier.testTag("confirm_clear_all_button")
        ) {
          Text("Delete Everything")
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirmDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("journal_screen_list"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Log / Edit Entry Card
    item {
      ElevatedCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("save_entry_card"),
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
              imageVector = Icons.Outlined.Save,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (editingEntry != null) "Edit Logged Entry" else "Log Current Weight & BMI",
              style = MaterialTheme.typography.titleLarge
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = entryDateInput,
              onValueChange = onDateChange,
              label = { Text("Date (YYYY-MM-DD)") },
              singleLine = true,
              trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                  Icon(Icons.Outlined.CalendarToday, contentDescription = "Pick date from calendar")
                }
              },
              modifier = Modifier
                .weight(1f)
                .testTag("entry_date_input")
            )
          }

          OutlinedTextField(
            value = entryNoteInput,
            onValueChange = onNoteChange,
            label = { Text("Note (optional, max 80 chars)") },
            placeholder = { Text("Morning weigh-in, post-workout, etc.") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("entry_note_input")
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = onSaveEntry,
              modifier = Modifier
                .weight(1f)
                .testTag("save_entry_button")
            ) {
              Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(if (editingEntry != null) "Update Entry" else "Save Entry")
            }

            if (editingEntry != null) {
              OutlinedButton(
                onClick = onCancelEdit,
                modifier = Modifier.testTag("cancel_edit_button")
              ) {
                Text("Cancel Edit")
              }
            }
          }
        }
      }
    }

    // 2. Interactive Trend Chart & Summary Stats Card
    item {
      ElevatedCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("trend_chart_card"),
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
                imageVector = Icons.Outlined.ShowChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Biometric Trend",
                style = MaterialTheme.typography.titleLarge
              )
            }
          }

          SingleChoiceSegmentedButtonRow(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("trend_metric_selector")
          ) {
            TrendMetricMode.entries.forEachIndexed { idx, mode ->
              SegmentedButton(
                selected = trendMetricMode == mode,
                onClick = { onTrendMetricModeChange(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = idx, count = TrendMetricMode.entries.size),
                modifier = Modifier.testTag("trend_mode_${mode.name}")
              ) {
                Text(mode.label)
              }
            }
          }

          val entries = trendSummary.entriesChronological
          val values = if (trendMetricMode == TrendMetricMode.BMI) {
            entries.map { it.bmi.toFloat() }
          } else {
            entries.map { BiometricEngine.fromKg(it.weightKg, weightUnit).toFloat() }
          }

          TrendLineChart(
            values = values,
            unitSuffix = if (trendMetricMode == TrendMetricMode.BMI) "" else " ${weightUnit.code}"
          )

          if (entries.isNotEmpty() &&
            trendSummary.lowestBmi != null &&
            trendSummary.highestBmi != null &&
            trendSummary.averageBmi != null &&
            trendSummary.totalWeightChangeFormatted != null
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              MetricTile(
                label = "Lowest BMI",
                value = BiometricEngine.formatNum(trendSummary.lowestBmi, 1),
                modifier = Modifier.weight(1f)
              )
              MetricTile(
                label = "Highest BMI",
                value = BiometricEngine.formatNum(trendSummary.highestBmi, 1),
                modifier = Modifier.weight(1f)
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              MetricTile(
                label = "Average BMI",
                value = BiometricEngine.formatNum(trendSummary.averageBmi, 1),
                modifier = Modifier.weight(1f)
              )
              MetricTile(
                label = "Total Change",
                value = trendSummary.totalWeightChangeFormatted,
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }
    }

    // 3. History Entries Header & Cards
    item {
      ElevatedCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("history_card"),
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
              imageVector = Icons.Outlined.History,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Logged History (${trendSummary.entriesReverseChronological.size})",
              style = MaterialTheme.typography.titleLarge
            )
          }

          if (trendSummary.entriesReverseChronological.isEmpty()) {
            Surface(
              color = MaterialTheme.colorScheme.surfaceContainerHigh,
              shape = MaterialTheme.shapes.medium,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "No entries yet. Save your first weigh-in above to track your progress.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                  .padding(16.dp)
                  .testTag("empty_history_text")
              )
            }
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              trendSummary.entriesReverseChronological.forEach { item ->
                Surface(
                  color = MaterialTheme.colorScheme.surfaceContainerHigh,
                  shape = MaterialTheme.shapes.medium,
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_entry_${item.id}")
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                      ) {
                        Text(
                          text = item.dateIso,
                          style = MaterialTheme.typography.labelLarge,
                          color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                          text = BiometricEngine.formatWeight(item.weightKg, weightUnit),
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight = FontWeight.Bold
                        )
                        Text(
                          text = "BMI ${BiometricEngine.formatNum(item.bmi, 1)}",
                          style = MaterialTheme.typography.labelMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                      if (item.note.isNotBlank()) {
                        Text(
                          text = item.note,
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                    }

                    Row {
                      IconButton(
                        onClick = { onEditEntry(item) },
                        modifier = Modifier.testTag("edit_entry_${item.id}")
                      ) {
                        Icon(
                          imageVector = Icons.Outlined.Edit,
                          contentDescription = "Edit entry ${item.dateIso}"
                        )
                      }
                      IconButton(
                        onClick = { onDeleteEntry(item.id) },
                        modifier = Modifier.testTag("delete_entry_${item.id}")
                      ) {
                        Icon(
                          imageVector = Icons.Outlined.DeleteOutline,
                          contentDescription = "Delete entry ${item.dateIso}",
                          tint = MaterialTheme.colorScheme.error
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

    // 4. Data Backup, Export & Portability Card
    item {
      ElevatedCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("data_portability_card"),
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
              imageVector = Icons.Outlined.Download,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Data Backup & Portability",
              style = MaterialTheme.typography.titleLarge
            )
          }

          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilledTonalButton(
              onClick = onExportCsv,
              modifier = Modifier.testTag("export_csv_button")
            ) {
              Text("Export CSV")
            }
            FilledTonalButton(
              onClick = onExportJson,
              modifier = Modifier.testTag("export_json_button")
            ) {
              Text("Export JSON")
            }
            OutlinedButton(
              onClick = onRestoreBackup,
              modifier = Modifier.testTag("restore_backup_button")
            ) {
              Icon(Icons.Outlined.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Restore JSON")
            }
          }

          OutlinedTextField(
            value = dataPortableBoxText,
            onValueChange = onDataBoxChange,
            label = { Text("Backup / Export Box") },
            placeholder = { Text("Exports appear here. Paste a JSON backup and tap Restore JSON.") },
            minLines = 3,
            maxLines = 6,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("backup_box_input")
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            OutlinedButton(
              onClick = onCopyBox,
              modifier = Modifier.testTag("copy_box_button")
            ) {
              Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Copy Box")
            }

            TextButton(
              onClick = { showClearConfirmDialog = true },
              colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.error
              ),
              modifier = Modifier.testTag("clear_all_data_button")
            ) {
              Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Clear All Data")
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TrendLineChart(
  values: List<Float>,
  unitSuffix: String
) {
  val cdChart = stringResource(R.string.cd_trend_chart)
  val lineColor = MaterialTheme.colorScheme.primary

  Surface(
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
    shape = MaterialTheme.shapes.medium,
    modifier = Modifier.fillMaxWidth()
  ) {
    if (values.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(150.dp)
          .padding(16.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Save 1+ entries to visualize your trend curve",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )
      }
    } else {
      val minVal = values.minOrNull() ?: 0f
      val maxVal = values.maxOrNull() ?: 1f
      val range = (maxVal - minVal).takeIf { it > 0.01f } ?: 1f

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "High: ${BiometricEngine.formatNum(maxVal.toDouble(), 1)}$unitSuffix",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Low: ${BiometricEngine.formatNum(minVal.toDouble(), 1)}$unitSuffix",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Canvas(
          modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .semantics { contentDescription = cdChart }
            .testTag("trend_chart_canvas")
        ) {
          val padH = 14.dp.toPx()
          val padV = 12.dp.toPx()
          val usableW = (size.width - padH * 2f).coerceAtLeast(1f)
          val usableH = (size.height - padV * 2f).coerceAtLeast(1f)

          val points = values.mapIndexed { idx, v ->
            val x = if (values.size > 1) {
              padH + (idx.toFloat() / (values.size - 1)) * usableW
            } else {
              size.width / 2f
            }
            val y = padV + usableH - ((v - minVal) / range) * usableH
            Offset(x, y)
          }

          if (points.size > 1) {
            val strokePath = Path().apply {
              moveTo(points.first().x, points.first().y)
              for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
              }
            }
            val fillPath = Path().apply {
              addPath(strokePath)
              lineTo(points.last().x, size.height)
              lineTo(points.first().x, size.height)
              close()
            }
            drawPath(
              path = fillPath,
              brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.28f), lineColor.copy(alpha = 0.0f))
              )
            )
            drawPath(
              path = strokePath,
              color = lineColor,
              style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
          }

          points.forEach { pt ->
            drawCircle(
              color = lineColor,
              radius = 5.dp.toPx(),
              center = pt
            )
          }
        }
      }
    }
  }
}
