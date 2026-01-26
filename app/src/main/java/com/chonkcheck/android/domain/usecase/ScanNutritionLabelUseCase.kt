package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.data.api.NutritionLabelApi
import com.chonkcheck.android.data.api.dto.ScanNutritionLabelRequest
import com.chonkcheck.android.data.mappers.toDomain
import com.chonkcheck.android.domain.model.NutritionLabelScanResult
import javax.inject.Inject

class ScanNutritionLabelUseCase @Inject constructor(
    private val nutritionLabelApi: NutritionLabelApi
) {
    suspend operator fun invoke(
        imageBase64: String,
        mediaType: String = "image/jpeg"
    ): NutritionLabelScanResult {
        return try {
            val request = ScanNutritionLabelRequest(
                image = imageBase64,
                mediaType = mediaType
            )
            val response = nutritionLabelApi.scanNutritionLabel(request)

            if (response.success && response.nutritionData != null) {
                NutritionLabelScanResult.Success(response.nutritionData.toDomain())
            } else {
                NutritionLabelScanResult.Error(
                    response.message ?: "Could not extract nutrition information from the image"
                )
            }
        } catch (e: Exception) {
            NutritionLabelScanResult.Error(e.message ?: "An error occurred while scanning the label")
        }
    }
}
