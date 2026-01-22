package com.chonkcheck.android.presentation.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chonkcheck.android.domain.model.ActivityLevel
import com.chonkcheck.android.domain.model.DietPreset
import com.chonkcheck.android.domain.model.HeightUnit
import com.chonkcheck.android.domain.model.MacroTargets
import com.chonkcheck.android.domain.model.Sex
import com.chonkcheck.android.domain.model.WeightGoal
import com.chonkcheck.android.domain.model.WeightUnit
import com.chonkcheck.android.domain.repository.AuthRepository
import com.chonkcheck.android.domain.usecase.CalculateTdeeUseCase
import com.chonkcheck.android.domain.usecase.CompleteOnboardingUseCase
import com.chonkcheck.android.domain.usecase.OnboardingData
import com.chonkcheck.android.domain.usecase.TdeeResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class OnboardingStep {
    UNITS,
    PROFILE,
    GOALS
}

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.UNITS,

    // Step 1: Units
    val weightUnit: WeightUnit = WeightUnit.KG,
    val heightUnit: HeightUnit = HeightUnit.CM,

    // Step 2: Profile
    val heightCm: Double? = null,
    val currentWeightKg: Double? = null,
    val birthDate: LocalDate? = null,
    val sex: Sex? = null,
    val activityLevel: ActivityLevel? = null,

    // Step 3: Goals
    val weightGoal: WeightGoal? = null,
    val targetWeightKg: Double? = null,
    val weeklyGoalKg: Double? = null,
    val dietPreset: DietPreset = DietPreset.BALANCED,

    // Calculated preview
    val tdeePreview: TdeeResult? = null,
    val caloriePreview: Int? = null,
    val macroTargets: MacroTargets? = null,

    // UI state
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isComplete: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val calculateTdeeUseCase: CalculateTdeeUseCase,
    private val completeOnboardingUseCase: CompleteOnboardingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun updateWeightUnit(unit: WeightUnit) {
        _uiState.update { it.copy(weightUnit = unit) }
    }

    fun updateHeightUnit(unit: HeightUnit) {
        _uiState.update { it.copy(heightUnit = unit) }
    }

    fun updateHeight(heightCm: Double) {
        _uiState.update { it.copy(heightCm = heightCm) }
        recalculateTdeePreview()
    }

    fun updateCurrentWeight(weightKg: Double) {
        _uiState.update { it.copy(currentWeightKg = weightKg) }
        recalculateTdeePreview()
    }

    fun updateBirthDate(date: LocalDate) {
        _uiState.update { it.copy(birthDate = date) }
        recalculateTdeePreview()
    }

    fun updateSex(sex: Sex) {
        _uiState.update { it.copy(sex = sex) }
        recalculateTdeePreview()
    }

    fun updateActivityLevel(level: ActivityLevel) {
        _uiState.update { it.copy(activityLevel = level) }
        recalculateTdeePreview()
    }

    fun updateWeightGoal(goal: WeightGoal) {
        _uiState.update { it.copy(weightGoal = goal) }
        updateCaloriePreview()
    }

    fun updateTargetWeight(weightKg: Double?) {
        _uiState.update { it.copy(targetWeightKg = weightKg) }
    }

    fun updateWeeklyGoal(weeklyGoalKg: Double?) {
        _uiState.update { it.copy(weeklyGoalKg = weeklyGoalKg) }
        updateCaloriePreview()
    }

    fun updateDietPreset(preset: DietPreset) {
        _uiState.update { it.copy(dietPreset = preset) }
        updateMacroTargets()
    }

    private fun recalculateTdeePreview() {
        val state = _uiState.value
        val height = state.heightCm ?: return
        val weight = state.currentWeightKg ?: return
        val birthDate = state.birthDate ?: return
        val sex = state.sex ?: return
        val activityLevel = state.activityLevel ?: return

        val tdeeResult = calculateTdeeUseCase(
            weightKg = weight,
            heightCm = height,
            birthDate = birthDate,
            sex = sex,
            activityLevel = activityLevel
        )

        _uiState.update { it.copy(tdeePreview = tdeeResult) }
        updateCaloriePreview()
    }

    private fun updateCaloriePreview() {
        val state = _uiState.value
        val tdee = state.tdeePreview?.tdee ?: return
        val weightGoal = state.weightGoal ?: return

        val weeklyGoal = state.weeklyGoalKg ?: when (weightGoal) {
            WeightGoal.LOSE -> -0.5
            WeightGoal.MAINTAIN -> 0.0
            WeightGoal.GAIN -> 0.25
        }

        val dailyDeficit = (weeklyGoal * 7700 / 7).toInt()
        val calories = (tdee + dailyDeficit).coerceAtLeast(1200)

        _uiState.update { it.copy(caloriePreview = calories) }
        updateMacroTargets()
    }

    private fun updateMacroTargets() {
        val state = _uiState.value
        val calories = state.caloriePreview ?: return
        val macros = state.dietPreset.calculateMacros(calories)
        _uiState.update { it.copy(macroTargets = macros) }
    }

    fun goToNextStep() {
        _uiState.update { state ->
            val nextStep = when (state.currentStep) {
                OnboardingStep.UNITS -> OnboardingStep.PROFILE
                OnboardingStep.PROFILE -> OnboardingStep.GOALS
                OnboardingStep.GOALS -> OnboardingStep.GOALS
            }
            state.copy(currentStep = nextStep, errorMessage = null)
        }
    }

    fun goToPreviousStep() {
        _uiState.update { state ->
            val previousStep = when (state.currentStep) {
                OnboardingStep.UNITS -> OnboardingStep.UNITS
                OnboardingStep.PROFILE -> OnboardingStep.UNITS
                OnboardingStep.GOALS -> OnboardingStep.PROFILE
            }
            state.copy(currentStep = previousStep, errorMessage = null)
        }
    }

    fun canProceedFromUnits(): Boolean {
        return true
    }

    fun canProceedFromProfile(): Boolean {
        val state = _uiState.value
        return state.heightCm != null &&
            state.currentWeightKg != null &&
            state.birthDate != null &&
            state.sex != null &&
            state.activityLevel != null
    }

    fun canProceedFromGoals(): Boolean {
        val state = _uiState.value
        return state.weightGoal != null
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            val state = _uiState.value

            if (!canProceedFromProfile() || !canProceedFromGoals()) {
                _uiState.update { it.copy(errorMessage = "Please fill in all required fields") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val user = authRepository.currentUser.first()
            if (user == null) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "User not found")
                }
                return@launch
            }

            val data = OnboardingData(
                weightUnit = state.weightUnit,
                heightUnit = state.heightUnit,
                heightCm = state.heightCm!!,
                currentWeightKg = state.currentWeightKg!!,
                birthDate = state.birthDate!!,
                sex = state.sex!!,
                activityLevel = state.activityLevel!!,
                weightGoal = state.weightGoal!!,
                targetWeightKg = state.targetWeightKg,
                weeklyGoalKg = state.weeklyGoalKg
            )

            completeOnboardingUseCase(user.id, data)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isComplete = true) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Failed to save")
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
