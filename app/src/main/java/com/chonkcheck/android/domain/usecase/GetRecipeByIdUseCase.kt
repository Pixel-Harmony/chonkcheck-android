package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.Recipe
import com.chonkcheck.android.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecipeByIdUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(id: String): Flow<Recipe?> {
        return recipeRepository.getRecipeById(id)
    }
}
