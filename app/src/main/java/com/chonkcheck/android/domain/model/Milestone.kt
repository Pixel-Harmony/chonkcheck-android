package com.chonkcheck.android.domain.model

import java.time.LocalDate

/**
 * Type of milestone celebration.
 */
enum class MilestoneType {
    WEEKLY,
    MONTHLY;

    fun toApiValue(): String = name.lowercase()

    companion object {
        fun fromApiValue(value: String): MilestoneType = when (value.lowercase()) {
            "weekly" -> WEEKLY
            "monthly" -> MONTHLY
            else -> throw IllegalArgumentException("Unknown milestone type: $value")
        }
    }
}

/**
 * Outcome of a milestone period.
 */
enum class MilestoneOutcome {
    LOST,
    MAINTAINED,
    GAINED;

    companion object {
        fun fromApiValue(value: String): MilestoneOutcome = when (value.lowercase()) {
            "lost" -> LOST
            "maintained" -> MAINTAINED
            "gained" -> GAINED
            else -> throw IllegalArgumentException("Unknown milestone outcome: $value")
        }
    }
}

/**
 * Data for a milestone celebration.
 */
data class MilestoneData(
    val type: MilestoneType,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val periodLabel: String,
    val startWeight: Double,
    val endWeight: Double,
    val change: Double,
    val changeFormatted: String,
    val totalLost: Double,
    val totalLostFormatted: String,
    val outcome: MilestoneOutcome
)

/**
 * Response containing pending milestones.
 */
data class PendingMilestones(
    val weekly: MilestoneData?,
    val monthly: MilestoneData?
) {
    /**
     * Get the most significant pending milestone.
     * Monthly takes priority over weekly.
     */
    fun getNextMilestone(): MilestoneData? = monthly ?: weekly
}
