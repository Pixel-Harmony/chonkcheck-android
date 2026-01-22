package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.Recipe
import com.chonkcheck.android.domain.model.UpdateRecipeParams
import com.chonkcheck.android.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(id: String, params: UpdateRecipeParams): Result<Recipe> {
        // Check if recipe exists
        val existingRecipe = recipeRepository.getRecipeById(id).first()
            ?: return Result.failure(IllegalArgumentException("Recipe not found"))

        // Validate name if provided
        if (params.name != null && params.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Recipe name cannot be empty"))
        }

        // Validate total servings if provided
        if (params.totalServings != null && params.totalServings <= 0) {
            return Result.failure(IllegalArgumentException("Total servings must be positive"))
        }

        // Validate ingredients if provided
        if (params.ingredients != null && params.ingredients.isEmpty()) {
            return Result.failure(IllegalArgumentException("Recipe must have at least one ingredient"))
        }

        return recipeRepository.updateRecipe(id, params)
    }
}
