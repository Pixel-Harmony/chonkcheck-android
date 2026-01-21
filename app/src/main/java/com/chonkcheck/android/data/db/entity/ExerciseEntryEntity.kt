package com.chonkcheck.android.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_entries",
    indices = [
        Index("userId"),
        Index("date"),
        Index(value = ["userId", "date"])
    ]
)
data class ExerciseEntryEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val date: String, // ISO date (YYYY-MM-DD)
    val name: String,
    val description: String?,
    val durationMinutes: Int?,
    val caloriesBurned: Int,
    val notes: String?,

    // Sync
    val syncedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)
