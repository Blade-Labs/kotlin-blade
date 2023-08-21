# JS wrapper response types

The Result interface is a generic interface that defines two properties:

* completionKey: A String that represents a unique identifier for the result.
* data: A generic type T that represents the actual data of the result. This interface is implemented by various classes in the io.bladewallet.bladesdk package, which represent the different types of responses that can be returned by the Blade API. The completionKey property is used to correlate the response with the original request, and the data property contains the actual data returned by the API.

```kotlin
interface Result<T>{
    var completionKey: String
    var data: T
}
```

This is a data class definition for BladeJSError with two properties: name and reason.
In this case, BladeJSError has two properties: name and reason.
The purpose of this class is to hold information about an error that occurred in the context of BladeJS, with the name property indicating the type of the error and the reason property providing more detailed information about what went wrong.

```kotlin
data class BladeJSError(
    var name: String,
    var reason: String
)   
```


The ContractFunctionParameter data class represents a parameter of a contract function. It has two properties:

* type: A String representing the data type of the parameter.
* value: A List of Strings representing the value(s) of the parameter.

```kotlin
data class ContractFunctionParameter (
    var type: String,
    var value: List<String>
)
```


```kotlin
data class Response (
    override var completionKey: String,
    override var data: Any,
    var error: BladeJSError?,
) : Result<Any>
```

```kotlin
data class InfoData(
    var apiKey: String,
    var dAppCode: String,
    var network: String,
    var visitorId: String,
    var sdkEnvironment: String,
    var sdkVersion: String,
    var nonce: Int
)
```

```kotlin
data class BalanceData(
    var hbars: Double,
    var tokens: List<BalanceDataToken>
)
```

```kotlin
data class BalanceDataToken(
    var balance: Double,
    var tokenId: String
)
```

```kotlin
data class AccountInfoData(
    var accountId: String,
    var evmAddress: String,
    var calculatedEvmAddress: String
)
```

```kotlin
data class TransferData(
    var nodeId: String,
    var transactionHash: String,
    var transactionId: String,
)
```

```kotlin
data class CreatedAccountData(
    var seedPhrase: String,
    var publicKey: String,
    var privateKey: String,
    var accountId: String?,
    var evmAddress: String,
    var transactionId: String?,
    var status: String,
    var queueNumber: Int?
)
```

```kotlin
data class TransactionReceiptData(
    var status: String,
    var contractId: String?,
    var topicSequenceNumber: String?,
    var totalSupply: String?,
    var serials: List<String>?
)
```

```kotlin
data class ContractQueryData(
    var gasUsed: Int,
    var values: List<ContractQueryRecord>
)
```

```kotlin
data class ContractQueryRecord(
    var type: String,
    var value: String
)
```

```kotlin
data class PrivateKeyData(
    var privateKey: String,
    var publicKey: String,
    var accounts: List<String>,
    var evmAddress: String
)
```

```kotlin
data class SignMessageData(
    var signedMessage: String
)
```

```kotlin
data class SignVerifyMessageData(
    var valid: Boolean
)
```

```kotlin
data class SplitSignatureData(
    var v: Int,
    var r: String,
    var s: String,
)
```

```kotlin
data class TransactionsHistoryData(
    var nextPage: String?,
    var transactions: List<TransactionHistoryDetail>
)
```

```kotlin
data class TransactionHistoryDetail(
    var fee: Int,
    var memo: String,
    var nftTransfers: List<TransactionHistoryNftTransfer>?,
    var time: String,
    var transactionId: String,
    var transfers: List<TransactionHistoryTransfer>,
    var type: String,
    var plainData: TransactionHistoryPlainData?,
    var consensusTimestamp: String
)
```

```kotlin
data class TransactionHistoryPlainData(
    var type: String,
    var token_id: String,
    var amount: Double,
    var senders: List<String>,
    var receivers: List<String>
)
```

```kotlin
data class TransactionHistoryTransfer(
    var account: String,
    var amount: Double,
    var is_approval: Boolean
)
```

```kotlin
data class TransactionHistoryNftTransfer(
    var is_approval: Boolean,
    var receiver_account_id: String,
    var sender_account_id: String,
    var serial_number: Int,
    var token_id: String
)
```

```kotlin
data class IntegrationUrlData(
    var url: String?,
)
```

```kotlin
data class RemoteConfig(
    var fpApiKey: String
)
```

```kotlin
enum class BladeEnv(val value: String) {
    Prod("Prod"),
    CI("CI")
}
```
