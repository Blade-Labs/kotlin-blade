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


### AccountProvider

```kotlin
enum class AccountProvider(val value: String) {
    PrivateKey("PrivateKey"),
    Magic("Magic");

    companion object {
        fun fromKey(key: String): AccountProvider {
            return AccountProvider.values().find { it.name == key }
                ?: throw IllegalArgumentException("Unknown AccountProvider key: $key")
        }
    }
}
```


### BladeEnv

```kotlin
enum class BladeEnv(val value: String) {
    Prod("Prod"),
    CI("CI");

    companion object {
        fun fromKey(key: String): BladeEnv {
            return BladeEnv.values().find { it.name == key }
                ?: throw IllegalArgumentException("Unknown BladeEnv key: $key")
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


### ExchangeStrategy

```kotlin
enum class ExchangeStrategy(val value: String) {
    BUY("Buy"),
    SELL("Sell"),
    SWAP("Swap");

    companion object {
        fun fromValue(value: String): ExchangeStrategy? {
            return values().find { it.value == value }
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


### KnownChains

```kotlin
enum class KnownChains(val value: String) {
    @SerializedName("eip155:1")
    ETHEREUM_MAINNET("eip155:1"),

    @SerializedName("eip155:11155111")
    ETHEREUM_SEPOLIA("eip155:11155111"),

    @SerializedName("hedera:295")
    HEDERA_MAINNET("hedera:295"),

    @SerializedName("hedera:296")
    HEDERA_TESTNET("hedera:296");

    companion object {
        fun fromString(value: String): KnownChains {
            return values().find { it.value == value }
                ?: throw IllegalArgumentException("Unknown chain: $value")
        }

        fun fromKey(key: String): KnownChains {
            return values().find { it.name == key }
                ?: throw IllegalArgumentException("Unknown KnownChains key: $key")
        }
    }
}
```


### NFTStorageProvider

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


### SupportedEncoding

```kotlin
enum class SupportedEncoding(val value: String) {
    base64("base64"),
    hex("hex"),
    utf8("utf8")
}
```



# Data types


### AccountInfoData

```kotlin
data class AccountInfoData(
    var accountAddress: String,
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
    var balance: String,
    var rawBalance: String,
    var decimals: Int,
    var tokens: List<TokenBalanceData>
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


### ContractCallQueryRecord

```kotlin
data class ContractCallQueryRecord(
    var type: String,
    var value: JsonElement // Use JsonElement to represent any type
) {
    val actualValue: Any
        get() = when {
            value.isJsonPrimitive -> {
                val primitive = value.asJsonPrimitive
                when {
                    primitive.isBoolean -> primitive.asBoolean
                    primitive.isNumber -> primitive.asNumber
                    primitive.isString -> primitive.asString
                    else -> throw IllegalStateException("Unknown primitive type")
                }
            }
            else -> throw IllegalStateException("Unknown value type")
        }
}
```


### ContractCallQueryRecordsData

```kotlin
data class ContractCallQueryRecordsData(
    var gasUsed: Int,
    var values: List<ContractCallQueryRecord>
)
```


### ContractCallQueryRecordsResponse

```kotlin
data class ContractCallQueryRecordsResponse(
    override var completionKey: String,
    override var data: ContractCallQueryRecordsData
): Result<ContractCallQueryRecordsData>
```


### ContractFunctionParameter

```kotlin
data class ContractFunctionParameter (
    var type: String,
    var value: List<String>
)
```


### CreatedAccountData

```kotlin
data class CreatedAccountData(
    var seedPhrase: String,
    var publicKey: String,
    var privateKey: String,
    var accountAddress: String?,
    var evmAddress: String,
    var status: String,
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


### ExchangeAsset

```kotlin
data class ExchangeAsset(
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


### ExchangeQuote

```kotlin
data class ExchangeQuote(
    var service: ICryptoFlowQuoteService,
    var source: IAssetQuote,
    var target: IAssetQuote,
    var rate: Double?,
    var widgetUrl: String?,
    var paymentMethods: List<String>?
)
```


### HederaKey

```kotlin
data class HederaKey(
    val _type: CryptoKeyType,
    val key: String,
)
```


### IAssetQuote

```kotlin
data class IAssetQuote(
    var asset: ExchangeAsset,
    var amountExpected: Double,
    var totalFee: Double?,
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
    var chain: KnownChains,
    var isTestnet: Boolean,
    var visitorId: String,
    var sdkEnvironment: BladeEnv,
    var sdkVersion: String,
    var nonce: Int,
    var user: UserInfoData
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


### KeyRecord

```kotlin
data class KeyRecord(
    val privateKey: String,
    val type: KeyType
)
```


### NftInfo

```kotlin
data class NftInfo(
    val account_id: String,
    val token_id: String,
    val delegating_spender: String?,
    val spender_id: String,
    val created_timestamp: String,
    val deleted: Boolean,
    val metadata: String,
    val modified_timestamp: String,
    val serial_number: Int,
)
```


### NftMetadata

```kotlin
data class NftMetadata(
    val name: String,
    val type: String,
    val creator: String,
    val author: String,
    val properties: Map<String, Any?>,
    val image: String
)
```


### NFTStorageConfig

```kotlin
data class NFTStorageConfig(
    val provider: NFTStorageProvider,
    val apiKey: String
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
    var fpApiKey: String
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
    var quotes: List<ExchangeQuote>
)
```


### SwapQuotesResponse

```kotlin
data class SwapQuotesResponse(
    override var completionKey: String,
    override var data: SwapQuotesData
): Result<SwapQuotesData>
```


### TokenBalanceData

```kotlin
data class TokenBalanceData(
    var balance: String,
    var decimals: Int,
    var name: String,
    var symbol: String,
    var address: String,
    var rawBalance: String
)
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


### TokenInfo

```kotlin
data class TokenInfo(
    val admin_key: HederaKey,
    val auto_renew_account: String,
    val auto_renew_period: Int,
    val created_timestamp: String,
    val decimals: String,
    val deleted: Boolean,
    val expiry_timestamp: Int,
    val fee_schedule_key: HederaKey?,
    val freeze_default: Boolean,
    val freeze_key: HederaKey,
    val initial_supply: String,
    val kyc_key: HederaKey,
    val max_supply: String,
    val memo: String,
    val modified_timestamp: String,
    val name: String,
    val pause_key: HederaKey,
    val pause_status: String,
    val supply_key: HederaKey,
    val supply_type: String,
    val symbol: String,
    val token_id: String,
    val total_supply: String,
    val treasury_account_id: String,
    val type: String,
    val wipe_key: HederaKey?,
)
```


### TokenInfoData

```kotlin
data class TokenInfoData(
    val token: TokenInfo,
    val nft: NftInfo?,
    val metadata: NftMetadata?
)
```


### TokenInfoResponse

```kotlin
data class TokenInfoResponse(
    override var completionKey: String,
    override var data: TokenInfoData
): Result<TokenInfoData>
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
    var contractAddress: String?,
    var topicSequenceNumber: String?,
    var totalSupply: String?,
    var serials: List<String>?,
    var transactionHash: String
)
```


### TransactionReceiptResponse

```kotlin
data class TransactionReceiptResponse(
    override var completionKey: String,
    override var data: TransactionReceiptData
): Result<TransactionReceiptData>
```


### TransactionResponseData

```kotlin
data class TransactionResponseData(
    var transactionHash: String,
    var transactionId: String
)
```


### TransactionResponseResponse

```kotlin
data class TransactionResponseResponse(
    override var completionKey: String,
    override var data: TransactionResponseData
): Result<TransactionResponseData>
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


### UserInfoData

```kotlin
data class UserInfoData(
    var address: String,
    var accountProvider: AccountProvider?,
    var privateKey: String,
    var publicKey: String
)
```


### UserInfoResponse

```kotlin
data class UserInfoResponse(
    override var completionKey: String,
    override var data: UserInfoData
): Result<UserInfoData>
```





