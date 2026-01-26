package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.MilestoneData
import com.chonkcheck.android.domain.repository.MilestoneRepository
import javax.inject.Inject

/**
 * Use case to mark a milestone as viewed.
 */
class MarkMilestoneViewedUseCase @Inject constructor(
    private val milestoneRepository: MilestoneRepository
) {
    suspend operator fun invoke(milestone: MilestoneData): Result<Unit> {
        return milestoneRepository.markMilestoneViewed(milestone)
    }
}
