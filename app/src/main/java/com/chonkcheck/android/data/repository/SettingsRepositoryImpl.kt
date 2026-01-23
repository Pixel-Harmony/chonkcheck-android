package com.chonkcheck.android.data.repository

import android.util.Log
import com.chonkcheck.android.data.api.UserApi
import com.chonkcheck.android.data.api.dto.MacroGoalsDto
import com.chonkcheck.android.data.api.dto.UpdateUserProfileRequest
import com.chonkcheck.android.data.db.dao.UserDao
import com.chonkcheck.android.data.mappers.toDomain
import com.chonkcheck.android.data.preferences.ThemeDataStore
import com.chonkcheck.android.data.preferences.UnitsDataStore
import com.chonkcheck.android.domain.model.ActivityLevel
import com.chonkcheck.android.domain.model.DietPreset
import com.chonkcheck.android.domain.model.HeightUnit
import com.chonkcheck.android.domain.model.Sex
import com.chonkcheck.android.domain.model.ThemePreference
import com.chonkcheck.android.domain.model.User
import com.chonkcheck.android.domain.model.WeightGoal
import com.chonkcheck.android.domain.model.WeightUnit
import com.chonkcheck.android.domain.repository.SettingsRepository
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SettingsRepository"

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userApi: UserApi,
    private val themeDataStore: ThemeDataStore,
    private val unitsDataStore: UnitsDataStore
) : SettingsRepository {

    override val themePreference: Flow<ThemePreference> = themeDataStore.themePreference
    override val weightUnit: Flow<WeightUnit?> = unitsDataStore.weightUnit
    override val heightUnit: Flow<HeightUnit?> = unitsDataStore.heightUnit

    override suspend fun setThemePreference(preference: ThemePreference) {
        themeDataStore.setThemePreference(preference)
    }

    override suspend fun setWeightUnit(unit: WeightUnit) {
        unitsDataStore.setWeightUnit(unit)
    }

    override suspend fun setHeightUnit(unit: HeightUnit) {
        unitsDataStore.setHeightUnit(unit)
    }

    override suspend fun updateUserProfile(
        weightUnit: WeightUnit,
        heightUnit: HeightUnit,
        heightCm: Double?,
        birthDate: String?,
        sex: Sex?,
        activityLevel: ActivityLevel?
    ): Result<User> {
        return try {
            val currentUser = userDao.getCurrentUserOnce()
                ?: return Result.failure(Exception("No user found"))

            // Update local database first (offline-first)
            userDao.updateProfile(
                userId = currentUser.id,
                weightUnit = weightUnit.name.lowercase(),
                heightUnit = heightUnit.name.lowercase(),
                height = heightCm,
                birthDate = birthDate,
                sex = sex?.name?.lowercase(),
                activityLevel = activityLevel?.name?.lowercase()
            )

            // Calculate age from birthDate
            val age = birthDate?.let {
                try {
                    val date = LocalDate.parse(it)
                    Period.between(date, LocalDate.now()).years
                } catch (e: Exception) {
                    null
                }
            }

            // Sync to API
            try {
                userApi.updateUserProfile(
                    UpdateUserProfileRequest(
                        weightUnit = weightUnit.toApiValue(),
                        heightUnit = heightUnit.toApiValue(),
                        height = heightCm,
                        age = age,
                        sex = sex?.toApiValue(),
                        activityLevel = activityLevel?.toApiValue()
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync profile to API", e)
                // Continue - local update succeeded
            }

            val updatedUser = userDao.getCurrentUserOnce()
            Result.success(updatedUser!!.toDomain())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update profile", e)
            Sentry.captureException(e)
            Result.failure(e)
        }
    }

    override suspend fun updateUserGoals(
        weightGoal: WeightGoal?,
        targetWeight: Double?,
        weeklyGoal: Double?,
        dailyCalorieTarget: Int,
        proteinTarget: Int,
        carbsTarget: Int,
        fatTarget: Int,
        bmr: Int?,
        tdee: Int?
    ): Result<User> {
        return try {
            val currentUser = userDao.getCurrentUserOnce()
                ?: return Result.failure(Exception("No user found"))

            // Update local database first (offline-first)
            userDao.updateFullGoals(
                userId = currentUser.id,
                weightGoal = weightGoal?.name?.lowercase(),
                targetWeight = targetWeight,
                weeklyGoal = weeklyGoal,
                calories = dailyCalorieTarget,
                protein = proteinTarget,
                carbs = carbsTarget,
                fat = fatTarget,
                bmr = bmr,
                tdee = tdee
            )

            // Sync to API
            try {
                userApi.updateUserProfile(
                    UpdateUserProfileRequest(
                        goals = MacroGoalsDto(
                            calories = dailyCalorieTarget,
                            protein = proteinTarget,
                            carbs = carbsTarget,
                            fat = fatTarget
                        ),
                        weightGoal = weightGoal?.toApiValue(),
                        weightGoalRate = weeklyGoal?.let { kotlin.math.abs(it) }
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync goals to API", e)
                // Continue - local update succeeded
            }

            val updatedUser = userDao.getCurrentUserOnce()
            Result.success(updatedUser!!.toDomain())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update goals", e)
            Sentry.captureException(e)
            Result.failure(e)
        }
    }

    override suspend fun exportUserData(): Result<String> {
        return try {
            val response = userApi.exportUserData()
            val json = Json { prettyPrint = true }
            Result.success(json.encodeToString(response))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export user data", e)
            Sentry.captureException(e)
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            userApi.deleteAccount()
            userDao.deleteAll()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete account", e)
            Sentry.captureException(e)
            Result.failure(e)
        }
    }

    // Extension functions to map Android enums to API values
    private fun WeightUnit.toApiValue(): String = when (this) {
        WeightUnit.KG -> "kg"
        WeightUnit.LB -> "lb"
        WeightUnit.ST -> "st"
    }

    private fun HeightUnit.toApiValue(): String = when (this) {
        HeightUnit.CM -> "cm"
        HeightUnit.FT -> "ft"
    }

    private fun Sex.toApiValue(): String = when (this) {
        Sex.MALE -> "male"
        Sex.FEMALE -> "female"
        Sex.OTHER -> "male" // API only supports male/female, default to male
    }

    private fun ActivityLevel.toApiValue(): String = when (this) {
        ActivityLevel.SEDENTARY -> "sedentary"
        ActivityLevel.LIGHTLY_ACTIVE -> "light"
        ActivityLevel.MODERATELY_ACTIVE -> "moderate"
        ActivityLevel.VERY_ACTIVE -> "active"
        ActivityLevel.EXTRA_ACTIVE -> "very_active"
    }

    private fun WeightGoal.toApiValue(): String = when (this) {
        WeightGoal.LOSE -> "lose"
        WeightGoal.MAINTAIN -> "maintain"
        WeightGoal.GAIN -> "gain"
    }

    private fun DietPreset.toApiValue(): String = when (this) {
        DietPreset.BALANCED -> "balanced"
        DietPreset.LOW_CARB -> "low-carb"
        DietPreset.HIGH_PROTEIN -> "high-protein"
        DietPreset.KETO -> "keto"
        DietPreset.GLP1_FRIENDLY -> "glp1"
        DietPreset.MEDITERRANEAN -> "mediterranean"
        DietPreset.CUSTOM -> "custom"
    }
}
