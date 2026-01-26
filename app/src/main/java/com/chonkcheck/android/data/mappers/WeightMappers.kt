package com.chonkcheck.android.data.mappers

import com.chonkcheck.android.data.api.dto.CreateWeightRequest
import com.chonkcheck.android.data.api.dto.WeightEntryDto
import com.chonkcheck.android.data.db.entity.WeightEntryEntity
import com.chonkcheck.android.domain.model.CreateWeightParams
import com.chonkcheck.android.domain.model.WeightEntry

/**
 * Convert API weight value to kg based on the unit field.
 * The API returns weight in the user's preferred unit.
 *
 * IMPORTANT: When unit is "st" (stone), the API returns the value in TOTAL POUNDS,
 * not in stones. This matches how the web app handles it.
 */
private fun convertToKg(weight: Double, unit: String): Double {
    return when (unit.lowercase()) {
        "kg" -> weight
        "lb" -> weight * 0.453592 // lb to kg
        "st" -> weight * 0.453592 // st unit means value is in total pounds, convert to kg
        else -> weight // Assume kg if unknown
    }
}

fun WeightEntryDto.toEntity(userId: String): WeightEntryEntity = WeightEntryEntity(
    id = "weight_${date}_${userId}",
    userId = userId,
    date = date,
    weight = convertToKg(weight, unit), // Convert from API unit to kg
    notes = notes,
    syncedAt = System.currentTimeMillis(),
    createdAt = createdAt?.parseTimestamp() ?: System.currentTimeMillis(),
    updatedAt = System.currentTimeMillis()
)

fun WeightEntryEntity.toDomain(): WeightEntry = WeightEntry(
    id = id,
    userId = userId,
    date = date.toLocalDate(),
    weight = weight,
    notes = notes,
    createdAt = createdAt
)

fun CreateWeightParams.toEntity(id: String, userId: String): WeightEntryEntity = WeightEntryEntity(
    id = id,
    userId = userId,
    date = date.toString(),
    weight = weight,
    notes = notes,
    createdAt = System.currentTimeMillis(),
    updatedAt = System.currentTimeMillis()
)

fun CreateWeightParams.toRequest(): CreateWeightRequest = CreateWeightRequest(
    date = date.toString(),
    weight = weight,
    unit = "kg", // Always send in kg - the app stores weight internally in kg
    notes = notes
)

