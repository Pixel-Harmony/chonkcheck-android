package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.Recipe
import com.chonkcheck.android.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class SearchRecipesUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(query: String, limit: Int = 50): Flow<List<Recipe>> {
        return recipeRepository.searchRecipes(query, limit)
            .onStart {
                // Trigger background refresh from API
                recipeRepository.refreshRecipes(query)
            }
    }
}
