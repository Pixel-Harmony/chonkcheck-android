package com.chonkcheck.android.domain.repository

import com.chonkcheck.android.domain.model.MilestoneData
import com.chonkcheck.android.domain.model.PendingMilestones

interface MilestoneRepository {
    /**
     * Get pending milestones that haven't been viewed yet.
     */
    suspend fun getPendingMilestones(): Result<PendingMilestones>

    /**
     * Mark a milestone as viewed.
     */
    suspend fun markMilestoneViewed(milestone: MilestoneData): Result<Unit>
}
