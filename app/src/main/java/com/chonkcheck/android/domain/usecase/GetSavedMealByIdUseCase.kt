package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.SavedMeal
import com.chonkcheck.android.domain.repository.SavedMealRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedMealByIdUseCase @Inject constructor(
    private val savedMealRepository: SavedMealRepository
) {
    operator fun invoke(id: String): Flow<SavedMeal?> {
        return savedMealRepository.getSavedMealById(id)
    }
}
