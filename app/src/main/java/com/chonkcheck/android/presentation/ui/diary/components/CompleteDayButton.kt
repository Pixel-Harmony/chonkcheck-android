package com.chonkcheck.android.presentation.ui.diary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen
import com.chonkcheck.android.ui.theme.ChonkGreenDark
import com.chonkcheck.android.ui.theme.Coral

@Composable
fun CompleteDayButton(
    isCompleted: Boolean,
    isToday: Boolean,
    hasEntries: Boolean,
    onComplete: () -> Unit,
    onReopen: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Only show button if there are entries
    if (!hasEntries) return

    if (isCompleted) {
        // Reopen button (orange)
        Button(
            onClick = onReopen,
            modifier = modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Coral
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Reopen Day",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    } else {
        // Complete button (green gradient)
        GradientButton(
            onClick = onComplete,
            modifier = modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = true
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isToday) "Complete Day" else "Mark Complete",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(ChonkGreen, ChonkGreenDark)
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides Color.White
        ) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompleteDayButtonIncompletePreview() {
    ChonkCheckTheme {
        CompleteDayButton(
            isCompleted = false,
            isToday = true,
            hasEntries = true,
            onComplete = {},
            onReopen = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CompleteDayButtonCompletedPreview() {
    ChonkCheckTheme {
        CompleteDayButton(
            isCompleted = true,
            isToday = true,
            hasEntries = true,
            onComplete = {},
            onReopen = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CompleteDayButtonNoEntriesPreview() {
    ChonkCheckTheme {
        CompleteDayButton(
            isCompleted = false,
            isToday = true,
            hasEntries = false,
            onComplete = {},
            onReopen = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
