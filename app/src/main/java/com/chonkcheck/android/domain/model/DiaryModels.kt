package com.chonkcheck.android.domain.model

import java.time.LocalDate

data class DiaryEntry(
    val id: String,
    val userId: String,
    val date: LocalDate,
    val mealType: MealType,
    val foodId: String?,
    val recipeId: String?,
    val servingSize: Double,
    val servingUnit: ServingUnit,
    val numberOfServings: Double,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val name: String,
    val brand: String?,
    val createdAt: Long
)

enum class MealType(val apiValue: String, val displayName: String) {
    BREAKFAST("breakfast", "Breakfast"),
    LUNCH("lunch", "Lunch"),
    DINNER("dinner", "Dinner"),
    SNACKS("snacks", "Snacks");

    companion object {
        fun fromApiValue(value: String): MealType {
            return entries.find { it.apiValue == value } ?: SNACKS
        }
    }
}

data class DiaryDay(
    val date: LocalDate,
    val entriesByMeal: Map<MealType, List<DiaryEntry>>,
    val totals: MacroTotals,
    val isCompleted: Boolean
) {
    val allEntries: List<DiaryEntry>
        get() = entriesByMeal.values.flatten()

    val hasEntries: Boolean
        get() = allEntries.isNotEmpty()
}

data class MacroTotals(
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
) {
    companion object {
        val ZERO = MacroTotals(0.0, 0.0, 0.0, 0.0)
    }

    operator fun plus(other: MacroTotals): MacroTotals {
        return MacroTotals(
            calories = calories + other.calories,
            protein = protein + other.protein,
            carbs = carbs + other.carbs,
            fat = fat + other.fat
        )
    }
}

data class MacroProgress(
    val current: MacroTotals,
    val goals: DailyGoals,
    val caloriePercent: Float,
    val proteinPercent: Float,
    val carbsPercent: Float,
    val fatPercent: Float
) {
    companion object {
        fun calculate(current: MacroTotals, goals: DailyGoals): MacroProgress {
            return MacroProgress(
                current = current,
                goals = goals,
                caloriePercent = if (goals.dailyCalorieTarget > 0)
                    (current.calories / goals.dailyCalorieTarget).toFloat().coerceIn(0f, 1f)
                else 0f,
                proteinPercent = if (goals.proteinTarget > 0)
                    (current.protein / goals.proteinTarget).toFloat().coerceIn(0f, 1f)
                else 0f,
                carbsPercent = if (goals.carbsTarget > 0)
                    (current.carbs / goals.carbsTarget).toFloat().coerceIn(0f, 1f)
                else 0f,
                fatPercent = if (goals.fatTarget > 0)
                    (current.fat / goals.fatTarget).toFloat().coerceIn(0f, 1f)
                else 0f
            )
        }
    }
}

data class CreateDiaryEntryParams(
    val date: LocalDate,
    val mealType: MealType,
    val foodId: String?,
    val recipeId: String?,
    val servingSize: Double,
    val servingUnit: ServingUnit,
    val numberOfServings: Double
)

data class UpdateDiaryEntryParams(
    val mealType: MealType? = null,
    val servingSize: Double? = null,
    val servingUnit: ServingUnit? = null,
    val numberOfServings: Double? = null
)
