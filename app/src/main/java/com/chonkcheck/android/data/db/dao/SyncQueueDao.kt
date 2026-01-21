package com.chonkcheck.android.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chonkcheck.android.data.db.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {

    @Query("SELECT * FROM sync_queue WHERE status = 'pending' ORDER BY createdAt LIMIT :limit")
    suspend fun getPendingItems(limit: Int = 50): List<SyncQueueEntity>

    @Query("SELECT * FROM sync_queue WHERE status = 'failed' AND retryCount < :maxRetries ORDER BY createdAt LIMIT :limit")
    suspend fun getFailedItems(maxRetries: Int = 3, limit: Int = 50): List<SyncQueueEntity>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'pending'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status IN ('pending', 'failed')")
    fun getTotalPendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SyncQueueEntity): Long

    @Update
    suspend fun update(item: SyncQueueEntity)

    @Query("UPDATE sync_queue SET status = :status, processedAt = :processedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, processedAt: Long = System.currentTimeMillis())

    @Query("""
        UPDATE sync_queue
        SET status = 'failed', retryCount = retryCount + 1, lastError = :error
        WHERE id = :id
    """)
    suspend fun markFailed(id: Long, error: String)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM sync_queue WHERE status = 'completed' AND processedAt < :threshold")
    suspend fun deleteOldCompleted(threshold: Long)

    @Query("DELETE FROM sync_queue WHERE entityType = :entityType AND entityId = :entityId")
    suspend fun deleteByEntity(entityType: String, entityId: String)

    @Query("DELETE FROM sync_queue")
    suspend fun deleteAll()
}
