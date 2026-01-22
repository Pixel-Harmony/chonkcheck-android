package com.chonkcheck.android.presentation.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chonkcheck.android.domain.model.DiaryDay
import com.chonkcheck.android.domain.model.MacroProgress
import com.chonkcheck.android.domain.model.WeightStats
import com.chonkcheck.android.domain.model.WeightUnit
import com.chonkcheck.android.domain.repository.AuthRepository
import com.chonkcheck.android.domain.usecase.CalculateWeightTrendUseCase
import com.chonkcheck.android.domain.usecase.GetDiaryDayUseCase
import com.chonkcheck.android.domain.usecase.GetUserGoalsUseCase
import com.chonkcheck.android.domain.usecase.GetWeightEntriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt

data class DashboardUiState(
    val currentDate: LocalDate = LocalDate.now(),
    val macroProgress: MacroProgress? = null,
    val todayCalories: Int = 0,
    val todayEntryCount: Int = 0,
    val latestWeight: Double? = null,
    val weightStats: WeightStats? = null,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDiaryDayUseCase: GetDiaryDayUseCase,
    private val getWeightEntriesUseCase: GetWeightEntriesUseCase,
    private val getUserGoalsUseCase: GetUserGoalsUseCase,
    private val calculateWeightTrendUseCase: CalculateWeightTrendUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeDashboardData()
    }

    private fun observeDashboardData() {
        combine(
            authRepository.currentUser,
            getDiaryDayUseCase(LocalDate.now()),
            getWeightEntriesUseCase(),
            getUserGoalsUseCase()
        ) { user, diaryDay, weightEntries, goals ->
            val weightUnit = user?.unitPreferences?.weightUnit ?: WeightUnit.KG
            val weightStats = calculateWeightTrendUseCase.calculateStats(weightEntries)
            val latestWeight = weightEntries.maxByOrNull { it.date }?.weight

            val macroProgress = goals?.let {
                MacroProgress.calculate(diaryDay.totals, it)
            }

            DashboardData(
                macroProgress = macroProgress,
                todayCalories = diaryDay.totals.calories.roundToInt(),
                todayEntryCount = diaryDay.allEntries.size,
                latestWeight = latestWeight,
                weightStats = weightStats,
                weightUnit = weightUnit
            )
        }
            .onEach { data ->
                _uiState.update {
                    it.copy(
                        macroProgress = data.macroProgress,
                        todayCalories = data.todayCalories,
                        todayEntryCount = data.todayEntryCount,
                        latestWeight = data.latestWeight,
                        weightStats = data.weightStats,
                        weightUnit = data.weightUnit,
                        isLoading = false,
                        error = null
                    )
                }
            }
            .catch { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load dashboard"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private data class DashboardData(
        val macroProgress: MacroProgress?,
        val todayCalories: Int,
        val todayEntryCount: Int,
        val latestWeight: Double?,
        val weightStats: WeightStats?,
        val weightUnit: WeightUnit
    )
}
