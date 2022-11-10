package xyz.webtubeapp

import android.content.Context
import android.webkit.WebView

class JSController(webView: WebView, context : Context) {
    private val webView: WebView = webView
    var js = "[{url: 'https://raw.githubusercontent.com/thewebtube/plugins/main/adguard/main.js'}];"
    fun exec(action: String) {
        webView.evaluateJavascript("""
        (() => {
        var plugins = $js
          var cache = {};
            function injectAll(mode = "all") {
              for (var i = 0; i < plugins.length; i++) {
                  if ((plugins[i].injectOnUrlChange || mode == "all")) {
                      injectScript(plugins[i].url);
                  }
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
            injectAll("all")
          })();
    """.trimIndent(), null)
    }
}
