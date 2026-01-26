package com.chonkcheck.android.presentation.ui.meals.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.domain.model.SavedMealItemType
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen
import com.chonkcheck.android.ui.theme.Coral
import com.chonkcheck.android.ui.theme.Purple

enum class MealItemInputMode {
    SERVINGS,
    AMOUNT
}

data class MealItemNutrition(
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

@Composable
fun MealItemListItem(
    itemName: String,
    itemType: SavedMealItemType,
    servingSize: Double,
    servingUnitName: String,
    inputMode: MealItemInputMode,
    quantity: Double,
    enteredAmount: Double?,
    nutrition: MealItemNutrition,
    onQuantityChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onToggleInputMode: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val indicatorColor = when (itemType) {
        SavedMealItemType.FOOD -> Coral
        SavedMealItemType.RECIPE -> ChonkGreen
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header row with name and delete
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Indicator dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(indicatorColor)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = itemName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Remove item",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quantity/amount input
            OutlinedTextField(
                value = if (inputMode == MealItemInputMode.SERVINGS) {
                    quantity.formatInput()
                } else {
                    enteredAmount?.formatInput() ?: ""
                },
                onValueChange = {
                    if (inputMode == MealItemInputMode.SERVINGS) {
                        onQuantityChange(it)
                    } else {
                        onAmountChange(it)
                    }
                },
                modifier = Modifier.width(100.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Label showing current mode
            Text(
                text = if (inputMode == MealItemInputMode.SERVINGS) {
                    "serving${if (quantity != 1.0) "s" else ""}"
                } else {
                    servingUnitName
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            // Toggle button (only show for foods, not recipes)
            if (itemType == SavedMealItemType.FOOD) {
                TextButton(onClick = onToggleInputMode) {
                    Text(
                        text = if (inputMode == MealItemInputMode.SERVINGS) {
                            "use $servingUnitName"
                        } else {
                            "use servings"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = Purple
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nutrition display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${nutrition.calories.toInt()} cal",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "P: ${nutrition.protein.formatMacro()}g - C: ${nutrition.carbs.formatMacro()}g - F: ${nutrition.fat.formatMacro()}g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun Double.formatInput(): String {
    return if (this == this.toLong().toDouble()) {
        this.toLong().toString()
    } else {
        this.toString()
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
private fun MealItemListItemFoodPreview() {
    ChonkCheckTheme {
        MealItemListItem(
            itemName = "Chicken Breast",
            itemType = SavedMealItemType.FOOD,
            servingSize = 100.0,
            servingUnitName = "g",
            inputMode = MealItemInputMode.SERVINGS,
            quantity = 1.5,
            enteredAmount = null,
            nutrition = MealItemNutrition(
                calories = 247.5,
                protein = 46.5,
                carbs = 0.0,
                fat = 5.4
            ),
            onQuantityChange = {},
            onAmountChange = {},
            onToggleInputMode = {},
            onDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MealItemListItemRecipePreview() {
    ChonkCheckTheme {
        MealItemListItem(
            itemName = "Chicken Stir Fry",
            itemType = SavedMealItemType.RECIPE,
            servingSize = 1.0,
            servingUnitName = "serving",
            inputMode = MealItemInputMode.SERVINGS,
            quantity = 2.0,
            enteredAmount = null,
            nutrition = MealItemNutrition(
                calories = 400.0,
                protein = 40.0,
                carbs = 30.0,
                fat = 12.0
            ),
            onQuantityChange = {},
            onAmountChange = {},
            onToggleInputMode = {},
            onDelete = {}
        )
    }
}
