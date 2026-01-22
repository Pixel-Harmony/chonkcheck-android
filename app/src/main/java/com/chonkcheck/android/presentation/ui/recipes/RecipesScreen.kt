package com.chonkcheck.android.presentation.ui.recipes

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chonkcheck.android.domain.model.Recipe
import com.chonkcheck.android.domain.model.RecipeServingUnit
import com.chonkcheck.android.presentation.ui.components.LoadingIndicator
import com.chonkcheck.android.presentation.ui.recipes.components.RecipeCard
import com.chonkcheck.android.presentation.ui.recipes.components.RecipeSearchBar
import com.chonkcheck.android.presentation.ui.recipes.components.RecipesEmptyState
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen

@Composable
fun RecipesScreen(
    onNavigateToCreateRecipe: () -> Unit,
    onNavigateToEditRecipe: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecipesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()

    // Handle navigation events
    LaunchedEffect(event) {
        when (event) {
            is RecipesEvent.NavigateToEditRecipe -> {
                onNavigateToEditRecipe((event as RecipesEvent.NavigateToEditRecipe).recipeId)
                viewModel.onEventConsumed()
            }
            is RecipesEvent.NavigateToCreateRecipe -> {
                onNavigateToCreateRecipe()
                viewModel.onEventConsumed()
            }
            is RecipesEvent.RecipeDeleted -> {
                viewModel.onEventConsumed()
            }
            is RecipesEvent.ShowError -> {
                // Could show snackbar here
                viewModel.onEventConsumed()
            }
            null -> {}
        }
    }

    RecipesScreenContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onRecipeClick = viewModel::onRecipeClick,
        onAddRecipeClick = viewModel::onAddRecipeClick,
        onDeleteClick = viewModel::onDeleteClick,
        onDeleteConfirm = viewModel::onDeleteConfirm,
        onDeleteCancel = viewModel::onDeleteCancel,
        modifier = modifier
    )
}

@Composable
fun RecipesScreenContent(
    uiState: RecipesUiState,
    onSearchQueryChange: (String) -> Unit,
    onRecipeClick: (String) -> Unit,
    onAddRecipeClick: () -> Unit,
    onDeleteClick: (Recipe) -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
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

@Preview(showBackground = true)
@Composable
private fun RecipesScreenPreview() {
    ChonkCheckTheme {
        RecipesScreenContent(
            uiState = RecipesUiState(
                recipes = listOf(
                    Recipe(
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
                    Recipe(
                        id = "2",
                        userId = "user1",
                        name = "Overnight Oats",
                        description = null,
                        totalServings = 2,
                        servingUnit = RecipeServingUnit.BOWL,
                        ingredients = emptyList(),
                        totalCalories = 600.0,
                        totalProtein = 20.0,
                        totalCarbs = 100.0,
                        totalFat = 15.0,
                        caloriesPerServing = 300.0,
                        proteinPerServing = 10.0,
                        carbsPerServing = 50.0,
                        fatPerServing = 7.5,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                ),
                isLoading = false
            ),
            onSearchQueryChange = {},
            onRecipeClick = {},
            onAddRecipeClick = {},
            onDeleteClick = {},
            onDeleteConfirm = {},
            onDeleteCancel = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RecipesScreenEmptyPreview() {
    ChonkCheckTheme {
        RecipesScreenContent(
            uiState = RecipesUiState(
                recipes = emptyList(),
                isLoading = false
            ),
            onSearchQueryChange = {},
            onRecipeClick = {},
            onAddRecipeClick = {},
            onDeleteClick = {},
            onDeleteConfirm = {},
            onDeleteCancel = {}
        )
    }
}
