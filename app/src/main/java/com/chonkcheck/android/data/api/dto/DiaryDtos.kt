package com.chonkcheck.android.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiaryDayResponse(
    val date: String,
    val entries: List<DiaryEntryDto>,
    val isCompleted: Boolean = false
)

@Serializable
data class DiaryEntryDto(
    val id: String,
    val date: String,
    @SerialName("meal")
    val mealType: String,
    val itemType: String,
    val foodId: String? = null,
    val recipeId: String? = null,
    val quantity: Double,
    val timestamp: String,
    val food: DiaryFoodDto? = null,
    val savedRecipe: DiaryRecipeDto? = null,
    val nutrition: NutritionDto
) {
    val name: String
        get() = food?.name ?: savedRecipe?.name ?: "Unknown"

    val brand: String?
        get() = food?.brand
}

@Serializable
data class DiaryFoodDto(
    val name: String,
    val brand: String? = null,
    val servingSize: Double,
    val servingUnit: String
)

@Serializable
data class DiaryRecipeDto(
    val name: String,
    val totalServings: Int,
    val servingUnit: String,
    val ingredientCount: Int
)

@Serializable
data class NutritionDto(
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
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
