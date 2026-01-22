package com.chonkcheck.android.presentation.ui.diary.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.domain.model.DiaryEntry
import com.chonkcheck.android.domain.model.MealType
import com.chonkcheck.android.domain.model.ServingUnit
import com.chonkcheck.android.ui.theme.Amber
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen
import com.chonkcheck.android.ui.theme.Coral
import com.chonkcheck.android.ui.theme.Purple
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun MealSection(
    mealType: MealType,
    entries: List<DiaryEntry>,
    onAddFood: () -> Unit,
    onEntryClick: (DiaryEntry) -> Unit,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }

    val mealColor = getMealColor(mealType)
    val totalCalories = entries.sumOf { it.calories }.roundToInt()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Color indicator
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(mealColor)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Meal name
                    Text(
                        text = mealType.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Entry count
                    if (entries.isNotEmpty()) {
                        Text(
                            text = "(${entries.size})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Total calories
                    if (entries.isNotEmpty()) {
                        Text(
                            text = "$totalCalories cal",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Expand/collapse icon
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    if (entries.isEmpty()) {
                        // Empty state
                        Text(
                            text = "No items",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        // Entry list
                        entries.forEachIndexed { index, entry ->
                            DiaryEntryCard(
                                entry = entry,
                                onClick = { onEntryClick(entry) }
                            )
                            if (index < entries.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }

                    // Add button (only show if not completed)
                    if (!isCompleted) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        TextButton(
                            onClick = onAddFood,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = mealColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add",
                                color = mealColor,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getMealColor(mealType: MealType): Color {
    return when (mealType) {
        MealType.BREAKFAST -> Coral
        MealType.LUNCH -> Amber
        MealType.DINNER -> ChonkGreen
        MealType.SNACKS -> Purple
    }
}

@Preview(showBackground = true)
@Composable
private fun MealSectionWithEntriesPreview() {
    ChonkCheckTheme {
        MealSection(
            mealType = MealType.BREAKFAST,
            entries = listOf(
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
                ),
                DiaryEntry(
                    id = "2",
                    userId = "user1",
                    date = LocalDate.now(),
                    mealType = MealType.BREAKFAST,
                    foodId = "food2",
                    recipeId = null,
                    servingSize = 50.0,
                    servingUnit = ServingUnit.GRAM,
                    numberOfServings = 2.0,
                    calories = 350.0,
                    protein = 10.0,
                    carbs = 60.0,
                    fat = 5.0,
                    name = "Oatmeal",
                    brand = null,
                    createdAt = System.currentTimeMillis()
                )
            ),
            onAddFood = {},
            onEntryClick = {},
            isCompleted = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MealSectionEmptyPreview() {
    ChonkCheckTheme {
        MealSection(
            mealType = MealType.LUNCH,
            entries = emptyList(),
            onAddFood = {},
            onEntryClick = {},
            isCompleted = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MealSectionCompletedPreview() {
    ChonkCheckTheme {
        MealSection(
            mealType = MealType.DINNER,
            entries = listOf(
                DiaryEntry(
                    id = "1",
                    userId = "user1",
                    date = LocalDate.now(),
                    mealType = MealType.DINNER,
                    foodId = "food1",
                    recipeId = null,
                    servingSize = 200.0,
                    servingUnit = ServingUnit.GRAM,
                    numberOfServings = 1.0,
                    calories = 450.0,
                    protein = 35.0,
                    carbs = 40.0,
                    fat = 15.0,
                    name = "Grilled Salmon with Rice",
                    brand = null,
                    createdAt = System.currentTimeMillis()
                )
            ),
            onAddFood = {},
            onEntryClick = {},
            isCompleted = true,
            modifier = Modifier.padding(16.dp)
        )
    }
}
