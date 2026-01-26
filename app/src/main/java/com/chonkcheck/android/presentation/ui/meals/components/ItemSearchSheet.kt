package com.chonkcheck.android.presentation.ui.meals.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.domain.model.Food
import com.chonkcheck.android.domain.model.FoodType
import com.chonkcheck.android.domain.model.Recipe
import com.chonkcheck.android.domain.model.RecipeServingUnit
import com.chonkcheck.android.domain.model.ServingUnit
import com.chonkcheck.android.presentation.ui.foods.components.FoodSearchBar
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen
import com.chonkcheck.android.ui.theme.Purple

sealed class SearchableItem {
    data class FoodItem(val food: Food) : SearchableItem()
    data class RecipeItem(val recipe: Recipe) : SearchableItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemSearchSheet(
    foods: List<Food>,
    recipes: List<Recipe>,
    isLoading: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onFoodSelected: (Food) -> Unit,
    onRecipeSelected: (Recipe) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var searchQuery by remember { mutableStateOf("") }

    // Combine foods and recipes into a single list
    val items = remember(foods, recipes) {
        val combined = mutableListOf<SearchableItem>()
        foods.forEach { combined.add(SearchableItem.FoodItem(it)) }
        recipes.forEach { combined.add(SearchableItem.RecipeItem(it)) }
        combined
    }

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
                text = "Add Item",
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
                placeholder = "Search foods & recipes...",
                accentColor = Purple
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (items.isEmpty() && !isLoading) {
                Text(
                    text = if (searchQuery.isNotEmpty()) "No items found" else "Start typing to search",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(
                        items = items,
                        key = { item ->
                            when (item) {
                                is SearchableItem.FoodItem -> "food_${item.food.id}"
                                is SearchableItem.RecipeItem -> "recipe_${item.recipe.id}"
                            }
                        }
                    ) { item ->
                        when (item) {
                            is SearchableItem.FoodItem -> ItemSearchRow(
                                name = item.food.name,
                                subtitle = item.food.brand,
                                servingInfo = "${item.food.servingSize.formatServing()} ${item.food.servingUnit.displayName}",
                                calories = item.food.calories.toInt(),
                                indicatorColor = null,
                                onClick = { onFoodSelected(item.food) }
                            )
                            is SearchableItem.RecipeItem -> ItemSearchRow(
                                name = item.recipe.name,
                                subtitle = null,
                                servingInfo = "1 ${item.recipe.servingUnit.displayName}",
                                calories = item.recipe.caloriesPerServing.toInt(),
                                indicatorColor = ChonkGreen,
                                onClick = { onRecipeSelected(item.recipe) }
                            )
                        }
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
private fun ItemSearchRow(
    name: String,
    subtitle: String?,
    servingInfo: String,
    calories: Int,
    indicatorColor: androidx.compose.ui.graphics.Color?,
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

                if (indicatorColor != null) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(indicatorColor)
                    )
                }
            }

            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = servingInfo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "$calories cal",
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
private fun ItemSearchRowFoodPreview() {
    ChonkCheckTheme {
        ItemSearchRow(
            name = "Chicken Breast",
            subtitle = "Tesco",
            servingInfo = "100 g",
            calories = 165,
            indicatorColor = null,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ItemSearchRowRecipePreview() {
    ChonkCheckTheme {
        ItemSearchRow(
            name = "Chicken Stir Fry",
            subtitle = null,
            servingInfo = "1 serving",
            calories = 350,
            indicatorColor = ChonkGreen,
            onClick = {}
        )
    }
}
