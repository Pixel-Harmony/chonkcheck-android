package com.chonkcheck.android.data.api.interceptor

import com.chonkcheck.android.data.auth.AuthManager
import com.chonkcheck.android.data.auth.TokenStorage
import io.sentry.Sentry
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp Authenticator that handles 401 responses by refreshing the access token.
 *
 * When a 401 is received, this authenticator:
 * 1. Attempts to refresh the token using Auth0
 * 2. Retries the original request with the new token
 * 3. If refresh fails, clears tokens (user will need to re-login)
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val authManager: AuthManager,
    private val tokenStorage: TokenStorage
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Don't retry if we've already retried (prevents infinite loops)
        if (response.request.header("Authorization-Retry") != null) {
            return null
        }

        // Check if we have a refresh token
        if (tokenStorage.getRefreshToken() == null) {
            // No refresh token available, can't refresh
            return null
        }

        synchronized(lock) {
            // Double-check: another thread may have already refreshed the token
            val currentToken = tokenStorage.getAccessToken()
            val requestToken = response.request.header("Authorization")
                ?.removePrefix("Bearer ")

            // If the current token is different from the one used in the failed request,
            // another thread has already refreshed it - retry with the new token
            if (currentToken != null && currentToken != requestToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .header("Authorization-Retry", "true")
                    .build()
            }

            // Attempt to refresh the token
            return try {
                val newToken = runBlocking {
                    authManager.refreshToken()
                }

                // Retry the request with the new token
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .header("Authorization-Retry", "true")
                    .build()
            } catch (e: Exception) {
                Sentry.captureException(e)
                // Token refresh failed - clear tokens so user will be prompted to re-login
                tokenStorage.clearTokens()
                null
            }
        }
    }
}
