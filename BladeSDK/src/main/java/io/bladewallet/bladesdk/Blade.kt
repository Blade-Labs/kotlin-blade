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
    private const val sdkVersion: String = "Kotlin@0.6.6"
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

        CoroutineScope(Dispatchers.Main).launch {
            try {
                remoteConfig = getRemoteConfig(network, dAppCode, sdkVersion, bladeEnv)
                visitorId = getVisitorId(remoteConfig.fpApiKey, context)
            } catch (e: java.lang.Exception) {
                initCompletion(null, BladeJSError("Init failed", e.toString()))
                return@launch
            }

            if (visitorId == "") {
                return@launch
            }

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

    /**
     * Get SDK info
     *
     * @param completion: callback function, with result of InfoData or BladeJSError
     */
    fun getInfo(completion: (InfoData?, BladeJSError?) -> Unit) {
        if (!webViewInitialized) {
            completion(null, BladeJSError("Error", "BladeSDK not initialized"))
            return
        }
        val completionKey = getCompletionKey("getInfo")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<InfoData, InfoResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getInfo('$completionKey')")
    }

    /**
     * Get balances by Hedera id (address)
     *
     * @param id Hedera account id
     * @param completion callback function, with result of BalanceData or BladeJSError
     */
    fun getBalance(id: String, completion: (BalanceData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getBalance")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<BalanceData, BalanceResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getBalance('${esc(id)}', '$completionKey')")
    }

    /**
     * Method to execure Hbar transfers from current account to receiver
     *
     * @param accountId: sender account id
     * @param accountPrivateKey: sender's private key to sign transfer transaction
     * @param receiverId: receiver
     * @param amount: amount
     * @param memo: memo (limited to 100 characters)
     * @param completion callback function, with result of TransferData or BladeJSError
     */
    fun transferHbars(accountId: String, accountPrivateKey: String, receiverId: String, amount: Double, memo: String, completion: (TransferData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("transferHbars")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransferData, TransferResponse>(data, error, completion)
        }
        executeJS("bladeSdk.transferHbars('${esc(accountId)}', '${esc(accountPrivateKey)}', '${esc(receiverId)}', '$amount', '${esc(memo)}', '$completionKey')")
    }

    /**
     * Method to execute token transfers from current account to receiver
     *
     * @param tokenId: token
     * @param accountId: sender account id
     * @param accountPrivateKey: sender's private key to sign transfer transaction
     * @param receiverId: receiver
     * @param amount: amount
     * @param memo: memo (limited to 100 characters)
     * @param freeTransfer: for tokens configured for this dAppCode on Blade backend
     * @param completion callback function, with result of TransferData or BladeJSError
     */
    fun transferTokens(tokenId: String, accountId: String, accountPrivateKey: String, receiverId: String, amount: Double, memo: String, freeTransfer: Boolean = true, completion: (TransferData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("transferTokens")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransferData, TransferResponse>(data, error, completion)
        }
        executeJS("bladeSdk.transferTokens('${esc(tokenId)}', '${esc(accountId)}', '${esc(accountPrivateKey)}', '${esc(receiverId)}', '$amount', '${esc(memo)}', $freeTransfer, '$completionKey')")
    }

    /**
     * Method to create Hedera account
     *
     * @param deviceId: unique device id (advanced security feature, required only for some dApps)
     * @param completion callback function, with result of CreatedAccountData or BladeJSError
     */
    fun createHederaAccount(deviceId: String, completion: (CreatedAccountData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("createAccount")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<CreatedAccountData, CreatedAccountResponse>(data, error, completion)
        }
        executeJS("bladeSdk.createAccount('${esc(deviceId)}', '$completionKey')")
    }

    /**
     * Method to create Hedera account
     *
     * @param transactionId: can be received on createHederaAccount method, when busy network is busy, and account creation added to queue
     * @param seedPhrase: returned from createHederaAccount method, required for updating keys and proper response
     * @param completion callback function, with result of CreatedAccountData or BladeJSError
     */
    fun getPendingAccount(transactionId: String, seedPhrase: String, completion: (CreatedAccountData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getPendingAccount")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<CreatedAccountData, CreatedAccountResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getPendingAccount('${esc(transactionId)}', '${esc(seedPhrase)}', '$completionKey')")
    }

    /**
     * Method to delete Hedera account
     *
     * @param deleteAccountId: account to delete - id
     * @param deletePrivateKey: account to delete - private key
     * @param transferAccountId: The ID of the account to transfer the remaining funds to.
     * @param operatorAccountId: operator account Id
     * @param operatorPrivateKey: operator account private key
     * @param completion callback function, with result of TransactionReceiptData or BladeJSError
     */
    fun deleteHederaAccount(deleteAccountId: String, deletePrivateKey: String, transferAccountId: String, operatorAccountId: String, operatorPrivateKey: String, completion: (TransactionReceiptData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("deleteHederaAccount")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
        }
        executeJS("bladeSdk.deleteAccount('${esc(deleteAccountId)}', '${esc(deletePrivateKey)}', '${esc(transferAccountId)}', '${esc(operatorAccountId)}', '${esc(operatorPrivateKey)}', '$completionKey')")
    }

    /**
     * Get account evmAddress and calculated evmAddress from public key
     *
     * @param accountId: Hedera account id
     * @param completion callback function, with result of AccountInfoData or BladeJSError
     */
    fun getAccountInfo(accountId: String, completion: (AccountInfoData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getAccountInfo")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<AccountInfoData, AccountInfoResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getAccountInfo('${esc(accountId)}', '$completionKey')")
    }

    /**
     * Restore public and private key by seed phrase
     *
     * @param mnemonic: seed phrase
     * @param lookupNames: lookup for accounts
     * @param completion callback function, with result of PrivateKeyData or BladeJSError
     */
    fun getKeysFromMnemonic (mnemonic: String, lookupNames: Boolean = false, completion: (PrivateKeyData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getKeysFromMnemonic")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<PrivateKeyData, PrivateKeyResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getKeysFromMnemonic('${esc(mnemonic)}', $lookupNames, '$completionKey')")
    }

    /**
     * Sign message with private key
     *
     * @param messageString: message in base64 string
     * @param privateKey: private key string
     * @param completion callback function, with result of SignMessageData or BladeJSError
     */
    fun sign (messageString: String, privateKey: String, completion: (SignMessageData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("sign")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<SignMessageData, SignMessageResponse>(data, error, completion)
        }
        executeJS("bladeSdk.sign('${esc(messageString)}', '${esc(privateKey)}', '$completionKey')")
    }

    /**
     * Verify message signature with public key
     *
     * @param messageString: message in base64 string
     * @param signature: hex-encoded signature string
     * @param publicKey: public key string
     * @param completion callback function, with result of SignVerifyMessageData or BladeJSError
     */
    fun signVerify(messageString: String, signature: String, publicKey: String, completion: (SignVerifyMessageData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("signVerify")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<SignVerifyMessageData, SignVerifyMessageResponse>(data, error, completion)
        }
        executeJS("bladeSdk.signVerify('${esc(messageString)}', '${esc(signature)}', '${esc(publicKey)}', '$completionKey')")
    }

    /**
     * Method to call smart-contract function
     *
     * @param contractId: contract id
     * @param functionName: contract function name
     * @param params: function arguments (instance of ContractFunctionParameters)
     * @param accountId: sender account id
     * @param accountPrivateKey: sender's private key to sign transfer transaction
     * @param gas: gas amount for transaction (default 100000)
     * @param bladePayFee: blade pay fee, otherwise fee will be pay from sender accountId
     * @param completion callback function, with result of TransactionReceiptData or BladeJSError
     */
    fun contractCallFunction(contractId: String, functionName: String, params: ContractFunctionParameters, accountId: String, accountPrivateKey: String, gas: Int = 100000, bladePayFee: Boolean, completion: (TransactionReceiptData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("contractCallFunction")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
        }
        // TODO check if we need to escape ContractFunctionParams.encode() result \'
        executeJS("bladeSdk.contractCallFunction('${esc(contractId)}', '${esc(functionName)}', '${params.encode()}', '${esc(accountId)}', '${esc(accountPrivateKey)}', $gas, $bladePayFee, '$completionKey')")
    }

    /**
     * Method to call smart-contract query
     *
     * @param contractId: contract id
     * @param functionName: contract function name
     * @param params: function arguments (instance of ContractFunctionParameters)
     * @param accountId: sender account id
     * @param accountPrivateKey: sender's private key to sign transfer transaction
     * @param gas: gas amount for transaction (default 100000)
     * @param bladePayFee: blade pay fee, otherwise fee will be pay from sender accountId
     * @param returnTypes: List of return types, e.g. listOf("string", "int32")
     * @param completion callback function, with result of ContractQueryData or BladeJSError
     */
    fun contractCallQueryFunction(contractId: String, functionName: String, params: ContractFunctionParameters, accountId: String, accountPrivateKey: String, gas: Int = 100000, bladePayFee: Boolean, returnTypes: List<String>, completion: (ContractQueryData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("contractCallQueryFunction")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<ContractQueryData, ContractQueryResponse>(data, error, completion)
        }

        executeJS("bladeSdk.contractCallQueryFunction('${esc(contractId)}', '${esc(functionName)}', '${params.encode()}', '${esc(accountId)}', '${esc(accountPrivateKey)}', $gas, $bladePayFee, ${returnTypes.joinToString(",", "[", "]") {"\'${esc(it)}\'"}}, '$completionKey')")
    }

    /**
     * Sign message with private key (ethers lib)
     *
     * @param messageString: message in base64 string
     * @param privateKey: private key string
     * @param completion callback function, with result of SignMessageData or BladeJSError
     */
    fun ethersSign(messageString: String, privateKey: String, completion: (SignMessageData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("ethersSign")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<SignMessageData, SignMessageResponse>(data, error, completion)
        }
        executeJS("bladeSdk.ethersSign('${esc(messageString)}', '${esc(privateKey)}', '$completionKey')")
    }

    /**
     * Method to split signature into v-r-s
     *
     * @param signature: signature string "0x21fbf0696......"
     * @param completion callback function, with result of SplitSignatureData or BladeJSError
     */
    fun splitSignature(signature: String, completion: (SplitSignatureData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("splitSignature")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<SplitSignatureData, SplitSignatureResponse>(data, error, completion)
        }
        executeJS("bladeSdk.splitSignature('${esc(signature)}', '$completionKey')")
    }

    /**
     * Get signature for contract params into v-r-s
     *
     * @param params: function arguments (instance of ContractFunctionParameters)
     * @param accountPrivateKey: account private key string
     * @param completion callback function, with result of SplitSignatureData or BladeJSError
     */
    fun getParamsSignature(params: ContractFunctionParameters, accountPrivateKey: String, completion: (SplitSignatureData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getParamsSignature")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<SplitSignatureData, SplitSignatureResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getParamsSignature('${params.encode()}', '${esc(accountPrivateKey)}', '$completionKey')")
    }

    /**
     * Method to get transactions history
     *
     * @param accountId: accountId of history
     * @param transactionType: filter by type of transaction
     * @param nextPage: link from response to load next page of history
     * @param transactionsLimit: limit of transactions to load
     * @param completion callback function, with result of TransactionsHistoryData or BladeJSError
     */
    fun getTransactions(accountId: String, transactionType: String, nextPage: String = "", transactionsLimit: Int = 10, completion: (TransactionsHistoryData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getTransactions")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionsHistoryData, TransactionsHistoryResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getTransactions('${esc(accountId)}', '${esc(transactionType)}', '${esc(nextPage)}', '$transactionsLimit', '$completionKey')")
    }

    /**
     * Method to get C14 url for payment
     *
     * @param asset: USDC, HBAR, KARATE or C14 asset uuid
     * @param account: receiver account id
     * @param amount: amount to buy
     * @param completion callback function, with result of IntegrationUrlData or BladeJSError
     */
    fun getC14url(asset: String, account: String, amount: String = "", completion: (IntegrationUrlData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getC14url")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<IntegrationUrlData, IntegrationUrlResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getC14url('${esc(asset)}', '${esc(account)}', '${esc(amount)}', '$completionKey')")
    }

    /**
     * Get swap quotes from different services
     *
     * @param sourceCode: name (HBAR, KARATE, other token code)
     * @param sourceAmount: amount to swap, buy or sell
     * @param targetCode: name (HBAR, KARATE, USDC, other token code)
     * @param strategy: one of enum CryptoFlowServiceStrategy (Buy, Sell, Swap)
     * @param completion: callback function, with result of SwapQuotesData or BladeJSError
    */
    fun exchangeGetQuotes(sourceCode: String, sourceAmount: Double, targetCode: String, strategy: CryptoFlowServiceStrategy, completion: (SwapQuotesData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("exchangeGetQuotes")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<SwapQuotesData, SwapQuotesResponse>(data, error, completion)
        }
        executeJS("bladeSdk.exchangeGetQuotes('${esc(sourceCode)}', ${sourceAmount}, '${esc(targetCode)}', '${esc(strategy.value)}', '$completionKey')")
    }

    /**
     * Get configured url to buy or sell tokens or fiat
     *
     * @param strategy: Buy / Sell
     * @param accountId: account id
     * @param sourceCode: name (HBAR, KARATE, USDC, other token code)
     * @param sourceAmount: amount to buy/sell
     * @param targetCode: name (HBAR, KARATE, USDC, other token code)
     * @param slippage: slippage in percents. Transaction will revert if the price changes unfavorably by more than this percentage.
     * @param serviceId: service id to use for swap (saucerswap, onmeta, etc)
     * @param completion: callback function, with result of IntegrationUrlData or BladeJSError
    */
    fun getTradeUrl(strategy: CryptoFlowServiceStrategy, accountId: String, sourceCode: String, sourceAmount: Double, targetCode: String, slippage: Double, serviceId: String, completion: (IntegrationUrlData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getTradeUrl")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<IntegrationUrlData, IntegrationUrlResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getTradeUrl('${esc(strategy.value)}', '${esc(accountId)}', '${esc(sourceCode)}', ${sourceAmount}, '${esc(targetCode)}', ${slippage}, '${esc(serviceId)}', '$completionKey')")
    }

    /**
     * Swap tokens
     *
     * @param accountId: account id
     * @param accountPrivateKey: account private key
     * @param sourceCode: name (HBAR, KARATE, other token code)
     * @param sourceAmount: amount to swap
     * @param targetCode: name (HBAR, KARATE, other token code)
     * @param slippage: slippage in percents. Transaction will revert if the price changes unfavorably by more than this percentage.
     * @param serviceId: service id to use for swap (saucerswap, etc)
     * @param completion: callback function, with result of ResultData or BladeJSError
    */
    fun swapTokens(accountId: String, accountPrivateKey: String, sourceCode: String, sourceAmount: Double, targetCode: String, slippage: Double, serviceId: String, completion: (ResultData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("swapTokens")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<ResultData, ResultResponse>(data, error, completion)
        }
        executeJS("bladeSdk.swapTokens('${esc(accountId)}', '${esc(accountPrivateKey)}', '${esc(sourceCode)}', ${sourceAmount}, '${esc(targetCode)}', ${slippage}, '${esc(serviceId)}', '$completionKey')")
    }

    /**
     * Method to clean-up webView
     */
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
