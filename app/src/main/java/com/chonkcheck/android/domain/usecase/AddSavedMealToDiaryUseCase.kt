package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.AddMealToDiaryParams
import com.chonkcheck.android.domain.model.DiaryEntry
import com.chonkcheck.android.domain.repository.SavedMealRepository
import javax.inject.Inject

class AddSavedMealToDiaryUseCase @Inject constructor(
    private val savedMealRepository: SavedMealRepository
) {
    suspend operator fun invoke(params: AddMealToDiaryParams): Result<List<DiaryEntry>> {
        if (params.items.isEmpty()) {
            return Result.failure(IllegalArgumentException("Meal must have at least one item"))
        }

        return savedMealRepository.addMealToDiary(params)
    }
}

