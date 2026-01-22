package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.DiaryEntry
import com.chonkcheck.android.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDiaryEntryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    operator fun invoke(id: String): Flow<DiaryEntry?> {
        return diaryRepository.getDiaryEntryById(id)
    }
}
