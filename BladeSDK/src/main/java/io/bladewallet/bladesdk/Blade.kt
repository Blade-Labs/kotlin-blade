package io.bladewallet.bladesdk

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
            typicalDeferredCallback<BalanceDataResponse, BalanceResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getBalance('$id', '$completionKey')")
    }

    fun transferHbars(accountId: String, accountPrivateKey: String, receiverId: String, amount: Double, completion: (TransferDataResponse?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("transferHbars");
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransferDataResponse, TransferResponse>(data, error, completion)
        }
        executeJS("bladeSdk.transferHbars('$accountId', '$accountPrivateKey', '$receiverId', '$amount', '$completionKey')")
    }

    fun transferTokens(tokenId: String, accountId: String, accountPrivateKey: String, receiverId: String, amount: Double, completion: (TransferDataResponse?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("transferTokens");
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransferDataResponse, TransferResponse>(data, error, completion)
        }
        executeJS("bladeSdk.transferTokens('$tokenId', '$accountId', '$accountPrivateKey', '$receiverId', '$amount', '$completionKey')")
    }

    fun createHederaAccount(completion: (CreatedAccountDataResponse?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("createAccount");
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<CreatedAccountDataResponse, CreatedAccountResponse>(data, error, completion)
        }
        executeJS("bladeSdk.createAccount('$completionKey')")
    }


    fun deleteHederaAccount(deleteAccountId: String, deletePrivateKey: String, transferAccountId: String, operatorAccountId: String, operatorPrivateKey: String, completion: (TransactionReceiptDataResponse?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("deleteHederaAccount");
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionReceiptDataResponse, TransactionReceiptResponse>(data, error, completion)
        }
        executeJS("bladeSdk.deleteAccount('$deleteAccountId', '$deletePrivateKey', '$transferAccountId', '$operatorAccountId', '$operatorPrivateKey', '$completionKey')")
    }

    fun getKeysFromMnemonic (menmonic: String, lookupNames: Boolean = false, completion: (PrivateKeyDataResponse?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getKeysFromMnemonic");
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<PrivateKeyDataResponse, PrivateKeyResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getKeysFromMnemonic('$menmonic', $lookupNames, '$completionKey')")
    }

    fun sign (messageString: String, privateKey: String, completion: (SignMessageDataResponse?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("sign");
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<SignMessageDataResponse, SignMessageResponse>(data, error, completion)
        }
        executeJS("bladeSdk.sign('$messageString', '$privateKey', '$completionKey')")
    }

    fun signVerify(messageString: String, signature: String, publicKey: String, completion: (SignVerifyMessageDataResponse?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("signVerify");
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<SignVerifyMessageDataResponse, SignVerifyMessageResponse>(data, error, completion)
        }
        executeJS("bladeSdk.signVerify('$messageString', '$signature', '$publicKey', '$completionKey')")
    }

//    fun contractCallFunction(contractId: String, functionName: String, params: ContractFunctionParameters, accountId: String, accountPrivateKey: String, gas: Int = 100000, completion: (TransactionReceiptDataResponse?, BladeJSError?) -> Unit) {
//        val completionKey = getCompletionKey("contractCallFunction");
//        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
//            typicalDeferredCallback<TransactionReceiptDataResponse, TransactionReceiptResponse>(data, error, completion)
//        }
//        // TODO: let paramsEncoded = params.encode();
//        executeJS("bladeSdk.contractCallFunction('$contractId', '$functionName', '$paramsEncoded', '$accountId', '$accountPrivateKey', $gas, '$completionKey')")
//    }

    fun hethersSign(messageString: String, privateKey: String, completion: (SignMessageDataResponse?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("hethersSign");
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<SignMessageDataResponse, SignMessageResponse>(data, error, completion)
        }
        executeJS("bladeSdk.hethersSign('$messageString', '$privateKey', '$completionKey')")
    }

    fun splitSignature(signature: String, completion: (SplitedSignature?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("splitSignature");
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<SplitedSignature, SplitedSignatureResponse>(data, error, completion)
        }
        executeJS("bladeSdk.splitSignature('$signature', '$completionKey')")
    }

//    fun getParamsSignature(params: ContractFunctionParameters, accountPrivateKey: String, completion: (SplitedSignature?, BladeJSError?) -> Unit) {
//        val completionKey = getCompletionKey("getParamsSignature");
//        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
//            typicalDeferredCallback<SplitedSignature, SplitedSignatureResponse>(data, error, completion)
//        }
//
//        // TODO: let paramsEncoded = params.encode();
//        executeJS("bladeSdk.getParamsSignature('$paramsEncoded', '$accountPrivateKey', '$completionKey')")
//    }

    fun getTransactions(accountId: String, transactionType: String, nextPage: String = "", completion: (TransactionsHistory?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getTransactions");
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionsHistory, TransactionsHistoryResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getTransactions('$accountId', '$transactionType', '$nextPage', '$completionKey')")
    }

    ////////////////////////////////////////////////////////////////

    @JavascriptInterface
    public fun postMessage(jsonString: String) {
        try {
            val response = gson.fromJson(jsonString, Response::class.java)

            if (response.completionKey == "") {
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

    private inline fun <Y, reified T:Result<Y>>typicalDeferredCallback(data: String, error: BladeJSError?, completion: (Y?, BladeJSError?) -> Unit) {
        if (error != null) {
            completion(null, error);
            return;
        }
        try {
            val response = gson.fromJson(data, T::class.java)
            completion(response.data, null)
        } catch (error: Exception) {
            print(error)
            completion(null, BladeJSError("Error", "$error"))
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