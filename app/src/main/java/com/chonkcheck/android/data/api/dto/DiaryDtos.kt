package com.chonkcheck.android.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiaryEntriesResponse(
    val entries: List<DiaryEntryDto>,
    val completed: Boolean = false
)

@Serializable
data class DiaryEntryDto(
    val id: String,
    val userId: String,
    val date: String,
    @SerialName("meal")
    val mealType: String,
    val foodId: String? = null,
    val recipeId: String? = null,
    val quantity: Double,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val name: String,
    val brand: String? = null,
    val createdAt: String,
    val updatedAt: String? = null
)

@Serializable
data class CreateDiaryEntryRequest(
    val date: String,
    val meal: String,
    val foodId: String? = null,
    val recipeId: String? = null,
    val quantity: Double
)

@Serializable
data class UpdateDiaryEntryRequest(
    val meal: String? = null,
    val quantity: Double? = null
)
