package com.chonkcheck.android.data.mappers

import com.chonkcheck.android.data.api.dto.AddMealToDiaryItemRequest
import com.chonkcheck.android.data.api.dto.AddMealToDiaryRequest
import com.chonkcheck.android.data.api.dto.CreateSavedMealItemRequest
import com.chonkcheck.android.data.api.dto.CreateSavedMealRequest
import com.chonkcheck.android.data.api.dto.SavedMealDto
import com.chonkcheck.android.data.api.dto.SavedMealItemDto
import com.chonkcheck.android.data.db.entity.SavedMealEntity
import com.chonkcheck.android.data.db.entity.SavedMealItemJson
import com.chonkcheck.android.domain.model.AddMealToDiaryParams
import com.chonkcheck.android.domain.model.CreateSavedMealParams
import com.chonkcheck.android.domain.model.SavedMeal
import com.chonkcheck.android.domain.model.SavedMealItem
import com.chonkcheck.android.domain.model.SavedMealItemParams
import com.chonkcheck.android.domain.model.SavedMealItemType
import com.chonkcheck.android.domain.model.ServingUnit
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

private val json = Json { ignoreUnknownKeys = true }

// DTO -> Entity
fun SavedMealDto.toEntity(): SavedMealEntity {
    val itemsJson = json.encodeToString(
        items.map { it.toItemJson() }
    )

    return SavedMealEntity(
        id = id,
        userId = userId,
        name = name,
        description = null,
        itemsJson = itemsJson,
        totalCalories = totalCalories,
        totalProtein = totalProtein,
        totalCarbs = totalCarbs,
        totalFat = totalFat,
        usageCount = 0,
        lastUsedAt = null,
        syncedAt = System.currentTimeMillis(),
        createdAt = createdAt.parseTimestamp() ?: System.currentTimeMillis(),
        updatedAt = updatedAt.parseTimestamp() ?: System.currentTimeMillis(),
        deletedAt = if (archivedAt != null) archivedAt.parseTimestamp() else null
    )
}

fun SavedMealItemDto.toItemJson(): SavedMealItemJson {
    return SavedMealItemJson(
        type = itemType,
        foodId = if (itemType == "food") itemId else null,
        recipeId = if (itemType == "recipe") itemId else null,
        name = itemName,
        brand = null,
        servingSize = 1.0,
        servingUnit = "serving",
        numberOfServings = quantity,
        enteredAmount = enteredAmount,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat
    )
}

// Entity -> Domain
fun SavedMealEntity.toDomain(): SavedMeal {
    val itemsList = try {
        json.decodeFromString<List<SavedMealItemJson>>(itemsJson)
    } catch (e: Exception) {
        emptyList()
    }

    return SavedMeal(
        id = id,
        userId = userId,
        name = name,
        items = itemsList.map { it.toDomain() },
        totalCalories = totalCalories,
        totalProtein = totalProtein,
        totalCarbs = totalCarbs,
        totalFat = totalFat,
        usageCount = usageCount,
        lastUsedAt = lastUsedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun SavedMealItemJson.toDomain(): SavedMealItem {
    return SavedMealItem(
        itemId = foodId ?: recipeId ?: "",
        itemType = SavedMealItemType.fromApiValue(type),
        itemName = name,
        brand = brand,
        servingSize = servingSize,
        servingUnit = servingUnit.toServingUnitSafe(),
        quantity = numberOfServings,
        enteredAmount = enteredAmount,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat
    )
}

// Domain -> Request
fun CreateSavedMealParams.toRequest(): CreateSavedMealRequest {
    return CreateSavedMealRequest(
        name = name,
        items = items.map { it.toRequest() }
    )
}

fun SavedMealItemParams.toRequest(): CreateSavedMealItemRequest {
    return CreateSavedMealItemRequest(
        itemId = itemId,
        itemType = itemType.toApiValue(),
        quantity = quantity,
        enteredAmount = enteredAmount
    )
}

fun AddMealToDiaryParams.toRequest(): AddMealToDiaryRequest {
    return AddMealToDiaryRequest(
        savedMealId = savedMealId,
        date = date,
        meal = mealType.apiValue,
        items = items.map { item ->
            AddMealToDiaryItemRequest(
                itemId = item.itemId,
                itemType = item.itemType.toApiValue(),
                quantity = item.quantity,
                enteredAmount = item.enteredAmount
            )
        }
    )
}

// Domain -> Entity (for local creation)
fun CreateSavedMealParams.toEntity(
    id: String,
    userId: String,
    itemDetails: List<SavedMealItemJson>
): SavedMealEntity {
    val totalCalories = itemDetails.sumOf { it.calories }
    val totalProtein = itemDetails.sumOf { it.protein }
    val totalCarbs = itemDetails.sumOf { it.carbs }
    val totalFat = itemDetails.sumOf { it.fat }

    return SavedMealEntity(
        id = id,
        userId = userId,
        name = name,
        description = null,
        itemsJson = json.encodeToString(itemDetails),
        totalCalories = totalCalories,
        totalProtein = totalProtein,
        totalCarbs = totalCarbs,
        totalFat = totalFat,
        usageCount = 0,
        lastUsedAt = null,
        syncedAt = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        deletedAt = null
    )
}

private fun String.toServingUnitSafe(): ServingUnit {
    return when (this.lowercase()) {
        "g", "gram" -> ServingUnit.GRAM
        "ml", "milliliter" -> ServingUnit.MILLILITER
        "oz", "ounce" -> ServingUnit.OUNCE
        "cup" -> ServingUnit.CUP
        "tbsp", "tablespoon" -> ServingUnit.TABLESPOON
        "tsp", "teaspoon" -> ServingUnit.TEASPOON
        "piece" -> ServingUnit.PIECE
        "slice" -> ServingUnit.SLICE
        "serving" -> ServingUnit.GRAM
        else -> ServingUnit.GRAM
    }
}

private fun String.parseTimestamp(): Long? {
    return try {
        Instant.parse(this).toEpochMilli()
    } catch (e: Exception) {
        null
    }
}
