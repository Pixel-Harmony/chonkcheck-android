package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.Food
import com.chonkcheck.android.domain.model.FoodType
import com.chonkcheck.android.domain.model.UpdateFoodParams
import com.chonkcheck.android.domain.repository.FoodRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateUserFoodUseCase @Inject constructor(
    private val foodRepository: FoodRepository
) {
    suspend operator fun invoke(id: String, params: UpdateFoodParams): Result<Food> {
        // Check if food exists and is user-owned
        val existingFood = foodRepository.getFoodById(id).first()
            ?: return Result.failure(IllegalArgumentException("Food not found"))

        if (existingFood.type != FoodType.USER) {
            return Result.failure(IllegalArgumentException("Cannot edit platform foods"))
        }

        // Validate updates
        if (params.name != null && params.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Food name cannot be empty"))
        }
        if (params.servingSize != null && params.servingSize <= 0) {
            return Result.failure(IllegalArgumentException("Serving size must be positive"))
        }
        if (params.calories != null && params.calories < 0) {
            return Result.failure(IllegalArgumentException("Calories cannot be negative"))
        }

        return foodRepository.updateFood(id, params)
    }
}
