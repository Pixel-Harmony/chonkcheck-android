package com.chonkcheck.android.presentation.ui.meals

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chonkcheck.android.domain.model.CreateSavedMealParams
import com.chonkcheck.android.domain.model.Food
import com.chonkcheck.android.domain.model.FoodFilter
import com.chonkcheck.android.domain.model.FoodFilterType
import com.chonkcheck.android.domain.model.Recipe
import com.chonkcheck.android.domain.model.SavedMeal
import com.chonkcheck.android.domain.model.SavedMealItemParams
import com.chonkcheck.android.domain.model.SavedMealItemType
import com.chonkcheck.android.domain.model.UpdateSavedMealParams
import com.chonkcheck.android.domain.usecase.CreateSavedMealUseCase
import com.chonkcheck.android.domain.usecase.GetSavedMealByIdUseCase
import com.chonkcheck.android.domain.usecase.SearchFoodsUseCase
import com.chonkcheck.android.domain.usecase.SearchRecipesUseCase
import com.chonkcheck.android.domain.usecase.UpdateSavedMealUseCase
import com.chonkcheck.android.presentation.navigation.NavArgs
import com.chonkcheck.android.presentation.ui.meals.components.MealItemInputMode
import com.chonkcheck.android.presentation.ui.meals.components.MealItemNutrition
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

data class SavedMealFormUiState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val name: String = "",
    val items: List<MealItemFormItem> = emptyList(),
    val totalNutrition: MealItemNutrition = MealItemNutrition(0.0, 0.0, 0.0, 0.0),
    val error: String? = null,
    val hasUnsavedChanges: Boolean = false,
    val showUnsavedChangesDialog: Boolean = false,
    val showItemSearch: Boolean = false,
    val searchFoods: List<Food> = emptyList(),
    val searchRecipes: List<Recipe> = emptyList(),
    val isSearchingItems: Boolean = false
)

data class MealItemFormItem(
    val itemId: String,
    val itemType: SavedMealItemType,
    val itemName: String,
    val brand: String?,
    val servingSize: Double,
    val servingUnitName: String,
    val inputMode: MealItemInputMode = MealItemInputMode.SERVINGS,
    val quantity: Double = 1.0,
    val enteredAmount: Double? = null,
    val calculatedNutrition: MealItemNutrition = MealItemNutrition(0.0, 0.0, 0.0, 0.0),
    // Base nutrition per serving (for calculation)
    val baseCalories: Double,
    val baseProtein: Double,
    val baseCarbs: Double,
    val baseFat: Double
)

sealed class SavedMealFormEvent {
    data object NavigateBack : SavedMealFormEvent()
    data object MealSaved : SavedMealFormEvent()
    data class ShowError(val message: String) : SavedMealFormEvent()
}

@HiltViewModel
class SavedMealFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getSavedMealByIdUseCase: GetSavedMealByIdUseCase,
    private val createSavedMealUseCase: CreateSavedMealUseCase,
    private val updateSavedMealUseCase: UpdateSavedMealUseCase,
    private val searchFoodsUseCase: SearchFoodsUseCase,
    private val searchRecipesUseCase: SearchRecipesUseCase
) : ViewModel() {

    private val savedMealId: String? = savedStateHandle[NavArgs.SAVED_MEAL_ID]

    private val _uiState = MutableStateFlow(SavedMealFormUiState())
    val uiState: StateFlow<SavedMealFormUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<SavedMealFormEvent?>(null)
    val events: StateFlow<SavedMealFormEvent?> = _events.asStateFlow()

    init {
        if (savedMealId != null) {
            loadSavedMeal(savedMealId)
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isEditMode = false
                )
            }
        }
    }

    private fun loadSavedMeal(id: String) {
        viewModelScope.launch {
            val meal = getSavedMealByIdUseCase(id).first()
            if (meal == null) {
                _events.value = SavedMealFormEvent.ShowError("Meal not found")
                _events.value = SavedMealFormEvent.NavigateBack
                return@launch
            }

            val state = meal.toFormState()
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

    fun showItemSearch() {
        _uiState.update { it.copy(showItemSearch = true) }
    }

    fun hideItemSearch() {
        _uiState.update {
            it.copy(
                showItemSearch = false,
                searchFoods = emptyList(),
                searchRecipes = emptyList()
            )
        }
    }

    fun searchItems(query: String) {
        _uiState.update { it.copy(isSearchingItems = true) }

        // Search foods
        val foodFilter = FoodFilter(
            query = query,
            type = FoodFilterType.ALL,
            includeRecipes = false,
            includeMeals = false,
            limit = 30
        )

        searchFoodsUseCase(foodFilter)
            .onEach { foods ->
                _uiState.update {
                    it.copy(
                        searchFoods = foods,
                        isSearchingItems = false
                    )
                }
            }
            .catch {
                _uiState.update { it.copy(isSearchingItems = false) }
            }
            .launchIn(viewModelScope)

        // Search recipes
        searchRecipesUseCase(query, limit = 20)
            .onEach { recipes ->
                _uiState.update {
                    it.copy(searchRecipes = recipes)
                }
            }
            .catch { /* ignore */ }
            .launchIn(viewModelScope)
    }

    fun addFood(food: Food) {
        val nutrition = calculateNutrition(food.calories, food.protein, food.carbs, food.fat, 1.0)
        val newItem = MealItemFormItem(
            itemId = food.id,
            itemType = SavedMealItemType.FOOD,
            itemName = food.name,
            brand = food.brand,
            servingSize = food.servingSize,
            servingUnitName = food.servingUnit.displayName,
            inputMode = MealItemInputMode.SERVINGS,
            quantity = 1.0,
            enteredAmount = null,
            calculatedNutrition = nutrition,
            baseCalories = food.calories,
            baseProtein = food.protein,
            baseCarbs = food.carbs,
            baseFat = food.fat
        )

        _uiState.update {
            it.copy(
                items = it.items + newItem,
                showItemSearch = false,
                hasUnsavedChanges = true
            )
        }
        recalculateTotalNutrition()
    }

    fun addRecipe(recipe: Recipe) {
        val nutrition = calculateNutrition(
            recipe.caloriesPerServing,
            recipe.proteinPerServing,
            recipe.carbsPerServing,
            recipe.fatPerServing,
            1.0
        )
        val newItem = MealItemFormItem(
            itemId = recipe.id,
            itemType = SavedMealItemType.RECIPE,
            itemName = recipe.name,
            brand = null,
            servingSize = 1.0,
            servingUnitName = recipe.servingUnit.displayName,
            inputMode = MealItemInputMode.SERVINGS,
            quantity = 1.0,
            enteredAmount = null,
            calculatedNutrition = nutrition,
            baseCalories = recipe.caloriesPerServing,
            baseProtein = recipe.proteinPerServing,
            baseCarbs = recipe.carbsPerServing,
            baseFat = recipe.fatPerServing
        )

        _uiState.update {
            it.copy(
                items = it.items + newItem,
                showItemSearch = false,
                hasUnsavedChanges = true
            )
        }
        recalculateTotalNutrition()
    }

    fun removeItem(index: Int) {
        _uiState.update {
            it.copy(
                items = it.items.toMutableList().apply { removeAt(index) },
                hasUnsavedChanges = true
            )
        }
        recalculateTotalNutrition()
    }

    fun updateItemQuantity(index: Int, value: String) {
        val quantity = value.toDoubleOrNull() ?: return
        updateItem(index) { item ->
            val nutrition = if (item.inputMode == MealItemInputMode.SERVINGS) {
                calculateNutrition(item.baseCalories, item.baseProtein, item.baseCarbs, item.baseFat, quantity)
            } else {
                item.calculatedNutrition
            }
            item.copy(
                quantity = quantity,
                calculatedNutrition = nutrition
            )
        }
    }

    fun updateItemAmount(index: Int, value: String) {
        val amount = value.toDoubleOrNull() ?: return
        updateItem(index) { item ->
            val effectiveQuantity = amount / item.servingSize
            val nutrition = calculateNutrition(item.baseCalories, item.baseProtein, item.baseCarbs, item.baseFat, effectiveQuantity)
            item.copy(
                enteredAmount = amount,
                quantity = effectiveQuantity,
                calculatedNutrition = nutrition
            )
        }
    }

    fun toggleInputMode(index: Int) {
        updateItem(index) { item ->
            val newMode = if (item.inputMode == MealItemInputMode.SERVINGS) {
                MealItemInputMode.AMOUNT
            } else {
                MealItemInputMode.SERVINGS
            }

            if (newMode == MealItemInputMode.AMOUNT) {
                val amount = item.servingSize * item.quantity
                item.copy(
                    inputMode = newMode,
                    enteredAmount = amount
                )
            } else {
                item.copy(
                    inputMode = newMode,
                    enteredAmount = null
                )
            }
        }
    }

    private fun updateItem(index: Int, update: (MealItemFormItem) -> MealItemFormItem) {
        _uiState.update { state ->
            val updatedItems = state.items.toMutableList().apply {
                if (index in indices) {
                    this[index] = update(this[index])
                }
            }
            state.copy(
                items = updatedItems,
                hasUnsavedChanges = true
            )
        }
        recalculateTotalNutrition()
    }

    private fun calculateNutrition(
        baseCalories: Double,
        baseProtein: Double,
        baseCarbs: Double,
        baseFat: Double,
        quantity: Double
    ): MealItemNutrition {
        return MealItemNutrition(
            calories = baseCalories * quantity,
            protein = baseProtein * quantity,
            carbs = baseCarbs * quantity,
            fat = baseFat * quantity
        )
    }

    private fun recalculateTotalNutrition() {
        val state = _uiState.value
        val totalCalories = state.items.sumOf { it.calculatedNutrition.calories }
        val totalProtein = state.items.sumOf { it.calculatedNutrition.protein }
        val totalCarbs = state.items.sumOf { it.calculatedNutrition.carbs }
        val totalFat = state.items.sumOf { it.calculatedNutrition.fat }

        _uiState.update {
            it.copy(
                totalNutrition = MealItemNutrition(
                    calories = totalCalories,
                    protein = totalProtein,
                    carbs = totalCarbs,
                    fat = totalFat
                )
            )
        }
    }

    fun onBackPressed() {
        if (_uiState.value.hasUnsavedChanges) {
            _uiState.update { it.copy(showUnsavedChangesDialog = true) }
        } else {
            _events.value = SavedMealFormEvent.NavigateBack
        }
    }

    fun dismissUnsavedChangesDialog() {
        _uiState.update { it.copy(showUnsavedChangesDialog = false) }
    }

    fun discardChanges() {
        _uiState.update { it.copy(showUnsavedChangesDialog = false) }
        _events.value = SavedMealFormEvent.NavigateBack
    }

    fun saveMeal() {
        val state = _uiState.value

        // Validate
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Meal name is required") }
            return
        }

        if (state.items.isEmpty()) {
            _uiState.update { it.copy(error = "Meal must have at least one item") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            val itemParams = state.items.map { item ->
                SavedMealItemParams(
                    itemId = item.itemId,
                    itemType = item.itemType,
                    quantity = item.quantity,
                    enteredAmount = item.enteredAmount
                )
            }

            val result = if (savedMealId != null) {
                updateSavedMealUseCase(
                    savedMealId,
                    UpdateSavedMealParams(
                        name = state.name.trim(),
                        items = itemParams
                    )
                )
            } else {
                createSavedMealUseCase(
                    CreateSavedMealParams(
                        name = state.name.trim(),
                        items = itemParams
                    )
                )
            }

            result.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                _events.value = SavedMealFormEvent.MealSaved
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = error.message ?: "Failed to save meal"
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

    private fun SavedMeal.toFormState(): SavedMealFormUiState {
        val formItems = items.map { item ->
            MealItemFormItem(
                itemId = item.itemId,
                itemType = item.itemType,
                itemName = item.itemName,
                brand = item.brand,
                servingSize = item.servingSize,
                servingUnitName = item.servingUnit.displayName,
                inputMode = if (item.enteredAmount != null) MealItemInputMode.AMOUNT else MealItemInputMode.SERVINGS,
                quantity = item.quantity,
                enteredAmount = item.enteredAmount,
                calculatedNutrition = MealItemNutrition(
                    calories = item.calories,
                    protein = item.protein,
                    carbs = item.carbs,
                    fat = item.fat
                ),
                baseCalories = item.calories / item.quantity,
                baseProtein = item.protein / item.quantity,
                baseCarbs = item.carbs / item.quantity,
                baseFat = item.fat / item.quantity
            )
        }

        return SavedMealFormUiState(
            isEditMode = true,
            isLoading = false,
            name = name,
            items = formItems,
            totalNutrition = MealItemNutrition(
                calories = totalCalories,
                protein = totalProtein,
                carbs = totalCarbs,
                fat = totalFat
            ),
            hasUnsavedChanges = false
        )
    }
}
