package io.bladewallet.bladesdk

import android.content.Context
import android.view.ViewGroup.LayoutParams
import android.webkit.WebView
import android.widget.RelativeLayout

object Blade {
    private var initVal: Int = 0;
    private lateinit var webView: WebView

    fun init(value: Int, context: Context, view: RelativeLayout) {
        this.initVal = value;

        this.webView = WebView(context)
        this.webView.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )

        view.addView(webView)
        webView.settings.javaScriptEnabled = true;
        webView.loadUrl("file:///android_asset/index_android.html")
    }

    fun poww(hello: Int): Int {
        return hello * hello + this.initVal;
    }
}

//load js file
//        call method
//        wait for event