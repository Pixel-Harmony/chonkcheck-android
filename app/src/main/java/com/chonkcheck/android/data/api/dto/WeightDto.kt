package com.chonkcheck.android.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class WeightEntriesResponse(
    val entries: List<WeightEntryDto>
)

@Serializable
data class WeightEntryDto(
    val id: String,
    val userId: String,
    val date: String,
    val weight: Double, // kg
    val notes: String? = null,
    val createdAt: String,
    val updatedAt: String? = null
)

@Serializable
data class CreateWeightRequest(
    val date: String,
    val weight: Double, // kg
    val notes: String? = null
)
