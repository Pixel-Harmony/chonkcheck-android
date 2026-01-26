package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.CreateExerciseParams
import com.chonkcheck.android.domain.model.Exercise
import com.chonkcheck.android.domain.repository.ExerciseRepository
import javax.inject.Inject

class CreateExerciseUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) {
    suspend operator fun invoke(params: CreateExerciseParams): Result<Exercise> {
        return exerciseRepository.createExercise(params)
    }
}
