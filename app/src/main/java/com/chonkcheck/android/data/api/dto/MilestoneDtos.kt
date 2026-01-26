package com.chonkcheck.android.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class MilestoneDataDto(
    val type: String,
    val periodStart: String,
    val periodEnd: String,
    val periodLabel: String,
    val startWeight: Double,
    val endWeight: Double,
    val change: Double,
    val changeFormatted: String,
    val totalLost: Double,
    val totalLostFormatted: String,
    val outcome: String
)

@Serializable
data class MilestonesResponse(
    val weekly: MilestoneDataDto? = null,
    val monthly: MilestoneDataDto? = null
)

@Serializable
data class MarkMilestoneViewedRequest(
    val type: String,
    val periodEnd: String
)
