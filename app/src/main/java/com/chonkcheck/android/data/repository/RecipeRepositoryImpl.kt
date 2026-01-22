package com.chonkcheck.android.data.repository

import com.chonkcheck.android.data.api.RecipeApi
import com.chonkcheck.android.data.db.dao.FoodDao
import com.chonkcheck.android.data.db.dao.RecipeDao
import com.chonkcheck.android.data.db.entity.RecipeIngredientJson
import com.chonkcheck.android.data.mappers.toDomain
import com.chonkcheck.android.data.mappers.toEntity
import com.chonkcheck.android.data.mappers.toRequest
import com.chonkcheck.android.domain.model.CreateRecipeParams
import com.chonkcheck.android.domain.model.Recipe
import com.chonkcheck.android.domain.model.UpdateRecipeParams
import com.chonkcheck.android.domain.repository.AuthRepository
import com.chonkcheck.android.domain.repository.RecipeRepository
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepositoryImpl @Inject constructor(
    private val recipeApi: RecipeApi,
    private val recipeDao: RecipeDao,
    private val foodDao: FoodDao,
    private val authRepository: AuthRepository
) : RecipeRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun searchRecipes(query: String, limit: Int): Flow<List<Recipe>> {
        return authRepository.currentUser
            .map { user ->
                if (user != null) {
                    recipeDao.searchRecipes(user.id, query, limit).first().map { it.toDomain() }
                } else {
                    emptyList()
                }
            }
    }

    override fun getRecipeById(id: String): Flow<Recipe?> {
        return recipeDao.getRecipeById(id).map { it?.toDomain() }
    }

    override suspend fun createRecipe(params: CreateRecipeParams): Result<Recipe> {
        return try {
            val currentUser = authRepository.currentUser.first()
                ?: return Result.failure(IllegalStateException("User not authenticated"))

            // Build ingredient details from foods in local database
            val ingredientDetails = buildIngredientDetails(params)

            // Generate a temporary ID for local storage
            val tempId = "temp_${UUID.randomUUID()}"

            // Save to local database first (offline-first)
            val localEntity = params.toEntity(tempId, currentUser.id, ingredientDetails)
            recipeDao.insert(localEntity)

            // Try to sync with API
            try {
                val response = recipeApi.createRecipe(params.toRequest())
                val syncedEntity = response.toEntity()

                // Replace temp entity with synced entity
                recipeDao.delete(tempId)
                recipeDao.insert(syncedEntity)

                Result.success(syncedEntity.toDomain())
            } catch (apiError: Exception) {
                // API failed, but local save succeeded - return local recipe
                Sentry.captureException(apiError)
                Result.success(localEntity.toDomain())
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
            Result.failure(e)
        }
    }

    override suspend fun updateRecipe(id: String, params: UpdateRecipeParams): Result<Recipe> {
        return try {
            val existingRecipe = recipeDao.getRecipeByIdOnce(id)
                ?: return Result.failure(IllegalArgumentException("Recipe not found"))

            // Build updated ingredient details if provided
            val ingredientDetails = if (params.ingredients != null) {
                buildIngredientDetailsFromParams(params)
            } else {
                json.decodeFromString<List<RecipeIngredientJson>>(existingRecipe.ingredientsJson)
            }

            val totalCalories = ingredientDetails.sumOf { it.calories }
            val totalProtein = ingredientDetails.sumOf { it.protein }
            val totalCarbs = ingredientDetails.sumOf { it.carbs }
            val totalFat = ingredientDetails.sumOf { it.fat }
            val servings = params.totalServings ?: existingRecipe.servings

            // Update locally first
            val updatedEntity = existingRecipe.copy(
                name = params.name ?: existingRecipe.name,
                description = params.description ?: existingRecipe.description,
                servings = servings,
                servingUnit = params.servingUnit?.name?.lowercase() ?: existingRecipe.servingUnit,
                caloriesPerServing = totalCalories / servings,
                proteinPerServing = totalProtein / servings,
                carbsPerServing = totalCarbs / servings,
                fatPerServing = totalFat / servings,
                ingredientsJson = json.encodeToString(ingredientDetails),
                updatedAt = System.currentTimeMillis(),
                syncedAt = null
            )
            recipeDao.update(updatedEntity)

            // Try to sync with API
            try {
                val response = recipeApi.updateRecipe(id, params.toRequest())
                val syncedEntity = response.toEntity()
                recipeDao.update(syncedEntity)
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

    override suspend fun deleteRecipe(id: String): Result<Unit> {
        return try {
            // Soft delete locally first
            recipeDao.softDelete(id)

            // Try to sync with API
            try {
                recipeApi.deleteRecipe(id)
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

    override suspend fun refreshRecipes(query: String) {
        try {
            val response = recipeApi.listRecipes(
                search = query.ifBlank { null }
            )
            val entities = response.recipes.map { it.toEntity() }
            recipeDao.insertAll(entities)
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }

    private suspend fun buildIngredientDetails(params: CreateRecipeParams): List<RecipeIngredientJson> {
        return params.ingredients.mapNotNull { ingredientParam ->
            val food = foodDao.getFoodByIdOnce(ingredientParam.foodId) ?: return@mapNotNull null
            RecipeIngredientJson(
                foodId = food.id,
                foodName = food.name,
                servingSize = food.servingSize,
                servingUnit = food.servingUnit,
                numberOfServings = ingredientParam.quantity,
                calories = food.calories * ingredientParam.quantity,
                protein = food.protein * ingredientParam.quantity,
                carbs = food.carbs * ingredientParam.quantity,
                fat = food.fat * ingredientParam.quantity
            )
        }
    }

    private suspend fun buildIngredientDetailsFromParams(params: UpdateRecipeParams): List<RecipeIngredientJson> {
        return params.ingredients?.mapNotNull { ingredientParam ->
            val food = foodDao.getFoodByIdOnce(ingredientParam.foodId) ?: return@mapNotNull null
            RecipeIngredientJson(
                foodId = food.id,
                foodName = food.name,
                servingSize = food.servingSize,
                servingUnit = food.servingUnit,
                numberOfServings = ingredientParam.quantity,
                calories = food.calories * ingredientParam.quantity,
                protein = food.protein * ingredientParam.quantity,
                carbs = food.carbs * ingredientParam.quantity,
                fat = food.fat * ingredientParam.quantity
            )
        } ?: emptyList()
    }
}
