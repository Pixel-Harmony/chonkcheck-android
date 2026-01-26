package com.chonkcheck.android.presentation.ui.meals.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.Purple

@Composable
fun MealNutritionSummaryCard(
    totalNutrition: MealItemNutrition,
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
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Nutrition Summary",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Total row
            NutritionRow(
                label = "Total",
                calories = totalNutrition.calories,
                protein = totalNutrition.protein,
                carbs = totalNutrition.carbs,
                fat = totalNutrition.fat,
                isHighlighted = true
            )
        }
    }
}

@Composable
private fun NutritionRow(
    label: String,
    calories: Double,
    protein: Double,
    carbs: Double,
    fat: Double,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isHighlighted) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (isHighlighted) Purple else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MacroValue(label = "cal", value = calories.toInt())
            MacroValue(label = "P", value = protein.formatMacro(), suffix = "g")
            MacroValue(label = "C", value = carbs.formatMacro(), suffix = "g")
            MacroValue(label = "F", value = fat.formatMacro(), suffix = "g")
        }
    }
}

@Composable
private fun MacroValue(
    label: String,
    value: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = "$value $label",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
private fun MacroValue(
    label: String,
    value: String,
    suffix: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = "$label: $value$suffix",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

private fun Double.formatMacro(): String {
    return if (this == this.toLong().toDouble()) {
        this.toLong().toString()
    } else {
        String.format("%.1f", this)
    }
}

@Preview(showBackground = true)
@Composable
private fun MealNutritionSummaryCardPreview() {
    ChonkCheckTheme {
        MealNutritionSummaryCard(
            totalNutrition = MealItemNutrition(
                calories = 650.0,
                protein = 45.0,
                carbs = 60.0,
                fat = 25.0
            )
        )
    }
}
