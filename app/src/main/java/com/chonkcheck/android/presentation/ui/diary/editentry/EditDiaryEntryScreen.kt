package com.chonkcheck.android.presentation.ui.diary.editentry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.chonkcheck.android.domain.model.DiaryEntry
import com.chonkcheck.android.domain.model.DiaryItemType
import com.chonkcheck.android.domain.model.MealType
import com.chonkcheck.android.domain.model.ServingUnit
import com.chonkcheck.android.presentation.ui.components.LoadingIndicator
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen
import java.time.LocalDate
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDiaryEntryScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditDiaryEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()

    LaunchedEffect(event) {
        when (event) {
            is EditDiaryEntryEvent.EntrySaved -> {
                onNavigateBack()
                viewModel.onEventConsumed()
            }
            is EditDiaryEntryEvent.EntryDeleted -> {
                onNavigateBack()
                viewModel.onEventConsumed()
            }
            is EditDiaryEntryEvent.ShowError -> {
                // Could show snackbar
                viewModel.onEventConsumed()
            }
            null -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Entry") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (uiState.entry != null) {
                        IconButton(onClick = viewModel::onDeleteClick) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
            uiState.entry == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: "Entry not found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {
                EditEntryContent(
                    uiState = uiState,
                    onServingSizeChange = viewModel::onServingSizeChange,
                    onNumberOfServingsChange = viewModel::onNumberOfServingsChange,
                    onMealTypeChange = viewModel::onMealTypeChange,
                    onSave = viewModel::onSave,
                    onDelete = viewModel::onDeleteClick,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    // Delete confirmation dialog
    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::onDeleteCancel,
            title = {
                Text(
                    text = "Delete entry?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            text = {
                Text(
                    text = "\"${uiState.entry?.name}\" will be removed from your diary.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::onDeleteConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDeleteCancel) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditEntryContent(
    uiState: EditDiaryEntryUiState,
    onServingSizeChange: (String) -> Unit,
    onNumberOfServingsChange: (String) -> Unit,
    onMealTypeChange: (MealType) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val entry = uiState.entry ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        // Food info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                if (!entry.brand.isNullOrBlank()) {
                    Text(
                        text = entry.brand,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = if (entry.itemType == DiaryItemType.RECIPE) "Recipe" else "Food",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Meal type selector
        var mealDropdownExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = mealDropdownExpanded,
            onExpandedChange = { mealDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = uiState.mealType.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Meal") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = mealDropdownExpanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )

            ExposedDropdownMenu(
                expanded = mealDropdownExpanded,
                onDismissRequest = { mealDropdownExpanded = false }
            ) {
                MealType.entries.forEach { mealType ->
                    DropdownMenuItem(
                        text = { Text(mealType.displayName) },
                        onClick = {
                            onMealTypeChange(mealType)
                            mealDropdownExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Serving inputs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.servingSize.formatServing(),
                onValueChange = onServingSizeChange,
                label = { Text("Serving size") },
                suffix = { Text(uiState.servingUnit.displayName) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = uiState.numberOfServings.formatServing(),
                onValueChange = onNumberOfServingsChange,
                label = { Text("Servings") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Nutrition preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Nutrition",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    NutritionItem("Calories", "${uiState.calculatedCalories.roundToInt()}", "cal")
                    NutritionItem("Protein", "${uiState.calculatedProtein.roundToInt()}", "g")
                    NutritionItem("Carbs", "${uiState.calculatedCarbs.roundToInt()}", "g")
                    NutritionItem("Fat", "${uiState.calculatedFat.roundToInt()}", "g")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Save button
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ChonkGreen
            ),
            enabled = !uiState.isSaving && !uiState.isDeleting
        ) {
            Text(
                text = if (uiState.isSaving) "Saving..." else "Save Changes",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Delete button
        OutlinedButton(
            onClick = onDelete,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            enabled = !uiState.isSaving && !uiState.isDeleting
        ) {
            Text(
                text = if (uiState.isDeleting) "Deleting..." else "Delete Entry",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun NutritionItem(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "$label ($unit)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun Double.formatServing(): String {
    return if (this == this.toLong().toDouble()) {
        this.toLong().toString()
    } else {
        String.format("%.1f", this)
    }
}

@Preview(showBackground = true)
@Composable
private fun EditEntryContentPreview() {
    ChonkCheckTheme {
        EditEntryContent(
            uiState = EditDiaryEntryUiState(
                isLoading = false,
                entry = DiaryEntry(
                    id = "1",
                    userId = "user1",
                    date = LocalDate.now(),
                    mealType = MealType.BREAKFAST,
                    foodId = "food1",
                    recipeId = null,
                    servingSize = 100.0,
                    servingUnit = ServingUnit.GRAM,
                    numberOfServings = 1.5,
                    calories = 247.5,
                    protein = 46.5,
                    carbs = 0.0,
                    fat = 5.4,
                    name = "Chicken Breast",
                    brand = "Tesco",
                    createdAt = System.currentTimeMillis(),
                    itemType = DiaryItemType.FOOD
                ),
                servingSize = 100.0,
                servingUnit = ServingUnit.GRAM,
                numberOfServings = 1.5,
                mealType = MealType.BREAKFAST,
                calculatedCalories = 247.5,
                calculatedProtein = 46.5,
                calculatedCarbs = 0.0,
                calculatedFat = 5.4
            ),
            onServingSizeChange = {},
            onNumberOfServingsChange = {},
            onMealTypeChange = {},
            onSave = {},
            onDelete = {}
        )
    }
}
