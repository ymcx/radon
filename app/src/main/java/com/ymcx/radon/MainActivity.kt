package com.ymcx.radon

import android.os.Bundle
import android.view.View
import android.content.Intent
import android.webkit.WebView
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.widget.FrameLayout
import android.media.AudioManager
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.webkit.WebResourceRequest
import androidx.core.view.WindowInsetsCompat
import android.window.OnBackInvokedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat

class WebView(b: Context, c: AttributeSet) : WebView(b, c) {
    private val a = b.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    override fun onWindowVisibilityChanged(d: Int) {
        if (a.getStreamVolume(AudioManager.STREAM_MUSIC) == 0 || d == VISIBLE || !url!!.contains("watch?v=")) {
            super.onWindowVisibilityChanged(d)
        }
    }
}

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
                return when (request.url.host) {
                    "m.youtube.com", "youtube.com", "www.youtube.com", "youtu.be", "accounts.google.com", "accounts.youtube.com", "accounts.google.fi" -> false
                    else -> {
                        Intent(Intent.ACTION_VIEW, request.url).apply {
                            startActivity(this)
                        }
                        true
                    }
                }
            }
            override fun onPageFinished(view: WebView, url: String) {
                webView!!.evaluateJavascript("""
                    a = document.createElement('style')
                    a.innerHTML = '.center.player-controls-middle > button.icon-button:nth-of-type(1),\
                        .center.player-controls-middle > button.icon-button:nth-of-type(5),\
                        ytm-video-with-context-renderer:has([data-style=SHORTS]),\
                        ytm-pivot-bar-item-renderer:nth-of-type(1),\
                        ytm-pivot-bar-item-renderer:nth-of-type(2),\
                        ytm-channel-list-sub-menu-renderer {display:none!important}\
                        body {-webkit-tap-highlight-color:transparent}'
                    document.head.appendChild(a)
                    const b = JSON.parse
                    JSON.parse = (...c) => {
                        d = b.apply(this, c)
                        d["adPlacements"] = []
                        return d
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
