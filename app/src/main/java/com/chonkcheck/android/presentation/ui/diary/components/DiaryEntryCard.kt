package com.chonkcheck.android.presentation.ui.diary.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.domain.model.DiaryEntry
import com.chonkcheck.android.domain.model.MealType
import com.chonkcheck.android.domain.model.ServingUnit
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun DiaryEntryCard(
    entry: DiaryEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Name
            Text(
                text = entry.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Brand if present
            if (!entry.brand.isNullOrBlank()) {
                Text(
                    text = entry.brand,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Quantity info
            Text(
                text = formatQuantity(entry),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Macros row
            Text(
                text = "P: ${entry.protein.formatMacro()}g · C: ${entry.carbs.formatMacro()}g · F: ${entry.fat.formatMacro()}g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Calories on the right
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = entry.calories.roundToInt().toString(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "cal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatQuantity(entry: DiaryEntry): String {
    val servingText = if (entry.numberOfServings == 1.0) {
        "${entry.servingSize.formatServing()} ${entry.servingUnit.displayName}"
    } else {
        "${entry.numberOfServings.formatServing()} × ${entry.servingSize.formatServing()} ${entry.servingUnit.displayName}"
    }
    return servingText
}

private fun Double.formatServing(): String {
    return if (this == this.toLong().toDouble()) {
        this.toLong().toString()
    } else {
        String.format("%.1f", this)
    }
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
private fun DiaryEntryCardPreview() {
    ChonkCheckTheme {
        DiaryEntryCard(
            entry = DiaryEntry(
                id = "1",
                userId = "user1",
                date = LocalDate.now(),
                mealType = MealType.BREAKFAST,
                foodId = "food1",
                recipeId = null,
                servingSize = 100.0,
                servingUnit = ServingUnit.GRAM,
                numberOfServings = 1.5,
                calories = 247.5,
                protein = 46.5,
                carbs = 0.0,
                fat = 5.4,
                name = "Chicken Breast",
                brand = "Tesco",
                createdAt = System.currentTimeMillis()
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DiaryEntryCardNoBrandPreview() {
    ChonkCheckTheme {
        DiaryEntryCard(
            entry = DiaryEntry(
                id = "2",
                userId = "user1",
                date = LocalDate.now(),
                mealType = MealType.LUNCH,
                foodId = "food2",
                recipeId = null,
                servingSize = 200.0,
                servingUnit = ServingUnit.GRAM,
                numberOfServings = 1.0,
                calories = 320.0,
                protein = 28.0,
                carbs = 35.0,
                fat = 8.0,
                name = "Grilled Salmon with Rice",
                brand = null,
                createdAt = System.currentTimeMillis()
            ),
            onClick = {}
        )
    }
}
