package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.SavedMeal
import com.chonkcheck.android.domain.model.UpdateSavedMealParams
import com.chonkcheck.android.domain.repository.SavedMealRepository
import javax.inject.Inject

class UpdateSavedMealUseCase @Inject constructor(
    private val savedMealRepository: SavedMealRepository
) {
    suspend operator fun invoke(id: String, params: UpdateSavedMealParams): Result<SavedMeal> {
        // Validate if name is provided
        if (params.name != null && params.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Meal name cannot be blank"))
        }
        // Validate if items are provided
        if (params.items != null && params.items.isEmpty()) {
            return Result.failure(IllegalArgumentException("Meal must have at least one item"))
        }

        return savedMealRepository.updateSavedMeal(id, params)
    }
}
