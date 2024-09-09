# Contents

* [initialize](usage.md#initialize)
* [getInfo](usage.md#getinfo)
* [setUser](usage.md#setuser)
* [resetUser](usage.md#resetuser)
* [getBalance](usage.md#getbalance)
* [transferBalance](usage.md#transferbalance)
* [transferTokens](usage.md#transfertokens)
* [getCoinList](usage.md#getcoinlist)
* [getCoinPrice](usage.md#getcoinprice)
* [contractCallFunction](usage.md#contractcallfunction)
* [contractCallQueryFunction](usage.md#contractcallqueryfunction)
* [createScheduleTransaction](usage.md#createscheduletransaction)
* [signScheduleId](usage.md#signscheduleid)
* [createAccount](usage.md#createaccount)
* [deleteAccount](usage.md#deleteaccount)
* [getAccountInfo](usage.md#getaccountinfo)
* [getNodeList](usage.md#getnodelist)
* [stakeToNode](usage.md#staketonode)
* [searchAccounts](usage.md#searchaccounts)
* [dropTokens](usage.md#droptokens)
* [sign](usage.md#sign)
* [verify](usage.md#verify)
* [splitSignature](usage.md#splitsignature)
* [getParamsSignature](usage.md#getparamssignature)
* [getTransactions](usage.md#gettransactions)
* [exchangeGetQuotes](usage.md#exchangegetquotes)
* [getTradeUrl](usage.md#gettradeurl)
* [swapTokens](usage.md#swaptokens)
* [createToken](usage.md#createtoken)
* [associateToken](usage.md#associatetoken)
* [nftMint](usage.md#nftmint)
* [getTokenInfo](usage.md#gettokeninfo)
* [cleanup](usage.md#cleanup)
* [postMessage](usage.md#postmessage)

# Methods

## initialize

Init instance of BladeSDK for correct work with Blade API and other endpoints.

`initialize (apiKey: String, chain: KnownChains, dAppCode: String, bladeEnv: BladeEnv = BladeEnv.Prod, context: Context, force: Boolean = false, completion: (InfoData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `apiKey` | `String` | Unique key for API provided by Blade team. |
| `chain` | `KnownChains` | one of supported chains from KnownChains |
| `dAppCode` | `String` | your dAppCode - request specific one by contacting BladeLabs team |
| `bladeEnv` | `BladeEnv` | field to set BladeAPI environment (Prod, CI). Prod used by default. |
| `context` | `Context` | android context |
| `force` | `Boolean` | optional field to force init. Will not crash if already initialized |
| `completion` | `(InfoData?,BladeJSError?)->Unit` | callback function, with result of InfoData or BladeJSError |

#### Returns

`InfoData` - with information about Blade instance, including visitorId

#### Example

```kotlin
Blade.initialize(
    Config.apiKey, Config.chain, Config.dAppCode, Config.bladeEnv, requireContext(), false
) { infoData, error ->
    println(infoData ?: error)
}
```

## getInfo

Returns information about initialized instance of BladeSDK.

`getInfo (completion: (InfoData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `completion` | `(InfoData?,BladeJSError?)->Unit` | callback function, with result of InfoData or BladeJSError |

#### Returns

`InfoData` - with information about Blade instance, including visitorId

#### Example

```kotlin
Blade.getInfo { infoData, error ->
    println(infoData ?: error)
}
```

## setUser

Set active user for further operations.

`setUser (accountProvider: AccountProvider, accountIdOrEmail: String, privateKey: String, completion: (UserInfoData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `accountProvider` | `AccountProvider` | one of supported providers: PrivateKey or Magic |
| `accountIdOrEmail` | `String` | account id (0.0.xxxxx, 0xABCDEF..., EMAIL) or empty string for some Chains |
| `privateKey` | `String` | private key for account (hex encoded privateKey with DER-prefix or 0xABCDEF...) In case of Magic provider - empty string |
| `completion` | `(UserInfoData?,BladeJSError?)->Unit` |  |

#### Returns

`UserInfoData` - with information about active user

#### Example

```kotlin
Blade.setUser(AccountProvider.PrivateKey, "0.0.1234", "302d300706052b8104000a032200029dc73991b0d9cd...") { userInfoData, error ->
    println(userInfoData ?: error)
}
```

## resetUser

Reset active user

`resetUser (completion: (UserInfoData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `completion` | `(UserInfoData?,BladeJSError?)->Unit` |  |

#### Returns

`UserInfoData` - with information about active user

#### Example

```kotlin
Blade.resetUser { userInfoData, error ->
    println(userInfoData ?: error)
}
```

## getBalance

Get balance and token balances for specific account.

`getBalance (accountAddress: String, completion: (BalanceData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `accountAddress` | `String` | Hedera account id (0.0.xxxxx) or Ethereum address (0x...) or empty string to use current user account |
| `completion` | `(BalanceData?,BladeJSError?)->Unit` | callback function, with result of BalanceData or BladeJSError |

#### Returns

{BalanceData}

#### Example

```kotlin
Blade.getBalance("0.0.45467464") { result, error ->
    println("${ result ?: error}")
}
```

## transferBalance

Send account balance (HBAR/ETH) to specific account.

`transferBalance (receiverAddress: String, amount: String, memo: String, completion: (TransactionResponseData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `receiverAddress` | `String` | receiver address (0.0.xxxxx, 0x123456789abcdef...) |
| `amount` | `String` | amount of currency to send, as a string representing a decimal number (e.g., "211.3424324") |
| `memo` | `String` | memo (limited to 100 characters) |
| `completion` | `(TransactionResponseData?,BladeJSError?)->Unit` | callback function, with result of TransactionReceiptData or BladeJSError |

#### Returns

`TransactionResponseData` - receipt

#### Example

```kotlin
val receiverAddress = "0.0.10002"
val amount = "2.5"
Blade.transferBalance(
    receiverAddress,
    amount,
    "Some memo text"
) { result, error ->
    println(result ?: error)
}
```

## transferTokens

Send token to specific address

`transferTokens (tokenAddress: String, receiverAddress: String, amountOrSerial: String, memo: String, usePaymaster: Boolean = true, completion: (TransactionResponseData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `tokenAddress` | `String` | token address to send (0.0.xxxxx or 0x123456789abcdef...) |
| `receiverAddress` | `String` | receiver account address (0.0.xxxxx or 0x123456789abcdef...) |
| `amountOrSerial` | `String` | amount of fungible tokens to send (with token-decimals correction) on NFT serial number. (e.g. amount 0.01337 when token decimals 8 will send 1337000 units of token) |
| `memo` | `String` | transaction memo (limited to 100 characters) |
| `usePaymaster` | `Boolean` | if true, Paymaster account will pay fee transaction, for dApp configured fungible-token |
| `completion` | `(TransactionResponseData?,BladeJSError?)->Unit` | callback function, with result of TransactionReceiptData or BladeJSError |

#### Returns

`TransactionResponseData` - receipt

#### Example

```kotlin
val tokenAddress = "0.0.1337"
val receiverAddress = "0.0.10002"
val amount = "2.5"
Blade.transferTokens(
    tokenAddress,
    receiverAddress,
    amount,
    "Token transfer memo"
) { result, error ->
    println(result ?: error)
}
```

## getCoinList

Get list of all available coins on CoinGecko.

`getCoinList (completion: (CoinListData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `completion` | `(CoinListData?,BladeJSError?)->Unit` | callback function, with result of CoinListData or BladeJSError |

#### Returns

`CoinListData` - with list of coins described by name, alias, platforms

#### Example

```kotlin
Blade.getCoinList { result, error ->
    if (result != null) {
        for (coin in result.coins) {
            println(coin)
        }
    } else {
        println(error)
    }
}
```

## getCoinPrice

Get coin price and coin info from CoinGecko. Search can be coin id or address in one of the coin platforms.

* In addition to the price in USD, the price in the currency you specified is returned

`getCoinPrice (search: String, currency: String = "usd", completion: (CoinInfoData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `search` | `String` | CoinGecko coinId, or address in one of the coin platforms or `hbar` (default, alias for `hedera-hashgraph`) |
| `currency` | `String` | result currency for price field |
| `completion` | `(CoinInfoData?,BladeJSError?)->Unit` | callback function, with result of CoinListData or BladeJSError |

#### Returns

{CoinInfoData}

#### Example

```kotlin
Blade.getCoinPrice(
    search = "hbar",
    currency = "uah"
) { result, bladeJSError ->
    println("${result ?: bladeJSError}")
}
```

## contractCallFunction

Call contract function. Directly or via BladeAPI using paymaster account (fee will be paid by Paymaster account), depending on your dApp configuration.

`contractCallFunction (contractAddress: String, functionName: String, params: ContractFunctionParameters, gas: Int = 100000, usePaymaster: Boolean, completion: (TransactionReceiptData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `contractAddress` | `String` | - contract address (0.0.xxxxx or 0x123456789abcdef...) |
| `functionName` | `String` | name of the contract function to call |
| `params` | `ContractFunctionParameters` | function argument. Can be generated with |
| `gas` | `Int` | gas limit for the transaction |
| `usePaymaster` | `Boolean` | if true, fee will be paid by Paymaster account (note: msg.sender inside the contract will be Paymaster account) |
| `completion` | `(TransactionReceiptData?,BladeJSError?)->Unit` | callback function, with result of TransactionReceiptData or BladeJSError |

#### Returns

`TransactionReceiptData` - receipt

#### Example

```kotlin
val contractAddress = "0.0.123456"
val functionName = "set_message"
val parameters = ContractFunctionParameters().addString("hello")
val gas = 155000
val usePaymaster = false
Blade.contractCallFunction(
    contractId,
    functionName,
    parameters,
    gas,
    usePaymaster
) { result, error ->
    println(result ?: error)
}
```

## contractCallQueryFunction

Call query on contract function. Similar to {@link contractCallFunction} can be called directly or via BladeAPI using Paymaster account.

`contractCallQueryFunction (contractAddress: String, functionName: String, params: ContractFunctionParameters, gas: Int = 100000, usePaymaster: Boolean, returnTypes: List<String>, completion: (ContractCallQueryRecordsData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `contractAddress` | `String` | contract address (0.0.xxxxx or 0x123456789abcdef...) |
| `functionName` | `String` | name of the contract function to call |
| `params` | `ContractFunctionParameters` | function argument. Can be generated with |
| `gas` | `Int` | gas limit for the transaction |
| `usePaymaster` | `Boolean` | if true, the fee will be paid by paymaster account (note: msg.sender inside the contract will be Paymaster account) |
| `returnTypes` | `List<String>` | List of return types, e.g. listOf("string", "int32") |
| `completion` | `(ContractCallQueryRecordsData?,BladeJSError?)->Unit` | callback function, with result of ContractQueryData or BladeJSError |

#### Returns

`ContractCallQueryRecordsData` - contract query call result

#### Example

```kotlin
val contractAddress = "0.0.123456"
val functionName = "get_message"
val parameters = ContractFunctionParameters()
val gas = 55000
val usePaymaster = false
val returnTypes = listOf("string", "int32")
Blade.contractCallQueryFunction(
    contractAddress,
    functionName,
    parameters,
    gas,
    usePaymaster,
    returnTypes
) { result, error ->
    lifecycleScope.launch {
        println(result ?: error)
    }
}
```

## createScheduleTransaction

Create scheduled transaction

`createScheduleTransaction ( type: ScheduleTransactionType, transfers: List<ScheduleTransactionTransfer>, usePaymaster: Boolean = false, completion: (CreateScheduleData?, BladeJSError?) -> Unit )`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `type` | `ScheduleTransactionType` | schedule transaction type (currently only TRANSFER supported) |
| `transfers` | `List<ScheduleTransactionTransfer>` | array of transfers to schedule (HBAR, FT, NFT) |
| `usePaymaster` | `Boolean` | if true, Paymaster account will pay transaction fee (also dApp had to be configured for free schedules) |
| `completion` | `(CreateScheduleData?,BladeJSError?)->Unit` | callback function, with result of CreateScheduleData or BladeJSError |

#### Returns

`CreateScheduleData` - scheduleId

#### Example

```kotlin
val senderId = "0.0.10001"
val receiverId = "0.0.10002"
val tokenId = "0.0.1337"
var scheduleId = ""
Blade.createScheduleTransaction(
    type = ScheduleTransactionType.TRANSFER,
    transfers = listOf(
        ScheduleTransactionTransferHbar(sender = senderId, receiver = receiverId, 10000000),
        ScheduleTransactionTransferToken(sender = senderId, receiver = receiverId, tokenId = tokenId, value = 3)
    ),
    usePaymaster = true,
) { result, error ->
    if (result != null) {
        println(result.scheduleId)
    }
}
```

## signScheduleId

Method to sign scheduled transaction

`signScheduleId ( scheduleId: String, receiverAccountAddress: String = "", usePaymaster: Boolean = false, completion: (TransactionReceiptData?, BladeJSError?) -> Unit )`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `scheduleId` | `String` | scheduled transaction id (0.0.xxxxx) |
| `receiverAccountAddress` | `String` | account id of receiver for additional validation in case of dApp freeSchedule transactions configured |
| `usePaymaster` | `Boolean` | if true, Paymaster account will pay transaction fee (also dApp had to be configured for free schedules) |
| `completion` | `(TransactionReceiptData?,BladeJSError?)->Unit` | callback function, with result of TransactionReceiptData or BladeJSError |

#### Returns

`TransactionReceiptData` - receipt

#### Example

```kotlin
val receiverAddress = "0.0.10002"
var scheduleId = "0.0...." // result of createScheduleTransaction on receiver side
Blade.signScheduleId(
    scheduleId = scheduleId,
    receiverAccountAddress = receiverAddress,
    usePaymaster = true
) { result, bladeJSError ->
    println(result ?: bladeJSError)
}
```

## createAccount

Create new account (ECDSA by default). Depending on dApp config Blade will create an account, associate tokens, etc.

`createAccount (privateKey: String = "", deviceId: String = "", completion: (CreatedAccountData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `privateKey` | `String` | optional field if you need specify account key (hex encoded privateKey with DER-prefix) |
| `deviceId` | `String` | optional field unique device id (advanced security feature, required only for some dApps) |
| `completion` | `(CreatedAccountData?,BladeJSError?)->Unit` | callback function, with result of CreatedAccountData or BladeJSError |

#### Returns

`CreatedAccountData` - new account data, including private key and account id

#### Example

```kotlin
Blade.createAccount() { result, error ->
    println(result ?: error)
}
```

## deleteAccount

Delete Hedera account.

`deleteAccount (deleteAccountId: String, deletePrivateKey: String, transferAccountId: String, completion: (TransactionReceiptData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `deleteAccountId` | `String` | account id of account to delete (0.0.xxxxx) |
| `deletePrivateKey` | `String` | account private key (DER encoded hex string) |
| `transferAccountId` | `String` | The ID of the account to transfer the remaining funds to. |
| `completion` | `(TransactionReceiptData?,BladeJSError?)->Unit` | callback function, with result of TransactionReceiptData or BladeJSError |

#### Returns

`TransactionReceiptData` - receipt

#### Example

```kotlin
val deleteAccountId = "0.0.65468464"
val deletePrivateKey = "3030020100300706052b8104000a04220420ebc..."
val transferAccountId = "0.0.10001"
Blade.deleteAccount(
    deleteAccountId,
    deletePrivateKey,
    transferAccountId,
) { result, error ->
    println(result ?: error)
}
```

## getAccountInfo

Get account info.

* EvmAddress is address of Hedera account if exists. Else accountId will be converted to solidity address.

* CalculatedEvmAddress is calculated from account public key. May be different from evmAddress.

`getAccountInfo (accountId: String, completion: (AccountInfoData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `accountId` | `String` | Hedera account id (0.0.xxxxx) |
| `completion` | `(AccountInfoData?,BladeJSError?)->Unit` | callback function, with result of AccountInfoData or BladeJSError |

#### Returns

{AccountInfoData}

#### Example

```kotlin
Blade.getAccountInfo("0.0.10002") { accountInfoData, error ->
    println(accountInfoData ?: error)
}
```

## getNodeList

Get Hedera node list available for stake

`getNodeList (completion: (NodesData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `completion` | `(NodesData?,BladeJSError?)->Unit` | callback function, with result of NodesData or BladeJSError |

#### Returns

`NodesData` - node list

#### Example

```kotlin
Blade.getNodeList { nodeListData, error ->
    println(nodeListData ?: error)
}
```

## stakeToNode

Stake/unstake hedera account

`stakeToNode (nodeId: Int, completion: (TransactionReceiptData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `nodeId` | `Int` | node id to stake to. If negative or null, account will be unstaked |
| `completion` | `(TransactionReceiptData?,BladeJSError?)->Unit` | callback function, with result of TransactionReceiptData or BladeJSError |

#### Returns

`TransactionReceiptData` - receipt

#### Example

```kotlin
Blade.stakeToNode(5) { result, error ->
    println(result ?: error)
}
```

## searchAccounts

Get accounts list and keys from private key or mnemonic

* Supporting standard and legacy key derivation.

* Every key with account will be returned.

`searchAccounts (keyOrMnemonic: String, completion: (AccountPrivateData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `keyOrMnemonic` | `String` | BIP39 mnemonic, private key with DER header |
| `completion` | `(AccountPrivateData?,BladeJSError?)->Unit` | callback function, with result of AccountPrivateData or BladeJSError |

#### Returns

`AccountPrivateData` - list of found accounts with private keys

#### Example

```kotlin
Blade.searchAccounts("purity slab doctor swamp tackle rebuild summer bean craft toddler blouse switch") { result, error ->
    println(result ?: error)
}
```

## dropTokens

Bladelink drop to account

`dropTokens (secretNonce: String, completion: (TokenDropData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `secretNonce` | `String` | configured for dApp. Should be kept in secret |
| `completion` | `(TokenDropData?,BladeJSError?)->Unit` | callback function, with result of TokenDropData or BladeJSError |

#### Returns

`TokenDropData` - status

#### Example

```kotlin
val secretNonce = "[ REDACTED ]"
Blade.dropTokens(secretNonce) { result, error ->
    println(result ?: error)
}
```

## sign

Sign encoded message with private key. Returns hex-encoded signature.

`sign (encodedMessage: String, encoding: SupportedEncoding, likeEthers: Boolean, completion: (SignMessageData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `encodedMessage` | `String` | encoded message to sign |
| `encoding` | `SupportedEncoding` | one of the supported encodings (hex/base64/utf8) |
| `likeEthers` | `Boolean` | to get signature in ethers format. Works only for ECDSA keys. Ignored on chains other than Hedera |
| `completion` | `(SignMessageData?,BladeJSError?)->Unit` | callback function, with result of SignMessageData or BladeJSError |

#### Returns

`SignMessageData` - signature

#### Example

```kotlin
val encodedMessage = "hello"
val encoding = SupportedEncoding.utf8
val likeEthers = false
Blade.sign(
    encodedMessage,
    encoding,
    likeEthers
) { result, error ->
    println(result ?: error)
}
```

## verify

Verify message signature with public key

`verify (encodedMessage: String, encoding: SupportedEncoding, signature: String, addressOrPublicKey: String, completion: (SignVerifyMessageData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `encodedMessage` | `String` | encoded message (same as provided to `sign()` method) |
| `encoding` | `SupportedEncoding` | one of the supported encodings (hex/base64/utf8) |
| `signature` | `String` | hex-encoded signature (result from `sign()` method) |
| `addressOrPublicKey` | `String` | EVM-address, publicKey, or Hedera address (0x11f8D856FF2aF6700CCda4999845B2ed4502d8fB, 0x0385a2fa81f8acbc47fcfbae4aeee6608c2d50ac2756ed88262d102f2a0a07f5b8, 0.0.1512, or empty for current account) |
| `completion` | `(SignVerifyMessageData?,BladeJSError?)->Unit` | callback function, with result of SignVerifyMessageData or BladeJSError |

#### Returns

`SignVerifyMessageData` - verification result

#### Example

```kotlin
val encodedMessage = "hello"
val encoding = SupportedEncoding.utf8
val signature = "27cb9d51434cf1e76d7ac515b19442c619f641e6fccddbf4a3756b14466becb6992dc1d2a82268018147141fc8d66ff9ade43b7f78c176d070a66372d655f942"
val addressOrPublicKey = "302d300706052b8104000a032200029dc73991b0d9cdbb59b2cd0a97a0eaff6de801726cb39804ea9461df6be2dd30"
Blade.verify(
    encodedMessage,
    encoding,
    signature,
    addressOrPublicKey
) { result, error ->
    lifecycleScope.launch {
        println(result ?: error)
    }
}
```

## splitSignature

Split signature to v-r-s format.

`splitSignature (signature: String, completion: (SplitSignatureData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `signature` | `String` | hex-encoded signature |
| `completion` | `(SplitSignatureData?,BladeJSError?)->Unit` | callback function, with result of SplitSignatureData or BladeJSError |

#### Returns

`SplitSignatureData` - v-r-s signature

#### Example

```kotlin
Blade.splitSignature(
    "0x27cb9d51434cf1e76d7ac515b19442c619f641e6fccddbf4a3756b14466becb6992dc1d2a82268018147141fc8d66ff9ade43b7f78c176d070a66372d655f942",
) { result, error ->
    println(result ?: error)
}
```

## getParamsSignature

Get v-r-s signature of contract function params

`getParamsSignature (params: ContractFunctionParameters, completion: (SplitSignatureData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `params` | `ContractFunctionParameters` | data to sign. (instance of ContractFunctionParameters) |
| `completion` | `(SplitSignatureData?,BladeJSError?)->Unit` | callback function, with result of SplitSignatureData or BladeJSError |

#### Returns

`SplitSignatureData` - v-r-s signature

#### Example

```kotlin
val parameters = ContractFunctionParameters().addString("hello")
Blade.getParamsSignature(
    parameters,
) { result, error ->
    println(result ?: error)
}
```

## getTransactions

Get transactions history for account. Can be filtered by transaction type.

* Transaction requested from mirror node. Every transaction requested for child transactions. Result are flattened.

* If transaction type is not provided, all transactions will be returned.

* If transaction type is CRYPTOTRANSFERTOKEN records will additionally contain plainData field with decoded data.

`getTransactions (accountAddress: String, transactionType: String, nextPage: String = "", transactionsLimit: Int = 10, completion: (TransactionsHistoryData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `accountAddress` | `String` | account id to get transactions for (0.0.xxxxx) |
| `transactionType` | `String` | one of enum MirrorNodeTransactionType or "CRYPTOTRANSFERTOKEN" |
| `nextPage` | `String` | link to next page of transactions from previous request |
| `transactionsLimit` | `Int` | number of transactions to return. Speed of request depends on this value if transactionType is set. |
| `completion` | `(TransactionsHistoryData?,BladeJSError?)->Unit` | callback function, with result of TransactionsHistoryData or BladeJSError |

#### Returns

`TransactionsHistoryData` - transactions list

#### Example

```kotlin
Blade.getTransactions(
    accountAddress = "0.0.10002",
    transactionType = "",
    nextPage = "",
    transactionsLimit = 15
) { result, error ->
    println(result ?: error)
}
```

## exchangeGetQuotes

Get quotes from different services for buy, sell or swap

`exchangeGetQuotes (sourceCode: String, sourceAmount: Double, targetCode: String, strategy: ExchangeStrategy, completion: (SwapQuotesData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `sourceCode` | `String` | name (HBAR, KARATE, other token code) |
| `sourceAmount` | `Double` | amount to swap, buy or sell |
| `targetCode` | `String` | name (HBAR, KARATE, USDC, other token code) |
| `strategy` | `ExchangeStrategy` | one of enum CryptoFlowServiceStrategy (Buy, Sell, Swap) |
| `completion` | `(SwapQuotesData?,BladeJSError?)->Unit` | callback function, with result of SwapQuotesData or BladeJSError |

#### Returns

`SwapQuotesData` - quotes from different providers

#### Example

```kotlin
Blade.exchangeGetQuotes(
    sourceCode = "EUR",
    sourceAmount = 50.0,
    targetCode = "HBAR",
    strategy = CryptoFlowServiceStrategy.BUY
) { result, error ->
    println(result ?: error)
}
```

## getTradeUrl

Get configured url to buy or sell tokens or fiat

`getTradeUrl (strategy: ExchangeStrategy, accountAddress: String, sourceCode: String, sourceAmount: Double, targetCode: String, slippage: Double, serviceId: String, redirectUrl: String = "", completion: (IntegrationUrlData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `strategy` | `ExchangeStrategy` | Buy / Sell |
| `accountAddress` | `String` | account id |
| `sourceCode` | `String` | name (HBAR, KARATE, USDC, other token code) |
| `sourceAmount` | `Double` | amount to buy/sell |
| `targetCode` | `String` | name (HBAR, KARATE, USDC, other token code) |
| `slippage` | `Double` | slippage in percents. Transaction will revert if the price changes unfavorably by more than this percentage. |
| `serviceId` | `String` | service id to use for swap (saucerswap, onmeta, etc) |
| `redirectUrl` | `String` | url to redirect after final step |
| `completion` | `(IntegrationUrlData?,BladeJSError?)->Unit` | callback function, with result of IntegrationUrlData or BladeJSError |

#### Returns

`IntegrationUrlData` - url to open

#### Example

```kotlin
Blade.getTradeUrl(
    strategy = CryptoFlowServiceStrategy.BUY,
    accountAddress = "0.0.10002",
    sourceCode = "EUR",
    sourceAmount = 50.0,
    targetCode = "HBAR",
    slippage = 0.5,
    serviceId = "moonpay"
) { result, error ->
    println(result ?: error)
}
```

## swapTokens

Swap tokens

`swapTokens (sourceCode: String, sourceAmount: Double, targetCode: String, slippage: Double, serviceId: String, completion: (ResultData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `sourceCode` | `String` | name (HBAR, KARATE, other token code) |
| `sourceAmount` | `Double` | amount to swap |
| `targetCode` | `String` | name (HBAR, KARATE, other token code) |
| `slippage` | `Double` | slippage in percents. Transaction will revert if the price changes unfavorably by more than this percentage. |
| `serviceId` | `String` | service id to use for swap (saucerswap, etc) |
| `completion` | `(ResultData?,BladeJSError?)->Unit` | callback function, with result of ResultData or BladeJSError |

#### Returns

`ResultData` - swap result

#### Example

```kotlin
val sourceCode = "USDC"
val targetCode = "KARATE"
Blade.swapTokens(
    sourceCode,
    sourceAmount = 123.4,
    targetCode,
    slippage = 0.5,
    serviceId = "moonpay"
) { result, error ->
    println(result ?: error)
}
```

## createToken

Create token (NFT or Fungible Token)

`createToken ( tokenName: String, tokenSymbol: String, isNft: Boolean, keys: List<KeyRecord>, decimals: Int, initialSupply: Int, maxSupply: Int, completion: (CreateTokenData?, BladeJSError?) -> Unit )`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `tokenName` | `String` | token name (string up to 100 bytes) |
| `tokenSymbol` | `String` | token symbol (string up to 100 bytes) |
| `isNft` | `Boolean` | set token type NFT |
| `keys` | `List<KeyRecord>` | token keys |
| `decimals` | `Int` | token decimals (0 for nft) |
| `initialSupply` | `Int` | token initial supply (0 for nft) |
| `maxSupply` | `Int` | token max supply |
| `completion` | `(CreateTokenData?,BladeJSError?)->Unit` | callback function, with result of CreateTokenData or BladeJSError |

#### Returns

`CreateTokenData` - token id

#### Example

```kotlin
val keys = listOf(
    KeyRecord(Config.adminPrivateKey, KeyType.admin)
)
Blade.createToken(
        tokenName = "Blade Demo Token",
        tokenSymbol = "GD",
        isNft = true,
        keys,
        decimals = 0,
        initialSupply = 0,
        maxSupply = 250
) { result, error ->
    println(result ?: error)
}
```

## associateToken

Associate token to hedera account. Association fee will be covered by PayMaster, if tokenId configured in dApp

`associateToken ( tokenIdOrCampaign: String, completion: (TransactionReceiptData?, BladeJSError?) -> Unit )`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `tokenIdOrCampaign` | `String` | token id to associate. Empty to associate all tokens configured in dApp.  Campaign name to associate on demand |
| `completion` | `(TransactionReceiptData?,BladeJSError?)->Unit` | callback function, with result of TransactionReceiptData or BladeJSError |

#### Returns

`TransactionReceiptData` - receipt

#### Example

```kotlin
Blade.associateToken(
    tokenIdOrCampaign = "0.0.1337",
) { result, error ->
    println(result ?: error)
}
```

## nftMint

Mint one NFT

`nftMint ( tokenAddress: String, file: String, metadata: Map<String, Any>, storageConfig: NFTStorageConfig, completion: (TransactionReceiptData?, BladeJSError?) -> Unit )`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `tokenAddress` | `String` | token id to mint NFT |
| `file` | `String` | image to mint (base64 DataUrl image, eg.: data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAA...) |
| `metadata` | `Map<String,Any>` | NFT metadata |
| `storageConfig` | `NFTStorageConfig` | IPFS provider config |
| `completion` | `(TransactionReceiptData?,BladeJSError?)->Unit` | callback function, with result of CreateTokenData or BladeJSError |

#### Returns

`TransactionReceiptData` - receipt

#### Example

```kotlin
val tokenAddress = "0.0.13377"
val base64Image = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAYAAAAeP4ixAAAARUlEQVR42u3PMREAAAgEIO1fzU5vBlcPGtCVTD3QIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIiIXCyqyi6fIALs1AAAAAElFTkSuQmCC"
val metaData = mapOf<String, Any>(
    "name" to "NFTitle",
    "score" to 10,
    "power" to 4,
    "intelligence" to 6,
    "speed" to 10
)
val storageConfig = NFTStorageConfig(
    provider = NFTStorageProvider.nftStorage,
    apiKey = "eyJhbGcsfgrgsrgInR5cCI6IkpXVCJ9.eyJzd5235326ZXRocjoweDfsdfsdfFM0ZkZFOEJhNjdCNjc1NDk1Q2NEREFiYjk0NTE4Njdsfc3MiOiJuZnQtc3RvcmFnZSIsImlhdCI6sdfNDQ2NDUxODQ2MiwibmFt4I6IkJsYWRlUcvxcRLLXRlc3RrdffifQ.t1wCiEuiTvcYOwssdZgiYaug4aF8ZrvMBdkTASojWGU"
)
Blade.nftMint(
    tokenAddress,
    file = base64Image,
    metaData,
    storageConfig,
) { result, error ->
    println(result ?: error)
}
```

## getTokenInfo

Get FT or NFT token info

`getTokenInfo ( tokenAddress: String, serial: String, completion: (TokenInfoData?, BladeJSError?) -> Unit )`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `tokenAddress` | `String` | token address (0.0.xxxxx or 0x123456789abcdef...) |
| `serial` | `String` | serial number in case of NFT token |
| `completion` | `(TokenInfoData?,BladeJSError?)->Unit` | callback function, with result of TokenInfoData or BladeJSError |

#### Returns

{TokenInfoData}

#### Example

```kotlin
Blade.getTokenInfo("0.0.1234", "3") { result, error ->
    println(result ?: error)
}
```

## cleanup

Method to clean-up webView



`cleanup ()`




## postMessage

Method to handle JS responses. By technical reasons, must be public, but you can skip it :)



`postMessage (jsonString: String)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `jsonString` | `String` |  |



