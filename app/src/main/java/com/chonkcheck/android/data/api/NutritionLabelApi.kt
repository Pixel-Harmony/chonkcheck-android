package com.chonkcheck.android.data.api

import com.chonkcheck.android.data.api.dto.ScanNutritionLabelRequest
import com.chonkcheck.android.data.api.dto.ScanNutritionLabelResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface NutritionLabelApi {

    @POST("nutrition-labels")
    suspend fun scanNutritionLabel(@Body request: ScanNutritionLabelRequest): ScanNutritionLabelResponse
}
