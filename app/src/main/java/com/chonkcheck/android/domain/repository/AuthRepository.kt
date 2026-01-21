package com.chonkcheck.android.domain.repository

import com.chonkcheck.android.domain.model.AuthState
import com.chonkcheck.android.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val authState: Flow<AuthState>

    val currentUser: Flow<User?>

    suspend fun login(): Result<User>

    suspend fun logout()

    suspend fun refreshToken(): Result<String>

    fun isAuthenticated(): Boolean

    fun getAccessToken(): String?
}
