package com.omang.app.ui.webViewer.viewmodel

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.omang.app.data.database.user.User
import com.omang.app.data.model.explore.WebItem
import com.omang.app.ui.base.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import javax.inject.Inject

@HiltViewModel
class WebViewerViewModel @Inject constructor(
    application: Application,
) : BaseViewModel(application) {

    private val _urlState = MutableLiveData<UrlState>()
    val urlState: LiveData<UrlState> get() = _urlState

    private val _initDownload = MutableLiveData<String>()
    val initDownload: LiveData<String> get() = _initDownload



    fun urlInterceptor(view: WebView?, request: WebResourceRequest?, webItem: WebItem?): Boolean {
        /*
        * like dev rohit said b4,
        * "worst logic" - in a good way..
        * only me and God knows what's going on here.. so cut some slack for me.. :( */

        if (request != null) {
            Timber.tag("URL LOGIC -> ").e("incoming url " + request.url.toString())
        } else return true

        try {
            Timber.tag("URL LOGIC ->")
                .e("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^")
            for (altUrl in webItem?.alternateUrls!!) {
                Timber.v("Alternate url $altUrl")
            }
            Timber.tag("URL LOGIC ->")
                .e("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val incomingUrl = request.url.toString()

        return if (webItem?.alternateUrls.isNullOrEmpty()) {
//             case where there are no alternate url, allow every incoming
            Timber.tag("URL LOGIC ->").e("1")
            _urlState.value = UrlState(ERRORS.URL_ACCESS_RESTRICTED, incomingUrl)
            false

        } else if (webItem?.alternateUrls!!.contains(incomingUrl)) {
//             case where the incoming url is specified in alternate url
            Timber.tag("URL LOGIC ->").e("2")
            _initDownload.value = incomingUrl
            true

        } else {
            for (altUrl in webItem.alternateUrls) {
                if (incomingUrl.contains(altUrl)) {
                    Timber.tag("URL LOGIC ->").e("3")

                    _initDownload.value = incomingUrl
                    return true
                }
            }
            Timber.tag("URL LOGIC ->").e("4")
            _urlState.value = UrlState(ERRORS.URL_ACCESS_RESTRICTED, incomingUrl)
            false

        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    fun downloadContent(
        url: String,
        context: Activity,
        userAgent: String,
        onComplete: BroadcastReceiver
    ): Pair<Long, String> {

        /* I guess the download manaer won't support the non-ssl urls
        * for eg. non-ssl mobituta files are not downloading in download manager,
        * but ssl supported urls are downloading from other websites */

        /*note that the download manager only supports the pdf now,
        * need to implement the image and videos  */

        if (!url.contains(".pdf")) {
            return Pair(0, "")
        }

        val source = Uri.parse(url)
        var fileName: String? = null
        try {
            fileName = URLDecoder.decode(url.substring(url.lastIndexOf('/') + 1), "utf-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        var fileDownloaded: File? = null

        fileDownloaded = File(
            Environment.getExternalStorageDirectory().absolutePath,
            Environment.DIRECTORY_DOWNLOADS
        )

        fileDownloaded =
            File(fileDownloaded.toString() + File.separator + fileName!!.replace("%20", " "))

        if (!fileDownloaded.exists()) {
            Timber.tag("WebViewerFragment").e("authenticated download $url")

            val download = DownloadManager.Request(source)
            download.setDescription("requested $url downloading")
            download.setTitle(fileName)
            val cookies = CookieManager.getInstance().getCookie(url)
            download.addRequestHeader("cookie", cookies)
            download.addRequestHeader("User-Agent", userAgent)
            download.allowScanningByMediaScanner()
            download.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            download.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            if (url.contains(".pdf")) {
                download.setMimeType("application/pdf")
            } else if (url.contains(".png")) {
                download.setMimeType("image/png")
            } else if (url.contains(".jpg")) {
                download.setMimeType("image/jpg")
            } else if (url.contains(".gif")) {
                download.setMimeType("")
            }

            val manager =
                context.applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            context.applicationContext.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )  // pending receiver flag

            Timber.tag("WebViewerFragment").e("initiated fileDownloaded : $fileDownloaded")

            return Pair(manager.enqueue(download), fileDownloaded.toString())

        } else {

//            return the data that the file is already downloaded (-1)
            return Pair(-1, fileDownloaded.toString())
        }

    }

    internal fun loadUserAgent(url: String) = run {
        /*
        *user agent load logic*/
        url.replace("<v>", Build.VERSION.RELEASE).replace("<bId>", Build.ID)
            .replace("<cId>", chromeVersion)
    }

    fun getIsDebugUser() = sharedPref.isDebugUser

    public override fun onCleared() {
        super.onCleared()
    }

    data class UrlState(val errors: ERRORS, val incomingUrl: String)

    enum class ERRORS {
        URL_ACCESS_RESTRICTED, URL_ERROR, UNKNOWN
    }

    companion object {
        //        should update it, when there is a new update
        private const val chromeVersion = "116.0.5845.163"

    }

}



