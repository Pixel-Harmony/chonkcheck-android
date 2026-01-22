package com.chonkcheck.android.data.api

import com.chonkcheck.android.data.api.dto.CreateDiaryEntryRequest
import com.chonkcheck.android.data.api.dto.DiaryEntriesResponse
import com.chonkcheck.android.data.api.dto.DiaryEntryDto
import com.chonkcheck.android.data.api.dto.UpdateDiaryEntryRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DiaryApi {

    @GET("diary/{date}")
    suspend fun getDiaryEntries(@Path("date") date: String): DiaryEntriesResponse

    @POST("diary")
    suspend fun createDiaryEntry(@Body request: CreateDiaryEntryRequest): DiaryEntryDto

    @PUT("diary/{id}")
    suspend fun updateDiaryEntry(
        @Path("id") id: String,
        @Body request: UpdateDiaryEntryRequest
    ): DiaryEntryDto

    @DELETE("diary/{id}")
    suspend fun deleteDiaryEntry(@Path("id") id: String)

    @POST("diary/{date}/complete")
    suspend fun completeDay(@Path("date") date: String)

    @DELETE("diary/{date}/complete")
    suspend fun uncompleteDay(@Path("date") date: String)
}
