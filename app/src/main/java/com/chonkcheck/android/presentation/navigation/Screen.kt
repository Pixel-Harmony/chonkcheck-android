package com.chonkcheck.android.presentation.navigation

sealed class Screen(val route: String) {
    // Auth
    data object Login : Screen("login")

    // Onboarding
    data object Onboarding : Screen("onboarding")

    // Main screens
    data object Dashboard : Screen("dashboard")
    data object Diary : Screen("diary")
    data object Foods : Screen("foods")
    data object Recipes : Screen("recipes")
    data object Weight : Screen("weight")
    data object Settings : Screen("settings")

    // Detail screens
    data object FoodDetail : Screen("food/{foodId}") {
        fun createRoute(foodId: String) = "food/$foodId"
    }
    data object CreateFood : Screen("food/create")
    data object EditFood : Screen("food/{foodId}/edit") {
        fun createRoute(foodId: String) = "food/$foodId/edit"
    }

    data object RecipeDetail : Screen("recipe/{recipeId}") {
        fun createRoute(recipeId: String) = "recipe/$recipeId"
    }
    data object CreateRecipe : Screen("recipe/create")
    data object EditRecipe : Screen("recipe/{recipeId}/edit") {
        fun createRoute(recipeId: String) = "recipe/$recipeId/edit"
    }

    data object SavedMealDetail : Screen("saved_meal/{savedMealId}") {
        fun createRoute(savedMealId: String) = "saved_meal/$savedMealId"
    }
    data object CreateSavedMeal : Screen("saved_meal/create")
    data object EditSavedMeal : Screen("saved_meal/{savedMealId}/edit") {
        fun createRoute(savedMealId: String) = "saved_meal/$savedMealId/edit"
    }
    data object SavedMealPreview : Screen("saved_meal/{savedMealId}/preview/{date}/{mealType}") {
        fun createRoute(savedMealId: String, date: String, mealType: String) = "saved_meal/$savedMealId/preview/$date/$mealType"
    }

    // Diary entry screens
    data object DiaryAddEntry : Screen("diary/add/{date}/{mealType}") {
        fun createRoute(date: String, mealType: String) = "diary/add/$date/$mealType"
    }
    data object DiaryEditEntry : Screen("diary/edit/{entryId}") {
        fun createRoute(entryId: String) = "diary/edit/$entryId"
    }

    // Scanners
    data object BarcodeScanner : Screen("scanner/barcode")
    data object NutritionLabelScanner : Screen("scanner/nutrition_label")

    // Profile & Settings
    data object Profile : Screen("profile")
    data object Goals : Screen("settings/goals")
    data object Privacy : Screen("settings/privacy")

    // Exercise screens
    data object AddExercise : Screen("exercise/add/{date}") {
        fun createRoute(date: String) = "exercise/add/$date"
    }
    data object EditExercise : Screen("exercise/edit/{exerciseId}") {
        fun createRoute(exerciseId: String) = "exercise/edit/$exerciseId"
    }
}

object NavArgs {
    const val FOOD_ID = "foodId"
    const val RECIPE_ID = "recipeId"
    const val SAVED_MEAL_ID = "savedMealId"
    const val DATE = "date"
    const val MEAL_TYPE = "mealType"
    const val ENTRY_ID = "entryId"
    const val EXERCISE_ID = "exerciseId"
}
