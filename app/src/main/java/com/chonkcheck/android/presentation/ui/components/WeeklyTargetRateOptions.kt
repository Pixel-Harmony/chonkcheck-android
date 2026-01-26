package com.chonkcheck.android.presentation.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.domain.model.WeightGoal
import com.chonkcheck.android.domain.model.WeightUnit
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import kotlin.math.abs

/**
 * Rate options for weekly weight change targets.
 * Returns rates in the display unit (kg or lb).
 */
fun getWeeklyRateOptions(weightUnit: WeightUnit): List<Double> {
    return when (weightUnit) {
        WeightUnit.KG -> listOf(0.25, 0.5, 0.75, 1.0)
        WeightUnit.LB, WeightUnit.ST -> listOf(0.5, 1.0, 1.5, 2.0)
    }
}

/**
 * Converts a rate from display units to kg.
 */
fun rateToKg(rate: Double, weightUnit: WeightUnit): Double {
    return when (weightUnit) {
        WeightUnit.KG -> rate
        WeightUnit.LB, WeightUnit.ST -> rate / 2.20462
    }
}

/**
 * Gets the unit label for display.
 */
fun getWeeklyRateUnitLabel(weightUnit: WeightUnit): String {
    return if (weightUnit == WeightUnit.ST) "lb" else weightUnit.symbol
}

@Composable
fun WeeklyTargetRateOptions(
    weightUnit: WeightUnit,
    weightGoal: WeightGoal,
    weeklyGoalKg: Double?,
    tdee: Int?,
    onWeeklyGoalChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val rateOptions = getWeeklyRateOptions(weightUnit)
    val unitLabel = getWeeklyRateUnitLabel(weightUnit)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        rateOptions.forEach { rate ->
            val rateInKg = rateToKg(rate, weightUnit)

            // Calculate target calories (7700 cal per kg)
            val calorieChange = (rateInKg * 7700 / 7).toInt()
            val targetCalories = if (weightGoal == WeightGoal.LOSE) {
                (tdee ?: 2000) - calorieChange
            } else {
                (tdee ?: 2000) + calorieChange
            }

            val isTooLow = targetCalories < 1200
            val isSelected = weeklyGoalKg?.let {
                abs(it - rateInKg) < 0.01
            } ?: false

            RateOptionButton(
                rate = rate,
                unitLabel = unitLabel,
                targetCalories = targetCalories,
                isSelected = isSelected,
                isTooLow = isTooLow,
                onClick = {
                    if (!isTooLow) {
                        // Always store as positive - direction determined by weightGoal
                        onWeeklyGoalChange(rateInKg)
                    }
                }
            )
        }
    }
}

@Composable
fun RateOptionButton(
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
                    text = "${formatCalories(targetCalories)} cal/day",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isTooLow) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                if (isTooLow) {
                    Text(
                        text = "!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun formatCalories(calories: Int): String {
    return calories.toString().reversed().chunked(3).joinToString(",").reversed()
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WeeklyTargetRateOptionsPreview() {
    ChonkCheckTheme {
        Surface {
            WeeklyTargetRateOptions(
                weightUnit = WeightUnit.KG,
                weightGoal = WeightGoal.LOSE,
                weeklyGoalKg = 0.5,
                tdee = 2400,
                onWeeklyGoalChange = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WeeklyTargetRateOptionsLbPreview() {
    ChonkCheckTheme {
        Surface {
            WeeklyTargetRateOptions(
                weightUnit = WeightUnit.LB,
                weightGoal = WeightGoal.GAIN,
                weeklyGoalKg = 0.45, // ~1 lb
                tdee = 2400,
                onWeeklyGoalChange = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
