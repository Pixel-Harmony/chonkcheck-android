package com.chonkcheck.android.data.api

import com.chonkcheck.android.data.api.dto.CreateRecipeRequest
import com.chonkcheck.android.data.api.dto.RecipeDto
import com.chonkcheck.android.data.api.dto.RecipesResponse
import com.chonkcheck.android.data.api.dto.UpdateRecipeRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RecipeApi {

    @GET("recipes")
    suspend fun listRecipes(
        @Query("search") search: String? = null,
        @Query("limit") limit: Int = 50
    ): RecipesResponse

    @GET("recipes/{id}")
    suspend fun getRecipeById(@Path("id") id: String): RecipeDto

    @POST("recipes")
    suspend fun createRecipe(@Body request: CreateRecipeRequest): RecipeDto

    @PUT("recipes/{id}")
    suspend fun updateRecipe(
        @Path("id") id: String,
        @Body request: UpdateRecipeRequest
    ): RecipeDto

    @DELETE("recipes/{id}")
    suspend fun deleteRecipe(@Path("id") id: String)
}
