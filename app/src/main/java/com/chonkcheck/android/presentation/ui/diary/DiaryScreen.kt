package com.chonkcheck.android.presentation.ui.diary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chonkcheck.android.domain.model.DailyGoals
import com.chonkcheck.android.domain.model.DiaryDay
import com.chonkcheck.android.domain.model.DiaryEntry
import com.chonkcheck.android.domain.model.MacroProgress
import com.chonkcheck.android.domain.model.MacroTotals
import com.chonkcheck.android.domain.model.MealType
import com.chonkcheck.android.domain.model.ServingUnit
import com.chonkcheck.android.presentation.ui.components.LoadingIndicator
import com.chonkcheck.android.presentation.ui.diary.components.CompleteDayButton
import com.chonkcheck.android.presentation.ui.diary.components.DailyMacroSummary
import com.chonkcheck.android.presentation.ui.diary.components.DateNavigator
import com.chonkcheck.android.presentation.ui.diary.components.MealSection
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import java.time.LocalDate

@Composable
fun DiaryScreen(
    onNavigateToAddFood: (date: LocalDate, mealType: MealType) -> Unit,
    onNavigateToEditEntry: (entryId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()

    // Handle navigation events
    LaunchedEffect(event) {
        when (val currentEvent = event) {
            is DiaryEvent.NavigateToAddFood -> {
                onNavigateToAddFood(currentEvent.date, currentEvent.mealType)
                viewModel.onEventConsumed()
            }
            is DiaryEvent.NavigateToEditEntry -> {
                onNavigateToEditEntry(currentEvent.entryId)
                viewModel.onEventConsumed()
            }
            is DiaryEvent.ShowDeleteSuccess -> {
                viewModel.onEventConsumed()
            }
            is DiaryEvent.ShowError -> {
                // Could show snackbar here
                viewModel.onEventConsumed()
            }
            null -> {}
        }
    }

    DiaryScreenContent(
        uiState = uiState,
        onPreviousDay = viewModel::previousDay,
        onNextDay = viewModel::nextDay,
        onDateClick = { /* TODO: Show date picker */ },
        onAddFood = viewModel::onAddFood,
        onEntryClick = viewModel::onEntryClick,
        onDeleteClick = viewModel::onDeleteClick,
        onDeleteConfirm = viewModel::onDeleteConfirm,
        onDeleteCancel = viewModel::onDeleteCancel,
        onCompleteDay = viewModel::onCompleteDay,
        onReopenDay = viewModel::onReopenDay,
        modifier = modifier
    )
}

@Composable
fun DiaryScreenContent(
    uiState: DiaryUiState,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onDateClick: () -> Unit,
    onAddFood: (MealType) -> Unit,
    onEntryClick: (DiaryEntry) -> Unit,
    onDeleteClick: (DiaryEntry) -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
    onCompleteDay: () -> Unit,
    onReopenDay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isToday = uiState.selectedDate == LocalDate.now()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Text(
            text = "Diary",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Date navigator
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            DateNavigator(
                selectedDate = uiState.selectedDate,
                onPreviousDay = onPreviousDay,
                onNextDay = onNextDay,
                onDateClick = onDateClick
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Macro summary
                DailyMacroSummary(progress = uiState.macroProgress)

                // Complete/Reopen day button
                CompleteDayButton(
                    isCompleted = uiState.diaryDay?.isCompleted == true,
                    isToday = isToday,
                    hasEntries = uiState.diaryDay?.hasEntries == true,
                    onComplete = onCompleteDay,
                    onReopen = onReopenDay
                )

                // Meal sections
                MealType.entries.forEach { mealType ->
                    val entries = uiState.diaryDay?.entriesByMeal?.get(mealType) ?: emptyList()
                    MealSection(
                        mealType = mealType,
                        entries = entries,
                        onAddFood = { onAddFood(mealType) },
                        onEntryClick = onEntryClick,
                        isCompleted = uiState.diaryDay?.isCompleted == true
                    )
                }

                // Bottom padding for navigation bar
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Delete confirmation dialog
    if (uiState.deleteConfirmation != null) {
        AlertDialog(
            onDismissRequest = onDeleteCancel,
            title = {
                Text(
                    text = "Delete entry?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            text = {
                Text(
                    text = "\"${uiState.deleteConfirmation.name}\" will be removed from your diary.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = onDeleteConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = onDeleteCancel) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DiaryScreenPreview() {
    ChonkCheckTheme {
        DiaryScreenContent(
            uiState = DiaryUiState(
                selectedDate = LocalDate.now(),
                diaryDay = DiaryDay(
                    date = LocalDate.now(),
                    entriesByMeal = mapOf(
                        MealType.BREAKFAST to listOf(
                            DiaryEntry(
                                id = "1",
                                userId = "user1",
                                date = LocalDate.now(),
                                mealType = MealType.BREAKFAST,
                                foodId = "food1",
                                recipeId = null,
                                servingSize = 100.0,
                                servingUnit = ServingUnit.GRAM,
                                numberOfServings = 1.0,
                                calories = 165.0,
                                protein = 31.0,
                                carbs = 0.0,
                                fat = 3.6,
                                name = "Chicken Breast",
                                brand = "Tesco",
                                createdAt = System.currentTimeMillis()
                            )
                        ),
                        MealType.LUNCH to emptyList(),
                        MealType.DINNER to emptyList(),
                        MealType.SNACKS to emptyList()
                    ),
                    totals = MacroTotals(
                        calories = 165.0,
                        protein = 31.0,
                        carbs = 0.0,
                        fat = 3.6
                    ),
                    isCompleted = false
                ),
                macroProgress = MacroProgress(
                    current = MacroTotals(165.0, 31.0, 0.0, 3.6),
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
                    caloriePercent = 0.07f,
                    proteinPercent = 0.21f,
                    carbsPercent = 0f,
                    fatPercent = 0.06f
                ),
                isLoading = false
            ),
            onPreviousDay = {},
            onNextDay = {},
            onDateClick = {},
            onAddFood = {},
            onEntryClick = {},
            onDeleteClick = {},
            onDeleteConfirm = {},
            onDeleteCancel = {},
            onCompleteDay = {},
            onReopenDay = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DiaryScreenLoadingPreview() {
    ChonkCheckTheme {
        DiaryScreenContent(
            uiState = DiaryUiState(isLoading = true),
            onPreviousDay = {},
            onNextDay = {},
            onDateClick = {},
            onAddFood = {},
            onEntryClick = {},
            onDeleteClick = {},
            onDeleteConfirm = {},
            onDeleteCancel = {},
            onCompleteDay = {},
            onReopenDay = {}
        )
    }
}
