package com.chonkcheck.android.data.mappers

import com.chonkcheck.android.data.api.dto.CreateRecipeIngredientRequest
import com.chonkcheck.android.data.api.dto.CreateRecipeRequest
import com.chonkcheck.android.data.api.dto.RecipeDto
import com.chonkcheck.android.data.api.dto.RecipeIngredientDto
import com.chonkcheck.android.data.api.dto.UpdateRecipeRequest
import com.chonkcheck.android.data.db.entity.RecipeEntity
import com.chonkcheck.android.data.db.entity.RecipeIngredientJson
import com.chonkcheck.android.domain.model.CreateRecipeParams
import com.chonkcheck.android.domain.model.Recipe
import com.chonkcheck.android.domain.model.RecipeIngredient
import com.chonkcheck.android.domain.model.RecipeIngredientParams
import com.chonkcheck.android.domain.model.RecipeServingUnit
import com.chonkcheck.android.domain.model.ServingUnit
import com.chonkcheck.android.domain.model.UpdateRecipeParams
import kotlinx.serialization.encodeToString

fun RecipeDto.toEntity(): RecipeEntity {
    val ingredientsJson = mapperJson.encodeToString(
        ingredients.map { it.toIngredientJson() }
    )

    return RecipeEntity(
        id = id,
        userId = userId,
        name = name,
        description = description,
        imageUrl = null,
        servings = totalServings.toInt(),
        servingSize = null,
        servingUnit = servingUnit,
        caloriesPerServing = caloriesPerServing,
        proteinPerServing = proteinPerServing,
        carbsPerServing = carbsPerServing,
        fatPerServing = fatPerServing,
        ingredientsJson = ingredientsJson,
        instructions = null,
        prepTimeMinutes = null,
        cookTimeMinutes = null,
        syncedAt = System.currentTimeMillis(),
        createdAt = createdAt.parseTimestamp() ?: System.currentTimeMillis(),
        updatedAt = updatedAt.parseTimestamp() ?: System.currentTimeMillis(),
        deletedAt = if (archivedAt != null) archivedAt.parseTimestamp() else null
    )
}

fun RecipeIngredientDto.toIngredientJson(): RecipeIngredientJson {
    return RecipeIngredientJson(
        foodId = foodId,
        foodName = foodName,
        servingSize = 1.0, // Base serving
        servingUnit = "serving",
        numberOfServings = quantity,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat
    )
}

fun RecipeEntity.toDomain(): Recipe {
    val ingredientsList = try {
        mapperJson.decodeFromString<List<RecipeIngredientJson>>(ingredientsJson)
    } catch (e: Exception) {
        emptyList()
    }

    val ingredients = ingredientsList.map { it.toDomain() }
    val totalCalories = ingredients.sumOf { it.calories }
    val totalProtein = ingredients.sumOf { it.protein }
    val totalCarbs = ingredients.sumOf { it.carbs }
    val totalFat = ingredients.sumOf { it.fat }

    return Recipe(
        id = id,
        userId = userId,
        name = name,
        description = description,
        totalServings = servings,
        servingUnit = servingUnit.toRecipeServingUnit(),
        ingredients = ingredients,
        totalCalories = totalCalories,
        totalProtein = totalProtein,
        totalCarbs = totalCarbs,
        totalFat = totalFat,
        caloriesPerServing = caloriesPerServing,
        proteinPerServing = proteinPerServing,
        carbsPerServing = carbsPerServing,
        fatPerServing = fatPerServing,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun RecipeIngredientJson.toDomain(): RecipeIngredient {
    return RecipeIngredient(
        foodId = foodId,
        foodName = foodName,
        servingSize = servingSize,
        servingUnit = servingUnit.toServingUnit(),
        quantity = numberOfServings,
        enteredAmount = null,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat
    )
}

fun CreateRecipeParams.toRequest(): CreateRecipeRequest {
    return CreateRecipeRequest(
        name = name,
        description = description,
        totalServings = totalServings.toDouble(),
        servingUnit = servingUnit.toApiValue(),
        ingredients = ingredients.map { it.toRequest() }
    )
}

fun RecipeIngredientParams.toRequest(): CreateRecipeIngredientRequest {
    return CreateRecipeIngredientRequest(
        foodId = foodId,
        quantity = quantity,
        enteredAmount = enteredAmount
    )
}

fun UpdateRecipeParams.toRequest(): UpdateRecipeRequest {
    return UpdateRecipeRequest(
        name = name,
        description = description,
        totalServings = totalServings?.toDouble(),
        servingUnit = servingUnit?.toApiValue(),
        ingredients = ingredients?.map { it.toRequest() }
    )
}

fun CreateRecipeParams.toEntity(
    id: String,
    userId: String,
    ingredientDetails: List<RecipeIngredientJson>
): RecipeEntity {
    val totalCalories = ingredientDetails.sumOf { it.calories }
    val totalProtein = ingredientDetails.sumOf { it.protein }
    val totalCarbs = ingredientDetails.sumOf { it.carbs }
    val totalFat = ingredientDetails.sumOf { it.fat }

    return RecipeEntity(
        id = id,
        userId = userId,
        name = name,
        description = description,
        imageUrl = null,
        servings = totalServings,
        servingSize = null,
        servingUnit = servingUnit.toApiValue(),
        caloriesPerServing = totalCalories / totalServings,
        proteinPerServing = totalProtein / totalServings,
        carbsPerServing = totalCarbs / totalServings,
        fatPerServing = totalFat / totalServings,
        ingredientsJson = mapperJson.encodeToString(ingredientDetails),
        instructions = null,
        prepTimeMinutes = null,
        cookTimeMinutes = null,
        syncedAt = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        deletedAt = null
    )
}

private fun String?.toRecipeServingUnit(): RecipeServingUnit {
    return when (this?.lowercase()) {
        "serving" -> RecipeServingUnit.SERVING
        "bowl" -> RecipeServingUnit.BOWL
        "plate" -> RecipeServingUnit.PLATE
        "portion" -> RecipeServingUnit.PORTION
        "cup" -> RecipeServingUnit.CUP
        "piece" -> RecipeServingUnit.PIECE
        else -> RecipeServingUnit.SERVING
    }
}

private fun RecipeServingUnit.toApiValue(): String {
    return when (this) {
        RecipeServingUnit.SERVING -> "serving"
        RecipeServingUnit.BOWL -> "bowl"
        RecipeServingUnit.PLATE -> "plate"
        RecipeServingUnit.PORTION -> "portion"
        RecipeServingUnit.CUP -> "cup"
        RecipeServingUnit.PIECE -> "piece"
    }
}

