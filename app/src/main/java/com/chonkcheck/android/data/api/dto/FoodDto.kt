package com.chonkcheck.android.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class FoodDto(
    val id: String,
    val name: String,
    val brand: String? = null,
    val barcode: String? = null,
    val servingSize: Double,
    val servingUnit: String,
    val servingsPerContainer: Double? = null,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double? = null,
    val sugar: Double? = null,
    val sodium: Double? = null,
    val saturatedFat: Double? = null,
    val transFat: Double? = null,
    val cholesterol: Double? = null,
    val addedSugar: Double? = null,
    val vitaminA: Double? = null,
    val vitaminC: Double? = null,
    val vitaminD: Double? = null,
    val calcium: Double? = null,
    val iron: Double? = null,
    val potassium: Double? = null,
    val type: String,
    val userId: String? = null,
    val source: String? = null,
    val verified: Boolean = false,
    val promotionRequested: Boolean = false,
    val promotionRequestedAt: String? = null,
    val overrideOf: String? = null,
    val imageUrl: String? = null,
    val createdAt: String,
    val updatedAt: String? = null,
    val archivedAt: String? = null
)