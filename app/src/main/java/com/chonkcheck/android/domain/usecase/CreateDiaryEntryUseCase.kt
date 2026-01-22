package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.CreateDiaryEntryParams
import com.chonkcheck.android.domain.model.DiaryEntry
import com.chonkcheck.android.domain.repository.DiaryRepository
import javax.inject.Inject

class CreateDiaryEntryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(params: CreateDiaryEntryParams): Result<DiaryEntry> {
        return diaryRepository.createEntry(params)
    }
}
