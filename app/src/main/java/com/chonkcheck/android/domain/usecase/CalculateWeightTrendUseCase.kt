package com.chonkcheck.android.domain.usecase

import com.chonkcheck.android.domain.model.WeightChartPoint
import com.chonkcheck.android.domain.model.WeightEntry
import com.chonkcheck.android.domain.model.WeightStats
import com.chonkcheck.android.domain.model.WeightTrend
import com.chonkcheck.android.domain.model.WeightTrendPrediction
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class CalculateWeightTrendUseCase @Inject constructor() {

    fun calculateStats(entries: List<WeightEntry>): WeightStats {
        if (entries.isEmpty()) {
            return WeightStats(
                startingWeight = null,
                currentWeight = null,
                totalChange = null,
                trend = WeightTrend.UNKNOWN
            )
        }

        val sortedEntries = entries.sortedBy { it.date }
        val startingWeight = sortedEntries.first().weight
        val currentWeight = sortedEntries.last().weight
        val totalChange = currentWeight - startingWeight

        val trend = calculateTrend(sortedEntries)

        return WeightStats(
            startingWeight = startingWeight,
            currentWeight = currentWeight,
            totalChange = totalChange,
            trend = trend
        )
    }

    fun calculateTrendPrediction(entries: List<WeightEntry>): WeightTrendPrediction? {
        if (entries.size < MIN_ENTRIES_FOR_TREND) {
            return null
        }

        val sortedEntries = entries.sortedBy { it.date }
        val regression = calculateLinearRegression(sortedEntries)

        val ratePerWeek = regression.slope * DAYS_PER_WEEK
        val trend = determineTrend(ratePerWeek)

        // Generate full trend line from start to 4 weeks past the last entry
        val firstEntry = sortedEntries.first()
        val lastEntry = sortedEntries.last()
        val trendLinePoints = generateFullTrendLine(firstEntry.date, lastEntry.date, regression, PROJECTION_WEEKS)

        return WeightTrendPrediction(
            ratePerWeek = ratePerWeek,
            trend = trend,
            projectedPoints = trendLinePoints
        )
    }

    fun generateChartData(
        entries: List<WeightEntry>,
        prediction: WeightTrendPrediction?
    ): List<WeightChartPoint> {
        val actualPoints = entries
            .sortedBy { it.date }
            .map { WeightChartPoint(it.date, it.weight, isTrend = false) }

        val trendPoints = prediction?.projectedPoints ?: emptyList()

        return actualPoints + trendPoints
    }

    private fun generateFullTrendLine(
        startDate: LocalDate,
        lastActualDate: LocalDate,
        regression: LinearRegression,
        weeksAhead: Int
    ): List<WeightChartPoint> {
        val points = mutableListOf<WeightChartPoint>()
        val endDate = lastActualDate.plusDays((weeksAhead * DAYS_PER_WEEK).toLong())
        val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toInt()

        // Generate points along the trend line (start, end, and a few in between)
        val pointDates = listOf(
            startDate,
            endDate
        )

        pointDates.forEach { date ->
            val x = ChronoUnit.DAYS.between(regression.startDate, date).toDouble()
            val weight = regression.intercept + regression.slope * x

            points.add(
                WeightChartPoint(
                    date = date,
                    weight = weight,
                    isTrend = true
                )
            )
        }

        return points.sortedBy { it.date }
    }

    private fun calculateTrend(entries: List<WeightEntry>): WeightTrend {
        if (entries.size < MIN_ENTRIES_FOR_TREND) {
            return WeightTrend.UNKNOWN
        }

        val regression = calculateLinearRegression(entries)
        val ratePerWeek = regression.slope * DAYS_PER_WEEK

        return determineTrend(ratePerWeek)
    }

    private fun determineTrend(ratePerWeek: Double): WeightTrend {
        return when {
            ratePerWeek < -MAINTAINING_THRESHOLD -> WeightTrend.LOSING
            ratePerWeek > MAINTAINING_THRESHOLD -> WeightTrend.GAINING
            else -> WeightTrend.MAINTAINING
        }
    }

    private fun calculateLinearRegression(entries: List<WeightEntry>): LinearRegression {
        val firstDate = entries.first().date
        val n = entries.size.toDouble()

        var sumX = 0.0
        var sumY = 0.0
        var sumXY = 0.0
        var sumX2 = 0.0

        entries.forEach { entry ->
            val x = ChronoUnit.DAYS.between(firstDate, entry.date).toDouble()
            val y = entry.weight

            sumX += x
            sumY += y
            sumXY += x * y
            sumX2 += x * x
        }

        val slope = if (n * sumX2 - sumX * sumX != 0.0) {
            (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
        } else {
            0.0
        }

        val intercept = (sumY - slope * sumX) / n

        return LinearRegression(slope, intercept, firstDate)
    }

    private fun generateProjectedPoints(
        startDate: LocalDate,
        regression: LinearRegression,
        weeks: Int
    ): List<WeightChartPoint> {
        val points = mutableListOf<WeightChartPoint>()
        val daysFromRegressionStart = ChronoUnit.DAYS.between(regression.startDate, startDate)

        for (week in 1..weeks) {
            val daysAhead = week * DAYS_PER_WEEK
            val projectedDate = startDate.plusDays(daysAhead.toLong())
            val x = daysFromRegressionStart + daysAhead
            val projectedWeight = regression.intercept + regression.slope * x

            points.add(
                WeightChartPoint(
                    date = projectedDate,
                    weight = projectedWeight,
                    isTrend = true
                )
            )
        }

        return points
    }

    private data class LinearRegression(
        val slope: Double,
        val intercept: Double,
        val startDate: LocalDate
    )

    companion object {
        private const val MIN_ENTRIES_FOR_TREND = 3
        private const val MAINTAINING_THRESHOLD = 0.05 // kg per week
        private const val DAYS_PER_WEEK = 7
        private const val PROJECTION_WEEKS = 4
    }
}
