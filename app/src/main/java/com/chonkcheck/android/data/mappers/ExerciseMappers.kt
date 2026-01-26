package com.chonkcheck.android.data.mappers

import com.chonkcheck.android.data.api.dto.CreateExerciseRequest
import com.chonkcheck.android.data.api.dto.ExerciseDto
import com.chonkcheck.android.data.api.dto.UpdateExerciseRequest
import com.chonkcheck.android.data.db.entity.ExerciseEntryEntity
import com.chonkcheck.android.domain.model.CreateExerciseParams
import com.chonkcheck.android.domain.model.Exercise
import com.chonkcheck.android.domain.model.UpdateExerciseParams
import java.time.Instant
import java.time.LocalDate

fun ExerciseDto.toEntity(userId: String): ExerciseEntryEntity {
    return ExerciseEntryEntity(
        id = id,
        userId = userId,
        date = date,
        name = name,
        description = description,
        caloriesBurned = caloriesBurned,
        syncedAt = System.currentTimeMillis(),
        createdAt = Instant.parse(createdAt).toEpochMilli(),
        updatedAt = Instant.parse(updatedAt).toEpochMilli()
    )
}

fun ExerciseEntryEntity.toDomain(): Exercise {
    return Exercise(
        id = id,
        userId = userId,
        date = LocalDate.parse(date),
        name = name,
        caloriesBurned = caloriesBurned,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun CreateExerciseParams.toRequest(): CreateExerciseRequest {
    return CreateExerciseRequest(
        name = name,
        caloriesBurned = caloriesBurned,
        description = description,
        date = date.toString()
    )
}

fun UpdateExerciseParams.toRequest(): UpdateExerciseRequest {
    return UpdateExerciseRequest(
        name = name,
        caloriesBurned = caloriesBurned,
        description = description,
        date = date.toString()
    )
}

fun CreateExerciseParams.toEntity(tempId: String, userId: String): ExerciseEntryEntity {
    val now = System.currentTimeMillis()
    return ExerciseEntryEntity(
        id = tempId,
        userId = userId,
        date = date.toString(),
        name = name,
        description = description,
        caloriesBurned = caloriesBurned,
        syncedAt = null,
        createdAt = now,
        updatedAt = now
    )
}
