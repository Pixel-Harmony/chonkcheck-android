package com.chonkcheck.android.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class RecipeDto(
    val id: String,
    val userId: String,
    val name: String,
    val description: String? = null,
    val totalServings: Double,
    val servingUnit: String,
    val ingredients: List<RecipeIngredientDto>,
    val totalCalories: Double? = null,
    val totalProtein: Double? = null,
    val totalCarbs: Double? = null,
    val totalFat: Double? = null,
    val totalFiber: Double? = null,
    val totalSugar: Double? = null,
    val totalSodium: Double? = null,
    val caloriesPerServing: Double,
    val proteinPerServing: Double,
    val carbsPerServing: Double,
    val fatPerServing: Double,
    val fiberPerServing: Double? = null,
    val sugarPerServing: Double? = null,
    val sodiumPerServing: Double? = null,
    val createdAt: String,
    val updatedAt: String,
    val archivedAt: String? = null
)

@Serializable
data class RecipeIngredientDto(
    val foodId: String,
    val foodName: String,
    val quantity: Double,
    val enteredAmount: Double? = null,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double? = null,
    val sugar: Double? = null,
    val sodium: Double? = null
)

@Serializable
data class RecipesResponse(
    val recipes: List<RecipeDto>
)

@Serializable
data class CreateRecipeRequest(
    val name: String,
    val description: String? = null,
    val totalServings: Double,
    val servingUnit: String = "serving",
    val ingredients: List<CreateRecipeIngredientRequest>
)

@Serializable
data class CreateRecipeIngredientRequest(
    val foodId: String,
    val quantity: Double,
    val enteredAmount: Double? = null
)

@Serializable
data class UpdateRecipeRequest(
    val name: String? = null,
    val description: String? = null,
    val totalServings: Double? = null,
    val servingUnit: String? = null,
    val ingredients: List<CreateRecipeIngredientRequest>? = null
)
