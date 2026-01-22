package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.repository.DiaryRepository
import javax.inject.Inject

class DeleteDiaryEntryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return diaryRepository.deleteEntry(id)
    }
}
