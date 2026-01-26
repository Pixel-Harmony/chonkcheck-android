package com.chonkcheck.android.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.domain.model.SyncStatus
import com.chonkcheck.android.ui.theme.ChonkGreen
import com.chonkcheck.android.ui.theme.Coral
import kotlinx.coroutines.delay

/**
 * Displays the current sync status with appropriate icon and animation.
 *
 * @param syncStatus The current sync status
 * @param onErrorClick Callback when error indicator is clicked (to show details)
 * @param modifier Modifier for styling
 */
@Composable
fun SyncStatusIndicator(
    syncStatus: SyncStatus,
    onErrorClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showSyncedState by remember { mutableStateOf(false) }

    // Auto-hide synced state after 3 seconds
    LaunchedEffect(syncStatus) {
        if (syncStatus is SyncStatus.Synced) {
            showSyncedState = true
            delay(3000)
            showSyncedState = false
        }
    }

    val isVisible = when (syncStatus) {
        is SyncStatus.Idle -> false
        is SyncStatus.Syncing -> true
        is SyncStatus.Synced -> showSyncedState
        is SyncStatus.Error -> true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .background(
                    color = when (syncStatus) {
                        is SyncStatus.Error -> Coral.copy(alpha = 0.1f)
                        is SyncStatus.Synced -> ChonkGreen.copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .then(
                    if (syncStatus is SyncStatus.Error && onErrorClick != null) {
                        Modifier.clickable { onErrorClick() }
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            when (syncStatus) {
                is SyncStatus.Idle -> {
                    // Hidden
                }
                is SyncStatus.Syncing -> {
                    SyncingIndicator(pendingCount = syncStatus.pendingCount)
                }
                is SyncStatus.Synced -> {
                    SyncedIndicator()
                }
                is SyncStatus.Error -> {
                    ErrorIndicator(failedCount = syncStatus.failedCount)
                }
            }
        }
    }
}

@Composable
private fun SyncingIndicator(pendingCount: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Sync,
            contentDescription = "Syncing",
            modifier = Modifier
                .size(16.dp)
                .rotate(rotation),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = if (pendingCount > 1) "Syncing $pendingCount items" else "Syncing",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SyncedIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Synced",
            modifier = Modifier.size(16.dp),
            tint = ChonkGreen
        )
        Text(
            text = "Synced",
            style = MaterialTheme.typography.labelSmall,
            color = ChonkGreen
        )
    }
}

@Composable
private fun ErrorIndicator(failedCount: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Sync error",
            modifier = Modifier.size(16.dp),
            tint = Coral
        )
        Text(
            text = if (failedCount > 1) "$failedCount items failed to sync" else "Sync failed",
            style = MaterialTheme.typography.labelSmall,
            color = Coral
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SyncingPreview() {
    SyncStatusIndicator(syncStatus = SyncStatus.Syncing(5))
}

@Preview(showBackground = true)
@Composable
private fun SyncedPreview() {
    SyncStatusIndicator(syncStatus = SyncStatus.Synced)
}

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() {
    SyncStatusIndicator(
        syncStatus = SyncStatus.Error(3, "Network error"),
        onErrorClick = {}
    )
}
