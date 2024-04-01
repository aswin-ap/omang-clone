package com.omang.app.utils

import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import java.io.File

class DownloadManger {
    sealed class DownloadMangerStatus {
        data class Initialized(val id: Int) : DownloadMangerStatus()
        object Started : DownloadMangerStatus()
        object Paused : DownloadMangerStatus()
        object Cancelled : DownloadMangerStatus()
        data class OnProgress(val progress: Int) : DownloadMangerStatus()
        data class Downloaded(val path: String) : DownloadMangerStatus()
        data class OnError(val error: Error?) : DownloadMangerStatus()
    }

    sealed class BulkDownloadStatus {
        data class OnProgress(val progress: Int) : BulkDownloadStatus()
        data class Downloaded(val path: String) : BulkDownloadStatus()
        data class OnError(val error: Error?) : BulkDownloadStatus()
    }

    companion object {
        fun download(url: String, fileName: String, path: String): Flow<DownloadMangerStatus> =
            callbackFlow {

                Timber.tag("PRDownloader").v("url : $url")
                Timber.tag("PRDownloader").v("path : $path")

                val pr = PRDownloader.download(url, path, fileName)
                    .build()
                    .setOnStartOrResumeListener {
                        trySend(DownloadMangerStatus.Started)

                    }
                    .setOnPauseListener {
                        trySend(DownloadMangerStatus.Paused)

                    }.setOnCancelListener {
                        trySend(DownloadMangerStatus.Cancelled)

                    }.setOnProgressListener { progress ->
                        Timber.tag("DR_PROGRESS").d("{$progress}")
                        val currentProgress =
                            (progress.currentBytes * 100 / progress.totalBytes).toInt()

                        trySend(
                            DownloadMangerStatus.OnProgress(
                                currentProgress
                            )
                        )
                    }.start(object : OnDownloadListener {
                        override fun onDownloadComplete() {
                            trySend(
                                DownloadMangerStatus.Downloaded(
                                    path + File.separator + fileName
                                )
                            )
                        }

                        override fun onError(error: Error?) {
                            trySend(
                                DownloadMangerStatus.OnError(
                                    error
                                )
                            )
                        }
                    })

                trySend(DownloadMangerStatus.Initialized(pr))
                awaitClose {

                }
            }
    }

}