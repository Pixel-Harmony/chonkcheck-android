package com.chonkcheck.android.presentation.ui.diary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.domain.model.DiaryEntry
import com.chonkcheck.android.domain.model.DiaryItemType
import com.chonkcheck.android.domain.model.MealType
import com.chonkcheck.android.domain.model.ServingUnit
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen
import com.chonkcheck.android.ui.theme.Coral
import com.chonkcheck.android.ui.theme.MealPurple
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun DiaryEntryCard(
    entry: DiaryEntry,
    onClick: () -> Unit,
    onDeleteClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    // Color based on item type: foods = orange, recipes = green, meals = purple
    val typeColor = when {
        entry.mealGroupId != null -> MealPurple
        entry.itemType == DiaryItemType.RECIPE -> ChonkGreen
        else -> Coral
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Name with type indicator (rounded square)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Rounded square type indicator
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(typeColor)
                    )
                }

                // Brand if present
                if (!entry.brand.isNullOrBlank()) {
                    Text(
                        text = entry.brand,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Quantity info
                Text(
                    text = formatQuantity(entry),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Calories on the right
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = entry.calories.roundToInt().toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
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

            // Delete button (only show if onDeleteClick is provided)
            if (onDeleteClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete entry",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

private fun formatQuantity(entry: DiaryEntry): String {
    // If we have an entered amount, display it directly
    val amount = entry.enteredAmount ?: (entry.servingSize * entry.numberOfServings)

    return if (entry.numberOfServings == 1.0) {
        "${amount.formatServing()} ${entry.servingUnit.displayName}"
    } else {
        "${entry.numberOfServings.formatServing()} × ${entry.servingSize.formatServing()} ${entry.servingUnit.displayName}"
    }
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
private fun DiaryEntryCardFoodPreview() {
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
                createdAt = System.currentTimeMillis(),
                itemType = DiaryItemType.FOOD
            ),
            onClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DiaryEntryCardRecipePreview() {
    ChonkCheckTheme {
        DiaryEntryCard(
            entry = DiaryEntry(
                id = "2",
                userId = "user1",
                date = LocalDate.now(),
                mealType = MealType.LUNCH,
                foodId = null,
                recipeId = "recipe1",
                servingSize = 200.0,
                servingUnit = ServingUnit.GRAM,
                numberOfServings = 1.0,
                calories = 320.0,
                protein = 28.0,
                carbs = 35.0,
                fat = 8.0,
                name = "Grilled Salmon with Rice",
                brand = null,
                createdAt = System.currentTimeMillis(),
                itemType = DiaryItemType.RECIPE
            ),
            onClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DiaryEntryCardNoDeletePreview() {
    ChonkCheckTheme {
        DiaryEntryCard(
            entry = DiaryEntry(
                id = "3",
                userId = "user1",
                date = LocalDate.now(),
                mealType = MealType.DINNER,
                foodId = "food3",
                recipeId = null,
                servingSize = 150.0,
                servingUnit = ServingUnit.GRAM,
                numberOfServings = 1.0,
                calories = 200.0,
                protein = 20.0,
                carbs = 15.0,
                fat = 6.0,
                name = "Grilled Chicken",
                brand = null,
                createdAt = System.currentTimeMillis(),
                itemType = DiaryItemType.FOOD
            ),
            onClick = {},
            onDeleteClick = null
        )
    }
}
