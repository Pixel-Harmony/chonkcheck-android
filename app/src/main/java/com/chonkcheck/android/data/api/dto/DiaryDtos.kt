package com.chonkcheck.android.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiaryTotalsDto(
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val exerciseCalories: Double? = null,
    val netCalories: Double? = null
)

@Serializable
data class ExerciseDto(
    val id: String,
    val name: String,
    val caloriesBurned: Double,
    val description: String? = null,
    val date: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class DiaryDayResponse(
    val date: String,
    val entries: List<DiaryEntryDto>,
    val byMeal: Map<String, List<DiaryEntryDto>>? = null,
    val totals: DiaryTotalsDto? = null,
    val exercises: List<ExerciseDto>? = null,
    val isCompleted: Boolean = false
)

/**
 * Nutrition data included in diary entries.
 * This is the pre-calculated nutrition for the entry (already multiplied by quantity).
 */
@Serializable
data class DiaryEntryNutritionDto(
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

/**
 * Simplified recipe data embedded in diary entries.
 * This is NOT the full RecipeDto - it's a minimal representation.
 */
@Serializable
data class DiaryEntrySavedRecipeDto(
    val name: String,
    val totalServings: Double? = null,
    val servingUnit: String? = null,
    val ingredientCount: Int? = null
)

/**
 * Simplified food data embedded in diary entries.
 * This is NOT the full FoodDto - it's a minimal representation for display.
 */
@Serializable
data class DiaryEntryFoodDto(
    val name: String,
    val brand: String? = null,
    val servingSize: Double? = null,
    val servingUnit: String? = null
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
    val enteredAmount: Double? = null,
    val mealGroupId: String? = null,
    val mealGroupName: String? = null,
    val food: DiaryEntryFoodDto? = null,
    val savedRecipe: DiaryEntrySavedRecipeDto? = null,
    val nutrition: DiaryEntryNutritionDto? = null
) {
    val name: String
        get() = food?.name ?: savedRecipe?.name ?: "Unknown"

    val brand: String?
        get() = food?.brand

    // Use the pre-calculated nutrition from the API (always available in diary entries)
    val calories: Double
        get() = nutrition?.calories ?: 0.0

    val protein: Double
        get() = nutrition?.protein ?: 0.0

    val carbs: Double
        get() = nutrition?.carbs ?: 0.0

    val fat: Double
        get() = nutrition?.fat ?: 0.0
}

@Serializable
data class CreateDiaryEntryRequest(
    val foodId: String? = null,
    val recipeId: String? = null,
    val itemType: String? = null,
    val quantity: Double,
    val date: String,
    val meal: String,
    val enteredAmount: Double? = null
)

