package com.chonkcheck.android.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class FoodsResponse(
    val foods: List<FoodDto>,
    val recipes: List<RecipeDto>? = null,
    val meals: List<SavedMealDto>? = null
)

@Serializable
data class SavedMealItemDto(
    val itemId: String,
    val itemType: String,
    val itemName: String,
    val quantity: Double,
    val enteredAmount: Double? = null,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

@Serializable
data class SavedMealDto(
    val id: String,
    val userId: String,
    val name: String,
    val items: List<SavedMealItemDto>,
    val totalCalories: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val createdAt: String,
    val updatedAt: String,
    val archivedAt: String? = null
)
