package com.chonkcheck.android.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val name: String?,
    val avatarUrl: String?,

    // Unit preferences
    val weightUnit: String, // "kg" or "lb"
    val heightUnit: String, // "cm" or "ft"

    // Profile
    val height: Double?, // Always stored in cm
    val birthDate: String?, // ISO date
    val sex: String?, // "male", "female", "other"
    val activityLevel: String?, // "sedentary", "lightly_active", "moderately_active", "very_active", "extra_active"

    // Goals
    val weightGoal: String?, // "lose", "maintain", "gain"
    val targetWeight: Double?, // Always stored in kg
    val weeklyGoal: Double?, // kg per week (negative for loss)
    val dailyCalorieTarget: Int?,
    val proteinTarget: Int?, // grams
    val carbsTarget: Int?, // grams
    val fatTarget: Int?, // grams

    // TDEE calculation
    val bmr: Int?,
    val tdee: Int?,

    // Onboarding
    val onboardingCompleted: Boolean = false,

    // Sync
    val syncedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
