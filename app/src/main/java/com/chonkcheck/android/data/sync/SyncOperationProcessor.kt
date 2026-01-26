package com.chonkcheck.android.data.sync

import com.chonkcheck.android.data.api.DiaryApi
import com.chonkcheck.android.data.api.ExerciseApi
import com.chonkcheck.android.data.api.FoodApi
import com.chonkcheck.android.data.api.RecipeApi
import com.chonkcheck.android.data.api.SavedMealApi
import com.chonkcheck.android.data.api.WeightApi
import com.chonkcheck.android.data.api.dto.CreateDiaryEntryRequest
import com.chonkcheck.android.data.api.dto.CreateExerciseRequest
import com.chonkcheck.android.data.api.dto.CreateFoodRequest
import com.chonkcheck.android.data.api.dto.CreateRecipeRequest
import com.chonkcheck.android.data.api.dto.CreateSavedMealRequest
import com.chonkcheck.android.data.api.dto.CreateWeightRequest
import com.chonkcheck.android.data.api.dto.UpdateExerciseRequest
import com.chonkcheck.android.data.api.dto.UpdateFoodRequest
import com.chonkcheck.android.data.api.dto.UpdateRecipeRequest
import com.chonkcheck.android.data.db.dao.DiaryDao
import com.chonkcheck.android.data.db.dao.ExerciseDao
import com.chonkcheck.android.data.db.dao.FoodDao
import com.chonkcheck.android.data.db.dao.RecipeDao
import com.chonkcheck.android.data.db.dao.SavedMealDao
import com.chonkcheck.android.data.db.dao.SyncQueueDao
import com.chonkcheck.android.data.db.dao.WeightDao
import com.chonkcheck.android.data.db.entity.SyncQueueEntity
import com.chonkcheck.android.data.mappers.toEntity
import com.chonkcheck.android.domain.model.SyncConflict
import com.chonkcheck.android.domain.model.SyncEntityType
import com.chonkcheck.android.domain.model.SyncOperation
import com.chonkcheck.android.domain.repository.AuthRepository
import io.sentry.Sentry
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Processes individual sync operations from the sync queue.
 * Handles API calls and updates local database with server responses.
 */
@Singleton
class SyncOperationProcessor @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    private val foodApi: FoodApi,
    private val foodDao: FoodDao,
    private val diaryApi: DiaryApi,
    private val diaryDao: DiaryDao,
    private val recipeApi: RecipeApi,
    private val recipeDao: RecipeDao,
    private val savedMealApi: SavedMealApi,
    private val savedMealDao: SavedMealDao,
    private val weightApi: WeightApi,
    private val weightDao: WeightDao,
    private val exerciseApi: ExerciseApi,
    private val exerciseDao: ExerciseDao,
    private val authRepository: AuthRepository,
    private val json: Json
) {
    private val _conflicts = MutableSharedFlow<SyncConflict>(extraBufferCapacity = 10)
    val conflicts: SharedFlow<SyncConflict> = _conflicts.asSharedFlow()

    /**
     * Process a single sync queue item.
     * @return true if successful, false if failed
     */
    suspend fun processItem(item: SyncQueueEntity): Boolean {
        return try {
            val entityType = SyncEntityType.fromString(item.entityType)
            val operation = SyncOperation.fromString(item.operation)

            when (entityType) {
                SyncEntityType.FOOD -> processFood(item, operation)
                SyncEntityType.DIARY_ENTRY -> processDiaryEntry(item, operation)
                SyncEntityType.RECIPE -> processRecipe(item, operation)
                SyncEntityType.SAVED_MEAL -> processSavedMeal(item, operation)
                SyncEntityType.WEIGHT_ENTRY -> processWeightEntry(item, operation)
                SyncEntityType.EXERCISE_ENTRY -> processExerciseEntry(item, operation)
                SyncEntityType.USER -> true // User syncs are handled separately
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
            syncQueueDao.markFailed(item.id, e.message ?: "Unknown error")
            false
        }
    }

    private suspend fun processFood(item: SyncQueueEntity, operation: SyncOperation): Boolean {
        return when (operation) {
            SyncOperation.CREATE -> {
                val payload = item.payload ?: return false
                val request = json.decodeFromString<CreateFoodRequest>(payload)
                val response = foodApi.createFood(request)
                val serverEntity = response.toEntity()

                // Replace temp entity with server entity
                if (item.entityId.startsWith("temp_")) {
                    foodDao.delete(item.entityId)
                }
                foodDao.insert(serverEntity)
                syncQueueDao.updateStatus(item.id, "completed")
                true
            }
            SyncOperation.UPDATE -> {
                val payload = item.payload ?: return false
                val request = json.decodeFromString<UpdateFoodRequest>(payload)
                val response = foodApi.updateFood(item.entityId, request)
                val serverEntity = response.toEntity()
                foodDao.update(serverEntity)
                syncQueueDao.updateStatus(item.id, "completed")
                true
            }
            SyncOperation.DELETE -> {
                foodApi.deleteFood(item.entityId)
                foodDao.delete(item.entityId)
                syncQueueDao.updateStatus(item.id, "completed")
                true
            }
        }
    }

    private suspend fun processDiaryEntry(item: SyncQueueEntity, operation: SyncOperation): Boolean {
        return when (operation) {
            SyncOperation.CREATE -> {
                val payload = item.payload ?: return false
                val request = json.decodeFromString<CreateDiaryEntryRequest>(payload)
                val response = diaryApi.createDiaryEntry(request)
                // We need userId to create entity - for now skip user association
                // The repository will handle this on next refresh
                syncQueueDao.updateStatus(item.id, "completed")
                true
            }
            SyncOperation.UPDATE -> {
                // Diary API doesn't support updates, so just mark complete
                syncQueueDao.updateStatus(item.id, "completed")
                true
            }
            SyncOperation.DELETE -> {
                diaryApi.deleteDiaryEntry(item.entityId)
                diaryDao.delete(item.entityId)
                syncQueueDao.updateStatus(item.id, "completed")
                true
            }
        }
    }

    private suspend fun processRecipe(item: SyncQueueEntity, operation: SyncOperation): Boolean {
        return when (operation) {
            SyncOperation.CREATE -> {
                val payload = item.payload ?: return false
                val request = json.decodeFromString<CreateRecipeRequest>(payload)
                val response = recipeApi.createRecipe(request)
                val serverEntity = response.toEntity()

                // Replace temp entity with server entity
                if (item.entityId.startsWith("temp_")) {
                    recipeDao.delete(item.entityId)
                }
                recipeDao.insert(serverEntity)
                syncQueueDao.updateStatus(item.id, "completed")
                true
            }
            SyncOperation.UPDATE -> {
                val payload = item.payload ?: return false
                val request = json.decodeFromString<UpdateRecipeRequest>(payload)
                val response = recipeApi.updateRecipe(item.entityId, request)
                val serverEntity = response.toEntity()
                recipeDao.update(serverEntity)
                syncQueueDao.updateStatus(item.id, "completed")
                true
            }
            SyncOperation.DELETE -> {
                recipeApi.deleteRecipe(item.entityId)
                recipeDao.delete(item.entityId)
                syncQueueDao.updateStatus(item.id, "completed")
                true
            }
        }
    }

    private suspend fun processSavedMeal(item: SyncQueueEntity, operation: SyncOperation): Boolean {
        return when (operation) {
            SyncOperation.CREATE -> {
                val payload = item.payload ?: return false
                val request = json.decodeFromString<CreateSavedMealRequest>(payload)
                val response = savedMealApi.createSavedMeal(request)
                val serverEntity = response.toEntity()

                // Replace temp entity with server entity
                if (item.entityId.startsWith("temp_")) {
                    savedMealDao.delete(item.entityId)
                }
                savedMealDao.insert(serverEntity)
                syncQueueDao.updateStatus(item.id, "completed")
                true
            }
            SyncOperation.UPDATE -> {
                val payload = item.payload ?: return false
                val request = json.decodeFromString<CreateSavedMealRequest>(payload)
                val response = savedMealApi.updateSavedMeal(item.entityId, request)
                val serverEntity = response.toEntity()
                savedMealDao.update(serverEntity)
                syncQueueDao.updateStatus(item.id, "completed")
                true
            }
            SyncOperation.DELETE -> {
                savedMealApi.deleteSavedMeal(item.entityId)
                savedMealDao.delete(item.entityId)
                syncQueueDao.updateStatus(item.id, "completed")
                true
            }
        }
    }

    private suspend fun processWeightEntry(item: SyncQueueEntity, operation: SyncOperation): Boolean {
        val currentUser = authRepository.currentUser.first()
            ?: return false

        return when (operation) {
            SyncOperation.CREATE -> {
                val payload = item.payload ?: return false
                val request = json.decodeFromString<CreateWeightRequest>(payload)
                val response = weightApi.createWeightEntry(request)
                val serverEntity = response.toEntity(currentUser.id)

                // Replace temp entity with server entity
                if (item.entityId.startsWith("temp_")) {
                    weightDao.delete(item.entityId)
                }
                weightDao.insert(serverEntity)
                syncQueueDao.updateStatus(item.id, "completed")
                true
            }
            SyncOperation.UPDATE -> {
                // Weight API doesn't support updates - delete and recreate
                val payload = item.payload ?: return false
                val request = json.decodeFromString<CreateWeightRequest>(payload)
                try {
                    weightApi.deleteWeightEntry(request.date)
                } catch (_: Exception) {
                    // May not exist, ignore
                }
                val response = weightApi.createWeightEntry(request)
                val serverEntity = response.toEntity(currentUser.id)
                weightDao.update(serverEntity)
                syncQueueDao.updateStatus(item.id, "completed")
                true
            }
            SyncOperation.DELETE -> {
                // entityId for weight is the date
                weightApi.deleteWeightEntry(item.entityId)
                // Delete locally by soft deleting using the temp ID in entityId
                // The actual entity would have been soft-deleted already
                syncQueueDao.updateStatus(item.id, "completed")
                true
            }
        }
    }

    private suspend fun processExerciseEntry(item: SyncQueueEntity, operation: SyncOperation): Boolean {
        val currentUser = authRepository.currentUser.first()
            ?: return false

        return when (operation) {
            SyncOperation.CREATE -> {
                val payload = item.payload ?: return false
                val request = json.decodeFromString<CreateExerciseRequest>(payload)
                val response = exerciseApi.createExercise(request)
                val serverEntity = response.toEntity(currentUser.id)

                // Replace temp entity with server entity
                if (item.entityId.startsWith("temp_")) {
                    exerciseDao.delete(item.entityId)
                }
                exerciseDao.insert(serverEntity)
                syncQueueDao.updateStatus(item.id, "completed")
                true
            }
            SyncOperation.UPDATE -> {
                val payload = item.payload ?: return false
                val request = json.decodeFromString<UpdateExerciseRequest>(payload)
                val response = exerciseApi.updateExercise(item.entityId, request)
                val serverEntity = response.toEntity(currentUser.id)
                exerciseDao.update(serverEntity)
                syncQueueDao.updateStatus(item.id, "completed")
                true
            }
            SyncOperation.DELETE -> {
                exerciseApi.deleteExercise(item.entityId)
                exerciseDao.delete(item.entityId)
                syncQueueDao.updateStatus(item.id, "completed")
                true
            }
        }
    }

    /**
     * Emit a conflict notification to observers.
     */
    suspend fun notifyConflict(conflict: SyncConflict) {
        _conflicts.emit(conflict)
    }
}
