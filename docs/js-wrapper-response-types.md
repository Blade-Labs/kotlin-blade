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
    var calculatedEvmAddress: String,
    var publicKey: String,
    var stakingInfo: StakingInfo
)
```

```kotlin
data class StakingInfo(
    val pendingReward: Long,
    val stakedNodeId: Int?,
    val stakePeriodStart: String?
)
```

```kotlin
data class NodesResponse(
    override var completionKey: String,
    override var data: NodesData
): Result<NodesData>
```

```kotlin
data class NodesData(
    var nodes: List<NodeInfo>,
)

data class NodeInfo(
    var description: String,
    var max_stake: Long,
    var min_stake: Long,
    var node_id: Int,
    var node_account_id: String,
    var stake: Long,
    var stake_not_rewarded: Long,
    var stake_rewarded: Long,
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
data class AccountPrivateData(
    var accounts: List<AccountPrivateRecord>
)
```

```kotlin
data class AccountPrivateRecord(
    var privateKey: String,
    var publicKey: String,
    var evmAddress: String,
    var address: String,
    var path: String,
    val keyType: CryptoKeyType
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
    var fee: Double,
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
data class SwapQuotesData(
    var quotes: List<ICryptoFlowQuote>
)
```

```kotlin
data class ICryptoFlowQuote(
    var service: ICryptoFlowQuoteService,
    var source: IAssetQuote,
    var target: IAssetQuote,
    var rate: Double?,
    var widgetUrl: String?,
    var paymentMethods: List<String>?
)
```

```kotlin
data class ICryptoFlowQuoteService(
    var id: String,
    var name: String,
    var logo: String,
    var description: String?
)
```

```kotlin
data class IAssetQuote(
    var asset: ICryptoFlowAsset,
    var amountExpected: Double,
    var totalFee: Double?,
)
```

```kotlin
data class ICryptoFlowAsset(
    var name: String,
    var code: String,
    var type: String,
    // crypto only
    var address: String?,
    var chainId: Int?,
    var decimals: Int?,
    var minAmount: Double?,
    var maxAmount: Double?,
    // fiat only
    var symbol: String?,
    // both
    var imageUrl: String?,
)
```

```kotlin
data class ResultData(
    var success: Boolean
)
```

```kotlin
data class CreateTokenData(
    var tokenId: String
)
```

```kotlin
data class RemoteConfig(
    var fpApiKey: String
)
```

```kotlin
data class CoinListResponse(
    override var completionKey: String,
    override var data: CoinListData
) : Result<CoinListData>
```

```kotlin
data class CoinListData(
    var coins: List<CoinItem>
)
```

```kotlin
data class CoinItem (
    var id: String,
    var symbol: String,
    var name: String,
    var platforms: List<CoinGeckoPlatform>
)
```

```kotlin
data class CoinGeckoPlatform (
    var name: String,
    var address: String
)
```

```kotlin
data class CoinInfoResponse(
    override var completionKey: String,
    override var data: CoinInfoData
) : Result<CoinInfoData>
```

```kotlin
data class CoinInfoData(
    var coin: CoinData,
    var priceUsd: Double,
    var price: Double?,
    var currency: String
)
```

```kotlin
data class CoinData(
    var id: String,
    var symbol: String,
    var name: String,
    var web_slug: String,
    var description: CoinDataDescription,
    var image: CoinDataImage,
    var market_data: CoinDataMarket,
    var platforms: List<CoinGeckoPlatform>
)
```

```kotlin
data class CoinDataDescription(
    val en: String
)
```

```kotlin
data class CoinDataImage(
    val thumb: String,
    val small: String,
    val large: String
)
```

```kotlin
data class CoinDataMarket(
    val currentPrice: Map<String, Double>
)
```

```kotlin
data class KeyRecord(
    val privateKey: String,
    val type: KeyType
)
```

```kotlin
data class NFTStorageConfig(
    val provider: NFTStorageProvider,
    val apiKey: String
)
```

```kotlin
enum class NFTStorageProvider(val value: String) {
    nftStorage("nftStorage");

    companion object {
        fun fromValue(value: String): NFTStorageProvider? {
            return NFTStorageProvider.values().find { it.value == value }
        }
    }
}
```

```kotlin
enum class KeyType(val value: String) {
    admin("admin"),
    kyc("kyc"),
    freeze("freeze"),
    wipe("wipe"),
    pause("pause"),
    feeSchedule("feeSchedule");

    companion object {
        fun fromValue(value: String): KeyType? {
            return KeyType.values().find { it.value == value }
        }
    }
}
```

```kotlin
enum class BladeEnv(val value: String) {
    Prod("Prod"),
    CI("CI")
}
```

```kotlin
enum class CryptoFlowServiceStrategy(val value: String) {
    BUY("Buy"),
    SELL("Sell"),
    SWAP("Swap");

    companion object {
        fun fromValue(value: String): CryptoFlowServiceStrategy? {
            return values().find { it.value == value }
        }
    }
}
```

```kotlin
enum class CryptoKeyType(val value: String) {
    ECDSA_SECP256K1("ECDSA_SECP256K1"),
    ED25519("ED25519");

    companion object {
        fun fromValue(value: String): CryptoKeyType? {
            return CryptoKeyType.values().find { it.value == value }
        }
    }
}
```