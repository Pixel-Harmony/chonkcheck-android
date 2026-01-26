package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.data.network.NetworkMonitor
import com.chonkcheck.android.domain.model.SavedMeal
import com.chonkcheck.android.domain.repository.SavedMealRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SearchSavedMealsUseCase @Inject constructor(
    private val savedMealRepository: SavedMealRepository,
    private val networkMonitor: NetworkMonitor
) {
    operator fun invoke(query: String, limit: Int = 50): Flow<List<SavedMeal>> = flow {
        // When online, wait for API refresh to complete before emitting
        if (networkMonitor.isOnline()) {
            try {
                savedMealRepository.refreshSavedMeals(query)
            } catch (e: Exception) {
                // If refresh fails, fall through to emit cached data
            }
        }
        emitAll(savedMealRepository.searchSavedMeals(query, limit))
    }
}
