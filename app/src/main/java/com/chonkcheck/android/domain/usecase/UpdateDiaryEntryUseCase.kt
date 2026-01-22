package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.DiaryEntry
import com.chonkcheck.android.domain.model.UpdateDiaryEntryParams
import com.chonkcheck.android.domain.repository.DiaryRepository
import javax.inject.Inject

class UpdateDiaryEntryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(id: String, params: UpdateDiaryEntryParams): Result<DiaryEntry> {
        return diaryRepository.updateEntry(id, params)
    }
}
