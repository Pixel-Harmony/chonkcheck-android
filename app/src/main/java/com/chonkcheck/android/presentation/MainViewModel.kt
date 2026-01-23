package com.chonkcheck.android.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chonkcheck.android.domain.model.AuthState
import com.chonkcheck.android.domain.model.ThemePreference
import com.chonkcheck.android.domain.repository.AuthRepository
import com.chonkcheck.android.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AppStartupState {
    data object Loading : AppStartupState
    data object RequiresLogin : AppStartupState
    data object RequiresOnboarding : AppStartupState
    data object Ready : AppStartupState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val themePreference: StateFlow<ThemePreference> = settingsRepository.themePreference
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemePreference.SYSTEM
        )

    val startupState: StateFlow<AppStartupState> = authRepository.authState
        .map { authState ->
            when (authState) {
                is AuthState.Loading -> AppStartupState.Loading
                is AuthState.Unauthenticated -> AppStartupState.RequiresLogin
                is AuthState.Error -> AppStartupState.RequiresLogin
                is AuthState.Authenticated -> {
                    if (authState.user.onboardingCompleted) {
                        AppStartupState.Ready
                    } else {
                        AppStartupState.RequiresOnboarding
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppStartupState.Loading
        )

    init {
        loadInitialState()
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            authRepository.loadUserFromCache()
        }
    }
}
