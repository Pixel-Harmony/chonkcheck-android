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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chonkcheck.android.domain.model.FoodType
import com.chonkcheck.android.domain.model.NutritionLabelData
import com.chonkcheck.android.domain.model.ServingUnit
import com.chonkcheck.android.presentation.ui.components.LoadingIndicator
import com.chonkcheck.android.ui.theme.Amber
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.Coral

@Composable
fun FoodFormScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateOverride: (String) -> Unit,
    onNavigateToBarcodeScanner: () -> Unit,
    onNavigateToLabelScanner: () -> Unit,
    scannedBarcode: String? = null,
    scannedLabelData: NutritionLabelData? = null,
    modifier: Modifier = Modifier,
    viewModel: FoodFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()

    // Process scanned barcode when returning from scanner
    LaunchedEffect(scannedBarcode) {
        scannedBarcode?.let { barcode ->
            if (barcode.isNotBlank()) {
                viewModel.onBarcodeScanned(barcode)
            }
        }
    }

    // Process scanned nutrition label data when returning from scanner
    LaunchedEffect(scannedLabelData) {
        scannedLabelData?.let { data ->
            viewModel.onNutritionLabelScanned(
                name = data.name,
                brand = data.brand,
                servingSize = data.servingSize,
                servingUnit = data.servingUnit,
                calories = data.calories,
                protein = data.protein,
                carbs = data.carbs,
                fat = data.fat,
                fiber = data.fiber,
                sugar = data.sugar,
                sodium = data.sodium
            )
        }
    }

    LaunchedEffect(event) {
        when (event) {
            is FoodFormEvent.NavigateBack -> {
                onNavigateBack()
                viewModel.onEventConsumed()
            }
            is FoodFormEvent.FoodSaved -> {
                onNavigateBack()
                viewModel.onEventConsumed()
            }
            is FoodFormEvent.NavigateToCreateOverride -> {
                onNavigateToCreateOverride((event as FoodFormEvent.NavigateToCreateOverride).foodId)
                viewModel.onEventConsumed()
            }
            is FoodFormEvent.ShowError -> {
                viewModel.onEventConsumed()
            }
            is FoodFormEvent.NavigateToBarcodeScanner -> {
                onNavigateToBarcodeScanner()
                viewModel.onEventConsumed()
            }
            is FoodFormEvent.NavigateToLabelScanner -> {
                onNavigateToLabelScanner()
                viewModel.onEventConsumed()
            }
            null -> {}
        }
    }

    FoodFormScreenContent(
        uiState = uiState,
        onBackPressed = viewModel::onBackPressed,
        onNameChange = viewModel::updateName,
        onBrandChange = viewModel::updateBrand,
        onBarcodeChange = viewModel::updateBarcode,
        onServingSizeChange = viewModel::updateServingSize,
        onServingUnitChange = viewModel::updateServingUnit,
        onCaloriesChange = viewModel::updateCalories,
        onProteinChange = viewModel::updateProtein,
        onCarbsChange = viewModel::updateCarbs,
        onFatChange = viewModel::updateFat,
        onFiberChange = viewModel::updateFiber,
        onSugarChange = viewModel::updateSugar,
        onSodiumChange = viewModel::updateSodium,
        onSave = viewModel::saveFood,
        onRequestPromotion = viewModel::requestPromotion,
        onCreateOverride = viewModel::createOverride,
        onScanBarcode = onNavigateToBarcodeScanner,
        onScanLabel = onNavigateToLabelScanner,
        onDismissUnsavedChangesDialog = viewModel::dismissUnsavedChangesDialog,
        onDiscardChanges = viewModel::discardChanges,
        onClearError = viewModel::clearError,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodFormScreenContent(
    uiState: FoodFormUiState,
    onBackPressed: () -> Unit,
    onNameChange: (String) -> Unit,
    onBrandChange: (String) -> Unit,
    onBarcodeChange: (String) -> Unit,
    onServingSizeChange: (String) -> Unit,
    onServingUnitChange: (ServingUnit) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onProteinChange: (String) -> Unit,
    onCarbsChange: (String) -> Unit,
    onFatChange: (String) -> Unit,
    onFiberChange: (String) -> Unit,
    onSugarChange: (String) -> Unit,
    onSodiumChange: (String) -> Unit,
    onSave: () -> Unit,
    onRequestPromotion: () -> Unit,
    onCreateOverride: () -> Unit,
    onScanBarcode: () -> Unit,
    onScanLabel: () -> Unit,
    onDismissUnsavedChangesDialog: () -> Unit,
    onDiscardChanges: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isReadOnly = (uiState.mode as? FoodFormMode.Edit)?.isReadOnly == true
    val isEditMode = uiState.mode is FoodFormMode.Edit
    val food = uiState.food
    val scrollState = rememberScrollState()

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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when {
                    isReadOnly -> "View Food"
                    isEditMode -> "Edit Food"
                    else -> "Add Food"
                },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            TextButton(onClick = onBackPressed) {
                Text(
                    text = "Back",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions (only show in Create mode)
            if (uiState.mode is FoodFormMode.Create) {
                QuickActionCards(
                    onScanBarcode = onScanBarcode,
                    onScanLabel = onScanLabel,
                    isLoading = uiState.isLookingUpBarcode
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Barcode lookup loading indicator
            if (uiState.isLookingUpBarcode) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Looking up barcode in database...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Scanned from Open Food Facts notice
            if (uiState.scannedFromOpenFoodFacts) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFCCFBF1)
                    )
                ) {
                    Text(
                        text = "Product details loaded from Open Food Facts. Review and edit as needed before saving.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF0D9488)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Platform food notice
            if (isReadOnly) {
                InfoCard(
                    icon = Icons.Default.Info,
                    iconColor = Color(0xFF2563EB),
                    backgroundColor = Color(0xFFDBEAFE),
                    text = "This is a platform food and cannot be edited."
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Create override option for platform foods
            if (isReadOnly && food != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFEF3C7)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Disagree with these nutrition facts? Create your own version.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFB45309)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onCreateOverride,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Amber
                            )
                        ) {
                            Text("Create My Version")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Submit to ChonkCheck (for user foods that haven't been submitted)
            if (!isReadOnly && food != null && food.type == FoodType.USER && !food.promotionRequested) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFDBEAFE)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (food.overrideOf != null) {
                                "Think your corrections should replace the platform version? Submit as a revision for review."
                            } else {
                                "Want to share this food with all ChonkCheck users? Submit it for review."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1D4ED8)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onRequestPromotion,
                            enabled = !uiState.isRequestingPromotion,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB)
                            )
                        ) {
                            Text(
                                if (uiState.isRequestingPromotion) "Submitting..."
                                else if (food.overrideOf != null) "Submit Revision"
                                else "Submit to ChonkCheck"
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Promotion requested status
            if (!isReadOnly && food != null && food.promotionRequested) {
                InfoCard(
                    icon = Icons.Default.AccessTime,
                    iconColor = Color(0xFFCA8A04),
                    backgroundColor = Color(0xFFFEF3C7),
                    text = if (food.overrideOf != null) "Revision submitted for review" else "Submitted for review"
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Error message
                    if (uiState.error != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = uiState.error,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Name field
                    FormTextField(
                        label = "Name *",
                        value = uiState.formState.name,
                        onValueChange = onNameChange,
                        placeholder = "e.g., Chicken Breast",
                        enabled = !isReadOnly
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Brand and Barcode row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FormTextField(
                            label = "Brand",
                            value = uiState.formState.brand,
                            onValueChange = onBrandChange,
                            placeholder = "Optional",
                            enabled = !isReadOnly,
                            modifier = Modifier.weight(1f)
                        )
                        FormTextField(
                            label = "Barcode",
                            value = uiState.formState.barcode,
                            onValueChange = onBarcodeChange,
                            placeholder = "Optional",
                            enabled = !isReadOnly,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Serving Size and Unit row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FormTextField(
                            label = "Serving Size *",
                            value = uiState.formState.servingSize,
                            onValueChange = onServingSizeChange,
                            placeholder = "100",
                            enabled = !isReadOnly,
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f)
                        )

                        ServingUnitDropdown(
                            selectedUnit = uiState.formState.servingUnit,
                            onUnitSelected = onServingUnitChange,
                            enabled = !isReadOnly,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Nutrition section
                    Text(
                        text = "Nutrition (per serving)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Calories and Protein row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FormTextField(
                            label = "Calories *",
                            value = uiState.formState.calories,
                            onValueChange = onCaloriesChange,
                            placeholder = "0",
                            enabled = !isReadOnly,
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                        FormTextField(
                            label = "Protein (g) *",
                            value = uiState.formState.protein,
                            onValueChange = onProteinChange,
                            placeholder = "0",
                            enabled = !isReadOnly,
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Carbs and Fat row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FormTextField(
                            label = "Carbs (g) *",
                            value = uiState.formState.carbs,
                            onValueChange = onCarbsChange,
                            placeholder = "0",
                            enabled = !isReadOnly,
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f)
                        )
                        FormTextField(
                            label = "Fat (g) *",
                            value = uiState.formState.fat,
                            onValueChange = onFatChange,
                            placeholder = "0",
                            enabled = !isReadOnly,
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Optional fields
                    Text(
                        text = "Optional",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Fiber, Sugar, Sodium row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FormTextField(
                            label = "Fiber (g)",
                            value = uiState.formState.fiber,
                            onValueChange = onFiberChange,
                            placeholder = "",
                            enabled = !isReadOnly,
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f)
                        )
                        FormTextField(
                            label = "Sugar (g)",
                            value = uiState.formState.sugar,
                            onValueChange = onSugarChange,
                            placeholder = "",
                            enabled = !isReadOnly,
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f)
                        )
                        FormTextField(
                            label = "Sodium (mg)",
                            value = uiState.formState.sodium,
                            onValueChange = onSodiumChange,
                            placeholder = "",
                            enabled = !isReadOnly,
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Save button (only for editable foods)
                    if (!isReadOnly) {
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onSave,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = !uiState.isSaving,
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
                                text = if (uiState.isSaving) "Saving..."
                                else if (isEditMode) "Save Changes"
                                else "Save Food",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Unsaved changes dialog
    if (uiState.showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = onDismissUnsavedChangesDialog,
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
                TextButton(onClick = onDismissUnsavedChangesDialog) {
                    Text("Stay")
                }
            }
        )
    }
}

@Composable
private fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            ),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServingUnitDropdown(
    selectedUnit: ServingUnit,
    onUnitSelected: (ServingUnit) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = "Unit *",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))

        ExposedDropdownMenuBox(
            expanded = expanded && enabled,
            onExpandedChange = { if (enabled) expanded = it }
        ) {
            OutlinedTextField(
                value = getUnitDisplayName(selectedUnit),
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
            )

            ExposedDropdownMenu(
                expanded = expanded && enabled,
                onDismissRequest = { expanded = false }
            ) {
                ServingUnit.entries.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(getUnitDisplayName(unit)) },
                        onClick = {
                            onUnitSelected(unit)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

private fun getUnitDisplayName(unit: ServingUnit): String {
    return when (unit) {
        ServingUnit.GRAM -> "grams (g)"
        ServingUnit.MILLILITER -> "milliliters (ml)"
        ServingUnit.OUNCE -> "ounces (oz)"
        ServingUnit.CUP -> "cup"
        ServingUnit.TABLESPOON -> "tablespoon"
        ServingUnit.TEASPOON -> "teaspoon"
        ServingUnit.PIECE -> "piece"
        ServingUnit.SLICE -> "slice"
        ServingUnit.SERVING -> "serving"
    }
}

@Composable
private fun QuickActionCards(
    onScanBarcode: () -> Unit,
    onScanLabel: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Scan Barcode Card
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = !isLoading) { onScanBarcode() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    painter = rememberBarcodeIcon(),
                    contentDescription = null,
                    tint = Coral,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Scan Barcode",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Add product barcode",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Scan Label Card
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = !isLoading) { onScanLabel() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    painter = rememberCameraIcon(),
                    contentDescription = null,
                    tint = Coral,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Scan Label",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Photo of nutrition info",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun rememberBarcodeIcon(): androidx.compose.ui.graphics.painter.Painter {
    return androidx.compose.ui.graphics.vector.rememberVectorPainter(
        image = androidx.compose.ui.graphics.vector.ImageVector.Builder(
            name = "Barcode",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            addPath(
                pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString(
                    "M12 4v1M18 15h2M12 15h-2v4M12 11v3M12 11h0.01M12 12h4.01M16 20h4M4 12h4M20 12h0.01M5 8h2a1 1 0 001-1V5a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1zM17 8h2a1 1 0 001-1V5a1 1 0 00-1-1h-2a1 1 0 00-1 1v2a1 1 0 001 1zM5 20h2a1 1 0 001-1v-2a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1z"
                ).toNodes(),
                fill = null,
                stroke = androidx.compose.ui.graphics.SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        }.build()
    )
}

@Composable
private fun rememberCameraIcon(): androidx.compose.ui.graphics.painter.Painter {
    return androidx.compose.ui.graphics.vector.rememberVectorPainter(
        image = androidx.compose.ui.graphics.vector.ImageVector.Builder(
            name = "Camera",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            addPath(
                pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString(
                    "M3 9a2 2 0 012-2h0.93a2 2 0 001.664-0.89l0.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664 0.89l0.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"
                ).toNodes(),
                fill = null,
                stroke = androidx.compose.ui.graphics.SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
            )
            addPath(
                pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString(
                    "M15 13a3 3 0 11-6 0 3 3 0 016 0z"
                ).toNodes(),
                fill = null,
                stroke = androidx.compose.ui.graphics.SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
                strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        }.build()
    )
}

@Composable
private fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    backgroundColor: Color,
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = iconColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FoodFormScreenCreatePreview() {
    ChonkCheckTheme {
        FoodFormScreenContent(
            uiState = FoodFormUiState(
                isLoading = false,
                mode = FoodFormMode.Create
            ),
            onBackPressed = {},
            onNameChange = {},
            onBrandChange = {},
            onBarcodeChange = {},
            onServingSizeChange = {},
            onServingUnitChange = {},
            onCaloriesChange = {},
            onProteinChange = {},
            onCarbsChange = {},
            onFatChange = {},
            onFiberChange = {},
            onSugarChange = {},
            onSodiumChange = {},
            onSave = {},
            onRequestPromotion = {},
            onCreateOverride = {},
            onScanBarcode = {},
            onScanLabel = {},
            onDismissUnsavedChangesDialog = {},
            onDiscardChanges = {},
            onClearError = {}
        )
    }
}
