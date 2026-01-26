package com.chonkcheck.android

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.chonkcheck.android.data.sync.SyncManager
import com.chonkcheck.android.data.sync.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ChonkCheckApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var syncManager: SyncManager

    @Inject
    lateinit var syncScheduler: SyncScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Initialize Sentry for error tracking
        SentryInitializer.init(this)

        // Start sync manager to observe connectivity changes
        syncManager.start()

        // Schedule periodic background sync
        syncScheduler.schedulePeriodicSync()
    }
}
