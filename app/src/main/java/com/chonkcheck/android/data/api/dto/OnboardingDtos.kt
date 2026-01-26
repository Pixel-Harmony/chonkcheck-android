package com.chonkcheck.android.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class CompleteOnboardingRequest(
    val weightUnit: String,
    val heightUnit: String,
    val height: Double,
    val age: Int,
    val sex: String,
    val activityLevel: String,
    val startingWeight: Double,
    val goal: String,
    val weightGoalRate: Double,
    val dietPreset: String? = null,
    val customMacroRatios: CustomMacroRatiosDto? = null
)

@Serializable
data class CompleteOnboardingResponse(
    val profile: UserProfileDto,
    val tdee: Int,
    val goals: MacroGoalsDto
)
