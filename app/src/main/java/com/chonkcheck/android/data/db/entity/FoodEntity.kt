package com.chonkcheck.android.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "foods",
    indices = [
        Index("name"),
        Index("barcode"),
        Index("type"),
        Index("userId")
    ]
)
data class FoodEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val brand: String?,
    val barcode: String?,

    // Serving info
    val servingSize: Double,
    val servingUnit: String,
    val servingsPerContainer: Double?,

    // Nutrition per serving
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,

    // Optional detailed nutrition
    val saturatedFat: Double?,
    val transFat: Double?,
    val cholesterol: Double?,
    val sodium: Double?,
    val fiber: Double?,
    val sugar: Double?,
    val addedSugar: Double?,
    val vitaminA: Double?,
    val vitaminC: Double?,
    val vitaminD: Double?,
    val calcium: Double?,
    val iron: Double?,
    val potassium: Double?,

    // Type: "platform" (from database) or "user" (user created)
    val type: String,
    val userId: String?, // Only for user-created foods

    // Source: "user_submitted" or "open_food_facts"
    val source: String?,

    // Metadata
    val verified: Boolean = false,
    val promotionRequested: Boolean = false,
    val promotionRequestedAt: Long? = null,
    val overrideOf: String? = null,
    val imageUrl: String?,

    // Sync
    val syncedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val archivedAt: Long? = null
)
