package com.chonkcheck.android.domain.repository

import com.chonkcheck.android.domain.model.ActivityLevel
import com.chonkcheck.android.domain.model.DietPreset
import com.chonkcheck.android.domain.model.HeightUnit
import com.chonkcheck.android.domain.model.Sex
import com.chonkcheck.android.domain.model.ThemePreference
import com.chonkcheck.android.domain.model.User
import com.chonkcheck.android.domain.model.WeightGoal
import com.chonkcheck.android.domain.model.WeightUnit
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    val themePreference: Flow<ThemePreference>
    val weightUnit: Flow<WeightUnit?>
    val heightUnit: Flow<HeightUnit?>

    suspend fun setThemePreference(preference: ThemePreference)
    suspend fun setWeightUnit(unit: WeightUnit)
    suspend fun setHeightUnit(unit: HeightUnit)

    suspend fun updateUserProfile(
        weightUnit: WeightUnit,
        heightUnit: HeightUnit,
        heightCm: Double?,
        birthDate: String?,
        sex: Sex?,
        activityLevel: ActivityLevel?
    ): Result<User>

    suspend fun updateUserGoals(
        weightGoal: WeightGoal?,
        targetWeight: Double?,
        weeklyGoal: Double?,
        dailyCalorieTarget: Int,
        proteinTarget: Int,
        carbsTarget: Int,
        fatTarget: Int,
        bmr: Int?,
        tdee: Int?
    ): Result<User>

    suspend fun exportUserData(): Result<String>

    suspend fun deleteAccount(): Result<Unit>
}
