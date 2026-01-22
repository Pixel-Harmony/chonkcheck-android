package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.Food
import com.chonkcheck.android.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFoodByIdUseCase @Inject constructor(
    private val foodRepository: FoodRepository
) {
    operator fun invoke(id: String): Flow<Food?> {
        return foodRepository.getFoodById(id)
    }
}
