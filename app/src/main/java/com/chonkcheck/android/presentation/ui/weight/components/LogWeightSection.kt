package com.chonkcheck.android.presentation.ui.weight.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.domain.model.WeightUnit
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogWeightSection(
    weightUnit: WeightUnit,
    isSaving: Boolean,
    onLogWeight: (weight: Double, date: LocalDate, notes: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var weightText by remember { mutableStateOf("") }
    var stonesText by remember { mutableStateOf("") }
    var poundsText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

    val isValid = when (weightUnit) {
        WeightUnit.ST -> {
            val stones = stonesText.toIntOrNull() ?: 0
            val pounds = poundsText.toIntOrNull() ?: 0
            stones > 0 || pounds > 0
        }
        else -> weightText.toDoubleOrNull()?.let { it > 0 } == true
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Log Weight",
                style = MaterialTheme.typography.titleMedium
            )

            when (weightUnit) {
                WeightUnit.ST -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        OutlinedTextField(
                            value = stonesText,
                            onValueChange = { stonesText = it.filter { c -> c.isDigit() } },
                            label = { Text("Stones") },
                            suffix = { Text("st") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = poundsText,
                            onValueChange = { poundsText = it.filter { c -> c.isDigit() } },
                            label = { Text("Pounds") },
                            suffix = { Text("lb") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                else -> {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Weight") },
                        suffix = { Text(weightUnit.symbol) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            OutlinedTextField(
                value = selectedDate.format(dateFormatter),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Select date"
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    val weightInKg = when (weightUnit) {
                        WeightUnit.KG -> weightText.toDoubleOrNull() ?: return@Button
                        WeightUnit.LB -> {
                            val lb = weightText.toDoubleOrNull() ?: return@Button
                            WeightUnitConverter.unitToKg(lb, WeightUnit.LB)
                        }
                        WeightUnit.ST -> {
                            val stones = stonesText.toIntOrNull() ?: 0
                            val pounds = poundsText.toIntOrNull() ?: 0
                            WeightUnitConverter.stonePoundsToKg(stones, pounds)
                        }
                    }

                    onLogWeight(weightInKg, selectedDate, notes.takeIf { it.isNotBlank() })

                    // Reset form
                    weightText = ""
                    stonesText = ""
                    poundsText = ""
                    notes = ""
                    selectedDate = LocalDate.now()
                },
                enabled = isValid && !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Log Weight")
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
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
private fun LogWeightSectionKgPreview() {
    ChonkCheckTheme {
        LogWeightSection(
            weightUnit = WeightUnit.KG,
            isSaving = false,
            onLogWeight = { _, _, _ -> },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LogWeightSectionLbPreview() {
    ChonkCheckTheme {
        LogWeightSection(
            weightUnit = WeightUnit.LB,
            isSaving = false,
            onLogWeight = { _, _, _ -> },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LogWeightSectionStPreview() {
    ChonkCheckTheme {
        LogWeightSection(
            weightUnit = WeightUnit.ST,
            isSaving = false,
            onLogWeight = { _, _, _ -> },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LogWeightSectionSavingPreview() {
    ChonkCheckTheme {
        LogWeightSection(
            weightUnit = WeightUnit.KG,
            isSaving = true,
            onLogWeight = { _, _, _ -> },
            modifier = Modifier.padding(16.dp)
        )
    }
}
