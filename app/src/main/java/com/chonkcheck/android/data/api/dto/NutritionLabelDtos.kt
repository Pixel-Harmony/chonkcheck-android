package com.chonkcheck.android.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ScanNutritionLabelRequest(
    val image: String,
    val mediaType: String
)

@Serializable
data class ScanNutritionLabelResponse(
    val success: Boolean,
    val nutritionData: NutritionDataDto? = null,
    val message: String? = null
)

@Serializable
data class NutritionDataDto(
    val name: String,
    val brand: String? = null,
    val servingSize: Double,
    val servingUnit: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double? = null,
    val sugar: Double? = null,
    val sodium: Double? = null,
    val saturatedFat: Double? = null,
    val cholesterol: Double? = null
)
