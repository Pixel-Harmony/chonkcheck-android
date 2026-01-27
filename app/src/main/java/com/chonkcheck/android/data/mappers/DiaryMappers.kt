package com.chonkcheck.android.data.mappers

import com.chonkcheck.android.data.api.dto.CreateDiaryEntryRequest
import com.chonkcheck.android.data.api.dto.DiaryEntryDto
import com.chonkcheck.android.data.db.entity.DiaryEntryEntity
import com.chonkcheck.android.domain.model.CreateDiaryEntryParams
import com.chonkcheck.android.domain.model.DiaryEntry
import com.chonkcheck.android.domain.model.DiaryItemType
import com.chonkcheck.android.domain.model.MealType
import com.chonkcheck.android.domain.model.ServingUnit

fun DiaryEntryDto.toEntity(userId: String): DiaryEntryEntity = DiaryEntryEntity(
    id = id,
    userId = userId,
    date = date,
    mealType = mealType,
    foodId = foodId,
    recipeId = recipeId,
    servingSize = food?.servingSize ?: savedRecipe?.totalServings ?: 1.0, // Base serving size
    servingUnit = food?.servingUnit ?: savedRecipe?.servingUnit ?: "serving",
    numberOfServings = quantity, // How many servings logged
    enteredAmount = enteredAmount, // Optional: actual amount user entered
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    name = name,
    brand = brand,
    itemType = itemType,
    mealGroupId = mealGroupId,
    mealGroupName = mealGroupName,
    syncedAt = System.currentTimeMillis(),
    createdAt = timestamp.parseTimestamp() ?: System.currentTimeMillis(),
    updatedAt = System.currentTimeMillis()
)

fun DiaryEntryEntity.toDomain(): DiaryEntry = DiaryEntry(
    id = id,
    userId = userId,
    date = date.toLocalDate(),
    mealType = MealType.fromApiValue(mealType),
    foodId = foodId,
    recipeId = recipeId,
    servingSize = servingSize,
    servingUnit = servingUnit.toServingUnit(),
    numberOfServings = numberOfServings,
    enteredAmount = enteredAmount,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    name = name,
    brand = brand,
    createdAt = createdAt,
    itemType = DiaryItemType.fromApiValue(itemType),
    mealGroupId = mealGroupId,
    mealGroupName = mealGroupName
)

fun CreateDiaryEntryParams.toEntity(
    id: String,
    userId: String,
    food: com.chonkcheck.android.domain.model.Food
): DiaryEntryEntity {
    val multiplier = (servingSize / food.servingSize) * numberOfServings
    return DiaryEntryEntity(
        id = id,
        userId = userId,
        date = date.toApiDate(),
        mealType = mealType.apiValue,
        foodId = foodId,
        recipeId = recipeId,
        servingSize = servingSize,
        servingUnit = servingUnit.toApiValue(),
        numberOfServings = numberOfServings,
        calories = food.calories * multiplier,
        protein = food.protein * multiplier,
        carbs = food.carbs * multiplier,
        fat = food.fat * multiplier,
        name = food.name,
        brand = food.brand
    )
}

fun CreateDiaryEntryParams.toRequest(): CreateDiaryEntryRequest = CreateDiaryEntryRequest(
    foodId = foodId,
    recipeId = recipeId,
    itemType = if (foodId != null) "food" else if (recipeId != null) "recipe" else null,
    quantity = (servingSize / foodServingSize) * numberOfServings, // Number of servings, not raw grams
    date = date.toApiDate(),
    meal = mealType.apiValue,
    enteredAmount = servingSize
)

