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
data class ViewedMilestonesDto(
    val weekly: String? = null,
    val monthly: String? = null
)

@Serializable
data class UserProfileDto(
    val email: String? = null,
    val name: String? = null,
    val goals: MacroGoalsDto,
    val weightUnit: String,
    val theme: String? = null,
    val consentGiven: Boolean,
    val consentDate: String? = null,
    val createdAt: String,
    val height: Double? = null,
    val heightUnit: String? = null,
    val age: Int? = null,
    val sex: String? = null,
    val activityLevel: String? = null,
    val startingWeight: Double? = null,
    val inviteCode: String? = null,
    val inviteCodeUsedAt: String? = null,
    val activated: Boolean? = null,
    val onboardingCompleted: Boolean? = null,
    val tdee: Int? = null,
    val weightGoal: String? = null,
    val weightGoalRate: Double? = null,
    val dietPreset: String? = null,
    val customMacroRatios: CustomMacroRatiosDto? = null,
    val viewedMilestones: ViewedMilestonesDto? = null
)

@Serializable
data class UserExportResponse(
    val exportDate: String,
    val userId: String,
    val profile: UserProfileDto,
    val diaryEntries: List<ExportDiaryEntryDto>,
    val weightEntries: List<ExportWeightEntryDto>
)

@Serializable
data class ExportDiaryEntryDto(
    val id: String,
    val date: String,
    val itemType: String,
    val quantity: Double,
    val meal: String,
    val timestamp: String,
    val foodId: String? = null,
    val recipeId: String? = null,
    val enteredAmount: Double? = null,
    val mealGroupId: String? = null,
    val mealGroupName: String? = null
)

@Serializable
data class ExportWeightEntryDto(
    val date: String,
    val weight: Double,
    val unit: String,
    val notes: String? = null,
    val createdAt: String? = null
)
