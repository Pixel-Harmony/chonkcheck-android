package com.chonkcheck.android.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "recipes",
    indices = [
        Index("userId"),
        Index("name")
    ]
)
data class RecipeEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val description: String?,
    val imageUrl: String?,

    // Serving info
    val servings: Int,
    val servingSize: Double?,
    val servingUnit: String?,

    // Calculated nutrition per serving
    val caloriesPerServing: Double,
    val proteinPerServing: Double,
    val carbsPerServing: Double,
    val fatPerServing: Double,

    // Ingredients stored as JSON
    val ingredientsJson: String,

    // Instructions
    val instructions: String?,
    val prepTimeMinutes: Int?,
    val cookTimeMinutes: Int?,

    // Sync
    val syncedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)

@Serializable
data class RecipeIngredientJson(
    val foodId: String,
    val foodName: String,
    val servingSize: Double,
    val servingUnit: String,
    val numberOfServings: Double,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)
