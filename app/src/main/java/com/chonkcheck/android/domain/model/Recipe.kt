package com.chonkcheck.android.domain.model

data class Recipe(
    val id: String,
    val userId: String,
    val name: String,
    val description: String?,
    val totalServings: Int,
    val servingUnit: RecipeServingUnit,
    val ingredients: List<RecipeIngredient>,
    // Total nutrition
    val totalCalories: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    // Per serving nutrition
    val caloriesPerServing: Double,
    val proteinPerServing: Double,
    val carbsPerServing: Double,
    val fatPerServing: Double,
    val createdAt: Long,
    val updatedAt: Long
)

data class RecipeIngredient(
    val foodId: String,
    val foodName: String,
    val servingSize: Double,
    val servingUnit: ServingUnit,
    val quantity: Double,
    val enteredAmount: Double?,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

enum class RecipeServingUnit(val displayName: String) {
    SERVING("serving"),
    BOWL("bowl"),
    PLATE("plate"),
    PORTION("portion"),
    CUP("cup"),
    PIECE("piece")
}

data class CreateRecipeParams(
    val name: String,
    val description: String?,
    val totalServings: Int,
    val servingUnit: RecipeServingUnit,
    val ingredients: List<RecipeIngredientParams>
)

data class UpdateRecipeParams(
    val name: String?,
    val description: String?,
    val totalServings: Int?,
    val servingUnit: RecipeServingUnit?,
    val ingredients: List<RecipeIngredientParams>?
)

data class RecipeIngredientParams(
    val foodId: String,
    val quantity: Double,
    val enteredAmount: Double?
)
