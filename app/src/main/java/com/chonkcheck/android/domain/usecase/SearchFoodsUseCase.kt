package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.Food
import com.chonkcheck.android.domain.model.FoodFilter
import com.chonkcheck.android.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class SearchFoodsUseCase @Inject constructor(
    private val foodRepository: FoodRepository
) {
    operator fun invoke(filter: FoodFilter): Flow<List<Food>> {
        return foodRepository.searchFoods(filter)
            .onStart {
                // Trigger background refresh from API
                foodRepository.refreshFoods(filter)
            }
    }
}
