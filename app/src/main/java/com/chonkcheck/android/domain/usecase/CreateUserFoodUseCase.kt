package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.CreateFoodParams
import com.chonkcheck.android.domain.model.Food
import com.chonkcheck.android.domain.repository.FoodRepository
import javax.inject.Inject

class CreateUserFoodUseCase @Inject constructor(
    private val foodRepository: FoodRepository
) {
    suspend operator fun invoke(params: CreateFoodParams): Result<Food> {
        // Validate required fields
        if (params.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Food name is required"))
        }
        if (params.servingSize <= 0) {
            return Result.failure(IllegalArgumentException("Serving size must be positive"))
        }
        if (params.calories < 0) {
            return Result.failure(IllegalArgumentException("Calories cannot be negative"))
        }

        // Check for barcode conflicts if provided
        if (!params.barcode.isNullOrBlank()) {
            val existingFood = foodRepository.getFoodByBarcode(params.barcode)
            if (existingFood != null) {
                return Result.failure(
                    IllegalStateException("A food with this barcode already exists: ${existingFood.name}")
                )
            }
        }

        return foodRepository.createFood(params)
    }
}
