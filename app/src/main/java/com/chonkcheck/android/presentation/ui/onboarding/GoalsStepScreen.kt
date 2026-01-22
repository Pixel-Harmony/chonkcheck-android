package com.chonkcheck.android.presentation.ui.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.domain.model.DietPreset
import com.chonkcheck.android.domain.model.MacroTargets
import com.chonkcheck.android.domain.model.WeightGoal
import com.chonkcheck.android.domain.model.WeightUnit
import com.chonkcheck.android.domain.usecase.TdeeResult
import com.chonkcheck.android.presentation.ui.components.ChonkButton
import com.chonkcheck.android.presentation.ui.components.ChonkOutlinedButton
import com.chonkcheck.android.ui.theme.CarbsColor
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen
import com.chonkcheck.android.ui.theme.FatColor
import com.chonkcheck.android.ui.theme.ProteinColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GoalsStepScreen(
    weightUnit: WeightUnit,
    weightGoal: WeightGoal?,
    weeklyGoalKg: Double?,
    tdeePreview: TdeeResult?,
    caloriePreview: Int?,
    dietPreset: DietPreset?,
    macroTargets: MacroTargets?,
    onWeightGoalChange: (WeightGoal) -> Unit,
    onWeeklyGoalChange: (Double) -> Unit,
    onDietPresetChange: (DietPreset) -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit,
    canComplete: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val weeklyGoalRange = remember(weightGoal) {
        when (weightGoal) {
            WeightGoal.LOSE -> -1.0f..-0.25f
            WeightGoal.GAIN -> 0.1f..0.5f
            else -> 0f..0f
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your Daily Goals",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pick your goal. You can always change it later in Settings.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        WeightGoal.entries.forEach { goal ->
            GoalOptionCard(
                goal = goal,
                isSelected = weightGoal == goal,
                onClick = { onWeightGoalChange(goal) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (weightGoal != null && weightGoal != WeightGoal.MAINTAIN) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Weekly Target",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            val weeklyGoalDisplay = weeklyGoalKg?.let { kg ->
                when (weightUnit) {
                    WeightUnit.KG -> String.format("%.2f kg", kotlin.math.abs(kg))
                    WeightUnit.LB -> String.format("%.1f lb", kotlin.math.abs(kg) * 2.20462)
                    WeightUnit.ST -> {
                        val totalLb = kotlin.math.abs(kg) * 2.20462
                        String.format("%.1f lb", totalLb)
                    }
                }
            } ?: ""

            Text(
                text = if (weightGoal == WeightGoal.LOSE) {
                    "Lose $weeklyGoalDisplay per week"
                } else {
                    "Gain $weeklyGoalDisplay per week"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = (weeklyGoalKg?.toFloat() ?: weeklyGoalRange.start),
                onValueChange = { onWeeklyGoalChange(it.toDouble()) },
                valueRange = weeklyGoalRange,
                steps = when (weightGoal) {
                    WeightGoal.LOSE -> 2
                    WeightGoal.GAIN -> 3
                    else -> 0
                },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (weightGoal == WeightGoal.LOSE) "Gradual" else "Slow",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (weightGoal == WeightGoal.LOSE) "Aggressive" else "Fast",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (tdeePreview != null && caloriePreview != null) {
            Spacer(modifier = Modifier.height(24.dp))

            TdeeCalorieCard(
                tdee = tdeePreview.tdee,
                dailyTarget = caloriePreview,
                weightGoal = weightGoal,
                macroTargets = macroTargets
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Diet Preset",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
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
                text = "Start Tracking \u2192",
                onClick = onComplete,
                enabled = canComplete,
                isLoading = isLoading,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
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
private fun TdeeCalorieCard(
    tdee: Int,
    dailyTarget: Int,
    weightGoal: WeightGoal?,
    macroTargets: MacroTargets?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ChonkGreen
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Your Daily Calorie Target",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$dailyTarget",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "calories per day",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            if (macroTargets != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MacroDisplay(
                        label = "Protein",
                        grams = macroTargets.protein,
                        color = ProteinColor
                    )
                    MacroDisplay(
                        label = "Carbs",
                        grams = macroTargets.carbs,
                        color = CarbsColor
                    )
                    MacroDisplay(
                        label = "Fat",
                        grams = macroTargets.fat,
                        color = FatColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val difference = dailyTarget - tdee
            val differenceText = when {
                weightGoal == WeightGoal.MAINTAIN -> "Maintenance calories (TDEE: $tdee)"
                difference < 0 -> "${kotlin.math.abs(difference)} cal deficit from TDEE ($tdee)"
                difference > 0 -> "${difference} cal surplus from TDEE ($tdee)"
                else -> "At maintenance"
            }

            Text(
                text = differenceText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun MacroDisplay(
    label: String,
    grams: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${grams}g",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GoalsStepScreenPreview() {
    ChonkCheckTheme {
        GoalsStepScreen(
            weightUnit = WeightUnit.KG,
            weightGoal = WeightGoal.LOSE,
            weeklyGoalKg = -0.5,
            tdeePreview = TdeeResult(bmr = 1800, tdee = 2400, maintenanceCalories = 2400),
            caloriePreview = 1900,
            dietPreset = DietPreset.BALANCED,
            macroTargets = MacroTargets(protein = 143, carbs = 190, fat = 63),
            onWeightGoalChange = {},
            onWeeklyGoalChange = {},
            onDietPresetChange = {},
            onComplete = {},
            onBack = {},
            canComplete = true,
            isLoading = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GoalsStepScreenEmptyPreview() {
    ChonkCheckTheme {
        GoalsStepScreen(
            weightUnit = WeightUnit.KG,
            weightGoal = null,
            weeklyGoalKg = null,
            tdeePreview = null,
            caloriePreview = null,
            dietPreset = null,
            macroTargets = null,
            onWeightGoalChange = {},
            onWeeklyGoalChange = {},
            onDietPresetChange = {},
            onComplete = {},
            onBack = {},
            canComplete = false,
            isLoading = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GoalsStepScreenHighProteinPreview() {
    ChonkCheckTheme {
        GoalsStepScreen(
            weightUnit = WeightUnit.LB,
            weightGoal = WeightGoal.GAIN,
            weeklyGoalKg = 0.25,
            tdeePreview = TdeeResult(bmr = 1800, tdee = 2400, maintenanceCalories = 2400),
            caloriePreview = 2700,
            dietPreset = DietPreset.HIGH_PROTEIN,
            macroTargets = MacroTargets(protein = 270, carbs = 236, fat = 75),
            onWeightGoalChange = {},
            onWeeklyGoalChange = {},
            onDietPresetChange = {},
            onComplete = {},
            onBack = {},
            canComplete = true,
            isLoading = false
        )
    }
}
