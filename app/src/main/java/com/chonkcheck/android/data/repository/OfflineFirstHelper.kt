package com.chonkcheck.android.data.repository

import io.sentry.Sentry
import java.util.UUID

/**
 * Helper utilities for offline-first repository operations.
 *
 * These functions encapsulate the common patterns used across repositories
 * for creating and deleting entities with local-first persistence and
 * background API synchronization.
 */
object OfflineFirstHelper {

    /**
     * Generates a temporary ID for local entities that haven't been synced yet.
     * The prefix helps identify unsynced entities.
     */
    fun generateTempId(): String = "temp_${UUID.randomUUID()}"

    /**
     * Checks if an entity ID is a temporary (unsynced) ID.
     */
    fun isTempId(id: String): Boolean = id.startsWith("temp_")
}

/**
 * Executes an offline-first create operation.
 *
 * This function:
 * 1. Creates the entity locally first (immediate feedback)
 * 2. Attempts to sync with the API in the background
 * 3. If sync succeeds, replaces the temp entity with the synced one
 * 4. If sync fails, keeps the local entity (will sync later)
 *
 * @param T The type of entity/domain object
 * @param createLocal Function to create and save the entity locally, returns the local entity
 * @param syncToApi Function to sync with the API, returns the synced entity from server
 * @param replaceTempWithSynced Function to replace the temp entity with the synced one
 * @param mapToDomain Function to map the entity to a domain object for the result
 * @return Result containing the domain object (either synced or local)
 */
suspend inline fun <T, E> offlineFirstCreate(
    crossinline createLocal: suspend () -> E,
    crossinline syncToApi: suspend () -> E,
    crossinline replaceTempWithSynced: suspend (localEntity: E, syncedEntity: E) -> Unit,
    crossinline mapToDomain: (E) -> T
): Result<T> {
    return try {
        // Save locally first (offline-first)
        val localEntity = createLocal()

        // Try to sync with API
        try {
            val syncedEntity = syncToApi()
            replaceTempWithSynced(localEntity, syncedEntity)
            Result.success(mapToDomain(syncedEntity))
        } catch (apiError: Exception) {
            // API failed, but local save succeeded - return local data
            // Will be synced later when connectivity is restored
            Sentry.captureException(apiError)
            Result.success(mapToDomain(localEntity))
        }
    } catch (e: Exception) {
        Sentry.captureException(e)
        Result.failure(e)
    }
}

/**
 * Executes an offline-first delete operation.
 *
 * This function:
 * 1. Soft deletes the entity locally first (immediate feedback)
 * 2. Attempts to sync the deletion with the API
 * 3. If sync fails, the local deletion stands (will sync later)
 *
 * @param deleteLocal Function to soft delete the entity locally
 * @param syncDeleteToApi Function to delete via API
 * @return Result indicating success or failure
 */
suspend inline fun offlineFirstDelete(
    crossinline deleteLocal: suspend () -> Unit,
    crossinline syncDeleteToApi: suspend () -> Unit
): Result<Unit> {
    return try {
        // Soft delete locally first
        deleteLocal()

        // Try to sync with API
        try {
            syncDeleteToApi()
        } catch (apiError: Exception) {
            // API failed, but local delete succeeded - will sync later
            Sentry.captureException(apiError)
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Sentry.captureException(e)
        Result.failure(e)
    }
}

/**
 * Executes an offline-first update operation.
 *
 * This function:
 * 1. Updates the entity locally first (immediate feedback)
 * 2. Attempts to sync with the API
 * 3. If sync succeeds, updates with the server response
 * 4. If sync fails, keeps the local update (will sync later)
 *
 * @param T The type of domain object
 * @param E The type of entity
 * @param updateLocal Function to update the entity locally, returns the updated entity
 * @param syncToApi Function to sync with the API, returns the synced entity
 * @param updateWithSynced Function to update local storage with the synced entity
 * @param mapToDomain Function to map the entity to a domain object
 * @return Result containing the domain object (either synced or local)
 */
suspend inline fun <T, E> offlineFirstUpdate(
    crossinline updateLocal: suspend () -> E,
    crossinline syncToApi: suspend () -> E,
    crossinline updateWithSynced: suspend (E) -> Unit,
    crossinline mapToDomain: (E) -> T
): Result<T> {
    return try {
        // Update locally first
        val localEntity = updateLocal()

        // Try to sync with API
        try {
            val syncedEntity = syncToApi()
            updateWithSynced(syncedEntity)
            Result.success(mapToDomain(syncedEntity))
        } catch (apiError: Exception) {
            // API failed, but local update succeeded
            Sentry.captureException(apiError)
            Result.success(mapToDomain(localEntity))
        }
    } catch (e: Exception) {
        Sentry.captureException(e)
        Result.failure(e)
    }
}
