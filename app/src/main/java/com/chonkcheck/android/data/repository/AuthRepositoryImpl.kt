package com.chonkcheck.android.data.repository

import android.app.Activity
import android.util.Log
import com.chonkcheck.android.data.api.UserApi
import com.chonkcheck.android.data.auth.AuthManager
import com.chonkcheck.android.data.db.dao.UserDao
import com.chonkcheck.android.data.mappers.mergeWithApiProfile
import com.chonkcheck.android.data.mappers.toDomain
import com.chonkcheck.android.data.mappers.toEntity
import com.chonkcheck.android.domain.model.AuthState
import com.chonkcheck.android.domain.model.User
import com.chonkcheck.android.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AuthRepository"

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authManager: AuthManager,
    private val userApi: UserApi,
    private val userDao: UserDao
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)

    override val authState: Flow<AuthState> = _authState.asStateFlow()

    override val currentUser: Flow<User?> = userDao.getCurrentUser().map { it?.toDomain() }

    init {
        checkInitialAuthState()
    }

    private fun checkInitialAuthState() {
        _authState.value = if (authManager.hasValidCredentials()) {
            AuthState.Loading
        } else {
            AuthState.Unauthenticated
        }
    }

    override suspend fun login(activity: Activity): Result<User> {
        return try {
            Log.d(TAG, "Login started")
            _authState.value = AuthState.Loading

            val credentials = authManager.login(activity)
            Log.d(TAG, "Credentials received: ${credentials.accessToken.take(20)}...")

            val userProfile = credentials.user
            Log.d(TAG, "User profile: id=${userProfile.getId()}, email=${userProfile.email}, name=${userProfile.name}")

            val user = userProfile.toDomain()
            Log.d(TAG, "Domain user: id=${user.id}, email=${user.email}")

            userDao.insert(user.toEntity())
            Log.d(TAG, "User inserted to DB")

            _authState.value = AuthState.Authenticated(user)
            Log.d(TAG, "Auth state set to Authenticated")

            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            _authState.value = AuthState.Error(e.message ?: "Login failed")
            Result.failure(e)
        }
    }

    override suspend fun logout(activity: Activity) {
        try {
            authManager.logout(activity)
            userDao.deleteAll()
            _authState.value = AuthState.Unauthenticated
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    override suspend fun refreshToken(): Result<String> {
        return try {
            val token = authManager.refreshToken()
            Result.success(token)
        } catch (e: Exception) {
            _authState.value = AuthState.Unauthenticated
            Result.failure(e)
        }
    }

    override fun isAuthenticated(): Boolean = authManager.hasValidCredentials()

    override fun getAccessToken(): String? = authManager.getAccessToken()

    override suspend fun loadUserFromCache() {
        if (!authManager.hasValidCredentials()) {
            _authState.value = AuthState.Unauthenticated
            return
        }

        try {
            val cachedUser = userDao.getCurrentUserOnce()
            if (cachedUser != null) {
                // Show cached state immediately for fast startup
                _authState.value = AuthState.Authenticated(cachedUser.toDomain())

                // Sync with API in background to get latest onboarding status
                try {
                    val apiProfile = userApi.getUserProfile()
                    val updatedEntity = cachedUser.mergeWithApiProfile(apiProfile)
                    userDao.update(updatedEntity)

                    // Re-emit if onboarding status changed (important for reinstall scenario)
                    if (updatedEntity.onboardingCompleted != cachedUser.onboardingCompleted) {
                        Log.d(TAG, "Onboarding status updated from API: ${updatedEntity.onboardingCompleted}")
                        _authState.value = AuthState.Authenticated(updatedEntity.toDomain())
                    }
                } catch (e: Exception) {
                    // Network failure is acceptable - we have cached data
                    Log.w(TAG, "Failed to sync user profile from API", e)
                }
            } else {
                // No cached user but have credentials - require re-login to get fresh user info
                _authState.value = AuthState.Unauthenticated
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Failed to load user")
        }
    }
}
