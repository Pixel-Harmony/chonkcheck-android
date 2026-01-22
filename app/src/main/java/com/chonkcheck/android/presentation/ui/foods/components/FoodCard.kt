package com.chonkcheck.android.presentation.ui.foods.components

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chonkcheck.android.domain.model.Food
import com.chonkcheck.android.domain.model.FoodType
import com.chonkcheck.android.domain.model.ServingUnit
import com.chonkcheck.android.ui.theme.Amber
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen
import com.chonkcheck.android.ui.theme.Coral
import com.chonkcheck.android.ui.theme.Teal

// Badge colors
private val TealBadgeBackground = Color(0xFFCCFBF1)
private val TealBadgeBackgroundDark = Color(0xFF134E4A)
private val TealBadgeText = Color(0xFF0F766E)
private val TealBadgeTextDark = Color(0xFF5EEAD4)

private val CyanBadgeBackground = Color(0xFFCFFAFE)
private val CyanBadgeBackgroundDark = Color(0xFF164E63)
private val CyanBadgeText = Color(0xFF0E7490)
private val CyanBadgeTextDark = Color(0xFF67E8F9)

private val AmberBadgeBackground = Color(0xFFFEF3C7)
private val AmberBadgeBackgroundDark = Color(0xFF78350F)
private val AmberBadgeText = Color(0xFFB45309)
private val AmberBadgeTextDark = Color(0xFFFCD34D)

@Composable
fun FoodCard(
    food: Food,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Name row with badges and indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (food.type == FoodType.PLATFORM) {
                    FoodBadge(
                        text = "Platform",
                        backgroundColor = TealBadgeBackground,
                        textColor = TealBadgeText
                    )
                }

                if (food.overrideOf != null) {
                    FoodBadge(
                        text = "Revised",
                        backgroundColor = CyanBadgeBackground,
                        textColor = CyanBadgeText
                    )
                }

                if (food.promotionRequested) {
                    FoodBadge(
                        text = "Review",
                        backgroundColor = AmberBadgeBackground,
                        textColor = AmberBadgeText
                    )
                }

                // Orange indicator dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Coral)
                )
            }

            // Brand
            if (!food.brand.isNullOrBlank()) {
                Text(
                    text = food.brand,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Serving info
            Text(
                text = "${food.servingSize.formatServing()} ${food.servingUnit.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Macros
            Text(
                text = "P: ${food.protein.formatMacro()}g · C: ${food.carbs.formatMacro()}g · F: ${food.fat.formatMacro()}g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Right content - calories and delete
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Calories
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = food.calories.toInt().toString(),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "cal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Delete button (only for user foods)
            if (food.type == FoodType.USER && onDelete != null) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete food",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FoodBadge(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        fontSize = 10.sp
    )
}

private fun Double.formatServing(): String {
    return if (this == this.toLong().toDouble()) {
        this.toLong().toString()
    } else {
        this.toString()
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
private fun FoodCardPreview() {
    ChonkCheckTheme {
        FoodCard(
            food = Food(
                id = "1",
                name = "Chicken Breast",
                brand = "Tesco",
                barcode = null,
                servingSize = 100.0,
                servingUnit = ServingUnit.GRAM,
                servingsPerContainer = null,
                calories = 165.0,
                protein = 31.0,
                carbs = 0.0,
                fat = 3.6,
                fiber = null,
                sugar = null,
                sodium = null,
                saturatedFat = null,
                transFat = null,
                cholesterol = null,
                addedSugar = null,
                vitaminA = null,
                vitaminC = null,
                vitaminD = null,
                calcium = null,
                iron = null,
                potassium = null,
                type = FoodType.PLATFORM,
                source = null,
                verified = true,
                promotionRequested = false,
                overrideOf = null,
                imageUrl = null,
                createdAt = System.currentTimeMillis()
            ),
            onClick = {},
            onDelete = null
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FoodCardUserFoodPreview() {
    ChonkCheckTheme {
        FoodCard(
            food = Food(
                id = "2",
                name = "My Custom Meal Prep",
                brand = null,
                barcode = "1234567890",
                servingSize = 250.0,
                servingUnit = ServingUnit.GRAM,
                servingsPerContainer = null,
                calories = 450.0,
                protein = 35.0,
                carbs = 40.0,
                fat = 15.0,
                fiber = 5.0,
                sugar = 3.0,
                sodium = 500.0,
                saturatedFat = null,
                transFat = null,
                cholesterol = null,
                addedSugar = null,
                vitaminA = null,
                vitaminC = null,
                vitaminD = null,
                calcium = null,
                iron = null,
                potassium = null,
                type = FoodType.USER,
                source = null,
                verified = false,
                promotionRequested = true,
                overrideOf = null,
                imageUrl = null,
                createdAt = System.currentTimeMillis()
            ),
            onClick = {},
            onDelete = {}
        )
    }
}
