package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.DiaryDay
import com.chonkcheck.android.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import java.time.LocalDate
import javax.inject.Inject

class GetDiaryDayUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    operator fun invoke(date: LocalDate): Flow<DiaryDay> {
        return diaryRepository.getDiaryDay(date)
            .onStart {
                // Trigger background refresh from API
                diaryRepository.refresh(date)
            }
    }
}
