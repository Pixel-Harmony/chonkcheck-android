package com.chonkcheck.android.data.api

import com.chonkcheck.android.data.api.dto.CreateWeightRequest
import com.chonkcheck.android.data.api.dto.WeightEntriesResponse
import com.chonkcheck.android.data.api.dto.WeightEntryDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WeightApi {

    @GET("weight")
    suspend fun getWeightEntries(@Query("limit") limit: Int? = null): WeightEntriesResponse

    @POST("weight")
    suspend fun createWeightEntry(@Body request: CreateWeightRequest): WeightEntryDto

    @DELETE("weight/{date}")
    suspend fun deleteWeightEntry(@Path("date") date: String)
}
