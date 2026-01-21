package com.chonkcheck.android.core.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun <T> Flow<T>.asResult(): Flow<Result<T>> = this
    .map<T, Result<T>> { Result.Success(it) }
    .catch { emit(Result.Error(it, it.message)) }

fun Double.formatCalories(): String = "%.0f".format(this)

fun Double.formatMacro(): String = "%.1f".format(this)

fun Double.formatWeight(isMetric: Boolean): String {
    return if (isMetric) {
        "%.1f kg".format(this)
    } else {
        "%.1f lbs".format(this * 2.20462)
    }
}

fun Double.formatHeight(isMetric: Boolean): String {
    return if (isMetric) {
        "%.0f cm".format(this)
    } else {
        val totalInches = (this / 2.54).toInt()
        val feet = totalInches / 12
        val inches = totalInches % 12
        "$feet'$inches\""
    }
}

fun LocalDate.formatDisplay(): String = this.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))

fun LocalDate.formatShort(): String = this.format(DateTimeFormatter.ofPattern("MMM d"))

fun LocalDate.formatIso(): String = this.format(DateTimeFormatter.ISO_LOCAL_DATE)

fun LocalDateTime.formatDisplay(): String = this.format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a"))

fun String.parseLocalDate(): LocalDate = LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)

fun String.parseLocalDateTime(): LocalDateTime = LocalDateTime.parse(this, DateTimeFormatter.ISO_LOCAL_DATE_TIME)

fun Int.toOrdinal(): String {
    val suffix = when {
        this % 100 in 11..13 -> "th"
        this % 10 == 1 -> "st"
        this % 10 == 2 -> "nd"
        this % 10 == 3 -> "rd"
        else -> "th"
    }
    return "$this$suffix"
}
