package com.chonkcheck.android.domain.repository

import android.app.Activity
import com.chonkcheck.android.domain.model.AuthState
import com.chonkcheck.android.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val authState: Flow<AuthState>

    val currentUser: Flow<User?>

    suspend fun login(activity: Activity): Result<User>

    suspend fun logout(activity: Activity)

    suspend fun refreshToken(): Result<String>

    suspend fun loadUserFromCache()

    fun isAuthenticated(): Boolean

    fun getAccessToken(): String?
}
