package com.chonkcheck.android.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateFoodRequest(
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
    val imageUrl: String? = null
)

@Serializable
data class UpdateFoodRequest(
    val name: String? = null,
    val brand: String? = null,
    val barcode: String? = null,
    val servingSize: Double? = null,
    val servingUnit: String? = null,
    val servingsPerContainer: Double? = null,
    val calories: Double? = null,
    val protein: Double? = null,
    val carbs: Double? = null,
    val fat: Double? = null,
    val fiber: Double? = null,
    val sugar: Double? = null,
    val sodium: Double? = null,
    val saturatedFat: Double? = null,
    val transFat: Double? = null,
    val cholesterol: Double? = null,
    val addedSugar: Double? = null,
    val imageUrl: String? = null
)
