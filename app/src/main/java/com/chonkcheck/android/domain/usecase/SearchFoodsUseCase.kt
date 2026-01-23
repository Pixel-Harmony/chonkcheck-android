package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.data.network.NetworkMonitor
import com.chonkcheck.android.domain.model.Food
import com.chonkcheck.android.domain.model.FoodFilter
import com.chonkcheck.android.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SearchFoodsUseCase @Inject constructor(
    private val foodRepository: FoodRepository,
    private val networkMonitor: NetworkMonitor
) {
    operator fun invoke(filter: FoodFilter): Flow<List<Food>> = flow {
        // When online, wait for API refresh to complete before emitting
        if (networkMonitor.isOnline()) {
            try {
                foodRepository.refreshFoods(filter)
            } catch (e: Exception) {
                // If refresh fails, fall through to emit cached data
            }
        }
        emitAll(foodRepository.searchFoods(filter))
    }
}
