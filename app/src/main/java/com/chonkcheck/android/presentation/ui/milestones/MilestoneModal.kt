package com.chonkcheck.android.presentation.ui.milestones

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.chonkcheck.android.domain.model.MilestoneData
import com.chonkcheck.android.domain.model.MilestoneOutcome
import com.chonkcheck.android.domain.model.MilestoneType
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen
import com.chonkcheck.android.ui.theme.Coral
import com.chonkcheck.android.ui.theme.Teal
import java.time.LocalDate

/**
 * Modal dialog for celebrating milestones.
 *
 * @param milestone The milestone data to display
 * @param onDismiss Callback when the user dismisses the modal
 */
@Composable
fun MilestoneModal(
    milestone: MilestoneData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfetti by remember { mutableStateOf(milestone.outcome == MilestoneOutcome.LOST) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            // Confetti effect for weight loss
            if (showConfetti) {
                ConfettiEffect(
                    milestoneType = milestone.type,
                    onAnimationEnd = { showConfetti = false }
                )
            }

            // Main card
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Text(
                        text = MilestoneCopy.getHeader(milestone.type, milestone.outcome),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Emoji
                    Text(
                        text = MilestoneCopy.getEmoji(milestone.outcome),
                        fontSize = 64.sp
                    )

                    // Title
                    Text(
                        text = MilestoneCopy.getTitle(milestone.outcome),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (milestone.outcome) {
                            MilestoneOutcome.LOST -> ChonkGreen
                            MilestoneOutcome.MAINTAINED -> Teal
                            MilestoneOutcome.GAINED -> Coral
                        }
                    )

                    // Period label
                    Text(
                        text = milestone.periodLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Weight change
                    val changePrefix = when (milestone.outcome) {
                        MilestoneOutcome.LOST -> "-"
                        MilestoneOutcome.MAINTAINED -> ""
                        MilestoneOutcome.GAINED -> "+"
                    }
                    Text(
                        text = "$changePrefix${milestone.changeFormatted}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = when (milestone.outcome) {
                            MilestoneOutcome.LOST -> ChonkGreen
                            MilestoneOutcome.MAINTAINED -> Teal
                            MilestoneOutcome.GAINED -> Coral
                        }
                    )

                    // Subtitle
                    Text(
                        text = MilestoneCopy.getSubtitle(milestone.outcome, milestone.changeFormatted),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Total lost (only for LOST outcome)
                    if (milestone.outcome == MilestoneOutcome.LOST && milestone.totalLost > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = MilestoneCopy.getTotalLostMessage(milestone.totalLostFormatted),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ChonkGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dismiss button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (milestone.outcome) {
                                MilestoneOutcome.LOST -> ChonkGreen
                                MilestoneOutcome.MAINTAINED -> Teal
                                MilestoneOutcome.GAINED -> Coral
                            }
                        )
                    ) {
                        Text(
                            text = MilestoneCopy.getDismissButtonText(),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun MilestoneModalLostPreview() {
    ChonkCheckTheme {
        MilestoneModal(
            milestone = MilestoneData(
                type = MilestoneType.WEEKLY,
                periodStart = LocalDate.now().minusDays(7),
                periodEnd = LocalDate.now(),
                periodLabel = "Jan 13 - Jan 20",
                startWeight = 80.0,
                endWeight = 79.2,
                change = 0.8,
                changeFormatted = "0.8 kg",
                totalLost = 5.2,
                totalLostFormatted = "5.2 kg",
                outcome = MilestoneOutcome.LOST
            ),
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun MilestoneModalMaintainedPreview() {
    ChonkCheckTheme {
        MilestoneModal(
            milestone = MilestoneData(
                type = MilestoneType.WEEKLY,
                periodStart = LocalDate.now().minusDays(7),
                periodEnd = LocalDate.now(),
                periodLabel = "Jan 13 - Jan 20",
                startWeight = 79.2,
                endWeight = 79.2,
                change = 0.0,
                changeFormatted = "0.0 kg",
                totalLost = 5.2,
                totalLostFormatted = "5.2 kg",
                outcome = MilestoneOutcome.MAINTAINED
            ),
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun MilestoneModalGainedPreview() {
    ChonkCheckTheme {
        MilestoneModal(
            milestone = MilestoneData(
                type = MilestoneType.MONTHLY,
                periodStart = LocalDate.now().minusMonths(1),
                periodEnd = LocalDate.now(),
                periodLabel = "December 2024",
                startWeight = 79.0,
                endWeight = 80.5,
                change = 1.5,
                changeFormatted = "1.5 kg",
                totalLost = 3.7,
                totalLostFormatted = "3.7 kg",
                outcome = MilestoneOutcome.GAINED
            ),
            onDismiss = {}
        )
    }
}
