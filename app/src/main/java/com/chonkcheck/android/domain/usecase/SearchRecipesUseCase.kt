package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.data.network.NetworkMonitor
import com.chonkcheck.android.domain.model.Recipe
import com.chonkcheck.android.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SearchRecipesUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val networkMonitor: NetworkMonitor
) {
    operator fun invoke(query: String, limit: Int = 50): Flow<List<Recipe>> = flow {
        // When online, wait for API refresh to complete before emitting
        if (networkMonitor.isOnline()) {
            try {
                recipeRepository.refreshRecipes(query)
            } catch (e: Exception) {
                // If refresh fails, fall through to emit cached data
            }
        }
        emitAll(recipeRepository.searchRecipes(query, limit))
    }
}
