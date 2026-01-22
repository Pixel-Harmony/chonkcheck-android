package com.chonkcheck.android.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class FoodsResponse(
    val foods: List<FoodDto>,
    val recipes: List<FoodRecipeSummaryDto>? = null,
    val meals: List<SavedMealDto>? = null
)

// Simplified recipe summary for food search results
@Serializable
data class FoodRecipeSummaryDto(
    val id: String,
    val name: String
)

@Serializable
data class SavedMealDto(
    val id: String,
    val name: String
)
