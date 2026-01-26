package com.chonkcheck.android.data.api

import com.chonkcheck.android.data.api.dto.AddMealToDiaryRequest
import com.chonkcheck.android.data.api.dto.AddMealToDiaryResponse
import com.chonkcheck.android.data.api.dto.CreateSavedMealRequest
import com.chonkcheck.android.data.api.dto.SavedMealDto
import com.chonkcheck.android.data.api.dto.SavedMealsResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SavedMealApi {

    @GET("meals")
    suspend fun listSavedMeals(
        @Query("search") search: String? = null,
        @Query("limit") limit: Int = 50
    ): SavedMealsResponse

    @GET("meals/{id}")
    suspend fun getSavedMealById(@Path("id") id: String): SavedMealDto

    @POST("meals")
    suspend fun createSavedMeal(@Body request: CreateSavedMealRequest): SavedMealDto

    @PUT("meals/{id}")
    suspend fun updateSavedMeal(
        @Path("id") id: String,
        @Body request: CreateSavedMealRequest
    ): SavedMealDto

    @DELETE("meals/{id}")
    suspend fun deleteSavedMeal(@Path("id") id: String)

    @POST("diary/meal")
    suspend fun addMealToDiary(@Body request: AddMealToDiaryRequest): AddMealToDiaryResponse
}
