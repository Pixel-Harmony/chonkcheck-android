package com.chonkcheck.android.domain.model

enum class DietPreset(
    val displayName: String,
    val description: String,
    val proteinPercent: Int,
    val carbsPercent: Int,
    val fatPercent: Int
) {
    BALANCED(
        displayName = "Balanced",
        description = "Equal mix of all macros",
        proteinPercent = 30,
        carbsPercent = 40,
        fatPercent = 30
    ),
    LOW_CARB(
        displayName = "Low Carb",
        description = "Reduced carbohydrates",
        proteinPercent = 35,
        carbsPercent = 25,
        fatPercent = 40
    ),
    HIGH_PROTEIN(
        displayName = "High Protein",
        description = "Focus on protein intake",
        proteinPercent = 40,
        carbsPercent = 35,
        fatPercent = 25
    ),
    KETO(
        displayName = "Keto",
        description = "Very low carb, high fat",
        proteinPercent = 25,
        carbsPercent = 5,
        fatPercent = 70
    ),
    GLP1_FRIENDLY(
        displayName = "GLP-1 Friendly",
        description = "Optimized for GLP-1 medications",
        proteinPercent = 40,
        carbsPercent = 35,
        fatPercent = 25
    ),
    MEDITERRANEAN(
        displayName = "Mediterranean",
        description = "Heart-healthy eating pattern",
        proteinPercent = 25,
        carbsPercent = 45,
        fatPercent = 30
    ),
    CUSTOM(
        displayName = "Custom",
        description = "Set your own macros",
        proteinPercent = 30,
        carbsPercent = 40,
        fatPercent = 30
    );

    fun calculateMacros(totalCalories: Int): MacroTargets {
        val proteinCalories = totalCalories * proteinPercent / 100
        val carbsCalories = totalCalories * carbsPercent / 100
        val fatCalories = totalCalories * fatPercent / 100

        return MacroTargets(
            protein = proteinCalories / 4, // 4 cal per gram
            carbs = carbsCalories / 4, // 4 cal per gram
            fat = fatCalories / 9 // 9 cal per gram
        )
    }
}

data class MacroTargets(
    val protein: Int,
    val carbs: Int,
    val fat: Int
)
