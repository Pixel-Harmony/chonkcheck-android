package com.chonkcheck.android.presentation.ui.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chonkcheck.android.domain.model.CreateWeightParams
import com.chonkcheck.android.domain.model.WeightChartPoint
import com.chonkcheck.android.domain.model.WeightEntry
import com.chonkcheck.android.domain.model.WeightStats
import com.chonkcheck.android.domain.model.WeightTrendPrediction
import com.chonkcheck.android.domain.model.WeightUnit
import com.chonkcheck.android.domain.repository.AuthRepository
import com.chonkcheck.android.domain.usecase.CalculateWeightTrendUseCase
import com.chonkcheck.android.domain.usecase.CreateWeightEntryUseCase
import com.chonkcheck.android.domain.usecase.DeleteWeightEntryUseCase
import com.chonkcheck.android.domain.usecase.GetWeightEntriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class WeightUiState(
    val entries: List<WeightEntry> = emptyList(),
    val stats: WeightStats? = null,
    val chartData: List<WeightChartPoint> = emptyList(),
    val trendPrediction: WeightTrendPrediction? = null,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSaving: Boolean = false,
    val deleteConfirmation: WeightEntry? = null,
    val showSaveSuccess: Boolean = false
)

sealed class WeightEvent {
    data object ShowSaveSuccess : WeightEvent()
    data object ShowDeleteSuccess : WeightEvent()
    data class ShowError(val message: String) : WeightEvent()
}

@HiltViewModel
class WeightViewModel @Inject constructor(
    private val getWeightEntriesUseCase: GetWeightEntriesUseCase,
    private val createWeightEntryUseCase: CreateWeightEntryUseCase,
    private val deleteWeightEntryUseCase: DeleteWeightEntryUseCase,
    private val calculateWeightTrendUseCase: CalculateWeightTrendUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeightUiState())
    val uiState: StateFlow<WeightUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<WeightEvent?>(null)
    val events: StateFlow<WeightEvent?> = _events.asStateFlow()

    init {
        observeWeightEntries()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeWeightEntries() {
        combine(
            getWeightEntriesUseCase(),
            authRepository.currentUser
        ) { entries, user ->
            val weightUnit = user?.unitPreferences?.weightUnit ?: WeightUnit.KG
            val stats = calculateWeightTrendUseCase.calculateStats(entries)
            val prediction = calculateWeightTrendUseCase.calculateTrendPrediction(entries)
            val chartData = calculateWeightTrendUseCase.generateChartData(entries, prediction)

            WeightData(
                entries = entries,
                stats = stats,
                chartData = chartData,
                prediction = prediction,
                weightUnit = weightUnit
            )
        }
            .onEach { data ->
                _uiState.update {
                    it.copy(
                        entries = data.entries,
                        stats = data.stats,
                        chartData = data.chartData,
                        trendPrediction = data.prediction,
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
                        error = error.message ?: "Failed to load weight entries"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun logWeight(weightInKg: Double, date: LocalDate, notes: String?) {
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            createWeightEntryUseCase(
                CreateWeightParams(
                    weight = weightInKg,
                    date = date,
                    notes = notes?.takeIf { it.isNotBlank() }
                )
            )
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, showSaveSuccess = true) }
                    _events.value = WeightEvent.ShowSaveSuccess
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false) }
                    _events.value = WeightEvent.ShowError(error.message ?: "Failed to save weight")
                }
        }
    }

    fun onDeleteClick(entry: WeightEntry) {
        _uiState.update { it.copy(deleteConfirmation = entry) }
    }

    fun onDeleteConfirm() {
        val entry = _uiState.value.deleteConfirmation ?: return
        _uiState.update { it.copy(deleteConfirmation = null) }

        viewModelScope.launch {
            deleteWeightEntryUseCase(entry.date)
                .onSuccess {
                    _events.value = WeightEvent.ShowDeleteSuccess
                }
                .onFailure { error ->
                    _events.value = WeightEvent.ShowError(error.message ?: "Failed to delete entry")
                }
        }
    }

    fun onDeleteCancel() {
        _uiState.update { it.copy(deleteConfirmation = null) }
    }

    fun onEventConsumed() {
        _events.value = null
    }

    fun dismissSaveSuccess() {
        _uiState.update { it.copy(showSaveSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private data class WeightData(
        val entries: List<WeightEntry>,
        val stats: WeightStats,
        val chartData: List<WeightChartPoint>,
        val prediction: WeightTrendPrediction?,
        val weightUnit: WeightUnit
    )
}
