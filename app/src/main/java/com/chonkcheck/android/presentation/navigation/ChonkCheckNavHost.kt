package com.chonkcheck.android.presentation.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chonkcheck.android.domain.model.MealType
import com.chonkcheck.android.domain.model.NutritionLabelData
import com.chonkcheck.android.presentation.ui.auth.LoginScreen
import com.chonkcheck.android.presentation.ui.dashboard.DashboardScreen
import com.chonkcheck.android.presentation.ui.diary.DiaryScreen
import com.chonkcheck.android.presentation.ui.diary.addentry.AddDiaryEntryScreen
import com.chonkcheck.android.presentation.ui.diary.editentry.EditDiaryEntryScreen
import com.chonkcheck.android.presentation.ui.foods.FoodFormScreen
import com.chonkcheck.android.presentation.ui.foods.FoodsScreen
import com.chonkcheck.android.presentation.ui.onboarding.OnboardingScreen
import com.chonkcheck.android.presentation.ui.meals.SavedMealFormScreen
import com.chonkcheck.android.presentation.ui.meals.SavedMealPreviewScreen
import com.chonkcheck.android.presentation.ui.recipes.RecipeFormScreen
import com.chonkcheck.android.presentation.ui.recipes.RecipesScreen
import com.chonkcheck.android.presentation.ui.exercise.ExerciseFormScreen
import com.chonkcheck.android.presentation.ui.scanner.BarcodeScannerScreen
import com.chonkcheck.android.presentation.ui.scanner.NutritionLabelScannerScreen
import com.chonkcheck.android.presentation.ui.settings.SettingsScreen
import com.chonkcheck.android.presentation.ui.weight.WeightScreen
import java.time.LocalDate

@Composable
fun ChonkCheckNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Dashboard.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarScreens = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Diary,
        BottomNavItem.Weight,
        BottomNavItem.Foods,
        BottomNavItem.Settings
    )

    val isDarkTheme = isSystemInDarkTheme()

    val showBottomBar = currentDestination?.route in bottomBarScreens.map { it.screen.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomBarScreens.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                        val activeColor = if (isDarkTheme) item.activeColorDark else item.activeColorLight

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painter = painterResource(item.iconRes),
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = activeColor,
                                selectedTextColor = activeColor,
                                indicatorColor = Color.Transparent
                            ),
                            onClick = {
                                val isStartDestination = item.screen.route == Screen.Dashboard.route
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = !isStartDestination
                                    }
                                    launchSingleTop = true
                                    restoreState = !isStartDestination
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // Onboarding
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onOnboardingComplete = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // Main screens
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToDiary = { navController.navigate(Screen.Diary.route) },
                    onNavigateToAddFood = {
                        navController.navigate(
                            Screen.DiaryAddEntry.createRoute(
                                LocalDate.now().toString(),
                                MealType.SNACKS.apiValue
                            )
                        )
                    },
                    onNavigateToWeight = { navController.navigate(Screen.Weight.route) }
                )
            }

            composable(Screen.Diary.route) {
                DiaryScreen(
                    onNavigateToAddFood = { date, mealType ->
                        navController.navigate(
                            Screen.DiaryAddEntry.createRoute(date.toString(), mealType.apiValue)
                        )
                    },
                    onNavigateToEditEntry = { entryId ->
                        navController.navigate(
                            Screen.DiaryEditEntry.createRoute(entryId)
                        )
                    },
                    onNavigateToAddExercise = { date ->
                        navController.navigate(
                            Screen.AddExercise.createRoute(date.toString())
                        )
                    },
                    onNavigateToEditExercise = { exerciseId ->
                        navController.navigate(
                            Screen.EditExercise.createRoute(exerciseId)
                        )
                    }
                )
            }

            composable(Screen.Foods.route) {
                FoodsScreen(
                    onNavigateToEditFood = { foodId ->
                        navController.navigate(Screen.EditFood.createRoute(foodId))
                    },
                    onNavigateToCreateFood = {
                        navController.navigate(Screen.CreateFood.route)
                    },
                    onNavigateToCreateRecipe = {
                        navController.navigate(Screen.CreateRecipe.route)
                    },
                    onNavigateToEditRecipe = { recipeId ->
                        navController.navigate(Screen.EditRecipe.createRoute(recipeId))
                    },
                    onNavigateToCreateMeal = {
                        navController.navigate(Screen.CreateSavedMeal.route)
                    },
                    onNavigateToEditMeal = { mealId ->
                        navController.navigate(Screen.EditSavedMeal.createRoute(mealId))
                    }
                )
            }

            composable(Screen.CreateFood.route) { backStackEntry ->
                // Get scanned barcode from saved state handle
                val scannedBarcode = backStackEntry.savedStateHandle.get<String>("scanned_barcode")
                val scannedLabelData = backStackEntry.savedStateHandle.get<NutritionLabelData>("scanned_label_data")

                // Clear the saved state after reading
                if (scannedBarcode != null) {
                    backStackEntry.savedStateHandle.remove<String>("scanned_barcode")
                }
                if (scannedLabelData != null) {
                    backStackEntry.savedStateHandle.remove<NutritionLabelData>("scanned_label_data")
                }

                FoodFormScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCreateOverride = { _ ->
                        // For create screen, this shouldn't be called
                        navController.popBackStack()
                    },
                    onNavigateToBarcodeScanner = {
                        navController.navigate(Screen.BarcodeScanner.route)
                    },
                    onNavigateToLabelScanner = {
                        navController.navigate(Screen.NutritionLabelScanner.route)
                    },
                    scannedBarcode = scannedBarcode,
                    scannedLabelData = scannedLabelData
                )
            }

            composable(
                route = Screen.EditFood.route,
                arguments = listOf(
                    navArgument(NavArgs.FOOD_ID) { type = NavType.StringType }
                )
            ) { backStackEntry ->
                // Get scanned barcode from saved state handle
                val scannedBarcode = backStackEntry.savedStateHandle.get<String>("scanned_barcode")
                val scannedLabelData = backStackEntry.savedStateHandle.get<NutritionLabelData>("scanned_label_data")

                // Clear the saved state after reading
                if (scannedBarcode != null) {
                    backStackEntry.savedStateHandle.remove<String>("scanned_barcode")
                }
                if (scannedLabelData != null) {
                    backStackEntry.savedStateHandle.remove<NutritionLabelData>("scanned_label_data")
                }

                FoodFormScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCreateOverride = { overrideOfId ->
                        // Navigate to create screen with override info
                        // For now, just navigate to create - override functionality can be added later
                        navController.navigate(Screen.CreateFood.route)
                    },
                    onNavigateToBarcodeScanner = {
                        navController.navigate(Screen.BarcodeScanner.route)
                    },
                    onNavigateToLabelScanner = {
                        navController.navigate(Screen.NutritionLabelScanner.route)
                    },
                    scannedBarcode = scannedBarcode,
                    scannedLabelData = scannedLabelData
                )
            }

            // Scanners
            composable(Screen.BarcodeScanner.route) {
                BarcodeScannerScreen(
                    onBarcodeScanned = { barcode ->
                        // Pop back and let the ViewModel handle the barcode
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("scanned_barcode", barcode)
                        navController.popBackStack()
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.NutritionLabelScanner.route) {
                NutritionLabelScannerScreen(
                    onLabelScanned = { nutritionData ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("scanned_label_data", nutritionData)
                        navController.popBackStack()
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Diary entry screens
            composable(
                route = Screen.DiaryAddEntry.route,
                arguments = listOf(
                    navArgument(NavArgs.DATE) { type = NavType.StringType },
                    navArgument(NavArgs.MEAL_TYPE) { type = NavType.StringType }
                )
            ) {
                AddDiaryEntryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToBarcodeScanner = {
                        navController.navigate(Screen.BarcodeScanner.route)
                    },
                    onFoodAdded = { navController.popBackStack() },
                    onNavigateToMealPreview = { savedMealId, date, mealType ->
                        navController.navigate(Screen.SavedMealPreview.createRoute(savedMealId, date, mealType))
                    }
                )
            }

            composable(
                route = Screen.DiaryEditEntry.route,
                arguments = listOf(
                    navArgument(NavArgs.ENTRY_ID) { type = NavType.StringType }
                )
            ) {
                EditDiaryEntryScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Recipes.route) {
                RecipesScreen(
                    onNavigateToCreateRecipe = {
                        navController.navigate(Screen.CreateRecipe.route)
                    },
                    onNavigateToEditRecipe = { recipeId ->
                        navController.navigate(Screen.EditRecipe.createRoute(recipeId))
                    }
                )
            }

            composable(Screen.CreateRecipe.route) {
                RecipeFormScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditRecipe.route,
                arguments = listOf(
                    navArgument(NavArgs.RECIPE_ID) { type = NavType.StringType }
                )
            ) {
                RecipeFormScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CreateSavedMeal.route) {
                SavedMealFormScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditSavedMeal.route,
                arguments = listOf(
                    navArgument(NavArgs.SAVED_MEAL_ID) { type = NavType.StringType }
                )
            ) {
                SavedMealFormScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.SavedMealPreview.route,
                arguments = listOf(
                    navArgument(NavArgs.SAVED_MEAL_ID) { type = NavType.StringType },
                    navArgument(NavArgs.DATE) { type = NavType.StringType },
                    navArgument(NavArgs.MEAL_TYPE) { type = NavType.StringType }
                )
            ) {
                SavedMealPreviewScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onMealAdded = { navController.popBackStack() }
                )
            }

            composable(Screen.Weight.route) {
                WeightScreen()
            }

            // Exercise screens
            composable(
                route = Screen.AddExercise.route,
                arguments = listOf(
                    navArgument(NavArgs.DATE) { type = NavType.StringType }
                )
            ) {
                ExerciseFormScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditExercise.route,
                arguments = listOf(
                    navArgument(NavArgs.EXERCISE_ID) { type = NavType.StringType }
                )
            ) {
                ExerciseFormScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onLoggedOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Additional detail screens will be added in later phases
        }
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
