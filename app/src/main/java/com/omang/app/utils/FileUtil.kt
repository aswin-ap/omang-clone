package com.omang.app.utils

import android.content.Context
import android.os.Environment
import android.os.StatFs
import timber.log.Timber
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar

object FileUtil {

    /**
     * Function to check whether a file exist
     */
    fun isFileExists(file: File): Boolean {
        return file.exists() && !file.isDirectory
    }

    /**
     * Function to get the downloads path inside the app (android/data/app_name/files/downloads)
     */
    fun getDownloadsPathInAppFolder(context: Context): String? = context.getExternalFilesDir(
        Environment.DIRECTORY_DOWNLOADS
    )?.absolutePath

    /**
     * Function to get the pictures path inside the app (android/data/app_name/files/pictures)
     */
    fun getPicturesPathInAppFolder(context: Context): String? = context.getExternalFilesDir(
        Environment.DIRECTORY_PICTURES
    )?.absolutePath

    /**
     * Function to get the movies path inside the app (android/data/app_name/files/movies)
     */
    fun getMoviesPathInAppFolder(context: Context): String? = context.getExternalFilesDir(
        Environment.DIRECTORY_MOVIES
    )?.absolutePath

    /**
     * Function to get the documents path inside the app (android/data/app_name/files/pictures)
     */
    fun getDocumentsPathInAppFolder(context: Context): String? = context.getExternalFilesDir(
        Environment.DIRECTORY_DOCUMENTS
    )?.absolutePath

    fun getApkPath(context: Context): String {
        val apkDir = File(getDownloadsPathInAppFolder(context), "Apk")
        if (!apkDir.exists()) {
            if (!apkDir.mkdirs()) {
                Timber.tag("FILE-MANAGE").e("Omang App created")
            }
        }
        return apkDir.absolutePath
    }

    fun getImageUrlPath(context: Context): String {
        val apkDir = File(getPicturesPathInAppFolder(context), "ImageUrl")
        if (!apkDir.exists()) {
            if (!apkDir.mkdirs()) {
                Timber.tag("FILE-MANAGE").e("Omang App created")
            }
        }
        return apkDir.absolutePath
    }

    fun getEmojiUrlPath(context: Context): String {
        val apkDir = File(getPicturesPathInAppFolder(context), "EmojiUrl")
        if (!apkDir.exists()) {
            if (!apkDir.mkdirs()) {
                Timber.tag("FILE-MANAGE").e("Omang App created")
            }
        }
        return apkDir.absolutePath
    }

    fun getPsmUrlPath(context: Context): String {
        val apkDir = File(getPicturesPathInAppFolder(context), "Psm")
        if (!apkDir.exists()) {
            if (!apkDir.mkdirs()) {
                Timber.tag("FILE-MANAGE").e("Psm file created")
            }
        }
        return apkDir.absolutePath
    }

    fun getTempFilePath(context: Context): String {
        val tempDir = File(getDownloadsPathInAppFolder(context), "temp")
        if (!tempDir.exists()) {
            if (!tempDir.mkdirs()) {
                Timber.tag("FILE-MANAGE").e(" Omang tempDir created")
            }

        }
        return tempDir.absolutePath
    }

    fun getTempFilePathForCameraPicture(context: Context): File {
        val tempDir = File(getPicturesPathInAppFolder(context), "temp")
        if (!tempDir.exists()) {
            if (!tempDir.mkdirs()) {
                Timber.tag("FILE-MANAGE").e(" Omang tempDir created")
            }
        }
        return File(tempDir.absolutePath)
    }

    fun getTempFilePathForCameraVideo(context: Context): String {
        val tempDir = File(getMoviesPathInAppFolder(context), "temp")
        if (!tempDir.exists()) {
            if (!tempDir.mkdirs()) {
                Timber.tag("FILE-MANAGE").e(" Omang tempDir created")
            }
        }
        return tempDir.absolutePath
    }

    private fun getRootDir(context: Context): File {
        val omangDir = getTempFilePathForCameraPicture(context)
        if (!omangDir.exists()) {
            if (!omangDir.mkdirs()) {
                Timber.tag("FILE-MANAGE").e(" Omang dir not created")
            }
        }
        return omangDir
    }

    fun isFileAlreadyDownloadedInsideDownloadsFolder(context: Context, fileUrl: String): Boolean {
        val path = getDownloadsPathInAppFolder(context)
        val fileName = fileUrl.substring(
            fileUrl.lastIndexOf(
                '/', fileUrl.lastIndex
            ) + 1
        )
        File("$path/$fileName").also {
            Timber.v("isFileExist -> $path/$fileName : exist -> ${it.exists()}")
            return (it.exists())
        }
    }

    fun isFileAlreadyDownloadedInsideImagesFolder(context: Context, fileUrl: String): Boolean {
        val path = getImageUrlPath(context)
        val fileName = fileUrl.substring(
            fileUrl.lastIndexOf(
                '/', fileUrl.lastIndex
            ) + 1
        )
        File("$path/$fileName").also {
            Timber.v("isFileExist -> $path/$fileName : exist -> ${it.exists()}")
            return (it.exists())
        }
    }

    fun isEmojiAlreadyDownloadedInsideImagesFolder(context: Context, fileUrl: String): Boolean {
        val path = getEmojiUrlPath(context)
        val fileName = fileUrl.substring(
            fileUrl.lastIndexOf(
                '/', fileUrl.lastIndex
            ) + 1
        )
        File("$path/$fileName").also {
            Timber.v("isFileExist -> $path/$fileName : exist -> ${it.exists()}")
            return (it.exists())
        }
    }
    fun isPsmAlreadyDownloadedInsideImagesFolder(context: Context, fileUrl: String): Boolean {
        val path = getPsmUrlPath(context)
        val fileName = fileUrl.substring(
            fileUrl.lastIndexOf(
                '/', fileUrl.lastIndex
            ) + 1
        )
        File("$path/$fileName").also {
            Timber.v("isFileExist -> $path/$fileName : exist -> ${it.exists()}")
            return (it.exists())
        }
    }

    fun getFileNameFromUrl(url: String): String {
        return url.substring(
            url.lastIndexOf(
                '/', url.lastIndex
            ) + 1
        )
    }

    fun getFileFromFileName(context: Context, url: String): File? {
        val file = getDownloadsPathInAppFolder(context)?.let {
            File("$it/$url")
        }
        Timber.tag("PdfViewerViewModel").v("fetchFile is $file")
        file?.let {
            return file
        }
        return null
    }

    fun deleteFile(context: Context, url: String): Boolean {
        val file = getDownloadsPathInAppFolder(context)?.let {
            File("$it/$url")
        }
        file?.let {
            if (file.exists()) {
                return file.delete()
            }
        }
        return false
    }

    fun deleteFileFromPath(filePath: File): Boolean {

        filePath.let { file ->
            if (file.exists()) {
                return file.delete()
            }
        }
        return false
    }

    fun getTempPhotoPath(context: Context): String {
        val photoPath = getRootDir(context)
        if (!photoPath.exists()) {
            if (!photoPath.mkdirs()) {
                Timber.tag("FILE-MANAGE").e("Photos dir not created")
            }
        }
        return photoPath.toString()
    }

    fun getStorageInPercentage(context: Context): Int {
        return (getUsedStorageInfo(context) * 100 / getTotalStorageInfo(context)).toInt()
    }

    private fun getTotalStorageInfo(context: Context): Long {
        var statFs: StatFs? = null
        statFs =
            StatFs(context.getExternalFilesDir(null)?.path ?: "")
        val t: Long = statFs.totalBytes
        return t / (1024 * 1024 * 1024) //GB conversion
    }

    private fun getUsedStorageInfo(context: Context): Long {
        var statFs: StatFs? = null
        statFs =
            StatFs(context.getExternalFilesDir(null)?.path ?: "")
        val u: Long = statFs.totalBytes - statFs.availableBytes
        return u / (1024 * 1024 * 1024) //GB conversion
    }

    private fun getDayFromDaysToDelete(daysToDelete: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -daysToDelete)
        return cal.timeInMillis
    }

    fun deleteFileIfCreatedBefore(fileCreationTime: Long, daysToDelete: Int): Boolean {
        val cutoffTime = getDayFromDaysToDelete(daysToDelete)
        return fileCreationTime < cutoffTime
    }

    fun isFilesExists(path: String, context: Context): Boolean {
        val tempFile: File = File(getDownloadsPathInAppFolder(context) + path)
        return tempFile.exists()
    }

}