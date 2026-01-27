package com.chonkcheck.android.presentation.ui.diary.addentry

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chonkcheck.android.domain.model.Food
import com.chonkcheck.android.domain.model.FoodType
import com.chonkcheck.android.domain.model.MealType
import com.chonkcheck.android.domain.model.Recipe
import com.chonkcheck.android.domain.model.SavedMeal
import com.chonkcheck.android.domain.model.ServingUnit
import com.chonkcheck.android.presentation.ui.components.LoadingIndicator
import com.chonkcheck.android.presentation.ui.foods.components.FoodSearchBar
import com.chonkcheck.android.presentation.ui.meals.components.SavedMealCard
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen
import com.chonkcheck.android.ui.theme.Coral
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import java.time.LocalDate
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDiaryEntryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBarcodeScanner: () -> Unit,
    onFoodAdded: () -> Unit,
    onNavigateToMealPreview: (savedMealId: String, date: String, mealType: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddDiaryEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()

    // Handle events
    LaunchedEffect(event) {
        when (event) {
            is AddDiaryEntryEvent.EntrySaved -> {
                onFoodAdded()
                viewModel.onEventConsumed()
            }
            is AddDiaryEntryEvent.NavigateBack -> {
                onNavigateBack()
                viewModel.onEventConsumed()
            }
            is AddDiaryEntryEvent.NavigateToMealPreview -> {
                val e = event as AddDiaryEntryEvent.NavigateToMealPreview
                onNavigateToMealPreview(e.savedMealId, e.date, e.mealType)
                viewModel.onEventConsumed()
            }
            is AddDiaryEntryEvent.ShowError -> {
                // Could show snackbar
                viewModel.onEventConsumed()
            }
            null -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (uiState.phase) {
                            AddEntryPhase.SEARCH -> "Add Food"
                            AddEntryPhase.DETAILS -> "Set Quantity"
                            AddEntryPhase.SAVING -> "Saving..."
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when (uiState.phase) {
                            AddEntryPhase.SEARCH -> onNavigateBack()
                            AddEntryPhase.DETAILS -> viewModel.onBackToSearch()
                            AddEntryPhase.SAVING -> { /* Ignore during save */ }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        when (uiState.phase) {
            AddEntryPhase.SEARCH -> SearchPhaseContent(
                uiState = uiState,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onFoodSelected = viewModel::onFoodSelected,
                onRecipeSelected = viewModel::onRecipeSelected,
                onMealSelected = viewModel::onMealSelected,
                modifier = Modifier.padding(innerPadding)
            )
            AddEntryPhase.DETAILS -> DetailsPhaseContent(
                uiState = uiState,
                onServingSizeChange = viewModel::onServingSizeChange,
                onNumberOfServingsChange = viewModel::onNumberOfServingsChange,
                onMealTypeChange = viewModel::onMealTypeChange,
                onSave = viewModel::onSave,
                modifier = Modifier.padding(innerPadding)
            )
            AddEntryPhase.SAVING -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
        }
    }
}

@Composable
private fun SearchPhaseContent(
    uiState: AddDiaryEntryUiState,
    onSearchQueryChange: (String) -> Unit,
    onFoodSelected: (Food) -> Unit,
    onRecipeSelected: (Recipe) -> Unit,
    onMealSelected: (SavedMeal) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Search bar
        FoodSearchBar(
            query = uiState.searchQuery,
            onQueryChange = onSearchQueryChange,
            placeholder = "Search foods, recipes, meals..."
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Results
        if (uiState.isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        } else {
            val displayFoods = if (uiState.searchQuery.isBlank()) {
                uiState.recentFoods
            } else {
                uiState.searchResults
            }

            val displayRecipes = if (uiState.searchQuery.isBlank()) {
                uiState.recentRecipes
            } else {
                uiState.recipeResults
            }

            val displayMeals = if (uiState.searchQuery.isBlank()) {
                uiState.recentMeals
            } else {
                uiState.savedMealResults
            }

            val hasNoResults = displayFoods.isEmpty() && displayRecipes.isEmpty() && displayMeals.isEmpty() && uiState.searchQuery.isNotBlank()

            if (hasNoResults) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No foods, recipes, or meals found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Saved Meals section
                    if (displayMeals.isNotEmpty()) {
                        item {
                            Text(
                                text = if (uiState.searchQuery.isBlank()) "Recent Meals" else "Saved Meals",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(
                            items = displayMeals,
                            key = { "meal_${it.id}" }
                        ) { meal ->
                            SavedMealCard(
                                savedMeal = meal,
                                onClick = { onMealSelected(meal) },
                                onDelete = null
                            )
                        }
                    }

                    // Recipes section
                    if (displayRecipes.isNotEmpty()) {
                        item {
                            Text(
                                text = if (uiState.searchQuery.isBlank()) "Recent Recipes" else "Recipes",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column {
                                    displayRecipes.forEachIndexed { index, recipe ->
                                        RecipeSearchResultItem(
                                            recipe = recipe,
                                            onClick = { onRecipeSelected(recipe) }
                                        )
                                        if (index < displayRecipes.lastIndex) {
                                            HorizontalDivider(
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Foods section
                    if (displayFoods.isNotEmpty()) {
                        item {
                            Text(
                                text = if (uiState.searchQuery.isBlank()) "Recent Foods" else "Foods",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column {
                                    displayFoods.forEachIndexed { index, food ->
                                        FoodSearchResultItem(
                                            food = food,
                                            onClick = { onFoodSelected(food) }
                                        )
                                        if (index < displayFoods.lastIndex) {
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
            }
        }
    }
}

@Composable
private fun FoodSearchResultItem(
    food: Food,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Orange indicator for foods
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Coral)
                )
            }
            if (!food.brand.isNullOrBlank()) {
                Text(
                    text = food.brand,
                    style = MaterialTheme.typography.bodySmall,
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

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${food.calories.roundToInt()}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = "cal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecipeSearchResultItem(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Green indicator for recipes
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(ChonkGreen)
                )
            }
            Text(
                text = "${recipe.totalServings} ${recipe.servingUnit.displayName}${if (recipe.totalServings > 1) "s" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${recipe.caloriesPerServing.roundToInt()}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = "cal/serving",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsPhaseContent(
    uiState: AddDiaryEntryUiState,
    onServingSizeChange: (String) -> Unit,
    onNumberOfServingsChange: (String) -> Unit,
    onMealTypeChange: (MealType) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val food = uiState.selectedFood
    val recipe = uiState.selectedRecipe

    if (food == null && recipe == null) return

    val itemName = food?.name ?: recipe?.name ?: ""
    val itemBrand = food?.brand
    val perServingText = if (food != null) {
        "Per ${food.servingSize.formatServing()} ${food.servingUnit.displayName}: ${food.calories.roundToInt()} cal"
    } else if (recipe != null) {
        "Per serving: ${recipe.caloriesPerServing.roundToInt()} cal"
    } else ""
    val servingUnitLabel = if (food != null) uiState.servingUnit.displayName else "servings"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        // Food/Recipe info card
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
                    text = itemName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                if (!itemBrand.isNullOrBlank()) {
                    Text(
                        text = itemBrand,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = perServingText,
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
                value = uiState.servingSizeText,
                onValueChange = onServingSizeChange,
                label = { Text(if (recipe != null) "Servings" else "Serving size") },
                suffix = { Text(servingUnitLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = uiState.numberOfServingsText,
                onValueChange = onNumberOfServingsChange,
                label = { Text("Quantity") },
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
            enabled = !uiState.isSaving
        ) {
            Text(
                text = if (uiState.isSaving) "Saving..." else "Add to Diary →",
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
private fun SearchPhasePreview() {
    ChonkCheckTheme {
        SearchPhaseContent(
            uiState = AddDiaryEntryUiState(
                recentFoods = listOf(
                    Food(
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
                    )
                )
            ),
            onSearchQueryChange = {},
            onFoodSelected = {},
            onRecipeSelected = {},
            onMealSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailsPhasePreview() {
    ChonkCheckTheme {
        DetailsPhaseContent(
            uiState = AddDiaryEntryUiState(
                phase = AddEntryPhase.DETAILS,
                selectedFood = Food(
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
                servingSize = 100.0,
                servingSizeText = "100",
                servingUnit = ServingUnit.GRAM,
                numberOfServings = 1.5,
                numberOfServingsText = "1.5",
                calculatedCalories = 247.5,
                calculatedProtein = 46.5,
                calculatedCarbs = 0.0,
                calculatedFat = 5.4
            ),
            onServingSizeChange = {},
            onNumberOfServingsChange = {},
            onMealTypeChange = {},
            onSave = {}
        )
    }
}
