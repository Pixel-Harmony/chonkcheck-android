package com.chonkcheck.android.presentation.ui.foods.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.domain.model.FoodFilterType
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.Coral

@Composable
fun FoodFilterChips(
    selectedFilter: FoodFilterType,
    onFilterSelected: (FoodFilterType) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Coral
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            text = "All Foods",
            isSelected = selectedFilter == FoodFilterType.ALL,
            onClick = { onFilterSelected(FoodFilterType.ALL) },
            accentColor = accentColor,
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            text = "My Foods",
            isSelected = selectedFilter == FoodFilterType.USER,
            onClick = { onFilterSelected(FoodFilterType.USER) },
            accentColor = accentColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) accentColor else MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp,
            pressedElevation = 6.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FoodFilterChipsAllSelectedPreview() {
    ChonkCheckTheme {
        FoodFilterChips(
            selectedFilter = FoodFilterType.ALL,
            onFilterSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FoodFilterChipsUserSelectedPreview() {
    ChonkCheckTheme {
        FoodFilterChips(
            selectedFilter = FoodFilterType.USER,
            onFilterSelected = {}
        )
    }
}
