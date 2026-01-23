package com.chonkcheck.android.presentation.ui.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chonkcheck.android.domain.model.ActivityLevel
import com.chonkcheck.android.domain.model.DietPreset
import com.chonkcheck.android.domain.model.HeightUnit
import com.chonkcheck.android.domain.model.MacroTargets
import com.chonkcheck.android.domain.model.Sex
import com.chonkcheck.android.domain.model.ThemePreference
import com.chonkcheck.android.domain.model.User
import com.chonkcheck.android.domain.model.WeightGoal
import com.chonkcheck.android.domain.model.WeightUnit
import com.chonkcheck.android.domain.repository.AuthRepository
import com.chonkcheck.android.domain.repository.SettingsRepository
import com.chonkcheck.android.domain.usecase.CalculateTdeeUseCase
import com.chonkcheck.android.domain.usecase.TdeeResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class SettingsUiState(
    val user: User? = null,
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoggedOut: Boolean = false,
    val isDeleted: Boolean = false,

    // Form state for editing
    val editWeightUnit: WeightUnit = WeightUnit.KG,
    val editHeightUnit: HeightUnit = HeightUnit.CM,
    val editHeightCm: Double? = null,
    val editBirthDate: LocalDate? = null,
    val editSex: Sex? = null,
    val editActivityLevel: ActivityLevel? = null,
    val editCurrentWeightKg: Double? = null,
    val editWeightGoal: WeightGoal? = null,
    val editTargetWeightKg: Double? = null,
    val editWeeklyGoalKg: Double? = null,
    val editDietPreset: DietPreset = DietPreset.BALANCED,
    val editCalories: Int = 2000,
    val editProtein: Int = 150,
    val editCarbs: Int = 200,
    val editFat: Int = 67,

    // Calculated values
    val tdeePreview: TdeeResult? = null,
    val macroPreview: MacroTargets? = null,

    // Section expanded states
    val accountExpanded: Boolean = true,
    val preferencesExpanded: Boolean = false,
    val bodyGoalsExpanded: Boolean = false,
    val privacyExpanded: Boolean = false,
    val dangerZoneExpanded: Boolean = false
) {
    val age: Int?
        get() = editBirthDate?.let { Period.between(it, LocalDate.now()).years }

    val hasProfileChanges: Boolean
        get() {
            val userProfile = user?.profile
            val userGoals = user?.goals
            return user != null && (
                editWeightUnit != user.unitPreferences.weightUnit ||
                editHeightUnit != user.unitPreferences.heightUnit ||
                editHeightCm != userProfile?.height ||
                editBirthDate?.toString() != userProfile?.birthDate ||
                editSex != userProfile?.sex ||
                editActivityLevel != userProfile?.activityLevel ||
                editWeightGoal != userGoals?.weightGoal ||
                editTargetWeightKg != userGoals?.targetWeight ||
                editWeeklyGoalKg != userGoals?.weeklyGoal ||
                editCalories != userGoals?.dailyCalorieTarget ||
                editProtein != userGoals?.proteinTarget ||
                editCarbs != userGoals?.carbsTarget ||
                editFat != userGoals?.fatTarget
            )
        }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val calculateTdeeUseCase: CalculateTdeeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadUserAndTheme()
    }

    private fun loadUserAndTheme() {
        combine(
            authRepository.currentUser,
            settingsRepository.themePreference,
            settingsRepository.weightUnit,
            settingsRepository.heightUnit
        ) { user, theme, weightUnit, heightUnit ->
            SettingsPreferences(user, theme, weightUnit, heightUnit)
        }
            .onEach { prefs ->
                if (prefs.user != null) {
                    initializeFormFromUser(prefs.user, prefs.theme, prefs.weightUnit, prefs.heightUnit)
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
            .catch { e ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message)
                }
            }
            .launchIn(viewModelScope)
    }

    private data class SettingsPreferences(
        val user: User?,
        val theme: ThemePreference,
        val weightUnit: WeightUnit?,
        val heightUnit: HeightUnit?
    )

    private fun initializeFormFromUser(
        user: User,
        theme: ThemePreference,
        savedWeightUnit: WeightUnit?,
        savedHeightUnit: HeightUnit?
    ) {
        val profile = user.profile
        val goals = user.goals
        val birthDate = profile?.birthDate?.let {
            try {
                LocalDate.parse(it)
            } catch (e: Exception) {
                null
            }
        }

        _uiState.update {
            it.copy(
                user = user,
                themePreference = theme,
                isLoading = false,
                // Use locally saved unit preference if available, otherwise fall back to user profile
                editWeightUnit = savedWeightUnit ?: user.unitPreferences.weightUnit,
                editHeightUnit = savedHeightUnit ?: user.unitPreferences.heightUnit,
                editHeightCm = profile?.height,
                editBirthDate = birthDate,
                editSex = profile?.sex,
                editActivityLevel = profile?.activityLevel,
                editWeightGoal = goals?.weightGoal,
                editTargetWeightKg = goals?.targetWeight,
                editWeeklyGoalKg = goals?.weeklyGoal,
                editCalories = goals?.dailyCalorieTarget ?: 2000,
                editProtein = goals?.proteinTarget ?: 150,
                editCarbs = goals?.carbsTarget ?: 200,
                editFat = goals?.fatTarget ?: 67,
                tdeePreview = if (goals?.bmr != null && goals.tdee != null) {
                    TdeeResult(bmr = goals.bmr, tdee = goals.tdee, maintenanceCalories = goals.tdee)
                } else null
            )
        }

        recalculateTdeePreview()
    }

    // Section toggle functions
    fun toggleAccountSection() {
        _uiState.update { it.copy(accountExpanded = !it.accountExpanded) }
    }

    fun togglePreferencesSection() {
        _uiState.update { it.copy(preferencesExpanded = !it.preferencesExpanded) }
    }

    fun toggleBodyGoalsSection() {
        _uiState.update { it.copy(bodyGoalsExpanded = !it.bodyGoalsExpanded) }
    }

    fun togglePrivacySection() {
        _uiState.update { it.copy(privacyExpanded = !it.privacyExpanded) }
    }

    fun toggleDangerZoneSection() {
        _uiState.update { it.copy(dangerZoneExpanded = !it.dangerZoneExpanded) }
    }

    // Preferences updates
    fun updateWeightUnit(unit: WeightUnit) {
        viewModelScope.launch {
            settingsRepository.setWeightUnit(unit)
            _uiState.update { it.copy(editWeightUnit = unit) }
        }
    }

    fun updateHeightUnit(unit: HeightUnit) {
        viewModelScope.launch {
            settingsRepository.setHeightUnit(unit)
            _uiState.update { it.copy(editHeightUnit = unit) }
        }
    }

    fun updateTheme(preference: ThemePreference) {
        viewModelScope.launch {
            settingsRepository.setThemePreference(preference)
            _uiState.update { it.copy(themePreference = preference) }
        }
    }

    // Profile updates
    fun updateHeight(heightCm: Double?) {
        _uiState.update { it.copy(editHeightCm = heightCm) }
        recalculateTdeePreview()
    }

    fun updateBirthDate(date: LocalDate?) {
        _uiState.update { it.copy(editBirthDate = date) }
        recalculateTdeePreview()
    }

    fun updateSex(sex: Sex?) {
        _uiState.update { it.copy(editSex = sex) }
        recalculateTdeePreview()
    }

    fun updateActivityLevel(level: ActivityLevel?) {
        _uiState.update { it.copy(editActivityLevel = level) }
        recalculateTdeePreview()
    }

    fun updateCurrentWeight(weightKg: Double?) {
        _uiState.update { it.copy(editCurrentWeightKg = weightKg) }
        recalculateTdeePreview()
    }

    // Goals updates
    fun updateWeightGoal(goal: WeightGoal?) {
        _uiState.update { it.copy(editWeightGoal = goal) }
        updateCaloriePreview()
    }

    fun updateTargetWeight(weightKg: Double?) {
        _uiState.update { it.copy(editTargetWeightKg = weightKg) }
    }

    fun updateWeeklyGoal(weeklyGoalKg: Double?) {
        _uiState.update { it.copy(editWeeklyGoalKg = weeklyGoalKg) }
        updateCaloriePreview()
    }

    fun updateDietPreset(preset: DietPreset) {
        _uiState.update { it.copy(editDietPreset = preset) }
        if (preset != DietPreset.CUSTOM) {
            val calories = _uiState.value.editCalories
            val macros = preset.calculateMacros(calories)
            _uiState.update {
                it.copy(
                    editProtein = macros.protein,
                    editCarbs = macros.carbs,
                    editFat = macros.fat,
                    macroPreview = macros
                )
            }
        }
    }

    fun updateCalories(calories: Int) {
        _uiState.update { it.copy(editCalories = calories, editDietPreset = DietPreset.CUSTOM) }
        recalculateMacrosFromPreset()
    }

    fun updateProtein(protein: Int) {
        _uiState.update { it.copy(editProtein = protein, editDietPreset = DietPreset.CUSTOM) }
    }

    fun updateCarbs(carbs: Int) {
        _uiState.update { it.copy(editCarbs = carbs, editDietPreset = DietPreset.CUSTOM) }
    }

    fun updateFat(fat: Int) {
        _uiState.update { it.copy(editFat = fat, editDietPreset = DietPreset.CUSTOM) }
    }

    private fun recalculateTdeePreview() {
        val state = _uiState.value
        val height = state.editHeightCm ?: return
        val weight = state.editCurrentWeightKg ?: return
        val birthDate = state.editBirthDate ?: return
        val sex = state.editSex ?: return
        val activityLevel = state.editActivityLevel ?: return

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
        val weightGoal = state.editWeightGoal

        if (weightGoal == null) {
            _uiState.update { it.copy(editCalories = tdee) }
            recalculateMacrosFromPreset()
            return
        }

        // Weekly goal is always positive (rate), direction determined by weightGoal
        val weeklyGoalRate = state.editWeeklyGoalKg ?: when (weightGoal) {
            WeightGoal.LOSE -> 0.5
            WeightGoal.MAINTAIN -> 0.0
            WeightGoal.GAIN -> 0.25
        }

        // 7700 calories per kg of body weight
        val dailyCalorieChange = (weeklyGoalRate * 7700 / 7).toInt()
        val calories = when (weightGoal) {
            WeightGoal.LOSE -> (tdee - dailyCalorieChange).coerceAtLeast(1200)
            WeightGoal.GAIN -> tdee + dailyCalorieChange
            WeightGoal.MAINTAIN -> tdee
        }

        _uiState.update { it.copy(editCalories = calories) }
        recalculateMacrosFromPreset()
    }

    private fun recalculateMacrosFromPreset() {
        val state = _uiState.value
        if (state.editDietPreset != DietPreset.CUSTOM) {
            val macros = state.editDietPreset.calculateMacros(state.editCalories)
            _uiState.update {
                it.copy(
                    editProtein = macros.protein,
                    editCarbs = macros.carbs,
                    editFat = macros.fat,
                    macroPreview = macros
                )
            }
        }
    }

    fun saveChanges() {
        viewModelScope.launch {
            val state = _uiState.value
            val user = state.user ?: return@launch

            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            // Update profile first
            val profileResult = settingsRepository.updateUserProfile(
                weightUnit = state.editWeightUnit,
                heightUnit = state.editHeightUnit,
                heightCm = state.editHeightCm,
                birthDate = state.editBirthDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                sex = state.editSex,
                activityLevel = state.editActivityLevel
            )

            if (profileResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = profileResult.exceptionOrNull()?.message ?: "Failed to save profile"
                    )
                }
                return@launch
            }

            // Then update goals
            val goalsResult = settingsRepository.updateUserGoals(
                weightGoal = state.editWeightGoal,
                targetWeight = state.editTargetWeightKg,
                weeklyGoal = state.editWeeklyGoalKg,
                dailyCalorieTarget = state.editCalories,
                proteinTarget = state.editProtein,
                carbsTarget = state.editCarbs,
                fatTarget = state.editFat,
                bmr = state.tdeePreview?.bmr,
                tdee = state.tdeePreview?.tdee
            )

            if (goalsResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = goalsResult.exceptionOrNull()?.message ?: "Failed to save goals"
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isSaving = false,
                    successMessage = "Changes saved successfully"
                )
            }
        }
    }

    fun logout(activity: Activity) {
        viewModelScope.launch {
            authRepository.logout(activity)
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }

    fun exportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            settingsRepository.exportUserData()
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Data exported successfully"
                        )
                    }
                    // Data handling (download/share) would be implemented in the UI
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to export data"
                        )
                    }
                }
        }
    }

    fun showDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = true) }
    }

    fun hideDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showDeleteConfirmation = false) }

            settingsRepository.deleteAccount()
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isDeleted = true) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to delete account"
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
