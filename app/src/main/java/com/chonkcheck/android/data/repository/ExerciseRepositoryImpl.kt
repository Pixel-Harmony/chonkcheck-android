package com.chonkcheck.android.data.repository

import com.chonkcheck.android.data.api.ExerciseApi
import com.chonkcheck.android.data.db.dao.ExerciseDao
import com.chonkcheck.android.data.mappers.toDomain
import com.chonkcheck.android.data.mappers.toEntity
import com.chonkcheck.android.data.mappers.toRequest
import com.chonkcheck.android.domain.model.CreateExerciseParams
import com.chonkcheck.android.domain.model.Exercise
import com.chonkcheck.android.domain.model.UpdateExerciseParams
import com.chonkcheck.android.domain.repository.AuthRepository
import com.chonkcheck.android.domain.repository.ExerciseRepository
import io.sentry.Sentry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseApi: ExerciseApi,
    private val exerciseDao: ExerciseDao,
    private val authRepository: AuthRepository
) : ExerciseRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getExercisesForDate(date: LocalDate): Flow<List<Exercise>> {
        return authRepository.currentUser.flatMapLatest { user ->
            val userId = user?.id
            if (userId != null) {
                exerciseDao.getEntriesForDate(userId, date.toString())
                    .map { entities -> entities.map { it.toDomain() } }
            } else {
                flowOf(emptyList())
            }
        }
    }

    override fun getExerciseById(id: String): Flow<Exercise?> {
        return exerciseDao.getEntryById(id).map { it?.toDomain() }
    }

    override suspend fun createExercise(params: CreateExerciseParams): Result<Exercise> {
        return try {
            val currentUser = authRepository.currentUser.first()
                ?: return Result.failure(IllegalStateException("User not authenticated"))

            val tempId = "temp_${UUID.randomUUID()}"
            val localEntity = params.toEntity(tempId, currentUser.id)
            exerciseDao.insert(localEntity)

            try {
                val response = exerciseApi.createExercise(params.toRequest())
                val syncedEntity = response.toEntity(currentUser.id)

                exerciseDao.delete(tempId)
                exerciseDao.insert(syncedEntity)

                Result.success(syncedEntity.toDomain())
            } catch (apiError: Exception) {
                Sentry.captureException(apiError)
                Result.success(localEntity.toDomain())
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
            Result.failure(e)
        }
    }

    override suspend fun updateExercise(id: String, params: UpdateExerciseParams): Result<Exercise> {
        return try {
            val existingEntry = exerciseDao.getEntryByIdOnce(id)
                ?: return Result.failure(IllegalArgumentException("Exercise not found"))

            val updatedEntity = existingEntry.copy(
                name = params.name,
                caloriesBurned = params.caloriesBurned,
                description = params.description,
                date = params.date.toString(),
                updatedAt = System.currentTimeMillis(),
                syncedAt = null
            )
            exerciseDao.update(updatedEntity)

            try {
                val response = exerciseApi.updateExercise(id, params.toRequest())
                val syncedEntity = response.toEntity(existingEntry.userId)
                exerciseDao.update(syncedEntity)
                Result.success(syncedEntity.toDomain())
            } catch (apiError: Exception) {
                Sentry.captureException(apiError)
                Result.success(updatedEntity.toDomain())
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
            Result.failure(e)
        }
    }

    override suspend fun deleteExercise(id: String): Result<Unit> {
        return try {
            exerciseDao.softDelete(id)

            try {
                exerciseApi.deleteExercise(id)
            } catch (apiError: Exception) {
                Sentry.captureException(apiError)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Sentry.captureException(e)
            Result.failure(e)
        }
    }

    override suspend fun refresh(date: LocalDate) {
        try {
            val currentUser = authRepository.currentUser.first() ?: return

            val response = exerciseApi.getExercisesForDate(date.toString())
            val entities = response.map { it.toEntity(currentUser.id) }

            // Note: We don't have a deleteAllForDate method in ExerciseDao,
            // so we'll just upsert the entities
            exerciseDao.insertAll(entities)
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }
}
