package com.chonkcheck.android.presentation.ui.weight.components

import android.content.res.Configuration
import android.graphics.DashPathEffect
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.domain.model.WeightChartPoint
import com.chonkcheck.android.domain.model.WeightUnit
import com.chonkcheck.android.ui.theme.ChonkCheckTheme
import com.chonkcheck.android.ui.theme.ChonkGreen
import com.chonkcheck.android.ui.theme.Coral
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToLong

private const val DAYS_TO_SHOW = 90

@Composable
fun WeightChart(
    chartData: List<WeightChartPoint>,
    weightUnit: WeightUnit,
    modifier: Modifier = Modifier
) {
    if (chartData.isEmpty()) return

    // Filter to last 90 days for actual points
    val cutoffDate = LocalDate.now().minusDays(DAYS_TO_SHOW.toLong())
    val filteredData = chartData.filter { it.date >= cutoffDate || it.isTrend }

    val actualPoints = filteredData.filter { !it.isTrend }.sortedBy { it.date }
    val trendPoints = filteredData.filter { it.isTrend }.sortedBy { it.date }

    if (actualPoints.isEmpty()) return

    val modelProducer = remember { CartesianChartModelProducer() }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d") }

    // Map dates to x values (days from first date)
    val firstDate = actualPoints.first().date
    val dateToX: (LocalDate) -> Float = { date ->
        java.time.temporal.ChronoUnit.DAYS.between(firstDate, date).toFloat()
    }

    val actualX = actualPoints.map { dateToX(it.date) }
    val actualY = actualPoints.map {
        WeightUnitConverter.kgToUnit(it.weight, weightUnit)
    }

    // Trend line as a separate regression line spanning the entire chart
    val hasTrendData = trendPoints.isNotEmpty()
    val trendX = trendPoints.map { dateToX(it.date) }
    val trendY = trendPoints.map { WeightUnitConverter.kgToUnit(it.weight, weightUnit) }

    // Calculate Y-axis range based on data
    val allYValues = actualY + trendY
    val dataMinY = allYValues.minOrNull() ?: 0.0
    val dataMaxY = allYValues.maxOrNull() ?: 100.0
    val padding = (dataMaxY - dataMinY).coerceAtLeast(1.0) * 0.15
    val yAxisMin = (dataMinY - padding).coerceAtLeast(0.0)
    val yAxisMax = dataMaxY + padding

    val rangeProvider = CartesianLayerRangeProvider.fixed(
        minY = yAxisMin,
        maxY = yAxisMax
    )

    // Calculate date from x value (inverse of dateToX)
    val bottomAxisFormatter = CartesianValueFormatter { _, x, _ ->
        val daysFromFirst = x.roundToLong()
        val date = firstDate.plusDays(daysFromFirst)
        date.format(dateFormatter)
    }

    val verticalAxisFormatter = CartesianValueFormatter { _, y, _ ->
        String.format("%.1f", y)
    }

    LaunchedEffect(actualX, actualY, trendX, trendY) {
        modelProducer.runTransaction {
            if (hasTrendData && trendX.isNotEmpty()) {
                lineSeries {
                    series(actualX, actualY)
                    series(trendX, trendY)
                }
            } else {
                lineSeries {
                    series(actualX, actualY)
                }
            }
        }
    }

    // Create dashed line for trend
    val dashedTrendLine = remember {
        DashedLine(
            fill = LineCartesianLayer.LineFill.single(fill(Coral)),
            dashLength = 10f,
            gapLength = 10f
        )
    }

    // Create line providers based on whether we have trend data
    val lineLayer = if (hasTrendData) {
        rememberLineCartesianLayer(
            lineProvider = LineCartesianLayer.LineProvider.series(
                LineCartesianLayer.rememberLine(
                    fill = LineCartesianLayer.LineFill.single(fill(ChonkGreen)),
                    areaFill = null
                ),
                dashedTrendLine
            ),
            rangeProvider = rangeProvider
        )
    } else {
        rememberLineCartesianLayer(
            lineProvider = LineCartesianLayer.LineProvider.series(
                LineCartesianLayer.rememberLine(
                    fill = LineCartesianLayer.LineFill.single(fill(ChonkGreen)),
                    areaFill = null
                )
            ),
            rangeProvider = rangeProvider
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                lineLayer,
                startAxis = VerticalAxis.rememberStart(
                    valueFormatter = verticalAxisFormatter
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = bottomAxisFormatter
                )
            ),
            modelProducer = modelProducer,
            scrollState = rememberVicoScrollState(scrollEnabled = false),
            zoomState = rememberVicoZoomState(
                zoomEnabled = false,
                initialZoom = Zoom.Content
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(192.dp)
                .padding(16.dp)
        )
    }
}

private class DashedLine(
    fill: LineCartesianLayer.LineFill,
    dashLength: Float,
    gapLength: Float
) : LineCartesianLayer.Line(fill, areaFill = null) {
    init {
        linePaint.pathEffect = DashPathEffect(floatArrayOf(dashLength, gapLength), 0f)
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WeightChartPreview() {
    val today = LocalDate.now()
    val chartData = listOf(
        WeightChartPoint(today.minusDays(14), 75.0, false),
        WeightChartPoint(today.minusDays(12), 74.5, false),
        WeightChartPoint(today.minusDays(10), 74.8, false),
        WeightChartPoint(today.minusDays(7), 74.2, false),
        WeightChartPoint(today.minusDays(5), 73.8, false),
        WeightChartPoint(today.minusDays(3), 73.5, false),
        WeightChartPoint(today, 73.0, false),
        WeightChartPoint(today.plusDays(7), 72.5, true),
        WeightChartPoint(today.plusDays(14), 72.0, true),
        WeightChartPoint(today.plusDays(21), 71.5, true),
        WeightChartPoint(today.plusDays(28), 71.0, true)
    )

    ChonkCheckTheme {
        WeightChart(
            chartData = chartData,
            weightUnit = WeightUnit.KG,
            modifier = Modifier.padding(16.dp)
        )
    }
}
