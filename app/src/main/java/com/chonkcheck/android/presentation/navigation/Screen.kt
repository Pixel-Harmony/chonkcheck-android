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
    data object SavedMeals : Screen("saved_meals")
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

    // Scanners
    data object BarcodeScanner : Screen("scanner/barcode")
    data object NutritionLabelScanner : Screen("scanner/nutrition_label")

    // Profile & Settings
    data object Profile : Screen("profile")
    data object Goals : Screen("settings/goals")
    data object Privacy : Screen("settings/privacy")
}

object NavArgs {
    const val FOOD_ID = "foodId"
    const val RECIPE_ID = "recipeId"
    const val SAVED_MEAL_ID = "savedMealId"
    const val DATE = "date"
}
