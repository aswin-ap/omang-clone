package com.omang.app.ui.webViewer.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.google.android.material.button.MaterialButton
import com.omang.app.R
import com.omang.app.data.database.dataAnalytics.AnalyticsEntity
import com.omang.app.data.model.explore.WebItem
import com.omang.app.databinding.FragmentWebViewerBinding
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.webViewer.viewmodel.WebViewerViewModel
import com.omang.app.utils.SystemPermissionHelper
import com.omang.app.utils.ViewUtil
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.visible
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File

@Suppress("DEPRECATION")
@AndroidEntryPoint
class WebViewerFragment : Fragment() {

    private lateinit var _binding: FragmentWebViewerBinding
    private val viewModel: WebViewerViewModel by viewModels()
    private val args: WebViewerFragmentArgs by navArgs()
    private val FILE_CHOOSER_RESULT_CODE = 1
    val description = ""
    private var webItem: WebItem? = null
    private var webView: WebView? = null
    var isFabOpen = false
    var isFullScreen = false

    private val MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 101
    private var myRequest: PermissionRequest? = null

    private lateinit var dAE: AnalyticsEntity

    private var fileDownloaded: File? = null
    var fileID: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentWebViewerBinding.inflate(inflater, container, false)
        webItem = args.weblinkData

        dAE = AnalyticsEntity(
            webPlatformId = args.weblinkData.id,
            lessonId = if (args.lessonId != 0) args.lessonId else null,
            classroomId = if (args.classroomId != 0) args.classroomId else null,
            menu = args.menu
        )
        observer()
        return _binding.root
    }

    override fun onResume() {
        super.onResume()
        dAE.startTime = ViewUtil.getUtcTimeWithMSec()
        fileDownloaded = null
        fileID = null
    }

    override fun onPause() {
        super.onPause()
        dAE.webUrl = webView?.url ?: ""
        dAE.endTime = ViewUtil.getUtcTimeWithMSec()
        viewModel.insertWatchData(dAE) // insert data to analytics table for explore websites

    }

    private var youtube_com = "youtube.com"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        webItem?.url?.let { url ->
            if (!url.contains(youtube_com)) {
                initWebView()
            } else {
                _binding.youtubePlayerView.visibility = View.VISIBLE
                _binding.webView.visibility = View.GONE
                _binding.youtubePlayerView.addYouTubePlayerListener(object :
                    AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        val videoId = Uri.parse(url).getQueryParameter("v").toString()
                        Timber.e("Youtube url id addYouTubePlayerListener $videoId")
                        //isYouTubePlayerReady = true
                        youTubePlayer.loadVideo(videoId, 0f)
                        super.onReady(youTubePlayer)
                    }
                })
            }
        }

        clickListeners()

    }

    private fun initView() {
        (activity as HomeActivity).configureToolbar(
            CurrentActivity.EXPLORE,
            0,
            webItem?.name ?: "Website"
        )
        _binding.apply {
            imgRotate.setOnClickListener {
                onChangeOrientation()
            }

            info.setOnClickListener {
                if (webItem?.description!!.isEmpty()) {
                    Toast.makeText(requireContext(), "No info Available", Toast.LENGTH_SHORT).show()
                } else {
                    showDialog()
                }
            }

            llRefresh.setOnClickListener {
                webView.reload()
            }
            llNext.setOnClickListener {
                if (_binding.webView.canGoForward()) {
                    _binding.webView.goForward()
                }
            }
            llFullscreen.setOnClickListener {
                if (isFullScreen) {
                    fullscreenDisable()
                } else {
                    isFullScreen = true
                    (requireActivity() as HomeActivity).setDeviceFullScreen(true)
                    clParent.setPadding(0, 0, 0, 0)

                }
            }
            fab.setOnClickListener {
                if (!isFabOpen) {
                    showFabMenu()
                } else {
                    closeFabMenu()
                }
            }
            llPrev.setOnClickListener {

                if (_binding.webView.canGoBack()) {
                    _binding.webView.goBack()
                } else {
                    requireActivity().onBackPressed()
                }
            }

            llPdfPrev.setOnClickListener {
                hidePDFUI()

            }
        }


    }

    private fun fullscreenDisable() {
        isFullScreen = false
        (requireActivity() as HomeActivity).setDeviceFullScreen(false)

        val actionBarSize = TypedValue()
        requireActivity().theme.resolveAttribute(android.R.attr.actionBarSize, actionBarSize, true)
        val actionBarSizePixels = TypedValue.complexToDimensionPixelSize(
            actionBarSize.data, resources.displayMetrics
        )
        _binding.clParent.setPadding(0, actionBarSizePixels, 0, 0)
    }

    private fun observer() {
        viewModel.apply {
            urlState.observe(viewLifecycleOwner) {
                errorDialog(it.errors, it.incomingUrl)
            }

            initDownload.observe(viewLifecycleOwner) { url ->
                viewModel.downloadContent(
                    url,
                    requireActivity(),
                    webItem?.userAgent ?: "",
                    onComplete
                ).also { returnData ->
                    Timber.tag("WebViewerFragment").e("Download detected ${returnData.first}")
                    Timber.tag("WebViewerFragment").e("Download detected ${returnData.second}")

                    fileID = returnData.first
                    fileDownloaded = File(returnData.second)

                    if (fileID?.toInt() == -1) {

                        loadPdf(fileDownloaded!!)

                    }
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webView = _binding.webView
        webView?.let { webView1 ->
            val webSettings: WebSettings = webView1.settings
            webView1.webViewClient = CustomWebViewClient()
            webView1.webChromeClient = CustomWebChromeClient()
            webView1.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
            webSettings.setSupportZoom(true)
            webSettings.loadWithOverviewMode = true
            webSettings.builtInZoomControls = true
            webSettings.displayZoomControls = false
            webSettings.javaScriptEnabled = true
            webSettings.allowFileAccess = true
            webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH)
            webSettings.pluginState = WebSettings.PluginState.ON_DEMAND
            webSettings.domStorageEnabled = true
            webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
            webSettings.useWideViewPort = true
            webSettings.savePassword = true
            webSettings.saveFormData = true
            webSettings.mediaPlaybackRequiresUserGesture = false

            val userAgent = webItem?.userAgent?.let { viewModel.loadUserAgent(it) }

            Timber.v("Inject to USER AGENT :  $userAgent")

            webSettings.userAgentString = userAgent
            webItem?.url?.let { loadUrl(it) }
        }
    }

    fun loadPdf(file: File) {

        showPDFUI()

        _binding.apply {
            pdfView.fromFile(file)
                .defaultPage(0)
                .scrollHandle(DefaultScrollHandle(requireContext()))
                .swipeHorizontal(true)
                .enableDoubletap(true)
                .enableAntialiasing(true)
                .spacing(20)
                .onError {
                    Timber.e("pdf not loaded ${it.printStackTrace()}")
                    showPDFErrorUI()

                }
                .onLoad {
                    Timber.e("pdf loaded $it")
                }
                .load()
        }
    }

    private fun showPDFErrorUI() {
        showToast("Pdf is corrupted")
    }

    private fun showPDFUI() {
        _binding.apply {
            pdfView.visible()
            llPdfPrev.visible()
            progressBar.gone()
            webView.gone()
            youtubePlayerView.gone()
        }
    }

    private fun hidePDFUI() {
        _binding.apply {
            pdfView.gone()
            llPdfPrev.gone()
            webView.visible()
            youtubePlayerView.gone()
        }
    }

    private fun showDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_web_desc)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        val txtDescription: TextView = dialog.findViewById(R.id.txt_description)
        txtDescription.movementMethod = ScrollingMovementMethod()
        val deleteIcon = dialog.findViewById<ImageView>(R.id.delete_icon)
        txtDescription.text = "description"
        deleteIcon.setOnClickListener { dialog.dismiss() }
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window!!.attributes = lp
        dialog.show()
    }

    @SuppressLint("RestrictedApi")
    private fun showFabMenu() {
        _binding.apply {
            isFabOpen = true
            llFullscreen.visibility = View.VISIBLE
            if (webView.canGoBack()) {
                llPrev.visibility = View.VISIBLE
            }
            llRefresh.visibility = View.VISIBLE
            info.visibility = View.VISIBLE
            if (webView.canGoForward()) {
                llNext.visibility = View.VISIBLE
            }
            fab.animate().rotationBy(180f)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun closeFabMenu() {
        _binding.apply {
            isFabOpen = false
            fab.animate().rotation(0f)
            llFullscreen.animate().translationY(0f)
            llPrev.animate().translationY(0f)
            llNext.animate().translationY(0f)
            llRefresh.animate().translationY(0f)
            info.animate().translationY(0f)

            llFullscreen.visibility = View.GONE
            llNext.visibility = View.GONE
            llPrev.visibility = View.GONE
            llRefresh.visibility = View.GONE
            info.visibility = View.GONE

        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun onChangeOrientation() {
        val orientation = (requireActivity() as HomeActivity).resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            (requireActivity() as HomeActivity).requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            (requireActivity() as HomeActivity).requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    private fun loadUrl(url: String = "") {
        Timber.v("Inject to URL : $url")
        webView?.loadUrl(url)

    }

    private fun clickListeners() {
        _binding.apply {
            /*  webView.setOnClickListener {
              }*/
        }
    }

    private inner class CustomWebViewClient : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Timber.v("onPageStarted")
            showLoader()
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {

            /**
             * @param true means do not load incoming url
             * @param false means do load incoming url
             *
             * if alternateUrls are null then also incoming url can be loaded
             * */

            return !viewModel.urlInterceptor(view, request, webItem)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            Timber.v("onPageFinished")
            hideLoader()

        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            Timber.e("onReceivedError : ${request?.url.toString()}")
            hideLoader()
        }

        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            handler?.let {
                it.proceed()
            }
            super.onReceivedSslError(view, handler, error)
        }
    }

    private inner class CustomWebChromeClient : WebChromeClient() {

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            Timber.v("loading -> $newProgress")
            showLoader(newProgress)
        }

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            Timber.e("onShowFileChooser")
            chooser()
            return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
        }

        override fun onPermissionRequest(request: PermissionRequest?) {
            Timber.e("onPermissionRequest")
            request?.let {
                onPermission(request)

            } ?: run {
                showToast("the website's permission cannot be granted")
            }
            super.onPermissionRequest(request)
        }

    }

    fun showLoader(progress: Int = 0) {
        _binding.progressBar.progress = progress
        _binding.progressBar.visibility = View.VISIBLE

    }

    fun hideLoader() {
        _binding.progressBar.visibility = View.GONE
        /* if (webView?.canGoForward() == true) {
             if (isFabOpen) {
                 _binding.llNext.visibility = View.VISIBLE
             } else {
                 _binding.llNext.visibility = View.GONE
             }
         } else {
             _binding.llNext.visibility = View.VISIBLE
         } */
    }

    private fun chooser() {
        try {
            val intent = Intent()
            intent.type = "*/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                intent,
                FILE_CHOOSER_RESULT_CODE
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onPermission(request: PermissionRequest) {
        request.grant(request.resources)
        myRequest = request
        for (permission in request.resources) {
            when (permission) {
                "android.webkit.resource.AUDIO_CAPTURE" -> {
                    askForPermission(
                        request.origin.toString(),
                        Manifest.permission.RECORD_AUDIO, MY_PERMISSIONS_REQUEST_RECORD_AUDIO
                    )
                }
            }
        }
        val systemPermissionHelper = SystemPermissionHelper(activity)
        if (systemPermissionHelper.isAudioPermissionGranted) {
        } else {
            systemPermissionHelper.requestPermissionsMic()
        }
        if (systemPermissionHelper.isMicroPhonePermissionGranted) {
        } else {
            systemPermissionHelper.requestPermissionsMicrophone()
        }
        if (systemPermissionHelper.isCameraPermissionGranted) {
        } else {
            systemPermissionHelper.requestPermissionsTakePhoto()
        }
    }

    fun askForPermission(origin: String, permission: String, requestCode: Int) {
        Timber.tag("WebView").d("%s%s", "inside askForPermission for" + origin + "with", permission)
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                permission
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    permission
                )
            ) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(permission),
                    requestCode
                )
            }
        } else {
            // myRequest.grant(myRequest.getResources());
        }
    }

    override fun onStop() {
        super.onStop()
        _binding.youtubePlayerView.release()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        Timber.e("onDestroy")
        fullscreenDisable()

        viewModel.onCleared()
        webView?.let {
            it.clearCache(true)
            it.clearHistory()
            (requireActivity() as HomeActivity).removeWebViewInstance(_binding.webView)
        }
    }

    private fun errorDialog(errors: WebViewerViewModel.ERRORS, incomingUrl: String) {
        Timber.e("errorDialog : $errors")
        val dialog = Dialog(requireContext())

        dialog.setContentView(R.layout.dialog_url_errors)
        dialog.setCancelable(false)

        val tvError = dialog.findViewById<TextView>(R.id.tvError)
        val btOK = dialog.findViewById<MaterialButton>(R.id.btOK)

        /*
        * this is where the isDebugUser flag should be checked, if the flag is true then show the incoming url in dialog
        * and if the flag is false then remove the incoming url in actual text of the dialog*/
        Timber.tag("WEBVIEW DEBUG USER").e("Debug User in webview ${viewModel.getIsDebugUser()}")

        when (errors) {
            WebViewerViewModel.ERRORS.URL_ACCESS_RESTRICTED -> tvError.text =
                if (!viewModel.getIsDebugUser()) getString(R.string.url_access_restricted)
                else getString(R.string.url_access_restricted) + "  \n: -> " + incomingUrl

            WebViewerViewModel.ERRORS.URL_ERROR ->
                tvError.text =
                    if (!viewModel.getIsDebugUser()) getString(R.string.url_error)
                    else getString(R.string.url_error) + "  \n: -> " + incomingUrl

            WebViewerViewModel.ERRORS.UNKNOWN ->
                tvError.text =
                    if (!viewModel.getIsDebugUser()) getString(R.string.url_unknown_error)
                    else getString(R.string.url_unknown_error) + "  \n: -> " + incomingUrl
        }

        btOK.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window!!.attributes = lp

        dialog.show()
    }

    private var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            Timber.tag("WebViewerFragment").e("Download complete on Receive : $fileDownloaded")

            /*this where we get the file download complete case, this sh!8 should emmit it.. I guess it is*/

            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            _binding.progressBar.visibility = View.GONE

            if (id == fileID) {
                fileDownloaded?.let { file ->
                    Timber.tag("WebViewerFragment").e("redirection")
                    loadPdf(file)
                }
            }
        }
    }


}

