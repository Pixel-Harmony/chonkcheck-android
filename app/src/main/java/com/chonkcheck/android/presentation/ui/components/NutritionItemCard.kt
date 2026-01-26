package com.chonkcheck.android.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.core.util.formatMacro
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen
import com.chonkcheck.android.ui.theme.Coral
import com.chonkcheck.android.ui.theme.Purple

/**
 * A shared card layout for nutrition items (foods, recipes, saved meals).
 *
 * @param name The item name
 * @param indicatorColor Color for the indicator dot
 * @param calories Calorie count
 * @param protein Protein amount in grams
 * @param carbs Carbohydrate amount in grams
 * @param fat Fat amount in grams
 * @param onClick Callback when the card is clicked
 * @param modifier Modifier for the card
 * @param onDelete Optional callback for delete action
 * @param badges Optional composable slot for badges shown after the name
 * @param metadata Optional composable slot for additional metadata (brand, servings, etc.)
 */
@Composable
fun NutritionItemCard(
    name: String,
    indicatorColor: Color,
    calories: Double,
    protein: Double,
    carbs: Double,
    fat: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null,
    badges: @Composable (RowScope.() -> Unit)? = null,
    metadata: @Composable (() -> Unit)? = null
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
            // Name row with badges and indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                // Optional badges
                badges?.invoke(this)

                // Indicator dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(indicatorColor)
                )
            }

            // Optional metadata (brand, serving info, etc.)
            metadata?.invoke()

            // Macros
            Text(
                text = "P: ${protein.formatMacro()}g - C: ${carbs.formatMacro()}g - F: ${fat.formatMacro()}g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Right content - calories and delete
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Calories
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = calories.toInt().toString(),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "cal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Delete button (if provided)
            if (onDelete != null) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NutritionItemCardFoodPreview() {
    ChonkCheckTheme {
        NutritionItemCard(
            name = "Chicken Breast",
            indicatorColor = Coral,
            calories = 165.0,
            protein = 31.0,
            carbs = 0.0,
            fat = 3.6,
            onClick = {},
            onDelete = {},
            metadata = {
                Text(
                    text = "Tesco",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "100 g",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NutritionItemCardRecipePreview() {
    ChonkCheckTheme {
        NutritionItemCard(
            name = "Chicken Stir Fry",
            indicatorColor = ChonkGreen,
            calories = 200.0,
            protein = 20.0,
            carbs = 15.0,
            fat = 5.0,
            onClick = {},
            onDelete = {},
            metadata = {
                Text(
                    text = "3 ingredients - 4 servings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NutritionItemCardMealPreview() {
    ChonkCheckTheme {
        NutritionItemCard(
            name = "My Usual Breakfast",
            indicatorColor = Purple,
            calories = 300.0,
            protein = 16.0,
            carbs = 33.0,
            fat = 12.0,
            onClick = {},
            onDelete = {},
            metadata = {
                Text(
                    text = "2 items",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NutritionItemCardNoDeletePreview() {
    ChonkCheckTheme {
        NutritionItemCard(
            name = "Platform Food",
            indicatorColor = Coral,
            calories = 165.0,
            protein = 31.0,
            carbs = 0.0,
            fat = 3.6,
            onClick = {},
            onDelete = null
        )
    }
}
