package com.chonkcheck.android.presentation.ui.meals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chonkcheck.android.domain.model.MealType
import com.chonkcheck.android.domain.model.SavedMealItemType
import com.chonkcheck.android.presentation.ui.components.LoadingIndicator
import com.chonkcheck.android.presentation.ui.meals.components.MealItemNutrition
import com.chonkcheck.android.presentation.ui.meals.components.MealNutritionSummaryCard
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen
import com.chonkcheck.android.ui.theme.Coral
import com.chonkcheck.android.ui.theme.Purple

@Composable
fun SavedMealPreviewScreen(
    onNavigateBack: () -> Unit,
    onMealAdded: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SavedMealPreviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()

    LaunchedEffect(event) {
        when (event) {
            is SavedMealPreviewEvent.NavigateBack -> {
                onNavigateBack()
                viewModel.onEventConsumed()
            }
            is SavedMealPreviewEvent.MealAddedToDiary -> {
                onMealAdded()
                viewModel.onEventConsumed()
            }
            is SavedMealPreviewEvent.ShowError -> {
                viewModel.onEventConsumed()
            }
            null -> {}
        }
    }

    SavedMealPreviewScreenContent(
        uiState = uiState,
        onBackClick = onNavigateBack,
        onMealTypeSelected = viewModel::updateMealType,
        onItemQuantityChange = viewModel::updateItemQuantity,
        onLogClick = viewModel::logMeal,
        modifier = modifier
    )
}

@Composable
fun SavedMealPreviewScreenContent(
    uiState: SavedMealPreviewUiState,
    onBackClick: () -> Unit,
    onMealTypeSelected: (MealType) -> Unit,
    onItemQuantityChange: (Int, String) -> Unit,
    onLogClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingIndicator()
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = uiState.mealName,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Purple indicator
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Purple)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Error message
            if (uiState.error != null) {
                Text(
                    text = uiState.error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Meal type selector
            Text(
                text = "Add to",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            MealTypeSelector(
                selectedMealType = uiState.selectedMealType,
                onMealTypeSelected = onMealTypeSelected
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Items section
            Text(
                text = "Items",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    uiState.items.forEachIndexed { index, item ->
                        PreviewItemRow(
                            item = item,
                            onQuantityChange = { onItemQuantityChange(index, it) }
                        )
                        if (index < uiState.items.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            MealNutritionSummaryCard(totalNutrition = uiState.totalNutrition)

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = "Back",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Button(
                onClick = onLogClick,
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple
                )
            ) {
                Text(
                    text = if (uiState.isSaving) "Logging..." else "Log All",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun MealTypeSelector(
    selectedMealType: MealType,
    onMealTypeSelected: (MealType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MealType.entries.forEach { mealType ->
            val isSelected = selectedMealType == mealType
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) Purple else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onMealTypeSelected(mealType) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mealType.displayName,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PreviewItemRow(
    item: PreviewItem,
    onQuantityChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val indicatorColor = when (item.itemType) {
        SavedMealItemType.FOOD -> Coral
        SavedMealItemType.RECIPE -> ChonkGreen
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Name row
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(indicatorColor)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = item.itemName,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quantity input row
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = item.quantity.formatInput(),
                onValueChange = onQuantityChange,
                modifier = Modifier.width(80.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (item.quantity == 1.0) "serving" else "servings",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "${item.nutrition.calories.toInt()} cal",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun Double.formatInput(): String {
    return if (this == this.toLong().toDouble()) {
        this.toLong().toString()
    } else {
        this.toString()
    }
}

@Preview(showBackground = true)
@Composable
private fun SavedMealPreviewScreenPreview() {
    ChonkCheckTheme {
        SavedMealPreviewScreenContent(
            uiState = SavedMealPreviewUiState(
                isLoading = false,
                mealName = "My Usual Breakfast",
                items = listOf(
                    PreviewItem(
                        itemId = "1",
                        itemType = SavedMealItemType.FOOD,
                        itemName = "Eggs",
                        servingUnitName = "serving",
                        quantity = 2.0,
                        enteredAmount = null,
                        nutrition = MealItemNutrition(140.0, 12.0, 1.0, 10.0),
                        baseCalories = 70.0,
                        baseProtein = 6.0,
                        baseCarbs = 0.5,
                        baseFat = 5.0
                    ),
                    PreviewItem(
                        itemId = "2",
                        itemType = SavedMealItemType.FOOD,
                        itemName = "Toast",
                        servingUnitName = "slice",
                        quantity = 2.0,
                        enteredAmount = null,
                        nutrition = MealItemNutrition(160.0, 4.0, 32.0, 2.0),
                        baseCalories = 80.0,
                        baseProtein = 2.0,
                        baseCarbs = 16.0,
                        baseFat = 1.0
                    )
                ),
                totalNutrition = MealItemNutrition(300.0, 16.0, 33.0, 12.0),
                selectedMealType = MealType.BREAKFAST
            ),
            onBackClick = {},
            onMealTypeSelected = {},
            onItemQuantityChange = { _, _ -> },
            onLogClick = {}
        )
    }
}
