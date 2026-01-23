package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.data.network.NetworkMonitor
import com.chonkcheck.android.domain.model.WeightEntry
import com.chonkcheck.android.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetWeightEntriesUseCase @Inject constructor(
    private val weightRepository: WeightRepository,
    private val networkMonitor: NetworkMonitor
) {
    operator fun invoke(limit: Int? = null): Flow<List<WeightEntry>> = flow {
        // When online, wait for API refresh to complete before emitting
        if (networkMonitor.isOnline()) {
            try {
                weightRepository.refresh()
            } catch (e: Exception) {
                // If refresh fails, fall through to emit cached data
            }
        }
        emitAll(weightRepository.getWeightEntries(limit))
    }
}
