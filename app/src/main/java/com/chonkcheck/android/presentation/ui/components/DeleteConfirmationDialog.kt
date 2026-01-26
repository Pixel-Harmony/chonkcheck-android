package com.chonkcheck.android.presentation.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.chonkcheck.android.ui.theme.ChonkCheckTheme

/**
 * A reusable delete confirmation dialog.
 *
 * @param title The dialog title (e.g., "Delete food?")
 * @param message The confirmation message explaining what will happen
 * @param onConfirm Callback when user confirms deletion
 * @param onDismiss Callback when user dismisses/cancels the dialog
 * @param confirmText Text for the confirm button (default "Delete")
 * @param dismissText Text for the dismiss button (default "Cancel")
 */
@Composable
fun DeleteConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "Delete",
    dismissText: String = "Cancel"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun DeleteConfirmationDialogPreview() {
    ChonkCheckTheme {
        DeleteConfirmationDialog(
            title = "Delete food?",
            message = "This food will be removed from your list. Your past diary entries will remain intact.",
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DeleteRecipeDialogPreview() {
    ChonkCheckTheme {
        DeleteConfirmationDialog(
            title = "Delete recipe?",
            message = "This recipe will be removed. Your past diary entries using this recipe will remain intact.",
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DeleteMealDialogPreview() {
    ChonkCheckTheme {
        DeleteConfirmationDialog(
            title = "Delete meal?",
            message = "This meal will be removed. Your past diary entries using this meal will remain intact.",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
