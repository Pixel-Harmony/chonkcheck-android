@file:OptIn(ExperimentalMaterial3Api::class)

package com.chonkcheck.android.presentation.ui.meals

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.chonkcheck.android.domain.model.Food
import com.chonkcheck.android.domain.model.Recipe
import com.chonkcheck.android.presentation.ui.components.LoadingIndicator
import com.chonkcheck.android.presentation.ui.meals.components.ItemSearchSheet
import com.chonkcheck.android.presentation.ui.meals.components.MealItemListItem
import com.chonkcheck.android.presentation.ui.meals.components.MealItemNutrition
import com.chonkcheck.android.presentation.ui.meals.components.MealNutritionSummaryCard
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.Purple

@Composable
fun SavedMealFormScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SavedMealFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()

    // Handle back gesture
    BackHandler(enabled = true) {
        viewModel.onBackPressed()
    }

    // Handle navigation events
    LaunchedEffect(event) {
        when (event) {
            is SavedMealFormEvent.NavigateBack -> {
                onNavigateBack()
                viewModel.onEventConsumed()
            }
            is SavedMealFormEvent.MealSaved -> {
                onNavigateBack()
                viewModel.onEventConsumed()
            }
            is SavedMealFormEvent.ShowError -> {
                viewModel.onEventConsumed()
            }
            null -> {}
        }
    }

    SavedMealFormScreenContent(
        uiState = uiState,
        onBackClick = viewModel::onBackPressed,
        onNameChange = viewModel::updateName,
        onAddItemClick = viewModel::showItemSearch,
        onRemoveItem = viewModel::removeItem,
        onItemQuantityChange = viewModel::updateItemQuantity,
        onItemAmountChange = viewModel::updateItemAmount,
        onToggleInputMode = viewModel::toggleInputMode,
        onSaveClick = viewModel::saveMeal,
        onDismissUnsavedChanges = viewModel::dismissUnsavedChangesDialog,
        onDiscardChanges = viewModel::discardChanges,
        onItemSearchDismiss = viewModel::hideItemSearch,
        onFoodSelected = viewModel::addFood,
        onRecipeSelected = viewModel::addRecipe,
        onSearchItems = viewModel::searchItems,
        modifier = modifier
    )
}

@Composable
fun SavedMealFormScreenContent(
    uiState: SavedMealFormUiState,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onAddItemClick: () -> Unit,
    onRemoveItem: (Int) -> Unit,
    onItemQuantityChange: (Int, String) -> Unit,
    onItemAmountChange: (Int, String) -> Unit,
    onToggleInputMode: (Int) -> Unit,
    onSaveClick: () -> Unit,
    onDismissUnsavedChanges: () -> Unit,
    onDiscardChanges: () -> Unit,
    onItemSearchDismiss: () -> Unit,
    onFoodSelected: (Food) -> Unit,
    onRecipeSelected: (Recipe) -> Unit,
    onSearchItems: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingIndicator()
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = if (uiState.isEditMode) "Edit Meal" else "Create Meal",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Error message
            if (uiState.error != null) {
                Text(
                    text = uiState.error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Meal Name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text("Meal Name*") },
                placeholder = { Text("e.g., My Usual Breakfast") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Save a combination of foods and recipes to log together",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Items section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Items",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                TextButton(
                    onClick = onAddItemClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Purple,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Add",
                        color = Purple
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.items.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "No items added yet. Tap \"Add\" to search for foods and recipes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        uiState.items.forEachIndexed { index, item ->
                            MealItemListItem(
                                itemName = item.itemName,
                                itemType = item.itemType,
                                servingSize = item.servingSize,
                                servingUnitName = item.servingUnitName,
                                inputMode = item.inputMode,
                                quantity = item.quantity,
                                enteredAmount = item.enteredAmount,
                                nutrition = item.calculatedNutrition,
                                onQuantityChange = { onItemQuantityChange(index, it) },
                                onAmountChange = { onItemAmountChange(index, it) },
                                onToggleInputMode = { onToggleInputMode(index) },
                                onDelete = { onRemoveItem(index) }
                            )
                            if (index < uiState.items.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            // Nutrition summary (only show when there are items)
            if (uiState.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                MealNutritionSummaryCard(
                    totalNutrition = uiState.totalNutrition
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Save button
        Button(
            onClick = onSaveClick,
            enabled = !uiState.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple
            )
        ) {
            Text(
                text = when {
                    uiState.isSaving -> "Saving..."
                    uiState.isEditMode -> "Update Meal"
                    else -> "Save Meal"
                },
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Unsaved changes dialog
    if (uiState.showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = onDismissUnsavedChanges,
            title = {
                Text(
                    text = "Unsaved changes",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            text = {
                Text(
                    text = "You have unsaved changes. Are you sure you want to go back?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = onDiscardChanges,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissUnsavedChanges) {
                    Text("Keep Editing")
                }
            }
        )
    }

    // Item search sheet
    if (uiState.showItemSearch) {
        ItemSearchSheet(
            foods = uiState.searchFoods,
            recipes = uiState.searchRecipes,
            isLoading = uiState.isSearchingItems,
            onSearchQueryChange = onSearchItems,
            onFoodSelected = onFoodSelected,
            onRecipeSelected = onRecipeSelected,
            onDismiss = onItemSearchDismiss
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SavedMealFormScreenCreatePreview() {
    ChonkCheckTheme {
        SavedMealFormScreenContent(
            uiState = SavedMealFormUiState(
                isLoading = false,
                isEditMode = false
            ),
            onBackClick = {},
            onNameChange = {},
            onAddItemClick = {},
            onRemoveItem = {},
            onItemQuantityChange = { _, _ -> },
            onItemAmountChange = { _, _ -> },
            onToggleInputMode = {},
            onSaveClick = {},
            onDismissUnsavedChanges = {},
            onDiscardChanges = {},
            onItemSearchDismiss = {},
            onFoodSelected = {},
            onRecipeSelected = {},
            onSearchItems = {}
        )
    }
}
