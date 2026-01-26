package com.chonkcheck.android.data.sync

import com.chonkcheck.android.data.db.dao.SyncQueueDao
import com.chonkcheck.android.data.db.entity.SyncQueueEntity
import com.chonkcheck.android.domain.model.SyncEntityType
import com.chonkcheck.android.domain.model.SyncOperation
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for adding items to the sync queue.
 * Repositories use this to queue failed API operations for later sync.
 */
@Singleton
class SyncQueueHelper @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    internal val json: Json
) {
    /**
     * Queue a create operation for later sync.
     */
    suspend fun <T> queueCreate(
        entityType: SyncEntityType,
        entityId: String,
        payload: T,
        serializer: KSerializer<T>
    ) {
        val entity = SyncQueueEntity(
            entityType = entityType.toApiValue(),
            entityId = entityId,
            operation = SyncOperation.CREATE.toApiValue(),
            payload = json.encodeToString(serializer, payload)
        )
        syncQueueDao.insert(entity)
    }

    /**
     * Queue an update operation for later sync.
     */
    suspend fun <T> queueUpdate(
        entityType: SyncEntityType,
        entityId: String,
        payload: T,
        serializer: KSerializer<T>
    ) {
        // Remove any existing pending operations for this entity
        syncQueueDao.deleteByEntity(entityType.toApiValue(), entityId)

        val entity = SyncQueueEntity(
            entityType = entityType.toApiValue(),
            entityId = entityId,
            operation = SyncOperation.UPDATE.toApiValue(),
            payload = json.encodeToString(serializer, payload)
        )
        syncQueueDao.insert(entity)
    }

    /**
     * Queue a delete operation for later sync.
     */
    suspend fun queueDelete(
        entityType: SyncEntityType,
        entityId: String
    ) {
        // Remove any existing pending operations for this entity
        syncQueueDao.deleteByEntity(entityType.toApiValue(), entityId)

        val entity = SyncQueueEntity(
            entityType = entityType.toApiValue(),
            entityId = entityId,
            operation = SyncOperation.DELETE.toApiValue(),
            payload = null
        )
        syncQueueDao.insert(entity)
    }

    /**
     * Remove pending operations for an entity (e.g., when sync succeeds).
     */
    suspend fun removePending(entityType: SyncEntityType, entityId: String) {
        syncQueueDao.deleteByEntity(entityType.toApiValue(), entityId)
    }
}
