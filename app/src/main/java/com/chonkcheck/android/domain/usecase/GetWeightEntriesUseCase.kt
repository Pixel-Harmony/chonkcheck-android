package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.WeightEntry
import com.chonkcheck.android.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class GetWeightEntriesUseCase @Inject constructor(
    private val weightRepository: WeightRepository
) {
    operator fun invoke(limit: Int? = null): Flow<List<WeightEntry>> {
        return weightRepository.getWeightEntries(limit)
            .onStart {
                weightRepository.refresh()
            }
    }
}
