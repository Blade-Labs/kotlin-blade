# Public methods 📢

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

##  Method to execure Hbar transfers from current account to receiver

### Parameters:

 * `accountId`: sender account id
 * `accountPrivateKey`: sender's private key to sign transfer transaction
 * `receiverId`: receiver
 * `amount`: amount
 * `memo`: memo (limited to 100 characters)
 * `completion`: callback function, with result of TransferData or BladeJSError

```kotlin
fun transferHbars(accountId: String, accountPrivateKey: String, receiverId: String, amount: Double, memo: String, completion: (TransferData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("transferHbars")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<TransferData, TransferResponse>(data, error, completion)
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
 * `amount`: amount
 * `memo`: memo (limited to 100 characters)
 * `freeTransfer`: for tokens configured for this dAppCode on Blade backend
 * `completion`: callback function, with result of TransferData or BladeJSError

```kotlin
fun transferTokens(tokenId: String, accountId: String, accountPrivateKey: String, receiverId: String, amount: Double, memo: String, freeTransfer: Boolean = true, completion: (TransferData?, BladeJSError?) -> Unit) {
    val completionKey = getCompletionKey("transferTokens")
    deferCompletion(completionKey) { data: String, error: BladeJSError? ->
        typicalDeferredCallback<TransferData, TransferResponse>(data, error, completion)
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

## Restore public and private key by seed phrase

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