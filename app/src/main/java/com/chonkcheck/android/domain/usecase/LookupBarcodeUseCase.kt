package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.Food
import com.chonkcheck.android.domain.repository.FoodRepository
import javax.inject.Inject

sealed class BarcodeResult {
    data class Found(val food: Food) : BarcodeResult()
    data object NotFound : BarcodeResult()
    data class Error(val message: String) : BarcodeResult()
}

class LookupBarcodeUseCase @Inject constructor(
    private val foodRepository: FoodRepository
) {
    suspend operator fun invoke(barcode: String): BarcodeResult {
        if (barcode.isBlank()) {
            return BarcodeResult.Error("Invalid barcode")
        }

        return try {
            val food = foodRepository.getFoodByBarcode(barcode)
            if (food != null) {
                BarcodeResult.Found(food)
            } else {
                BarcodeResult.NotFound
            }
        } catch (e: Exception) {
            BarcodeResult.Error(e.message ?: "Unknown error")
        }
    }
}
