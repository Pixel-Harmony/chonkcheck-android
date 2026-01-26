package com.chonkcheck.android.data.sync

import com.chonkcheck.android.data.db.dao.SyncQueueDao
import com.chonkcheck.android.domain.model.SyncConflict
import com.chonkcheck.android.domain.model.SyncStatus
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.random.Random

/**
 * Manages the synchronization of pending operations with the server.
 * Handles retry logic with exponential backoff and conflict resolution.
 */
@Singleton
class SyncManager @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    private val syncOperationProcessor: SyncOperationProcessor,
    private val connectivityMonitor: ConnectivityMonitor
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val syncMutex = Mutex()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    /** Conflicts emitted during sync operations */
    val conflicts: SharedFlow<SyncConflict> = syncOperationProcessor.conflicts

    companion object {
        private const val MAX_RETRIES = 3
        private const val BASE_DELAY_MS = 1000L
        private const val MAX_DELAY_MS = 30000L
        private const val BATCH_SIZE = 50
    }

    /**
     * Start the sync manager and observe connectivity changes.
     */
    fun start() {
        scope.launch {
            connectivityMonitor.connectivityFlow.collect { isOnline ->
                if (isOnline) {
                    // Trigger sync when connectivity is restored
                    syncPendingItems()
                }
            }
        }

        // Initial sync check
        scope.launch {
            if (connectivityMonitor.isOnline.value) {
                syncPendingItems()
            }
        }
    }

    /**
     * Trigger a manual sync of all pending items.
     */
    suspend fun syncNow() {
        syncPendingItems()
    }

    /**
     * Sync all pending items in the queue.
     * Uses mutex to prevent concurrent sync operations.
     */
    private suspend fun syncPendingItems() {
        // Prevent concurrent syncs
        if (!syncMutex.tryLock()) {
            return
        }

        try {
            // Check connectivity
            if (!connectivityMonitor.isOnline.value) {
                _syncStatus.value = SyncStatus.Idle
                return
            }

            // Get pending count
            val pendingCount = syncQueueDao.getTotalPendingCount().first()
            if (pendingCount == 0) {
                _syncStatus.value = SyncStatus.Synced
                // Reset to idle after showing synced state briefly
                delay(2000)
                _syncStatus.value = SyncStatus.Idle
                return
            }

            _syncStatus.value = SyncStatus.Syncing(pendingCount)

            // Process pending items
            val pendingItems = syncQueueDao.getPendingItems(BATCH_SIZE)
            var successCount = 0
            var failureCount = 0

            for (item in pendingItems) {
                // Check connectivity before each operation
                if (!connectivityMonitor.isOnline.value) {
                    break
                }

                val success = syncOperationProcessor.processItem(item)
                if (success) {
                    successCount++
                } else {
                    failureCount++
                }

                // Update status
                val remaining = pendingCount - successCount
                if (remaining > 0) {
                    _syncStatus.value = SyncStatus.Syncing(remaining)
                }
            }

            // Process failed items with retry
            val failedItems = syncQueueDao.getFailedItems(MAX_RETRIES, BATCH_SIZE)
            for (item in failedItems) {
                if (!connectivityMonitor.isOnline.value) {
                    break
                }

                // Apply exponential backoff
                val delayMs = calculateBackoff(item.retryCount)
                delay(delayMs)

                val success = syncOperationProcessor.processItem(item)
                if (success) {
                    successCount++
                } else {
                    failureCount++
                }
            }

            // Update final status
            val finalPendingCount = syncQueueDao.getTotalPendingCount().first()
            if (finalPendingCount == 0) {
                _syncStatus.value = SyncStatus.Synced
                delay(2000)
                _syncStatus.value = SyncStatus.Idle
            } else if (failureCount > 0) {
                val lastError = syncQueueDao.getFailedItems(MAX_RETRIES, 1)
                    .firstOrNull()?.lastError
                _syncStatus.value = SyncStatus.Error(failureCount, lastError)
            }

            // Clean up old completed items (older than 24 hours)
            val cleanupThreshold = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
            syncQueueDao.deleteOldCompleted(cleanupThreshold)
        } catch (e: Exception) {
            Sentry.captureException(e)
            _syncStatus.value = SyncStatus.Error(0, e.message)
        } finally {
            syncMutex.unlock()
        }
    }

    /**
     * Calculate exponential backoff with jitter.
     * Formula: min(BASE_DELAY * 2^retryCount + jitter, MAX_DELAY)
     */
    private fun calculateBackoff(retryCount: Int): Long {
        val exponentialDelay = BASE_DELAY_MS * (1 shl retryCount) // 2^retryCount
        val jitter = Random.nextLong(0, BASE_DELAY_MS)
        return min(exponentialDelay + jitter, MAX_DELAY_MS)
    }

    /**
     * Get the current count of pending sync operations.
     */
    suspend fun getPendingCount(): Int {
        return syncQueueDao.getTotalPendingCount().first()
    }

    /**
     * Clear all pending sync operations.
     * Use with caution - this will discard unsynced changes.
     */
    suspend fun clearPending() {
        syncQueueDao.deleteAll()
        _syncStatus.value = SyncStatus.Idle
    }
}
