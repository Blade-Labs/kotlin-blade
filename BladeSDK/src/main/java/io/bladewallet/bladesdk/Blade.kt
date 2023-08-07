package io.bladewallet.bladesdk

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup.LayoutParams
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.fingerprintjs.android.fpjs_pro.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.util.*

@SuppressLint("StaticFieldLeak")
object Blade {
    private const val sdkVersion: String = "Kotlin@0.6.1"
    private var webView: WebView? = null
    private lateinit var apiKey: String
    private var visitorId: String = ""
    private lateinit var remoteConfig: RemoteConfig
    private var network: String = "Testnet"
    private lateinit var dAppCode: String
    private var webViewInitialized: Boolean = false
    private var completionId: Int = 0
    private lateinit var initCompletion: ((InfoData?, BladeJSError?) -> Unit)
    private var deferCompletions = mutableMapOf<String, (String, BladeJSError?) -> Unit>()
    private val gson = Gson()

    @SuppressLint("SetJavaScriptEnabled")
    fun initialize(apiKey: String, dAppCode: String, network: String, bladeEnv: BladeEnv, context: Context, completion: (InfoData?, BladeJSError?) -> Unit) {
        if (webViewInitialized) {
            println("Error while doing double init of BladeSDK")
            throw Exception("Error while doing double init of BladeSDK")
        }
        initCompletion = completion
        this.apiKey = apiKey
        this.dAppCode = dAppCode
        this.network = network

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {
            runBlocking {
                try {
                    remoteConfig = getRemoteConfig(network, dAppCode, sdkVersion, bladeEnv)
                    visitorId = getVisitorId(remoteConfig.fpApiKey, context)
                } catch (e: java.lang.Exception) {
                    initCompletion(null, BladeJSError("Init failed", e.toString()))
                }
            }

            if (visitorId == "") {
                return@launch
            }

            launch(Dispatchers.Main) {
                webView = WebView(context)
                webView?.let { webView ->
                    webView.layoutParams = LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT
                    )

                    webView.settings.javaScriptEnabled = true
                    webView.loadUrl("file:///android_asset/index.html")

                    webView.addJavascriptInterface(this@Blade, "bladeMessageHandler")
                    webView.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            // on webView init
                            webViewInitialized = true
                            val completionKey = getCompletionKey("initBladeSdkJS")
                            deferCompletion(completionKey) { data: String, error: BladeJSError? ->
                                typicalDeferredCallback<InfoData, InfoResponse>(data, error, initCompletion)
                            }
                            executeJS("bladeSdk.init('${esc(apiKey)}', '${esc(network.lowercase())}', '${esc(dAppCode)}', '$visitorId', '$bladeEnv', '${esc(sdkVersion)}', '$completionKey')")
                        }
                    }
                }
            }
        }
    }

    fun getBalance(id: String, completion: (BalanceData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getBalance")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<BalanceData, BalanceResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getBalance('${esc(id)}', '$completionKey')")
    }

    fun transferHbars(accountId: String, accountPrivateKey: String, receiverId: String, amount: Double, memo: String, completion: (TransferData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("transferHbars")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransferData, TransferResponse>(data, error, completion)
        }
        executeJS("bladeSdk.transferHbars('${esc(accountId)}', '${esc(accountPrivateKey)}', '${esc(receiverId)}', '$amount', '${esc(memo)}', '$completionKey')")
    }

    fun transferTokens(tokenId: String, accountId: String, accountPrivateKey: String, receiverId: String, amount: Double, memo: String, freeTransfer: Boolean = true, completion: (TransferData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("transferTokens")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransferData, TransferResponse>(data, error, completion)
        }
        executeJS("bladeSdk.transferTokens('${esc(tokenId)}', '${esc(accountId)}', '${esc(accountPrivateKey)}', '${esc(receiverId)}', '$amount', '${esc(memo)}', $freeTransfer, '$completionKey')")
    }

    fun createHederaAccount(deviceId: String, completion: (CreatedAccountData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("createAccount")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<CreatedAccountData, CreatedAccountResponse>(data, error, completion)
        }
        executeJS("bladeSdk.createAccount('${esc(deviceId)}', '$completionKey')")
    }

    fun getPendingAccount(transactionId: String, seedPhrase: String, completion: (CreatedAccountData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getPendingAccount")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<CreatedAccountData, CreatedAccountResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getPendingAccount('${esc(transactionId)}', '${esc(seedPhrase)}', '$completionKey')")
    }

    fun deleteHederaAccount(deleteAccountId: String, deletePrivateKey: String, transferAccountId: String, operatorAccountId: String, operatorPrivateKey: String, completion: (TransactionReceiptData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("deleteHederaAccount")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
        }
        executeJS("bladeSdk.deleteAccount('${esc(deleteAccountId)}', '${esc(deletePrivateKey)}', '${esc(transferAccountId)}', '${esc(operatorAccountId)}', '${esc(operatorPrivateKey)}', '$completionKey')")
    }

    fun getAccountInfo(accountId: String, completion: (AccountInfoData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getAccountInfo")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<AccountInfoData, AccountInfoResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getAccountInfo('${esc(accountId)}', '$completionKey')")
    }

    fun getKeysFromMnemonic (menmonic: String, lookupNames: Boolean = false, completion: (PrivateKeyData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getKeysFromMnemonic")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<PrivateKeyData, PrivateKeyResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getKeysFromMnemonic('${esc(menmonic)}', $lookupNames, '$completionKey')")
    }

    fun sign (messageString: String, privateKey: String, completion: (SignMessageData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("sign")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<SignMessageData, SignMessageResponse>(data, error, completion)
        }
        executeJS("bladeSdk.sign('${esc(messageString)}', '${esc(privateKey)}', '$completionKey')")
    }

    fun signVerify(messageString: String, signature: String, publicKey: String, completion: (SignVerifyMessageData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("signVerify")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<SignVerifyMessageData, SignVerifyMessageResponse>(data, error, completion)
        }
        executeJS("bladeSdk.signVerify('${esc(messageString)}', '${esc(signature)}', '${esc(publicKey)}', '$completionKey')")
    }

    fun contractCallFunction(contractId: String, functionName: String, params: ContractFunctionParameters, accountId: String, accountPrivateKey: String, gas: Int = 100000, bladePayFee: Boolean, completion: (TransactionReceiptData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("contractCallFunction")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
        }
        // TODO check if we need to escape ContractFunctionParams.encode() result \'
        executeJS("bladeSdk.contractCallFunction('${esc(contractId)}', '${esc(functionName)}', '${params.encode()}', '${esc(accountId)}', '${esc(accountPrivateKey)}', $gas, $bladePayFee, '$completionKey')")
    }

    fun contractCallQueryFunction(contractId: String, functionName: String, params: ContractFunctionParameters, accountId: String, accountPrivateKey: String, gas: Int = 100000, bladePayFee: Boolean, returnTypes: List<String>, completion: (ContractQueryData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("contractCallQueryFunction")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<ContractQueryData, ContractQueryResponse>(data, error, completion)
        }

        executeJS("bladeSdk.contractCallQueryFunction('${esc(contractId)}', '${esc(functionName)}', '${params.encode()}', '${esc(accountId)}', '${esc(accountPrivateKey)}', $gas, $bladePayFee, ${returnTypes.joinToString(",", "[", "]") {"\'${esc(it)}\'"}}, '$completionKey')")
    }

    fun hethersSign(messageString: String, privateKey: String, completion: (SignMessageData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("hethersSign")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<SignMessageData, SignMessageResponse>(data, error, completion)
        }
        executeJS("bladeSdk.hethersSign('${esc(messageString)}', '${esc(privateKey)}', '$completionKey')")
    }

    fun splitSignature(signature: String, completion: (SplitSignatureData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("splitSignature")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<SplitSignatureData, SplitSignatureResponse>(data, error, completion)
        }
        executeJS("bladeSdk.splitSignature('${esc(signature)}', '$completionKey')")
    }

    fun getParamsSignature(params: ContractFunctionParameters, accountPrivateKey: String, completion: (SplitSignatureData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getParamsSignature")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<SplitSignatureData, SplitSignatureResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getParamsSignature('${params.encode()}', '${esc(accountPrivateKey)}', '$completionKey')")
    }

    fun getTransactions(accountId: String, transactionType: String, nextPage: String = "", transactionsLimit: Int = 10, completion: (TransactionsHistoryData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getTransactions")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionsHistoryData, TransactionsHistoryResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getTransactions('${esc(accountId)}', '${esc(transactionType)}', '${esc(nextPage)}', '$transactionsLimit', '$completionKey')")
    }

    fun getC14url(asset: String, account: String, amount: String = "", completion: (IntegrationUrlData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getC14url")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<IntegrationUrlData, IntegrationUrlResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getC14url('${esc(asset)}', '${esc(account)}', '${esc(amount)}', '$completionKey')")
    }

    fun cleanup() {
        webView?.let { webView ->
            webView.removeJavascriptInterface("bladeMessageHandler")
            webView.webViewClient = object : WebViewClient() {} // empty WebViewClient
            webView.clearCache(true)
            webView.clearHistory()
            webView.destroy()
        }
        webView = null

        webViewInitialized = false
        deferCompletions.clear()
        apiKey = ""
        dAppCode = ""
    }

    ////////////////////////////////////////////////////////////////

    @JavascriptInterface
    fun postMessage(jsonString: String) {
        try {
            val response = gson.fromJson(jsonString, Response::class.java)

            if (response.completionKey == "") {
                throw Exception("Received JS response without completionKey")
            }

            val deferredCompletion = deferCompletions[response.completionKey]
            if (deferredCompletion != null) {
                if (response.error != null) {
                    deferredCompletion("", response.error)
                } else {
                    deferredCompletion(jsonString, null)
                }
            } else {
                throw Exception("Deferred function not exists")
            }
        } catch (e: Exception) {
            println(e)
            throw e
        }
    }

    private inline fun <Y, reified T:Result<Y>>typicalDeferredCallback(data: String, error: BladeJSError?, completion: (Y?, BladeJSError?) -> Unit) {
        if (error != null) {
            completion(null, error)
            return
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
        webView?.evaluateJavascript("javascript:$script", null)
    }

    private fun esc(string: String): String {
        return string.replace("'", "\\'")
    }

    private fun deferCompletion(forKey: String, completion: (data: String, error: BladeJSError?) -> Unit) {
        deferCompletions[forKey] = completion
    }

    private fun getCompletionKey(tag: String): String {
        completionId += 1
        return tag + completionId
    }
}
