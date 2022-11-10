package xyz.webtubeapp

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
    var jsc: JSController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        webView = findViewById(R.id.webView)
        webView!!.setVisibility(View.INVISIBLE);
        jsc = JSController(webView!!, this)
        webView!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView!!.settings.javaScriptEnabled = true
        webView!!.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        if (savedInstanceState == null) {
            webView!!.loadUrl("https://m.youtube.com/")
        }
        webView!!.webChromeClient = object : WebChromeClient() {
            private var mCustomView: View? = null
            override fun onHideCustomView() {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    show(WindowInsetsCompat.Type.systemBars())
                    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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
                if (progress == 100) webView!!.setVisibility(View.VISIBLE);
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
                        jsc?.exec()
                    }
                }
                urlFinished = url
                super.onPageFinished(view, url)
                CookieManager.getInstance().flush();
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
}
