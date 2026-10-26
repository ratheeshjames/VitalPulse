package com.example.ui.components

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.AppSettingsState
import com.example.domain.CategoryThresholdRow
import com.example.domain.ProfileData
import com.example.ui.theme.AccentPreset
import com.example.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileManagerSheet(
  profiles: List<ProfileData>,
  activeProfileName: String,
  onSelectProfile: (String) -> Unit,
  onCreateProfile: (String) -> Unit,
  onDeleteActiveProfile: () -> Unit,
  onDismiss: () -> Unit
) {
  var newProfileText by remember { mutableStateOf("") }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    modifier = Modifier.testTag("profile_manager_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 28.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Outlined.PersonOutline,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Profiles Studio",
          style = MaterialTheme.typography.headlineSmall
        )
      }

      Text(
        text = "Maintain independent measurements, goals, and weight logs for multiple people.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        profiles.forEach { p ->
          val selected = p.name == activeProfileName
          FilterChip(
            selected = selected,
            onClick = { onSelectProfile(p.name) },
            label = { Text(p.name) },
            leadingIcon = if (selected) {
              { Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
            } else null,
            modifier = Modifier.testTag("profile_chip_${p.name}")
          )
        }
      }

      HorizontalDivider()

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = newProfileText,
          onValueChange = { newProfileText = it.take(20) },
          label = { Text("New profile name") },
          singleLine = true,
          modifier = Modifier
            .weight(1f)
            .testTag("new_profile_name_input")
        )
        Button(
          onClick = {
            onCreateProfile(newProfileText)
            newProfileText = ""
          },
          modifier = Modifier.testTag("add_profile_button")
        ) {
          Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Add")
        }
      }

      if (profiles.size > 1) {
        Button(
          onClick = onDeleteActiveProfile,
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("delete_profile_button")
        ) {
          Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Delete Profile \"$activeProfileName\"")
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsSheet(
  settings: AppSettingsState,
  onThemeModeChange: (ThemeMode) -> Unit,
  onAccentPresetChange: (AccentPreset) -> Unit,
  onDynamicColorToggle: (Boolean) -> Unit,
  onAsianCutoffsToggle: (Boolean) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    modifier = Modifier.testTag("appearance_settings_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 28.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Outlined.Palette,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Theme & Preferences",
          style = MaterialTheme.typography.headlineSmall
        )
      }

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Appearance Mode",
          style = MaterialTheme.typography.titleSmall
        )
        SingleChoiceSegmentedButtonRow(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("theme_mode_selector")
        ) {
          ThemeMode.entries.forEachIndexed { idx, mode ->
            SegmentedButton(
              selected = settings.themeMode == mode,
              onClick = { onThemeModeChange(mode) },
              shape = SegmentedButtonDefaults.itemShape(index = idx, count = ThemeMode.entries.size),
              modifier = Modifier.testTag("theme_mode_${mode.name}")
            ) {
              Text(mode.label)
            }
          }
        }
      }

      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Color Accent Palette",
          style = MaterialTheme.typography.titleSmall
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          AccentPreset.entries.forEach { preset ->
            val selected = !settings.useDynamicColor && settings.accentPreset == preset
            OutlinedCard(
              onClick = { onAccentPresetChange(preset) },
              modifier = Modifier
                .weight(1f)
                .testTag("accent_preset_${preset.name}")
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(preset.previewHex)
                    .then(
                      if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                      else Modifier
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  if (selected) {
                    Icon(
                      imageVector = Icons.Outlined.Check,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.surface,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
                Text(
                  text = preset.label.substringBefore(" "),
                  style = MaterialTheme.typography.labelSmall
                )
              }
            }
          }
        }
      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Surface(
          color = MaterialTheme.colorScheme.surfaceContainerHigh,
          shape = MaterialTheme.shapes.medium,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Material You Dynamic Wallpaper Color", style = MaterialTheme.typography.titleSmall)
              Text(
                "Adapt palette to Android 12+ system wallpaper",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = settings.useDynamicColor,
              onCheckedChange = onDynamicColorToggle
            )
          }
        }
      }

      Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Asian-Population Cutoffs", style = MaterialTheme.typography.titleSmall)
            Text(
              "Use WHO Asian thresholds (overweight from 23.0)",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Switch(
            checked = settings.useAsianCutoffs,
            onCheckedChange = onAsianCutoffsToggle
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferenceGuideSheet(
  referenceRows: List<CategoryThresholdRow>,
  useAsianCutoffs: Boolean,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val faqs = listOf(
    "Is BMI accurate for athletes?" to "Not always. Muscle tissue is denser than adipose tissue, so muscular athletes can score in the overweight range despite low body fat. Pair BMI with Waist-to-Height Ratio.",
    "Why are Asian cutoffs different?" to "At the same BMI, many Asian populations carry a higher proportion of visceral body fat and cardiometabolic risk, so the WHO suggests lower screening thresholds (overweight at 23.0, obesity at 27.5).",
    "How are children & teens measured?" to "For ages 2–19, BMI is evaluated against age- and sex-specific growth percentiles rather than fixed adult cutoffs. VitalPulse computes a log-normal percentile estimate.",
    "Where is my data stored?" to "Exclusively on your device in an encrypted-capable local database. Nothing is uploaded to external servers. Use the CSV/JSON backup tools in the Journal tab to move your data."
  )

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    modifier = Modifier.testTag("reference_guide_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Outlined.Info,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "BMI Reference & Clinical Guide",
          style = MaterialTheme.typography.headlineSmall
        )
      }

      Text(
        text = "BMI is weight in kilograms divided by height in meters squared (kg/m²). It screens for weight categories across populations but does not directly measure adipose distribution.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      // BMI Categories Table
      Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = if (useAsianCutoffs) "WHO Asian-Population Cutoffs" else "WHO Standard Adult Cutoffs",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
          )
          referenceRows.forEachIndexed { idx, row ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(row.category.color)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = row.category.label,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Medium
                )
              }
              Text(
                text = row.rangeText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            if (idx < referenceRows.lastIndex) {
              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
          }
        }
      }

      Text(
        text = "Frequently Asked Questions",
        style = MaterialTheme.typography.titleMedium
      )

      faqs.forEach { (q, a) ->
        var expanded by remember { mutableStateOf(false) }
        Surface(
          color = MaterialTheme.colorScheme.surfaceContainerLow,
          shape = MaterialTheme.shapes.medium,
          modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable { expanded = !expanded }
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = q,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
              )
              Icon(
                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand"
              )
            }
            AnimatedVisibility(visible = expanded) {
              Text(
                text = a,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
              )
            }
          }
        }
      }

      Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = stringResource(R.string.medical_disclaimer),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSecondaryContainer,
          modifier = Modifier.padding(12.dp)
        )
      }
    }
  }
}
