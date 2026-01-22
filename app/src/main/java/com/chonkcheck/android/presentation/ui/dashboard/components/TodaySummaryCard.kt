package com.chonkcheck.android.presentation.ui.dashboard.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.Coral

@Composable
fun TodaySummaryCard(
    calories: Int,
    entryCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Coral
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Today",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            if (entryCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    StatColumn(
                        value = formatNumber(calories),
                        label = "Cal"
                    )
                    StatColumn(
                        value = entryCount.toString(),
                        label = if (entryCount == 1) "Item" else "Items"
                    )
                }
            } else {
                Text(
                    text = "Nothing logged yet",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun StatColumn(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 12.sp
        )
    }
}

private fun formatNumber(value: Int): String {
    return when {
        value >= 1000 -> String.format("%,d", value)
        else -> value.toString()
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TodaySummaryCardPreview() {
    ChonkCheckTheme {
        TodaySummaryCard(
            calories = 1850,
            entryCount = 7,
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodaySummaryCardEmptyPreview() {
    ChonkCheckTheme {
        TodaySummaryCard(
            calories = 0,
            entryCount = 0,
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodaySummaryCardSingleItemPreview() {
    ChonkCheckTheme {
        TodaySummaryCard(
            calories = 350,
            entryCount = 1,
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
