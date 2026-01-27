package com.chonkcheck.android.presentation.ui.diary.components

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
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.domain.model.Exercise
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.Coral
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun ExerciseEntryCard(
    exercise: Exercise,
    onClick: () -> Unit,
    onDeleteClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!exercise.description.isNullOrBlank()) {
                    Text(
                        text = exercise.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "-${exercise.caloriesBurned.roundToInt()}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Coral
                )
                Text(
                    text = "cal",
                    style = MaterialTheme.typography.bodySmall,
                    color = Coral.copy(alpha = 0.8f)
                )
            }

            // Delete button (only show if onDeleteClick is provided)
            if (onDeleteClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete exercise",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseEntryCardPreview() {
    ChonkCheckTheme {
        ExerciseEntryCard(
            exercise = Exercise(
                id = "1",
                userId = "user1",
                date = LocalDate.now(),
                name = "Running",
                caloriesBurned = 350.0,
                description = "30 minutes, felt great afterwards",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            onClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseEntryCardNoDeletePreview() {
    ChonkCheckTheme {
        ExerciseEntryCard(
            exercise = Exercise(
                id = "2",
                userId = "user1",
                date = LocalDate.now(),
                name = "Swimming",
                caloriesBurned = 500.0,
                description = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            onClick = {},
            onDeleteClick = null
        )
    }
}
