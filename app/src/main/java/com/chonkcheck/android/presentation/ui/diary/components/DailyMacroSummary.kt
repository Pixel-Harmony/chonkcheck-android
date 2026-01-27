package com.chonkcheck.android.presentation.ui.diary.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chonkcheck.android.domain.model.DailyGoals
import com.chonkcheck.android.domain.model.MacroProgress
import com.chonkcheck.android.domain.model.MacroTotals
import com.chonkcheck.android.ui.theme.CaloriesColor
import com.chonkcheck.android.ui.theme.CarbsColor
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.FatColor
import com.chonkcheck.android.ui.theme.ProteinColor
import kotlin.math.roundToInt

@Composable
fun DailyMacroSummary(
    progress: MacroProgress?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        if (progress == null) {
            // Empty state - no goals set
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Set your goals to track progress",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // All 4 macro circles in one row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroCircleItem(
                    label = "Cal",
                    current = progress.current.calories.roundToInt(),
                    goal = progress.goals.dailyCalorieTarget,
                    progress = progress.caloriePercent,
                    color = CaloriesColor,
                    unit = ""
                )
                MacroCircleItem(
                    label = "Pro",
                    current = progress.current.protein.roundToInt(),
                    goal = progress.goals.proteinTarget,
                    progress = progress.proteinPercent,
                    color = ProteinColor,
                    unit = "g"
                )
                MacroCircleItem(
                    label = "Carb",
                    current = progress.current.carbs.roundToInt(),
                    goal = progress.goals.carbsTarget,
                    progress = progress.carbsPercent,
                    color = CarbsColor,
                    unit = "g"
                )
                MacroCircleItem(
                    label = "Fat",
                    current = progress.current.fat.roundToInt(),
                    goal = progress.goals.fatTarget,
                    progress = progress.fatPercent,
                    color = FatColor,
                    unit = "g"
                )
            }
        }
    }
}

@Composable
private fun MacroCircleItem(
    label: String,
    current: Int,
    goal: Int,
    progress: Float,
    color: Color,
    unit: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circle with percentage inside
        CircularProgressWithPercent(
            progress = progress,
            color = color,
            size = 64.dp,
            strokeWidth = 5.dp
        )

        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Current value (bold)
        Text(
            text = formatNumber(current) + unit,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Goal value (subtle)
        Text(
            text = "/ ${formatNumber(goal)}$unit",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CircularProgressWithPercent(
    progress: Float,
    color: Color,
    size: Dp,
    strokeWidth: Dp,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "progress"
    )

    // Cap at 100% for display but allow arc to show overflow
    val displayPercent = (progress * 100).roundToInt().coerceAtMost(999)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )

            // Background circle
            drawArc(
                color = color.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )

            // Progress arc (capped at 100%)
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = animatedProgress.coerceAtMost(1f) * 360f,
                useCenter = false,
                style = stroke
            )
        }

        // Percentage text inside circle
        Text(
            text = "${displayPercent}%",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatNumber(value: Int): String {
    return when {
        value >= 1000 -> String.format("%,d", value)
        else -> value.toString()
    }
}

@Preview(showBackground = true)
@Composable
private fun DailyMacroSummaryPreview() {
    ChonkCheckTheme {
        DailyMacroSummary(
            progress = MacroProgress(
                current = MacroTotals(
                    calories = 1800.0,
                    protein = 120.0,
                    carbs = 180.0,
                    fat = 50.0
                ),
                goals = DailyGoals(
                    weightGoal = null,
                    targetWeight = null,
                    weeklyGoal = null,
                    dailyCalorieTarget = 2400,
                    proteinTarget = 150,
                    carbsTarget = 200,
                    fatTarget = 65,
                    bmr = null,
                    tdee = null
                ),
                caloriePercent = 0.75f,
                proteinPercent = 0.8f,
                carbsPercent = 0.9f,
                fatPercent = 0.77f
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DailyMacroSummaryEmptyPreview() {
    ChonkCheckTheme {
        DailyMacroSummary(
            progress = null,
            modifier = Modifier.padding(16.dp)
        )
    }
}
