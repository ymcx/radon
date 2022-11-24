package com.ymcx.radon

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {
    var webView: WebView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webView)
        webView!!.visibility = View.INVISIBLE
        webView!!.settings.javaScriptEnabled = true
        webView!!.settings.domStorageEnabled = true
        if (!loadUrlFromIntent(intent)) {
            webView!!.loadUrl("https://m.youtube.com/feed/subscriptions")
        }
        webView!!.webChromeClient = object : WebChromeClient() {
            private var mCustomView: View? = null
            override fun onHideCustomView() {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    show(WindowInsetsCompat.Type.systemBars())
                }
                (this@MainActivity.window.decorView as FrameLayout).removeView(mCustomView)
            }
            override fun onShowCustomView(
                paramView: View?,
                paramCustomViewCallback: CustomViewCallback?
            ) {
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
            override fun getDefaultVideoPoster(): Bitmap? {
                return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            }
        }
        webView!!.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    startActivity(this)
                }
                return when (Uri.parse(url).host) {
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
    }
    override fun onBackPressed() {
        if (webView!!.canGoBack()) {
            webView!!.goBack()
        } else {
            finish()
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }
    private fun loadUrlFromIntent(intent: Intent): Boolean {
        return if (Intent.ACTION_VIEW == intent.action) {
            val url = intent.data.toString()
            if (url != webView!!.url) {
                webView!!.loadUrl(url)
            }
            true
        } else {
            false
        }
    }

}
