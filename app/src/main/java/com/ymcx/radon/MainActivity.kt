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
        loadUrlFromIntent(intent)
        webView!!.webChromeClient = object : WebChromeClient() {
            private var mCustomView: View? = null
            override fun onHideCustomView() {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    show(WindowInsetsCompat.Type.systemBars())
                }
                (this@MainActivity.window.decorView as FrameLayout).removeView(mCustomView)
            }
            override fun onShowCustomView(paramView: View, paramCustomViewCallback: CustomViewCallback) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    hide(WindowInsetsCompat.Type.systemBars())
                    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
                mCustomView = paramView
                (this@MainActivity.window.decorView as FrameLayout).addView(mCustomView)
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
                Intent(Intent.ACTION_VIEW, request.url).apply {
                    startActivity(this)
                }
                return when (request.url.host) {
                    "m.youtube.com", "youtube.com", "www.youtube.com", "youtu.be", "accounts.google.com" -> false
                    else -> true
                }
            }
            override fun onPageFinished(view: WebView, url: String) {
                webView!!.evaluateJavascript("""
(() => {
    const pageScript = () => {
      const hideAds = () => {
        const style = document.createElement("style");
        style.innerHTML = `ytm-channel-list-sub-menu-renderer, ytm-companion-slot, ytm-promoted-sparkles-web-renderer {display:none!important;} \n body {-webkit-tap-highlight-color:transparent!important;}`;
        document.head.appendChild(style);
        const elements = document.querySelectorAll("#contents > ytd-rich-item-renderer ytd-display-ad-renderer");
        if (elements.length === 0) {
          return;
        }
        elements.forEach((el) => {
          if (el.parentNode && el.parentNode.parentNode) {
            const parent = el.parentNode.parentNode;
            if (parent.localName === "ytd-rich-item-renderer") {
              parent.style.display = "none";
            }
          }
        });
      };
      const overrideObject = (obj, propertyName, overrideValue) => {
        if (!obj) {
          return false;
        }
        for (const key in obj) {
          if (obj.hasOwnProperty(key) && key === propertyName) {
            obj[key] = overrideValue;
          }
        }
      };
      const jsonOverride = (propertyName, overrideValue) => {
        const nativeJSONParse = JSON.parse;
        JSON.parse = (...args) => {
          const obj = nativeJSONParse.apply(this, args);
          overrideObject(obj, propertyName, overrideValue);
          return obj;
        };
      };
      jsonOverride("adPlacements", []);
      hideAds();
    };
    const script = document.createElement("script");
    script.innerHTML = `(`+pageScript.toString()+`)();`;
    document.head.appendChild(script);
})();
        """.trimIndent(), null)
            }
        }
        onBackInvokedDispatcher.registerOnBackInvokedCallback(
            OnBackInvokedDispatcher.PRIORITY_DEFAULT
        ) {
            if (webView!!.canGoBack()) {
                webView!!.goBack()
            } else {
                finish()
            }
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        loadUrlFromIntent(intent)
    }
    private fun loadUrlFromIntent(intent: Intent) {
        if (Intent.ACTION_VIEW == intent.action) {
            webView!!.loadUrl(intent.data.toString())
        } else {
            webView!!.loadUrl("https://m.youtube.com/feed/subscriptions")
        }
    }
}
