# Contents

* [initialize](#initialize)
* [getInfo](#getInfo)
* [getBalance](#getBalance)
* [getCoinList](#getCoinList)
* [getCoinPrice](#getCoinPrice)
* [transferHbars](#transferHbars)
* [transferTokens](#transferTokens)
* [createScheduleTransaction](#createScheduleTransaction)
* [signScheduleId](#signScheduleId)
* [createHederaAccount](#createHederaAccount)
* [getPendingAccount](#getPendingAccount)
* [deleteHederaAccount](#deleteHederaAccount)
* [getAccountInfo](#getAccountInfo)
* [getNodeList](#getNodeList)
* [stakeToNode](#stakeToNode)
* [getKeysFromMnemonic](#getKeysFromMnemonic)
* [searchAccounts](#searchAccounts)
* [dropTokens](#dropTokens)
* [sign](#sign)
* [signVerify](#signVerify)
* [contractCallFunction](#contractCallFunction)
* [contractCallQueryFunction](#contractCallQueryFunction)
* [ethersSign](#ethersSign)
* [splitSignature](#splitSignature)
* [getParamsSignature](#getParamsSignature)
* [getTransactions](#getTransactions)
* [getC14url](#getC14url)
* [exchangeGetQuotes](#exchangeGetQuotes)
* [getTradeUrl](#getTradeUrl)
* [swapTokens](#swapTokens)
* [createToken](#createToken)
* [associateToken](#associateToken)
* [nftMint](#nftMint)
* [cleanup](#cleanup)
* [postMessage](#postMessage)

# Methods

## initialize

Init instance of BladeSDK for correct work with Blade API and Hedera network.

`initialize (apiKey: String, dAppCode: String, network: String, bladeEnv: BladeEnv = BladeEnv.Prod, context: Context, force: Boolean = false, completion: (InfoData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `apiKey` | `String` | Unique key for API provided by Blade team. |
| `dAppCode` | `String` | your dAppCode - request specific one by contacting Bladelabs team |
| `network` | `String` | "Mainnet" or "Testnet" of Hedera network |
| `bladeEnv` | `BladeEnv` | field to set BladeAPI environment (Prod, CI). Prod used by default. |
| `context` | `Context` | android context |
| `force` | `Boolean` | optional field to force init. Will not crash if already initialized |
| `completion` | `(InfoData?,BladeJSError?)->Unit` | callback function, with result of InfoData or BladeJSError |

#### Example

```kotlin
Blade.initialize(
    Config.apiKey, Config.dAppCode, Config.network, Config.bladeEnv, requireContext(), false
) { infoData, error ->
    println(infoData ?: error)
}
```

## getInfo

Get SDK info and check if SDK initialized

`getInfo (completion: (InfoData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `completion` | `(InfoData?,BladeJSError?)->Unit` | callback function, with result of InfoData or BladeJSError |

#### Example

```kotlin
Blade.getInfo { infoData, error ->
    println(infoData ?: error)
}
```

## getBalance

Get balances by account id.

`getBalance (id: String, completion: (BalanceData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `id` | `String` | Hedera account id |
| `completion` | `(BalanceData?,BladeJSError?)->Unit` | callback function, with result of BalanceData or BladeJSError |

#### Example

```kotlin
Blade.getBalance("0.0.45467464") { result, error ->
    println("${ result ?: error}")
}
```

## getCoinList

Get list of all available coins on CoinGecko.

`getCoinList (completion: (CoinListData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `completion` | `(CoinListData?,BladeJSError?)->Unit` | callback function, with result of CoinListData or BladeJSError |

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

#### Example

```kotlin
Blade.getCoinPrice(
    search = "hbar",
    currency = "uah"
) { result, bladeJSError ->
    println("${result ?: bladeJSError}")
}
```

## transferHbars

Method to execute Hbar transfers from current account to receiver

`transferHbars (accountId: String, accountPrivateKey: String, receiverId: String, amount: Double, memo: String, completion: (TransactionReceiptData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `accountId` | `String` | sender account id |
| `accountPrivateKey` | `String` | sender's private key to sign transfer transaction |
| `receiverId` | `String` | receiver |
| `amount` | `Double` | amount |
| `memo` | `String` | memo (limited to 100 characters) |
| `completion` | `(TransactionReceiptData?,BladeJSError?)->Unit` | callback function, with result of TransactionReceiptData or BladeJSError |

#### Example

```kotlin
val senderId = "0.0.10001"
val senderKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
val receiverId = "0.0.10002"
val amount = 2.5
Blade.transferHbars(
    senderId,
    senderKey,
    receiverId,
    amount,
    "Some memo text"
) { result, error ->
    println(result ?: error)
}
```

## transferTokens

Method to execute token transfers from current account to receiver

`transferTokens (tokenId: String, accountId: String, accountPrivateKey: String, receiverId: String, amountOrSerial: Double, memo: String, usePaymaster: Boolean = true, completion: (TransactionReceiptData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `tokenId` | `String` | token id to send (0.0.xxxxx) |
| `accountId` | `String` | sender account id (0.0.xxxxx) |
| `accountPrivateKey` | `String` | sender's hex-encoded private key with DER-header (302e020100300506032b657004220420...). ECDSA or Ed25519 |
| `receiverId` | `String` | receiver account id (0.0.xxxxx) |
| `amountOrSerial` | `Double` | amount of fungible tokens to send (with token-decimals correction) on NFT serial number |
| `memo` | `String` | transaction memo (limited to 100 characters) |
| `usePaymaster` | `Boolean` | if true, Paymaster account will pay fee transaction. Only for single dApp configured fungible-token. In that case tokenId not used |
| `completion` | `(TransactionReceiptData?,BladeJSError?)->Unit` | callback function, with result of TransactionReceiptData or BladeJSError |

#### Example

```kotlin
val tokenId = "0.0.1337"
val senderId = "0.0.10001"
val senderKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
val receiverId = "0.0.10002"
val amount = 2.5
Blade.transferTokens(
    tokenId,
    senderId,
    senderKey,
    receiverId,
    amount,
    "Token transfer memo"
) { result, error ->
    println(result ?: error)
}
```

## createScheduleTransaction

Create scheduled transaction

`createScheduleTransaction ( accountId: String, accountPrivateKey: String, type: ScheduleTransactionType, transfers: List<ScheduleTransactionTransfer>, usePaymaster: Boolean = false, completion: (CreateScheduleData?, BladeJSError?) -> Unit )`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `accountId` | `String` | account id (0.0.xxxxx) |
| `accountPrivateKey` | `String` | optional field if you need specify account key (hex encoded privateKey with DER-prefix) |
| `type` | `ScheduleTransactionType` | schedule transaction type (currently only TRANSFER supported) |
| `transfers` | `List<ScheduleTransactionTransfer>` | array of transfers to schedule (HBAR, FT, NFT) |
| `usePaymaster` | `Boolean` | if true, Paymaster account will pay transaction fee (also dApp had to be configured for free schedules) |
| `completion` | `(CreateScheduleData?,BladeJSError?)->Unit` | callback function, with result of CreateScheduleData or BladeJSError |

#### Example

```kotlin
val receiverId = "0.0.10002"
val receiverKey = "302d300706052b8104000a032200029dc73991b00002..."
val senderId = "0.0.10001"
val tokenId = "0.0.1337"
var scheduleId = ""
Blade.createScheduleTransaction(
    accountId = receiverId,
    accountPrivateKey = receiverKey,
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

`signScheduleId ( scheduleId: String, accountId: String, accountPrivateKey: String, receiverAccountId: String = "", usePaymaster: Boolean = false, completion: (TransactionReceiptData?, BladeJSError?) -> Unit )`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `scheduleId` | `String` | scheduled transaction id (0.0.xxxxx) |
| `accountId` | `String` | account id (0.0.xxxxx) |
| `accountPrivateKey` | `String` | optional field if you need specify account key (hex encoded privateKey with DER-prefix) |
| `receiverAccountId` | `String` | account id of receiver for additional validation in case of dApp freeSchedule transactions configured |
| `usePaymaster` | `Boolean` | if true, Paymaster account will pay transaction fee (also dApp had to be configured for free schedules) |
| `completion` | `(TransactionReceiptData?,BladeJSError?)->Unit` | callback function, with result of TransactionReceiptData or BladeJSError |

#### Example

```kotlin
val senderId = "0.0.10001"
val senderKey = "302d300706052b8104000a032200029dc73991b00001..."
val receiverId = "0.0.10002"
var scheduleId = "0.0...." // result of createScheduleTransaction on receiver side
Blade.signScheduleId(
    scheduleId = scheduleId,
    accountId = senderId,
    accountPrivateKey = senderKey,
    receiverAccountId = receiverId,
    usePaymaster = true
) { result, bladeJSError ->
    println(result ?: bladeJSError)
}
```

## createHederaAccount

Create new Hedera account (ECDSA). Only for configured dApps. Depending on dApp config Blade create account, associate tokens, etc.

* In case of not using pre-created accounts pool and network high load, this method can return transactionId and no accountId.

* In that case account creation added to queue, and you should wait some time and call `getPendingAccount()` method.

`createHederaAccount (privateKey: String = "", deviceId: String = "", completion: (CreatedAccountData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `privateKey` | `String` | optional field if you need specify account key (hex encoded privateKey with DER-prefix) |
| `deviceId` | `String` | optional field unique device id (advanced security feature, required only for some dApps) |
| `completion` | `(CreatedAccountData?,BladeJSError?)->Unit` | callback function, with result of CreatedAccountData or BladeJSError |

#### Example

```kotlin
Blade.createHederaAccount() { result, error ->
    println(result ?: error)
}
```

## getPendingAccount

Get account from queue (read more at `createAccount()`).

* If account already created, return account data.

* If account not created yet, response will be same as in `createAccount()` method if account in queue.

`getPendingAccount (transactionId: String, seedPhrase: String, completion: (CreatedAccountData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `transactionId` | `String` | can be received on createHederaAccount method, when busy network is busy, and account creation added to queue |
| `seedPhrase` | `String` | returned from createHederaAccount method, required for updating keys and proper response |
| `completion` | `(CreatedAccountData?,BladeJSError?)->Unit` | callback function, with result of CreatedAccountData or BladeJSError |


## deleteHederaAccount

Delete Hedera account. This method requires account private key and operator private key. Operator is the one who paying fees

`deleteHederaAccount (deleteAccountId: String, deletePrivateKey: String, transferAccountId: String, operatorAccountId: String, operatorPrivateKey: String, completion: (TransactionReceiptData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `deleteAccountId` | `String` | account id of account to delete (0.0.xxxxx) |
| `deletePrivateKey` | `String` | account private key (DER encoded hex string) |
| `transferAccountId` | `String` | The ID of the account to transfer the remaining funds to. |
| `operatorAccountId` | `String` | operator account id (0.0.xxxxx). Used for fee |
| `operatorPrivateKey` | `String` | operator's account private key (DER encoded hex string) |
| `completion` | `(TransactionReceiptData?,BladeJSError?)->Unit` | callback function, with result of TransactionReceiptData or BladeJSError |

#### Example

```kotlin
val deleteAccountId = "0.0.65468464"
val deletePrivateKey = "3030020100300706052b8104000a04220420ebc..."
val transferAccountId = "0.0.10001"
val operatorAccountId = "0.0.10002"
val operatorPrivateKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
Blade.deleteHederaAccount(
    deleteAccountId,
    deletePrivateKey,
    transferAccountId,
    operatorAccountId,
    operatorPrivateKey,
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

#### Example

```kotlin
Blade.getAccountInfo("0.0.10002") { accountInfoData, error ->
    println(accountInfoData ?: error)
}
```

## getNodeList

Get Node list and use it for choosing account stacking node

`getNodeList (completion: (NodesData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `completion` | `(NodesData?,BladeJSError?)->Unit` | callback function, with result of NodesData or BladeJSError |

#### Example

```kotlin
Blade.getNodeList { nodeListData, error ->
    println(nodeListData ?: error)
}
```

## stakeToNode

Stake/unstake account

`stakeToNode (accountId: String, accountPrivateKey: String, nodeId: Int, completion: (TransactionReceiptData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `accountId` | `String` | Hedera account id (0.0.xxxxx) |
| `accountPrivateKey` | `String` | account private key (DER encoded hex string) |
| `nodeId` | `Int` | node id to stake to. If negative or null, account will be unstaked |
| `completion` | `(TransactionReceiptData?,BladeJSError?)->Unit` | callback function, with result of TransactionReceiptData or BladeJSError |

#### Example

```kotlin
Blade.stakeToNode("0.0.10002", "302d300706052b8104000a032200029dc73991b0d9cd...", 5) { result, error ->
    println(result ?: error)
}
```

## getKeysFromMnemonic

Get private key and accountId from mnemonic. Supported standard and legacy key derivation.

* If account not found, standard ECDSA key will be returned.

* Keys returned with DER header. EvmAddress computed from Public key.

`getKeysFromMnemonic (mnemonic: String, lookupNames: Boolean = false, completion: (PrivateKeyData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `mnemonic` | `String` | seed phrase (BIP39 mnemonic) |
| `lookupNames` | `Boolean` | lookup for accounts (not used anymore, account search is mandatory) |
| `completion` | `(PrivateKeyData?,BladeJSError?)->Unit` | callback function, with result of PrivateKeyData or BladeJSError |

#### Example

```kotlin
Blade.getKeysFromMnemonic("purity slab doctor swamp tackle rebuild summer bean craft toddler blouse switch") { result, error ->
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

#### Example

```kotlin
Blade.searchAccounts("purity slab doctor swamp tackle rebuild summer bean craft toddler blouse switch") { result, error ->
    println(result ?: error)
}
```

## dropTokens

Bladelink drop to account

`dropTokens (accountId: String, accountPrivateKey: String, secretNonce: String, completion: (TokenDropData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `accountId` | `String` | Hedera account id (0.0.xxxxx) |
| `accountPrivateKey` | `String` | account private key (DER encoded hex string) |
| `secretNonce` | `String` | configured for dApp. Should be kept in secret |
| `completion` | `(TokenDropData?,BladeJSError?)->Unit` | callback function, with result of TokenDropData or BladeJSError |

#### Example

```kotlin
val accountId = "0.0.10002"
val accountPrivateKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
val secretNonce = "[ CENSORED ]"
Blade.dropTokens(
    accountId,
    accountPrivateKey,
    secretNonce,
) { result, error ->
    println(result ?: error)
}
```

## sign

Sign base64-encoded message with private key. Returns hex-encoded signature.

`sign (messageString: String, privateKey: String, completion: (SignMessageData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `messageString` | `String` | base64-encoded message to sign |
| `privateKey` | `String` | hex-encoded private key with DER header |
| `completion` | `(SignMessageData?,BladeJSError?)->Unit` | callback function, with result of SignMessageData or BladeJSError |

#### Example

```kotlin
import java.util.Base64
// ...
val originalString = "hello"
val encodedString: String = Base64.getEncoder().encodeToString(originalString.toByteArray())
val accountPrivateKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
Blade.sign(
    encodedString,
    accountPrivateKey
) { result, error ->
    println(result ?: error)
}
```

## signVerify

Verify message signature with public key

`signVerify (messageString: String, signature: String, publicKey: String, completion: (SignVerifyMessageData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `messageString` | `String` | base64-encoded message (same as provided to `sign()` method) |
| `signature` | `String` | hex-encoded signature (result from `sign()` method) |
| `publicKey` | `String` | hex-encoded public key with DER header |
| `completion` | `(SignVerifyMessageData?,BladeJSError?)->Unit` | callback function, with result of SignVerifyMessageData or BladeJSError |

#### Example

```kotlin
val originalString = "hello"
val encodedString: String = Base64.getEncoder().encodeToString(originalString.toByteArray())
val signature = "27cb9d51434cf1e76d7ac515b19442c619f641e6fccddbf4a3756b14466becb6992dc1d2a82268018147141fc8d66ff9ade43b7f78c176d070a66372d655f942"
val publicKey = "302d300706052b8104000a032200029dc73991b0d9cdbb59b2cd0a97a0eaff6de801726cb39804ea9461df6be2dd30"
Blade.signVerify(
    encodedString,
    signature,
    publicKey
) { result, error ->
    lifecycleScope.launch {
        println(result ?: error)
    }
}
```

## contractCallFunction

Call contract function. Directly or via BladeAPI using paymaster account (fee will be paid by Paymaster account), depending on your dApp configuration.

`contractCallFunction (contractId: String, functionName: String, params: ContractFunctionParameters, accountId: String, accountPrivateKey: String, gas: Int = 100000, usePaymaster: Boolean, completion: (TransactionReceiptData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `contractId` | `String` | contract id (0.0.xxxxx) |
| `functionName` | `String` | name of the contract function to call |
| `params` | `ContractFunctionParameters` | function argument. Can be generated with |
| `accountId` | `String` | operator account id (0.0.xxxxx) |
| `accountPrivateKey` | `String` | operator's hex-encoded private key with DER-header, ECDSA or Ed25519 |
| `gas` | `Int` | gas limit for the transaction |
| `usePaymaster` | `Boolean` | if true, fee will be paid by Paymaster account (note: msg.sender inside the contract will be Paymaster account) |
| `completion` | `(TransactionReceiptData?,BladeJSError?)->Unit` | callback function, with result of TransactionReceiptData or BladeJSError |

#### Example

```kotlin
val contractId = "0.0.123456"
val functionName = "set_message"
val parameters = ContractFunctionParameters().addString("hello")
val accountId = "0.0.10002"
val accountPrivateKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
val gas = 155000
val usePaymaster = false
Blade.contractCallFunction(
    contractId,
    functionName,
    parameters,
    accountId,
    accountPrivateKey,
    gas,
    usePaymaster
) { result, error ->
    println(result ?: error)
}
```

## contractCallQueryFunction

Call query on contract function. Similar to {@link contractCallFunction} can be called directly or via BladeAPI using Paymaster account.

`contractCallQueryFunction (contractId: String, functionName: String, params: ContractFunctionParameters, accountId: String, accountPrivateKey: String, gas: Int = 100000, usePaymaster: Boolean, returnTypes: List<String>, completion: (ContractQueryData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `contractId` | `String` | contract id (0.0.xxxxx) |
| `functionName` | `String` | name of the contract function to call |
| `params` | `ContractFunctionParameters` | function argument. Can be generated with |
| `accountId` | `String` | operator account id (0.0.xxxxx) |
| `accountPrivateKey` | `String` | operator's hex-encoded private key with DER-header, ECDSA or Ed25519 |
| `gas` | `Int` | gas limit for the transaction |
| `usePaymaster` | `Boolean` | if true, the fee will be paid by paymaster account (note: msg.sender inside the contract will be Paymaster account) |
| `returnTypes` | `List<String>` | List of return types, e.g. listOf("string", "int32") |
| `completion` | `(ContractQueryData?,BladeJSError?)->Unit` | callback function, with result of ContractQueryData or BladeJSError |

#### Example

```kotlin
val contractId = "0.0.123456"
val functionName = "get_message"
val parameters = ContractFunctionParameters()
val accountId = "0.0.10002"
val accountPrivateKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
val gas = 55000
val usePaymaster = false
val returnTypes = listOf("string", "int32")
Blade.contractCallQueryFunction(
    contractId,
    functionName,
    parameters,
    accountId,
    accountPrivateKey,
    gas,
    usePaymaster,
    returnTypes
) { result, error ->
    lifecycleScope.launch {
        println(result ?: error)
    }
}
```

## ethersSign

Sign base64-encoded message with private key using ethers lib. Returns hex-encoded signature.

`ethersSign (messageString: String, privateKey: String, completion: (SignMessageData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `messageString` | `String` | base64-encoded message to sign |
| `privateKey` | `String` | hex-encoded private key with DER header |
| `completion` | `(SignMessageData?,BladeJSError?)->Unit` | callback function, with result of SignMessageData or BladeJSError |

#### Example

```kotlin
import java.util.Base64
// ...
val originalString = "hello"
val encodedString: String = Base64.getEncoder().encodeToString(originalString.toByteArray())
val accountPrivateKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
Blade.ethersSign(
    encodedString,
    accountPrivateKey
) { result, error ->
    println(result ?: error)
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

`getParamsSignature (params: ContractFunctionParameters, accountPrivateKey: String, completion: (SplitSignatureData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `params` | `ContractFunctionParameters` | data to sign. (instance of ContractFunctionParameters) |
| `accountPrivateKey` | `String` | signer private key (hex-encoded with DER header) |
| `completion` | `(SplitSignatureData?,BladeJSError?)->Unit` | callback function, with result of SplitSignatureData or BladeJSError |

#### Example

```kotlin
val parameters = ContractFunctionParameters().addString("hello")
val accountPrivateKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
Blade.getParamsSignature(
    parameters,
    accountPrivateKey
) { result, error ->
    println(result ?: error)
}
```

## getTransactions

Get transactions history for account. Can be filtered by transaction type.

* Transaction requested from mirror node. Every transaction requested for child transactions. Result are flattened.

* If transaction type is not provided, all transactions will be returned.

* If transaction type is CRYPTOTRANSFERTOKEN records will additionally contain plainData field with decoded data.

`getTransactions (accountId: String, transactionType: String, nextPage: String = "", transactionsLimit: Int = 10, completion: (TransactionsHistoryData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `accountId` | `String` | account id to get transactions for (0.0.xxxxx) |
| `transactionType` | `String` | one of enum MirrorNodeTransactionType or "CRYPTOTRANSFERTOKEN" |
| `nextPage` | `String` | link to next page of transactions from previous request |
| `transactionsLimit` | `Int` | number of transactions to return. Speed of request depends on this value if transactionType is set. |
| `completion` | `(TransactionsHistoryData?,BladeJSError?)->Unit` | callback function, with result of TransactionsHistoryData or BladeJSError |

#### Example

```kotlin
Blade.getTransactions(
    accountId = "0.0.10002",
    transactionType = "",
    nextPage = "",
    transactionsLimit = 15
) { result, error ->
    println(result ?: error)
}
```

## getC14url

Get configured url for C14 integration (iframe or popup)

`getC14url (asset: String, account: String, amount: String = "", completion: (IntegrationUrlData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `asset` | `String` | USDC, HBAR, KARATE or C14 asset uuid |
| `account` | `String` | receiver account id (0.0.xxxxx) |
| `amount` | `String` | preset amount. May be overwritten if out of range (min/max) |
| `completion` | `(IntegrationUrlData?,BladeJSError?)->Unit` | callback function, with result of IntegrationUrlData or BladeJSError |

#### Example

```kotlin
Blade.getC14url(
    asset = "HBAR",
    account = "0.0.10002",
    amount = "120"
) { result, error ->
    println(result ?: error)
}
```

## exchangeGetQuotes

Get quotes from different services for buy, sell or swap

`exchangeGetQuotes (sourceCode: String, sourceAmount: Double, targetCode: String, strategy: CryptoFlowServiceStrategy, completion: (SwapQuotesData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `sourceCode` | `String` | name (HBAR, KARATE, other token code) |
| `sourceAmount` | `Double` | amount to swap, buy or sell |
| `targetCode` | `String` | name (HBAR, KARATE, USDC, other token code) |
| `strategy` | `CryptoFlowServiceStrategy` | one of enum CryptoFlowServiceStrategy (Buy, Sell, Swap) |
| `completion` | `(SwapQuotesData?,BladeJSError?)->Unit` | callback function, with result of SwapQuotesData or BladeJSError |

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

`getTradeUrl (strategy: CryptoFlowServiceStrategy, accountId: String, sourceCode: String, sourceAmount: Double, targetCode: String, slippage: Double, serviceId: String, redirectUrl: String = "", completion: (IntegrationUrlData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `strategy` | `CryptoFlowServiceStrategy` | Buy / Sell |
| `accountId` | `String` | account id |
| `sourceCode` | `String` | name (HBAR, KARATE, USDC, other token code) |
| `sourceAmount` | `Double` | amount to buy/sell |
| `targetCode` | `String` | name (HBAR, KARATE, USDC, other token code) |
| `slippage` | `Double` | slippage in percents. Transaction will revert if the price changes unfavorably by more than this percentage. |
| `serviceId` | `String` | service id to use for swap (saucerswap, onmeta, etc) |
| `redirectUrl` | `String` | url to redirect after final step |
| `completion` | `(IntegrationUrlData?,BladeJSError?)->Unit` | callback function, with result of IntegrationUrlData or BladeJSError |

#### Example

```kotlin
Blade.getTradeUrl(
    strategy = CryptoFlowServiceStrategy.BUY,
    accountId = "0.0.10002",
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

`swapTokens (accountId: String, accountPrivateKey: String, sourceCode: String, sourceAmount: Double, targetCode: String, slippage: Double, serviceId: String, completion: (ResultData?, BladeJSError?) -> Unit)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `accountId` | `String` | account id |
| `accountPrivateKey` | `String` | account private key |
| `sourceCode` | `String` | name (HBAR, KARATE, other token code) |
| `sourceAmount` | `Double` | amount to swap |
| `targetCode` | `String` | name (HBAR, KARATE, other token code) |
| `slippage` | `Double` | slippage in percents. Transaction will revert if the price changes unfavorably by more than this percentage. |
| `serviceId` | `String` | service id to use for swap (saucerswap, etc) |
| `completion` | `(ResultData?,BladeJSError?)->Unit` | callback function, with result of ResultData or BladeJSError |

#### Example

```kotlin
val accountId = "0.0.10001"
val accountPrivateKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
val sourceCode = "USDC"
val targetCode = "KARATE"
Blade.swapTokens(
    accountId,
    accountPrivateKey,
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

`createToken ( treasuryAccountId: String, supplyPrivateKey: String, tokenName: String, tokenSymbol: String, isNft: Boolean, keys: List<KeyRecord>, decimals: Int, initialSupply: Int, maxSupply: Int, completion: (CreateTokenData?, BladeJSError?) -> Unit )`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `treasuryAccountId` | `String` | treasury account id |
| `supplyPrivateKey` | `String` | supply account private key |
| `tokenName` | `String` | token name (string up to 100 bytes) |
| `tokenSymbol` | `String` | token symbol (string up to 100 bytes) |
| `isNft` | `Boolean` | set token type NFT |
| `keys` | `List<KeyRecord>` | token keys |
| `decimals` | `Int` | token decimals (0 for nft) |
| `initialSupply` | `Int` | token initial supply (0 for nft) |
| `maxSupply` | `Int` | token max supply |
| `completion` | `(CreateTokenData?,BladeJSError?)->Unit` | callback function, with result of CreateTokenData or BladeJSError |

#### Example

```kotlin
val keys = listOf(
    KeyRecord(Config.adminPrivateKey, KeyType.admin)
)
Blade.createToken(
        treasuryAccountId = Config.accountId,
        supplyPrivateKey = Config.privateKey,
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

Associate token to account. Association fee will be covered by PayMaster, if tokenId configured in dApp

`associateToken ( tokenId: String, accountId: String, accountPrivateKey: String, completion: (TransactionReceiptData?, BladeJSError?) -> Unit )`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `tokenId` | `String` | token id to associate. Empty to associate all tokens configured in dApp |
| `accountId` | `String` | account id to associate token |
| `accountPrivateKey` | `String` | account private key |
| `completion` | `(TransactionReceiptData?,BladeJSError?)->Unit` | callback function, with result of TransactionReceiptData or BladeJSError |

#### Example

```kotlin
Blade.associateToken(
    tokenId = "0.0.1337",
    accountId = "0.0.10001",
    accountPrivateKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
) { result, error ->
    println(result ?: error)
}
```

## nftMint

Mint one NFT

`nftMint ( tokenId: String, supplyAccountId: String, supplyPrivateKey: String, file: String, metadata: Map<String, Any>, storageConfig: NFTStorageConfig, completion: (TransactionReceiptData?, BladeJSError?) -> Unit )`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `tokenId` | `String` | token id to mint NFT |
| `supplyAccountId` | `String` | token supply account id |
| `supplyPrivateKey` | `String` | token supply private key |
| `file` | `String` | image to mint (base64 DataUrl image, eg.: data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAA...) |
| `metadata` | `Map<String,Any>` | NFT metadata |
| `storageConfig` | `NFTStorageConfig` | IPFS provider config |
| `completion` | `(TransactionReceiptData?,BladeJSError?)->Unit` | callback function, with result of CreateTokenData or BladeJSError |

#### Example

```kotlin
val tokenId = "0.0.13377"
val supplyAccountId = "0.0.10001"
val supplyPrivateKey = "302d300706052b8104000a032200029dc73991b0d9cd..."
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
    tokenId,
    supplyAccountId,
    supplyPrivateKey,
    file = base64Image,
    metaData,
    storageConfig,
) { result, error ->
    println(result ?: error)
}
```

## cleanup

Method to clean-up webView



`cleanup ()`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |


## postMessage

Method to handle JS responses. By technical reasons, must be public, but you can skip it :)



`postMessage (jsonString: String)`

#### Parameters

| Name | Type | Description |
|------|------| ----------- |
| `jsonString` | `String` |  |


