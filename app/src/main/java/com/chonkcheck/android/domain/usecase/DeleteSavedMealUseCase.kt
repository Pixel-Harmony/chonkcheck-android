package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.repository.SavedMealRepository
import javax.inject.Inject

class DeleteSavedMealUseCase @Inject constructor(
    private val savedMealRepository: SavedMealRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return savedMealRepository.deleteSavedMeal(id)
    }
}
