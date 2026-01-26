package com.chonkcheck.android.data.api

import com.chonkcheck.android.data.api.dto.CreateExerciseRequest
import com.chonkcheck.android.data.api.dto.ExerciseDto
import com.chonkcheck.android.data.api.dto.UpdateExerciseRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ExerciseApi {

    @GET("exercises/{date}")
    suspend fun getExercisesForDate(@Path("date") date: String): List<ExerciseDto>

    @POST("exercises")
    suspend fun createExercise(@Body request: CreateExerciseRequest): ExerciseDto

    @PUT("exercises/{id}")
    suspend fun updateExercise(
        @Path("id") id: String,
        @Body request: UpdateExerciseRequest
    ): ExerciseDto

    @DELETE("exercises/{id}")
    suspend fun deleteExercise(@Path("id") id: String)
}
