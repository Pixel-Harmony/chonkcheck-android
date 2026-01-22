package com.chonkcheck.android.presentation.ui.recipes

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chonkcheck.android.domain.model.Food
import com.chonkcheck.android.domain.model.RecipeServingUnit
import com.chonkcheck.android.presentation.ui.components.LoadingIndicator
import com.chonkcheck.android.presentation.ui.recipes.components.IngredientListItem
import com.chonkcheck.android.presentation.ui.recipes.components.IngredientSearchSheet
import com.chonkcheck.android.presentation.ui.recipes.components.NutritionSummaryCard
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen

@Composable
fun RecipeFormScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecipeFormViewModel = hiltViewModel()
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
            is RecipeFormEvent.NavigateBack -> {
                onNavigateBack()
                viewModel.onEventConsumed()
            }
            is RecipeFormEvent.RecipeSaved -> {
                onNavigateBack()
                viewModel.onEventConsumed()
            }
            is RecipeFormEvent.ShowError -> {
                viewModel.onEventConsumed()
            }
            null -> {}
        }
    }

    RecipeFormScreenContent(
        uiState = uiState,
        onBackClick = viewModel::onBackPressed,
        onNameChange = viewModel::updateName,
        onDescriptionChange = viewModel::updateDescription,
        onTotalServingsChange = viewModel::updateTotalServings,
        onServingUnitChange = viewModel::updateServingUnit,
        onAddIngredientClick = viewModel::showIngredientSearch,
        onRemoveIngredient = viewModel::removeIngredient,
        onIngredientQuantityChange = viewModel::updateIngredientQuantity,
        onIngredientAmountChange = viewModel::updateIngredientAmount,
        onToggleInputMode = viewModel::toggleInputMode,
        onSaveClick = viewModel::saveRecipe,
        onDismissUnsavedChanges = viewModel::dismissUnsavedChangesDialog,
        onDiscardChanges = viewModel::discardChanges,
        onIngredientSearchDismiss = viewModel::hideIngredientSearch,
        onIngredientSelected = viewModel::addIngredient,
        onSearchFoods = viewModel::searchFoods,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeFormScreenContent(
    uiState: RecipeFormUiState,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTotalServingsChange: (String) -> Unit,
    onServingUnitChange: (RecipeServingUnit) -> Unit,
    onAddIngredientClick: () -> Unit,
    onRemoveIngredient: (Int) -> Unit,
    onIngredientQuantityChange: (Int, String) -> Unit,
    onIngredientAmountChange: (Int, String) -> Unit,
    onToggleInputMode: (Int) -> Unit,
    onSaveClick: () -> Unit,
    onDismissUnsavedChanges: () -> Unit,
    onDiscardChanges: () -> Unit,
    onIngredientSearchDismiss: () -> Unit,
    onIngredientSelected: (Food) -> Unit,
    onSearchFoods: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var servingUnitExpanded by remember { mutableStateOf(false) }

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
                text = if (uiState.isEditMode) "Edit Recipe" else "Create Recipe",
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

            // Recipe Name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text("Recipe Name*") },
                placeholder = { Text("e.g., Chicken Stir Fry") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ChonkGreen,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = { Text("Description") },
                placeholder = { Text("Optional notes about this recipe") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ChonkGreen,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Total Servings and Serving Unit row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total Servings
                OutlinedTextField(
                    value = uiState.totalServings,
                    onValueChange = onTotalServingsChange,
                    label = { Text("Total Servings*") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChonkGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                // Serving Unit dropdown
                ExposedDropdownMenuBox(
                    expanded = servingUnitExpanded,
                    onExpandedChange = { servingUnitExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = uiState.servingUnit.displayName.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Serving Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = servingUnitExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChonkGreen,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = servingUnitExpanded,
                        onDismissRequest = { servingUnitExpanded = false }
                    ) {
                        RecipeServingUnit.entries.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit.displayName.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    onServingUnitChange(unit)
                                    servingUnitExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Ingredients section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ingredients",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                TextButton(
                    onClick = onAddIngredientClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = ChonkGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Add",
                        color = ChonkGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.ingredients.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "No ingredients added yet. Tap \"Add\" to search for foods.",
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
                        uiState.ingredients.forEachIndexed { index, ingredient ->
                            IngredientListItem(
                                food = ingredient.food,
                                inputMode = ingredient.inputMode,
                                quantity = ingredient.quantity,
                                enteredAmount = ingredient.enteredAmount,
                                nutrition = ingredient.calculatedNutrition,
                                onQuantityChange = { onIngredientQuantityChange(index, it) },
                                onAmountChange = { onIngredientAmountChange(index, it) },
                                onToggleInputMode = { onToggleInputMode(index) },
                                onDelete = { onRemoveIngredient(index) }
                            )
                            if (index < uiState.ingredients.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            // Nutrition summary (only show when there are ingredients)
            if (uiState.ingredients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                NutritionSummaryCard(
                    totalNutrition = uiState.totalNutrition,
                    perServingNutrition = uiState.perServingNutrition,
                    servingUnit = uiState.servingUnit
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
                containerColor = ChonkGreen
            )
        ) {
            Text(
                text = when {
                    uiState.isSaving -> "Saving..."
                    uiState.isEditMode -> "Update Recipe"
                    else -> "Save Recipe"
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

    // Ingredient search sheet
    if (uiState.showIngredientSearch) {
        IngredientSearchSheet(
            foods = uiState.searchFoods,
            isLoading = uiState.isSearchingFoods,
            onSearchQueryChange = onSearchFoods,
            onFoodSelected = onIngredientSelected,
            onDismiss = onIngredientSearchDismiss
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RecipeFormScreenCreatePreview() {
    ChonkCheckTheme {
        RecipeFormScreenContent(
            uiState = RecipeFormUiState(
                isLoading = false,
                isEditMode = false
            ),
            onBackClick = {},
            onNameChange = {},
            onDescriptionChange = {},
            onTotalServingsChange = {},
            onServingUnitChange = {},
            onAddIngredientClick = {},
            onRemoveIngredient = {},
            onIngredientQuantityChange = { _, _ -> },
            onIngredientAmountChange = { _, _ -> },
            onToggleInputMode = {},
            onSaveClick = {},
            onDismissUnsavedChanges = {},
            onDiscardChanges = {},
            onIngredientSearchDismiss = {},
            onIngredientSelected = {},
            onSearchFoods = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RecipeFormScreenEditPreview() {
    ChonkCheckTheme {
        RecipeFormScreenContent(
            uiState = RecipeFormUiState(
                isLoading = false,
                isEditMode = true,
                name = "Chicken Stir Fry",
                description = "A quick and healthy meal",
                totalServings = "4",
                servingUnit = RecipeServingUnit.SERVING
            ),
            onBackClick = {},
            onNameChange = {},
            onDescriptionChange = {},
            onTotalServingsChange = {},
            onServingUnitChange = {},
            onAddIngredientClick = {},
            onRemoveIngredient = {},
            onIngredientQuantityChange = { _, _ -> },
            onIngredientAmountChange = { _, _ -> },
            onToggleInputMode = {},
            onSaveClick = {},
            onDismissUnsavedChanges = {},
            onDiscardChanges = {},
            onIngredientSearchDismiss = {},
            onIngredientSelected = {},
            onSearchFoods = {}
        )
    }
}
