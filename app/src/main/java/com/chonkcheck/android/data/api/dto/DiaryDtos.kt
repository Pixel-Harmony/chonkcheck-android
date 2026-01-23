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
    val food: FoodDto? = null,
    val savedRecipe: RecipeDto? = null
) {
    val name: String
        get() = food?.name ?: savedRecipe?.name ?: "Unknown"

    val brand: String?
        get() = food?.brand

    val calories: Double
        get() = when {
            food != null -> food.calories * quantity
            savedRecipe != null -> savedRecipe.caloriesPerServing * quantity
            else -> 0.0
        }

    val protein: Double
        get() = when {
            food != null -> food.protein * quantity
            savedRecipe != null -> savedRecipe.proteinPerServing * quantity
            else -> 0.0
        }

    val carbs: Double
        get() = when {
            food != null -> food.carbs * quantity
            savedRecipe != null -> savedRecipe.carbsPerServing * quantity
            else -> 0.0
        }

    val fat: Double
        get() = when {
            food != null -> food.fat * quantity
            savedRecipe != null -> savedRecipe.fatPerServing * quantity
            else -> 0.0
        }
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

