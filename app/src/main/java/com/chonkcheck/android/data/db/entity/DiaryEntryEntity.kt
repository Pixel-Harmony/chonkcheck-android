package com.chonkcheck.android.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "diary_entries",
    indices = [
        Index("date"),
        Index("userId"),
        Index("foodId"),
        Index("recipeId"),
        Index(value = ["date", "mealType"])
    ]
    // Note: Foreign key removed - diary entries are self-contained with cached nutrition data
    // and the referenced food may not exist locally (e.g., on fresh install)
)
data class DiaryEntryEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val date: String, // ISO date (YYYY-MM-DD)

    // Meal type: "breakfast", "lunch", "dinner", "snacks"
    val mealType: String,

    // Reference to food or recipe
    val foodId: String?,
    val recipeId: String?,

    // Serving info for this entry
    val servingSize: Double,
    val servingUnit: String,
    val numberOfServings: Double,

    // Calculated nutrition for this entry
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,

    // Display name (cached from food/recipe)
    val name: String,
    val brand: String?,

    // Sync
    val syncedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)
