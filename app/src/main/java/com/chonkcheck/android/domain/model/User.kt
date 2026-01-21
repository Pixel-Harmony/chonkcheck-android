package com.chonkcheck.android.domain.model

data class User(
    val id: String,
    val email: String,
    val name: String?,
    val avatarUrl: String?,
    val unitPreferences: UnitPreferences,
    val profile: UserProfile?,
    val goals: DailyGoals?,
    val onboardingCompleted: Boolean
)

data class UnitPreferences(
    val weightUnit: WeightUnit,
    val heightUnit: HeightUnit
)

enum class WeightUnit(val symbol: String) {
    KG("kg"),
    LB("lb")
}

enum class HeightUnit(val symbol: String) {
    CM("cm"),
    FT("ft")
}

data class UserProfile(
    val height: Double?, // Always in cm
    val birthDate: String?,
    val sex: Sex?,
    val activityLevel: ActivityLevel?
)

enum class Sex {
    MALE,
    FEMALE,
    OTHER
}

enum class ActivityLevel(val factor: Double, val displayName: String) {
    SEDENTARY(1.2, "Sedentary"),
    LIGHTLY_ACTIVE(1.375, "Lightly Active"),
    MODERATELY_ACTIVE(1.55, "Moderately Active"),
    VERY_ACTIVE(1.725, "Very Active"),
    EXTRA_ACTIVE(1.9, "Extra Active")
}

data class DailyGoals(
    val weightGoal: WeightGoal?,
    val targetWeight: Double?, // Always in kg
    val weeklyGoal: Double?, // kg per week
    val dailyCalorieTarget: Int,
    val proteinTarget: Int,
    val carbsTarget: Int,
    val fatTarget: Int,
    val bmr: Int?,
    val tdee: Int?
)

enum class WeightGoal(val displayName: String) {
    LOSE("Lose Weight"),
    MAINTAIN("Maintain Weight"),
    GAIN("Gain Weight")
}
