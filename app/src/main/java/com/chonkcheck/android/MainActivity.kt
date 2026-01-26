package com.chonkcheck.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chonkcheck.android.presentation.AppStartupState
import com.chonkcheck.android.presentation.MainViewModel
import com.chonkcheck.android.presentation.navigation.ChonkCheckNavHost
import com.chonkcheck.android.presentation.navigation.Screen
import com.chonkcheck.android.presentation.ui.components.LoadingIndicator
import com.chonkcheck.android.presentation.ui.milestones.MilestoneModal
import com.chonkcheck.android.presentation.viewmodel.MilestoneViewModel
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChonkCheckApp()
        }
    }
}

@Composable
private fun ChonkCheckApp(
    viewModel: MainViewModel = hiltViewModel(),
    milestoneViewModel: MilestoneViewModel = hiltViewModel()
) {
    val startupState by viewModel.startupState.collectAsStateWithLifecycle()
    val themePreference by viewModel.themePreference.collectAsStateWithLifecycle()
    val pendingMilestone by milestoneViewModel.pendingMilestone.collectAsStateWithLifecycle()

    ChonkCheckTheme(themePreference = themePreference) {
        // Show milestone modal if there's a pending milestone
        pendingMilestone?.let { milestone ->
            MilestoneModal(
                milestone = milestone,
                onDismiss = { milestoneViewModel.dismissMilestone() }
            )
        }

        when (startupState) {
            is AppStartupState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
            is AppStartupState.RequiresLogin -> {
                ChonkCheckNavHost(startDestination = Screen.Login.route)
            }
            is AppStartupState.RequiresOnboarding -> {
                ChonkCheckNavHost(startDestination = Screen.Onboarding.route)
            }
            is AppStartupState.Ready -> {
                ChonkCheckNavHost(startDestination = Screen.Dashboard.route)
            }
        }
    }
}
