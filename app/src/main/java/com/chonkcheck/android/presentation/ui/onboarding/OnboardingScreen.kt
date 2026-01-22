package com.chonkcheck.android.presentation.ui.onboarding

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chonkcheck.android.domain.model.ActivityLevel
import com.chonkcheck.android.domain.model.DietPreset
import com.chonkcheck.android.domain.model.HeightUnit
import com.chonkcheck.android.domain.model.Sex
import com.chonkcheck.android.domain.model.WeightGoal
import com.chonkcheck.android.domain.model.WeightUnit
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import java.time.LocalDate

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onOnboardingComplete()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    OnboardingContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onWeightUnitChange = viewModel::updateWeightUnit,
        onHeightUnitChange = viewModel::updateHeightUnit,
        onHeightChange = viewModel::updateHeight,
        onWeightChange = viewModel::updateCurrentWeight,
        onBirthDateChange = viewModel::updateBirthDate,
        onSexChange = viewModel::updateSex,
        onActivityLevelChange = viewModel::updateActivityLevel,
        onWeightGoalChange = viewModel::updateWeightGoal,
        onWeeklyGoalChange = viewModel::updateWeeklyGoal,
        onDietPresetChange = viewModel::updateDietPreset,
        onNextStep = viewModel::goToNextStep,
        onPreviousStep = viewModel::goToPreviousStep,
        onComplete = viewModel::completeOnboarding,
        canProceedFromProfile = viewModel.canProceedFromProfile(),
        canProceedFromGoals = viewModel.canProceedFromGoals()
    )
}

@Composable
private fun OnboardingContent(
    uiState: OnboardingUiState,
    snackbarHostState: SnackbarHostState,
    onWeightUnitChange: (WeightUnit) -> Unit,
    onHeightUnitChange: (HeightUnit) -> Unit,
    onHeightChange: (Double) -> Unit,
    onWeightChange: (Double) -> Unit,
    onBirthDateChange: (LocalDate) -> Unit,
    onSexChange: (Sex) -> Unit,
    onActivityLevelChange: (ActivityLevel) -> Unit,
    onWeightGoalChange: (WeightGoal) -> Unit,
    onWeeklyGoalChange: (Double) -> Unit,
    onDietPresetChange: (DietPreset) -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onComplete: () -> Unit,
    canProceedFromProfile: Boolean,
    canProceedFromGoals: Boolean
) {
    val progress = when (uiState.currentStep) {
        OnboardingStep.UNITS -> 0.33f
        OnboardingStep.PROFILE -> 0.66f
        OnboardingStep.GOALS -> 1f
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    } else {
                        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                    }
                },
                label = "onboarding_step"
            ) { step ->
                when (step) {
                    OnboardingStep.UNITS -> {
                        UnitsStepScreen(
                            weightUnit = uiState.weightUnit,
                            heightUnit = uiState.heightUnit,
                            onWeightUnitChange = onWeightUnitChange,
                            onHeightUnitChange = onHeightUnitChange,
                            onContinue = onNextStep
                        )
                    }

                    OnboardingStep.PROFILE -> {
                        ProfileStepScreen(
                            weightUnit = uiState.weightUnit,
                            heightUnit = uiState.heightUnit,
                            heightCm = uiState.heightCm,
                            currentWeightKg = uiState.currentWeightKg,
                            birthDate = uiState.birthDate,
                            sex = uiState.sex,
                            activityLevel = uiState.activityLevel,
                            onHeightChange = onHeightChange,
                            onWeightChange = onWeightChange,
                            onBirthDateChange = onBirthDateChange,
                            onSexChange = onSexChange,
                            onActivityLevelChange = onActivityLevelChange,
                            onContinue = onNextStep,
                            onBack = onPreviousStep,
                            canContinue = canProceedFromProfile
                        )
                    }

                    OnboardingStep.GOALS -> {
                        GoalsStepScreen(
                            weightUnit = uiState.weightUnit,
                            weightGoal = uiState.weightGoal,
                            weeklyGoalKg = uiState.weeklyGoalKg,
                            tdeePreview = uiState.tdeePreview,
                            caloriePreview = uiState.caloriePreview,
                            dietPreset = uiState.dietPreset,
                            macroTargets = uiState.macroTargets,
                            onWeightGoalChange = onWeightGoalChange,
                            onWeeklyGoalChange = onWeeklyGoalChange,
                            onDietPresetChange = onDietPresetChange,
                            onComplete = onComplete,
                            onBack = onPreviousStep,
                            canComplete = canProceedFromGoals,
                            isLoading = uiState.isLoading
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OnboardingContentPreview() {
    ChonkCheckTheme {
        OnboardingContent(
            uiState = OnboardingUiState(),
            snackbarHostState = remember { SnackbarHostState() },
            onWeightUnitChange = {},
            onHeightUnitChange = {},
            onHeightChange = {},
            onWeightChange = {},
            onBirthDateChange = {},
            onSexChange = {},
            onActivityLevelChange = {},
            onWeightGoalChange = {},
            onWeeklyGoalChange = {},
            onDietPresetChange = {},
            onNextStep = {},
            onPreviousStep = {},
            onComplete = {},
            canProceedFromProfile = false,
            canProceedFromGoals = false
        )
    }
}
