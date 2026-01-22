package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.CreateWeightParams
import com.chonkcheck.android.domain.model.WeightEntry
import com.chonkcheck.android.domain.repository.WeightRepository
import javax.inject.Inject

class CreateWeightEntryUseCase @Inject constructor(
    private val weightRepository: WeightRepository
) {
    suspend operator fun invoke(params: CreateWeightParams): Result<WeightEntry> {
        return weightRepository.createEntry(params)
    }
}
