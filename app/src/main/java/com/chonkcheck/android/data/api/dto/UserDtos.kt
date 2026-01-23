package com.chonkcheck.android.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MacroGoalsDto(
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)

@Serializable
data class CustomMacroRatiosDto(
    val protein: Int? = null,
    val carbs: Int? = null,
    val fat: Int? = null
)

@Serializable
data class UpdateUserProfileRequest(
    val name: String? = null,
    val goals: MacroGoalsDto? = null,
    val weightUnit: String? = null,
    val theme: String? = null,
    val height: Double? = null,
    val heightUnit: String? = null,
    val age: Int? = null,
    val sex: String? = null,
    val activityLevel: String? = null,
    val startingWeight: Double? = null,
    val weightGoal: String? = null,
    val weightGoalRate: Double? = null,
    val dietPreset: String? = null,
    val customMacroRatios: CustomMacroRatiosDto? = null
)

@Serializable
data class UserProfileDto(
    val email: String? = null,
    val name: String? = null,
    val goals: MacroGoalsDto,
    val weightUnit: String,
    val theme: String? = null,
    val consentGiven: Boolean,
    val createdAt: String? = null,
    val height: Double? = null,
    val heightUnit: String? = null,
    val age: Int? = null,
    val sex: String? = null,
    val activityLevel: String? = null,
    val startingWeight: Double? = null,
    val weightGoal: String? = null,
    val weightGoalRate: Double? = null,
    val dietPreset: String? = null,
    val customMacroRatios: CustomMacroRatiosDto? = null,
    val tdee: Int? = null,
    val bmr: Int? = null
)

@Serializable
data class UserExportResponse(
    val data: String
)
