package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.Exercise
import com.chonkcheck.android.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetExercisesForDateUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) {
    operator fun invoke(date: LocalDate): Flow<List<Exercise>> {
        return exerciseRepository.getExercisesForDate(date)
    }
}
