package com.chonkcheck.android.presentation.ui.settings.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.domain.model.ActivityLevel
import com.chonkcheck.android.domain.model.DietPreset
import com.chonkcheck.android.domain.model.HeightUnit
import com.chonkcheck.android.domain.model.MacroTargets
import com.chonkcheck.android.domain.model.Sex
import com.chonkcheck.android.domain.model.WeightGoal
import com.chonkcheck.android.domain.model.WeightUnit
import com.chonkcheck.android.domain.usecase.TdeeResult
import com.chonkcheck.android.presentation.ui.components.ChonkButton
import com.chonkcheck.android.ui.theme.CarbsColor
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen
import com.chonkcheck.android.ui.theme.FatColor
import com.chonkcheck.android.ui.theme.ProteinColor
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BodyGoalsSection(
    weightUnit: WeightUnit,
    heightUnit: HeightUnit,
    heightCm: Double?,
    age: Int?,
    birthDate: LocalDate?,
    sex: Sex?,
    activityLevel: ActivityLevel?,
    currentWeightKg: Double?,
    tdeePreview: TdeeResult?,
    weightGoal: WeightGoal?,
    weeklyGoalKg: Double?,
    dietPreset: DietPreset,
    calories: Int,
    protein: Int,
    carbs: Int,
    fat: Int,
    onHeightChange: (Double?) -> Unit,
    onBirthDateChange: (LocalDate?) -> Unit,
    onSexChange: (Sex?) -> Unit,
    onActivityLevelChange: (ActivityLevel?) -> Unit,
    onCurrentWeightChange: (Double?) -> Unit,
    onWeightGoalChange: (WeightGoal?) -> Unit,
    onWeeklyGoalChange: (Double?) -> Unit,
    onDietPresetChange: (DietPreset) -> Unit,
    onCaloriesChange: (Int) -> Unit,
    onProteinChange: (Int) -> Unit,
    onCarbsChange: (Int) -> Unit,
    onFatChange: (Int) -> Unit,
    onSaveChanges: () -> Unit,
    isSaving: Boolean,
    hasChanges: Boolean,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }

    // Rate options matching the web app
    val rateOptionsKg = listOf(0.25, 0.5, 0.75, 1.0)
    val rateOptionsLb = listOf(0.5, 1.0, 1.5, 2.0)

    val rateOptions = when (weightUnit) {
        WeightUnit.KG -> rateOptionsKg
        WeightUnit.LB, WeightUnit.ST -> rateOptionsLb
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Body Measurements Section
        Text(
            text = "Body Measurements",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Height input
        when (heightUnit) {
            HeightUnit.CM -> {
                OutlinedTextField(
                    value = heightCm?.toInt()?.toString() ?: "",
                    onValueChange = { value ->
                        onHeightChange(value.toDoubleOrNull())
                    },
                    label = { Text("Height") },
                    suffix = { Text("cm") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            HeightUnit.FT -> {
                val (feet, inches) = remember(heightCm) {
                    heightCm?.let {
                        val totalInches = it / 2.54
                        val ft = (totalInches / 12).toInt()
                        val inn = (totalInches % 12).toInt()
                        ft to inn
                    } ?: (null to null)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = feet?.toString() ?: "",
                        onValueChange = { value ->
                            val ft = value.toIntOrNull() ?: 0
                            val inn = inches ?: 0
                            val totalInches = ft * 12 + inn
                            onHeightChange(totalInches * 2.54)
                        },
                        label = { Text("Feet") },
                        suffix = { Text("ft") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = inches?.toString() ?: "",
                        onValueChange = { value ->
                            val inn = value.toIntOrNull() ?: 0
                            val ft = feet ?: 0
                            val totalInches = ft * 12 + inn
                            onHeightChange(totalInches * 2.54)
                        },
                        label = { Text("Inches") },
                        suffix = { Text("in") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Birth Date
        OutlinedTextField(
            value = birthDate?.format(dateFormatter) ?: "",
            onValueChange = {},
            label = { Text("Birth Date") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                TextButton(onClick = { showDatePicker = true }) {
                    Text("Select")
                }
            }
        )

        if (age != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$age years old",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sex
        Text(
            text = "Sex",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Sex.entries.forEach { option ->
                FilterChip(
                    selected = sex == option,
                    onClick = { onSexChange(option) },
                    label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Activity Level
        Text(
            text = "Activity Level",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActivityLevel.entries.forEach { level ->
                FilterChip(
                    selected = activityLevel == level,
                    onClick = { onActivityLevelChange(level) },
                    label = { Text(level.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Current Weight
        when (weightUnit) {
            WeightUnit.KG -> {
                OutlinedTextField(
                    value = currentWeightKg?.let { String.format("%.1f", it) } ?: "",
                    onValueChange = { value ->
                        onCurrentWeightChange(value.toDoubleOrNull())
                    },
                    label = { Text("Current Weight") },
                    suffix = { Text("kg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            WeightUnit.LB -> {
                OutlinedTextField(
                    value = currentWeightKg?.let { String.format("%.1f", it * 2.20462) } ?: "",
                    onValueChange = { value ->
                        value.toDoubleOrNull()?.let { lb ->
                            onCurrentWeightChange(lb / 2.20462)
                        }
                    },
                    label = { Text("Current Weight") },
                    suffix = { Text("lb") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            WeightUnit.ST -> {
                val (stones, pounds) = remember(currentWeightKg) {
                    currentWeightKg?.let {
                        val totalLb = it * 2.20462
                        val st = (totalLb / 14).toInt()
                        val lb = (totalLb % 14).toInt()
                        st to lb
                    } ?: (null to null)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = stones?.toString() ?: "",
                        onValueChange = { value ->
                            val st = value.toIntOrNull() ?: 0
                            val lb = pounds ?: 0
                            val totalLb = st * 14 + lb
                            onCurrentWeightChange(totalLb / 2.20462)
                        },
                        label = { Text("Stones") },
                        suffix = { Text("st") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = pounds?.toString() ?: "",
                        onValueChange = { value ->
                            val lb = value.toIntOrNull() ?: 0
                            val st = stones ?: 0
                            val totalLb = st * 14 + lb
                            onCurrentWeightChange(totalLb / 2.20462)
                        },
                        label = { Text("Pounds") },
                        suffix = { Text("lb") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // TDEE Card
        if (tdeePreview != null) {
            Spacer(modifier = Modifier.height(24.dp))

            TdeeCard(
                tdee = tdeePreview.tdee,
                bmr = tdeePreview.bmr
            )
        }

        // Weight Goals Section
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Weight Goals",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        WeightGoal.entries.forEach { goal ->
            GoalOptionCard(
                goal = goal,
                isSelected = weightGoal == goal,
                onClick = { onWeightGoalChange(goal) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Weekly target rate options
        if (weightGoal != null && weightGoal != WeightGoal.MAINTAIN) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Weekly Target",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            val unitLabel = if (weightUnit == WeightUnit.ST) "lb" else weightUnit.symbol

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                rateOptions.forEach { rate ->
                    // Convert rate to kg for calorie calculation
                    val rateInKg = if (weightUnit == WeightUnit.KG) rate else rate / 2.20462

                    // Calculate target calories (7700 cal per kg)
                    val calorieChange = (rateInKg * 7700 / 7).toInt()
                    val targetCalories = if (weightGoal == WeightGoal.LOSE) {
                        (tdeePreview?.tdee ?: 2000) - calorieChange
                    } else {
                        (tdeePreview?.tdee ?: 2000) + calorieChange
                    }

                    val isTooLow = targetCalories < 1200
                    val isSelected = weeklyGoalKg?.let {
                        kotlin.math.abs(it - rateInKg) < 0.01
                    } ?: false

                    RateOptionButton(
                        rate = rate,
                        unitLabel = unitLabel,
                        targetCalories = targetCalories,
                        isSelected = isSelected,
                        isTooLow = isTooLow,
                        onClick = {
                            if (!isTooLow) {
                                onWeeklyGoalChange(rateInKg)
                            }
                        }
                    )
                }
            }
        }

        // Diet Preset Section
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "How You Eat",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DietPreset.entries.filter { it != DietPreset.CUSTOM }.forEach { preset ->
                FilterChip(
                    selected = dietPreset == preset,
                    onClick = { onDietPresetChange(preset) },
                    label = { Text(preset.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // Daily Macro Goals Section
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Daily Macro Goals",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = calories.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { onCaloriesChange(it) }
                },
                label = { Text("Calories") },
                suffix = { Text("cal") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = protein.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { onProteinChange(it) }
                },
                label = { Text("Protein") },
                suffix = { Text("g") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = carbs.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { onCarbsChange(it) }
                },
                label = { Text("Carbs") },
                suffix = { Text("g") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = fat.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { onFatChange(it) }
                },
                label = { Text("Fat") },
                suffix = { Text("g") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        // Save Button
        Spacer(modifier = Modifier.height(24.dp))

        ChonkButton(
            text = "Save Changes",
            onClick = onSaveChanges,
            enabled = hasChanges && !isSaving,
            isLoading = isSaving,
            modifier = Modifier.fillMaxWidth()
        )
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = birthDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            onBirthDateChange(date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
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
}

@Composable
private fun TdeeCard(
    tdee: Int,
    bmr: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ChonkGreen),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Your Daily Calorie Burn (TDEE)",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$tdee",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "calories per day",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "BMR: $bmr cal",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun GoalOptionCard(
    goal: WeightGoal,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when (goal) {
        WeightGoal.LOSE -> Icons.AutoMirrored.Filled.TrendingDown
        WeightGoal.MAINTAIN -> Icons.AutoMirrored.Filled.TrendingFlat
        WeightGoal.GAIN -> Icons.AutoMirrored.Filled.TrendingUp
    }

    val description = when (goal) {
        WeightGoal.LOSE -> "Burn more than you eat"
        WeightGoal.MAINTAIN -> "Keep your current weight"
        WeightGoal.GAIN -> "Build muscle and strength"
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun RateOptionButton(
    rate: Double,
    unitLabel: String,
    targetCalories: Int,
    isSelected: Boolean,
    isTooLow: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rateDisplay = if (rate == rate.toInt().toDouble()) {
        "${rate.toInt()}"
    } else {
        String.format("%.1f", rate).trimEnd('0').trimEnd('.')
    }

    Card(
        onClick = onClick,
        enabled = !isTooLow,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isSelected) "●" else "○",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = "$rateDisplay $unitLabel/week",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isTooLow) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    } else if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${targetCalories.toString().chunked(3).joinToString(",")} cal/day",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isTooLow) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                if (isTooLow) {
                    Text(
                        text = "⚠️",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BodyGoalsSectionPreview() {
    ChonkCheckTheme {
        Surface {
            BodyGoalsSection(
                weightUnit = WeightUnit.KG,
                heightUnit = HeightUnit.CM,
                heightCm = 175.0,
                age = 34,
                birthDate = LocalDate.of(1990, 5, 15),
                sex = Sex.MALE,
                activityLevel = ActivityLevel.MODERATELY_ACTIVE,
                currentWeightKg = 80.0,
                tdeePreview = TdeeResult(bmr = 1800, tdee = 2400, maintenanceCalories = 2400),
                weightGoal = WeightGoal.LOSE,
                weeklyGoalKg = 0.5,
                dietPreset = DietPreset.BALANCED,
                calories = 1900,
                protein = 143,
                carbs = 190,
                fat = 63,
                onHeightChange = {},
                onBirthDateChange = {},
                onSexChange = {},
                onActivityLevelChange = {},
                onCurrentWeightChange = {},
                onWeightGoalChange = {},
                onWeeklyGoalChange = {},
                onDietPresetChange = {},
                onCaloriesChange = {},
                onProteinChange = {},
                onCarbsChange = {},
                onFatChange = {},
                onSaveChanges = {},
                isSaving = false,
                hasChanges = true,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
