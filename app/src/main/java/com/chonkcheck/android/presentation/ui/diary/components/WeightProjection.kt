package com.chonkcheck.android.presentation.ui.diary.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.domain.model.WeightUnit
import com.chonkcheck.android.presentation.ui.weight.components.WeightUnitConverter
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen
import com.chonkcheck.android.ui.theme.Coral
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Data class for weight projection calculation
 */
data class WeightProjectionData(
    val tdee: Int,
    val todayCalories: Double,
    val currentWeightKg: Double,
    val weightUnit: WeightUnit = WeightUnit.KG
) {
    val dailyDeficit: Double = tdee - todayCalories
    val weeklyDeficitKg: Double = (dailyDeficit * 7) / 7700 // 7700 cal = 1kg
    val projectedWeightChangeKg: Double = weeklyDeficitKg * 5 // 5 weeks
    val projectedWeightKg: Double = currentWeightKg - projectedWeightChangeKg

    val isDeficit: Boolean = dailyDeficit > 0
    val isSurplus: Boolean = dailyDeficit < 0
    val isMaintenance: Boolean = dailyDeficit.absoluteValue < 100 // Within 100 cal
}

@Composable
fun WeightProjection(
    data: WeightProjectionData,
    modifier: Modifier = Modifier
) {
    val deficitText = when {
        data.isMaintenance -> "Maintenance"
        data.isDeficit -> "${data.dailyDeficit.roundToInt()} cal deficit"
        else -> "${data.dailyDeficit.absoluteValue.roundToInt()} cal surplus"
    }

    val projectionColor = when {
        data.isMaintenance -> MaterialTheme.colorScheme.onSurface
        data.isDeficit -> ChonkGreen
        else -> Coral
    }

    val projectedWeight = WeightUnitConverter.formatWeight(data.projectedWeightKg, data.weightUnit)
    val currentWeight = WeightUnitConverter.formatWeight(data.currentWeightKg, data.weightUnit)
    val weightChange = WeightUnitConverter.formatWeight(data.projectedWeightChangeKg.absoluteValue, data.weightUnit)

    val changeDirection = when {
        data.isMaintenance -> "stay at"
        data.isDeficit -> "lose"
        else -> "gain"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Text(
                text = "If every day was like today...",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Main projection
            Text(
                text = "In 5 weeks you would weigh $projectedWeight",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = projectionColor
            )

            if (!data.isMaintenance) {
                Text(
                    text = "You would $changeDirection $weightChange",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "TDEE",
                    value = "${data.tdee}"
                )
                StatItem(
                    label = "Today",
                    value = "${data.todayCalories.roundToInt()}"
                )
                StatItem(
                    label = "Daily",
                    value = deficitText,
                    valueColor = projectionColor
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeightProjectionDeficitPreview() {
    ChonkCheckTheme {
        WeightProjection(
            data = WeightProjectionData(
                tdee = 2400,
                todayCalories = 1800.0,
                currentWeightKg = 80.0,
                weightUnit = WeightUnit.KG
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeightProjectionSurplusPreview() {
    ChonkCheckTheme {
        WeightProjection(
            data = WeightProjectionData(
                tdee = 2400,
                todayCalories = 2800.0,
                currentWeightKg = 70.0,
                weightUnit = WeightUnit.KG
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeightProjectionMaintenancePreview() {
    ChonkCheckTheme {
        WeightProjection(
            data = WeightProjectionData(
                tdee = 2400,
                todayCalories = 2380.0,
                currentWeightKg = 75.0,
                weightUnit = WeightUnit.KG
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
