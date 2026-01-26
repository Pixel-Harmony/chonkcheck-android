package com.chonkcheck.android.presentation.ui.settings

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.chonkcheck.android.domain.model.ActivityLevel
import com.chonkcheck.android.domain.model.DietPreset
import com.chonkcheck.android.domain.model.HeightUnit
import com.chonkcheck.android.domain.model.Sex
import com.chonkcheck.android.domain.model.ThemePreference
import com.chonkcheck.android.domain.model.WeightGoal
import com.chonkcheck.android.domain.model.WeightUnit
import com.chonkcheck.android.domain.usecase.TdeeResult
import com.chonkcheck.android.presentation.ui.settings.components.AccountSection
import com.chonkcheck.android.presentation.ui.settings.components.BodyGoalsSection
import com.chonkcheck.android.presentation.ui.settings.components.CollapsibleSection
import com.chonkcheck.android.presentation.ui.settings.components.DangerZoneSection
import com.chonkcheck.android.presentation.ui.settings.components.DeleteAccountDialog
import com.chonkcheck.android.presentation.ui.settings.components.PreferencesSection
import com.chonkcheck.android.presentation.ui.settings.components.PrivacyDataSection
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import java.time.LocalDate

@Composable
fun SettingsScreen(
    onLoggedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(uiState.isLoggedOut, uiState.isDeleted) {
        if (uiState.isLoggedOut || uiState.isDeleted) {
            onLoggedOut()
        }
    }

    // Handle exported data - share via intent
    LaunchedEffect(uiState.exportedData) {
        uiState.exportedData?.let { jsonData ->
            try {
                // Create a temp file in cache directory
                val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"))
                val fileName = "ChonkCheck-export-$timestamp.json"
                val file = File(context.cacheDir, fileName)
                file.writeText(jsonData)

                // Get URI via FileProvider
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                // Create share intent
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(Intent.createChooser(shareIntent, "Export Data"))
                viewModel.clearExportedData()
            } catch (e: Exception) {
                // If file sharing fails, fall back to sharing as text
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, jsonData)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Export Data"))
                viewModel.clearExportedData()
            }
        }
    }

    SettingsScreenContent(
        uiState = uiState,
        onToggleAccount = viewModel::toggleAccountSection,
        onTogglePreferences = viewModel::togglePreferencesSection,
        onToggleBodyGoals = viewModel::toggleBodyGoalsSection,
        onTogglePrivacy = viewModel::togglePrivacySection,
        onToggleDangerZone = viewModel::toggleDangerZoneSection,
        onSignOut = { activity?.let { viewModel.logout(it) } },
        onWeightUnitChange = viewModel::updateWeightUnit,
        onHeightUnitChange = viewModel::updateHeightUnit,
        onThemeChange = viewModel::updateTheme,
        onHeightChange = viewModel::updateHeight,
        onBirthDateChange = viewModel::updateBirthDate,
        onSexChange = viewModel::updateSex,
        onActivityLevelChange = viewModel::updateActivityLevel,
        onCurrentWeightChange = viewModel::updateCurrentWeight,
        onWeightGoalChange = viewModel::updateWeightGoal,
        onWeeklyGoalChange = viewModel::updateWeeklyGoal,
        onDietPresetChange = viewModel::updateDietPreset,
        onCaloriesChange = viewModel::updateCalories,
        onProteinChange = viewModel::updateProtein,
        onCarbsChange = viewModel::updateCarbs,
        onFatChange = viewModel::updateFat,
        onSaveChanges = viewModel::saveChanges,
        onExportData = viewModel::exportData,
        onPrivacyPolicy = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://app.chonkcheck.com/privacy"))
            context.startActivity(intent)
        },
        onDeleteAccount = viewModel::showDeleteConfirmation,
        onConfirmDelete = viewModel::deleteAccount,
        onDismissDelete = viewModel::hideDeleteConfirmation,
        onClearMessages = viewModel::clearMessages
    )
}

@Composable
private fun SettingsScreenContent(
    uiState: SettingsUiState,
    onToggleAccount: () -> Unit,
    onTogglePreferences: () -> Unit,
    onToggleBodyGoals: () -> Unit,
    onTogglePrivacy: () -> Unit,
    onToggleDangerZone: () -> Unit,
    onSignOut: () -> Unit,
    onWeightUnitChange: (WeightUnit) -> Unit,
    onHeightUnitChange: (HeightUnit) -> Unit,
    onThemeChange: (ThemePreference) -> Unit,
    onHeightChange: (Double?) -> Unit,
    onBirthDateChange: (LocalDate?) -> Unit,
    onSexChange: (Sex?) -> Unit,
    onActivityLevelChange: (ActivityLevel?) -> Unit,
    onCurrentWeightChange: (Double?) -> Unit,
    onWeightGoalChange: (WeightGoal?) -> Unit,
    onWeeklyGoalChange: (Double?) -> Unit,
    onDietPresetChange: (DietPreset) -> Unit,
    onCaloriesChange: (Int) -> Unit,
    onProteinChange: (Int) -> Unit,
    onCarbsChange: (Int) -> Unit,
    onFatChange: (Int) -> Unit,
    onSaveChanges: () -> Unit,
    onExportData: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onDeleteAccount: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    onClearMessages: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Unable to load settings",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Account Section
                CollapsibleSection(
                    title = "Account",
                    isExpanded = uiState.accountExpanded,
                    onToggle = onToggleAccount
                ) {
                    AccountSection(
                        email = uiState.user.email,
                        onSignOut = onSignOut
                    )
                }

                // Preferences Section
                CollapsibleSection(
                    title = "Preferences",
                    isExpanded = uiState.preferencesExpanded,
                    onToggle = onTogglePreferences
                ) {
                    PreferencesSection(
                        weightUnit = uiState.editWeightUnit,
                        heightUnit = uiState.editHeightUnit,
                        themePreference = uiState.themePreference,
                        onWeightUnitChange = onWeightUnitChange,
                        onHeightUnitChange = onHeightUnitChange,
                        onThemeChange = onThemeChange
                    )
                }

                // Body & Goals Section
                CollapsibleSection(
                    title = "Body & Goals",
                    isExpanded = uiState.bodyGoalsExpanded,
                    onToggle = onToggleBodyGoals
                ) {
                    BodyGoalsSection(
                        weightUnit = uiState.editWeightUnit,
                        heightUnit = uiState.editHeightUnit,
                        heightCm = uiState.editHeightCm,
                        age = uiState.age,
                        birthDate = uiState.editBirthDate,
                        sex = uiState.editSex,
                        activityLevel = uiState.editActivityLevel,
                        currentWeightKg = uiState.editCurrentWeightKg,
                        tdeePreview = uiState.tdeePreview,
                        weightGoal = uiState.editWeightGoal,
                        weeklyGoalKg = uiState.editWeeklyGoalKg,
                        dietPreset = uiState.editDietPreset,
                        calories = uiState.editCalories,
                        protein = uiState.editProtein,
                        carbs = uiState.editCarbs,
                        fat = uiState.editFat,
                        onHeightChange = onHeightChange,
                        onBirthDateChange = onBirthDateChange,
                        onSexChange = onSexChange,
                        onActivityLevelChange = onActivityLevelChange,
                        onCurrentWeightChange = onCurrentWeightChange,
                        onWeightGoalChange = onWeightGoalChange,
                        onWeeklyGoalChange = onWeeklyGoalChange,
                        onDietPresetChange = onDietPresetChange,
                        onCaloriesChange = onCaloriesChange,
                        onProteinChange = onProteinChange,
                        onCarbsChange = onCarbsChange,
                        onFatChange = onFatChange,
                        onSaveChanges = onSaveChanges,
                        isSaving = uiState.isSaving,
                        hasChanges = uiState.hasProfileChanges
                    )
                }

                // Privacy & Data Section
                CollapsibleSection(
                    title = "Privacy & Data",
                    isExpanded = uiState.privacyExpanded,
                    onToggle = onTogglePrivacy
                ) {
                    PrivacyDataSection(
                        onExportData = onExportData,
                        onPrivacyPolicy = onPrivacyPolicy,
                        isExporting = uiState.isExporting
                    )
                }

                // Danger Zone Section
                CollapsibleSection(
                    title = "Danger Zone",
                    isExpanded = uiState.dangerZoneExpanded,
                    onToggle = onToggleDangerZone,
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ) {
                    DangerZoneSection(
                        onDeleteAccount = onDeleteAccount
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Delete Account Dialog
        if (uiState.showDeleteConfirmation) {
            DeleteAccountDialog(
                onConfirm = onConfirmDelete,
                onDismiss = onDismissDelete
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenPreview() {
    ChonkCheckTheme {
        SettingsScreenContent(
            uiState = SettingsUiState(
                user = com.chonkcheck.android.domain.model.User(
                    id = "1",
                    email = "user@example.com",
                    name = "Test User",
                    avatarUrl = null,
                    unitPreferences = com.chonkcheck.android.domain.model.UnitPreferences(
                        weightUnit = WeightUnit.KG,
                        heightUnit = HeightUnit.CM
                    ),
                    profile = com.chonkcheck.android.domain.model.UserProfile(
                        height = 175.0,
                        birthDate = "1990-05-15",
                        sex = Sex.MALE,
                        activityLevel = ActivityLevel.MODERATELY_ACTIVE
                    ),
                    goals = com.chonkcheck.android.domain.model.DailyGoals(
                        weightGoal = WeightGoal.LOSE,
                        targetWeight = 75.0,
                        weeklyGoal = 0.5,
                        dailyCalorieTarget = 1900,
                        proteinTarget = 143,
                        carbsTarget = 190,
                        fatTarget = 63,
                        bmr = 1800,
                        tdee = 2400
                    ),
                    onboardingCompleted = true
                ),
                isLoading = false,
                accountExpanded = true,
                preferencesExpanded = false,
                bodyGoalsExpanded = false,
                privacyExpanded = false,
                dangerZoneExpanded = false,
                editWeightUnit = WeightUnit.KG,
                editHeightUnit = HeightUnit.CM,
                editHeightCm = 175.0,
                editBirthDate = LocalDate.of(1990, 5, 15),
                editSex = Sex.MALE,
                editActivityLevel = ActivityLevel.MODERATELY_ACTIVE,
                editWeightGoal = WeightGoal.LOSE,
                editWeeklyGoalKg = 0.5,
                editDietPreset = DietPreset.BALANCED,
                editCalories = 1900,
                editProtein = 143,
                editCarbs = 190,
                editFat = 63,
                tdeePreview = TdeeResult(bmr = 1800, tdee = 2400, maintenanceCalories = 2400)
            ),
            onToggleAccount = {},
            onTogglePreferences = {},
            onToggleBodyGoals = {},
            onTogglePrivacy = {},
            onToggleDangerZone = {},
            onSignOut = {},
            onWeightUnitChange = {},
            onHeightUnitChange = {},
            onThemeChange = {},
            onHeightChange = {},
            onBirthDateChange = {},
            onSexChange = {},
            onActivityLevelChange = {},
            onCurrentWeightChange = {},
            onWeightGoalChange = {},
            onWeeklyGoalChange = {},
            onDietPresetChange = {},
            onCaloriesChange = {},
            onProteinChange = {},
            onCarbsChange = {},
            onFatChange = {},
            onSaveChanges = {},
            onExportData = {},
            onPrivacyPolicy = {},
            onDeleteAccount = {},
            onConfirmDelete = {},
            onDismissDelete = {},
            onClearMessages = {}
        )
    }
}
