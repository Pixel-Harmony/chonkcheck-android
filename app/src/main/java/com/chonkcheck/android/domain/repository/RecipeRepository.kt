package com.chonkcheck.android.domain.repository

import com.chonkcheck.android.domain.model.CreateRecipeParams
import com.chonkcheck.android.domain.model.Recipe
import com.chonkcheck.android.domain.model.UpdateRecipeParams
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {

    fun searchRecipes(query: String, limit: Int = 50): Flow<List<Recipe>>

    fun getRecipeById(id: String): Flow<Recipe?>

    suspend fun createRecipe(params: CreateRecipeParams): Result<Recipe>

    suspend fun updateRecipe(id: String, params: UpdateRecipeParams): Result<Recipe>

    suspend fun deleteRecipe(id: String): Result<Unit>

    suspend fun refreshRecipes(query: String)
}
