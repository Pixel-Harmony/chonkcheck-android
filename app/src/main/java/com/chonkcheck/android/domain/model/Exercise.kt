package com.chonkcheck.android.domain.model

import java.time.LocalDate

data class Exercise(
    val id: String,
    val userId: String,
    val date: LocalDate,
    val name: String,
    val caloriesBurned: Double,
    val description: String?,
    val createdAt: Long,
    val updatedAt: Long
)

data class CreateExerciseParams(
    val date: LocalDate,
    val name: String,
    val caloriesBurned: Double,
    val description: String?
)

data class UpdateExerciseParams(
    val name: String,
    val caloriesBurned: Double,
    val description: String?,
    val date: LocalDate
)
