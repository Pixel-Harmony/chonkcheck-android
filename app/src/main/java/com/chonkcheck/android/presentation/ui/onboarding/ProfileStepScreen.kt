package com.chonkcheck.android.presentation.ui.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.domain.model.ActivityLevel
import com.chonkcheck.android.domain.model.HeightUnit
import com.chonkcheck.android.domain.model.Sex
import com.chonkcheck.android.domain.model.WeightUnit
import com.chonkcheck.android.presentation.ui.components.ChonkButton
import com.chonkcheck.android.presentation.ui.components.ChonkOutlinedButton
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileStepScreen(
    weightUnit: WeightUnit,
    heightUnit: HeightUnit,
    heightCm: Double?,
    currentWeightKg: Double?,
    birthDate: LocalDate?,
    sex: Sex?,
    activityLevel: ActivityLevel?,
    onHeightChange: (Double) -> Unit,
    onWeightChange: (Double) -> Unit,
    onBirthDateChange: (LocalDate) -> Unit,
    onSexChange: (Sex) -> Unit,
    onActivityLevelChange: (ActivityLevel) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    canContinue: Boolean,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "About You",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "We use this to work out your daily calorie needs",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        when (heightUnit) {
            HeightUnit.CM -> {
                OutlinedTextField(
                    value = heightCm?.toInt()?.toString() ?: "",
                    onValueChange = { value ->
                        value.toDoubleOrNull()?.let { onHeightChange(it) }
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

        Spacer(modifier = Modifier.height(16.dp))

        when (weightUnit) {
            WeightUnit.KG -> {
                OutlinedTextField(
                    value = currentWeightKg?.let { String.format("%.1f", it) } ?: "",
                    onValueChange = { value ->
                        value.toDoubleOrNull()?.let { onWeightChange(it) }
                    },
                    label = { Text("Weight") },
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
                            onWeightChange(lb / 2.20462)
                        }
                    },
                    label = { Text("Weight") },
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
                            onWeightChange(totalLb / 2.20462)
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
                            onWeightChange(totalLb / 2.20462)
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

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Sex",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

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

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Activity Level",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

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

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ChonkOutlinedButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier.weight(1f)
            )
            ChonkButton(
                text = "Continue",
                onClick = onContinue,
                enabled = canContinue,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

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

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileStepScreenPreview() {
    ChonkCheckTheme {
        ProfileStepScreen(
            weightUnit = WeightUnit.KG,
            heightUnit = HeightUnit.CM,
            heightCm = 175.0,
            currentWeightKg = 80.0,
            birthDate = LocalDate.of(1990, 5, 15),
            sex = Sex.MALE,
            activityLevel = ActivityLevel.MODERATELY_ACTIVE,
            onHeightChange = {},
            onWeightChange = {},
            onBirthDateChange = {},
            onSexChange = {},
            onActivityLevelChange = {},
            onContinue = {},
            onBack = {},
            canContinue = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileStepScreenFeetInchesPreview() {
    ChonkCheckTheme {
        ProfileStepScreen(
            weightUnit = WeightUnit.LB,
            heightUnit = HeightUnit.FT,
            heightCm = 175.0,
            currentWeightKg = 80.0,
            birthDate = null,
            sex = null,
            activityLevel = null,
            onHeightChange = {},
            onWeightChange = {},
            onBirthDateChange = {},
            onSexChange = {},
            onActivityLevelChange = {},
            onContinue = {},
            onBack = {},
            canContinue = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileStepScreenStonesPreview() {
    ChonkCheckTheme {
        ProfileStepScreen(
            weightUnit = WeightUnit.ST,
            heightUnit = HeightUnit.FT,
            heightCm = 175.0,
            currentWeightKg = 80.0,
            birthDate = LocalDate.of(1990, 5, 15),
            sex = Sex.FEMALE,
            activityLevel = ActivityLevel.LIGHTLY_ACTIVE,
            onHeightChange = {},
            onWeightChange = {},
            onBirthDateChange = {},
            onSexChange = {},
            onActivityLevelChange = {},
            onContinue = {},
            onBack = {},
            canContinue = true
        )
    }
}
