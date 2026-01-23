package com.chonkcheck.android.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class WeightEntriesResponse(
    val weights: List<WeightEntryDto>,
    val count: Int? = null
)

@Serializable
data class WeightEntryDto(
    val date: String,
    val weight: Double,
    val unit: String,
    val notes: String? = null,
    val createdAt: String? = null
)

@Serializable
data class CreateWeightRequest(
    val weight: Double,
    val date: String,
    val unit: String? = null,
    val notes: String? = null
)
