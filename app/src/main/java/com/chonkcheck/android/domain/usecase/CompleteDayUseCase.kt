package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.repository.DiaryRepository
import java.time.LocalDate
import javax.inject.Inject

class CompleteDayUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    suspend fun complete(date: LocalDate): Result<Unit> {
        return diaryRepository.completeDay(date)
    }

    suspend fun uncomplete(date: LocalDate): Result<Unit> {
        return diaryRepository.uncompleteDay(date)
    }
}
