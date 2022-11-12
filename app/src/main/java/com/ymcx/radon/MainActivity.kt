package com.ymcx.radon

import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var urlFinished: String = ""
    var webView: WebView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        webView = findViewById(R.id.webView)
        webView!!.visibility = View.INVISIBLE;
        webView!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView!!.settings.javaScriptEnabled = true
        webView!!.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        if (savedInstanceState == null) {
            webView!!.loadUrl("https://m.youtube.com/feed/subscriptions/")
        }
        webView!!.webChromeClient = object : WebChromeClient() {
            private var mCustomView: View? = null
            override fun onHideCustomView() {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    show(WindowInsetsCompat.Type.systemBars())
                }
                (this@MainActivity.window.decorView as FrameLayout).removeView(mCustomView)
                mCustomView = null
            }
            override fun onShowCustomView(
                paramView: View?,
                paramCustomViewCallback: CustomViewCallback?
            ) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    hide(WindowInsetsCompat.Type.systemBars())
                    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
                mCustomView = paramView
                (this@MainActivity.window.decorView as FrameLayout).addView(
                    mCustomView,
                    FrameLayout.LayoutParams(-1, -1)
                )
            }
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (progress == 100) webView!!.visibility = View.VISIBLE;
            }
            override fun getDefaultVideoPoster(): Bitmap? {
                return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
            }
        }
        webView!!.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                if (urlFinished != url) {
                    val host = Uri.parse(url).host.toString()
                    if (host.contains("m.youtube.com")) {
                        exec()
                    }
                }
                urlFinished = url
                super.onPageFinished(view, url)
            }
        }
        webView!!.setOnKeyListener(View.OnKeyListener { _: View?, keyCode: Int, keyEvent: KeyEvent ->
            if (keyEvent.action != KeyEvent.ACTION_DOWN) return@OnKeyListener true
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (webView!!.canGoBack()) {
                    webView!!.goBack()
                } else {
                    finish()
                }
                return@OnKeyListener true
            }
            false
        })
    }
    var js = "[{url: 'https://raw.githubusercontent.com/AdguardTeam/BlockYouTubeAdsShortcut/master/dist/index.js'}]"
    fun exec() {
        webView!!.evaluateJavascript("""
            (() => {
                const style = document.createElement('style');
                style.textContent = `body {-webkit-tap-highlight-color:transparent !Important;}`;
                document.head.append(style);
                var plugins = $js
                var cache = {};
                function injectAll() {
                    for (var i = 0; i < plugins.length; i++) {
                        injectScript(plugins[i].url);
                    }
                }
                function injectScript(url) {
                    if (cache[url]) {
                        eval(cache[url]);
                    } else {
                        var xhr = new XMLHttpRequest();
                        xhr.open("GET", url, true);
                        xhr.onreadystatechange = function () {
                            if (xhr.readyState == 4) {
                                if (xhr.status == 200) {
                                    cache[url] = xhr.responseText;
                                    eval(xhr.responseText);
                                }
                            }
                        }
                        xhr.send();
                    }
                }
                injectAll()
            })();
        """.trimIndent(), null)
    }
}
