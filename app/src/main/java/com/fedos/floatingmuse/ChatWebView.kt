package com.fedos.floatingmuse

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Message
import android.util.AttributeSet
import android.view.Gravity
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView

class ChatWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : WebView(context, attrs) {
    private val errorView = TextView(context).apply {
        setTextColor(Color.DKGRAY)
        setBackgroundColor(Color.WHITE)
        gravity = Gravity.CENTER
        textSize = 16f
        setPadding(32, 32, 32, 32)
        text = "Muse could not be loaded. Check your connection and try again."
        visibility = GONE
    }

    init {
        addView(errorView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        setBackgroundColor(Color.WHITE)
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.setSupportZoom(false)
        settings.builtInZoomControls = false
        settings.setSupportMultipleWindows(false)
        settings.javaScriptCanOpenWindowsAutomatically = false
        settings.loadWithOverviewMode = false
        settings.useWideViewPort = true

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

        webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
                errorView.visibility = GONE
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView, url: String) {
                errorView.visibility = GONE
                super.onPageFinished(view, url)
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (request.isForMainFrame) {
                    errorView.text = "Muse could not be loaded. Check your connection and try again.\n\n${error.description}"
                    errorView.visibility = VISIBLE
                }
                super.onReceivedError(view, request, error)
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return !isWebUrl(request.url)
            }

            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return !isWebUrl(Uri.parse(url))
            }
        }

        // Reject target=_blank and window.open attempts instead of handing them to a browser.
        webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message
            ): Boolean = false
        }
    }

    private fun isWebUrl(uri: Uri): Boolean {
        return uri.scheme == "https" || uri.scheme == "http"
    }
}
