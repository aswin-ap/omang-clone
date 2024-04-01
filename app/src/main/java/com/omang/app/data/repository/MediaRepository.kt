package com.omang.app.data.repository

import android.content.Context
import com.omang.app.utils.FileUtil
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class MediaRepository @Inject constructor() {

    fun fetchFile(context: Context, url: String): File? {
        val file = FileUtil.getDownloadsPathInAppFolder(context)?.let {
            File("$it/$url")
        }
        Timber.tag("PdfViewerViewModel").v("fetchFile is $file")
        file?.let {
            return file
        }
        return null
    }
}