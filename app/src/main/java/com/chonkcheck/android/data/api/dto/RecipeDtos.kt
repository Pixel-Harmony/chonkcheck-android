package com.chonkcheck.android.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class RecipeDto(
    val id: String,
    val userId: String,
    val name: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val servings: Int,
    val servingSize: Double? = null,
    val servingUnit: String? = null,
    val caloriesPerServing: Double,
    val proteinPerServing: Double,
    val carbsPerServing: Double,
    val fatPerServing: Double,
    val ingredients: List<RecipeIngredientDto>,
    val instructions: String? = null,
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val createdAt: String,
    val updatedAt: String? = null
)

@Serializable
data class RecipeIngredientDto(
    val foodId: String,
    val foodName: String,
    val servingSize: Double,
    val servingUnit: String,
    val numberOfServings: Double,
    val enteredAmount: Double? = null,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

@Serializable
data class RecipesResponse(
    val recipes: List<RecipeDto>,
    val total: Int
)

@Serializable
data class CreateRecipeRequest(
    val name: String,
    val description: String? = null,
    val totalServings: Int,
    val servingUnit: String,
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
    val totalServings: Int? = null,
    val servingUnit: String? = null,
    val ingredients: List<CreateRecipeIngredientRequest>? = null
)
