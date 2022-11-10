package xyz.webtubeapp

import android.content.Context
import android.provider.Settings.Global.getString
import android.webkit.WebView

class JSController(webView: WebView, context : Context) {
    private val webView: WebView = webView
    init {
        this.exec("init")
    }

var pluginsjs = "[{name: 'AdGuard', url: 'https://raw.githubusercontent.com/thewebtube/plugins/main/adguard/main.js', injectOnUrlChange: false, enabled: true},\n];"

        var script :String = """
            
            var plugins = $pluginsjs
            
              var cache = {};
                
                function injectAll(mode = "all") {
                  for (var i = 0; i < plugins.length; i++) {
                      if (plugins[i].enabled && (plugins[i].injectOnUrlChange || mode == "all")) {
                          injectScript(plugins[i].url);
                      }
                  }
                }
                
                
                function injectScript(url) {
                  if (cache[url]) {
                      console.log("Injecting " + url + " from cache");
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
                
                // on page change 
                
                oldurl = window.location.href;
                
                setInterval(function () {
                  if (oldurl != window.location.href) {
                      oldurl = window.location.href;
                      injectAll("page change");
                  }
                }
                  , 1000);
                
                injectAll("all")
              
        """.trimIndent();

    private val initScript = """(() => {${script}})();""".trimIndent()

    fun exec(action: String) {
        var script = ""
        if (action == "init") {
            script = initScript
        }
        if (script != "") {
            try {
                webView.evaluateJavascript(script, null)
            } catch (e: Error) {
            }
        }
    }
    }



