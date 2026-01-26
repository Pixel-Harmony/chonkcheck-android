package com.chonkcheck.android.data.mappers

import com.chonkcheck.android.data.api.dto.CreateWeightRequest
import com.chonkcheck.android.data.api.dto.WeightEntryDto
import com.chonkcheck.android.data.db.entity.WeightEntryEntity
import com.chonkcheck.android.domain.model.CreateWeightParams
import com.chonkcheck.android.domain.model.WeightEntry

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

