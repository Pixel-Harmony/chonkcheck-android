package com.chonkcheck.android.presentation.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chonkcheck.android.domain.model.DiaryDay
import com.chonkcheck.android.domain.model.DiaryEntry
import com.chonkcheck.android.domain.model.Exercise
import com.chonkcheck.android.domain.model.MacroProgress
import com.chonkcheck.android.domain.model.MealType
import com.chonkcheck.android.domain.model.WeightUnit
import com.chonkcheck.android.domain.repository.AuthRepository
import com.chonkcheck.android.domain.repository.WeightRepository
import com.chonkcheck.android.domain.usecase.CompleteDayUseCase
import com.chonkcheck.android.domain.usecase.DeleteDiaryEntryUseCase
import com.chonkcheck.android.domain.usecase.DeleteExerciseUseCase
import com.chonkcheck.android.domain.usecase.GetDiaryDayUseCase
import com.chonkcheck.android.domain.usecase.GetUserGoalsUseCase
import com.chonkcheck.android.presentation.ui.diary.components.WeightProjectionData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DiaryUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val diaryDay: DiaryDay? = null,
    val macroProgress: MacroProgress? = null,
    val weightProjection: WeightProjectionData? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val deleteConfirmation: DiaryEntry? = null,
    val deleteExerciseConfirmation: Exercise? = null
)

sealed class DiaryEvent {
    data class NavigateToAddFood(val date: LocalDate, val mealType: MealType) : DiaryEvent()
    data class NavigateToEditEntry(val entryId: String) : DiaryEvent()
    data class NavigateToAddExercise(val date: LocalDate) : DiaryEvent()
    data class NavigateToEditExercise(val exerciseId: String) : DiaryEvent()
    data object ShowDeleteSuccess : DiaryEvent()
    data class ShowError(val message: String) : DiaryEvent()
}

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val getDiaryDayUseCase: GetDiaryDayUseCase,
    private val deleteDiaryEntryUseCase: DeleteDiaryEntryUseCase,
    private val deleteExerciseUseCase: DeleteExerciseUseCase,
    private val completeDayUseCase: CompleteDayUseCase,
    private val getUserGoalsUseCase: GetUserGoalsUseCase,
    private val authRepository: AuthRepository,
    private val weightRepository: WeightRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _refreshTrigger = MutableStateFlow(0)

    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<DiaryEvent?>(null)
    val events: StateFlow<DiaryEvent?> = _events.asStateFlow()

    init {
        observeDiaryDay()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeDiaryDay() {
        combine(_selectedDate, _refreshTrigger) { date, _ -> date }
            .flatMapLatest { date ->
                combine(
                    getDiaryDayUseCase(date),
                    getUserGoalsUseCase(),
                    authRepository.currentUser,
                    weightRepository.getWeightEntries(limit = 1)
                ) { diaryDay, goals, user, weights ->
                    val progress = goals?.let {
                        MacroProgress.calculate(diaryDay.totals, it)
                    }

                    // Calculate weight projection if conditions are met
                    val projection = if (
                        date == LocalDate.now() &&
                        diaryDay.isCompleted &&
                        goals?.tdee != null &&
                        weights.isNotEmpty()
                    ) {
                        val currentWeight = weights.first().weight
                        val weightUnit = user?.unitPreferences?.weightUnit ?: WeightUnit.KG
                        WeightProjectionData(
                            tdee = goals.tdee,
                            todayCalories = diaryDay.totals.calories,
                            currentWeightKg = currentWeight,
                            weightUnit = weightUnit
                        )
                    } else null

                    DiaryData(date, diaryDay, progress, projection)
                }
            }
            .onEach { data ->
                _uiState.update {
                    it.copy(
                        selectedDate = data.date,
                        diaryDay = data.diaryDay,
                        macroProgress = data.progress,
                        weightProjection = data.projection,
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
                        error = error.message ?: "Failed to load diary"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private data class DiaryData(
        val date: LocalDate,
        val diaryDay: DiaryDay,
        val progress: MacroProgress?,
        val projection: WeightProjectionData?
    )

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        _uiState.update { it.copy(isLoading = true) }
    }

    fun previousDay() {
        selectDate(_selectedDate.value.minusDays(1))
    }

    fun nextDay() {
        selectDate(_selectedDate.value.plusDays(1))
    }

    fun onAddFood(mealType: MealType) {
        _events.value = DiaryEvent.NavigateToAddFood(_selectedDate.value, mealType)
    }

    fun onEntryClick(entry: DiaryEntry) {
        _events.value = DiaryEvent.NavigateToEditEntry(entry.id)
    }

    fun onDeleteClick(entry: DiaryEntry) {
        _uiState.update { it.copy(deleteConfirmation = entry) }
    }

    fun onDeleteConfirm() {
        val entry = _uiState.value.deleteConfirmation ?: return
        _uiState.update { it.copy(deleteConfirmation = null) }

        viewModelScope.launch {
            deleteDiaryEntryUseCase(entry.id)
                .onSuccess {
                    _events.value = DiaryEvent.ShowDeleteSuccess
                }
                .onFailure { error ->
                    _events.value = DiaryEvent.ShowError(error.message ?: "Failed to delete entry")
                }
        }
    }

    fun onDeleteCancel() {
        _uiState.update { it.copy(deleteConfirmation = null) }
    }

    fun onAddExercise() {
        _events.value = DiaryEvent.NavigateToAddExercise(_selectedDate.value)
    }

    fun onExerciseClick(exercise: Exercise) {
        _events.value = DiaryEvent.NavigateToEditExercise(exercise.id)
    }

    fun onDeleteExerciseClick(exercise: Exercise) {
        _uiState.update { it.copy(deleteExerciseConfirmation = exercise) }
    }

    fun onDeleteExerciseConfirm() {
        val exercise = _uiState.value.deleteExerciseConfirmation ?: return
        _uiState.update { it.copy(deleteExerciseConfirmation = null) }

        viewModelScope.launch {
            deleteExerciseUseCase(exercise.id)
                .onSuccess {
                    _events.value = DiaryEvent.ShowDeleteSuccess
                }
                .onFailure { error ->
                    _events.value = DiaryEvent.ShowError(error.message ?: "Failed to delete exercise")
                }
        }
    }

    fun onDeleteExerciseCancel() {
        _uiState.update { it.copy(deleteExerciseConfirmation = null) }
    }

    fun onCompleteDay() {
        viewModelScope.launch {
            completeDayUseCase.complete(_selectedDate.value)
                .onFailure { error ->
                    _events.value = DiaryEvent.ShowError(error.message ?: "Failed to complete day")
                }
        }
    }

    fun onReopenDay() {
        viewModelScope.launch {
            completeDayUseCase.uncomplete(_selectedDate.value)
                .onFailure { error ->
                    _events.value = DiaryEvent.ShowError(error.message ?: "Failed to reopen day")
                }
        }
    }

    fun onEventConsumed() {
        _events.value = null
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        _refreshTrigger.value++
    }
}
