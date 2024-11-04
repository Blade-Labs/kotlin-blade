# Response types

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

# ENUMs


### BladeEnv

```kotlin
enum class BladeEnv(val value: String) {
    Prod("Prod"),
    CI("CI")
}
```


### CryptoFlowServiceStrategy

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


### CryptoKeyType

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


### IPFSProvider

```kotlin
enum class IPFSProvider(val value: String) {
    pinata("pinata");

    companion object {
        fun fromValue(value: String): IPFSProvider? {
            return IPFSProvider.values().find { it.value == value }
        }
    }
}
```


### KeyType

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


### ScheduleTransactionType

```kotlin
enum class ScheduleTransactionType(val value: String) {
    TRANSFER("TRANSFER");
    // SUBMIT_MESSAGE("SUBMIT_MESSAGE"),
    // APPROVE_ALLOWANCE("APPROVE_ALLOWANCE"),
    // TOKEN_MINT("TOKEN_MINT"),
    // TOKEN_BURN("TOKEN_BURN");
}
```


### ScheduleTransferType

```kotlin
enum class ScheduleTransferType(val value: String) {
    HBAR("HBAR"),
    FT("FT"),
    NFT("NFT")
}
```



# Data types


### AccountInfoData

```kotlin
data class AccountInfoData(
    var accountId: String,
    var evmAddress: String,
    var calculatedEvmAddress: String,
    var publicKey: String,
    var stakingInfo: StakingInfo
)
```


### AccountInfoResponse

```kotlin
data class AccountInfoResponse(
    override var completionKey: String,
    override var data: AccountInfoData
): Result<AccountInfoData>
```


### AccountPrivateData

```kotlin
data class AccountPrivateData(
    var accounts: List<AccountPrivateRecord>
)
```


### AccountPrivateRecord

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


### AccountPrivateResponse

```kotlin
data class AccountPrivateResponse(
    override var completionKey: String,
    override var data: AccountPrivateData
): Result<AccountPrivateData>
```


### BalanceData

```kotlin
data class BalanceData(
    var hbars: Double,
    var tokens: List<BalanceDataToken>
)
```


### BalanceDataToken

```kotlin
data class BalanceDataToken(
    var balance: Double,
    var tokenId: String
)
```


### BalanceResponse

```kotlin
data class BalanceResponse(
    override var completionKey: String,
    override var data: BalanceData
) : Result<BalanceData>
```


### BladeJSError

```kotlin
data class BladeJSError(
    var name: String,
    var reason: String
) : Throwable()
```


### CoinData

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


### CoinDataDescription

```kotlin
data class CoinDataDescription(
    val en: String
)
```


### CoinDataImage

```kotlin
data class CoinDataImage(
    val thumb: String,
    val small: String,
    val large: String
)
```


### CoinDataMarket

```kotlin
data class CoinDataMarket(
    val current_price: Map<String, Double>
)
```


### CoinGeckoPlatform

```kotlin
data class CoinGeckoPlatform (
    var name: String,
    var address: String
)
```


### CoinInfoData

```kotlin
data class CoinInfoData(
    var coin: CoinData,
    var priceUsd: Double,
    var price: Double?,
    var currency: String
)
```


### CoinInfoResponse

```kotlin
data class CoinInfoResponse(
    override var completionKey: String,
    override var data: CoinInfoData
) : Result<CoinInfoData>
```


### CoinItem

```kotlin
data class CoinItem (
    var id: String,
    var symbol: String,
    var name: String,
    var platforms: List<CoinGeckoPlatform>
)
```


### CoinListData

```kotlin
data class CoinListData(
    var coins: List<CoinItem>
)
```


### CoinListResponse

```kotlin
data class CoinListResponse(
    override var completionKey: String,
    override var data: CoinListData
) : Result<CoinListData>
```


### ContractFunctionParameter

```kotlin
data class ContractFunctionParameter (
    var type: String,
    var value: List<String>
)
```


### ContractQueryData

```kotlin
data class ContractQueryData(
    var gasUsed: Int,
    var values: List<ContractQueryRecord>
)
```


### ContractQueryRecord

```kotlin
data class ContractQueryRecord(
    var type: String,
    var value: String
)
```


### ContractQueryResponse

```kotlin
data class ContractQueryResponse(
    override var completionKey: String,
    override var data: ContractQueryData
): Result<ContractQueryData>
```


### CreatedAccountData

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


### CreatedAccountResponse

```kotlin
data class CreatedAccountResponse(
    override var completionKey: String,
    override var data: CreatedAccountData
): Result<CreatedAccountData>
```


### CreateScheduleData

```kotlin
data class CreateScheduleData(
    var scheduleId: String
)
```


### CreateScheduleResponse

```kotlin
data class CreateScheduleResponse(
    override var completionKey: String,
    override var data: CreateScheduleData
) : Result<CreateScheduleData>
```


### CreateTokenData

```kotlin
data class CreateTokenData(
    var tokenId: String
)
```


### CreateTokenResponse

```kotlin
data class CreateTokenResponse(
    override var completionKey: String,
    override var data: CreateTokenData
) : Result<CreateTokenData>
```


### EmergencyTransferData

```kotlin
data class EmergencyTransferData(
    var isValid: Boolean,
    var transferStatus: String
)
```


### EmergencyTransferResponse

```kotlin
data class EmergencyTransferResponse(
    override var completionKey: String,
    override var data: EmergencyTransferData
): Result<EmergencyTransferData>
```


### IAssetQuote

```kotlin
data class IAssetQuote(
    var asset: ICryptoFlowAsset,
    var amountExpected: Double,
    var totalFee: Double?,
)
```


### ICryptoFlowAsset

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


### ICryptoFlowQuote

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


### ICryptoFlowQuoteService

```kotlin
data class ICryptoFlowQuoteService(
    var id: String,
    var name: String,
    var logo: String,
    var description: String?
)
```


### InfoData

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


### InfoResponse

```kotlin
data class InfoResponse(
    override var completionKey: String,
    override var data: InfoData
): Result<InfoData>
```


### IntegrationUrlData

```kotlin
data class IntegrationUrlData(
    var url: String?,
)
```


### IntegrationUrlResponse

```kotlin
data class IntegrationUrlResponse(
    override var completionKey: String,
    override var data: IntegrationUrlData
): Result<IntegrationUrlData>
```


### IPFSProviderConfig

```kotlin
data class IPFSProviderConfig(
    val provider: IPFSProvider,
    val token: String
)
```


### KeyRecord

```kotlin
data class KeyRecord(
    val privateKey: String,
    val type: KeyType
)
```


### NodeInfo

```kotlin
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


### NodesData

```kotlin
data class NodesData(
    var nodes: List<NodeInfo>,
)
```


### NodesResponse

```kotlin
data class NodesResponse(
    override var completionKey: String,
    override var data: NodesData
): Result<NodesData>
```


### PrivateKeyData

```kotlin
data class PrivateKeyData(
    var privateKey: String,
    var publicKey: String,
    var accounts: List<String>,
    var evmAddress: String
)
```


### PrivateKeyResponse

```kotlin
data class PrivateKeyResponse(
    override var completionKey: String,
    override var data: PrivateKeyData
): Result<PrivateKeyData>
```


### RemoteConfig

```kotlin
data class RemoteConfig(
    var fpApiKey: String,
    var fpSubdomain: String
)
```


### Response

```kotlin
data class Response (
    override var completionKey: String,
    override var data: Any,
    var error: BladeJSError?,
) : Result<Any>
```


### Result

```kotlin
interface Result<T>{
    var completionKey: String
    var data: T
}
```


### ResultData

```kotlin
data class ResultData(
    var success: Boolean
)
```


### ResultResponse

```kotlin
data class ResultResponse(
    override var completionKey: String,
    override var data: ResultData
): Result<ResultData>
```


### ScheduleTransactionTransfer

```kotlin
abstract class ScheduleTransactionTransfer(
    var type: ScheduleTransferType,
    sender: String,
    receiver: String,
    value: Int,
    tokenId: String = "",
    serial: Int
)
```


### ScheduleTransactionTransferHbar

```kotlin
data class ScheduleTransactionTransferHbar(
    var sender: String,
    var receiver: String,
    var value: Int,
) : ScheduleTransactionTransfer(ScheduleTransferType.HBAR, sender, receiver, value, "", 0)
```


### ScheduleTransactionTransferNFT

```kotlin
data class ScheduleTransactionTransferNFT(
    var sender: String,
    var receiver: String,
    var tokenId: String,
    var serial: Int
) : ScheduleTransactionTransfer(ScheduleTransferType.NFT, sender, receiver, 0, tokenId, serial)
```


### ScheduleTransactionTransferToken

```kotlin
data class ScheduleTransactionTransferToken(
    var sender: String,
    var receiver: String,
    var tokenId: String,
    var value: Int,
) : ScheduleTransactionTransfer(ScheduleTransferType.FT, sender, receiver, value, tokenId, 0)
```


### SignMessageData

```kotlin
data class SignMessageData(
    var signedMessage: String
)
```


### SignMessageResponse

```kotlin
data class SignMessageResponse(
    override var completionKey: String,
    override var data: SignMessageData
): Result<SignMessageData>
```


### SignVerifyMessageData

```kotlin
data class SignVerifyMessageData(
    var valid: Boolean
)
```


### SignVerifyMessageResponse

```kotlin
data class SignVerifyMessageResponse(
    override var completionKey: String,
    override var data: SignVerifyMessageData
): Result<SignVerifyMessageData>
```


### SplitSignatureData

```kotlin
data class SplitSignatureData(
    var v: Int,
    var r: String,
    var s: String,
)
```


### SplitSignatureResponse

```kotlin
data class SplitSignatureResponse(
    override var completionKey: String,
    override var data: SplitSignatureData
): Result<SplitSignatureData>
```


### StakingInfo

```kotlin
data class StakingInfo(
    val pendingReward: Long,
    val stakedNodeId: Int?,
    val stakePeriodStart: String?
)
```


### SwapQuotesData

```kotlin
data class SwapQuotesData(
    var quotes: List<ICryptoFlowQuote>
)
```


### SwapQuotesResponse

```kotlin
data class SwapQuotesResponse(
    override var completionKey: String,
    override var data: SwapQuotesData
): Result<SwapQuotesData>
```


### SwapResultData

```kotlin
data class SwapResultData(
    var success: Boolean,
    var sourceAddress: String,
    var targetAddress: String,
    var balance: BalanceData
)
```


### SwapResultResponse

```kotlin
data class SwapResultResponse(
    override var completionKey: String,
    override var data: SwapResultData
): Result<SwapResultData>
```


### TokenDropData

```kotlin
data class TokenDropData(
    var status: String,
    var statusCode: Int,
    var timestamp: String,
    var executionStatus: String,
    var requestId: String,
    var accountId: String,
    var redirectUrl: String
)
```


### TokenDropResponse

```kotlin
data class TokenDropResponse(
    override var completionKey: String,
    override var data: TokenDropData
) : Result<TokenDropData>
```


### TransactionHistoryDetail

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


### TransactionHistoryNftTransfer

```kotlin
data class TransactionHistoryNftTransfer(
    var is_approval: Boolean,
    var receiver_account_id: String,
    var sender_account_id: String?,
    var serial_number: Int,
    var token_id: String
)
```


### TransactionHistoryPlainData

```kotlin
data class TransactionHistoryPlainData(
    var type: String,
    var token_id: String,
    var amount: Double,
    var senders: List<String>,
    var receivers: List<String>
)
```


### TransactionHistoryTransfer

```kotlin
data class TransactionHistoryTransfer(
    var account: String,
    var amount: Double,
    var is_approval: Boolean,
    var token_id: String?
)
```


### TransactionReceiptData

```kotlin
data class TransactionReceiptData(
    var status: String,
    var contractId: String?,
    var topicSequenceNumber: String?,
    var totalSupply: String?,
    var serials: List<String>?
)
```


### TransactionReceiptResponse

```kotlin
data class TransactionReceiptResponse(
    override var completionKey: String,
    override var data: TransactionReceiptData
): Result<TransactionReceiptData>
```


### TransactionsHistoryData

```kotlin
data class TransactionsHistoryData(
    var nextPage: String?,
    var transactions: List<TransactionHistoryDetail>
)
```


### TransactionsHistoryResponse

```kotlin
data class TransactionsHistoryResponse(
    override var completionKey: String,
    override var data: TransactionsHistoryData
): Result<TransactionsHistoryData>
```


### TransakOrderInfoData

```kotlin
data class TransakOrderInfoData(
    var orderId: String,
    var status: String,
    var walletAddress: String?,
    var createdAt: String,
    var autoExpiresAt: String,
    var isBuyOrSell: String,
    var network: String,
    var notes: List<String>?,
    var fiatCurrency: String,
    var fiatAmount: Double,
    var cryptoCurrency: String,
    var cryptoAmount: Double,
    var conversionPrice: Double?,
    var slippage: Double?,
    var totalFeeInFiat: Double,
    var totalFee: Double?,
)
```


### TransakOrderInfoResponse

```kotlin
data class TransakOrderInfoResponse(
    override var completionKey: String,
    override var data: TransakOrderInfoData
): Result<TransakOrderInfoData>
```





