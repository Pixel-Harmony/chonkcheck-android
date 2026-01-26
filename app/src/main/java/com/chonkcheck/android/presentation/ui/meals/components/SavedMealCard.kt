package com.chonkcheck.android.presentation.ui.meals.components

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
import com.chonkcheck.android.domain.model.SavedMeal
import com.chonkcheck.android.domain.model.SavedMealItem
import com.chonkcheck.android.domain.model.SavedMealItemType
import com.chonkcheck.android.domain.model.ServingUnit
import com.chonkcheck.android.core.util.formatMacro
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.Purple

@Composable
fun SavedMealCard(
    savedMeal: SavedMeal,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Name row with indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = savedMeal.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                // Purple indicator dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Purple)
                )
            }

            // Item count
            Text(
                text = "${savedMeal.items.size} item${if (savedMeal.items.size != 1) "s" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Macros
            Text(
                text = "P: ${savedMeal.totalProtein.formatMacro()}g - C: ${savedMeal.totalCarbs.formatMacro()}g - F: ${savedMeal.totalFat.formatMacro()}g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Right content - calories and delete
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Total calories
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = savedMeal.totalCalories.toInt().toString(),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "cal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Delete button (only shown if onDelete provided)
            if (onDelete != null) {
                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete meal",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun SavedMealCardPreview() {
    ChonkCheckTheme {
        SavedMealCard(
            savedMeal = SavedMeal(
                id = "1",
                userId = "user1",
                name = "My Usual Breakfast",
                items = listOf(
                    SavedMealItem(
                        itemId = "food1",
                        itemType = SavedMealItemType.FOOD,
                        itemName = "Eggs",
                        brand = null,
                        servingSize = 50.0,
                        servingUnit = ServingUnit.GRAM,
                        quantity = 2.0,
                        enteredAmount = null,
                        calories = 140.0,
                        protein = 12.0,
                        carbs = 1.0,
                        fat = 10.0
                    ),
                    SavedMealItem(
                        itemId = "food2",
                        itemType = SavedMealItemType.FOOD,
                        itemName = "Toast",
                        brand = null,
                        servingSize = 30.0,
                        servingUnit = ServingUnit.GRAM,
                        quantity = 2.0,
                        enteredAmount = null,
                        calories = 160.0,
                        protein = 4.0,
                        carbs = 32.0,
                        fat = 2.0
                    )
                ),
                totalCalories = 300.0,
                totalProtein = 16.0,
                totalCarbs = 33.0,
                totalFat = 12.0,
                usageCount = 5,
                lastUsedAt = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            onClick = {},
            onDelete = {}
        )
    }
}
