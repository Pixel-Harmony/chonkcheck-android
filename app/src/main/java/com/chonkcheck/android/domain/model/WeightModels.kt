package com.chonkcheck.android.domain.model

import java.time.LocalDate

data class WeightEntry(
    val id: String,
    val userId: String,
    val date: LocalDate,
    val weight: Double, // Always in kg
    val notes: String?,
    val createdAt: Long
)

data class WeightStats(
    val startingWeight: Double?, // kg
    val currentWeight: Double?, // kg
    val totalChange: Double?, // kg (positive = gained, negative = lost)
    val trend: WeightTrend
)

enum class WeightTrend {
    LOSING,
    MAINTAINING,
    GAINING,
    UNKNOWN
}

data class CreateWeightParams(
    val weight: Double, // kg
    val date: LocalDate,
    val notes: String? = null
)

data class WeightChartPoint(
    val date: LocalDate,
    val weight: Double, // kg
    val isTrend: Boolean = false
)

data class WeightTrendPrediction(
    val ratePerWeek: Double, // kg per week
    val trend: WeightTrend,
    val projectedPoints: List<WeightChartPoint>
)
