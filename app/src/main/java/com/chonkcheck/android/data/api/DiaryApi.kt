package com.chonkcheck.android.data.api

import com.chonkcheck.android.data.api.dto.CreateDiaryEntryRequest
import com.chonkcheck.android.data.api.dto.DiaryDayResponse
import com.chonkcheck.android.data.api.dto.DiaryEntryDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface DiaryApi {

    @GET("diary/{date}")
    suspend fun getDiaryEntries(@Path("date") date: String): DiaryDayResponse

    @POST("diary")
    suspend fun createDiaryEntry(@Body request: CreateDiaryEntryRequest): DiaryEntryDto

    @DELETE("diary/{id}")
    suspend fun deleteDiaryEntry(@Path("id") id: String)

    @POST("diary/{date}/complete")
    suspend fun completeDay(@Path("date") date: String)

    @DELETE("diary/{date}/complete")
    suspend fun uncompleteDay(@Path("date") date: String)
}
