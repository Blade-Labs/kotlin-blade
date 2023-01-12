package io.bladewallet.bladesdk

import BalanceDataResponse
import BalanceResponse
import BladeJSError
import Response
import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup.LayoutParams
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.RelativeLayout
import com.google.gson.Gson
import java.util.*

@SuppressLint("StaticFieldLeak")
object Blade {
    private lateinit var webView: WebView

    private lateinit var apiKey: String
    private val uuid = UUID(0,0).toString()
    private var network: String = "Testnet"
    private lateinit var dAppCode: String
    private var webViewInitialized: Boolean = false
    private var completionId: Int = 0
    private lateinit var initCompletion: (() -> Unit)
    private var deferCompletions = mutableMapOf<String, (String, BladeJSError?) -> Unit>()
    private val gson = Gson()

    @SuppressLint("SetJavaScriptEnabled")
    fun initialize(apiKey: String, dAppCode: String, network: String, context: Context, view: RelativeLayout, completion: () -> Unit) {
        if (webViewInitialized) {
            println("Error while doing double init of BladeSDK")
            throw Exception("Error while doing double init of BladeSDK")
        }

        initCompletion = completion

        this.apiKey = apiKey
        this.dAppCode = dAppCode
        this.network = network

        this.webView = WebView(context)
        this.webView.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )

        // todo remove
        view.addView(webView)

        webView.settings.javaScriptEnabled = true;
        webView.loadUrl("file:///android_asset/index_android.html")

        webView.addJavascriptInterface(this, "bladeMessageHandler")
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // on webView init
                webViewInitialized = true;

                val completionKey = getCompletionKey("initBladeSdkJS");
                deferCompletion(completionKey) { _: String, _: BladeJSError? ->
                    initCompletion();
                }
                executeJS("bladeSdk.init('$apiKey', '${network.lowercase()}', '$dAppCode', '$uuid', '$completionKey');");
            }
        }
    }

    public fun getBalance(id: String, completion: (BalanceDataResponse?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getBalance");
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            if (error != null) {
                completion(null, error);
                return@deferCompletion;
            }
            try {
                val response = gson.fromJson(data, BalanceResponse::class.java)
                completion(response.data, null)
            } catch (error: Exception) {
                    print(error)
                    completion(null, BladeJSError("Error", "$error"))
            }
        }
        executeJS("bladeSdk.getBalance('$id', '$completionKey')")
    }

    @JavascriptInterface
    public fun postMessage(jsonString: String) {
        try {
            val response = gson.fromJson(jsonString, Response::class.java)

            if (response.completionKey == null) {
                throw Exception("Received JS response without completionKey")
            }

            val deferredCompletion = deferCompletions[response.completionKey]
            if (deferredCompletion != null) {
                // TODO: fix this hacky way of throwing error on data parse
                if (response.error != null) {
                    deferredCompletion("", response.error)
                } else {
                    deferredCompletion(jsonString, null)
                }
            } else {
                throw Exception("Deferred function not exists");
            }
        } catch (e: Exception) {
            println(e)
            throw e;
        }
    }

    private fun executeJS (script: String) {
        if (!webViewInitialized) {
            println("BladeSDK not initialized")
            throw Exception("BladeSDK not initialized")
        }
        webView.evaluateJavascript("javascript:$script", null);
    }

    private fun deferCompletion(forKey: String, completion: (data: String, error: BladeJSError?) -> Unit) {
        deferCompletions[forKey] = completion
    }

    private fun getCompletionKey(tag: String): String {
        completionId += 1;
        return tag + completionId;
    }
}
