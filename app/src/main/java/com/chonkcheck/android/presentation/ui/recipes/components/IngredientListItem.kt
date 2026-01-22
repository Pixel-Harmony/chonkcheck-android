package com.chonkcheck.android.presentation.ui.recipes.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.domain.model.Food
import com.chonkcheck.android.domain.model.FoodType
import com.chonkcheck.android.domain.model.ServingUnit
import com.chonkcheck.android.presentation.ui.recipes.InputMode
import com.chonkcheck.android.presentation.ui.recipes.NutritionSummary
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen
import com.chonkcheck.android.ui.theme.Coral

@Composable
fun IngredientListItem(
    food: Food,
    inputMode: InputMode,
    quantity: Double,
    enteredAmount: Double?,
    nutrition: NutritionSummary,
    onQuantityChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onToggleInputMode: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                // Orange dot indicator
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Coral)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = food.name,
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
                    contentDescription = "Remove ingredient",
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
                value = if (inputMode == InputMode.SERVINGS) {
                    quantity.formatInput()
                } else {
                    enteredAmount?.formatInput() ?: ""
                },
                onValueChange = {
                    if (inputMode == InputMode.SERVINGS) {
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
                    focusedBorderColor = ChonkGreen,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Label showing current mode
            Text(
                text = if (inputMode == InputMode.SERVINGS) {
                    "serving${if (quantity != 1.0) "s" else ""}"
                } else {
                    food.servingUnit.displayName
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            // Toggle button
            TextButton(onClick = onToggleInputMode) {
                Text(
                    text = if (inputMode == InputMode.SERVINGS) {
                        "use ${food.servingUnit.displayName}"
                    } else {
                        "use servings"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = ChonkGreen
                )
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
private fun IngredientListItemServingsPreview() {
    ChonkCheckTheme {
        IngredientListItem(
            food = Food(
                id = "1",
                name = "Chicken Breast",
                brand = null,
                barcode = null,
                servingSize = 100.0,
                servingUnit = ServingUnit.GRAM,
                servingsPerContainer = null,
                calories = 165.0,
                protein = 31.0,
                carbs = 0.0,
                fat = 3.6,
                fiber = null,
                sugar = null,
                sodium = null,
                saturatedFat = null,
                transFat = null,
                cholesterol = null,
                addedSugar = null,
                vitaminA = null,
                vitaminC = null,
                vitaminD = null,
                calcium = null,
                iron = null,
                potassium = null,
                type = FoodType.PLATFORM,
                source = null,
                verified = true,
                promotionRequested = false,
                overrideOf = null,
                imageUrl = null,
                createdAt = System.currentTimeMillis()
            ),
            inputMode = InputMode.SERVINGS,
            quantity = 1.5,
            enteredAmount = null,
            nutrition = NutritionSummary(
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
private fun IngredientListItemAmountPreview() {
    ChonkCheckTheme {
        IngredientListItem(
            food = Food(
                id = "1",
                name = "Chicken Breast",
                brand = null,
                barcode = null,
                servingSize = 100.0,
                servingUnit = ServingUnit.GRAM,
                servingsPerContainer = null,
                calories = 165.0,
                protein = 31.0,
                carbs = 0.0,
                fat = 3.6,
                fiber = null,
                sugar = null,
                sodium = null,
                saturatedFat = null,
                transFat = null,
                cholesterol = null,
                addedSugar = null,
                vitaminA = null,
                vitaminC = null,
                vitaminD = null,
                calcium = null,
                iron = null,
                potassium = null,
                type = FoodType.PLATFORM,
                source = null,
                verified = true,
                promotionRequested = false,
                overrideOf = null,
                imageUrl = null,
                createdAt = System.currentTimeMillis()
            ),
            inputMode = InputMode.AMOUNT,
            quantity = 1.5,
            enteredAmount = 150.0,
            nutrition = NutritionSummary(
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
