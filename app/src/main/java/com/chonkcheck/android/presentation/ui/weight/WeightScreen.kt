package com.chonkcheck.android.presentation.ui.weight

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chonkcheck.android.domain.model.WeightChartPoint
import com.chonkcheck.android.domain.model.WeightEntry
import com.chonkcheck.android.domain.model.WeightStats
import com.chonkcheck.android.domain.model.WeightTrend
import com.chonkcheck.android.domain.model.WeightTrendPrediction
import com.chonkcheck.android.domain.model.WeightUnit
import com.chonkcheck.android.presentation.ui.weight.components.LogWeightSection
import com.chonkcheck.android.presentation.ui.weight.components.WeightChart
import com.chonkcheck.android.presentation.ui.weight.components.WeightEntryCard
import com.chonkcheck.android.presentation.ui.weight.components.WeightStatsCards
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import java.time.LocalDate

@Composable
fun WeightScreen(
    viewModel: WeightViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(event) {
        when (event) {
            is WeightEvent.ShowSaveSuccess -> {
                snackbarHostState.showSnackbar("Weight logged successfully")
                viewModel.onEventConsumed()
            }
            is WeightEvent.ShowDeleteSuccess -> {
                snackbarHostState.showSnackbar("Entry deleted")
                viewModel.onEventConsumed()
            }
            is WeightEvent.ShowError -> {
                snackbarHostState.showSnackbar((event as WeightEvent.ShowError).message)
                viewModel.onEventConsumed()
            }
            null -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        WeightScreenContent(
            uiState = uiState,
            onLogWeight = viewModel::logWeight,
            onDeleteClick = viewModel::onDeleteClick,
            onDeleteConfirm = viewModel::onDeleteConfirm,
            onDeleteCancel = viewModel::onDeleteCancel
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun WeightScreenContent(
    uiState: WeightUiState,
    onLogWeight: (weight: Double, date: LocalDate, notes: String?) -> Unit,
    onDeleteClick: (WeightEntry) -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit
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
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Weight",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            WeightStatsCards(
                stats = uiState.stats,
                weightUnit = uiState.weightUnit
            )
        }

        if (uiState.chartData.isNotEmpty()) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.titleMedium
                        )

                        if (uiState.trendPrediction != null) {
                            IconButton(
                                onClick = { /* TODO: Show trend info */ },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "Trend info",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    WeightChart(
                        chartData = uiState.chartData,
                        weightUnit = uiState.weightUnit
                    )
                }
            }
        }

        item {
            LogWeightSection(
                weightUnit = uiState.weightUnit,
                isSaving = uiState.isSaving,
                onLogWeight = onLogWeight
            )
        }

        if (uiState.entries.isNotEmpty()) {
            item {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(
                items = uiState.entries,
                key = { it.id }
            ) { entry ->
                WeightEntryCard(
                    entry = entry,
                    weightUnit = uiState.weightUnit,
                    onDeleteClick = { onDeleteClick(entry) }
                )
            }
        }

        if (uiState.entries.isEmpty() && uiState.chartData.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No weight entries yet. Log your first weight above.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    uiState.deleteConfirmation?.let { entry ->
        AlertDialog(
            onDismissRequest = onDeleteCancel,
            title = { Text("Delete Entry") },
            text = {
                Text("Are you sure you want to delete this weight entry?")
            },
            confirmButton = {
                TextButton(onClick = onDeleteConfirm) {
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
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WeightScreenPreview() {
    val today = LocalDate.now()
    val entries = listOf(
        WeightEntry("1", "user1", today, 73.0, null, System.currentTimeMillis()),
        WeightEntry("2", "user1", today.minusDays(1), 73.5, null, System.currentTimeMillis()),
        WeightEntry("3", "user1", today.minusDays(3), 74.0, "After workout", System.currentTimeMillis()),
        WeightEntry("4", "user1", today.minusDays(5), 74.5, null, System.currentTimeMillis()),
        WeightEntry("5", "user1", today.minusDays(7), 75.0, null, System.currentTimeMillis())
    )

    val chartData = entries.sortedBy { it.date }.map {
        WeightChartPoint(it.date, it.weight, false)
    } + listOf(
        WeightChartPoint(today.plusDays(7), 72.5, true),
        WeightChartPoint(today.plusDays(14), 72.0, true),
        WeightChartPoint(today.plusDays(21), 71.5, true),
        WeightChartPoint(today.plusDays(28), 71.0, true)
    )

    ChonkCheckTheme {
        WeightScreenContent(
            uiState = WeightUiState(
                entries = entries,
                stats = WeightStats(
                    startingWeight = 75.0,
                    currentWeight = 73.0,
                    totalChange = -2.0,
                    trend = WeightTrend.LOSING
                ),
                chartData = chartData,
                trendPrediction = WeightTrendPrediction(
                    ratePerWeek = -0.5,
                    trend = WeightTrend.LOSING,
                    projectedPoints = chartData.filter { it.isTrend }
                ),
                weightUnit = WeightUnit.KG,
                isLoading = false
            ),
            onLogWeight = { _, _, _ -> },
            onDeleteClick = {},
            onDeleteConfirm = {},
            onDeleteCancel = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeightScreenEmptyPreview() {
    ChonkCheckTheme {
        WeightScreenContent(
            uiState = WeightUiState(
                entries = emptyList(),
                stats = null,
                chartData = emptyList(),
                trendPrediction = null,
                weightUnit = WeightUnit.KG,
                isLoading = false
            ),
            onLogWeight = { _, _, _ -> },
            onDeleteClick = {},
            onDeleteConfirm = {},
            onDeleteCancel = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeightScreenLoadingPreview() {
    ChonkCheckTheme {
        WeightScreenContent(
            uiState = WeightUiState(isLoading = true),
            onLogWeight = { _, _, _ -> },
            onDeleteClick = {},
            onDeleteConfirm = {},
            onDeleteCancel = {}
        )
    }
}
