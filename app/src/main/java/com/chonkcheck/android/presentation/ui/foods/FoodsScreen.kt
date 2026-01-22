package com.chonkcheck.android.presentation.ui.foods

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chonkcheck.android.domain.model.FoodFilterType
import com.chonkcheck.android.presentation.ui.components.LoadingIndicator
import com.chonkcheck.android.presentation.ui.foods.components.FoodCard
import com.chonkcheck.android.presentation.ui.foods.components.FoodFilterChips
import com.chonkcheck.android.presentation.ui.foods.components.FoodSearchBar
import com.chonkcheck.android.presentation.ui.foods.components.FoodsEmptyState
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.Coral

@Composable
fun FoodsScreen(
    onNavigateToEditFood: (String) -> Unit,
    onNavigateToCreateFood: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FoodsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()

    // Handle navigation events
    LaunchedEffect(event) {
        when (event) {
            is FoodsEvent.NavigateToEditFood -> {
                onNavigateToEditFood((event as FoodsEvent.NavigateToEditFood).foodId)
                viewModel.onEventConsumed()
            }
            is FoodsEvent.NavigateToCreateFood -> {
                onNavigateToCreateFood()
                viewModel.onEventConsumed()
            }
            is FoodsEvent.FoodDeleted -> {
                viewModel.onEventConsumed()
            }
            is FoodsEvent.ShowError -> {
                // Could show snackbar here
                viewModel.onEventConsumed()
            }
            null -> {}
        }
    }

    FoodsScreenContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onFilterTypeChange = viewModel::onFilterTypeChange,
        onFoodClick = viewModel::onFoodClick,
        onAddFoodClick = viewModel::onAddFoodClick,
        onDeleteClick = viewModel::onDeleteClick,
        onDeleteConfirm = viewModel::onDeleteConfirm,
        onDeleteCancel = viewModel::onDeleteCancel,
        modifier = modifier
    )
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
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

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
private fun FoodsScreenEmptyPreview() {
    ChonkCheckTheme {
        FoodsScreenContent(
            uiState = FoodsUiState(
                foods = emptyList(),
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
