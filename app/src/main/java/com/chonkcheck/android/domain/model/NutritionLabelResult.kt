package com.chonkcheck.android.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NutritionLabelData(
    val name: String,
    val brand: String? = null,
    val servingSize: Double,
    val servingUnit: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double? = null,
    val sugar: Double? = null,
    val sodium: Double? = null,
    val saturatedFat: Double? = null,
    val cholesterol: Double? = null
) : Parcelable

sealed class NutritionLabelScanResult {
    data class Success(val data: NutritionLabelData) : NutritionLabelScanResult()
    data class Error(val message: String) : NutritionLabelScanResult()
}
