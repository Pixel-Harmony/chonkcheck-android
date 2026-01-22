package com.chonkcheck.android.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "day_completions",
    indices = [
        Index(value = ["userId", "date"], unique = true)
    ]
)
data class DayCompletionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val date: String, // ISO date (YYYY-MM-DD)
    val completedAt: Long,
    val syncedAt: Long? = null
)
