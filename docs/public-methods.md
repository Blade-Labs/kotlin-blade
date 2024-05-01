# Public methods 📢

## Init BladeSDK

### Parameters:

* `apiKey: String` - your apiKey (unique per platform, network and bladeEnv)
* `dAppCode: String` - your dAppCode
* `network: String` - Hedera network, supported: `Testnet` or `Mainnet`
* `bladeEnv: BladeEnv` - Blade API environment: `.Prod` or `.CI`
* `context: Context` - application context
* `force: Boolean` - will force initialization of webView even if it was already initialized

```kotlin
@SuppressLint("SetJavaScriptEnabled")
fun initialize(apiKey: String, dAppCode: String, network: String, bladeEnv: BladeEnv, context: Context, force: Boolean = false, completion: (InfoData?, BladeJSError?) -> Unit) {
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
            visitorId = sharedPreferences.getString(context.resources.getString(R.string.visitorIdKey), "") ?: ""

            if (visitorId == "") {
                remoteConfig = getRemoteConfig(network, dAppCode, sdkVersion, bladeEnv)
                visitorId = getVisitorId(remoteConfig.fpApiKey, context)
                sharedPreferences.edit().putString(context.resources.getString(R.string.visitorIdKey), visitorId).apply()
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
```

## Get SDK info and check if SDK initialized

```kotlin
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
```

## Get balances by Hedera id (address)

### Parameters:

 * `id`: Hedera account id
 * `completion`: callback function, with result of `BalanceData` or `BladeJSError`

```kotlin
fun getBalance(id: String, completion: (BalanceData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("getBalance")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<BalanceData, BalanceResponse>(data, error, completion)
    }
    executeJS("bladeSdk.getBalance('${esc(id)}', '$completionKey')")
}
```

## Get list of all available coins on CoinGecko.

### Parameters:

 * `completion`: callback function, with result of `CoinListData` or `BladeJSError`

```kotlin
fun getCoinList(completion: (CoinListData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("getCoinList")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<CoinListData, CoinListResponse>(data, error, completion)
    }
    executeJS("bladeSdk.getCoinList('$completionKey')")
}
```

## Get coin price and coin info from CoinGecko. Search can be coin id or address in one of the coin platforms.

### Parameters:

 * `search`: CoinGecko coinId, or address in one of the coin platforms or `hbar` (default, alias for `hedera-hashgraph`)
 * `currency` result currency for price field
 * `completion`: callback function, with result of `BalanceData` or `BladeJSError`

```kotlin
fun getCoinPrice(search: String, currency: String = "usd", completion: (CoinInfoData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("getCoinPrice")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<CoinInfoData, CoinInfoResponse>(data, error, completion)
    }
    executeJS("bladeSdk.getCoinPrice('${esc(search)}', '${esc(currency)}', '$completionKey')")
}
```

##  Method to execute Hbar transfers from current account to receiver

### Parameters:

 * `accountId`: sender account id
 * `accountPrivateKey`: sender's private key to sign transfer transaction
 * `receiverId`: receiver
 * `amount`: amount
 * `memo`: memo (limited to 100 characters)
 * `completion`: callback function, with result of TransactionReceiptData or BladeJSError

```kotlin
fun transferHbars(accountId: String, accountPrivateKey: String, receiverId: String, amount: Double, memo: String, completion: (TransactionReceiptData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("transferHbars")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
    }
    executeJS("bladeSdk.transferHbars('${esc(accountId)}', '${esc(accountPrivateKey)}', '${esc(receiverId)}', '$amount', '${esc(memo)}', '$completionKey')")
}
```

##  Method to execute token transfers from current account to receiver

### Parameters:

 * `tokenId`: token
 * `accountId`: sender account id
 * `accountPrivateKey`: sender's private key to sign transfer transaction
 * `receiverId`: receiver
 * `amountOrSerial`: amount of fungible tokens to send (with token-decimals correction) on NFT serial number
 * `memo`: memo (limited to 100 characters)
 * `freeTransfer`: for tokens configured for this dAppCode on Blade backend
 * `completion`: callback function, with result of TransactionReceiptData or BladeJSError

```kotlin
fun transferTokens(tokenId: String, accountId: String, accountPrivateKey: String, receiverId: String, amountOrSerial: Double, memo: String, freeTransfer: Boolean = true, completion: (TransactionReceiptData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("transferTokens")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
    }
    executeJS("bladeSdk.transferTokens('${esc(tokenId)}', '${esc(accountId)}', '${esc(accountPrivateKey)}', '${esc(receiverId)}', '$amount', '${esc(memo)}', $freeTransfer, '$completionKey')")
}
```

## Method to create Hedera account

### Parameters:

 * `deviceId`: unique device id (advanced security feature, required only for some dApps)
 * `completion`: callback function, with result of CreatedAccountData or BladeJSError

```kotlin
fun createHederaAccount(deviceId: String, completion: (CreatedAccountData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("createAccount")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<CreatedAccountData, CreatedAccountResponse>(data, error, completion)
    }
    executeJS("bladeSdk.createAccount('${esc(deviceId)}', '$completionKey')")
}
```

## Method to create Hedera account

### Parameters:

 * `transactionId`: can be received on createHederaAccount method, when busy network is busy, and account creation added to queue
 * `seedPhrase`: returned from createHederaAccount method, required for updating keys and proper response
 * `completion`: callback function, with result of CreatedAccountData or BladeJSError

```kotlin
fun getPendingAccount(transactionId: String, seedPhrase: String, completion: (CreatedAccountData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("getPendingAccount")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<CreatedAccountData, CreatedAccountResponse>(data, error, completion)
    }
    executeJS("bladeSdk.getPendingAccount('${esc(transactionId)}', '${esc(seedPhrase)}', '$completionKey')")
}
```

## Method to delete Hedera account

### Parameters:

 * `deleteAccountId`: account to delete - id
 * `deletePrivateKey`: account to delete - private key
 * `transferAccountId`: The ID of the account to transfer the remaining funds to.
 * `operatorAccountId`: operator account Id
 * `operatorPrivateKey`: operator account private key
 * `completion`: callback function, with result of TransactionReceiptData or BladeJSError

```kotlin
fun deleteHederaAccount(deleteAccountId: String, deletePrivateKey: String, transferAccountId: String, operatorAccountId: String, operatorPrivateKey: String, completion: (TransactionReceiptData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("deleteHederaAccount")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
    }
    executeJS("bladeSdk.deleteAccount('${esc(deleteAccountId)}', '${esc(deletePrivateKey)}', '${esc(transferAccountId)}', '${esc(operatorAccountId)}', '${esc(operatorPrivateKey)}', '$completionKey')")
}
```

## Get account evmAddress and calculated evmAddress from public key

### Parameters:

 * `accountId`: Hedera account id
 * `completion`: callback function, with result of AccountInfoData or BladeJSError

```kotlin
fun getAccountInfo(accountId: String, completion: (AccountInfoData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("getAccountInfo")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<AccountInfoData, AccountInfoResponse>(data, error, completion)
    }
    executeJS("bladeSdk.getAccountInfo('${esc(accountId)}', '$completionKey')")
}
```

## Create scheduled transaction

### Parameters:

 * `accountId`: account id (0.0.xxxxx)
 * `accountPrivateKey`: hex encoded privateKey with DER-prefix
 * `type`: schedule transaction type (currently only TRANSFER supported)
 * `transfers`: array of transfers to schedule (HBAR, FT, NFT)
 * `freeSchedule` if true, Blade will pay transaction fee (also dApp had to be configured for free schedules)
 * `completion`: callback function, with result of CreateScheduleData or BladeJSError


```kotlin
fun createScheduleTransaction(
    accountId: String,
    accountPrivateKey: String,
    type: ScheduleTransactionType,
    transfers: List<ScheduleTransactionTransfer>,
    freeSchedule: Boolean = false,
    completion: (CreateScheduleData?, BladeJSError?) -> Unit
) {
    val completionKey = getCompletionKey("createScheduleTransaction")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<CreateScheduleData, CreateScheduleResponse>(data, error, completion)
    }
    executeJS("bladeSdk.createScheduleTransaction('${esc(accountId)}', '${esc(accountPrivateKey)}', '${esc(type.toString())}', ${transfers.joinToString(",", "[", "]") {gson.toJson(it)}}, $freeSchedule, '$completionKey')")
}
```

## Method to sign scheduled transaction

### Parameters:

 * `scheduleId`: scheduled transaction id (0.0.xxxxx)
 * `accountId`: account id (0.0.xxxxx)
 * `accountPrivateKey`: hex encoded privateKey with DER-prefix
 * `receiverAccountId` account id of receiver for additional validation in case of dApp freeSchedule transactions configured
 * `freeSchedule` if true, Blade will pay transaction fee (also dApp had to be configured for free schedules)
 * `completion`: callback function, with result of TransactionReceiptData or BladeJSError
     

```kotlin
fun signScheduleId(
    scheduleId: String,
    accountId: String,
    accountPrivateKey: String,
    receiverAccountId: String = "",
    freeSchedule: Boolean = false,
    completion: (TransactionReceiptData?, BladeJSError?) -> Unit
) {
    val completionKey = getCompletionKey("signScheduleId")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
    }
    executeJS("bladeSdk.signScheduleId('${esc(scheduleId)}', '${esc(accountId)}', '${esc(accountPrivateKey)}', '${esc(receiverAccountId)}', $freeSchedule, '$completionKey')")
}
```

## Get Node list

### Parameters:

 * `completion`: callback function, with result of AccountInfoData or BladeJSError

```kotlin
fun getNodeList(completion: (NodesData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("getNodeList")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<NodesData, NodesResponse>(data, error, completion)
    }
    executeJS("bladeSdk.getNodeList('$completionKey')")
}
```

## Stake/unstake account

### Parameters:

 * `accountId`: Hedera account id
 * `accountPrivateKey`: account private key (DER encoded hex string)
 * `nodeId`: node id to stake to. If negative or null, account will be unstaked
 * `completion`: callback function, with result of TransactionReceiptData or BladeJSError

```kotlin
fun stakeToNode(accountId: String, accountPrivateKey: String, nodeId: Int, completion: (TransactionReceiptData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("stakeToNode")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
    }
    executeJS("bladeSdk.stakeToNode('${esc(accountId)}', '${esc(accountPrivateKey)}', ${nodeId}, '$completionKey')")
}
```

## Restore public and private key by seed phrase

*deprecated. Use [searchAccounts]*

### Parameters:

 * `mnemonic`: seed phrase
 * `lookupNames`: lookup for accounts
 * `completion`: callback function, with result of PrivateKeyData or BladeJSError

```kotlin
fun getKeysFromMnemonic (mnemonic: String, lookupNames: Boolean = false, completion: (PrivateKeyData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("getKeysFromMnemonic")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<PrivateKeyData, PrivateKeyResponse>(data, error, completion)
    }
    executeJS("bladeSdk.getKeysFromMnemonic('${esc(mnemonic)}', $lookupNames, '$completionKey')")
}
```

## Get accounts list and keys from private key or mnemonic. Returned keys with DER header.

### Parameters:

 * `keyOrMnemonic`: BIP39 mnemonic, private key with DER header
 * `completion`: callback function, with result of AccountPrivateData or BladeJSError

```kotlin
fun searchAccounts (keyOrMnemonic: String, completion: (AccountPrivateData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("searchAccounts")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<AccountPrivateData, AccountPrivateResponse>(data, error, completion)
    }
    executeJS("bladeSdk.searchAccounts('${esc(keyOrMnemonic)}', '$completionKey')")
}
```

## Bladelink drop to account

### Parameters:

 * `accountId` Hedera account id (0.0.xxxxx)
 * `accountPrivateKey` account private key (DER encoded hex string)
 * `secretNonce` configured for dApp. Should be kept in secret
 * `completion` callback function, with result of TokenDropData or BladeJSError

```kotlin
fun dropTokens (accountId: String, accountPrivateKey: String, secretNonce: String, completion: (TokenDropData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("dropTokens")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<TokenDropData, TokenDropResponse>(data, error, completion)
    }
    executeJS("bladeSdk.dropTokens('${esc(accountId)}', '${esc(accountPrivateKey)}', '${esc(secretNonce)}', '$completionKey')")
}
```

## Sign message with private key

### Parameters:

 * `messageString`: message in base64 string
 * `privateKey`: private key string
 * `completion`: callback function, with result of SignMessageData or BladeJSError

```kotlin
fun sign (messageString: String, privateKey: String, completion: (SignMessageData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("sign")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<SignMessageData, SignMessageResponse>(data, error, completion)
    }
    executeJS("bladeSdk.sign('${esc(messageString)}', '${esc(privateKey)}', '$completionKey')")
}
```

## Verify message signature with public key

### Parameters:

 * `messageString`: message in base64 string
 * `signature`: hex-encoded signature string
 * `publicKey`: public key string
 * `completion`: callback function, with result of SignVerifyMessageData or BladeJSError

```kotlin
fun signVerify(messageString: String, signature: String, publicKey: String, completion: (SignVerifyMessageData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("signVerify")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<SignVerifyMessageData, SignVerifyMessageResponse>(data, error, completion)
    }
    executeJS("bladeSdk.signVerify('${esc(messageString)}', '${esc(signature)}', '${esc(publicKey)}', '$completionKey')")
}
```

## Method to call smart-contract function

### Parameters:

 * `contractId`: contract id
 * `functionName`: contract function name
 * `params`: function arguments (instance of ContractFunctionParameters)
 * `accountId`: sender account id
 * `accountPrivateKey`: sender's private key to sign transfer transaction
 * `gas`: gas amount for transaction (default 100000)
 * `bladePayFee`: blade pay fee, otherwise fee will be pay from sender accountId
 * `completion`: callback function, with result of TransactionReceiptData or BladeJSError

```kotlin
fun contractCallFunction(contractId: String, functionName: String, params: ContractFunctionParameters, accountId: String, accountPrivateKey: String, gas: Int = 100000, bladePayFee: Boolean, completion: (TransactionReceiptData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("contractCallFunction")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<TransactionReceiptData, TransactionReceiptResponse>(data, error, completion)
    }
    // TODO check if we need to escape ContractFunctionParams.encode() result \'
    executeJS("bladeSdk.contractCallFunction('${esc(contractId)}', '${esc(functionName)}', '${params.encode()}', '${esc(accountId)}', '${esc(accountPrivateKey)}', $gas, $bladePayFee, '$completionKey')")
}
```

## Method to call smart-contract query

### Parameters:

 * `contractId`: contract id
 * `functionName`: contract function name
 * `params`: function arguments (instance of ContractFunctionParameters)
 * `accountId`: sender account id
 * `accountPrivateKey`: sender's private key to sign transfer transaction
 * `gas`: gas amount for transaction (default 100000)
 * `bladePayFee`: blade pay fee, otherwise fee will be pay from sender accountId
 * `returnTypes`: List of return types, e.g. listOf("string", "int32")
 * `completion`: callback function, with result of ContractQueryData or BladeJSError

```kotlin
fun contractCallQueryFunction(contractId: String, functionName: String, params: ContractFunctionParameters, accountId: String, accountPrivateKey: String, gas: Int = 100000, bladePayFee: Boolean, returnTypes: List<String>, completion: (ContractQueryData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("contractCallQueryFunction")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<ContractQueryData, ContractQueryResponse>(data, error, completion)
    }

    executeJS("bladeSdk.contractCallQueryFunction('${esc(contractId)}', '${esc(functionName)}', '${params.encode()}', '${esc(accountId)}', '${esc(accountPrivateKey)}', $gas, $bladePayFee, ${returnTypes.joinToString(",", "[", "]") {"\'${esc(it)}\'"}}, '$completionKey')")
}
```

## Sign message with private key (ethers lib)

### Parameters:

 * `messageString`: message in base64 string
 * `privateKey`: private key string
 * `completion`: callback function, with result of SignMessageData or BladeJSError

```kotlin
fun ethersSign(messageString: String, privateKey: String, completion: (SignMessageData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("ethersSign")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<SignMessageData, SignMessageResponse>(data, error, completion)
    }
    executeJS("bladeSdk.ethersSign('${esc(messageString)}', '${esc(privateKey)}', '$completionKey')")
}
```

## Method to split signature into v-r-s

### Parameters:

 * `signature`: signature string "0x21fbf0696......"
 * `completion`: callback function, with result of SplitSignatureData or BladeJSError

```kotlin
fun splitSignature(signature: String, completion: (SplitSignatureData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("splitSignature")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<SplitSignatureData, SplitSignatureResponse>(data, error, completion)
    }
    executeJS("bladeSdk.splitSignature('${esc(signature)}', '$completionKey')")
}
```

## Get signature for contract params into v-r-s

### Parameters:

 * `params`: function arguments (instance of ContractFunctionParameters)
 * `accountPrivateKey`: account private key string
 * `completion`: callback function, with result of SplitSignatureData or BladeJSError

```kotlin
fun getParamsSignature(params: ContractFunctionParameters, accountPrivateKey: String, completion: (SplitSignatureData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("getParamsSignature")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<SplitSignatureData, SplitSignatureResponse>(data, error, completion)
    }
    executeJS("bladeSdk.getParamsSignature('${params.encode()}', '${esc(accountPrivateKey)}', '$completionKey')")
}
```

## Method to get transactions history

### Parameters:

 * `accountId`: accountId of history
 * `transactionType`: filter by type of transaction
 * `nextPage`: link from response to load next page of history
 * `transactionsLimit`: limit of transactions to load
 * `completion`: callback function, with result of TransactionsHistoryData or BladeJSError

```kotlin
fun getTransactions(accountId: String, transactionType: String, nextPage: String = "", transactionsLimit: Int = 10, completion: (TransactionsHistoryData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("getTransactions")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<TransactionsHistoryData, TransactionsHistoryResponse>(data, error, completion)
    }
    executeJS("bladeSdk.getTransactions('${esc(accountId)}', '${esc(transactionType)}', '${esc(nextPage)}', '$transactionsLimit', '$completionKey')")
}
```

## Method to get C14 url for payment

### Parameters:

 * `asset`: USDC, HBAR, KARATE or C14 asset uuid
 * `account`: receiver account id
 * `amount`: amount to buy
 * `completion`: callback function, with result of IntegrationUrlData or BladeJSError

```kotlin
fun getC14url(asset: String, account: String, amount: String = "", completion: (IntegrationUrlData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("getC14url")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<IntegrationUrlData, IntegrationUrlResponse>(data, error, completion)
    }
    executeJS("bladeSdk.getC14url('${esc(asset)}', '${esc(account)}', '${esc(amount)}', '$completionKey')")
}
```

## Method to get swap quotes from different services

### Parameters:

 * `sourceCode`: name (HBAR, KARATE, other token code)
 * `sourceAmount`: amount to swap, buy or sell
 * `targetCode`: name (HBAR, KARATE, USDC, other token code)
 * `strategy`: one of enum CryptoFlowServiceStrategy (Buy, Sell, Swap)
 * `completion`: callback function, with result of SwapQuotesData or BladeJSError

```kotlin
fun exchangeGetQuotes(sourceCode: String, sourceAmount: Double, targetCode: String, strategy: CryptoFlowServiceStrategy, completion: (SwapQuotesData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("exchangeGetQuotes")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<SwapQuotesData, SwapQuotesResponse>(data, error, completion)
    }
    executeJS("bladeSdk.exchangeGetQuotes('${esc(sourceCode)}', ${sourceAmount}, '${esc(targetCode)}', '${esc(strategy.value)}', '$completionKey')")
}
```

## Method to get configured url to buy or sell tokens or fiat

### Parameters:

 * `strategy`: Buy / Sell
 * `accountId`: account id
 * `sourceCode`: name (HBAR, KARATE, USDC, other token code)
 * `sourceAmount`: amount to buy/sell
 * `targetCode`: name (HBAR, KARATE, USDC, other token code)
 * `slippage`: slippage in percents. Transaction will revert if the price changes unfavorably by more than this percentage.
 * `serviceId`: service id to use for swap (saucerswap, onmeta, etc)
 * `redirectUrl`: url to redirect after final step
 * `completion`: callback function, with result of IntegrationUrlData or BladeJSError

```kotlin
fun getTradeUrl(strategy: CryptoFlowServiceStrategy, accountId: String, sourceCode: String, sourceAmount: Double, targetCode: String, slippage: Double, serviceId: String, redirectUrl: String = "", completion: (IntegrationUrlData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("getTradeUrl")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<IntegrationUrlData, IntegrationUrlResponse>(data, error, completion)
    }
    executeJS("bladeSdk.getTradeUrl('${esc(strategy.value)}', '${esc(accountId)}', '${esc(sourceCode)}', ${sourceAmount}, '${esc(targetCode)}', ${slippage}, '${esc(serviceId)}', '${esc(redirectUrl)}', '$completionKey')")
}
```

## Method to swap tokens

### Parameters:

 * `accountId`: account id
 * `accountPrivateKey`: account private key
 * `sourceCode`: name (HBAR, KARATE, other token code)
 * `sourceAmount`: amount to swap
 * `targetCode`: name (HBAR, KARATE, other token code)
 * `slippage`: slippage in percents. Transaction will revert if the price changes unfavorably by more than this percentage.
 * `serviceId`: service id to use for swap (saucerswap, etc)
 * `completion`: callback function, with result of ResultData or BladeJSError

```kotlin
fun swapTokens(accountId: String, accountPrivateKey: String, sourceCode: String, sourceAmount: Double, targetCode: String, slippage: Double, serviceId: String, completion: (ResultData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("swapTokens")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<ResultData, ResultResponse>(data, error, completion)
    }
    executeJS("bladeSdk.swapTokens('${esc(accountId)}', '${esc(accountPrivateKey)}', '${esc(sourceCode)}', ${sourceAmount}, '${esc(targetCode)}', ${slippage}, '${esc(serviceId)}', '$completionKey')")
}
```

## Create token (NFT or Fungible Token)

### Parameters:

 * `treasuryAccountId`: treasury account id
 * `supplyPrivateKey`: supply account private key
 * `tokenName`: token name (string up to 100 bytes)
 * `tokenSymbol`: token symbol (string up to 100 bytes)
 * `isNft`: set token type NFT
 * `keys`: token keys
 * `decimals`: token decimals (0 for nft)
 * `initialSupply`: token initial supply (0 for nft)
 * `maxSupply`: token max supply
 * `completion`: callback function, with result of CreateTokenData or BladeJSError

```kotlin
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
```


## Associate token to account. Association fee will be covered by Blade, if tokenId configured in dApp

### Parameters:

 * `tokenId`: token id to associate. Empty to associate all tokens configured in dApp
 * `accountId`: account id to associate token
 * `accountPrivateKey`: account private key
 * `completion`: callback function, with result of TransactionReceiptData or BladeJSError

```kotlin
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
```

## Mint one NFT

### Parameters:

 * `tokenId`: token id to mint NFT
 * `supplyAccountId`: token supply account id
 * `supplyPrivateKey`: token supply private key
 * `file`: image to mint
 * `metadata`: NFT metadata
 * `storageConfig`: IPFS provider config
 * `completion`: callback function, with result of CreateTokenData or BladeJSError

```kotlin
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
```

## Method to clean-up webView

```kotlin
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
```