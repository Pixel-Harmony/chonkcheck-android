package com.chonkcheck.android.presentation.ui.scanner

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chonkcheck.android.domain.model.NutritionLabelData
import com.chonkcheck.android.domain.model.NutritionLabelScanResult
import com.chonkcheck.android.domain.usecase.ScanNutritionLabelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ScannerUiState {
    data object Idle : ScannerUiState()
    data class Captured(val imageUri: Uri) : ScannerUiState()
    data object Processing : ScannerUiState()
    data class Success(val data: NutritionLabelData) : ScannerUiState()
    data class Error(val message: String) : ScannerUiState()
}

sealed class ScannerEvent {
    data class LabelScanned(val data: NutritionLabelData) : ScannerEvent()
    data object NavigateBack : ScannerEvent()
}

@HiltViewModel
class NutritionLabelScannerViewModel @Inject constructor(
    private val scanNutritionLabelUseCase: ScanNutritionLabelUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<ScannerEvent?>(null)
    val events: StateFlow<ScannerEvent?> = _events.asStateFlow()

    fun onPhotoCaptured(imageUri: Uri) {
        _uiState.value = ScannerUiState.Captured(imageUri)
    }

    fun onRetake() {
        _uiState.value = ScannerUiState.Idle
    }

    fun onUsePhoto(imageBase64: String) {
        _uiState.value = ScannerUiState.Processing

        viewModelScope.launch {
            when (val result = scanNutritionLabelUseCase(imageBase64)) {
                is NutritionLabelScanResult.Success -> {
                    _uiState.value = ScannerUiState.Success(result.data)
                    _events.value = ScannerEvent.LabelScanned(result.data)
                }
                is NutritionLabelScanResult.Error -> {
                    _uiState.value = ScannerUiState.Error(result.message)
                }
            }
        }
    }

    fun onDismissError() {
        _uiState.value = ScannerUiState.Idle
    }

    fun onEventConsumed() {
        _events.value = null
    }
}
