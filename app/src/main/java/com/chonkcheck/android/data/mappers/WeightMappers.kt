package com.chonkcheck.android.data.mappers

import com.chonkcheck.android.data.api.dto.CreateWeightRequest
import com.chonkcheck.android.data.api.dto.WeightEntryDto
import com.chonkcheck.android.data.db.entity.WeightEntryEntity
import com.chonkcheck.android.domain.model.CreateWeightParams
import com.chonkcheck.android.domain.model.WeightEntry
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun WeightEntryDto.toEntity(userId: String): WeightEntryEntity = WeightEntryEntity(
    id = "weight_${date}_${userId}",
    userId = userId,
    date = date,
    weight = weight,
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
    notes = notes
)

private fun String.toLocalDate(): LocalDate {
    return try {
        LocalDate.parse(this)
    } catch (e: DateTimeParseException) {
        LocalDate.now()
    }
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
