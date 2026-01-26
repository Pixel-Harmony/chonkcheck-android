package com.chonkcheck.android.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateExerciseRequest(
    val name: String,
    val caloriesBurned: Double,
    val description: String? = null,
    val date: String
)

@Serializable
data class UpdateExerciseRequest(
    val name: String,
    val caloriesBurned: Double,
    val description: String? = null,
    val date: String
)
