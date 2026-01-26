package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.Exercise
import com.chonkcheck.android.domain.model.UpdateExerciseParams
import com.chonkcheck.android.domain.repository.ExerciseRepository
import javax.inject.Inject

class UpdateExerciseUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) {
    suspend operator fun invoke(id: String, params: UpdateExerciseParams): Result<Exercise> {
        return exerciseRepository.updateExercise(id, params)
    }
}
