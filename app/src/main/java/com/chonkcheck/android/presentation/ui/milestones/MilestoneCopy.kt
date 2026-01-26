package com.chonkcheck.android.presentation.ui.milestones

import com.chonkcheck.android.domain.model.MilestoneOutcome
import com.chonkcheck.android.domain.model.MilestoneType

/**
 * Copy content for milestone celebrations.
 */
object MilestoneCopy {

    /**
     * Get the header text for a milestone.
     */
    fun getHeader(type: MilestoneType, outcome: MilestoneOutcome): String {
        return when (type) {
            MilestoneType.WEEKLY -> when (outcome) {
                MilestoneOutcome.LOST -> "Your week"
                MilestoneOutcome.MAINTAINED -> "Your week"
                MilestoneOutcome.GAINED -> "Your week"
            }
            MilestoneType.MONTHLY -> when (outcome) {
                MilestoneOutcome.LOST -> "Your month"
                MilestoneOutcome.MAINTAINED -> "Your month"
                MilestoneOutcome.GAINED -> "Your month"
            }
        }
    }

    /**
     * Get the celebration emoji for the outcome.
     */
    fun getEmoji(outcome: MilestoneOutcome): String {
        return when (outcome) {
            MilestoneOutcome.LOST -> "\uD83C\uDF89" // Party popper
            MilestoneOutcome.MAINTAINED -> "\u2696\uFE0F" // Scales
            MilestoneOutcome.GAINED -> "\uD83D\uDCC8" // Chart with upwards trend
        }
    }

    /**
     * Get the title text for the outcome.
     */
    fun getTitle(outcome: MilestoneOutcome): String {
        return when (outcome) {
            MilestoneOutcome.LOST -> "Smashing it!"
            MilestoneOutcome.MAINTAINED -> "Steady as she goes"
            MilestoneOutcome.GAINED -> "Keep going"
        }
    }

    /**
     * Get the subtitle/description for the outcome.
     */
    fun getSubtitle(outcome: MilestoneOutcome, changeFormatted: String): String {
        return when (outcome) {
            MilestoneOutcome.LOST -> "You lost $changeFormatted this period. Great work!"
            MilestoneOutcome.MAINTAINED -> "You maintained your weight. Consistency is key!"
            MilestoneOutcome.GAINED -> "You gained $changeFormatted this period. Don't worry, every journey has ups and downs."
        }
    }

    /**
     * Get the total lost message (only shown for LOST outcome).
     */
    fun getTotalLostMessage(totalLostFormatted: String): String {
        return "Total lost so far: $totalLostFormatted"
    }

    /**
     * Get the dismiss button text.
     */
    fun getDismissButtonText(): String = "Continue"
}
