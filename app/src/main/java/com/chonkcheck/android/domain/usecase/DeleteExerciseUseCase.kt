package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.repository.ExerciseRepository
import javax.inject.Inject

class DeleteExerciseUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return exerciseRepository.deleteExercise(id)
    }
}
