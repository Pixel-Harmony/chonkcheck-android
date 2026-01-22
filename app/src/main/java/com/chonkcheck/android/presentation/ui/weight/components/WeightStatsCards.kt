package com.chonkcheck.android.presentation.ui.weight.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.domain.model.WeightStats
import com.chonkcheck.android.domain.model.WeightTrend
import com.chonkcheck.android.domain.model.WeightUnit
import com.chonkcheck.android.ui.theme.Amber
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen
import com.chonkcheck.android.ui.theme.Purple

@Composable
fun WeightStatsCards(
    stats: WeightStats?,
    weightUnit: WeightUnit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label = "Starting",
            value = stats?.startingWeight?.let {
                WeightUnitConverter.formatWeight(it, weightUnit)
            } ?: "--",
            backgroundColor = Purple,
            modifier = Modifier.weight(1f)
        )

        StatCard(
            label = "Current",
            value = stats?.currentWeight?.let {
                WeightUnitConverter.formatWeight(it, weightUnit)
            } ?: "--",
            backgroundColor = ChonkGreen,
            modifier = Modifier.weight(1f)
        )

        StatCard(
            label = "Change",
            value = stats?.totalChange?.let {
                WeightUnitConverter.formatChange(it, weightUnit)
            } ?: "--",
            backgroundColor = Amber,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WeightStatsCardsPreview() {
    ChonkCheckTheme {
        WeightStatsCards(
            stats = WeightStats(
                startingWeight = 80.0,
                currentWeight = 75.5,
                totalChange = -4.5,
                trend = WeightTrend.LOSING
            ),
            weightUnit = WeightUnit.KG,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeightStatsCardsEmptyPreview() {
    ChonkCheckTheme {
        WeightStatsCards(
            stats = null,
            weightUnit = WeightUnit.KG,
            modifier = Modifier.padding(16.dp)
        )
    }
}
