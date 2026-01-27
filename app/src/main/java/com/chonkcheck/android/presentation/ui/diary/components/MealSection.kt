package com.chonkcheck.android.presentation.ui.diary.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.chonkcheck.android.ui.theme.Coral
import com.chonkcheck.android.ui.theme.Purple
import java.time.LocalDate

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
    // Different color for each meal type's "+ Add" button
    val addButtonColor = when (mealType) {
        MealType.BREAKFAST -> Coral
        MealType.LUNCH -> Coral
        MealType.DINNER -> ChonkGreen
        MealType.SNACKS -> Purple
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header row - meal type name and "+ Add" text
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = mealType.displayName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            // "+ Add" text button (only show if not completed)
            if (!isCompleted) {
                Text(
                    text = "+ Add",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = addButtonColor,
                    modifier = Modifier
                        .clickable(onClick = onAddFood)
                        .padding(vertical = 4.dp)
                )
            }
        }

        // Content - entries or empty state
        if (entries.isEmpty()) {
            Text(
                text = "No items",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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

            // Render items as individual cards
            displayItems.forEach { item ->
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
