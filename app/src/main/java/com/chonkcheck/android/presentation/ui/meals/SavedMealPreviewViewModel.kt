package com.chonkcheck.android.presentation.ui.meals

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chonkcheck.android.domain.model.AddMealToDiaryItemParams
import com.chonkcheck.android.domain.model.AddMealToDiaryParams
import com.chonkcheck.android.domain.model.MealType
import com.chonkcheck.android.domain.model.SavedMeal
import com.chonkcheck.android.domain.model.SavedMealItemType
import com.chonkcheck.android.domain.usecase.AddSavedMealToDiaryUseCase
import com.chonkcheck.android.domain.usecase.GetSavedMealByIdUseCase
import com.chonkcheck.android.presentation.navigation.NavArgs
import com.chonkcheck.android.presentation.ui.meals.components.MealItemNutrition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavedMealPreviewUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val mealName: String = "",
    val items: List<PreviewItem> = emptyList(),
    val totalNutrition: MealItemNutrition = MealItemNutrition(0.0, 0.0, 0.0, 0.0),
    val selectedMealType: MealType = MealType.SNACKS,
    val error: String? = null
)

data class PreviewItem(
    val itemId: String,
    val itemType: SavedMealItemType,
    val itemName: String,
    val servingUnitName: String,
    val quantity: Double,
    val enteredAmount: Double?,
    val nutrition: MealItemNutrition,
    // Base nutrition per serving (for recalculation)
    val baseCalories: Double,
    val baseProtein: Double,
    val baseCarbs: Double,
    val baseFat: Double
)

sealed class SavedMealPreviewEvent {
    data object NavigateBack : SavedMealPreviewEvent()
    data object MealAddedToDiary : SavedMealPreviewEvent()
    data class ShowError(val message: String) : SavedMealPreviewEvent()
}

@HiltViewModel
class SavedMealPreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getSavedMealByIdUseCase: GetSavedMealByIdUseCase,
    private val addSavedMealToDiaryUseCase: AddSavedMealToDiaryUseCase
) : ViewModel() {

    private val savedMealId: String = checkNotNull(savedStateHandle[NavArgs.SAVED_MEAL_ID])
    private val date: String = checkNotNull(savedStateHandle[NavArgs.DATE])
    private val initialMealType: String = checkNotNull(savedStateHandle[NavArgs.MEAL_TYPE])

    private val _uiState = MutableStateFlow(SavedMealPreviewUiState(
        selectedMealType = MealType.fromApiValue(initialMealType)
    ))
    val uiState: StateFlow<SavedMealPreviewUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<SavedMealPreviewEvent?>(null)
    val events: StateFlow<SavedMealPreviewEvent?> = _events.asStateFlow()

    init {
        loadMeal()
    }

    private fun loadMeal() {
        viewModelScope.launch {
            val meal = getSavedMealByIdUseCase(savedMealId).first()
            if (meal == null) {
                _events.value = SavedMealPreviewEvent.ShowError("Meal not found")
                _events.value = SavedMealPreviewEvent.NavigateBack
                return@launch
            }

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    mealName = meal.name,
                    items = meal.toPreviewItems(),
                    totalNutrition = MealItemNutrition(
                        calories = meal.totalCalories,
                        protein = meal.totalProtein,
                        carbs = meal.totalCarbs,
                        fat = meal.totalFat
                    )
                )
            }
        }
    }

    fun updateMealType(mealType: MealType) {
        _uiState.update { it.copy(selectedMealType = mealType) }
    }

    fun updateItemQuantity(index: Int, value: String) {
        val quantity = value.toDoubleOrNull() ?: return
        updateItem(index) { item ->
            val nutrition = MealItemNutrition(
                calories = item.baseCalories * quantity,
                protein = item.baseProtein * quantity,
                carbs = item.baseCarbs * quantity,
                fat = item.baseFat * quantity
            )
            item.copy(
                quantity = quantity,
                nutrition = nutrition
            )
        }
    }

    private fun updateItem(index: Int, update: (PreviewItem) -> PreviewItem) {
        _uiState.update { state ->
            val updatedItems = state.items.toMutableList().apply {
                if (index in indices) {
                    this[index] = update(this[index])
                }
            }
            val totalNutrition = MealItemNutrition(
                calories = updatedItems.sumOf { it.nutrition.calories },
                protein = updatedItems.sumOf { it.nutrition.protein },
                carbs = updatedItems.sumOf { it.nutrition.carbs },
                fat = updatedItems.sumOf { it.nutrition.fat }
            )
            state.copy(
                items = updatedItems,
                totalNutrition = totalNutrition
            )
        }
    }

    fun logMeal() {
        val state = _uiState.value

        if (state.items.isEmpty()) {
            _uiState.update { it.copy(error = "Meal has no items") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            val params = AddMealToDiaryParams(
                savedMealId = savedMealId,
                date = date,
                mealType = state.selectedMealType,
                items = state.items.map { item ->
                    AddMealToDiaryItemParams(
                        itemId = item.itemId,
                        itemType = item.itemType,
                        quantity = item.quantity,
                        enteredAmount = item.enteredAmount
                    )
                }
            )

            addSavedMealToDiaryUseCase(params)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false) }
                    _events.value = SavedMealPreviewEvent.MealAddedToDiary
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = error.message ?: "Failed to add meal to diary"
                        )
                    }
                }
        }
    }

    fun onEventConsumed() {
        _events.value = null
    }

    private fun SavedMeal.toPreviewItems(): List<PreviewItem> {
        return items.map { item ->
            PreviewItem(
                itemId = item.itemId,
                itemType = item.itemType,
                itemName = item.itemName,
                servingUnitName = item.servingUnit.displayName,
                quantity = item.quantity,
                enteredAmount = item.enteredAmount,
                nutrition = MealItemNutrition(
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
    }
}
