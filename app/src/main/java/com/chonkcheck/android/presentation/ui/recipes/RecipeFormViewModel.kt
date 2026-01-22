package com.chonkcheck.android.presentation.ui.recipes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chonkcheck.android.domain.model.CreateRecipeParams
import com.chonkcheck.android.domain.model.Food
import com.chonkcheck.android.domain.model.FoodFilter
import com.chonkcheck.android.domain.model.FoodFilterType
import com.chonkcheck.android.domain.model.Recipe
import com.chonkcheck.android.domain.model.RecipeIngredientParams
import com.chonkcheck.android.domain.model.RecipeServingUnit
import com.chonkcheck.android.domain.model.ServingUnit
import com.chonkcheck.android.domain.model.UpdateRecipeParams
import com.chonkcheck.android.domain.usecase.CreateRecipeUseCase
import com.chonkcheck.android.domain.usecase.GetRecipeByIdUseCase
import com.chonkcheck.android.domain.usecase.SearchFoodsUseCase
import com.chonkcheck.android.domain.usecase.UpdateRecipeUseCase
import com.chonkcheck.android.presentation.navigation.NavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipeFormUiState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val name: String = "",
    val description: String = "",
    val totalServings: String = "1",
    val servingUnit: RecipeServingUnit = RecipeServingUnit.SERVING,
    val ingredients: List<IngredientFormItem> = emptyList(),
    val totalNutrition: NutritionSummary = NutritionSummary(),
    val perServingNutrition: NutritionSummary = NutritionSummary(),
    val error: String? = null,
    val hasUnsavedChanges: Boolean = false,
    val showUnsavedChangesDialog: Boolean = false,
    val showIngredientSearch: Boolean = false,
    val searchFoods: List<Food> = emptyList(),
    val isSearchingFoods: Boolean = false
)

data class IngredientFormItem(
    val food: Food,
    val inputMode: InputMode = InputMode.SERVINGS,
    val quantity: Double = 1.0,
    val enteredAmount: Double? = null,
    val calculatedNutrition: NutritionSummary = NutritionSummary()
)

enum class InputMode {
    SERVINGS,
    AMOUNT
}

data class NutritionSummary(
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0
)

sealed class RecipeFormEvent {
    data object NavigateBack : RecipeFormEvent()
    data object RecipeSaved : RecipeFormEvent()
    data class ShowError(val message: String) : RecipeFormEvent()
}

@HiltViewModel
class RecipeFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRecipeByIdUseCase: GetRecipeByIdUseCase,
    private val createRecipeUseCase: CreateRecipeUseCase,
    private val updateRecipeUseCase: UpdateRecipeUseCase,
    private val searchFoodsUseCase: SearchFoodsUseCase
) : ViewModel() {

    private val recipeId: String? = savedStateHandle[NavArgs.RECIPE_ID]

    private val _uiState = MutableStateFlow(RecipeFormUiState())
    val uiState: StateFlow<RecipeFormUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<RecipeFormEvent?>(null)
    val events: StateFlow<RecipeFormEvent?> = _events.asStateFlow()

    private var originalState: RecipeFormUiState? = null

    init {
        if (recipeId != null) {
            loadRecipe(recipeId)
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isEditMode = false
                )
            }
        }
    }

    private fun loadRecipe(id: String) {
        viewModelScope.launch {
            val recipe = getRecipeByIdUseCase(id).first()
            if (recipe == null) {
                _events.value = RecipeFormEvent.ShowError("Recipe not found")
                _events.value = RecipeFormEvent.NavigateBack
                return@launch
            }

            val state = recipe.toFormState()
            originalState = state
            _uiState.update { state }
        }
    }

    fun updateName(value: String) {
        _uiState.update {
            it.copy(
                name = value,
                hasUnsavedChanges = true
            )
        }
    }

    fun updateDescription(value: String) {
        _uiState.update {
            it.copy(
                description = value,
                hasUnsavedChanges = true
            )
        }
    }

    fun updateTotalServings(value: String) {
        _uiState.update {
            it.copy(
                totalServings = value,
                hasUnsavedChanges = true
            )
        }
        recalculateNutrition()
    }

    fun updateServingUnit(unit: RecipeServingUnit) {
        _uiState.update {
            it.copy(
                servingUnit = unit,
                hasUnsavedChanges = true
            )
        }
    }

    fun showIngredientSearch() {
        _uiState.update { it.copy(showIngredientSearch = true) }
    }

    fun hideIngredientSearch() {
        _uiState.update { it.copy(showIngredientSearch = false, searchFoods = emptyList()) }
    }

    fun searchFoods(query: String) {
        _uiState.update { it.copy(isSearchingFoods = true) }

        val filter = FoodFilter(
            query = query,
            type = FoodFilterType.ALL,
            includeRecipes = false,
            includeMeals = false,
            limit = 50
        )

        searchFoodsUseCase(filter)
            .onEach { foods ->
                _uiState.update {
                    it.copy(
                        searchFoods = foods,
                        isSearchingFoods = false
                    )
                }
            }
            .catch {
                _uiState.update { it.copy(isSearchingFoods = false) }
            }
            .launchIn(viewModelScope)
    }

    fun addIngredient(food: Food) {
        val nutrition = calculateIngredientNutrition(food, 1.0)
        val newIngredient = IngredientFormItem(
            food = food,
            inputMode = InputMode.SERVINGS,
            quantity = 1.0,
            enteredAmount = null,
            calculatedNutrition = nutrition
        )

        _uiState.update {
            it.copy(
                ingredients = it.ingredients + newIngredient,
                showIngredientSearch = false,
                hasUnsavedChanges = true
            )
        }
        recalculateNutrition()
    }

    fun removeIngredient(index: Int) {
        _uiState.update {
            it.copy(
                ingredients = it.ingredients.toMutableList().apply { removeAt(index) },
                hasUnsavedChanges = true
            )
        }
        recalculateNutrition()
    }

    fun updateIngredientQuantity(index: Int, value: String) {
        val quantity = value.toDoubleOrNull() ?: return
        updateIngredient(index) { ingredient ->
            val nutrition = if (ingredient.inputMode == InputMode.SERVINGS) {
                calculateIngredientNutrition(ingredient.food, quantity)
            } else {
                ingredient.calculatedNutrition
            }
            ingredient.copy(
                quantity = quantity,
                calculatedNutrition = nutrition
            )
        }
    }

    fun updateIngredientAmount(index: Int, value: String) {
        val amount = value.toDoubleOrNull() ?: return
        updateIngredient(index) { ingredient ->
            val effectiveQuantity = amount / ingredient.food.servingSize
            val nutrition = calculateIngredientNutrition(ingredient.food, effectiveQuantity)
            ingredient.copy(
                enteredAmount = amount,
                quantity = effectiveQuantity,
                calculatedNutrition = nutrition
            )
        }
    }

    fun toggleInputMode(index: Int) {
        updateIngredient(index) { ingredient ->
            val newMode = if (ingredient.inputMode == InputMode.SERVINGS) {
                InputMode.AMOUNT
            } else {
                InputMode.SERVINGS
            }

            if (newMode == InputMode.AMOUNT) {
                // Switching to amount mode - calculate the amount
                val amount = ingredient.food.servingSize * ingredient.quantity
                ingredient.copy(
                    inputMode = newMode,
                    enteredAmount = amount
                )
            } else {
                // Switching to servings mode
                ingredient.copy(
                    inputMode = newMode,
                    enteredAmount = null
                )
            }
        }
    }

    private fun updateIngredient(index: Int, update: (IngredientFormItem) -> IngredientFormItem) {
        _uiState.update { state ->
            val updatedIngredients = state.ingredients.toMutableList().apply {
                if (index in indices) {
                    this[index] = update(this[index])
                }
            }
            state.copy(
                ingredients = updatedIngredients,
                hasUnsavedChanges = true
            )
        }
        recalculateNutrition()
    }

    private fun calculateIngredientNutrition(food: Food, quantity: Double): NutritionSummary {
        return NutritionSummary(
            calories = food.calories * quantity,
            protein = food.protein * quantity,
            carbs = food.carbs * quantity,
            fat = food.fat * quantity
        )
    }

    private fun recalculateNutrition() {
        val state = _uiState.value
        val totalCalories = state.ingredients.sumOf { it.calculatedNutrition.calories }
        val totalProtein = state.ingredients.sumOf { it.calculatedNutrition.protein }
        val totalCarbs = state.ingredients.sumOf { it.calculatedNutrition.carbs }
        val totalFat = state.ingredients.sumOf { it.calculatedNutrition.fat }

        val servings = state.totalServings.toIntOrNull() ?: 1

        _uiState.update {
            it.copy(
                totalNutrition = NutritionSummary(
                    calories = totalCalories,
                    protein = totalProtein,
                    carbs = totalCarbs,
                    fat = totalFat
                ),
                perServingNutrition = NutritionSummary(
                    calories = totalCalories / servings,
                    protein = totalProtein / servings,
                    carbs = totalCarbs / servings,
                    fat = totalFat / servings
                )
            )
        }
    }

    fun onBackPressed() {
        if (_uiState.value.hasUnsavedChanges) {
            _uiState.update { it.copy(showUnsavedChangesDialog = true) }
        } else {
            _events.value = RecipeFormEvent.NavigateBack
        }
    }

    fun dismissUnsavedChangesDialog() {
        _uiState.update { it.copy(showUnsavedChangesDialog = false) }
    }

    fun discardChanges() {
        _uiState.update { it.copy(showUnsavedChangesDialog = false) }
        _events.value = RecipeFormEvent.NavigateBack
    }

    fun saveRecipe() {
        val state = _uiState.value

        // Validate
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Recipe name is required") }
            return
        }

        val servings = state.totalServings.toIntOrNull()
        if (servings == null || servings <= 0) {
            _uiState.update { it.copy(error = "Total servings must be a positive number") }
            return
        }

        if (state.ingredients.isEmpty()) {
            _uiState.update { it.copy(error = "Recipe must have at least one ingredient") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            val ingredientParams = state.ingredients.map { ingredient ->
                RecipeIngredientParams(
                    foodId = ingredient.food.id,
                    quantity = ingredient.quantity,
                    enteredAmount = ingredient.enteredAmount
                )
            }

            val result = if (recipeId != null) {
                updateRecipeUseCase(
                    recipeId,
                    UpdateRecipeParams(
                        name = state.name.trim(),
                        description = state.description.takeIf { it.isNotBlank() }?.trim(),
                        totalServings = servings,
                        servingUnit = state.servingUnit,
                        ingredients = ingredientParams
                    )
                )
            } else {
                createRecipeUseCase(
                    CreateRecipeParams(
                        name = state.name.trim(),
                        description = state.description.takeIf { it.isNotBlank() }?.trim(),
                        totalServings = servings,
                        servingUnit = state.servingUnit,
                        ingredients = ingredientParams
                    )
                )
            }

            result.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                _events.value = RecipeFormEvent.RecipeSaved
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = error.message ?: "Failed to save recipe"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onEventConsumed() {
        _events.value = null
    }

    private fun Recipe.toFormState(): RecipeFormUiState {
        val ingredientItems = ingredients.map { ingredient ->
            // Create a minimal Food object for display
            val food = Food(
                id = ingredient.foodId,
                name = ingredient.foodName,
                brand = null,
                barcode = null,
                servingSize = ingredient.servingSize,
                servingUnit = ingredient.servingUnit,
                servingsPerContainer = null,
                calories = ingredient.calories / ingredient.quantity,
                protein = ingredient.protein / ingredient.quantity,
                carbs = ingredient.carbs / ingredient.quantity,
                fat = ingredient.fat / ingredient.quantity,
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
                type = com.chonkcheck.android.domain.model.FoodType.USER,
                source = null,
                verified = false,
                promotionRequested = false,
                overrideOf = null,
                imageUrl = null,
                createdAt = 0
            )

            IngredientFormItem(
                food = food,
                inputMode = if (ingredient.enteredAmount != null) InputMode.AMOUNT else InputMode.SERVINGS,
                quantity = ingredient.quantity,
                enteredAmount = ingredient.enteredAmount,
                calculatedNutrition = NutritionSummary(
                    calories = ingredient.calories,
                    protein = ingredient.protein,
                    carbs = ingredient.carbs,
                    fat = ingredient.fat
                )
            )
        }

        return RecipeFormUiState(
            isEditMode = true,
            isLoading = false,
            name = name,
            description = description ?: "",
            totalServings = totalServings.toString(),
            servingUnit = servingUnit,
            ingredients = ingredientItems,
            totalNutrition = NutritionSummary(
                calories = totalCalories,
                protein = totalProtein,
                carbs = totalCarbs,
                fat = totalFat
            ),
            perServingNutrition = NutritionSummary(
                calories = caloriesPerServing,
                protein = proteinPerServing,
                carbs = carbsPerServing,
                fat = fatPerServing
            ),
            hasUnsavedChanges = false
        )
    }
}
