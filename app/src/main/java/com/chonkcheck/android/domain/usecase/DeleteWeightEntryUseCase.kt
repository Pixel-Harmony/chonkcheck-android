package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.repository.WeightRepository
import java.time.LocalDate
import javax.inject.Inject

class DeleteWeightEntryUseCase @Inject constructor(
    private val weightRepository: WeightRepository
) {
    suspend operator fun invoke(date: LocalDate): Result<Unit> {
        return weightRepository.deleteEntry(date)
    }
}
