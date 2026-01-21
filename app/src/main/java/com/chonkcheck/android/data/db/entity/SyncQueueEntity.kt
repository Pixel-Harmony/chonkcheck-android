package com.chonkcheck.android.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sync_queue",
    indices = [
        Index("entityType"),
        Index("operation"),
        Index("status"),
        Index("createdAt")
    ]
)
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // What needs to be synced
    val entityType: String, // "food", "diary_entry", "recipe", "saved_meal", "weight_entry", "exercise_entry", "user"
    val entityId: String,
    val operation: String, // "create", "update", "delete"

    // Payload for create/update operations
    val payload: String?, // JSON representation of the entity

    // Sync status
    val status: String = "pending", // "pending", "in_progress", "completed", "failed"
    val retryCount: Int = 0,
    val lastError: String? = null,

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null
)
