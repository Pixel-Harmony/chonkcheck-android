package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.data.network.NetworkMonitor
import com.chonkcheck.android.domain.model.DiaryDay
import com.chonkcheck.android.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import javax.inject.Inject

class GetDiaryDayUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val networkMonitor: NetworkMonitor
) {
    operator fun invoke(date: LocalDate): Flow<DiaryDay> = flow {
        // When online, wait for API refresh to complete before emitting
        if (networkMonitor.isOnline()) {
            try {
                diaryRepository.refresh(date)
            } catch (e: Exception) {
                // If refresh fails, fall through to emit cached data
            }
        }
        emitAll(diaryRepository.getDiaryDay(date))
    }
}
