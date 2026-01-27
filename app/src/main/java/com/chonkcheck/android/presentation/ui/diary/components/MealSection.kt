package com.chonkcheck.android.presentation.ui.diary.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.domain.model.DiaryEntry
import com.chonkcheck.android.domain.model.MealType
import com.chonkcheck.android.domain.model.ServingUnit
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen
import java.time.LocalDate
import kotlin.math.roundToInt

private sealed class MealDisplayItem {
    data class Group(val id: String, val name: String, val entries: List<DiaryEntry>) : MealDisplayItem()
    data class Single(val entry: DiaryEntry) : MealDisplayItem()
}

@Composable
fun MealSection(
    mealType: MealType,
    entries: List<DiaryEntry>,
    onAddFood: () -> Unit,
    onEntryClick: (DiaryEntry) -> Unit,
    onDeleteClick: ((DiaryEntry) -> Unit)?,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }

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
                    .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // Add button in header (only show if not completed)
                    if (!isCompleted) {
                        IconButton(
                            onClick = onAddFood,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add to ${mealType.displayName}",
                                tint = ChonkGreen
                            )
                        }
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
                        // Group entries by mealGroupId
                        val (groupedEntries, individualEntries) = entries.partition { it.mealGroupId != null }
                        val mealGroups = groupedEntries.groupBy { it.mealGroupId }

                        // Build list of items to display (meal groups + individual entries)
                        val displayItems = mutableListOf<MealDisplayItem>()

                        // Add meal groups
                        mealGroups.forEach { (groupId, groupEntries) ->
                            val groupName = groupEntries.firstOrNull()?.mealGroupName ?: "Saved Meal"
                            displayItems.add(MealDisplayItem.Group(groupId!!, groupName, groupEntries))
                        }

                        // Add individual entries
                        individualEntries.forEach { entry ->
                            displayItems.add(MealDisplayItem.Single(entry))
                        }

                        // Render items
                        displayItems.forEachIndexed { index, item ->
                            when (item) {
                                is MealDisplayItem.Group -> {
                                    MealGroupCard(
                                        groupName = item.name,
                                        entries = item.entries,
                                        onDeleteClick = onDeleteClick?.let {
                                            // Delete all entries in the group
                                            { item.entries.forEach { entry -> it(entry) } }
                                        }
                                    )
                                }
                                is MealDisplayItem.Single -> {
                                    DiaryEntryCard(
                                        entry = item.entry,
                                        onClick = { onEntryClick(item.entry) },
                                        onDeleteClick = onDeleteClick?.let { { it(item.entry) } }
                                    )
                                }
                            }
                            if (index < displayItems.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
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
            onDeleteClick = {},
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
            onDeleteClick = {},
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
            onDeleteClick = null,
            isCompleted = true,
            modifier = Modifier.padding(16.dp)
        )
    }
}
