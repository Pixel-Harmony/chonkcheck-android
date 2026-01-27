package com.chonkcheck.android.presentation.ui.diary.editentry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chonkcheck.android.domain.model.DiaryEntry
import com.chonkcheck.android.domain.model.MealType
import com.chonkcheck.android.domain.model.ServingUnit
import com.chonkcheck.android.domain.model.UpdateDiaryEntryParams
import com.chonkcheck.android.domain.usecase.DeleteDiaryEntryUseCase
import com.chonkcheck.android.domain.usecase.GetDiaryEntryUseCase
import com.chonkcheck.android.domain.usecase.UpdateDiaryEntryUseCase
import com.chonkcheck.android.presentation.navigation.NavArgs
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

data class EditDiaryEntryUiState(
    val isLoading: Boolean = true,
    val entry: DiaryEntry? = null,

    // Editable fields
    val servingSize: Double = 0.0,
    val servingUnit: ServingUnit = ServingUnit.GRAM,
    val numberOfServings: Double = 1.0,
    val mealType: MealType = MealType.BREAKFAST,

    // Original values for ratio calculation
    val originalServingSize: Double = 0.0,
    val originalNumberOfServings: Double = 1.0,
    val originalCalories: Double = 0.0,
    val originalProtein: Double = 0.0,
    val originalCarbs: Double = 0.0,
    val originalFat: Double = 0.0,

    // Calculated values
    val calculatedCalories: Double = 0.0,
    val calculatedProtein: Double = 0.0,
    val calculatedCarbs: Double = 0.0,
    val calculatedFat: Double = 0.0,

    // State
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val error: String? = null
)

sealed class EditDiaryEntryEvent {
    data object EntrySaved : EditDiaryEntryEvent()
    data object EntryDeleted : EditDiaryEntryEvent()
    data class ShowError(val message: String) : EditDiaryEntryEvent()
}

@HiltViewModel
class EditDiaryEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDiaryEntryUseCase: GetDiaryEntryUseCase,
    private val updateDiaryEntryUseCase: UpdateDiaryEntryUseCase,
    private val deleteDiaryEntryUseCase: DeleteDiaryEntryUseCase
) : ViewModel() {

    private val entryId: String = savedStateHandle.get<String>(NavArgs.ENTRY_ID) ?: ""

    private val _uiState = MutableStateFlow(EditDiaryEntryUiState())
    val uiState: StateFlow<EditDiaryEntryUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<EditDiaryEntryEvent?>(null)
    val events: StateFlow<EditDiaryEntryEvent?> = _events.asStateFlow()

    init {
        loadEntry()
    }

    private fun loadEntry() {
        getDiaryEntryUseCase(entryId)
            .onEach { entry ->
                if (entry != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            entry = entry,
                            servingSize = entry.servingSize,
                            servingUnit = entry.servingUnit,
                            numberOfServings = entry.numberOfServings,
                            mealType = entry.mealType,
                            originalServingSize = entry.servingSize,
                            originalNumberOfServings = entry.numberOfServings,
                            originalCalories = entry.calories,
                            originalProtein = entry.protein,
                            originalCarbs = entry.carbs,
                            originalFat = entry.fat,
                            calculatedCalories = entry.calories,
                            calculatedProtein = entry.protein,
                            calculatedCarbs = entry.carbs,
                            calculatedFat = entry.fat
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Entry not found") }
                }
            }
            .catch { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
            .launchIn(viewModelScope)
    }

    fun onServingSizeChange(size: String) {
        val sizeValue = size.toDoubleOrNull() ?: return
        _uiState.update { it.copy(servingSize = sizeValue) }
        recalculateNutrition()
    }

    fun onNumberOfServingsChange(servings: String) {
        val servingsValue = servings.toDoubleOrNull() ?: return
        _uiState.update { it.copy(numberOfServings = servingsValue) }
        recalculateNutrition()
    }

    fun onMealTypeChange(mealType: MealType) {
        _uiState.update { it.copy(mealType = mealType) }
    }

    private fun recalculateNutrition() {
        val state = _uiState.value

        // Calculate ratio based on serving changes
        val originalTotal = state.originalServingSize * state.originalNumberOfServings
        val newTotal = state.servingSize * state.numberOfServings

        if (originalTotal > 0) {
            val ratio = newTotal / originalTotal
            _uiState.update {
                it.copy(
                    calculatedCalories = state.originalCalories * ratio,
                    calculatedProtein = state.originalProtein * ratio,
                    calculatedCarbs = state.originalCarbs * ratio,
                    calculatedFat = state.originalFat * ratio
                )
            }
        }
    }

    fun onSave() {
        val state = _uiState.value
        if (state.entry == null) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val params = UpdateDiaryEntryParams(
                mealType = state.mealType,
                servingSize = state.servingSize,
                servingUnit = state.servingUnit,
                numberOfServings = state.numberOfServings
            )

            updateDiaryEntryUseCase(entryId, params)
                .onSuccess {
                    _events.value = EditDiaryEntryEvent.EntrySaved
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false) }
                    _events.value = EditDiaryEntryEvent.ShowError(
                        error.message ?: "Failed to save entry"
                    )
                }
        }
    }

    fun onDeleteClick() {
        _uiState.update { it.copy(showDeleteConfirmation = true) }
    }

    fun onDeleteCancel() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }

    fun onDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirmation = false, isDeleting = true) }

        viewModelScope.launch {
            deleteDiaryEntryUseCase(entryId)
                .onSuccess {
                    _events.value = EditDiaryEntryEvent.EntryDeleted
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isDeleting = false) }
                    _events.value = EditDiaryEntryEvent.ShowError(
                        error.message ?: "Failed to delete entry"
                    )
                }
        }
    }

    fun onEventConsumed() {
        _events.value = null
    }
}
