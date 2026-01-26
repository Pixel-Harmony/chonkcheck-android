package com.chonkcheck.android.domain.model

/**
 * Represents the current sync status of the application.
 */
sealed class SyncStatus {
    /** No sync in progress, all data is synced */
    data object Idle : SyncStatus()

    /** Sync is in progress */
    data class Syncing(val pendingCount: Int) : SyncStatus()

    /** All data has been synced successfully */
    data object Synced : SyncStatus()

    /** Sync failed for some items */
    data class Error(val failedCount: Int, val lastError: String?) : SyncStatus()
}

/**
 * Represents a sync conflict that occurred during synchronization.
 */
data class SyncConflict(
    val entityType: String,
    val entityId: String,
    val message: String
)

/**
 * Sync operation types
 */
enum class SyncOperation {
    CREATE,
    UPDATE,
    DELETE;

    companion object {
        fun fromString(value: String): SyncOperation = when (value.lowercase()) {
            "create" -> CREATE
            "update" -> UPDATE
            "delete" -> DELETE
            else -> throw IllegalArgumentException("Unknown sync operation: $value")
        }
    }

    fun toApiValue(): String = name.lowercase()
}

/**
 * Entity types that can be synced
 */
enum class SyncEntityType {
    FOOD,
    DIARY_ENTRY,
    RECIPE,
    SAVED_MEAL,
    WEIGHT_ENTRY,
    EXERCISE_ENTRY,
    USER;

    companion object {
        fun fromString(value: String): SyncEntityType = when (value.lowercase()) {
            "food" -> FOOD
            "diary_entry" -> DIARY_ENTRY
            "recipe" -> RECIPE
            "saved_meal" -> SAVED_MEAL
            "weight_entry" -> WEIGHT_ENTRY
            "exercise_entry" -> EXERCISE_ENTRY
            "user" -> USER
            else -> throw IllegalArgumentException("Unknown entity type: $value")
        }
    }

    fun toApiValue(): String = name.lowercase()
}
