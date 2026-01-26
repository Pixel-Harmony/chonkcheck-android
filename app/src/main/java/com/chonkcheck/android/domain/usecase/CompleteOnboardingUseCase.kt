package com.chonkcheck.android.domain.usecase

import android.util.Log
import com.chonkcheck.android.data.api.UserApi
import com.chonkcheck.android.data.api.dto.CompleteOnboardingRequest
import com.chonkcheck.android.data.db.dao.UserDao
import com.chonkcheck.android.domain.model.ActivityLevel
import com.chonkcheck.android.domain.model.CreateWeightParams
import com.chonkcheck.android.domain.model.HeightUnit
import com.chonkcheck.android.domain.model.Sex
import com.chonkcheck.android.domain.model.WeightGoal
import com.chonkcheck.android.domain.model.WeightUnit
import com.chonkcheck.android.domain.repository.WeightRepository
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject

private const val TAG = "CompleteOnboarding"

data class OnboardingData(
    val weightUnit: WeightUnit,
    val heightUnit: HeightUnit,
    val heightCm: Double,
    val currentWeightKg: Double,
    val birthDate: LocalDate,
    val sex: Sex,
    val activityLevel: ActivityLevel,
    val weightGoal: WeightGoal,
    val targetWeightKg: Double?,
    val weeklyGoalKg: Double?
)

class CompleteOnboardingUseCase @Inject constructor(
    private val userApi: UserApi,
    private val userDao: UserDao,
    private val calculateTdeeUseCase: CalculateTdeeUseCase,
    private val weightRepository: WeightRepository
) {

    suspend operator fun invoke(userId: String, data: OnboardingData): Result<Unit> {
        return try {
            // Calculate age from birthDate
            val age = Period.between(data.birthDate, LocalDate.now()).years

            // Build API request
            val request = CompleteOnboardingRequest(
                weightUnit = data.weightUnit.name.lowercase(),
                heightUnit = data.heightUnit.name.lowercase(),
                height = data.heightCm,
                age = age,
                sex = data.sex.name.lowercase(),
                activityLevel = data.activityLevel.toApiValue(),
                startingWeight = data.currentWeightKg,
                goal = data.weightGoal.name.lowercase(),
                weightGoalRate = data.weeklyGoalKg ?: getDefaultWeeklyGoal(data.weightGoal)
            )

            // Try API call first
            val (tdee, dailyCalories, macros) = try {
                val response = userApi.completeOnboarding(request)
                Log.d(TAG, "Onboarding completed via API: tdee=${response.tdee}")
                Triple(
                    response.tdee,
                    response.goals.calories,
                    Macros(
                        protein = response.goals.protein,
                        carbs = response.goals.carbs,
                        fat = response.goals.fat
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "API call failed, falling back to local calculation", e)
                // Fall back to local calculation for offline support
                val tdeeResult = calculateTdeeUseCase(
                    weightKg = data.currentWeightKg,
                    heightCm = data.heightCm,
                    birthDate = data.birthDate,
                    sex = data.sex,
                    activityLevel = data.activityLevel
                )
                val calories = calculateDailyCalories(
                    tdee = tdeeResult.tdee,
                    weightGoal = data.weightGoal,
                    weeklyGoalKg = data.weeklyGoalKg
                )
                val calculatedMacros = calculateMacros(
                    calories = calories,
                    weightKg = data.currentWeightKg,
                    weightGoal = data.weightGoal
                )
                Triple(tdeeResult.tdee, calories, calculatedMacros)
            }

            // Calculate BMR locally (not returned by API)
            val tdeeResult = calculateTdeeUseCase(
                weightKg = data.currentWeightKg,
                heightCm = data.heightCm,
                birthDate = data.birthDate,
                sex = data.sex,
                activityLevel = data.activityLevel
            )

            userDao.completeOnboarding(
                userId = userId,
                weightUnit = data.weightUnit.name.lowercase(),
                heightUnit = data.heightUnit.name.lowercase(),
                height = data.heightCm,
                birthDate = data.birthDate.toString(),
                sex = data.sex.name.lowercase(),
                activityLevel = data.activityLevel.name.lowercase(),
                weightGoal = data.weightGoal.name.lowercase(),
                targetWeight = data.targetWeightKg,
                weeklyGoal = data.weeklyGoalKg,
                calories = dailyCalories,
                protein = macros.protein,
                carbs = macros.carbs,
                fat = macros.fat,
                bmr = tdeeResult.bmr,
                tdee = tdee
            )

            // Create initial weight entry from onboarding weight
            weightRepository.createEntry(
                CreateWeightParams(
                    weight = data.currentWeightKg,
                    date = LocalDate.now(),
                    notes = null
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to complete onboarding", e)
            Result.failure(e)
        }
    }

    private fun getDefaultWeeklyGoal(weightGoal: WeightGoal): Double {
        return when (weightGoal) {
            WeightGoal.LOSE -> 0.5
            WeightGoal.MAINTAIN -> 0.0
            WeightGoal.GAIN -> 0.25
        }
    }

    private fun ActivityLevel.toApiValue(): String {
        return when (this) {
            ActivityLevel.SEDENTARY -> "sedentary"
            ActivityLevel.LIGHTLY_ACTIVE -> "light"
            ActivityLevel.MODERATELY_ACTIVE -> "moderate"
            ActivityLevel.VERY_ACTIVE -> "active"
            ActivityLevel.EXTRA_ACTIVE -> "very_active"
        }
    }

    private fun calculateDailyCalories(
        tdee: Int,
        weightGoal: WeightGoal,
        weeklyGoalKg: Double?
    ): Int {
        val weeklyGoal = weeklyGoalKg ?: when (weightGoal) {
            WeightGoal.LOSE -> -0.5
            WeightGoal.MAINTAIN -> 0.0
            WeightGoal.GAIN -> 0.25
        }

        val dailyDeficit = (weeklyGoal * CALORIES_PER_KG / 7).toInt()
        return (tdee + dailyDeficit).coerceAtLeast(MIN_DAILY_CALORIES)
    }

    private fun calculateMacros(calories: Int, weightKg: Double, weightGoal: WeightGoal): Macros {
        val proteinMultiplier = when (weightGoal) {
            WeightGoal.LOSE -> 2.2
            WeightGoal.MAINTAIN -> 1.8
            WeightGoal.GAIN -> 2.0
        }
        val protein = (weightKg * proteinMultiplier).toInt()

        val fatCaloriesPercent = when (weightGoal) {
            WeightGoal.LOSE -> 0.25
            WeightGoal.MAINTAIN -> 0.30
            WeightGoal.GAIN -> 0.25
        }
        val fatCalories = (calories * fatCaloriesPercent).toInt()
        val fat = fatCalories / CALORIES_PER_GRAM_FAT

        val proteinCalories = protein * CALORIES_PER_GRAM_PROTEIN
        val carbCalories = calories - proteinCalories - fatCalories
        val carbs = (carbCalories / CALORIES_PER_GRAM_CARB).coerceAtLeast(MIN_CARBS)

        return Macros(protein = protein, carbs = carbs, fat = fat)
    }

    private data class Macros(val protein: Int, val carbs: Int, val fat: Int)

    companion object {
        private const val CALORIES_PER_KG = 7700
        private const val CALORIES_PER_GRAM_PROTEIN = 4
        private const val CALORIES_PER_GRAM_CARB = 4
        private const val CALORIES_PER_GRAM_FAT = 9
        private const val MIN_DAILY_CALORIES = 1200
        private const val MIN_CARBS = 50
    }
}
