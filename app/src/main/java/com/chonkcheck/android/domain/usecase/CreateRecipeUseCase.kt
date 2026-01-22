package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.CreateRecipeParams
import com.chonkcheck.android.domain.model.Recipe
import com.chonkcheck.android.domain.repository.RecipeRepository
import javax.inject.Inject

class CreateRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(params: CreateRecipeParams): Result<Recipe> {
        // Validate required fields
        if (params.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Recipe name is required"))
        }
        if (params.totalServings <= 0) {
            return Result.failure(IllegalArgumentException("Total servings must be positive"))
        }
        if (params.ingredients.isEmpty()) {
            return Result.failure(IllegalArgumentException("Recipe must have at least one ingredient"))
        }

        return recipeRepository.createRecipe(params)
    }
}
