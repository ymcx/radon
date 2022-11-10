package xyz.webtubeapp

import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.ActivityInfo
import android.content.res.Configuration
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
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebSettingsCompat.FORCE_DARK_OFF
import androidx.webkit.WebSettingsCompat.FORCE_DARK_ON

class MainActivity : AppCompatActivity() {
    private var urlFinished: String = ""
    var webView: WebView? = null
    var jsc: JSController? = null
    private var THEME = "THEME"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        supportActionBar?.hide()
        webView = findViewById(R.id.webView)
        webView!!.setVisibility(View.INVISIBLE);
        jsc = JSController(webView!!, this)
        webView!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        val webSettings = webView!!.settings
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        webSettings.javaScriptEnabled = true
        webView!!.settings.domStorageEnabled = true
        webView!!.settings.javaScriptCanOpenWindowsAutomatically = true
        webView!!.settings.loadsImagesAutomatically = true
        webSettings.allowFileAccess = true
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webView!!.settings.mediaPlaybackRequiresUserGesture = false;
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        initTheme()
        webSettings.mixedContentMode = 0
        webView!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView!!.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun onReceivedError(
                webView: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String,
            ) {
                try {
                    webView.stopLoading()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (webView.canGoBack()) {
                    webView.goBack()
                }
                webView.loadUrl("about:blank")
                super.onReceivedError(webView, errorCode, description, failingUrl)
            }
        }
        if (savedInstanceState == null) {
            openYT(intent.data.toString())
        }
        webView!!.webChromeClient = object : WebChromeClient() {
            private var mCustomView: View? = null
            private var mCustomViewCallback: CustomViewCallback? = null
            protected var mFullscreenContainer: FrameLayout? = null
            @SuppressLint("WrongConstant")
            override fun onHideCustomView() {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    show(WindowInsetsCompat.Type.systemBars())
                    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
                (this@MainActivity.window.decorView as FrameLayout).removeView(mCustomView)
                mCustomView = null
                mCustomViewCallback!!.onCustomViewHidden()
                mCustomViewCallback = null
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
                if (mCustomView != null) {
                    onHideCustomView()
                    return
                }
                mCustomView = paramView
                mCustomViewCallback = paramCustomViewCallback
                (this@MainActivity.window.decorView as FrameLayout).addView(
                    mCustomView,
                    FrameLayout.LayoutParams(-1, -1)
                )
            }
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (progress == 100) webView!!.setVisibility(View.VISIBLE);
            }
            override fun getDefaultVideoPoster(): Bitmap? {
                return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
            }
        }
        webView!!.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                val host = Uri.parse(url).host.toString()
                Uri.parse(url).path.toString()
                if (host == "m.youtube.com" || host == "youtube.com" || host.contains("accounts")) { // for google login
                    return false
                }
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    startActivity(this) //Here's the problem!
                }
                return true
            }
            override fun onPageFinished(view: WebView, url: String) {
                if (urlFinished != url) {
                    val host = Uri.parse(url).host.toString()
                    if (host.contains("m.youtube.com")) {
                        jsc?.exec("init")
                    }
                }
                urlFinished = url
                super.onPageFinished(view, url)
            }
        }
        webView!!.setOnKeyListener(View.OnKeyListener { view: View?, keyCode: Int, keyEvent: KeyEvent ->
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
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val url = intent.data
        webView!!.loadUrl("about:blank")
        openYT(url.toString())
    }
    private fun openYT(url : String) {
        if (url.contains("youtube.com/watch?v=") || url.toString().contains("youtu.be/")) {
            urlFinished = url.toString()
            urlFinished = urlFinished.replace("youtu.be/", "youtube.com/watch?v=")
            if (!urlFinished.contains("m.youtube.com")) {
                urlFinished = urlFinished.replace("www.", "")
                urlFinished = urlFinished.replace("youtube.com", "m.youtube.com")
                urlFinished = urlFinished + "?app=m"
            }
            webView!!.post(Runnable {
                webView!!.loadUrl(urlFinished)
            })
        } else {
            webView!!.post(Runnable { webView!!.loadUrl("https://m.youtube.com/?app=m") })
        }
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (isVideoView()) {
            webView!!.postDelayed({
                jsc?.exec("toggleFull")
            }, 200)
        }
    }
    override fun onResume() {
        initTheme()
        super.onResume()
    }
    override fun onPause() {
        jsc?.exec("pause")
        super.onPause()
    }
    private fun isDarkModeOn(): Boolean {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
    private fun initTheme(){
        val preferences: SharedPreferences = this.getSharedPreferences(THEME, MODE_PRIVATE)
        val webSettings = webView!!.settings
        if (isDarkModeOn()) {
            WebSettingsCompat.setForceDark(webSettings, FORCE_DARK_ON)
        } else {
            WebSettingsCompat.setForceDark(webSettings, FORCE_DARK_OFF)
        }
    }
    private fun isVideoView(): Boolean {
        return webView?.url.toString().contains("youtube.com/watch?v=")
    }
    override fun onDestroy() {
        super.onDestroy()
    }
}
