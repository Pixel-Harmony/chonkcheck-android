package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        // Check if recipe exists
        val existingRecipe = recipeRepository.getRecipeById(id).first()
            ?: return Result.failure(IllegalArgumentException("Recipe not found"))

        return recipeRepository.deleteRecipe(id)
    }
}
