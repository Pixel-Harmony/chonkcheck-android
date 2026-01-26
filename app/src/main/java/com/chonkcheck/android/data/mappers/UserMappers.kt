package com.chonkcheck.android.data.mappers

import com.auth0.android.result.UserProfile
import com.chonkcheck.android.data.api.dto.UserProfileDto
import com.chonkcheck.android.data.db.entity.UserEntity
import com.chonkcheck.android.domain.model.ActivityLevel
import com.chonkcheck.android.domain.model.DailyGoals
import com.chonkcheck.android.domain.model.HeightUnit
import com.chonkcheck.android.domain.model.Sex
import com.chonkcheck.android.domain.model.UnitPreferences
import com.chonkcheck.android.domain.model.User
import com.chonkcheck.android.domain.model.UserProfile as DomainUserProfile
import com.chonkcheck.android.domain.model.WeightGoal
import com.chonkcheck.android.domain.model.WeightUnit

fun UserProfile.toDomain(): User = User(
    id = getId() ?: "",
    email = email ?: "",
    name = name,
    avatarUrl = pictureURL,
    unitPreferences = UnitPreferences(
        weightUnit = WeightUnit.KG,
        heightUnit = HeightUnit.CM
    ),
    profile = null,
    goals = null,
    onboardingCompleted = false
)

fun UserEntity.toDomain(): User = User(
    id = id,
    email = email,
    name = name,
    avatarUrl = avatarUrl,
    unitPreferences = UnitPreferences(
        weightUnit = weightUnit.toWeightUnit(),
        heightUnit = heightUnit.toHeightUnit()
    ),
    profile = DomainUserProfile(
        height = height,
        birthDate = birthDate,
        sex = sex?.toSex(),
        activityLevel = activityLevel?.toActivityLevel()
    ),
    goals = if (dailyCalorieTarget != null) {
        DailyGoals(
            weightGoal = weightGoal?.toWeightGoal(),
            targetWeight = targetWeight,
            weeklyGoal = weeklyGoal,
            dailyCalorieTarget = dailyCalorieTarget,
            proteinTarget = proteinTarget ?: 0,
            carbsTarget = carbsTarget ?: 0,
            fatTarget = fatTarget ?: 0,
            bmr = bmr,
            tdee = tdee
        )
    } else null,
    onboardingCompleted = onboardingCompleted
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    email = email,
    name = name,
    avatarUrl = avatarUrl,
    weightUnit = unitPreferences.weightUnit.name.lowercase(),
    heightUnit = unitPreferences.heightUnit.name.lowercase(),
    height = profile?.height,
    birthDate = profile?.birthDate,
    sex = profile?.sex?.name?.lowercase(),
    activityLevel = profile?.activityLevel?.name?.lowercase(),
    weightGoal = goals?.weightGoal?.name?.lowercase(),
    targetWeight = goals?.targetWeight,
    weeklyGoal = goals?.weeklyGoal,
    dailyCalorieTarget = goals?.dailyCalorieTarget,
    proteinTarget = goals?.proteinTarget,
    carbsTarget = goals?.carbsTarget,
    fatTarget = goals?.fatTarget,
    bmr = goals?.bmr,
    tdee = goals?.tdee,
    onboardingCompleted = onboardingCompleted
)

/**
 * Merges API profile data into an existing local UserEntity.
 * Preserves local-only fields (id, avatarUrl, syncedAt) while updating
 * profile, goals, and onboarding status from the server.
 */
fun UserEntity.mergeWithApiProfile(dto: UserProfileDto): UserEntity = copy(
    name = dto.name ?: name,
    weightUnit = dto.weightUnit,
    heightUnit = dto.heightUnit ?: heightUnit,
    height = dto.height ?: height,
    sex = dto.sex ?: sex,
    activityLevel = dto.activityLevel?.toLocalActivityLevel() ?: activityLevel,
    weightGoal = dto.weightGoal ?: weightGoal,
    weeklyGoal = dto.weightGoalRate ?: weeklyGoal,
    dailyCalorieTarget = dto.goals.calories,
    proteinTarget = dto.goals.protein,
    carbsTarget = dto.goals.carbs,
    fatTarget = dto.goals.fat,
    tdee = dto.tdee ?: tdee,
    onboardingCompleted = dto.onboardingCompleted ?: onboardingCompleted,
    syncedAt = System.currentTimeMillis(),
    updatedAt = System.currentTimeMillis()
)

/**
 * Converts API activity level values to local entity format.
 * API uses: sedentary, light, moderate, active, very_active
 * Local uses: sedentary, lightly_active, moderately_active, very_active, extra_active
 */
private fun String.toLocalActivityLevel(): String = when (this.lowercase()) {
    "sedentary" -> "sedentary"
    "light" -> "lightly_active"
    "moderate" -> "moderately_active"
    "active" -> "very_active"
    "very_active" -> "extra_active"
    else -> this.lowercase()
}

private fun String.toWeightUnit(): WeightUnit = when (this.lowercase()) {
    "lb" -> WeightUnit.LB
    "st" -> WeightUnit.ST
    else -> WeightUnit.KG
}

private fun String.toHeightUnit(): HeightUnit = when (this.lowercase()) {
    "ft" -> HeightUnit.FT
    else -> HeightUnit.CM
}

private fun String.toSex(): Sex? = when (this.lowercase()) {
    "male" -> Sex.MALE
    "female" -> Sex.FEMALE
    "other" -> Sex.OTHER
    else -> null
}

private fun String.toActivityLevel(): ActivityLevel? = when (this.lowercase()) {
    "sedentary" -> ActivityLevel.SEDENTARY
    "lightly_active" -> ActivityLevel.LIGHTLY_ACTIVE
    "moderately_active" -> ActivityLevel.MODERATELY_ACTIVE
    "very_active" -> ActivityLevel.VERY_ACTIVE
    "extra_active" -> ActivityLevel.EXTRA_ACTIVE
    else -> null
}

private fun String.toWeightGoal(): WeightGoal? = when (this.lowercase()) {
    "lose" -> WeightGoal.LOSE
    "maintain" -> WeightGoal.MAINTAIN
    "gain" -> WeightGoal.GAIN
    else -> null
}
