package com.chonkcheck.android.presentation.ui.exercise

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chonkcheck.android.presentation.ui.components.LoadingIndicator
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.Coral
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ExerciseFormScreen(
    onNavigateBack: () -> Unit,
    onExerciseSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExerciseFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()

    BackHandler(enabled = true) {
        viewModel.onBackPressed()
    }

    LaunchedEffect(event) {
        when (event) {
            is ExerciseFormEvent.NavigateBack -> {
                onNavigateBack()
                viewModel.onEventConsumed()
            }
            is ExerciseFormEvent.ExerciseSaved -> {
                onExerciseSaved()
                viewModel.onEventConsumed()
            }
            is ExerciseFormEvent.ShowError -> {
                viewModel.onEventConsumed()
            }
            null -> {}
        }
    }

    ExerciseFormScreenContent(
        uiState = uiState,
        onBackClick = viewModel::onBackPressed,
        onNameChange = viewModel::updateName,
        onCaloriesChange = viewModel::updateCaloriesBurned,
        onDescriptionChange = viewModel::updateDescription,
        onSaveClick = viewModel::saveExercise,
        onDismissUnsavedChanges = viewModel::dismissUnsavedChangesDialog,
        onDiscardChanges = viewModel::discardChanges,
        modifier = modifier
    )
}

@Composable
fun ExerciseFormScreenContent(
    uiState: ExerciseFormUiState,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDismissUnsavedChanges: () -> Unit,
    onDiscardChanges: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingIndicator()
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = if (uiState.isEditMode) "Edit Exercise" else "Add Exercise",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Date display
        Text(
            text = uiState.date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 48.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Form content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name field
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text("Name") },
                placeholder = { Text("e.g., Running, Swimming, Yoga") },
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Coral,
                    focusedLabelColor = Coral,
                    cursorColor = Coral
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Calories field
            OutlinedTextField(
                value = uiState.caloriesBurned,
                onValueChange = onCaloriesChange,
                label = { Text("Calories Burned") },
                placeholder = { Text("e.g., 300") },
                isError = uiState.caloriesError != null,
                supportingText = uiState.caloriesError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Coral,
                    focusedLabelColor = Coral,
                    cursorColor = Coral
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Description field
            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = { Text("Description (optional)") },
                placeholder = { Text("e.g., 30 minutes, felt great afterwards") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Coral,
                    focusedLabelColor = Coral,
                    cursorColor = Coral
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Bottom buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onBackClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = onSaveClick,
                enabled = !uiState.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Coral
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(if (uiState.isEditMode) "Save Changes" else "Add Exercise")
            }
        }
    }

    // Unsaved changes dialog
    if (uiState.showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = onDismissUnsavedChanges,
            title = {
                Text(
                    text = "Discard changes?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            },
            text = {
                Text(
                    text = "You have unsaved changes. Are you sure you want to discard them?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = onDiscardChanges,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissUnsavedChanges) {
                    Text("Keep editing")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseFormScreenPreview() {
    ChonkCheckTheme {
        ExerciseFormScreenContent(
            uiState = ExerciseFormUiState(
                date = LocalDate.now(),
                name = "Running",
                caloriesBurned = "350",
                description = "Morning jog"
            ),
            onBackClick = {},
            onNameChange = {},
            onCaloriesChange = {},
            onDescriptionChange = {},
            onSaveClick = {},
            onDismissUnsavedChanges = {},
            onDiscardChanges = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseFormScreenEmptyPreview() {
    ChonkCheckTheme {
        ExerciseFormScreenContent(
            uiState = ExerciseFormUiState(
                date = LocalDate.now()
            ),
            onBackClick = {},
            onNameChange = {},
            onCaloriesChange = {},
            onDescriptionChange = {},
            onSaveClick = {},
            onDismissUnsavedChanges = {},
            onDiscardChanges = {}
        )
    }
}
