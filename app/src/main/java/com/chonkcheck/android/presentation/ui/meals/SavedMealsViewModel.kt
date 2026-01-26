package com.chonkcheck.android.presentation.ui.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chonkcheck.android.domain.model.SavedMeal
import com.chonkcheck.android.domain.usecase.DeleteSavedMealUseCase
import com.chonkcheck.android.domain.usecase.SearchSavedMealsUseCase
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

data class SavedMealsUiState(
    val meals: List<SavedMeal> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val deleteConfirmation: MealDeleteConfirmation? = null
)

data class MealDeleteConfirmation(
    val mealId: String,
    val mealName: String
)

sealed class SavedMealsEvent {
    data class NavigateToEditMeal(val mealId: String) : SavedMealsEvent()
    data object NavigateToCreateMeal : SavedMealsEvent()
    data class ShowError(val message: String) : SavedMealsEvent()
    data object MealDeleted : SavedMealsEvent()
}

@HiltViewModel
class SavedMealsViewModel @Inject constructor(
    private val searchSavedMealsUseCase: SearchSavedMealsUseCase,
    private val deleteSavedMealUseCase: DeleteSavedMealUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedMealsUiState())
    val uiState: StateFlow<SavedMealsUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<SavedMealsEvent?>(null)
    val events: StateFlow<SavedMealsEvent?> = _events.asStateFlow()

    init {
        loadMeals()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadMeals()
    }

    fun onMealClick(mealId: String) {
        _events.value = SavedMealsEvent.NavigateToEditMeal(mealId)
    }

    fun onAddMealClick() {
        _events.value = SavedMealsEvent.NavigateToCreateMeal
    }

    fun onDeleteClick(meal: SavedMeal) {
        _uiState.update {
            it.copy(
                deleteConfirmation = MealDeleteConfirmation(
                    mealId = meal.id,
                    mealName = meal.name
                )
            )
        }
    }

    fun onDeleteConfirm() {
        val confirmation = _uiState.value.deleteConfirmation ?: return
        _uiState.update { it.copy(deleteConfirmation = null) }

        viewModelScope.launch {
            deleteSavedMealUseCase(confirmation.mealId)
                .onSuccess {
                    _events.value = SavedMealsEvent.MealDeleted
                    loadMeals()
                }
                .onFailure { error ->
                    _events.value = SavedMealsEvent.ShowError(error.message ?: "Failed to delete meal")
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

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadMeals()
    }

    private fun loadMeals() {
        searchSavedMealsUseCase(_uiState.value.searchQuery, limit = 100)
            .onEach { meals ->
                _uiState.update {
                    it.copy(
                        meals = meals,
                        isLoading = false,
                        isRefreshing = false,
                        error = null
                    )
                }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = error.message ?: "Failed to load meals"
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
