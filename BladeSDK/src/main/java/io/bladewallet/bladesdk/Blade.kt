package io.bladewallet.bladesdk

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup.LayoutParams
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.Gson
import io.bladewallet.bladesdk.models.AccountInfoData
import io.bladewallet.bladesdk.models.AccountInfoResponse
import io.bladewallet.bladesdk.models.AccountPrivateData
import io.bladewallet.bladesdk.models.AccountPrivateResponse
import io.bladewallet.bladesdk.models.AccountProvider
import io.bladewallet.bladesdk.models.BalanceData
import io.bladewallet.bladesdk.models.BalanceResponse
import io.bladewallet.bladesdk.models.BladeEnv
import io.bladewallet.bladesdk.models.BladeJSError
import io.bladewallet.bladesdk.models.CoinInfoData
import io.bladewallet.bladesdk.models.CoinInfoResponse
import io.bladewallet.bladesdk.models.CoinListData
import io.bladewallet.bladesdk.models.CoinListResponse
import io.bladewallet.bladesdk.models.ContractCallQueryRecordsData
import io.bladewallet.bladesdk.models.ContractCallQueryRecordsResponse
import io.bladewallet.bladesdk.models.CreateScheduleData
import io.bladewallet.bladesdk.models.CreateScheduleResponse
import io.bladewallet.bladesdk.models.CreateTokenData
import io.bladewallet.bladesdk.models.CreateTokenResponse
import io.bladewallet.bladesdk.models.CreatedAccountData
import io.bladewallet.bladesdk.models.CreatedAccountResponse
import io.bladewallet.bladesdk.models.CryptoFlowServiceStrategy
import io.bladewallet.bladesdk.models.InfoData
import io.bladewallet.bladesdk.models.InfoResponse
import io.bladewallet.bladesdk.models.IntegrationUrlData
import io.bladewallet.bladesdk.models.IntegrationUrlResponse
import io.bladewallet.bladesdk.models.KeyRecord
import io.bladewallet.bladesdk.models.KnownChainIds
import io.bladewallet.bladesdk.models.NFTStorageConfig
import io.bladewallet.bladesdk.models.NodesData
import io.bladewallet.bladesdk.models.NodesResponse
import io.bladewallet.bladesdk.models.RemoteConfig
import io.bladewallet.bladesdk.models.Response
import io.bladewallet.bladesdk.models.Result
import io.bladewallet.bladesdk.models.ResultData
import io.bladewallet.bladesdk.models.ResultResponse
import io.bladewallet.bladesdk.models.ScheduleTransactionTransfer
import io.bladewallet.bladesdk.models.ScheduleTransactionType
import io.bladewallet.bladesdk.models.SignMessageData
import io.bladewallet.bladesdk.models.SignMessageResponse
import io.bladewallet.bladesdk.models.SignVerifyMessageData
import io.bladewallet.bladesdk.models.SignVerifyMessageResponse
import io.bladewallet.bladesdk.models.SplitSignatureData
import io.bladewallet.bladesdk.models.SplitSignatureResponse
import io.bladewallet.bladesdk.models.SupportedEncoding
import io.bladewallet.bladesdk.models.SwapQuotesData
import io.bladewallet.bladesdk.models.SwapQuotesResponse
import io.bladewallet.bladesdk.models.TokenDropData
import io.bladewallet.bladesdk.models.TokenDropResponse
import io.bladewallet.bladesdk.models.TokenInfoData
import io.bladewallet.bladesdk.models.TokenInfoResponse
import io.bladewallet.bladesdk.models.TransactionReceiptData
import io.bladewallet.bladesdk.models.TransactionReceiptResponse
import io.bladewallet.bladesdk.models.TransactionResponseData
import io.bladewallet.bladesdk.models.TransactionResponseResponse
import io.bladewallet.bladesdk.models.TransactionsHistoryData
import io.bladewallet.bladesdk.models.TransactionsHistoryResponse
import io.bladewallet.bladesdk.models.UserInfoData
import io.bladewallet.bladesdk.models.UserInfoResponse
import kotlinx.coroutines.*

@SuppressLint("StaticFieldLeak")
object Blade {
    private const val sdkVersion: String = "Kotlin@1.0.0"
    private var webView: WebView? = null
    private lateinit var apiKey: String
    private var visitorId: String = ""
    private lateinit var remoteConfig: RemoteConfig
    private var chainId: KnownChainIds = KnownChainIds.HEDERA_TESTNET
    private lateinit var dAppCode: String
    private var webViewInitialized: Boolean = false
    private var completionId: Int = 0
    private lateinit var initCompletion: ((InfoData?, BladeJSError?) -> Unit)
    private var deferCompletions = mutableMapOf<String, (String, BladeJSError?) -> Unit>()
    private val gson = Gson()

    /**
     * Init instance of BladeSDK for correct work with Blade API and Hedera network.
     *
     * @param apiKey Unique key for API provided by Blade team.
     * @param dAppCode your dAppCode - request specific one by contacting Bladelabs team
     * @param chainId one of supported chains from KnownChainIds
     * @param bladeEnv field to set BladeAPI environment (Prod, CI). Prod used by default.
     * @param context android context
     * @param force optional field to force init. Will not crash if already initialized
     * @param completion: callback function, with result of InfoData or BladeJSError
     * @return {InfoData} with information about Blade instance, including visitorId
     * @sample
     * TODO
     * Blade.initialize(
     *     Config.apiKey, Config.dAppCode, Config.network, Config.bladeEnv, requireContext(), false
     * ) { infoData, error ->
     *     println(infoData ?: error)
     * }
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun initialize(apiKey: String, chainId: KnownChainIds, dAppCode: String, bladeEnv: BladeEnv = BladeEnv.Prod, context: Context, force: Boolean = false, completion: (InfoData?, BladeJSError?) -> Unit) {
        if (webViewInitialized && !force) {
            println("Error while doing double init of BladeSDK")
            throw Exception("Error while doing double init of BladeSDK")
        }
        initCompletion = completion
        this.apiKey = apiKey
        this.dAppCode = dAppCode
        this.chainId = chainId

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val sharedPreferences = context.getSharedPreferences(context.resources.getString(R.string.sharedPreferences), Context.MODE_PRIVATE)
                if (
                    sharedPreferences.getString(context.resources.getString(R.string.visitorIdEnvKey), "") == bladeEnv.toString() &&
                    (System.currentTimeMillis() / 1000).toInt() - sharedPreferences.getInt(context.resources.getString(R.string.visitorIdTimestampKey), 0) < 3600 * 24 * 30
                ) {
                    // if visitorId was saved less than 30 days ago and in the same environment
                    visitorId = sharedPreferences.getString(context.resources.getString(R.string.visitorIdKey), "") ?: ""
                }
                if (visitorId == "") {
                    remoteConfig = getRemoteConfig(dAppCode, sdkVersion, bladeEnv)
                    visitorId = getVisitorId(remoteConfig.fpApiKey, context)
                    sharedPreferences.edit()
                        .putString(context.resources.getString(R.string.visitorIdEnvKey), bladeEnv.toString())
                        .putString(context.resources.getString(R.string.visitorIdKey), visitorId)
                        .putInt(context.resources.getString(R.string.visitorIdTimestampKey), (System.currentTimeMillis() / 1000).toInt())
                        .apply()
                }
            } catch (e: java.lang.Exception) {
                initCompletion(null, BladeJSError("Init failed", e.toString()))
                return@launch
            }

            if (visitorId == "") {
                return@launch
            }

            if (webView != null) {
                webView?.clearCache(true)
                webView?.clearHistory()
                webView?.destroy()
                webView = null
            }

            webView = WebView(context)
            webView?.let { webView ->
                webView.layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )

                webView.settings.javaScriptEnabled = true
                webView.settings.domStorageEnabled = true // to enable localStorage for Magic.link
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
                        executeJS("bladeSdk.init('${esc(apiKey)}', '${chainId.value}', '${esc(dAppCode)}', '$visitorId', '$bladeEnv', '${esc(sdkVersion)}', '$completionKey')")
                    }
                }
            }
        }
    }

    /**
     * Get SDK info and check if SDK initialized
     *
     * @param completion: callback function, with result of InfoData or BladeJSError
     * @return {InfoData} with information about Blade instance, including visitorId
     * @sample
     * Blade.getInfo { infoData, error ->
     *     println(infoData ?: error)
     * }
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
     * Set active user for further operations.
     *
     * @param accountProvider one of supported providers: PrivateKey or Magic
     * @param accountIdOrEmail account id (0.0.xxxxx, 0xABCDEF..., EMAIL) or empty string for some ChainId
     * @param privateKey private key for account (hex encoded privateKey with DER-prefix or 0xABCDEF...) In case of Magic provider - empty string
     * @return {UserInfoData} with information about active user
     * @sample
     * Blade.setUser(AccountProvider.PrivateKey, "0.0.1234", "302d300706052b8104000a032200029dc73991b0d9cd...") { userInfoData, error ->
     *     println(userInfoData ?: error)
     * }
     */
    fun setUser(accountProvider: AccountProvider, accountIdOrEmail: String, privateKey: String, completion: (UserInfoData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("setUser")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<UserInfoData, UserInfoResponse>(data, error, completion)
        }

        executeJS("bladeSdk.setUser('${accountProvider.value}', '${esc(accountIdOrEmail)}', '${esc(privateKey)}', '$completionKey')")
    }

    /**
     * Reset active user
     *
     * @return {UserInfoData} with information about active user
     * @sample
     * Blade.resetUser { userInfoData, error ->
     *     println(userInfoData ?: error)
     * }
     */
    fun resetUser(completion: (UserInfoData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("resetUser")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<UserInfoData, UserInfoResponse>(data, error, completion)
        }

        executeJS("bladeSdk.resetUser('$completionKey')")
    }



    /**
     * Get balance and token balances for specific account.
     *
     * @param accountAddress Hedera account id (0.0.xxxxx) or Ethereum address (0x...) or empty string to use current user account
     * @param completion callback function, with result of BalanceData or BladeJSError
     * @return {BalanceData} with information about Hedera account balances (hbar and list of token balances)
     * @sample
     * Blade.getBalance("0.0.45467464") { result, error ->
     *     println("${ result ?: error}")
     * }
     */
    fun getBalance(accountAddress: String, completion: (BalanceData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getBalance")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<BalanceData, BalanceResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getBalance('${esc(accountAddress)}', '$completionKey')")
    }

    /**
     * Send HBAR/ETH to specific account.
     *
     * @param receiverAddress receiver address (0.0.xxxxx, 0x123456789abcdef...)
     * @param amount amount of currency to send (decimal string)
     * @param memo: memo (limited to 100 characters)
     * @param completion callback function, with result of TransactionReceiptData or BladeJSError
     * @return {TransactionResponseData} receipt
     * @sample
     * val receiverAddress = "0.0.10002"
     * val amount = "2.5"
     *
     * Blade.transferBalance(
     *     receiverAddress,
     *     amount,
     *     "Some memo text"
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun transferBalance(receiverAddress: String, amount: String, memo: String, completion: (TransactionResponseData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("transferBalance")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionResponseData, TransactionResponseResponse>(data, error, completion)
        }
        executeJS("bladeSdk.transferBalance('${esc(receiverAddress)}', '${esc(amount)}', '${esc(memo)}', '$completionKey')")
    }

    /**
     * Send token to specific address
     *
     * @param tokenAddress token address to send (0.0.xxxxx or 0x123456789abcdef...)
     * @param receiverAddress receiver account address (0.0.xxxxx or 0x123456789abcdef...)
     * @param amountOrSerial amount of fungible tokens to send (with token-decimals correction) on NFT serial number. (e.g. amount 0.01337 when token decimals 8 will send 1337000 units of token)
     * @param memo: transaction memo (limited to 100 characters)
     * @param usePaymaster if true, Paymaster account will pay fee transaction. Only for single dApp configured fungible-token. In that case tokenId not used
     * @param completion callback function, with result of TransactionReceiptData or BladeJSError
     * @return {TransactionResponseData} receipt
     * @sample
     * val tokenAddress = "0.0.1337"
     * val receiverAddress = "0.0.10002"
     * val amount = "2.5"
     *
     * Blade.transferTokens(
     *     tokenAddress,
     *     receiverAddress,
     *     amount,
     *     "Token transfer memo"
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun transferTokens(tokenAddress: String, receiverAddress: String, amountOrSerial: String, memo: String, usePaymaster: Boolean = true, completion: (TransactionResponseData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("transferTokens")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionResponseData, TransactionResponseResponse>(data, error, completion)
        }
        executeJS("bladeSdk.transferTokens('${esc(tokenAddress)}', '${esc(receiverAddress)}', '${esc(amountOrSerial)}', '${esc(memo)}', $usePaymaster, '$completionKey')")
    }

    /**
     * Get list of all available coins on CoinGecko.
     *
     * @param completion callback function, with result of CoinListData or BladeJSError
     * @return {CoinListData} with list of coins described by name, alias, platforms
     * @sample
     * Blade.getCoinList { result, error ->
     *     if (result != null) {
     *         for (coin in result.coins) {
     *             println(coin)
     *         }
     *     } else {
     *         println(error)
     *     }
     * }
     */
    fun getCoinList(completion: (CoinListData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getCoinList")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<CoinListData, CoinListResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getCoinList('$completionKey')")
    }

    /**
     * Get coin price and coin info from CoinGecko. Search can be coin id or address in one of the coin platforms.
     * In addition to the price in USD, the price in the currency you specified is returned
     * @param search CoinGecko coinId, or address in one of the coin platforms or `hbar` (default, alias for `hedera-hashgraph`)
     * @param currency result currency for price field
     * @param completion callback function, with result of CoinListData or BladeJSError
     * @return {CoinInfoData}
     * @sample
     * Blade.getCoinPrice(
     *     search = "hbar",
     *     currency = "uah"
     * ) { result, bladeJSError ->
     *     println("${result ?: bladeJSError}")
     * }
     */
    fun getCoinPrice(search: String, currency: String = "usd", completion: (CoinInfoData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getCoinPrice")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<CoinInfoData, CoinInfoResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getCoinPrice('${esc(search)}', '${esc(currency)}', '$completionKey')")
    }

    /**
     * Call contract function. Directly or via BladeAPI using paymaster account (fee will be paid by Paymaster account), depending on your dApp configuration.
     *
     * @param contractAddress - contract address (0.0.xxxxx or 0x123456789abcdef...)
     * @param functionName: name of the contract function to call
     * @param params: function argument. Can be generated with {@link ContractFunctionParameters} object
     * @param gas: gas limit for the transaction
     * @param usePaymaster: if true, fee will be paid by Paymaster account (note: msg.sender inside the contract will be Paymaster account)
     * @param completion callback function, with result of TransactionReceiptData or BladeJSError
     * @return {TransactionReceiptData} receipt
     * @sample
     * val contractAddress = "0.0.123456"
     * val functionName = "set_message"
     * val parameters = ContractFunctionParameters().addString("hello")
     * val gas = 155000
     * val usePaymaster = false
     *
     * Blade.contractCallFunction(
     *     contractId,
     *     functionName,
     *     parameters,
     *     gas,
     *     usePaymaster
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun contractCallFunction(contractAddress: String, functionName: String, params: ContractFunctionParameters, gas: Int = 100000, usePaymaster: Boolean, completion: (TransactionReceiptData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("contractCallFunction")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
        }
        executeJS("bladeSdk.contractCallFunction('${esc(contractAddress)}', '${esc(functionName)}', '${params.encode()}', $gas, $usePaymaster, '$completionKey')")
    }

    /**
     * Call query on contract function. Similar to {@link contractCallFunction} can be called directly or via BladeAPI using Paymaster account.
     *
     * @param contractAddress contract address (0.0.xxxxx or 0x123456789abcdef...)
     * @param functionName: name of the contract function to call
     * @param params: function argument. Can be generated with {@link ContractFunctionParameters} object
     * @param gas: gas limit for the transaction
     * @param usePaymaster: if true, the fee will be paid by paymaster account (note: msg.sender inside the contract will be Paymaster account)
     * @param returnTypes: List of return types, e.g. listOf("string", "int32")
     * @param completion callback function, with result of ContractQueryData or BladeJSError
     * @return {ContractCallQueryRecordsData} contract query call result
     * @sample
     * val contractAddress = "0.0.123456"
     * val functionName = "get_message"
     * val parameters = ContractFunctionParameters()
     * val gas = 55000
     * val usePaymaster = false
     * val returnTypes = listOf("string", "int32")
     *
     * Blade.contractCallQueryFunction(
     *     contractAddress,
     *     functionName,
     *     parameters,
     *     gas,
     *     usePaymaster,
     *     returnTypes
     * ) { result, error ->
     *     lifecycleScope.launch {
     *         println(result ?: error)
     *     }
     * }
     */
    fun contractCallQueryFunction(contractAddress: String, functionName: String, params: ContractFunctionParameters, gas: Int = 100000, usePaymaster: Boolean, returnTypes: List<String>, completion: (ContractCallQueryRecordsData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("contractCallQueryFunction")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<ContractCallQueryRecordsData, ContractCallQueryRecordsResponse>(data, error, completion)
        }

        executeJS("bladeSdk.contractCallQueryFunction('${esc(contractAddress)}', '${esc(functionName)}', '${params.encode()}', $gas, $usePaymaster, ${returnTypes.joinToString(",", "[", "]") {"\'${esc(it)}\'"}}, '$completionKey')")
    }

    /**
     * Create scheduled transaction
     *
     * @param accountId account id (0.0.xxxxx)
     * @param accountPrivateKey account key (hex encoded privateKey with DER-prefix)
     * @param type schedule transaction type (currently only TRANSFER supported)
     * @param transfers array of transfers to schedule (HBAR, FT, NFT)
     * @param usePaymaster if true, Paymaster account will pay transaction fee (also dApp had to be configured for free schedules)
     * @param completion callback function, with result of CreateScheduleData or BladeJSError
     * @return {CreateScheduleData} scheduleId
     * @sample
     * val senderId = "0.0.10001"
     * val tokenId = "0.0.1337"
     * var scheduleId = ""
     *
     * Blade.createScheduleTransaction(
     *     type = ScheduleTransactionType.TRANSFER,
     *     transfers = listOf(
     *         ScheduleTransactionTransferHbar(sender = senderId, receiver = receiverId, 10000000),
     *         ScheduleTransactionTransferToken(sender = senderId, receiver = receiverId, tokenId = tokenId, value = 3)
     *     ),
     *     usePaymaster = true,
     * ) { result, error ->
     *     if (result != null) {
     *         println(result.scheduleId)
     *     }
     * }
     */
    fun createScheduleTransaction(
        type: ScheduleTransactionType,
        transfers: List<ScheduleTransactionTransfer>,
        usePaymaster: Boolean = false,
        completion: (CreateScheduleData?, BladeJSError?) -> Unit
    ) {
        val completionKey = getCompletionKey("createScheduleTransaction")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<CreateScheduleData, CreateScheduleResponse>(data, error, completion)
        }
        executeJS("bladeSdk.createScheduleTransaction('${esc(type.toString())}', ${transfers.joinToString(",", "[", "]") {gson.toJson(it)}}, $usePaymaster, '$completionKey')")
    }

    /**
     * Method to sign scheduled transaction
     *
     * @param scheduleId scheduled transaction id (0.0.xxxxx)
     * @param receiverAccountAddress account id of receiver for additional validation in case of dApp freeSchedule transactions configured
     * @param usePaymaster if true, Paymaster account will pay transaction fee (also dApp had to be configured for free schedules)
     * @param completion callback function, with result of TransactionReceiptData or BladeJSError
     * @return {TransactionReceiptData} receipt
     * @sample
     * val receiverAddress = "0.0.10002"
     * var scheduleId = "0.0...." // result of createScheduleTransaction on receiver side
     *
     * Blade.signScheduleId(
     *     scheduleId = scheduleId,
     *     receiverAccountAddress = receiverId,
     *     usePaymaster = true
     * ) { result, bladeJSError ->
     *     println(result ?: bladeJSError)
     * }
     */
    fun signScheduleId(
        scheduleId: String,
        receiverAccountAddress: String = "",
        usePaymaster: Boolean = false,
        completion: (TransactionReceiptData?, BladeJSError?) -> Unit
    ) {
        val completionKey = getCompletionKey("signScheduleId")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
        }
        executeJS("bladeSdk.signScheduleId('${esc(scheduleId)}', '${esc(receiverAccountAddress)}', $usePaymaster, '$completionKey')")
    }

    /**
     * Create new Hedera account (ECDSA). Only for configured dApps. Depending on dApp config Blade create account, associate tokens, etc.
     * In case of not using pre-created accounts pool and network high load, this method can return transactionId and no accountId.
     * In that case account creation added to queue, and you should wait some time and call `getPendingAccount()` method.
     *
     * @param privateKey: optional field if you need specify account key (hex encoded privateKey with DER-prefix)
     * @param deviceId: optional field unique device id (advanced security feature, required only for some dApps)
     * @param completion callback function, with result of CreatedAccountData or BladeJSError
     * @return {CreatedAccountData} new account data, including private key and account id
     * @sample
     * Blade.createAccount() { result, error ->
     *     println(result ?: error)
     * }
     */
    fun createAccount(privateKey: String = "", deviceId: String = "", completion: (CreatedAccountData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("createAccount")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<CreatedAccountData, CreatedAccountResponse>(data, error, completion)
        }
        executeJS("bladeSdk.createAccount('${esc(privateKey)}', '${esc(deviceId)}', '$completionKey')")
    }

    /**
     * Delete Hedera account. This method requires account private key and operator private key. Operator is the one who paying fees
     *
     * @param deleteAccountId: account id of account to delete (0.0.xxxxx)
     * @param deletePrivateKey: account private key (DER encoded hex string)
     * @param transferAccountId: The ID of the account to transfer the remaining funds to.
     * @param completion callback function, with result of TransactionReceiptData or BladeJSError
     * @return {TransactionReceiptData} receipt
     * @sample
     * val deleteAccountId = "0.0.65468464"
     * val deletePrivateKey = "3030020100300706052b8104000a04220420ebc..."
     * val transferAccountId = "0.0.10001"
     *
     * Blade.deleteAccount(
     *     deleteAccountId,
     *     deletePrivateKey,
     *     transferAccountId,
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun deleteAccount(deleteAccountId: String, deletePrivateKey: String, transferAccountId: String, completion: (TransactionReceiptData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("deleteAccount")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
        }
        executeJS("bladeSdk.deleteAccount('${esc(deleteAccountId)}', '${esc(deletePrivateKey)}', '${esc(transferAccountId)}', '$completionKey')")
    }

    /**
     * Get account info.
     * EvmAddress is address of Hedera account if exists. Else accountId will be converted to solidity address.
     * CalculatedEvmAddress is calculated from account public key. May be different from evmAddress.
     *
     * @param accountId: Hedera account id (0.0.xxxxx)
     * @param completion callback function, with result of AccountInfoData or BladeJSError
     * @return {AccountInfoData}
     * @sample
     * Blade.getAccountInfo("0.0.10002") { accountInfoData, error ->
     *     println(accountInfoData ?: error)
     * }
     */
    fun getAccountInfo(accountId: String, completion: (AccountInfoData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getAccountInfo")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<AccountInfoData, AccountInfoResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getAccountInfo('${esc(accountId)}', '$completionKey')")
    }

    /**
     * Get Node list and use it for choosing account stacking node
     *
     * @param completion callback function, with result of NodesData or BladeJSError
     * @return {NodesData} node list
     * @sample
     * Blade.getNodeList { nodeListData, error ->
     *     println(nodeListData ?: error)
     * }
     */
    fun getNodeList(completion: (NodesData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getNodeList")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<NodesData, NodesResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getNodeList('$completionKey')")
    }

    /**
     * Stake/unstake account
     *
     * @param nodeId node id to stake to. If negative or null, account will be unstaked
     * @param completion callback function, with result of TransactionReceiptData or BladeJSError
     * @return {TransactionReceiptData} receipt
     * @sample
     * Blade.stakeToNode(5) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun stakeToNode(nodeId: Int, completion: (TransactionReceiptData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("stakeToNode")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
        }
        executeJS("bladeSdk.stakeToNode(${nodeId}, '$completionKey')")
    }

    /**
     * Get accounts list and keys from private key or mnemonic
     * Supporting standard and legacy key derivation.
     * Every key with account will be returned.
     *
     * @param keyOrMnemonic BIP39 mnemonic, private key with DER header
     * @param completion callback function, with result of AccountPrivateData or BladeJSError
     * @return {AccountPrivateData} list of found accounts with private keys
     * @sample
     * Blade.searchAccounts("purity slab doctor swamp tackle rebuild summer bean craft toddler blouse switch") { result, error ->
     *     println(result ?: error)
     * }
     */
    fun searchAccounts (keyOrMnemonic: String, completion: (AccountPrivateData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("searchAccounts")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<AccountPrivateData, AccountPrivateResponse>(data, error, completion)
        }
        executeJS("bladeSdk.searchAccounts('${esc(keyOrMnemonic)}', '$completionKey')")
    }

    /**
     * Bladelink drop to account
     *
     * @param secretNonce configured for dApp. Should be kept in secret
     * @param completion callback function, with result of TokenDropData or BladeJSError
     * @return {TokenDropData} status
     * @sample
     * val secretNonce = "[ REDACTED ]"
     *
     * Blade.dropTokens(
     *     secretNonce,
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun dropTokens (secretNonce: String, completion: (TokenDropData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("dropTokens")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TokenDropData, TokenDropResponse>(data, error, completion)
        }
        executeJS("bladeSdk.dropTokens('${esc(secretNonce)}', '$completionKey')")
    }

    /**
     * Sign encoded message with private key. Returns hex-encoded signature.
     *
     * @param encodedMessage encoded message to sign
     * @param encoding one of the supported encodings (hex/base64/utf8)
     * @param likeEthers to get signature in ethers format. Works only for ECDSA keys. Ignored on chains other than Hedera
     * @param completion callback function, with result of SignMessageData or BladeJSError
     * @return {SignMessageData} signature
     * @sample
     * import java.util.Base64
     *
     * // ...
     *
     * val encodedMessage = "hello"
     * val encoding = SupportedEncoding.utf8
     * val likeEthers = true
     *
     * Blade.sign(
     *     encodedMessage,
     *     encoding,
     *     likeEthers
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun sign (encodedMessage: String, encoding: SupportedEncoding, likeEthers: Boolean, completion: (SignMessageData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("sign")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<SignMessageData, SignMessageResponse>(data, error, completion)
        }
        executeJS("bladeSdk.sign('${esc(encodedMessage)}', '$encoding', $likeEthers, '$completionKey')")
    }

    /**
     * Verify message signature with public key
     *
     * @param encodedMessage encoded message (same as provided to `sign()` method)
     * @param encoding one of the supported encodings (hex/base64/utf8)
     * @param signature hex-encoded signature (result from `sign()` method)
     * @param addressOrPublicKey EVM-address, publicKey, or Hedera address (0x11f8D856FF2aF6700CCda4999845B2ed4502d8fB, 0x0385a2fa81f8acbc47fcfbae4aeee6608c2d50ac2756ed88262d102f2a0a07f5b8, 0.0.1512, or empty for current account)
     * @param completion callback function, with result of SignVerifyMessageData or BladeJSError
     * @return {SignVerifyMessageData} verification result
     * @sample
     * val encodedMessage = "hello"
     * val encoding = SupportedEncoding.utf8
     * val signature = "27cb9d51434cf1e76d7ac515b19442c619f641e6fccddbf4a3756b14466becb6992dc1d2a82268018147141fc8d66ff9ade43b7f78c176d070a66372d655f942"
     * val addressOrPublicKey = "302d300706052b8104000a032200029dc73991b0d9cdbb59b2cd0a97a0eaff6de801726cb39804ea9461df6be2dd30"
     *
     * Blade.verify(
     *     encodedMessage,
     *     encoding,
     *     signature,
     *     addressOrPublicKey
     * ) { result, error ->
     *     lifecycleScope.launch {
     *         println(result ?: error)
     *     }
     * }
     */
    fun verify(encodedMessage: String, encoding: SupportedEncoding, signature: String, addressOrPublicKey: String, completion: (SignVerifyMessageData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("verify")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<SignVerifyMessageData, SignVerifyMessageResponse>(data, error, completion)
        }
        executeJS("bladeSdk.verify('${esc(encodedMessage)}', '$encoding', '${esc(signature)}', '${esc(addressOrPublicKey)}', '$completionKey')")
    }

    /**
     * Split signature to v-r-s format.
     *
     * @param signature: hex-encoded signature
     * @param completion callback function, with result of SplitSignatureData or BladeJSError
     * @return {SplitSignatureData} v-r-s signature
     * @sample
     * Blade.splitSignature(
     *     "0x27cb9d51434cf1e76d7ac515b19442c619f641e6fccddbf4a3756b14466becb6992dc1d2a82268018147141fc8d66ff9ade43b7f78c176d070a66372d655f942",
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun splitSignature(signature: String, completion: (SplitSignatureData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("splitSignature")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<SplitSignatureData, SplitSignatureResponse>(data, error, completion)
        }
        executeJS("bladeSdk.splitSignature('${esc(signature)}', '$completionKey')")
    }

    /**
     * Get v-r-s signature of contract function params
     *
     * @param params: data to sign. (instance of ContractFunctionParameters)
     * @param completion callback function, with result of SplitSignatureData or BladeJSError
     * @return {SplitSignatureData} v-r-s signature
     * @sample
     * val parameters = ContractFunctionParameters().addString("hello")
     * Blade.getParamsSignature(
     *     parameters,
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun getParamsSignature(params: ContractFunctionParameters, completion: (SplitSignatureData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getParamsSignature")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<SplitSignatureData, SplitSignatureResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getParamsSignature('${params.encode()}', '$completionKey')")
    }

    /**
     * Get transactions history for account. Can be filtered by transaction type.
     * Transaction requested from mirror node. Every transaction requested for child transactions. Result are flattened.
     * If transaction type is not provided, all transactions will be returned.
     * If transaction type is CRYPTOTRANSFERTOKEN records will additionally contain plainData field with decoded data.
     *
     * @param accountId: account id to get transactions for (0.0.xxxxx)
     * @param transactionType: one of enum MirrorNodeTransactionType or "CRYPTOTRANSFERTOKEN"
     * @param nextPage: link to next page of transactions from previous request
     * @param transactionsLimit: number of transactions to return. Speed of request depends on this value if transactionType is set.
     * @param completion callback function, with result of TransactionsHistoryData or BladeJSError
     * @return {TransactionsHistoryData} transactions list
     * @sample
     * Blade.getTransactions(
     *     accountId = "0.0.10002",
     *     transactionType = "",
     *     nextPage = "",
     *     transactionsLimit = 15
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun getTransactions(accountId: String, transactionType: String, nextPage: String = "", transactionsLimit: Int = 10, completion: (TransactionsHistoryData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getTransactions")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionsHistoryData, TransactionsHistoryResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getTransactions('${esc(accountId)}', '${esc(transactionType)}', '${esc(nextPage)}', '$transactionsLimit', '$completionKey')")
    }

    /**
     * Get quotes from different services for buy, sell or swap
     *
     * @param sourceCode: name (HBAR, KARATE, other token code)
     * @param sourceAmount: amount to swap, buy or sell
     * @param targetCode: name (HBAR, KARATE, USDC, other token code)
     * @param strategy: one of enum CryptoFlowServiceStrategy (Buy, Sell, Swap)
     * @param completion: callback function, with result of SwapQuotesData or BladeJSError
     * @return {SwapQuotesData} quotes from different providers
     * @sample
     * Blade.exchangeGetQuotes(
     *     sourceCode = "EUR",
     *     sourceAmount = 50.0,
     *     targetCode = "HBAR",
     *     strategy = CryptoFlowServiceStrategy.BUY
     * ) { result, error ->
     *     println(result ?: error)
     * }
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
     * @param redirectUrl: url to redirect after final step
     * @param completion: callback function, with result of IntegrationUrlData or BladeJSError
     * @return {IntegrationUrlData} url to open
     * @sample
     * Blade.getTradeUrl(
     *     strategy = CryptoFlowServiceStrategy.BUY,
     *     accountId = "0.0.10002",
     *     sourceCode = "EUR",
     *     sourceAmount = 50.0,
     *     targetCode = "HBAR",
     *     slippage = 0.5,
     *     serviceId = "moonpay"
     * ) { result, error ->
     *     println(result ?: error)
     * }
    */
    fun getTradeUrl(strategy: CryptoFlowServiceStrategy, accountId: String, sourceCode: String, sourceAmount: Double, targetCode: String, slippage: Double, serviceId: String, redirectUrl: String = "", completion: (IntegrationUrlData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getTradeUrl")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<IntegrationUrlData, IntegrationUrlResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getTradeUrl('${esc(strategy.value)}', '${esc(accountId)}', '${esc(sourceCode)}', ${sourceAmount}, '${esc(targetCode)}', ${slippage}, '${esc(serviceId)}', '${esc(redirectUrl)}', '$completionKey')")
    }

    /**
     * Swap tokens
     *
     * @param sourceCode: name (HBAR, KARATE, other token code)
     * @param sourceAmount: amount to swap
     * @param targetCode: name (HBAR, KARATE, other token code)
     * @param slippage: slippage in percents. Transaction will revert if the price changes unfavorably by more than this percentage.
     * @param serviceId: service id to use for swap (saucerswap, etc)
     * @param completion: callback function, with result of ResultData or BladeJSError
     * @return {ResultData} swap result
     * @sample
     * val sourceCode = "USDC"
     * val targetCode = "KARATE"
     *
     * Blade.swapTokens(
     *     sourceCode,
     *     sourceAmount = 123.4,
     *     targetCode,
     *     slippage = 0.5,
     *     serviceId = "moonpay"
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun swapTokens(sourceCode: String, sourceAmount: Double, targetCode: String, slippage: Double, serviceId: String, completion: (ResultData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("swapTokens")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<ResultData, ResultResponse>(data, error, completion)
        }
        executeJS("bladeSdk.swapTokens('${esc(sourceCode)}', ${sourceAmount}, '${esc(targetCode)}', ${slippage}, '${esc(serviceId)}', '$completionKey')")
    }

    /**
     * Create token (NFT or Fungible Token)
     *
     * @param tokenName: token name (string up to 100 bytes)
     * @param tokenSymbol: token symbol (string up to 100 bytes)
     * @param isNft: set token type NFT
     * @param keys: token keys
     * @param decimals: token decimals (0 for nft)
     * @param initialSupply: token initial supply (0 for nft)
     * @param maxSupply: token max supply
     * @param completion: callback function, with result of CreateTokenData or BladeJSError
     * @return {CreateTokenData} token id
     * @sample
     * val keys = listOf(
     *     KeyRecord(Config.adminPrivateKey, KeyType.admin)
     * )
     * Blade.createToken(
     *         tokenName = "Blade Demo Token",
     *         tokenSymbol = "GD",
     *         isNft = true,
     *         keys,
     *         decimals = 0,
     *         initialSupply = 0,
     *         maxSupply = 250
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun createToken(
        tokenName: String,
        tokenSymbol: String,
        isNft: Boolean,
        keys: List<KeyRecord>,
        decimals: Int,
        initialSupply: Int,
        maxSupply: Int,
        completion: (CreateTokenData?, BladeJSError?) -> Unit
    ) {
        val completionKey = getCompletionKey("createToken")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<CreateTokenData, CreateTokenResponse>(data, error, completion)
        }
        executeJS("bladeSdk.createToken('${esc(tokenName)}', '${esc(tokenSymbol)}', ${isNft}, ${keys.joinToString(",", "[", "]") {gson.toJson(it)}}, ${decimals}, ${initialSupply}, ${maxSupply}, '$completionKey')")
    }

    /**
     * Associate token to account. Association fee will be covered by PayMaster, if tokenId configured in dApp
     *
     * @param tokenIdOrCampaign: token id to associate. Empty to associate all tokens configured in dApp.  Campaign name to associate on demand
     * @param completion: callback function, with result of TransactionReceiptData or BladeJSError
     * @return {TransactionReceiptData} receipt
     * @sample
     * Blade.associateToken(
     *     tokenIdOrCampaign = "0.0.1337",
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun associateToken(
        tokenIdOrCampaign: String,
        completion: (TransactionReceiptData?, BladeJSError?) -> Unit
    ) {
        val completionKey = getCompletionKey("associateToken")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
        }
        executeJS("bladeSdk.associateToken('${esc(tokenIdOrCampaign)}', '$completionKey')")
    }

    /**
     * Mint one NFT
     *
     * @param tokenAddress: token id to mint NFT
     * @param file: image to mint (base64 DataUrl image, eg.: data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAA...)
     * @param metadata: NFT metadata
     * @param storageConfig: IPFS provider config
     * @param completion: callback function, with result of CreateTokenData or BladeJSError
     * @return {TransactionReceiptData} receipt
     * @sample
     * val tokenAddress = "0.0.13377"
     * val base64Image = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAYAAAAeP4ixAAAARUlEQVR42u3PMREAAAgEIO1fzU5vBlcPGtCVTD3QIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIXCyqyi6fIALs1AAAAAElFTkSuQmCC"
     * val metaData = mapOf<String, Any>(
     *     "name" to "NFTitle",
     *     "score" to 10,
     *     "power" to 4,
     *     "intelligence" to 6,
     *     "speed" to 10
     * )
     * val storageConfig = NFTStorageConfig(
     *     provider = NFTStorageProvider.nftStorage,
     *     apiKey = "eyJhbGcsfgrgsrgInR5cCI6IkpXVCJ9.eyJzd5235326ZXRocjoweDfsdfsdfFM0ZkZFOEJhNjdCNjc1NDk1Q2NEREFiYjk0NTE4Njdsfc3MiOiJuZnQtc3RvcmFnZSIsImlhdCI6sdfNDQ2NDUxODQ2MiwibmFt4I6IkJsYWRlUcvxcRLLXRlc3RrdffifQ.t1wCiEuiTvcYOwssdZgiYaug4aF8ZrvMBdkTASojWGU"
     * )
     *
     * Blade.nftMint(
     *     tokenAddress,
     *     file = base64Image,
     *     metaData,
     *     storageConfig,
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun nftMint(
        tokenAddress: String,
        file: String,
        metadata: Map<String, Any>,
        storageConfig: NFTStorageConfig,
        completion: (TransactionReceiptData?, BladeJSError?) -> Unit
    ) {
        val completionKey = getCompletionKey("nftMint")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
        }
        executeJS("bladeSdk.nftMint('${esc(tokenAddress)}', '${esc(file)}', ${gson.toJson(metadata)}, ${gson.toJson(storageConfig)}, '$completionKey')")
    }

    fun getTokenInfo(
        tokenAddress: String,
        serial: String,
        completion: (TokenInfoData?, BladeJSError?) -> Unit
    ) {
        val completionKey = getCompletionKey("getTokenInfo")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TokenInfoData, TokenInfoResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getTokenInfo('${esc(tokenAddress)}', '${esc(serial)}', '$completionKey')")
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
        visitorId = ""
        apiKey = ""
        dAppCode = ""
    }

    /**
     * Method to handle JS responses. By technical reasons, must be public, but you can skip it :)
     */
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

    private inline fun <Y, reified T: Result<Y>>typicalDeferredCallback(data: String, error: BladeJSError?, completion: (Y?, BladeJSError?) -> Unit) {
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
