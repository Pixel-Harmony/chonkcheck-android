package com.chonkcheck.android.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "saved_meals",
    indices = [
        Index("userId"),
        Index("name")
    ]
)
data class SavedMealEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val description: String?,

    // Items stored as JSON
    val itemsJson: String,

    // Total nutrition
    val totalCalories: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,

    // Usage tracking
    val usageCount: Int = 0,
    val lastUsedAt: Long? = null,

    // Sync
    val syncedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)

@Serializable
data class SavedMealItemJson(
    val type: String, // "food" or "recipe"
    val foodId: String?,
    val recipeId: String?,
    val name: String,
    val brand: String?,
    val servingSize: Double,
    val servingUnit: String,
    val numberOfServings: Double,
    val enteredAmount: Double? = null,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)
