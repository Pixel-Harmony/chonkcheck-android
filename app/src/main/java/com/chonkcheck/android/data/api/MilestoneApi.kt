package com.chonkcheck.android.data.api

import com.chonkcheck.android.data.api.dto.MarkMilestoneViewedRequest
import com.chonkcheck.android.data.api.dto.MilestonesResponse
import com.chonkcheck.android.data.api.dto.SuccessResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MilestoneApi {

    @GET("weight/milestones")
    suspend fun getPendingMilestones(): MilestonesResponse

    @POST("user/milestones/viewed")
    suspend fun markMilestoneViewed(@Body request: MarkMilestoneViewedRequest): SuccessResponse
}
