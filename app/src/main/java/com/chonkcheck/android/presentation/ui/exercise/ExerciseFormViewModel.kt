package com.chonkcheck.android.presentation.ui.exercise

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chonkcheck.android.domain.model.CreateExerciseParams
import com.chonkcheck.android.domain.model.Exercise
import com.chonkcheck.android.domain.model.UpdateExerciseParams
import com.chonkcheck.android.domain.usecase.CreateExerciseUseCase
import com.chonkcheck.android.domain.usecase.GetExerciseByIdUseCase
import com.chonkcheck.android.domain.usecase.UpdateExerciseUseCase
import com.chonkcheck.android.presentation.navigation.NavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ExerciseFormUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val exerciseId: String? = null,
    val date: LocalDate = LocalDate.now(),
    val name: String = "",
    val caloriesBurned: String = "",
    val description: String = "",
    val nameError: String? = null,
    val caloriesError: String? = null,
    val showUnsavedChangesDialog: Boolean = false,
    val hasUnsavedChanges: Boolean = false
)

sealed class ExerciseFormEvent {
    data object NavigateBack : ExerciseFormEvent()
    data object ExerciseSaved : ExerciseFormEvent()
    data class ShowError(val message: String) : ExerciseFormEvent()
}

@HiltViewModel
class ExerciseFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val createExerciseUseCase: CreateExerciseUseCase,
    private val updateExerciseUseCase: UpdateExerciseUseCase,
    private val getExerciseByIdUseCase: GetExerciseByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseFormUiState())
    val uiState: StateFlow<ExerciseFormUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<ExerciseFormEvent?>(null)
    val events: StateFlow<ExerciseFormEvent?> = _events.asStateFlow()

    private var originalExercise: Exercise? = null

    init {
        val exerciseId: String? = savedStateHandle[NavArgs.EXERCISE_ID]
        val dateStr: String? = savedStateHandle[NavArgs.DATE]

        if (exerciseId != null) {
            _uiState.update { it.copy(isEditMode = true, exerciseId = exerciseId, isLoading = true) }
            loadExercise(exerciseId)
        } else if (dateStr != null) {
            val date = LocalDate.parse(dateStr)
            _uiState.update { it.copy(date = date) }
        }
    }

    private fun loadExercise(exerciseId: String) {
        viewModelScope.launch {
            val exercise = getExerciseByIdUseCase(exerciseId).firstOrNull()
            if (exercise != null) {
                originalExercise = exercise
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        date = exercise.date,
                        name = exercise.name,
                        caloriesBurned = exercise.caloriesBurned.toInt().toString(),
                        description = exercise.description ?: ""
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
                _events.value = ExerciseFormEvent.ShowError("Exercise not found")
            }
        }
    }

    fun updateName(value: String) {
        if (value.length <= 200) {
            _uiState.update {
                it.copy(
                    name = value,
                    nameError = null,
                    hasUnsavedChanges = true
                )
            }
        }
    }

    fun updateCaloriesBurned(value: String) {
        val filtered = value.filter { it.isDigit() }
        val intValue = filtered.toIntOrNull() ?: 0
        if (intValue <= 10000) {
            _uiState.update {
                it.copy(
                    caloriesBurned = filtered,
                    caloriesError = null,
                    hasUnsavedChanges = true
                )
            }
        }
    }

    fun updateDescription(value: String) {
        if (value.length <= 500) {
            _uiState.update {
                it.copy(
                    description = value,
                    hasUnsavedChanges = true
                )
            }
        }
    }

    fun saveExercise() {
        val currentState = _uiState.value

        // Validate
        var hasErrors = false

        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            hasErrors = true
        }

        val calories = currentState.caloriesBurned.toDoubleOrNull()
        if (calories == null || calories <= 0) {
            _uiState.update { it.copy(caloriesError = "Enter a valid calorie amount") }
            hasErrors = true
        }

        if (hasErrors) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val result = if (currentState.isEditMode && currentState.exerciseId != null) {
                val params = UpdateExerciseParams(
                    name = currentState.name.trim(),
                    caloriesBurned = calories!!,
                    description = currentState.description.trim().ifEmpty { null },
                    date = currentState.date
                )
                updateExerciseUseCase(currentState.exerciseId, params)
            } else {
                val params = CreateExerciseParams(
                    date = currentState.date,
                    name = currentState.name.trim(),
                    caloriesBurned = calories!!,
                    description = currentState.description.trim().ifEmpty { null }
                )
                createExerciseUseCase(params)
            }

            result.onSuccess {
                _events.value = ExerciseFormEvent.ExerciseSaved
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false) }
                _events.value = ExerciseFormEvent.ShowError(error.message ?: "Failed to save exercise")
            }
        }
    }

    fun onBackPressed() {
        if (_uiState.value.hasUnsavedChanges) {
            _uiState.update { it.copy(showUnsavedChangesDialog = true) }
        } else {
            _events.value = ExerciseFormEvent.NavigateBack
        }
    }

    fun dismissUnsavedChangesDialog() {
        _uiState.update { it.copy(showUnsavedChangesDialog = false) }
    }

    fun discardChanges() {
        _uiState.update { it.copy(showUnsavedChangesDialog = false) }
        _events.value = ExerciseFormEvent.NavigateBack
    }

    fun onEventConsumed() {
        _events.value = null
    }
}
