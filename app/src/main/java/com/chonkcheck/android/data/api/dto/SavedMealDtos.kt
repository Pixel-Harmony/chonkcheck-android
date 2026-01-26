package com.chonkcheck.android.data.api.dto

import kotlinx.serialization.Serializable

// Note: SavedMealDto and SavedMealItemDto are defined in FoodsResponse.kt

@Serializable
data class SavedMealsResponse(
    val meals: List<SavedMealDto>
)

@Serializable
data class CreateSavedMealRequest(
    val name: String,
    val items: List<CreateSavedMealItemRequest>
)

@Serializable
data class CreateSavedMealItemRequest(
    val itemId: String,
    val itemType: String, // "food" or "recipe"
    val quantity: Double,
    val enteredAmount: Double? = null
)

@Serializable
data class AddMealToDiaryRequest(
    val savedMealId: String,
    val date: String,
    val meal: String,
    val items: List<AddMealToDiaryItemRequest>
)

@Serializable
data class AddMealToDiaryItemRequest(
    val itemId: String,
    val itemType: String,
    val quantity: Double,
    val enteredAmount: Double? = null
)

@Serializable
data class AddMealToDiaryResponse(
    val mealGroupId: String,
    val mealGroupName: String,
    val entries: List<DiaryEntryDto>
)
