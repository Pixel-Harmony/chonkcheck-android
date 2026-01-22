package com.chonkcheck.android.presentation.ui.weight.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.domain.model.WeightEntry
import com.chonkcheck.android.domain.model.WeightUnit
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun WeightEntryCard(
    entry: WeightEntry,
    weightUnit: WeightUnit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = entry.date.format(
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = WeightUnitConverter.formatWeight(entry.weight, weightUnit),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (!entry.notes.isNullOrBlank()) {
                    Text(
                        text = entry.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WeightEntryCardPreview() {
    ChonkCheckTheme {
        WeightEntryCard(
            entry = WeightEntry(
                id = "1",
                userId = "user1",
                date = LocalDate.now(),
                weight = 72.5,
                notes = null,
                createdAt = System.currentTimeMillis()
            ),
            weightUnit = WeightUnit.KG,
            onDeleteClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeightEntryCardWithNotesPreview() {
    ChonkCheckTheme {
        WeightEntryCard(
            entry = WeightEntry(
                id = "1",
                userId = "user1",
                date = LocalDate.now(),
                weight = 72.5,
                notes = "After workout",
                createdAt = System.currentTimeMillis()
            ),
            weightUnit = WeightUnit.LB,
            onDeleteClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
