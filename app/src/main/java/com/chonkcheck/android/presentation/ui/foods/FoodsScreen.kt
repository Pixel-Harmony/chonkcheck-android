package com.chonkcheck.android.presentation.ui.foods

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chonkcheck.android.domain.model.FoodFilterType
import com.chonkcheck.android.domain.model.Recipe
import com.chonkcheck.android.domain.model.RecipeServingUnit
import com.chonkcheck.android.presentation.ui.components.LoadingIndicator
import com.chonkcheck.android.presentation.ui.foods.components.FoodCard
import com.chonkcheck.android.presentation.ui.foods.components.FoodFilterChips
import com.chonkcheck.android.presentation.ui.foods.components.FoodSearchBar
import com.chonkcheck.android.presentation.ui.foods.components.FoodsEmptyState
import com.chonkcheck.android.presentation.ui.recipes.RecipeDeleteConfirmation
import com.chonkcheck.android.presentation.ui.recipes.RecipesEvent
import com.chonkcheck.android.presentation.ui.recipes.RecipesUiState
import com.chonkcheck.android.presentation.ui.recipes.RecipesViewModel
import com.chonkcheck.android.presentation.ui.recipes.components.RecipeCard
import com.chonkcheck.android.presentation.ui.recipes.components.RecipeSearchBar
import com.chonkcheck.android.presentation.ui.recipes.components.RecipesEmptyState
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen
import com.chonkcheck.android.ui.theme.Coral
import com.chonkcheck.android.ui.theme.Purple

enum class FoodHubTab {
    FOODS,
    RECIPES,
    MEALS
}

@Composable
fun FoodsScreen(
    onNavigateToEditFood: (String) -> Unit,
    onNavigateToCreateFood: () -> Unit,
    onNavigateToCreateRecipe: () -> Unit = {},
    onNavigateToEditRecipe: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    foodsViewModel: FoodsViewModel = hiltViewModel(),
    recipesViewModel: RecipesViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(FoodHubTab.FOODS) }

    val foodsUiState by foodsViewModel.uiState.collectAsStateWithLifecycle()
    val foodsEvent by foodsViewModel.events.collectAsStateWithLifecycle()

    val recipesUiState by recipesViewModel.uiState.collectAsStateWithLifecycle()
    val recipesEvent by recipesViewModel.events.collectAsStateWithLifecycle()

    // Handle foods navigation events
    LaunchedEffect(foodsEvent) {
        when (foodsEvent) {
            is FoodsEvent.NavigateToEditFood -> {
                onNavigateToEditFood((foodsEvent as FoodsEvent.NavigateToEditFood).foodId)
                foodsViewModel.onEventConsumed()
            }
            is FoodsEvent.NavigateToCreateFood -> {
                onNavigateToCreateFood()
                foodsViewModel.onEventConsumed()
            }
            is FoodsEvent.FoodDeleted -> {
                foodsViewModel.onEventConsumed()
            }
            is FoodsEvent.ShowError -> {
                foodsViewModel.onEventConsumed()
            }
            null -> {}
        }
    }

    // Handle recipes navigation events
    LaunchedEffect(recipesEvent) {
        when (recipesEvent) {
            is RecipesEvent.NavigateToEditRecipe -> {
                onNavigateToEditRecipe((recipesEvent as RecipesEvent.NavigateToEditRecipe).recipeId)
                recipesViewModel.onEventConsumed()
            }
            is RecipesEvent.NavigateToCreateRecipe -> {
                onNavigateToCreateRecipe()
                recipesViewModel.onEventConsumed()
            }
            is RecipesEvent.RecipeDeleted -> {
                recipesViewModel.onEventConsumed()
            }
            is RecipesEvent.ShowError -> {
                recipesViewModel.onEventConsumed()
            }
            null -> {}
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Main content area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                FoodHubTab.FOODS -> FoodsContent(
                    uiState = foodsUiState,
                    onSearchQueryChange = foodsViewModel::onSearchQueryChange,
                    onFilterTypeChange = foodsViewModel::onFilterTypeChange,
                    onFoodClick = foodsViewModel::onFoodClick,
                    onAddFoodClick = foodsViewModel::onAddFoodClick,
                    onDeleteClick = foodsViewModel::onDeleteClick,
                    onDeleteConfirm = foodsViewModel::onDeleteConfirm,
                    onDeleteCancel = foodsViewModel::onDeleteCancel,
                    onRefresh = foodsViewModel::refresh
                )
                FoodHubTab.RECIPES -> RecipesContent(
                    uiState = recipesUiState,
                    onSearchQueryChange = recipesViewModel::onSearchQueryChange,
                    onRecipeClick = recipesViewModel::onRecipeClick,
                    onAddRecipeClick = recipesViewModel::onAddRecipeClick,
                    onDeleteClick = recipesViewModel::onDeleteClick,
                    onDeleteConfirm = recipesViewModel::onDeleteConfirm,
                    onDeleteCancel = recipesViewModel::onDeleteCancel,
                    onRefresh = recipesViewModel::refresh
                )
                FoodHubTab.MEALS -> MealsContent(
                    onAddMealClick = { /* TODO: Implement meals */ }
                )
            }
        }

        // Tab bar at bottom
        FoodHubTabBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
    }
}

@Composable
private fun FoodHubTabBar(
    selectedTab: FoodHubTab,
    onTabSelected: (FoodHubTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FoodHubTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            val color = when (tab) {
                FoodHubTab.FOODS -> Coral
                FoodHubTab.RECIPES -> ChonkGreen
                FoodHubTab.MEALS -> Purple
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (tab) {
                        FoodHubTab.FOODS -> "Foods"
                        FoodHubTab.RECIPES -> "Recipes"
                        FoodHubTab.MEALS -> "Meals"
                    },
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodsContent(
    uiState: FoodsUiState,
    onSearchQueryChange: (String) -> Unit,
    onFilterTypeChange: (FoodFilterType) -> Unit,
    onFoodClick: (String) -> Unit,
    onAddFoodClick: () -> Unit,
    onDeleteClick: (com.chonkcheck.android.domain.model.Food) -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header with Add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Foods",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Button(
                onClick = onAddFoodClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Coral
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Text(
                    text = "+ Add",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter chips
        FoodFilterChips(
            selectedFilter = uiState.filterType,
            onFilterSelected = onFilterTypeChange
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search bar
        FoodSearchBar(
            query = uiState.searchQuery,
            onQueryChange = onSearchQueryChange,
            placeholder = "Search foods..."
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        } else if (uiState.foods.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                FoodsEmptyState(
                    message = when {
                        uiState.searchQuery.isNotEmpty() -> "No foods found"
                        uiState.filterType == FoodFilterType.USER -> "You haven't created any foods yet"
                        else -> "No foods in the database yet"
                    },
                    ctaText = "Add First Food",
                    onCtaClick = onAddFoodClick
                )
            }
        } else {
            // Foods list
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                LazyColumn {
                    items(
                        items = uiState.foods,
                        key = { it.id }
                    ) { food ->
                        FoodCard(
                            food = food,
                            onClick = { onFoodClick(food.id) },
                            onDelete = if (food.type == com.chonkcheck.android.domain.model.FoodType.USER) {
                                { onDeleteClick(food) }
                            } else null
                        )
                        if (food != uiState.foods.last()) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Attribution footer
            Text(
                text = buildAnnotatedString {
                    append("Some food data sourced from ")
                    pushStringAnnotation(tag = "URL", annotation = "https://world.openfoodfacts.org")
                    withStyle(
                        style = SpanStyle(
                            color = Coral,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("Open Food Facts")
                    }
                    pop()
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
        }
    }

    // Delete confirmation dialog
    if (uiState.deleteConfirmation != null) {
        AlertDialog(
            onDismissRequest = onDeleteCancel,
            title = {
                Text(
                    text = "Delete food?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            text = {
                Text(
                    text = "This food will be removed from your list. Your past diary entries will remain intact.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = onDeleteConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = onDeleteCancel) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipesContent(
    uiState: RecipesUiState,
    onSearchQueryChange: (String) -> Unit,
    onRecipeClick: (String) -> Unit,
    onAddRecipeClick: () -> Unit,
    onDeleteClick: (Recipe) -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header with Add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recipes",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Button(
                onClick = onAddRecipeClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChonkGreen
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Text(
                    text = "+ Add",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search bar
        RecipeSearchBar(
            query = uiState.searchQuery,
            onQueryChange = onSearchQueryChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        } else if (uiState.recipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                RecipesEmptyState(
                    onCreateRecipeClick = onAddRecipeClick,
                    isSearchResult = uiState.searchQuery.isNotEmpty()
                )
            }
        } else {
            // Recipes list
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                LazyColumn {
                    items(
                        items = uiState.recipes,
                        key = { it.id }
                    ) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            onClick = { onRecipeClick(recipe.id) },
                            onDelete = { onDeleteClick(recipe) }
                        )
                        if (recipe != uiState.recipes.last()) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
        }
    }

    // Delete confirmation dialog
    if (uiState.deleteConfirmation != null) {
        AlertDialog(
            onDismissRequest = onDeleteCancel,
            title = {
                Text(
                    text = "Delete recipe?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            text = {
                Text(
                    text = "This recipe will be removed. Your past diary entries using this recipe will remain intact.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = onDeleteConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = onDeleteCancel) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MealsContent(
    onAddMealClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header with Add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Meals",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Button(
                onClick = onAddMealClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Text(
                    text = "+ Add",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Placeholder content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Saved Meals",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Coming soon",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun FoodsScreenContent(
    uiState: FoodsUiState,
    onSearchQueryChange: (String) -> Unit,
    onFilterTypeChange: (FoodFilterType) -> Unit,
    onFoodClick: (String) -> Unit,
    onAddFoodClick: () -> Unit,
    onDeleteClick: (com.chonkcheck.android.domain.model.Food) -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    FoodsContent(
        uiState = uiState,
        onSearchQueryChange = onSearchQueryChange,
        onFilterTypeChange = onFilterTypeChange,
        onFoodClick = onFoodClick,
        onAddFoodClick = onAddFoodClick,
        onDeleteClick = onDeleteClick,
        onDeleteConfirm = onDeleteConfirm,
        onDeleteCancel = onDeleteCancel,
        onRefresh = onRefresh,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun FoodsScreenPreview() {
    ChonkCheckTheme {
        FoodsScreenContent(
            uiState = FoodsUiState(
                foods = listOf(
                    com.chonkcheck.android.domain.model.Food(
                        id = "1",
                        name = "Chicken Breast",
                        brand = "Tesco",
                        barcode = null,
                        servingSize = 100.0,
                        servingUnit = com.chonkcheck.android.domain.model.ServingUnit.GRAM,
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
                        type = com.chonkcheck.android.domain.model.FoodType.PLATFORM,
                        source = null,
                        verified = true,
                        promotionRequested = false,
                        overrideOf = null,
                        imageUrl = null,
                        createdAt = System.currentTimeMillis()
                    )
                ),
                isLoading = false
            ),
            onSearchQueryChange = {},
            onFilterTypeChange = {},
            onFoodClick = {},
            onAddFoodClick = {},
            onDeleteClick = {},
            onDeleteConfirm = {},
            onDeleteCancel = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FoodHubTabBarPreview() {
    ChonkCheckTheme {
        FoodHubTabBar(
            selectedTab = FoodHubTab.FOODS,
            onTabSelected = {}
        )
    }
}
