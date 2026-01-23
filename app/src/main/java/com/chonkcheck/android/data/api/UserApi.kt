package com.chonkcheck.android.data.api

import com.chonkcheck.android.data.api.dto.UpdateUserProfileRequest
import com.chonkcheck.android.data.api.dto.UserProfileDto
import com.chonkcheck.android.data.api.dto.UserExportResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserApi {

    @GET("user/profile")
    suspend fun getUserProfile(): UserProfileDto

    @PUT("user/profile")
    suspend fun updateUserProfile(@Body request: UpdateUserProfileRequest): UserProfileDto

    @GET("user/data-export")
    suspend fun exportUserData(): UserExportResponse

    @DELETE("user/account")
    suspend fun deleteAccount()
}
