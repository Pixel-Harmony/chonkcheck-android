package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.MilestoneData
import com.chonkcheck.android.domain.repository.MilestoneRepository
import javax.inject.Inject

/**
 * Use case to get the next pending milestone that should be shown to the user.
 */
class GetPendingMilestoneUseCase @Inject constructor(
    private val milestoneRepository: MilestoneRepository
) {
    /**
     * Get the next pending milestone.
     * Returns null if there are no pending milestones.
     */
    suspend operator fun invoke(): Result<MilestoneData?> {
        return milestoneRepository.getPendingMilestones().map { pendingMilestones ->
            pendingMilestones.getNextMilestone()
        }
    }
}
