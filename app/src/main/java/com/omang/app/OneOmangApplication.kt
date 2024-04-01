package com.omang.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject

@HiltAndroidApp
class OneOmangApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
        initPR()
    }

    private fun initPR() {
        PRDownloaderConfig.Builder()
            .setDatabaseEnabled(true)
            .setConnectTimeout(30000)
            .setReadTimeout(30000)
            .build().also { config ->
                PRDownloader.initialize(applicationContext, config)
            }
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory).build()
    }

}