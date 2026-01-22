package com.chonkcheck.android.data.repository

import com.chonkcheck.android.data.api.FoodApi
import com.chonkcheck.android.data.db.dao.FoodDao
import com.chonkcheck.android.data.mappers.toDomain
import com.chonkcheck.android.data.mappers.toEntity
import com.chonkcheck.android.data.mappers.toRequest
import com.chonkcheck.android.domain.model.CreateFoodParams
import com.chonkcheck.android.domain.model.Food
import com.chonkcheck.android.domain.model.FoodFilter
import com.chonkcheck.android.domain.model.FoodFilterType
import com.chonkcheck.android.domain.model.UpdateFoodParams
import com.chonkcheck.android.domain.repository.AuthRepository
import com.chonkcheck.android.domain.repository.FoodRepository
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepositoryImpl @Inject constructor(
    private val foodApi: FoodApi,
    private val foodDao: FoodDao,
    private val authRepository: AuthRepository
) : FoodRepository {

    override fun searchFoods(filter: FoodFilter): Flow<List<Food>> {
        return when (filter.type) {
            FoodFilterType.ALL -> foodDao.searchFoods(filter.query, filter.limit)
            FoodFilterType.PLATFORM -> foodDao.searchFoodsByType(filter.query, "platform", filter.limit)
            FoodFilterType.USER -> foodDao.searchFoodsByType(filter.query, "user", filter.limit)
        }.map { entities -> entities.map { it.toDomain() } }
    }

    override fun getFoodById(id: String): Flow<Food?> {
        return foodDao.getFoodById(id).map { it?.toDomain() }
    }

    override suspend fun getFoodByBarcode(barcode: String): Food? {
        // Check local cache first
        val localFood = foodDao.getFoodByBarcode(barcode)
        if (localFood != null) {
            return localFood.toDomain()
        }

        // Fetch from API
        return try {
            val remoteFood = foodApi.getFoodByBarcode(barcode)
            if (remoteFood != null) {
                val entity = remoteFood.toEntity()
                foodDao.insert(entity)
                entity.toDomain()
            } else {
                null
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
            null
        }
    }

    override suspend fun createFood(params: CreateFoodParams): Result<Food> {
        return try {
            val currentUser = authRepository.currentUser.first()
                ?: return Result.failure(IllegalStateException("User not authenticated"))

            // Generate a temporary ID for local storage
            val tempId = "temp_${UUID.randomUUID()}"

            // Save to local database first (offline-first)
            val localEntity = params.toEntity(tempId, currentUser.id)
            foodDao.insert(localEntity)

            // Try to sync with API
            try {
                val response = foodApi.createFood(params.toRequest())
                val syncedEntity = response.toEntity()

                // Replace temp entity with synced entity
                foodDao.delete(tempId)
                foodDao.insert(syncedEntity)

                Result.success(syncedEntity.toDomain())
            } catch (apiError: Exception) {
                // API failed, but local save succeeded - return local food
                // Will be synced later
                Sentry.captureException(apiError)
                Result.success(localEntity.toDomain())
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
            Result.failure(e)
        }
    }

    override suspend fun updateFood(id: String, params: UpdateFoodParams): Result<Food> {
        return try {
            val existingFood = foodDao.getFoodByIdOnce(id)
                ?: return Result.failure(IllegalArgumentException("Food not found"))

            // Update locally first
            val updatedEntity = existingFood.copy(
                name = params.name ?: existingFood.name,
                brand = params.brand ?: existingFood.brand,
                barcode = params.barcode ?: existingFood.barcode,
                servingSize = params.servingSize ?: existingFood.servingSize,
                servingUnit = params.servingUnit?.name?.lowercase() ?: existingFood.servingUnit,
                servingsPerContainer = params.servingsPerContainer ?: existingFood.servingsPerContainer,
                calories = params.calories ?: existingFood.calories,
                protein = params.protein ?: existingFood.protein,
                carbs = params.carbs ?: existingFood.carbs,
                fat = params.fat ?: existingFood.fat,
                fiber = params.fiber ?: existingFood.fiber,
                sugar = params.sugar ?: existingFood.sugar,
                sodium = params.sodium ?: existingFood.sodium,
                saturatedFat = params.saturatedFat ?: existingFood.saturatedFat,
                transFat = params.transFat ?: existingFood.transFat,
                cholesterol = params.cholesterol ?: existingFood.cholesterol,
                addedSugar = params.addedSugar ?: existingFood.addedSugar,
                imageUrl = params.imageUrl ?: existingFood.imageUrl,
                updatedAt = System.currentTimeMillis(),
                syncedAt = null // Mark as needing sync
            )
            foodDao.update(updatedEntity)

            // Try to sync with API
            try {
                val response = foodApi.updateFood(id, params.toRequest())
                val syncedEntity = response.toEntity()
                foodDao.update(syncedEntity)
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

    override suspend fun deleteFood(id: String): Result<Unit> {
        return try {
            // Soft delete locally first
            foodDao.softDelete(id)

            // Try to sync with API
            try {
                foodApi.deleteFood(id)
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

    override suspend fun promoteFood(id: String): Result<Unit> {
        return try {
            val response = foodApi.promoteFood(id)
            val entity = response.toEntity()
            foodDao.update(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Sentry.captureException(e)
            Result.failure(e)
        }
    }

    override suspend fun refreshFoods(filter: FoodFilter) {
        try {
            val typeParam = when (filter.type) {
                FoodFilterType.ALL -> null
                FoodFilterType.PLATFORM -> "platform"
                FoodFilterType.USER -> "user"
            }

            val response = foodApi.listFoods(
                search = filter.query.ifBlank { null },
                type = typeParam,
                includeRecipes = filter.includeRecipes,
                includeMeals = filter.includeMeals,
                limit = filter.limit
            )

            val entities = response.foods.map { it.toEntity() }
            foodDao.insertAll(entities)
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }

    override fun getRecentFoods(limit: Int): Flow<List<Food>> {
        return foodDao.getRecentFoods(limit).map { entities -> entities.map { it.toDomain() } }
    }

    override fun getUserFoods(limit: Int): Flow<List<Food>> {
        return authRepository.currentUser
            .map { user ->
                if (user != null) {
                    foodDao.getUserFoods(user.id, limit).first().map { it.toDomain() }
                } else {
                    emptyList()
                }
            }
    }
}
