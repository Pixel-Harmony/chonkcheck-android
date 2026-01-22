package com.chonkcheck.android.data.mappers

import com.chonkcheck.android.data.api.dto.CreateDiaryEntryRequest
import com.chonkcheck.android.data.api.dto.DiaryEntryDto
import com.chonkcheck.android.data.api.dto.UpdateDiaryEntryRequest
import com.chonkcheck.android.data.db.entity.DiaryEntryEntity
import com.chonkcheck.android.domain.model.CreateDiaryEntryParams
import com.chonkcheck.android.domain.model.DiaryEntry
import com.chonkcheck.android.domain.model.MealType
import com.chonkcheck.android.domain.model.ServingUnit
import com.chonkcheck.android.domain.model.UpdateDiaryEntryParams
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun DiaryEntryDto.toEntity(): DiaryEntryEntity = DiaryEntryEntity(
    id = id,
    userId = userId,
    date = date,
    mealType = mealType,
    foodId = foodId,
    recipeId = recipeId,
    servingSize = quantity,
    servingUnit = "g", // Default to grams, API uses quantity as total amount
    numberOfServings = 1.0,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    name = name,
    brand = brand,
    syncedAt = System.currentTimeMillis(),
    createdAt = createdAt.parseTimestamp() ?: System.currentTimeMillis(),
    updatedAt = updatedAt?.parseTimestamp() ?: System.currentTimeMillis()
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
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    name = name,
    brand = brand,
    createdAt = createdAt
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
    date = date.toApiDate(),
    meal = mealType.apiValue,
    foodId = foodId,
    recipeId = recipeId,
    quantity = servingSize * numberOfServings
)

fun UpdateDiaryEntryParams.toRequest(): UpdateDiaryEntryRequest = UpdateDiaryEntryRequest(
    meal = mealType?.apiValue,
    quantity = if (servingSize != null && numberOfServings != null) {
        servingSize * numberOfServings
    } else {
        servingSize ?: numberOfServings
    }
)

private fun String.toServingUnit(): ServingUnit = when (this.lowercase()) {
    "g", "gram" -> ServingUnit.GRAM
    "ml", "milliliter" -> ServingUnit.MILLILITER
    "oz", "ounce" -> ServingUnit.OUNCE
    "cup" -> ServingUnit.CUP
    "tbsp", "tablespoon" -> ServingUnit.TABLESPOON
    "tsp", "teaspoon" -> ServingUnit.TEASPOON
    "piece" -> ServingUnit.PIECE
    "slice" -> ServingUnit.SLICE
    else -> ServingUnit.GRAM
}

private fun ServingUnit.toApiValue(): String = when (this) {
    ServingUnit.GRAM -> "g"
    ServingUnit.MILLILITER -> "ml"
    ServingUnit.OUNCE -> "oz"
    ServingUnit.CUP -> "cup"
    ServingUnit.TABLESPOON -> "tbsp"
    ServingUnit.TEASPOON -> "tsp"
    ServingUnit.PIECE -> "piece"
    ServingUnit.SLICE -> "slice"
}

private fun String.toLocalDate(): LocalDate {
    return try {
        LocalDate.parse(this)
    } catch (e: DateTimeParseException) {
        LocalDate.now()
    }
}

private fun LocalDate.toApiDate(): String = this.toString()

private fun String.parseTimestamp(): Long? {
    return try {
        Instant.parse(this).toEpochMilli()
    } catch (e: Exception) {
        try {
            DateTimeFormatter.ISO_DATE_TIME.parse(this) { temporal ->
                Instant.from(temporal).toEpochMilli()
            }
        } catch (e: Exception) {
            null
        }
    }
}
