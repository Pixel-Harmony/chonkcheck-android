package com.chonkcheck.android

import android.content.Context
import io.sentry.android.core.SentryAndroid
import io.sentry.SentryOptions

/**
 * Initializes Sentry for error tracking and performance monitoring.
 */
object SentryInitializer {

    /**
     * Initialize Sentry with the configured DSN.
     * Only initializes if SENTRY_DSN is configured in local.properties.
     *
     * @param context Application context
     */
    fun init(context: Context) {
        val dsn = BuildConfig.SENTRY_DSN
        if (dsn.isBlank()) {
            // Sentry not configured, skip initialization
            return
        }

        SentryAndroid.init(context) { options ->
            options.dsn = dsn

            // Set environment based on build type
            options.environment = if (BuildConfig.DEBUG) "development" else "production"

            // Enable automatic session tracking
            options.isEnableAutoSessionTracking = true

            // Set sample rates
            options.tracesSampleRate = 1.0 // 100% of transactions for performance monitoring

            // Only log breadcrumbs in debug mode
            options.isDebug = BuildConfig.DEBUG && BuildConfig.SENTRY_DEBUG

            // Set release information
            options.release = "${BuildConfig.APPLICATION_ID}@${BuildConfig.VERSION_NAME}"

            // Configure before send callback to filter events in development
            if (BuildConfig.DEBUG && !BuildConfig.SENTRY_DEBUG) {
                options.beforeSend = SentryOptions.BeforeSendCallback { event, _ ->
                    // Don't send events in debug mode unless SENTRY_DEBUG is enabled
                    null
                }
            }
        }
    }
}
