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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.domain.model.DiaryEntry
import com.chonkcheck.android.domain.model.DiaryItemType
import com.chonkcheck.android.domain.model.MealType
import com.chonkcheck.android.domain.model.ServingUnit
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen
import com.chonkcheck.android.ui.theme.Coral
import com.chonkcheck.android.ui.theme.Purple
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun MealGroupCard(
    groupName: String,
    entries: List<DiaryEntry>,
    onDeleteClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val totalCalories = entries.sumOf { it.calories }.roundToInt()
    val totalProtein = entries.sumOf { it.protein }
    val totalCarbs = entries.sumOf { it.carbs }
    val totalFat = entries.sumOf { it.fat }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Name row with indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Group name
                    Text(
                        text = groupName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Purple indicator for meal groups
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Purple)
                    )
                }

                // Item count
                Text(
                    text = "${entries.size} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Macros row
                Text(
                    text = "P: ${totalProtein.formatMacro()}g · C: ${totalCarbs.formatMacro()}g · F: ${totalFat.formatMacro()}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Right content
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Total calories
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = totalCalories.toString(),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "cal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Delete button
                if (onDeleteClick != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete meal group",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Expanded content
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.padding(top = 8.dp)
            ) {
                entries.forEachIndexed { index, entry ->
                    MealGroupEntryRow(entry = entry)
                    if (index < entries.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MealGroupEntryRow(
    entry: DiaryEntry,
    modifier: Modifier = Modifier
) {
    val typeColor = when (entry.itemType) {
        DiaryItemType.FOOD -> Coral
        DiaryItemType.RECIPE -> ChonkGreen
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Name with type indicator
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(typeColor)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Entry name
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Serving info
            Text(
                text = formatQuantity(entry),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp)
            )

            // Macros
            Text(
                text = "P: ${entry.protein.formatMacro()}g · C: ${entry.carbs.formatMacro()}g · F: ${entry.fat.formatMacro()}g",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        // Calories
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${entry.calories.roundToInt()}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "cal",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatQuantity(entry: DiaryEntry): String {
    val amount = entry.enteredAmount ?: (entry.servingSize * entry.numberOfServings)

    return if (entry.numberOfServings == 1.0) {
        "${amount.formatServing()} ${entry.servingUnit.displayName}"
    } else {
        "${entry.numberOfServings.formatServing()} × ${entry.servingSize.formatServing()} ${entry.servingUnit.displayName}"
    }
}

private fun Double.formatServing(): String {
    return if (this == this.toLong().toDouble()) {
        this.toLong().toString()
    } else {
        String.format("%.1f", this)
    }
}

private fun Double.formatMacro(): String {
    return if (this == this.toLong().toDouble()) {
        this.toLong().toString()
    } else {
        String.format("%.1f", this)
    }
}

@Preview(showBackground = true)
@Composable
private fun MealGroupCardPreview() {
    ChonkCheckTheme {
        MealGroupCard(
            groupName = "My Breakfast Combo",
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
                    createdAt = System.currentTimeMillis(),
                    itemType = DiaryItemType.FOOD,
                    mealGroupId = "group1",
                    mealGroupName = "My Breakfast Combo"
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
                    createdAt = System.currentTimeMillis(),
                    itemType = DiaryItemType.FOOD,
                    mealGroupId = "group1",
                    mealGroupName = "My Breakfast Combo"
                )
            ),
            onDeleteClick = {},
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MealGroupCardNoDeletePreview() {
    ChonkCheckTheme {
        MealGroupCard(
            groupName = "Quick Lunch",
            entries = listOf(
                DiaryEntry(
                    id = "3",
                    userId = "user1",
                    date = LocalDate.now(),
                    mealType = MealType.LUNCH,
                    foodId = null,
                    recipeId = "recipe1",
                    servingSize = 200.0,
                    servingUnit = ServingUnit.GRAM,
                    numberOfServings = 1.0,
                    calories = 450.0,
                    protein = 35.0,
                    carbs = 40.0,
                    fat = 15.0,
                    name = "Grilled Salmon",
                    brand = null,
                    createdAt = System.currentTimeMillis(),
                    itemType = DiaryItemType.RECIPE,
                    mealGroupId = "group2",
                    mealGroupName = "Quick Lunch"
                )
            ),
            onDeleteClick = null,
            modifier = Modifier.padding(8.dp)
        )
    }
}
