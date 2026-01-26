package com.chonkcheck.android.domain.repository

import com.chonkcheck.android.domain.model.CreateExerciseParams
import com.chonkcheck.android.domain.model.Exercise
import com.chonkcheck.android.domain.model.UpdateExerciseParams
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ExerciseRepository {

    fun getExercisesForDate(date: LocalDate): Flow<List<Exercise>>

    fun getExerciseById(id: String): Flow<Exercise?>

    suspend fun createExercise(params: CreateExerciseParams): Result<Exercise>

    suspend fun updateExercise(id: String, params: UpdateExerciseParams): Result<Exercise>

    suspend fun deleteExercise(id: String): Result<Unit>

    suspend fun refresh(date: LocalDate)
}
