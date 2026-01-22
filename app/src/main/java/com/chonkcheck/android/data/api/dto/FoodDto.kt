package com.chonkcheck.android.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FoodDto(
    val id: String,
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

    @SerialName("vitamin_a")
    val vitaminA: Double? = null,

    @SerialName("vitamin_c")
    val vitaminC: Double? = null,

    @SerialName("vitamin_d")
    val vitaminD: Double? = null,

    val calcium: Double? = null,
    val iron: Double? = null,
    val potassium: Double? = null,

    val type: String,

    @SerialName("user_id")
    val userId: String? = null,

    val source: String? = null,
    val verified: Boolean = false,

    @SerialName("promotion_requested")
    val promotionRequested: Boolean = false,

    @SerialName("promotion_requested_at")
    val promotionRequestedAt: String? = null,

    @SerialName("override_of")
    val overrideOf: String? = null,

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String? = null,

    @SerialName("archived_at")
    val archivedAt: String? = null
)
