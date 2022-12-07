package com.ymcx.radon

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.window.OnBackInvokedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {
    var webView: WebView? = null
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webView)
        webView!!.visibility = View.INVISIBLE
        webView!!.settings.javaScriptEnabled = true
        webView!!.settings.domStorageEnabled = true
        if (loadUrlFromIntent(intent)) {
            webView!!.loadUrl("https://m.youtube.com/feed/subscriptions")
        }
        webView!!.webChromeClient = object : WebChromeClient() {
            private var mCustomView: View? = null
            private var mCustomViewCallback: CustomViewCallback? = null
            override fun onHideCustomView() {
                (this@MainActivity.window.decorView as FrameLayout).removeView(mCustomView)
                mCustomViewCallback!!.onCustomViewHidden()
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    show(WindowInsetsCompat.Type.systemBars())
                }
            }
            override fun onShowCustomView(paramView: View, paramCustomViewCallback: CustomViewCallback) {
                mCustomView = paramView
                mCustomViewCallback = paramCustomViewCallback
                (this@MainActivity.window.decorView as FrameLayout).addView(mCustomView, FrameLayout.LayoutParams(2282, 1080))
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    hide(WindowInsetsCompat.Type.systemBars())
                    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (progress == 100) {
                    webView!!.visibility = View.VISIBLE
                }
            }
            override fun getDefaultVideoPoster(): Bitmap {
                return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            }
        }
        webView!!.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (request.url.host == "m.youtube.com" || request.url.host == "youtube.com" || request.url.host == "www.youtube.com" || request.url.host == "youtu.be" || request.url.host!!.contains("accounts")) {
                    return false
                }
                Intent(Intent.ACTION_VIEW, request.url).apply {
                    startActivity(this)
                }
                return true
            }
            override fun onPageFinished(view: WebView, url: String) {
                webView!!.evaluateJavascript("""
                    a = document.createElement('style')
                    a.innerHTML = '\
                        .player-controls-background-action-items,\
                        .chips-visible,\
                        .top-standalone-badge-modern,\
                        ytm-video-with-context-renderer:has([data-style=SHORTS]),\
                        .center.player-controls-middle > button.icon-button:nth-of-type(1),\
                        .center.player-controls-middle > button.icon-button:nth-of-type(5),\
                        ytm-info-panel-container-renderer,\
                        .playlist-immersive-header-container,\
                        ytm-playlist-controls,\
                        .player-controls-top,\
                        .rich-grid-sticky-header,\
                        .cbox.ytm-autonav-bar,\
                        ytm-channel-list-sub-menu-renderer,\
                        ytm-pivot-bar-renderer,\
                        ytm-promoted-sparkles-web-renderer {display:none!important}\
                        body {-webkit-tap-highlight-color:transparent}\
                    '
                    document.head.appendChild(a)
                    const b = JSON.parse
                    JSON.parse = (...c) => {
                        d = b.apply(this, c)
                        d["adPlacements"] = []
                        return d
                    }
                    document.getElementById('home-icon').onclick = function() {
                        if (window.location != 'https://m.youtube.com/feed/subscriptions') {
                            window.location = 'https://m.youtube.com/feed/subscriptions'
                        } else {
                            window.location = 'https://m.youtube.com/playlist?list=WL'
                        }
                    }
                """.trimIndent(), null)
            }
        }
        onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT) {
            if (webView!!.canGoBack()) {
                if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                    webView!!.webChromeClient!!.onHideCustomView()
                } else {
                    webView!!.goBack()
                }
            } else {
                finish()
            }
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        loadUrlFromIntent(intent)
    }
    private fun loadUrlFromIntent(intent: Intent): Boolean {
        return if (Intent.ACTION_VIEW == intent.action) {
            webView!!.loadUrl(intent.data.toString())
            false
        } else {
            true
        }
    }
}
