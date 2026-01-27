package com.chonkcheck.android.data.mappers

import com.chonkcheck.android.domain.model.ServingUnit
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Shared Json instance for mappers that need to encode/decode JSON.
 * Configured to ignore unknown keys for forward compatibility.
 */
val mapperJson: Json = Json { ignoreUnknownKeys = true }

/**
 * Parses an ISO timestamp string to epoch milliseconds.
 * Handles both Instant.parse format and ISO_DATE_TIME format.
 *
 * @return The epoch milliseconds, or null if parsing fails.
 */
fun String.parseTimestamp(): Long? {
    return try {
        Instant.parse(this).toEpochMilli()
    } catch (e: Exception) {
        try {
            DateTimeFormatter.ISO_DATE_TIME.parse(this) { temporal ->
                Instant.from(temporal).toEpochMilli()
            }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Converts an API serving unit string to the domain ServingUnit enum.
 *
 * @return The corresponding ServingUnit, defaults to GRAM for unknown values.
 */
fun String.toServingUnit(): ServingUnit {
    return when (this.lowercase()) {
        "g", "gram" -> ServingUnit.GRAM
        "ml", "milliliter" -> ServingUnit.MILLILITER
        "oz", "ounce" -> ServingUnit.OUNCE
        "cup" -> ServingUnit.CUP
        "tbsp", "tablespoon" -> ServingUnit.TABLESPOON
        "tsp", "teaspoon" -> ServingUnit.TEASPOON
        "piece" -> ServingUnit.PIECE
        "slice" -> ServingUnit.SLICE
        "serving" -> ServingUnit.SERVING
        else -> ServingUnit.GRAM
    }
}

/**
 * Converts a ServingUnit enum to the API string value.
 *
 * @return The API string representation of the serving unit.
 */
fun ServingUnit.toApiValue(): String = when (this) {
    ServingUnit.GRAM -> "g"
    ServingUnit.MILLILITER -> "ml"
    ServingUnit.OUNCE -> "oz"
    ServingUnit.CUP -> "cup"
    ServingUnit.TABLESPOON -> "tbsp"
    ServingUnit.TEASPOON -> "tsp"
    ServingUnit.PIECE -> "piece"
    ServingUnit.SLICE -> "slice"
    ServingUnit.SERVING -> "serving"
}

/**
 * Parses an ISO date string to LocalDate.
 *
 * @return The parsed LocalDate, or today's date if parsing fails.
 */
fun String.toLocalDate(): LocalDate {
    return try {
        LocalDate.parse(this)
    } catch (e: DateTimeParseException) {
        LocalDate.now()
    }
}

/**
 * Converts a LocalDate to an ISO date string for API calls.
 *
 * @return The ISO date string representation.
 */
fun LocalDate.toApiDate(): String = this.toString()
