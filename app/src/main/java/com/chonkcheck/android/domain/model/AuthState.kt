package com.chonkcheck.android.domain.model

sealed interface AuthState {
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data class Authenticated(val user: User) : AuthState
    data class Error(val message: String) : AuthState

    val isAuthenticated: Boolean
        get() = this is Authenticated

    val user: User?
        get() = (this as? Authenticated)?.user
}
