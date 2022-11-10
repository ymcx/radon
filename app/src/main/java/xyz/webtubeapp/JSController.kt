package xyz.webtubeapp

import android.content.Context
import android.webkit.WebView

class JSController(webView: WebView, context : Context) {
    private val webView: WebView = webView
    var pluginsRepo : PluginsRepo = PluginsRepo(context)
    init {
        this.exec("init")
    }
    private val initScript = """
        const style = document.createElement('style');
        style.textContent = `body {-webkit-tap-highlight-color:transparent !Important;}`;
        document.head.append(style);
        if (!window.executed) {
            (() => {
                ${pluginsRepo.getJsScript()}
            })();
            window.addEventListener(
                "visibilitychange",
                function (event) {
                    event.stopImmediatePropagation();
                },
                true
            );
            window.addEventListener(
                "webkitvisibilitychange",
                function (event) {
                    event.stopImmediatePropagation();
                },
                true
            );
            window.addEventListener(
                "blur",
                function (event) {
                    event.stopImmediatePropagation();
                },
                true
            );
            setInterval(()=> {
                try { 
                    document.querySelector("#app > div.page-container > ytm-browse > ytm-single-column-browse-results-renderer > div > div > ytm-sign-in-promo-with-background-renderer > ytm-promo > a.sign-in-link").href="https://accounts.google.com/signin/v2/identifier?service=youtube&uilel=3&passive=false&continue=https://m.youtube.com/"
                } catch(e) {}
                try {
                    document.querySelector("#menu > div > ytm-multi-page-menu-renderer > div > ytm-multi-page-menu-section-renderer:nth-child(2) > ytm-compact-link-renderer > a").href="https://accounts.google.com/signin/v2/identifier?service=youtube&uilel=3&passive=false&continue=https://m.youtube.com/"
                } catch(e) {}
                try {
                    document.querySelector("#menu > div > ytm-multi-page-menu-renderer > div > ytm-multi-page-menu-section-renderer > ytm-compact-link-renderer:nth-child(1) > a").href="https://accounts.google.com/signin/v2/identifier?continue=https://m.youtube.com/&app=m&next=%2F&passive=false&service=youtube&uilel=0&flowName=GlifWebSignIn&flowEntry=AddSession"
                } catch(e) {}
            }
            ,1000)
            window.executed = true
        }
    """.trimIndent()
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
