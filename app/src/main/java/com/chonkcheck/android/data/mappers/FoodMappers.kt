package com.chonkcheck.android.data.mappers

import com.chonkcheck.android.data.api.dto.CreateFoodRequest
import com.chonkcheck.android.data.api.dto.FoodDto
import com.chonkcheck.android.data.api.dto.UpdateFoodRequest
import com.chonkcheck.android.data.db.entity.FoodEntity
import com.chonkcheck.android.domain.model.CreateFoodParams
import com.chonkcheck.android.domain.model.Food
import com.chonkcheck.android.domain.model.FoodSource
import com.chonkcheck.android.domain.model.FoodType
import com.chonkcheck.android.domain.model.ServingUnit
import com.chonkcheck.android.domain.model.UpdateFoodParams
import java.time.Instant
import java.time.format.DateTimeFormatter

fun FoodDto.toEntity(): FoodEntity = FoodEntity(
    id = id,
    name = name,
    brand = brand,
    barcode = barcode,
    servingSize = servingSize,
    servingUnit = servingUnit,
    servingsPerContainer = servingsPerContainer,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    saturatedFat = saturatedFat,
    transFat = transFat,
    cholesterol = cholesterol,
    sodium = sodium,
    fiber = fiber,
    sugar = sugar,
    addedSugar = addedSugar,
    vitaminA = vitaminA,
    vitaminC = vitaminC,
    vitaminD = vitaminD,
    calcium = calcium,
    iron = iron,
    potassium = potassium,
    type = type,
    userId = userId,
    source = source,
    verified = verified,
    promotionRequested = promotionRequested,
    promotionRequestedAt = promotionRequestedAt?.parseTimestamp(),
    overrideOf = overrideOf,
    imageUrl = imageUrl,
    syncedAt = System.currentTimeMillis(),
    createdAt = createdAt.parseTimestamp() ?: System.currentTimeMillis(),
    updatedAt = updatedAt?.parseTimestamp() ?: System.currentTimeMillis(),
    archivedAt = archivedAt?.parseTimestamp()
)

fun FoodEntity.toDomain(): Food = Food(
    id = id,
    name = name,
    brand = brand,
    barcode = barcode,
    servingSize = servingSize,
    servingUnit = servingUnit.toServingUnit(),
    servingsPerContainer = servingsPerContainer,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    fiber = fiber,
    sugar = sugar,
    sodium = sodium,
    saturatedFat = saturatedFat,
    transFat = transFat,
    cholesterol = cholesterol,
    addedSugar = addedSugar,
    vitaminA = vitaminA,
    vitaminC = vitaminC,
    vitaminD = vitaminD,
    calcium = calcium,
    iron = iron,
    potassium = potassium,
    type = type.toFoodType(),
    source = source?.toFoodSource(),
    verified = verified,
    promotionRequested = promotionRequested,
    overrideOf = overrideOf,
    imageUrl = imageUrl,
    createdAt = createdAt
)

fun CreateFoodParams.toEntity(
    id: String,
    userId: String
): FoodEntity = FoodEntity(
    id = id,
    name = name,
    brand = brand,
    barcode = barcode,
    servingSize = servingSize,
    servingUnit = servingUnit.toApiValue(),
    servingsPerContainer = servingsPerContainer,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    saturatedFat = saturatedFat,
    transFat = transFat,
    cholesterol = cholesterol,
    sodium = sodium,
    fiber = fiber,
    sugar = sugar,
    addedSugar = addedSugar,
    vitaminA = null,
    vitaminC = null,
    vitaminD = null,
    calcium = null,
    iron = null,
    potassium = null,
    type = "user",
    userId = userId,
    source = "user_submitted",
    verified = false,
    promotionRequested = false,
    promotionRequestedAt = null,
    overrideOf = null,
    imageUrl = imageUrl,
    syncedAt = null,
    createdAt = System.currentTimeMillis(),
    updatedAt = System.currentTimeMillis(),
    archivedAt = null
)

fun CreateFoodParams.toRequest(): CreateFoodRequest = CreateFoodRequest(
    name = name,
    brand = brand,
    barcode = barcode,
    servingSize = servingSize,
    servingUnit = servingUnit.toApiValue(),
    servingsPerContainer = servingsPerContainer,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    fiber = fiber,
    sugar = sugar,
    sodium = sodium,
    saturatedFat = saturatedFat,
    transFat = transFat,
    cholesterol = cholesterol,
    addedSugar = addedSugar,
    imageUrl = imageUrl
)

fun UpdateFoodParams.toRequest(): UpdateFoodRequest = UpdateFoodRequest(
    name = name,
    brand = brand,
    barcode = barcode,
    servingSize = servingSize,
    servingUnit = servingUnit?.toApiValue(),
    servingsPerContainer = servingsPerContainer,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    fiber = fiber,
    sugar = sugar,
    sodium = sodium,
    saturatedFat = saturatedFat,
    transFat = transFat,
    cholesterol = cholesterol,
    addedSugar = addedSugar,
    imageUrl = imageUrl
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

private fun String.toFoodType(): FoodType = when (this.lowercase()) {
    "platform" -> FoodType.PLATFORM
    "user" -> FoodType.USER
    else -> FoodType.USER
}

private fun String.toFoodSource(): FoodSource? = when (this.lowercase()) {
    "user_submitted" -> FoodSource.USER_SUBMITTED
    "open_food_facts" -> FoodSource.OPEN_FOOD_FACTS
    else -> null
}

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
