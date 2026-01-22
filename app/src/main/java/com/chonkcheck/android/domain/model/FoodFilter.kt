package com.chonkcheck.android.domain.model

data class FoodFilter(
    val query: String = "",
    val type: FoodFilterType = FoodFilterType.ALL,
    val includeRecipes: Boolean = false,
    val includeMeals: Boolean = false,
    val limit: Int = 50
)

enum class FoodFilterType {
    ALL,
    PLATFORM,
    USER
}
