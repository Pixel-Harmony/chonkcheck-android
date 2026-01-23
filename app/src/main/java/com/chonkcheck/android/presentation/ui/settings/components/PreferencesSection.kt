package com.chonkcheck.android.presentation.ui.settings.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.domain.model.HeightUnit
import com.chonkcheck.android.domain.model.ThemePreference
import com.chonkcheck.android.domain.model.WeightUnit
import com.chonkcheck.android.ui.theme.ChonkCheckTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesSection(
    weightUnit: WeightUnit,
    heightUnit: HeightUnit,
    themePreference: ThemePreference,
    onWeightUnitChange: (WeightUnit) -> Unit,
    onHeightUnitChange: (HeightUnit) -> Unit,
    onThemeChange: (ThemePreference) -> Unit,
    modifier: Modifier = Modifier
) {
    var weightExpanded by remember { mutableStateOf(false) }
    var heightExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Weight Unit
        Text(
            text = "Weight Unit",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = weightExpanded,
            onExpandedChange = { weightExpanded = it }
        ) {
            OutlinedTextField(
                value = weightUnit.displayName(),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = weightExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = weightExpanded,
                onDismissRequest = { weightExpanded = false }
            ) {
                WeightUnit.entries.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit.displayName()) },
                        onClick = {
                            onWeightUnitChange(unit)
                            weightExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Height Unit
        Text(
            text = "Height Unit",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = heightExpanded,
            onExpandedChange = { heightExpanded = it }
        ) {
            OutlinedTextField(
                value = heightUnit.displayName(),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = heightExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = heightExpanded,
                onDismissRequest = { heightExpanded = false }
            ) {
                HeightUnit.entries.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit.displayName()) },
                        onClick = {
                            onHeightUnitChange(unit)
                            heightExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Theme
        Text(
            text = "Theme",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemePreference.entries.forEach { theme ->
                FilterChip(
                    selected = themePreference == theme,
                    onClick = { onThemeChange(theme) },
                    label = { Text(theme.displayName()) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

private fun WeightUnit.displayName(): String = when (this) {
    WeightUnit.KG -> "Kilograms (kg)"
    WeightUnit.LB -> "Pounds (lb)"
    WeightUnit.ST -> "Stone (st)"
}

private fun HeightUnit.displayName(): String = when (this) {
    HeightUnit.CM -> "Centimeters (cm)"
    HeightUnit.FT -> "Feet & Inches (ft/in)"
}

private fun ThemePreference.displayName(): String = when (this) {
    ThemePreference.SYSTEM -> "System"
    ThemePreference.LIGHT -> "Light"
    ThemePreference.DARK -> "Dark"
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreferencesSectionPreview() {
    ChonkCheckTheme {
        Surface {
            PreferencesSection(
                weightUnit = WeightUnit.KG,
                heightUnit = HeightUnit.CM,
                themePreference = ThemePreference.SYSTEM,
                onWeightUnitChange = {},
                onHeightUnitChange = {},
                onThemeChange = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
