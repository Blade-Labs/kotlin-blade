package io.bladewallet.bladesdk

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup.LayoutParams
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.Gson
import kotlinx.coroutines.*

@SuppressLint("StaticFieldLeak")
object Blade {
    private const val sdkVersion: String = "Kotlin@0.6.25"
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

    /**
     * Init instance of BladeSDK for correct work with Blade API and Hedera network.
     *
     * @param apiKey Unique key for API provided by Blade team.
     * @param dAppCode your dAppCode - request specific one by contacting Bladelabs team
     * @param network "Mainnet" or "Testnet" of Hedera network
     * @param bladeEnv field to set BladeAPI environment (Prod, CI). Prod used by default.
     * @param context android context
     * @param force optional field to force init. Will not crash if already initialized
     * @param completion: callback function, with result of InfoData or BladeJSError
     * @return {InfoData} with information about Blade instance, including visitorId
     * @sample
     * Blade.initialize(
     *     Config.apiKey, Config.dAppCode, Config.network, Config.bladeEnv, requireContext(), false
     * ) { infoData, error ->
     *     println(infoData ?: error)
     * }
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun initialize(apiKey: String, dAppCode: String, network: String, bladeEnv: BladeEnv = BladeEnv.Prod, context: Context, force: Boolean = false, completion: (InfoData?, BladeJSError?) -> Unit) {
        if (webViewInitialized && !force) {
            println("Error while doing double init of BladeSDK")
            throw Exception("Error while doing double init of BladeSDK")
        }
        initCompletion = completion
        this.apiKey = apiKey
        this.dAppCode = dAppCode
        this.network = network

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
                    remoteConfig = getRemoteConfig(network, dAppCode, sdkVersion, bladeEnv)
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
     * Get balances by account id.
     *
     * @param id Hedera account id
     * @param completion callback function, with result of BalanceData or BladeJSError
     * @return {BalanceData} with information about Hedera account balances (hbar and list of token balances)
     * @sample
     * Blade.getBalance("0.0.45467464") { result, error ->
     *     println("${ result ?: error}")
     * }
     */
    fun getBalance(id: String, completion: (BalanceData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getBalance")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<BalanceData, BalanceResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getBalance('${esc(id)}', '$completionKey')")
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
     * Method to execute Hbar transfers from current account to receiver
     *
     * @param accountId: sender account id
     * @param accountPrivateKey: sender's private key to sign transfer transaction
     * @param receiverId: receiver
     * @param amount: amount
     * @param memo: memo (limited to 100 characters)
     * @param completion callback function, with result of TransactionReceiptData or BladeJSError
     * @return {TransactionReceiptData} receipt
     * @sample
     * val senderId = "0.0.10001"
     * val senderKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
     * val receiverId = "0.0.10002"
     * val amount = 2.5
     *
     * Blade.transferHbars(
     *     senderId,
     *     senderKey,
     *     receiverId,
     *     amount,
     *     "Some memo text"
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun transferHbars(accountId: String, accountPrivateKey: String, receiverId: String, amount: Double, memo: String, completion: (TransactionReceiptData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("transferHbars")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
        }
        executeJS("bladeSdk.transferHbars('${esc(accountId)}', '${esc(accountPrivateKey)}', '${esc(receiverId)}', '$amount', '${esc(memo)}', '$completionKey')")
    }

    /**
     * Method to execute token transfers from current account to receiver
     *
     * @param tokenId: token id to send (0.0.xxxxx)
     * @param accountId: sender account id (0.0.xxxxx)
     * @param accountPrivateKey: sender's hex-encoded private key with DER-header (302e020100300506032b657004220420...). ECDSA or Ed25519
     * @param receiverId: receiver account id (0.0.xxxxx)
     * @param amountOrSerial: amount of fungible tokens to send (with token-decimals correction) on NFT serial number
     * @param memo: transaction memo (limited to 100 characters)
     * @param usePaymaster if true, Paymaster account will pay fee transaction. Only for single dApp configured fungible-token. In that case tokenId not used
     * @param completion callback function, with result of TransactionReceiptData or BladeJSError
     * @return {TransactionReceiptData} receipt
     * @sample
     * val tokenId = "0.0.1337"
     * val senderId = "0.0.10001"
     * val senderKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
     * val receiverId = "0.0.10002"
     * val amount = 2.5
     *
     * Blade.transferTokens(
     *     tokenId,
     *     senderId,
     *     senderKey,
     *     receiverId,
     *     amount,
     *     "Token transfer memo"
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun transferTokens(tokenId: String, accountId: String, accountPrivateKey: String, receiverId: String, amountOrSerial: Double, memo: String, usePaymaster: Boolean = true, completion: (TransactionReceiptData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("transferTokens")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
        }
        executeJS("bladeSdk.transferTokens('${esc(tokenId)}', '${esc(accountId)}', '${esc(accountPrivateKey)}', '${esc(receiverId)}', '$amountOrSerial', '${esc(memo)}', $usePaymaster, '$completionKey')")
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
     * val receiverId = "0.0.10002"
     * val receiverKey = "302d300706052b8104000a032200029dc73991b00002..."
     * val senderId = "0.0.10001"
     * val tokenId = "0.0.1337"
     * var scheduleId = ""
     *
     * Blade.createScheduleTransaction(
     *     accountId = receiverId,
     *     accountPrivateKey = receiverKey,
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
        accountId: String,
        accountPrivateKey: String,
        type: ScheduleTransactionType,
        transfers: List<ScheduleTransactionTransfer>,
        usePaymaster: Boolean = false,
        completion: (CreateScheduleData?, BladeJSError?) -> Unit
    ) {
        val completionKey = getCompletionKey("createScheduleTransaction")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<CreateScheduleData, CreateScheduleResponse>(data, error, completion)
        }
        executeJS("bladeSdk.createScheduleTransaction('${esc(accountId)}', '${esc(accountPrivateKey)}', '${esc(type.toString())}', ${transfers.joinToString(",", "[", "]") {gson.toJson(it)}}, $usePaymaster, '$completionKey')")
    }

    /**
     * Method to sign scheduled transaction
     *
     * @param scheduleId scheduled transaction id (0.0.xxxxx)
     * @param accountId account id (0.0.xxxxx)
     * @param accountPrivateKey account key (hex encoded privateKey with DER-prefix)
     * @param receiverAccountId account id of receiver for additional validation in case of dApp freeSchedule transactions configured
     * @param usePaymaster if true, Paymaster account will pay transaction fee (also dApp had to be configured for free schedules)
     * @param completion callback function, with result of TransactionReceiptData or BladeJSError
     * @return {TransactionReceiptData} receipt
     * @sample
     * val senderId = "0.0.10001"
     * val senderKey = "302d300706052b8104000a032200029dc73991b00001..."
     *
     * val receiverId = "0.0.10002"
     * var scheduleId = "0.0...." // result of createScheduleTransaction on receiver side
     *
     * Blade.signScheduleId(
     *     scheduleId = scheduleId,
     *     accountId = senderId,
     *     accountPrivateKey = senderKey,
     *     receiverAccountId = receiverId,
     *     usePaymaster = true
     * ) { result, bladeJSError ->
     *     println(result ?: bladeJSError)
     * }
     */
    fun signScheduleId(
        scheduleId: String,
        accountId: String,
        accountPrivateKey: String,
        receiverAccountId: String = "",
        usePaymaster: Boolean = false,
        completion: (TransactionReceiptData?, BladeJSError?) -> Unit
    ) {
        val completionKey = getCompletionKey("signScheduleId")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
        }
        executeJS("bladeSdk.signScheduleId('${esc(scheduleId)}', '${esc(accountId)}', '${esc(accountPrivateKey)}', '${esc(receiverAccountId)}', $usePaymaster, '$completionKey')")
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
     * Blade.createHederaAccount() { result, error ->
     *     println(result ?: error)
     * }
     */
    fun createHederaAccount(privateKey: String = "", deviceId: String = "", completion: (CreatedAccountData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("createAccount")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<CreatedAccountData, CreatedAccountResponse>(data, error, completion)
        }
        executeJS("bladeSdk.createAccount('${esc(privateKey)}', '${esc(deviceId)}', '$completionKey')")
    }

    /**
     * Get account from queue (read more at `createAccount()`).
     * If account already created, return account data.
     * If account not created yet, response will be same as in `createAccount()` method if account in queue.
     *
     * @param transactionId: can be received on createHederaAccount method, when busy network is busy, and account creation added to queue
     * @param seedPhrase: returned from createHederaAccount method, required for updating keys and proper response
     * @param completion callback function, with result of CreatedAccountData or BladeJSError
     * @return {CreatedAccountData} new account data
     */
    fun getPendingAccount(transactionId: String, seedPhrase: String, completion: (CreatedAccountData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getPendingAccount")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<CreatedAccountData, CreatedAccountResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getPendingAccount('${esc(transactionId)}', '${esc(seedPhrase)}', '$completionKey')")
    }

    /**
     * Delete Hedera account. This method requires account private key and operator private key. Operator is the one who paying fees
     *
     * @param deleteAccountId: account id of account to delete (0.0.xxxxx)
     * @param deletePrivateKey: account private key (DER encoded hex string)
     * @param transferAccountId: The ID of the account to transfer the remaining funds to.
     * @param operatorAccountId: operator account id (0.0.xxxxx). Used for fee
     * @param operatorPrivateKey: operator's account private key (DER encoded hex string)
     * @param completion callback function, with result of TransactionReceiptData or BladeJSError
     * @return {TransactionReceiptData} receipt
     * @sample
     * val deleteAccountId = "0.0.65468464"
     * val deletePrivateKey = "3030020100300706052b8104000a04220420ebc..."
     * val transferAccountId = "0.0.10001"
     * val operatorAccountId = "0.0.10002"
     * val operatorPrivateKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
     *
     * Blade.deleteHederaAccount(
     *     deleteAccountId,
     *     deletePrivateKey,
     *     transferAccountId,
     *     operatorAccountId,
     *     operatorPrivateKey,
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun deleteHederaAccount(deleteAccountId: String, deletePrivateKey: String, transferAccountId: String, operatorAccountId: String, operatorPrivateKey: String, completion: (TransactionReceiptData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("deleteHederaAccount")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
        }
        executeJS("bladeSdk.deleteAccount('${esc(deleteAccountId)}', '${esc(deletePrivateKey)}', '${esc(transferAccountId)}', '${esc(operatorAccountId)}', '${esc(operatorPrivateKey)}', '$completionKey')")
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
     * @param accountId: Hedera account id (0.0.xxxxx)
     * @param accountPrivateKey account private key (DER encoded hex string)
     * @param nodeId node id to stake to. If negative or null, account will be unstaked
     * @param completion callback function, with result of TransactionReceiptData or BladeJSError
     * @return {TransactionReceiptData} receipt
     * @sample
     * Blade.stakeToNode("0.0.10002", "302d300706052b8104000a032200029dc73991b0d9cd...", 5) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun stakeToNode(accountId: String, accountPrivateKey: String, nodeId: Int, completion: (TransactionReceiptData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("stakeToNode")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
        }
        executeJS("bladeSdk.stakeToNode('${esc(accountId)}', '${esc(accountPrivateKey)}', ${nodeId}, '$completionKey')")
    }

    /**
     * Get private key and accountId from mnemonic. Supported standard and legacy key derivation.
     * If account not found, standard ECDSA key will be returned.
     * Keys returned with DER header. EvmAddress computed from Public key.
     *
     * @deprecated This method is deprecated. Please use [searchAccounts] instead. Will be removed in version 0.8
     * @param mnemonic: seed phrase (BIP39 mnemonic)
     * @param lookupNames: lookup for accounts (not used anymore, account search is mandatory)
     * @param completion callback function, with result of PrivateKeyData or BladeJSError
     * @return {PrivateKeyData} private key derived from mnemonic and account id
     * @sample
     * Blade.getKeysFromMnemonic("purity slab doctor swamp tackle rebuild summer bean craft toddler blouse switch") { result, error ->
     *     println(result ?: error)
     * }
     */
    @Deprecated("This method is deprecated. Please use [searchAccounts] instead. Will be removed in version 0.8")
    fun getKeysFromMnemonic (mnemonic: String, lookupNames: Boolean = false, completion: (PrivateKeyData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getKeysFromMnemonic")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<PrivateKeyData, PrivateKeyResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getKeysFromMnemonic('${esc(mnemonic)}', $lookupNames, '$completionKey')")
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
     * @param accountId Hedera account id (0.0.xxxxx)
     * @param accountPrivateKey account private key (DER encoded hex string)
     * @param secretNonce configured for dApp. Should be kept in secret
     * @param completion callback function, with result of TokenDropData or BladeJSError
     * @return {TokenDropData} status
     * @sample
     * val accountId = "0.0.10002"
     * val accountPrivateKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
     * val secretNonce = "[ CENSORED ]"
     *
     * Blade.dropTokens(
     *     accountId,
     *     accountPrivateKey,
     *     secretNonce,
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun dropTokens (accountId: String, accountPrivateKey: String, secretNonce: String, completion: (TokenDropData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("dropTokens")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TokenDropData, TokenDropResponse>(data, error, completion)
        }
        executeJS("bladeSdk.dropTokens('${esc(accountId)}', '${esc(accountPrivateKey)}', '${esc(secretNonce)}', '$completionKey')")
    }

    /**
     * Sign base64-encoded message with private key. Returns hex-encoded signature.
     *
     * @param messageString base64-encoded message to sign
     * @param privateKey hex-encoded private key with DER header
     * @param completion callback function, with result of SignMessageData or BladeJSError
     * @return {SignMessageData} signature
     * @sample
     * import java.util.Base64
     *
     * // ...
     *
     * val originalString = "hello"
     * val encodedString: String = Base64.getEncoder().encodeToString(originalString.toByteArray())
     * val accountPrivateKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
     *
     * Blade.sign(
     *     encodedString,
     *     accountPrivateKey
     * ) { result, error ->
     *     println(result ?: error)
     * }
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
     * @param messageString: base64-encoded message (same as provided to `sign()` method)
     * @param signature: hex-encoded signature (result from `sign()` method)
     * @param publicKey: hex-encoded public key with DER header
     * @param completion callback function, with result of SignVerifyMessageData or BladeJSError
     * @return {SignVerifyMessageData} verification result
     * @sample
     * val originalString = "hello"
     * val encodedString: String = Base64.getEncoder().encodeToString(originalString.toByteArray())
     * val signature = "27cb9d51434cf1e76d7ac515b19442c619f641e6fccddbf4a3756b14466becb6992dc1d2a82268018147141fc8d66ff9ade43b7f78c176d070a66372d655f942"
     * val publicKey = "302d300706052b8104000a032200029dc73991b0d9cdbb59b2cd0a97a0eaff6de801726cb39804ea9461df6be2dd30"
     *
     * Blade.signVerify(
     *     encodedString,
     *     signature,
     *     publicKey
     * ) { result, error ->
     *     lifecycleScope.launch {
     *         println(result ?: error)
     *     }
     * }
     */
    fun signVerify(messageString: String, signature: String, publicKey: String, completion: (SignVerifyMessageData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("signVerify")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<SignVerifyMessageData, SignVerifyMessageResponse>(data, error, completion)
        }
        executeJS("bladeSdk.signVerify('${esc(messageString)}', '${esc(signature)}', '${esc(publicKey)}', '$completionKey')")
    }

    /**
     * Call contract function. Directly or via BladeAPI using paymaster account (fee will be paid by Paymaster account), depending on your dApp configuration.
     *
     * @param contractId: contract id (0.0.xxxxx)
     * @param functionName: name of the contract function to call
     * @param params: function argument. Can be generated with {@link ContractFunctionParameters} object
     * @param accountId: operator account id (0.0.xxxxx)
     * @param accountPrivateKey: operator's hex-encoded private key with DER-header, ECDSA or Ed25519
     * @param gas: gas limit for the transaction
     * @param usePaymaster: if true, fee will be paid by Paymaster account (note: msg.sender inside the contract will be Paymaster account)
     * @param completion callback function, with result of TransactionReceiptData or BladeJSError
     * @return {TransactionReceiptData} receipt
     * @sample
     * val contractId = "0.0.123456"
     * val functionName = "set_message"
     * val parameters = ContractFunctionParameters().addString("hello")
     * val accountId = "0.0.10002"
     * val accountPrivateKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
     * val gas = 155000
     * val usePaymaster = false
     *
     * Blade.contractCallFunction(
     *     contractId,
     *     functionName,
     *     parameters,
     *     accountId,
     *     accountPrivateKey,
     *     gas,
     *     usePaymaster
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun contractCallFunction(contractId: String, functionName: String, params: ContractFunctionParameters, accountId: String, accountPrivateKey: String, gas: Int = 100000, usePaymaster: Boolean, completion: (TransactionReceiptData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("contractCallFunction")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
        }
        // TODO check if we need to escape ContractFunctionParams.encode() result \'
        executeJS("bladeSdk.contractCallFunction('${esc(contractId)}', '${esc(functionName)}', '${params.encode()}', '${esc(accountId)}', '${esc(accountPrivateKey)}', $gas, $usePaymaster, '$completionKey')")
    }

    /**
     * Call query on contract function. Similar to {@link contractCallFunction} can be called directly or via BladeAPI using Paymaster account.
     *
     * @param contractId: contract id (0.0.xxxxx)
     * @param functionName: name of the contract function to call
     * @param params: function argument. Can be generated with {@link ContractFunctionParameters} object
     * @param accountId: operator account id (0.0.xxxxx)
     * @param accountPrivateKey: operator's hex-encoded private key with DER-header, ECDSA or Ed25519
     * @param gas: gas limit for the transaction
     * @param usePaymaster: if true, the fee will be paid by paymaster account (note: msg.sender inside the contract will be Paymaster account)
     * @param returnTypes: List of return types, e.g. listOf("string", "int32")
     * @param completion callback function, with result of ContractQueryData or BladeJSError
     * @return {ContractQueryData} contract query call result
     * @sample
     * val contractId = "0.0.123456"
     * val functionName = "get_message"
     * val parameters = ContractFunctionParameters()
     * val accountId = "0.0.10002"
     * val accountPrivateKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
     * val gas = 55000
     * val usePaymaster = false
     * val returnTypes = listOf("string", "int32")
     *
     * Blade.contractCallQueryFunction(
     *     contractId,
     *     functionName,
     *     parameters,
     *     accountId,
     *     accountPrivateKey,
     *     gas,
     *     usePaymaster,
     *     returnTypes
     * ) { result, error ->
     *     lifecycleScope.launch {
     *         println(result ?: error)
     *     }
     * }
     */
    fun contractCallQueryFunction(contractId: String, functionName: String, params: ContractFunctionParameters, accountId: String, accountPrivateKey: String, gas: Int = 100000, usePaymaster: Boolean, returnTypes: List<String>, completion: (ContractQueryData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("contractCallQueryFunction")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<ContractQueryData, ContractQueryResponse>(data, error, completion)
        }

        executeJS("bladeSdk.contractCallQueryFunction('${esc(contractId)}', '${esc(functionName)}', '${params.encode()}', '${esc(accountId)}', '${esc(accountPrivateKey)}', $gas, $usePaymaster, ${returnTypes.joinToString(",", "[", "]") {"\'${esc(it)}\'"}}, '$completionKey')")
    }

    /**
     * Sign base64-encoded message with private key using ethers lib. Returns hex-encoded signature.
     *
     * @param messageString: base64-encoded message to sign
     * @param privateKey: hex-encoded private key with DER header
     * @param completion callback function, with result of SignMessageData or BladeJSError
     * @return {SignMessageData} signature
     * @sample
     * import java.util.Base64
     *
     * // ...
     *
     * val originalString = "hello"
     * val encodedString: String = Base64.getEncoder().encodeToString(originalString.toByteArray())
     * val accountPrivateKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
     *
     * Blade.ethersSign(
     *     encodedString,
     *     accountPrivateKey
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun ethersSign(messageString: String, privateKey: String, completion: (SignMessageData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("ethersSign")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<SignMessageData, SignMessageResponse>(data, error, completion)
        }
        executeJS("bladeSdk.ethersSign('${esc(messageString)}', '${esc(privateKey)}', '$completionKey')")
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
     * @param accountPrivateKey: signer private key (hex-encoded with DER header)
     * @param completion callback function, with result of SplitSignatureData or BladeJSError
     * @return {SplitSignatureData} v-r-s signature
     * @sample
     * val parameters = ContractFunctionParameters().addString("hello")
     * val accountPrivateKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
     * Blade.getParamsSignature(
     *     parameters,
     *     accountPrivateKey
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun getParamsSignature(params: ContractFunctionParameters, accountPrivateKey: String, completion: (SplitSignatureData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getParamsSignature")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<SplitSignatureData, SplitSignatureResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getParamsSignature('${params.encode()}', '${esc(accountPrivateKey)}', '$completionKey')")
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
     * Get configured url for C14 integration (iframe or popup)
     *
     * @deprecated Please use `exchangeGetQuotes` and `getTradeUrl` methods. Results aggregated on many providers, including C14
     * @param asset: USDC, HBAR, KARATE or C14 asset uuid
     * @param account: receiver account id (0.0.xxxxx)
     * @param amount: preset amount. May be overwritten if out of range (min/max)
     * @param completion callback function, with result of IntegrationUrlData or BladeJSError
     * @return {IntegrationUrlData} url to open
     * @sample
     * Blade.getC14url(
     *     asset = "HBAR",
     *     account = "0.0.10002",
     *     amount = "120"
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    @Deprecated("Please use `exchangeGetQuotes` and `getTradeUrl` methods. Results aggregated on many providers, including C14")
    fun getC14url(asset: String, account: String, amount: String = "", completion: (IntegrationUrlData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("getC14url")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<IntegrationUrlData, IntegrationUrlResponse>(data, error, completion)
        }
        executeJS("bladeSdk.getC14url('${esc(asset)}', '${esc(account)}', '${esc(amount)}', '$completionKey')")
    }

    /**
     * Get quotes from different services for buy, sell or swap
     *
     * @param sourceCode: name (HBAR, KARATE, other token code)
     * @param sourceAmount: amount to swap, buy or sell
     * @param targetCode: name (HBAR, KARATE, USDC, other token code)
     * @param strategy: one of enum CryptoFlowServiceStrategy (Buy, Sell, Swap)
     * @param completion: callback function, with result of SwapQuotesData or BladeJSError
     * @return {SwapQuotesData} quotes from different provider
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
     * @param accountId: account id
     * @param accountPrivateKey: account private key
     * @param sourceCode: name (HBAR, KARATE, other token code)
     * @param sourceAmount: amount to swap
     * @param targetCode: name (HBAR, KARATE, other token code)
     * @param slippage: slippage in percents. Transaction will revert if the price changes unfavorably by more than this percentage.
     * @param serviceId: service id to use for swap (saucerswap, etc)
     * @param completion: callback function, with result of ResultData or BladeJSError
     * @return {ResultData} swap result
     * @sample
     * val accountId = "0.0.10001"
     * val accountPrivateKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
     * val sourceCode = "USDC"
     * val targetCode = "KARATE"
     *
     * Blade.swapTokens(
     *     accountId,
     *     accountPrivateKey,
     *     sourceCode,
     *     sourceAmount = 123.4,
     *     targetCode,
     *     slippage = 0.5,
     *     serviceId = "moonpay"
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun swapTokens(accountId: String, accountPrivateKey: String, sourceCode: String, sourceAmount: Double, targetCode: String, slippage: Double, serviceId: String, completion: (ResultData?, BladeJSError?) -> Unit) {
        val completionKey = getCompletionKey("swapTokens")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<ResultData, ResultResponse>(data, error, completion)
        }
        executeJS("bladeSdk.swapTokens('${esc(accountId)}', '${esc(accountPrivateKey)}', '${esc(sourceCode)}', ${sourceAmount}, '${esc(targetCode)}', ${slippage}, '${esc(serviceId)}', '$completionKey')")
    }

    /**
     * Create token (NFT or Fungible Token)
     *
     * @param treasuryAccountId: treasury account id
     * @param supplyPrivateKey: supply account private key
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
     *         treasuryAccountId = Config.accountId,
     *         supplyPrivateKey = Config.privateKey,
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
        treasuryAccountId: String,
        supplyPrivateKey: String,
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
        executeJS("bladeSdk.createToken('${esc(treasuryAccountId)}', '${esc(supplyPrivateKey)}', '${esc(tokenName)}', '${esc(tokenSymbol)}', ${isNft}, ${keys.joinToString(",", "[", "]") {gson.toJson(it)}}, ${decimals}, ${initialSupply}, ${maxSupply}, '$completionKey')")
    }

    /**
     * Associate token to account. Association fee will be covered by PayMaster, if tokenId configured in dApp
     *
     * @param tokenId: token id to associate. Empty to associate all tokens configured in dApp
     * @param accountId: account id to associate token
     * @param accountPrivateKey: account private key
     * @param completion: callback function, with result of TransactionReceiptData or BladeJSError
     * @return {TransactionReceiptData} receipt
     * @sample
     * Blade.associateToken(
     *     tokenId = "0.0.1337",
     *     accountId = "0.0.10001",
     *     accountPrivateKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun associateToken(
        tokenId: String,
        accountId: String,
        accountPrivateKey: String,
        completion: (TransactionReceiptData?, BladeJSError?) -> Unit
    ) {
        val completionKey = getCompletionKey("associateToken")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
        }
        executeJS("bladeSdk.associateToken('${esc(tokenId)}', '${esc(accountId)}', '${esc(accountPrivateKey)}', '$completionKey')")
    }

    /**
     * Mint one NFT
     *
     * @param tokenId: token id to mint NFT
     * @param supplyAccountId: token supply account id
     * @param supplyPrivateKey: token supply private key
     * @param file: image to mint (base64 DataUrl image, eg.: data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAA...)
     * @param metadata: NFT metadata
     * @param storageConfig: IPFS provider config
     * @param completion: callback function, with result of CreateTokenData or BladeJSError
     * @return {TransactionReceiptData} receipt
     * @sample
     * val tokenId = "0.0.13377"
     * val supplyAccountId = "0.0.10001"
     * val supplyPrivateKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
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
     *     tokenId,
     *     supplyAccountId,
     *     supplyPrivateKey,
     *     file = base64Image,
     *     metaData,
     *     storageConfig,
     * ) { result, error ->
     *     println(result ?: error)
     * }
     */
    fun nftMint(
        tokenId: String,
        supplyAccountId: String,
        supplyPrivateKey: String,
        file: String,
        metadata: Map<String, Any>,
        storageConfig: NFTStorageConfig,
        completion: (TransactionReceiptData?, BladeJSError?) -> Unit
    ) {
        val completionKey = getCompletionKey("nftMint")
        deferCompletion(completionKey) { data: String, error: BladeJSError? ->
            typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
        }
        executeJS("bladeSdk.nftMint('${esc(tokenId)}', '${esc(supplyAccountId)}', '${esc(supplyPrivateKey)}', '${esc(file)}', ${gson.toJson(metadata)}, ${gson.toJson(storageConfig)}, '$completionKey')")
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
