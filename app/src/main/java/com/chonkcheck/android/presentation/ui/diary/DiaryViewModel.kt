package com.chonkcheck.android.presentation.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chonkcheck.android.domain.model.DiaryDay
import com.chonkcheck.android.domain.model.DiaryEntry
import com.chonkcheck.android.domain.model.MacroProgress
import com.chonkcheck.android.domain.model.MealType
import com.chonkcheck.android.domain.usecase.CompleteDayUseCase
import com.chonkcheck.android.domain.usecase.DeleteDiaryEntryUseCase
import com.chonkcheck.android.domain.usecase.GetDiaryDayUseCase
import com.chonkcheck.android.domain.usecase.GetUserGoalsUseCase
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
    val isLoading: Boolean = true,
    val error: String? = null,
    val deleteConfirmation: DiaryEntry? = null
)

sealed class DiaryEvent {
    data class NavigateToAddFood(val date: LocalDate, val mealType: MealType) : DiaryEvent()
    data class NavigateToEditEntry(val entryId: String) : DiaryEvent()
    data object ShowDeleteSuccess : DiaryEvent()
    data class ShowError(val message: String) : DiaryEvent()
}

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val getDiaryDayUseCase: GetDiaryDayUseCase,
    private val deleteDiaryEntryUseCase: DeleteDiaryEntryUseCase,
    private val completeDayUseCase: CompleteDayUseCase,
    private val getUserGoalsUseCase: GetUserGoalsUseCase
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())

    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<DiaryEvent?>(null)
    val events: StateFlow<DiaryEvent?> = _events.asStateFlow()

    init {
        observeDiaryDay()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeDiaryDay() {
        _selectedDate
            .flatMapLatest { date ->
                combine(
                    getDiaryDayUseCase(date),
                    getUserGoalsUseCase()
                ) { diaryDay, goals ->
                    val progress = goals?.let {
                        MacroProgress.calculate(diaryDay.totals, it)
                    }
                    Triple(date, diaryDay, progress)
                }
            }
            .onEach { (date, diaryDay, progress) ->
                _uiState.update {
                    it.copy(
                        selectedDate = date,
                        diaryDay = diaryDay,
                        macroProgress = progress,
                        isLoading = false,
                        error = null
                    )
                }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load diary"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

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
}
