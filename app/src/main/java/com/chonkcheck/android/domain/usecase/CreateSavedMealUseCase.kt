package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.CreateSavedMealParams
import com.chonkcheck.android.domain.model.SavedMeal
import com.chonkcheck.android.domain.repository.SavedMealRepository
import javax.inject.Inject

class CreateSavedMealUseCase @Inject constructor(
    private val savedMealRepository: SavedMealRepository
) {
    suspend operator fun invoke(params: CreateSavedMealParams): Result<SavedMeal> {
        // Validate required fields
        if (params.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Meal name is required"))
        }
        if (params.items.isEmpty()) {
            return Result.failure(IllegalArgumentException("Meal must have at least one item"))
        }

        return savedMealRepository.createSavedMeal(params)
    }
}
