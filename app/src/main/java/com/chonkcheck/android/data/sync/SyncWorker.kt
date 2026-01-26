package com.chonkcheck.android.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.sentry.Sentry

/**
 * WorkManager worker that performs background sync operations.
 * Uses Hilt for dependency injection.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncManager: SyncManager,
    private val connectivityMonitor: ConnectivityMonitor
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Check connectivity first
            if (!connectivityMonitor.checkCurrentConnectivity()) {
                // No network, retry later
                return Result.retry()
            }

            // Perform sync
            syncManager.syncNow()

            // Check if there are still pending items
            val pendingCount = syncManager.getPendingCount()
            if (pendingCount > 0) {
                // Still have items to sync, schedule retry
                Result.retry()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            Sentry.captureException(e)

            // Check retry count
            if (runAttemptCount < MAX_RETRY_COUNT) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME = "chonkcheck_sync"
        private const val MAX_RETRY_COUNT = 3
    }
}
