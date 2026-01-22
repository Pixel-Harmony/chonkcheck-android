package com.chonkcheck.android.presentation.ui.dashboard

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chonkcheck.android.domain.model.DailyGoals
import com.chonkcheck.android.domain.model.MacroProgress
import com.chonkcheck.android.domain.model.MacroTotals
import com.chonkcheck.android.domain.model.WeightStats
import com.chonkcheck.android.domain.model.WeightTrend
import com.chonkcheck.android.domain.model.WeightUnit
import com.chonkcheck.android.presentation.ui.dashboard.components.QuickActionCard
import com.chonkcheck.android.presentation.ui.dashboard.components.TodaySummaryCard
import com.chonkcheck.android.presentation.ui.diary.components.DailyMacroSummary
import com.chonkcheck.android.presentation.ui.weight.components.WeightStatsCards
import com.chonkcheck.android.presentation.ui.weight.components.WeightUnitConverter
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen
import com.chonkcheck.android.ui.theme.Teal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(
    onNavigateToDiary: () -> Unit,
    onNavigateToAddFood: () -> Unit,
    onNavigateToWeight: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardScreenContent(
        uiState = uiState,
        onTodayClick = onNavigateToDiary,
        onLogFoodClick = onNavigateToAddFood,
        onWeightClick = onNavigateToWeight
    )
}

@Composable
private fun DashboardScreenContent(
    uiState: DashboardUiState,
    onTodayClick: () -> Unit,
    onLogFoodClick: () -> Unit,
    onWeightClick: () -> Unit
) {
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date Header
        item {
            Text(
                text = formatDate(uiState.currentDate),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Macro Summary
        item {
            DailyMacroSummary(
                progress = uiState.macroProgress
            )
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Log Food",
                    subtitle = "Track meals",
                    icon = Icons.Filled.Add,
                    backgroundColor = ChonkGreen,
                    onClick = onLogFoodClick,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    title = "Weight",
                    subtitle = formatWeightSubtitle(uiState.latestWeight, uiState.weightUnit),
                    icon = Icons.Filled.Scale,
                    backgroundColor = Teal,
                    onClick = onWeightClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Today Summary Card
        item {
            TodaySummaryCard(
                calories = uiState.todayCalories,
                entryCount = uiState.todayEntryCount,
                onClick = onTodayClick
            )
        }

        // Weight Stats Cards
        item {
            Column {
                Text(
                    text = "Weight Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                WeightStatsCards(
                    stats = uiState.weightStats,
                    weightUnit = uiState.weightUnit
                )
            }
        }
    }
}

private fun formatDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
    return date.format(formatter)
}

private fun formatWeightSubtitle(weight: Double?, unit: WeightUnit): String {
    return if (weight != null) {
        WeightUnitConverter.formatWeight(weight, unit)
    } else {
        "Log weight"
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DashboardScreenPreview() {
    ChonkCheckTheme {
        DashboardScreenContent(
            uiState = DashboardUiState(
                currentDate = LocalDate.now(),
                macroProgress = MacroProgress(
                    current = MacroTotals(
                        calories = 1800.0,
                        protein = 120.0,
                        carbs = 180.0,
                        fat = 50.0
                    ),
                    goals = DailyGoals(
                        weightGoal = null,
                        targetWeight = null,
                        weeklyGoal = null,
                        dailyCalorieTarget = 2400,
                        proteinTarget = 150,
                        carbsTarget = 200,
                        fatTarget = 65,
                        bmr = null,
                        tdee = null
                    ),
                    caloriePercent = 0.75f,
                    proteinPercent = 0.8f,
                    carbsPercent = 0.9f,
                    fatPercent = 0.77f
                ),
                todayCalories = 1800,
                todayEntryCount = 7,
                latestWeight = 75.5,
                weightStats = WeightStats(
                    startingWeight = 80.0,
                    currentWeight = 75.5,
                    totalChange = -4.5,
                    trend = WeightTrend.LOSING
                ),
                weightUnit = WeightUnit.KG,
                isLoading = false
            ),
            onTodayClick = {},
            onLogFoodClick = {},
            onWeightClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenEmptyPreview() {
    ChonkCheckTheme {
        DashboardScreenContent(
            uiState = DashboardUiState(
                currentDate = LocalDate.now(),
                macroProgress = null,
                todayCalories = 0,
                todayEntryCount = 0,
                latestWeight = null,
                weightStats = null,
                weightUnit = WeightUnit.KG,
                isLoading = false
            ),
            onTodayClick = {},
            onLogFoodClick = {},
            onWeightClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenLoadingPreview() {
    ChonkCheckTheme {
        DashboardScreenContent(
            uiState = DashboardUiState(isLoading = true),
            onTodayClick = {},
            onLogFoodClick = {},
            onWeightClick = {}
        )
    }
}
