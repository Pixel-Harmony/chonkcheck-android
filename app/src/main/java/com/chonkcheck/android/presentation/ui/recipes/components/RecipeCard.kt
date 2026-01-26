package com.chonkcheck.android.presentation.ui.recipes.components

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
import com.chonkcheck.android.domain.model.Recipe
import com.chonkcheck.android.domain.model.RecipeServingUnit
import com.chonkcheck.android.core.util.formatMacro
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen

@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onDelete: () -> Unit,
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
                    text = recipe.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                // Green indicator dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(ChonkGreen)
                )
            }

            // Ingredient count and servings
            Text(
                text = "${recipe.ingredients.size} ingredient${if (recipe.ingredients.size != 1) "s" else ""} - ${recipe.totalServings} ${recipe.servingUnit.displayName}${if (recipe.totalServings != 1) "s" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Macros per serving
            Text(
                text = "P: ${recipe.proteinPerServing.formatMacro()}g - C: ${recipe.carbsPerServing.formatMacro()}g - F: ${recipe.fatPerServing.formatMacro()}g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Right content - calories and delete
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Calories per serving
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = recipe.caloriesPerServing.toInt().toString(),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "cal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete recipe",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun RecipeCardPreview() {
    ChonkCheckTheme {
        RecipeCard(
            recipe = Recipe(
                id = "1",
                userId = "user1",
                name = "Chicken Stir Fry",
                description = "A quick and healthy meal",
                totalServings = 4,
                servingUnit = RecipeServingUnit.SERVING,
                ingredients = emptyList(),
                totalCalories = 800.0,
                totalProtein = 80.0,
                totalCarbs = 60.0,
                totalFat = 20.0,
                caloriesPerServing = 200.0,
                proteinPerServing = 20.0,
                carbsPerServing = 15.0,
                fatPerServing = 5.0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            onClick = {},
            onDelete = {}
        )
    }
}
