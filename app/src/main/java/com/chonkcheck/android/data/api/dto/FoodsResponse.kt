package com.chonkcheck.android.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class FoodsResponse(
    val foods: List<FoodDto>,
    val recipes: List<RecipeDto>? = null,
    val meals: List<SavedMealDto>? = null
)

@Serializable
data class RecipeDto(
    val id: String,
    val name: String
)

@Serializable
data class SavedMealDto(
    val id: String,
    val name: String
)
