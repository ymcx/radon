package com.ymcx.radon

import android.webkit.WebView

class JSController(webView: WebView) {
    private val webView: WebView = webView
    var js = "[{url: 'https://raw.githubusercontent.com/AdguardTeam/BlockYouTubeAdsShortcut/master/dist/index.js'}]"
    fun exec() {
        webView.evaluateJavascript("""
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
