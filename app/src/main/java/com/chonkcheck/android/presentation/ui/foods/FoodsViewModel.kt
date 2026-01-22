package com.chonkcheck.android.presentation.ui.foods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chonkcheck.android.domain.model.Food
import com.chonkcheck.android.domain.model.FoodFilter
import com.chonkcheck.android.domain.model.FoodFilterType
import com.chonkcheck.android.domain.usecase.DeleteUserFoodUseCase
import com.chonkcheck.android.domain.usecase.SearchFoodsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodsUiState(
    val foods: List<Food> = emptyList(),
    val searchQuery: String = "",
    val filterType: FoodFilterType = FoodFilterType.ALL,
    val isLoading: Boolean = true,
    val error: String? = null,
    val deleteConfirmation: DeleteConfirmation? = null
)

data class DeleteConfirmation(
    val foodId: String,
    val foodName: String
)

sealed class FoodsEvent {
    data class NavigateToEditFood(val foodId: String) : FoodsEvent()
    data object NavigateToCreateFood : FoodsEvent()
    data class ShowError(val message: String) : FoodsEvent()
    data object FoodDeleted : FoodsEvent()
}

@HiltViewModel
class FoodsViewModel @Inject constructor(
    private val searchFoodsUseCase: SearchFoodsUseCase,
    private val deleteFoodUseCase: DeleteUserFoodUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodsUiState())
    val uiState: StateFlow<FoodsUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<FoodsEvent?>(null)
    val events: StateFlow<FoodsEvent?> = _events.asStateFlow()

    init {
        loadFoods()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadFoods()
    }

    fun onFilterTypeChange(filterType: FoodFilterType) {
        _uiState.update { it.copy(filterType = filterType, searchQuery = "") }
        loadFoods()
    }

    fun onFoodClick(foodId: String) {
        _events.value = FoodsEvent.NavigateToEditFood(foodId)
    }

    fun onAddFoodClick() {
        _events.value = FoodsEvent.NavigateToCreateFood
    }

    fun onDeleteClick(food: Food) {
        _uiState.update {
            it.copy(
                deleteConfirmation = DeleteConfirmation(
                    foodId = food.id,
                    foodName = food.name
                )
            )
        }
    }

    fun onDeleteConfirm() {
        val confirmation = _uiState.value.deleteConfirmation ?: return
        _uiState.update { it.copy(deleteConfirmation = null) }

        viewModelScope.launch {
            deleteFoodUseCase(confirmation.foodId)
                .onSuccess {
                    _events.value = FoodsEvent.FoodDeleted
                    loadFoods()
                }
                .onFailure { error ->
                    _events.value = FoodsEvent.ShowError(error.message ?: "Failed to delete food")
                }
        }
    }

    fun onDeleteCancel() {
        _uiState.update { it.copy(deleteConfirmation = null) }
    }

    fun onEventConsumed() {
        _events.value = null
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun loadFoods() {
        val filter = FoodFilter(
            query = _uiState.value.searchQuery,
            type = _uiState.value.filterType,
            includeRecipes = false,
            includeMeals = false,
            limit = 100
        )

        searchFoodsUseCase(filter)
            .onEach { foods ->
                _uiState.update {
                    it.copy(
                        foods = foods,
                        isLoading = false,
                        error = null
                    )
                }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load foods"
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
