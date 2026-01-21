package com.chonkcheck.android.data.db.converters

import androidx.room.TypeConverter
import com.chonkcheck.android.data.db.entity.RecipeIngredientJson
import com.chonkcheck.android.data.db.entity.SavedMealItemJson
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class Converters {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // LocalDate converters
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.format(DateTimeFormatter.ISO_LOCAL_DATE)

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? =
        dateString?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }

    // LocalDateTime converters
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): Long? =
        dateTime?.toInstant(ZoneOffset.UTC)?.toEpochMilli()

    @TypeConverter
    fun toLocalDateTime(timestamp: Long?): LocalDateTime? =
        timestamp?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC) }

    // List<String> converters
    @TypeConverter
    fun fromStringList(list: List<String>?): String = json.encodeToString(list ?: emptyList())

    @TypeConverter
    fun toStringList(value: String): List<String> =
        try {
            json.decodeFromString<List<String>>(value)
        } catch (e: Exception) {
            emptyList()
        }

    // Recipe ingredients JSON converters
    @TypeConverter
    fun fromRecipeIngredients(ingredients: List<RecipeIngredientJson>?): String =
        json.encodeToString(ingredients ?: emptyList())

    @TypeConverter
    fun toRecipeIngredients(value: String): List<RecipeIngredientJson> =
        try {
            json.decodeFromString<List<RecipeIngredientJson>>(value)
        } catch (e: Exception) {
            emptyList()
        }

    // Saved meal items JSON converters
    @TypeConverter
    fun fromSavedMealItems(items: List<SavedMealItemJson>?): String =
        json.encodeToString(items ?: emptyList())

    @TypeConverter
    fun toSavedMealItems(value: String): List<SavedMealItemJson> =
        try {
            json.decodeFromString<List<SavedMealItemJson>>(value)
        } catch (e: Exception) {
            emptyList()
        }
}
