package com.chonkcheck.android.data.repository

import com.chonkcheck.android.data.api.SavedMealApi
import com.chonkcheck.android.data.db.dao.FoodDao
import com.chonkcheck.android.data.db.dao.RecipeDao
import com.chonkcheck.android.data.db.dao.SavedMealDao
import com.chonkcheck.android.data.db.entity.SavedMealItemJson
import com.chonkcheck.android.data.mappers.toDomain
import com.chonkcheck.android.data.mappers.toEntity
import com.chonkcheck.android.data.mappers.toRequest
import com.chonkcheck.android.data.sync.SyncQueueHelper
import com.chonkcheck.android.domain.model.AddMealToDiaryParams
import com.chonkcheck.android.domain.model.CreateSavedMealParams
import com.chonkcheck.android.domain.model.DiaryEntry
import com.chonkcheck.android.domain.model.MealType
import com.chonkcheck.android.domain.model.SavedMeal
import com.chonkcheck.android.domain.model.SavedMealItemType
import com.chonkcheck.android.domain.model.ServingUnit
import com.chonkcheck.android.domain.model.SyncEntityType
import com.chonkcheck.android.domain.model.UpdateSavedMealParams
import com.chonkcheck.android.domain.repository.AuthRepository
import com.chonkcheck.android.domain.repository.SavedMealRepository
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedMealRepositoryImpl @Inject constructor(
    private val savedMealApi: SavedMealApi,
    private val savedMealDao: SavedMealDao,
    private val foodDao: FoodDao,
    private val recipeDao: RecipeDao,
    private val authRepository: AuthRepository,
    private val syncQueueHelper: SyncQueueHelper
) : SavedMealRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun searchSavedMeals(query: String, limit: Int): Flow<List<SavedMeal>> {
        return authRepository.currentUser
            .onStart {
                // Trigger background refresh from API
                CoroutineScope(Dispatchers.IO).launch {
                    refreshSavedMeals(query)
                }
            }
            .map { user ->
                if (user != null) {
                    savedMealDao.searchSavedMeals(user.id, query, limit).first().map { it.toDomain() }
                } else {
                    emptyList()
                }
            }
    }

    override fun getSavedMealById(id: String): Flow<SavedMeal?> {
        return savedMealDao.getSavedMealById(id).map { it?.toDomain() }
    }

    override suspend fun createSavedMeal(params: CreateSavedMealParams): Result<SavedMeal> {
        return try {
            val currentUser = authRepository.currentUser.first()
                ?: return Result.failure(IllegalStateException("User not authenticated"))

            // Build item details from foods/recipes in local database
            val itemDetails = buildItemDetails(params)

            // Generate a temporary ID for local storage
            val tempId = "temp_${UUID.randomUUID()}"

            // Save to local database first (offline-first)
            val localEntity = params.toEntity(tempId, currentUser.id, itemDetails)
            savedMealDao.insert(localEntity)

            // Try to sync with API
            try {
                val response = savedMealApi.createSavedMeal(params.toRequest())
                val syncedEntity = response.toEntity()

                // Replace temp entity with synced entity
                savedMealDao.delete(tempId)
                savedMealDao.insert(syncedEntity)

                Result.success(syncedEntity.toDomain())
            } catch (apiError: Exception) {
                // API failed, but local save succeeded - return local meal
                Sentry.captureException(apiError)
                // Queue for later sync
                syncQueueHelper.queueCreate(
                    entityType = SyncEntityType.SAVED_MEAL,
                    entityId = tempId,
                    payload = params.toRequest(),
                    serializer = com.chonkcheck.android.data.api.dto.CreateSavedMealRequest.serializer()
                )
                Result.success(localEntity.toDomain())
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
            Result.failure(e)
        }
    }

    override suspend fun updateSavedMeal(id: String, params: UpdateSavedMealParams): Result<SavedMeal> {
        return try {
            val existingMeal = savedMealDao.getSavedMealByIdOnce(id)
                ?: return Result.failure(IllegalArgumentException("Saved meal not found"))

            // Build updated item details if provided
            val itemDetails = if (params.items != null) {
                buildItemDetailsFromUpdateParams(params)
            } else {
                json.decodeFromString<List<SavedMealItemJson>>(existingMeal.itemsJson)
            }

            val totalCalories = itemDetails.sumOf { it.calories }
            val totalProtein = itemDetails.sumOf { it.protein }
            val totalCarbs = itemDetails.sumOf { it.carbs }
            val totalFat = itemDetails.sumOf { it.fat }

            // Update locally first
            val updatedEntity = existingMeal.copy(
                name = params.name ?: existingMeal.name,
                itemsJson = json.encodeToString(itemDetails),
                totalCalories = totalCalories,
                totalProtein = totalProtein,
                totalCarbs = totalCarbs,
                totalFat = totalFat,
                updatedAt = System.currentTimeMillis(),
                syncedAt = null
            )
            savedMealDao.update(updatedEntity)

            // Try to sync with API
            try {
                val request = com.chonkcheck.android.data.api.dto.CreateSavedMealRequest(
                    name = params.name ?: existingMeal.name,
                    items = params.items?.map { it.toRequest() } ?: emptyList()
                )
                val response = savedMealApi.updateSavedMeal(id, request)
                val syncedEntity = response.toEntity()
                savedMealDao.update(syncedEntity)
                Result.success(syncedEntity.toDomain())
            } catch (apiError: Exception) {
                Sentry.captureException(apiError)
                // Queue for later sync
                val request = com.chonkcheck.android.data.api.dto.CreateSavedMealRequest(
                    name = params.name ?: existingMeal.name,
                    items = params.items?.map { it.toRequest() } ?: emptyList()
                )
                syncQueueHelper.queueUpdate(
                    entityType = SyncEntityType.SAVED_MEAL,
                    entityId = id,
                    payload = request,
                    serializer = com.chonkcheck.android.data.api.dto.CreateSavedMealRequest.serializer()
                )
                Result.success(updatedEntity.toDomain())
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
            Result.failure(e)
        }
    }

    override suspend fun deleteSavedMeal(id: String): Result<Unit> {
        return try {
            // Soft delete locally first
            savedMealDao.softDelete(id)

            // Try to sync with API
            try {
                savedMealApi.deleteSavedMeal(id)
            } catch (apiError: Exception) {
                Sentry.captureException(apiError)
                // Queue for later sync
                syncQueueHelper.queueDelete(
                    entityType = SyncEntityType.SAVED_MEAL,
                    entityId = id
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Sentry.captureException(e)
            Result.failure(e)
        }
    }

    override suspend fun addMealToDiary(params: AddMealToDiaryParams): Result<List<DiaryEntry>> {
        return try {
            val currentUser = authRepository.currentUser.first()
                ?: return Result.failure(IllegalStateException("User not authenticated"))

            // Increment usage count locally
            savedMealDao.incrementUsage(params.savedMealId)

            // Call API to add meal to diary
            val response = savedMealApi.addMealToDiary(params.toRequest())

            // Map response entries to domain
            val entries = response.entries.map { dto ->
                DiaryEntry(
                    id = dto.id,
                    userId = currentUser.id,
                    date = LocalDate.parse(dto.date),
                    mealType = MealType.fromApiValue(dto.mealType),
                    foodId = dto.foodId,
                    recipeId = dto.recipeId,
                    servingSize = dto.quantity,
                    servingUnit = ServingUnit.GRAM,
                    numberOfServings = 1.0,
                    calories = dto.calories,
                    protein = dto.protein,
                    carbs = dto.carbs,
                    fat = dto.fat,
                    name = dto.name,
                    brand = dto.brand,
                    createdAt = System.currentTimeMillis()
                )
            }

            Result.success(entries)
        } catch (e: Exception) {
            Sentry.captureException(e)
            Result.failure(e)
        }
    }

    override suspend fun refreshSavedMeals(query: String) {
        try {
            val response = savedMealApi.listSavedMeals(
                search = query.ifBlank { null }
            )
            val entities = response.meals.map { it.toEntity() }
            savedMealDao.insertAll(entities)
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }

    private suspend fun buildItemDetails(params: CreateSavedMealParams): List<SavedMealItemJson> {
        return params.items.mapNotNull { itemParam ->
            when (itemParam.itemType) {
                SavedMealItemType.FOOD -> {
                    val food = foodDao.getFoodByIdOnce(itemParam.itemId) ?: return@mapNotNull null
                    SavedMealItemJson(
                        type = "food",
                        foodId = food.id,
                        recipeId = null,
                        name = food.name,
                        brand = food.brand,
                        servingSize = food.servingSize,
                        servingUnit = food.servingUnit,
                        numberOfServings = itemParam.quantity,
                        enteredAmount = itemParam.enteredAmount,
                        calories = food.calories * itemParam.quantity,
                        protein = food.protein * itemParam.quantity,
                        carbs = food.carbs * itemParam.quantity,
                        fat = food.fat * itemParam.quantity
                    )
                }
                SavedMealItemType.RECIPE -> {
                    val recipe = recipeDao.getRecipeByIdOnce(itemParam.itemId) ?: return@mapNotNull null
                    SavedMealItemJson(
                        type = "recipe",
                        foodId = null,
                        recipeId = recipe.id,
                        name = recipe.name,
                        brand = null,
                        servingSize = 1.0,
                        servingUnit = recipe.servingUnit ?: "serving",
                        numberOfServings = itemParam.quantity,
                        enteredAmount = itemParam.enteredAmount,
                        calories = recipe.caloriesPerServing * itemParam.quantity,
                        protein = recipe.proteinPerServing * itemParam.quantity,
                        carbs = recipe.carbsPerServing * itemParam.quantity,
                        fat = recipe.fatPerServing * itemParam.quantity
                    )
                }
            }
        }
    }

    private suspend fun buildItemDetailsFromUpdateParams(params: UpdateSavedMealParams): List<SavedMealItemJson> {
        return params.items?.mapNotNull { itemParam ->
            when (itemParam.itemType) {
                SavedMealItemType.FOOD -> {
                    val food = foodDao.getFoodByIdOnce(itemParam.itemId) ?: return@mapNotNull null
                    SavedMealItemJson(
                        type = "food",
                        foodId = food.id,
                        recipeId = null,
                        name = food.name,
                        brand = food.brand,
                        servingSize = food.servingSize,
                        servingUnit = food.servingUnit,
                        numberOfServings = itemParam.quantity,
                        enteredAmount = itemParam.enteredAmount,
                        calories = food.calories * itemParam.quantity,
                        protein = food.protein * itemParam.quantity,
                        carbs = food.carbs * itemParam.quantity,
                        fat = food.fat * itemParam.quantity
                    )
                }
                SavedMealItemType.RECIPE -> {
                    val recipe = recipeDao.getRecipeByIdOnce(itemParam.itemId) ?: return@mapNotNull null
                    SavedMealItemJson(
                        type = "recipe",
                        foodId = null,
                        recipeId = recipe.id,
                        name = recipe.name,
                        brand = null,
                        servingSize = 1.0,
                        servingUnit = recipe.servingUnit ?: "serving",
                        numberOfServings = itemParam.quantity,
                        enteredAmount = itemParam.enteredAmount,
                        calories = recipe.caloriesPerServing * itemParam.quantity,
                        protein = recipe.proteinPerServing * itemParam.quantity,
                        carbs = recipe.carbsPerServing * itemParam.quantity,
                        fat = recipe.fatPerServing * itemParam.quantity
                    )
                }
            }
        } ?: emptyList()
    }
}
