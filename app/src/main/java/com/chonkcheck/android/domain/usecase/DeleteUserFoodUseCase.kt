package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.FoodType
import com.chonkcheck.android.domain.repository.FoodRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteUserFoodUseCase @Inject constructor(
    private val foodRepository: FoodRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        // Check if food exists and is user-owned
        val existingFood = foodRepository.getFoodById(id).first()
            ?: return Result.failure(IllegalArgumentException("Food not found"))

        if (existingFood.type != FoodType.USER) {
            return Result.failure(IllegalArgumentException("Cannot delete platform foods"))
        }

        return foodRepository.deleteFood(id)
    }
}
