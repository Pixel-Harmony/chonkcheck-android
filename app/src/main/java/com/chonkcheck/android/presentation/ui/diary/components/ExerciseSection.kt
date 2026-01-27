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
import com.chonkcheck.android.domain.model.Exercise
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.Coral
import java.time.LocalDate

@Composable
fun ExerciseSection(
    exercises: List<Exercise>,
    onAddExercise: () -> Unit,
    onExerciseClick: (Exercise) -> Unit,
    onDeleteClick: ((Exercise) -> Unit)?,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header row - "Exercise" and "+ Add" text
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Exercise",
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
                    color = Coral,
                    modifier = Modifier
                        .clickable(onClick = onAddExercise)
                        .padding(vertical = 4.dp)
                )
            }
        }

        // Content - exercises or empty state
        if (exercises.isEmpty()) {
            Text(
                text = "No items",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            exercises.forEach { exercise ->
                ExerciseEntryCard(
                    exercise = exercise,
                    onClick = { onExerciseClick(exercise) },
                    onDeleteClick = onDeleteClick?.let { { it(exercise) } }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseSectionWithEntriesPreview() {
    ChonkCheckTheme {
        ExerciseSection(
            exercises = listOf(
                Exercise(
                    id = "1",
                    userId = "user1",
                    date = LocalDate.now(),
                    name = "Running",
                    caloriesBurned = 350.0,
                    description = "30 minutes morning run",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                ),
                Exercise(
                    id = "2",
                    userId = "user1",
                    date = LocalDate.now(),
                    name = "Weight Training",
                    caloriesBurned = 200.0,
                    description = null,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            ),
            onAddExercise = {},
            onExerciseClick = {},
            onDeleteClick = {},
            isCompleted = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseSectionEmptyPreview() {
    ChonkCheckTheme {
        ExerciseSection(
            exercises = emptyList(),
            onAddExercise = {},
            onExerciseClick = {},
            onDeleteClick = {},
            isCompleted = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseSectionCompletedPreview() {
    ChonkCheckTheme {
        ExerciseSection(
            exercises = listOf(
                Exercise(
                    id = "1",
                    userId = "user1",
                    date = LocalDate.now(),
                    name = "Swimming",
                    caloriesBurned = 450.0,
                    description = "1 hour at the pool",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            ),
            onAddExercise = {},
            onExerciseClick = {},
            onDeleteClick = null,
            isCompleted = true,
            modifier = Modifier.padding(16.dp)
        )
    }
}
