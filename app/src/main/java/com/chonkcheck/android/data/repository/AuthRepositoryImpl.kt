package com.chonkcheck.android.data.repository

import com.chonkcheck.android.data.auth.AuthManager
import com.chonkcheck.android.data.db.dao.UserDao
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

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authManager: AuthManager,
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

    override suspend fun login(): Result<User> {
        return try {
            _authState.value = AuthState.Loading

            val credentials = authManager.login()
            val auth0Profile = authManager.getUserProfile()
            val user = auth0Profile.toDomain()

            userDao.insert(user.toEntity())
            _authState.value = AuthState.Authenticated(user)

            Result.success(user)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Login failed")
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        try {
            authManager.logout()
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

    suspend fun loadUserFromCache() {
        if (!authManager.hasValidCredentials()) {
            _authState.value = AuthState.Unauthenticated
            return
        }

        try {
            val cachedUser = userDao.getCurrentUserOnce()
            if (cachedUser != null) {
                _authState.value = AuthState.Authenticated(cachedUser.toDomain())
            } else {
                val auth0Profile = authManager.getUserProfile()
                val user = auth0Profile.toDomain()
                userDao.insert(user.toEntity())
                _authState.value = AuthState.Authenticated(user)
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Failed to load user")
        }
    }
}
