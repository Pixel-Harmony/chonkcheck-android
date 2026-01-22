package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.DailyGoals
import com.chonkcheck.android.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetUserGoalsUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<DailyGoals?> {
        return authRepository.currentUser.map { user ->
            user?.goals
        }
    }
}
