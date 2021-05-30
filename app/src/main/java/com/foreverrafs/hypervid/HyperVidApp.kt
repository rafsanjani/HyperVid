package com.foreverrafs.hypervid

import android.app.Application
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import timber.log.Timber
import timber.log.Timber.DebugTree

private const val TAG = "HyperVidApp"

class HyperVidApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        // Enabling database for resume support even after the application is killed:
        val config: PRDownloaderConfig = PRDownloaderConfig.newBuilder()
            .setReadTimeout(30_000)
            .setConnectTimeout(30_000)
            .setDatabaseEnabled(true)
            .build()

        PRDownloader.initialize(applicationContext, config)

    }
}