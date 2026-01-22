package com.chonkcheck.android.presentation.ui.recipes.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.domain.model.Food
import com.chonkcheck.android.domain.model.FoodFilter
import com.chonkcheck.android.domain.model.FoodFilterType
import com.chonkcheck.android.domain.model.FoodType
import com.chonkcheck.android.domain.model.ServingUnit
import com.chonkcheck.android.presentation.ui.foods.components.FoodSearchBar
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientSearchSheet(
    foods: List<Food>,
    isLoading: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onFoodSelected: (Food) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var searchQuery by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Add Ingredient",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            FoodSearchBar(
                query = searchQuery,
                onQueryChange = { query ->
                    searchQuery = query
                    onSearchQueryChange(query)
                },
                placeholder = "Search foods...",
                accentColor = ChonkGreen
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (foods.isEmpty() && !isLoading) {
                Text(
                    text = if (searchQuery.isNotEmpty()) "No foods found" else "Start typing to search foods",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(
                        items = foods,
                        key = { it.id }
                    ) { food ->
                        IngredientSearchItem(
                            food = food,
                            onClick = { onFoodSelected(food) }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun IngredientSearchItem(
    food: Food,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = food.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!food.brand.isNullOrBlank()) {
                Text(
                    text = food.brand,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "${food.servingSize.formatServing()} ${food.servingUnit.displayName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${food.calories.toInt()} cal",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun Double.formatServing(): String {
    return if (this == this.toLong().toDouble()) {
        this.toLong().toString()
    } else {
        this.toString()
    }
}

@Preview(showBackground = true)
@Composable
private fun IngredientSearchItemPreview() {
    ChonkCheckTheme {
        IngredientSearchItem(
            food = Food(
                id = "1",
                name = "Chicken Breast",
                brand = "Tesco",
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
            onClick = {}
        )
    }
}
