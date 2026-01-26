package com.chonkcheck.android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chonkcheck.android.domain.model.MilestoneData
import com.chonkcheck.android.domain.usecase.GetPendingMilestoneUseCase
import com.chonkcheck.android.domain.usecase.MarkMilestoneViewedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing milestone celebrations.
 */
@HiltViewModel
class MilestoneViewModel @Inject constructor(
    private val getPendingMilestoneUseCase: GetPendingMilestoneUseCase,
    private val markMilestoneViewedUseCase: MarkMilestoneViewedUseCase
) : ViewModel() {

    private val _pendingMilestone = MutableStateFlow<MilestoneData?>(null)
    val pendingMilestone: StateFlow<MilestoneData?> = _pendingMilestone.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        checkForPendingMilestone()
    }

    /**
     * Check for any pending milestones that should be shown.
     */
    fun checkForPendingMilestone() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = getPendingMilestoneUseCase()
            result.onSuccess { milestone ->
                _pendingMilestone.value = milestone
            }
            _isLoading.value = false
        }
    }

    /**
     * Dismiss the current milestone and mark it as viewed.
     */
    fun dismissMilestone() {
        val milestone = _pendingMilestone.value ?: return

        viewModelScope.launch {
            // Immediately hide the modal
            _pendingMilestone.value = null

            // Mark as viewed in the background
            markMilestoneViewedUseCase(milestone)

            // Check if there's another milestone to show
            checkForPendingMilestone()
        }
    }
}
