package com.chonkcheck.android.data.repository

import com.chonkcheck.android.data.api.DiaryApi
import com.chonkcheck.android.data.db.dao.DiaryDao
import com.chonkcheck.android.data.db.dao.FoodDao
import com.chonkcheck.android.data.db.entity.DayCompletionEntity
import com.chonkcheck.android.data.mappers.toDomain
import com.chonkcheck.android.data.mappers.toEntity
import com.chonkcheck.android.data.mappers.toRequest
import com.chonkcheck.android.domain.model.CreateDiaryEntryParams
import com.chonkcheck.android.domain.model.DiaryDay
import com.chonkcheck.android.domain.model.DiaryEntry
import com.chonkcheck.android.domain.model.MacroTotals
import com.chonkcheck.android.domain.model.MealType
import com.chonkcheck.android.domain.model.UpdateDiaryEntryParams
import com.chonkcheck.android.domain.repository.AuthRepository
import com.chonkcheck.android.domain.repository.DiaryRepository
import io.sentry.Sentry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepositoryImpl @Inject constructor(
    private val diaryApi: DiaryApi,
    private val diaryDao: DiaryDao,
    private val foodDao: FoodDao,
    private val authRepository: AuthRepository
) : DiaryRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getDiaryDay(date: LocalDate): Flow<DiaryDay> {
        return authRepository.currentUser.flatMapLatest { user ->
            val userId = user?.id
            if (userId != null) {
                combine(
                    diaryDao.getEntriesForDate(userId, date.toString()),
                    diaryDao.getDayCompletion(userId, date.toString())
                ) { entries, completion ->
                    val domainEntries = entries.map { it.toDomain() }
                    val entriesByMeal = MealType.entries.associateWith { mealType ->
                        domainEntries.filter { entry -> entry.mealType == mealType }
                    }
                    val totals = calculateTotals(domainEntries)

                    DiaryDay(
                        date = date,
                        entriesByMeal = entriesByMeal,
                        totals = totals,
                        isCompleted = completion != null
                    )
                }
            } else {
                flowOf(
                    DiaryDay(
                        date = date,
                        entriesByMeal = MealType.entries.associateWith { emptyList() },
                        totals = MacroTotals.ZERO,
                        isCompleted = false
                    )
                )
            }
        }
    }

    override fun getDiaryEntryById(id: String): Flow<DiaryEntry?> {
        return diaryDao.getEntryById(id).map { it?.toDomain() }
    }

    override suspend fun createEntry(params: CreateDiaryEntryParams): Result<DiaryEntry> {
        return try {
            val currentUser = authRepository.currentUser.first()
                ?: return Result.failure(IllegalStateException("User not authenticated"))

            // Get food details for nutrition calculation
            val food = if (params.foodId != null) {
                foodDao.getFoodByIdOnce(params.foodId)?.toDomain()
            } else {
                null
            }

            if (food == null && params.foodId != null) {
                return Result.failure(IllegalArgumentException("Food not found"))
            }

            // Generate a temporary ID for local storage
            val tempId = "temp_${UUID.randomUUID()}"

            if (food != null) {
                // Save to local database first (offline-first)
                val localEntity = params.toEntity(tempId, currentUser.id, food)
                diaryDao.insert(localEntity)

                // Try to sync with API
                try {
                    val response = diaryApi.createDiaryEntry(params.toRequest())
                    val syncedEntity = response.toEntity(currentUser.id)

                    // Replace temp entity with synced entity
                    diaryDao.delete(tempId)
                    diaryDao.insert(syncedEntity)

                    Result.success(syncedEntity.toDomain())
                } catch (apiError: Exception) {
                    // API failed, but local save succeeded - return local entry
                    Sentry.captureException(apiError)
                    Result.success(localEntity.toDomain())
                }
            } else {
                // No food found, just try the API call
                try {
                    val response = diaryApi.createDiaryEntry(params.toRequest())
                    val syncedEntity = response.toEntity(currentUser.id)
                    diaryDao.insert(syncedEntity)
                    Result.success(syncedEntity.toDomain())
                } catch (apiError: Exception) {
                    Sentry.captureException(apiError)
                    Result.failure(apiError)
                }
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
            Result.failure(e)
        }
    }

    override suspend fun updateEntry(id: String, params: UpdateDiaryEntryParams): Result<DiaryEntry> {
        return try {
            val existingEntry = diaryDao.getEntryByIdOnce(id)
                ?: return Result.failure(IllegalArgumentException("Entry not found"))

            // Update locally first
            val updatedEntity = existingEntry.copy(
                mealType = params.mealType?.apiValue ?: existingEntry.mealType,
                servingSize = params.servingSize ?: existingEntry.servingSize,
                servingUnit = params.servingUnit?.name?.lowercase() ?: existingEntry.servingUnit,
                numberOfServings = params.numberOfServings ?: existingEntry.numberOfServings,
                updatedAt = System.currentTimeMillis(),
                syncedAt = null // Mark as needing sync
            ).let { entity ->
                // Recalculate nutrition if serving changed
                if (params.servingSize != null || params.numberOfServings != null) {
                    val food = existingEntry.foodId?.let { foodDao.getFoodByIdOnce(it)?.toDomain() }
                    if (food != null) {
                        val newServingSize = params.servingSize ?: existingEntry.servingSize
                        val newServings = params.numberOfServings ?: existingEntry.numberOfServings
                        val multiplier = (newServingSize / food.servingSize) * newServings
                        entity.copy(
                            calories = food.calories * multiplier,
                            protein = food.protein * multiplier,
                            carbs = food.carbs * multiplier,
                            fat = food.fat * multiplier
                        )
                    } else {
                        entity
                    }
                } else {
                    entity
                }
            }

            diaryDao.update(updatedEntity)

            // Try to sync with API
            try {
                val response = diaryApi.updateDiaryEntry(id, params.toRequest())
                val syncedEntity = response.toEntity(existingEntry.userId)
                diaryDao.update(syncedEntity)
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

    override suspend fun deleteEntry(id: String): Result<Unit> {
        return try {
            // Soft delete locally first
            diaryDao.softDelete(id)

            // Try to sync with API
            try {
                diaryApi.deleteDiaryEntry(id)
            } catch (apiError: Exception) {
                Sentry.captureException(apiError)
                // Local delete succeeded, will sync later
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Sentry.captureException(e)
            Result.failure(e)
        }
    }

    override suspend fun completeDay(date: LocalDate): Result<Unit> {
        return try {
            val currentUser = authRepository.currentUser.first()
                ?: return Result.failure(IllegalStateException("User not authenticated"))

            val completion = DayCompletionEntity(
                id = "${currentUser.id}_${date}",
                userId = currentUser.id,
                date = date.toString(),
                completedAt = System.currentTimeMillis()
            )
            diaryDao.insertDayCompletion(completion)

            // Try to sync with API
            try {
                diaryApi.completeDay(date.toString())
            } catch (apiError: Exception) {
                Sentry.captureException(apiError)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Sentry.captureException(e)
            Result.failure(e)
        }
    }

    override suspend fun uncompleteDay(date: LocalDate): Result<Unit> {
        return try {
            val currentUser = authRepository.currentUser.first()
                ?: return Result.failure(IllegalStateException("User not authenticated"))

            diaryDao.deleteDayCompletion(currentUser.id, date.toString())

            // Try to sync with API
            try {
                diaryApi.uncompleteDay(date.toString())
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

            val response = diaryApi.getDiaryEntries(date.toString())
            val entities = response.entries.map { it.toEntity(currentUser.id) }

            // Clear existing entries for date and insert fresh ones
            diaryDao.deleteAllForDate(currentUser.id, date.toString())
            diaryDao.insertAll(entities)

            // Update completion status
            if (response.isCompleted) {
                val completion = DayCompletionEntity(
                    id = "${currentUser.id}_${date}",
                    userId = currentUser.id,
                    date = date.toString(),
                    completedAt = System.currentTimeMillis(),
                    syncedAt = System.currentTimeMillis()
                )
                diaryDao.insertDayCompletion(completion)
            } else {
                diaryDao.deleteDayCompletion(currentUser.id, date.toString())
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }

    private fun calculateTotals(entries: List<DiaryEntry>): MacroTotals {
        return entries.fold(MacroTotals.ZERO) { acc, entry ->
            MacroTotals(
                calories = acc.calories + entry.calories,
                protein = acc.protein + entry.protein,
                carbs = acc.carbs + entry.carbs,
                fat = acc.fat + entry.fat
            )
        }
    }
}
