package io.bladewallet.bladesdk

import BalanceDataResponse
import BalanceResponse
import BladeJSError
import CreatedAccountDataResponse
import CreatedAccountResponse
import PrivateKeyDataResponse
import PrivateKeyResponse
import Response
import SignMessageDataResponse
import SignMessageResponse
import SignVerifyMessageDataResponse
import SignVerifyMessageResponse
import TransactionReceipt
import TransactionReceiptResponse
import TransferDataResponse
import TransferResponse
import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup.LayoutParams
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
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
    fun initialize(apiKey: String, dAppCode: String, network: String, context: Context, completion: () -> Unit) {
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

    fun getBalance(id: String, completion: (BalanceDataResponse?, BladeJSError?) -> Unit) {
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

    fun transferHbars(accountId: String, accountPrivateKey: String, receiverId: String, amount: Double, completion: (TransferDataResponse?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("transferHbars");
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            if (error != null) {
                return@deferCompletion completion(null, error)
            }
            try {
                val response = gson.fromJson(data, TransferResponse::class.java)
                completion(response.data, null)
            } catch (error: Exception) {
                print(error)
                completion(null, BladeJSError("Error", "$error"))
            }
        }
        executeJS("bladeSdk.transferHbars('$accountId', '$accountPrivateKey', '$receiverId', '$amount', '$completionKey')")
    }

    fun transferTokens(tokenId: String, accountId: String, accountPrivateKey: String, receiverId: String, amount: Double, completion: (TransferDataResponse?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("transferTokens");
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            if (error != null) {
                return@deferCompletion completion(null, error)
            }
            try {
                val response = gson.fromJson(data, TransferResponse::class.java)
                completion(response.data, null)
            } catch (error: Exception) {
                print(error)
                completion(null, BladeJSError("Error", "$error"))
            }
        }
        executeJS("bladeSdk.transferTokens('$tokenId', '$accountId', '$accountPrivateKey', '$receiverId', '$amount', '$completionKey')")
    }

    fun createHederaAccount(completion: (CreatedAccountDataResponse?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("createAccount");
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            if (error != null) {
                return@deferCompletion completion(null, error)
            }
            try {
                val response = gson.fromJson(data, CreatedAccountResponse::class.java)
                completion(response.data, null)
            } catch (error: Exception) {
                    print(error)
                    completion(null, BladeJSError("Error", "$error"))
            }
        }
        executeJS("bladeSdk.createAccount('$completionKey')")
    }


    fun deleteHederaAccount(deleteAccountId: String, deletePrivateKey: String, transferAccountId: String, operatorAccountId: String, operatorPrivateKey: String, completion: (TransactionReceipt?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("deleteHederaAccount");
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            if (error != null) {
                return@deferCompletion completion(null, error)
            }
            try {
                val response = gson.fromJson(data, TransactionReceiptResponse::class.java)
                completion(response.data, null)
            } catch (error: Exception) {
                print(error)
                completion(null, BladeJSError("Error", "$error"))
            }
        }
        executeJS("bladeSdk.deleteAccount('$deleteAccountId', '$deletePrivateKey', '$transferAccountId', '$operatorAccountId', '$operatorPrivateKey', '$completionKey')")
    }

    fun getKeysFromMnemonic (menmonic: String, lookupNames: Boolean = false, completion: (PrivateKeyDataResponse?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getKeysFromMnemonic");
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            if (error != null) {
                return@deferCompletion completion(null, error)
            }
            try {
                val response = gson.fromJson(data, PrivateKeyResponse::class.java)
                completion(response.data, null)
            } catch (error: Exception) {
                print(error)
                completion(null, BladeJSError("Error", "$error"))
            }
        }
        executeJS("bladeSdk.getKeysFromMnemonic('$menmonic', $lookupNames, '$completionKey')")
    }

    fun sign (messageString: String, privateKey: String, completion: (SignMessageDataResponse?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("sign");
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            if (error != null) {
                return@deferCompletion completion(null, error)
            }
            try {
                val response = gson.fromJson(data, SignMessageResponse::class.java)

                completion(response.data, null)
            } catch (error: Exception) {
                print(error)
                completion(null, BladeJSError("Error", "$error"))
            }
        }
        executeJS("bladeSdk.sign('$messageString', '$privateKey', '$completionKey')")
    }

    fun signVerify(messageString: String, signature: String, publicKey: String, completion: (SignVerifyMessageDataResponse?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("signVerify");
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            if (error != null) {
                return@deferCompletion completion(null, error)
            }
            try {
                val response = gson.fromJson(data, SignVerifyMessageResponse::class.java)
                completion(response.data, null)
            } catch (error: Exception) {
                print(error)
                completion(null, BladeJSError("Error", "$error"))
            }
        }
        executeJS("bladeSdk.signVerify('$messageString', '$signature', '$publicKey', '$completionKey')")
    }

    ////////////////////////////////////////////////////////////////

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
