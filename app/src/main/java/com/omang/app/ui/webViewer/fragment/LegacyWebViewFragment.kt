package com.omang.app.ui.webViewer.fragment

import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.omang.app.R
import com.omang.app.databinding.FragmentLegacyWebViewBinding
import com.omang.app.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class LegacyWebViewFragment : Fragment() {

    private lateinit var _binding: FragmentLegacyWebViewBinding
    private var webView: WebView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLegacyWebViewBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWebView()
    }

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
//            webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH)
//            webSettings.pluginState = WebSettings.PluginState.ON_DEMAND
            webSettings.domStorageEnabled = true
            webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
            webSettings.useWideViewPort = true
            webSettings.savePassword = true
            webSettings.saveFormData = true
            webSettings.mediaPlaybackRequiresUserGesture = false

//            val userAgent = webItem?.userAgent?.let { viewModel.loadUserAgent(it) }

//            Timber.v("Inject to USER AGENT :  $userAgent")

            webSettings.userAgentString = "\"Chrome/116.0.5845.163\""
//            webItem?.url?.let { loadUrl(it) }

            webView?.loadUrl("https://brainly.in/")
        }
    }


    private inner class CustomWebChromeClient : WebChromeClient() {

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            Timber.v("loading -> $newProgress")
//            showLoader(newProgress)
        }

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            Timber.e("onShowFileChooser")
//            chooser()
            return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
        }

        override fun onPermissionRequest(request: PermissionRequest?) {
            Timber.e("onPermissionRequest")
            request?.let {
//                onPermission(request)

            } ?: run {
                showToast("the website's permission cannot be granted")
            }
            super.onPermissionRequest(request)
        }

    }


    private inner class CustomWebViewClient : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Timber.v("onPageStarted")
//                showLoader()
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

            return false
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            Timber.v("onPageFinished")
//                hideLoader()

        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            Timber.e("onReceivedError : ${request?.url.toString()}")
//                hideLoader()
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

}

