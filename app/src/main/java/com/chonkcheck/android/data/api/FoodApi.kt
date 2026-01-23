package com.chonkcheck.android.data.api

import com.chonkcheck.android.data.api.dto.CreateFoodRequest
import com.chonkcheck.android.data.api.dto.FoodDto
import com.chonkcheck.android.data.api.dto.FoodsResponse
import com.chonkcheck.android.data.api.dto.UpdateFoodRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FoodApi {

    @GET("foods")
    suspend fun listFoods(
        @Query("search") search: String? = null,
        @Query("type") type: String? = null,
        @Query("includeRecipes") includeRecipes: Boolean = false,
        @Query("includeMeals") includeMeals: Boolean = false,
        @Query("limit") limit: Int = 50
    ): FoodsResponse

    @GET("foods/{id}")
    suspend fun getFoodById(@Path("id") id: String): FoodDto

    @POST("foods")
    suspend fun createFood(@Body request: CreateFoodRequest): FoodDto

    @PUT("foods/{id}")
    suspend fun updateFood(
        @Path("id") id: String,
        @Body request: UpdateFoodRequest
    ): FoodDto

    @DELETE("foods/{id}")
    suspend fun deleteFood(@Path("id") id: String)

    @POST("foods/{id}/promote")
    suspend fun promoteFood(@Path("id") id: String): FoodDto

    @GET("foods/barcode/{code}")
    suspend fun getFoodByBarcode(@Path("code") barcode: String): FoodDto?
}
