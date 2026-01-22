package com.chonkcheck.android.data.auth

import android.app.Activity
import android.content.Context
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import com.chonkcheck.android.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenStorage: TokenStorage
) {
    private val auth0: Auth0 by lazy {
        Auth0.getInstance(
            BuildConfig.AUTH0_CLIENT_ID,
            BuildConfig.AUTH0_DOMAIN
        )
    }

    private val authenticationClient: AuthenticationAPIClient by lazy {
        AuthenticationAPIClient(auth0)
    }

    suspend fun login(activity: Activity): Credentials = suspendCancellableCoroutine { continuation ->
        WebAuthProvider.login(auth0)
            .withScheme("com.chonkcheck.android")
            .withScope("openid profile email offline_access")
            .withAudience(BuildConfig.AUTH0_AUDIENCE)
            .start(activity, object : Callback<Credentials, AuthenticationException> {
                override fun onSuccess(result: Credentials) {
                    saveCredentials(result)
                    continuation.resume(result)
                }

                override fun onFailure(error: AuthenticationException) {
                    continuation.resumeWithException(error)
                }
            })
    }

    suspend fun logout(activity: Activity) = suspendCancellableCoroutine { continuation ->
        WebAuthProvider.logout(auth0)
            .withScheme("com.chonkcheck.android")
            .start(activity, object : Callback<Void?, AuthenticationException> {
                override fun onSuccess(result: Void?) {
                    clearCredentials()
                    continuation.resume(Unit)
                }

                override fun onFailure(error: AuthenticationException) {
                    clearCredentials()
                    continuation.resume(Unit)
                }
            })
    }

    suspend fun refreshToken(): String {
        val refreshToken = tokenStorage.getRefreshToken()
            ?: throw IllegalStateException("No refresh token available")

        return suspendCancellableCoroutine { continuation ->
            authenticationClient
                .renewAuth(refreshToken)
                .start(object : Callback<Credentials, AuthenticationException> {
                    override fun onSuccess(result: Credentials) {
                        saveCredentials(result)
                        continuation.resume(result.accessToken)
                    }

                    override fun onFailure(error: AuthenticationException) {
                        continuation.resumeWithException(error)
                    }
                })
        }
    }

    suspend fun getUserProfile(): UserProfile {
        val accessToken = tokenStorage.getAccessToken()
            ?: throw IllegalStateException("No access token available")

        return suspendCancellableCoroutine { continuation ->
            authenticationClient
                .userInfo(accessToken)
                .start(object : Callback<UserProfile, AuthenticationException> {
                    override fun onSuccess(result: UserProfile) {
                        continuation.resume(result)
                    }

                    override fun onFailure(error: AuthenticationException) {
                        continuation.resumeWithException(error)
                    }
                })
        }
    }

    private fun saveCredentials(credentials: Credentials) {
        tokenStorage.saveAccessToken(credentials.accessToken)
        credentials.refreshToken?.let { tokenStorage.saveRefreshToken(it) }
        credentials.idToken?.let { tokenStorage.saveIdToken(it) }
    }

    private fun clearCredentials() {
        tokenStorage.clearTokens()
    }

    fun hasValidCredentials(): Boolean = tokenStorage.hasValidToken()

    fun getAccessToken(): String? = tokenStorage.getAccessToken()
}
