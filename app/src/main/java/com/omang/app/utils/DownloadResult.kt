package com.omang.app.utils

import com.downloader.Error
import java.io.File

/**
 * It is used to handle the status of download in ViewModel
 */
sealed class DownloadResult {

    object OnStartListener : DownloadResult()
    object OnPauseListener : DownloadResult()
    object OnCancelListener : DownloadResult()
    data class OnProgressListener(val progress: Int) : DownloadResult()
    data class OnDownloadComplete(val apkFile: File) : DownloadResult()
    data class AlreadyDownloaded(val apkFile: File) : DownloadResult()
    data class OnError(val error: Error?) : DownloadResult()
}