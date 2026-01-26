package com.chonkcheck.android.data.repository

import com.chonkcheck.android.data.api.MilestoneApi
import com.chonkcheck.android.data.api.dto.MarkMilestoneViewedRequest
import com.chonkcheck.android.data.mappers.toDomain
import com.chonkcheck.android.domain.model.MilestoneData
import com.chonkcheck.android.domain.model.PendingMilestones
import com.chonkcheck.android.domain.repository.MilestoneRepository
import io.sentry.Sentry
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MilestoneRepositoryImpl @Inject constructor(
    private val milestoneApi: MilestoneApi
) : MilestoneRepository {

    override suspend fun getPendingMilestones(): Result<PendingMilestones> {
        return try {
            val response = milestoneApi.getPendingMilestones()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Sentry.captureException(e)
            Result.failure(e)
        }
    }

    override suspend fun markMilestoneViewed(milestone: MilestoneData): Result<Unit> {
        return try {
            val request = MarkMilestoneViewedRequest(
                type = milestone.type.toApiValue(),
                periodEnd = milestone.periodEnd.toString()
            )
            milestoneApi.markMilestoneViewed(request)
            Result.success(Unit)
        } catch (e: Exception) {
            Sentry.captureException(e)
            Result.failure(e)
        }
    }
}
