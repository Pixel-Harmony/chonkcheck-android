package com.chonkcheck.android.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateFoodRequest(
    val name: String,
    val brand: String? = null,
    val barcode: String? = null,

    @SerialName("serving_size")
    val servingSize: Double,

    @SerialName("serving_unit")
    val servingUnit: String,

    @SerialName("servings_per_container")
    val servingsPerContainer: Double? = null,

    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,

    val fiber: Double? = null,
    val sugar: Double? = null,
    val sodium: Double? = null,

    @SerialName("saturated_fat")
    val saturatedFat: Double? = null,

    @SerialName("trans_fat")
    val transFat: Double? = null,

    val cholesterol: Double? = null,

    @SerialName("added_sugar")
    val addedSugar: Double? = null,

    @SerialName("image_url")
    val imageUrl: String? = null
)

@Serializable
data class UpdateFoodRequest(
    val name: String? = null,
    val brand: String? = null,
    val barcode: String? = null,

    @SerialName("serving_size")
    val servingSize: Double? = null,

    @SerialName("serving_unit")
    val servingUnit: String? = null,

    @SerialName("servings_per_container")
    val servingsPerContainer: Double? = null,

    val calories: Double? = null,
    val protein: Double? = null,
    val carbs: Double? = null,
    val fat: Double? = null,

    val fiber: Double? = null,
    val sugar: Double? = null,
    val sodium: Double? = null,

    @SerialName("saturated_fat")
    val saturatedFat: Double? = null,

    @SerialName("trans_fat")
    val transFat: Double? = null,

    val cholesterol: Double? = null,

    @SerialName("added_sugar")
    val addedSugar: Double? = null,

    @SerialName("image_url")
    val imageUrl: String? = null
)
