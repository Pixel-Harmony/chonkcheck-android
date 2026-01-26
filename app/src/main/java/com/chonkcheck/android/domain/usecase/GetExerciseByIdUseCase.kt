package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.Exercise
import com.chonkcheck.android.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExerciseByIdUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) {
    operator fun invoke(id: String): Flow<Exercise?> {
        return exerciseRepository.getExerciseById(id)
    }
}
