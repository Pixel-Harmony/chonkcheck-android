package com.chonkcheck.android.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.chonkcheck.android.presentation.ui.auth.LoginScreen
import com.chonkcheck.android.presentation.ui.diary.DiaryScreen
import com.chonkcheck.android.presentation.ui.diary.addentry.AddDiaryEntryScreen
import com.chonkcheck.android.presentation.ui.foods.FoodFormScreen
import com.chonkcheck.android.presentation.ui.foods.FoodsScreen
import com.chonkcheck.android.presentation.ui.onboarding.OnboardingScreen
import com.chonkcheck.android.presentation.ui.scanner.BarcodeScannerScreen
import com.chonkcheck.android.presentation.ui.scanner.NutritionLabelScannerScreen
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
        BottomNavItem.Foods,
        BottomNavItem.Weight,
        BottomNavItem.Settings
    )

    val showBottomBar = currentDestination?.route in bottomBarScreens.map { it.screen.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomBarScreens.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (currentDestination?.hierarchy?.any { it.route == item.screen.route } == true) {
                                        item.selectedIcon
                                    } else {
                                        item.unselectedIcon
                                    },
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
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
                PlaceholderScreen("Dashboard")
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
                    }
                )
            }

            composable(Screen.CreateFood.route) { backStackEntry ->
                // Get scanned barcode from saved state handle
                val scannedBarcode = backStackEntry.savedStateHandle.get<String>("scanned_barcode")

                // Clear the saved state after reading
                if (scannedBarcode != null) {
                    backStackEntry.savedStateHandle.remove<String>("scanned_barcode")
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
                    scannedBarcode = scannedBarcode
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

                // Clear the saved state after reading
                if (scannedBarcode != null) {
                    backStackEntry.savedStateHandle.remove<String>("scanned_barcode")
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
                    scannedBarcode = scannedBarcode
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
                        // TODO: Pass nutrition data back when API is implemented
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
                    onFoodAdded = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.DiaryEditEntry.route,
                arguments = listOf(
                    navArgument(NavArgs.ENTRY_ID) { type = NavType.StringType }
                )
            ) {
                // TODO: Implement EditDiaryEntryScreen
                PlaceholderScreen("Edit Entry")
            }

            composable(Screen.Recipes.route) {
                PlaceholderScreen("Recipes")
            }

            composable(Screen.SavedMeals.route) {
                PlaceholderScreen("Saved Meals")
            }

            composable(Screen.Weight.route) {
                PlaceholderScreen("Weight")
            }

            composable(Screen.Settings.route) {
                PlaceholderScreen("Settings")
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
