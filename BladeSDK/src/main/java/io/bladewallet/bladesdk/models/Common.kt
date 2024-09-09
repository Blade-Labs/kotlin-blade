package io.bladewallet.bladesdk.models

import com.google.gson.JsonElement

data class ContractFunctionParameter (
    var type: String,
    var value: List<String>
)

interface Result<T>{
    var completionKey: String
    var data: T
}

data class BladeJSError(
    var name: String,
    var reason: String
) : Throwable()

data class Response (
    override var completionKey: String,
    override var data: Any,
    var error: BladeJSError?,
) : Result<Any>

data class InfoResponse(
    override var completionKey: String,
    override var data: InfoData
): Result<InfoData>

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

data class UserInfoResponse(
    override var completionKey: String,
    override var data: UserInfoData
): Result<UserInfoData>

data class UserInfoData(
    var address: String,
    var accountProvider: AccountProvider?,
    var privateKey: String,
    var publicKey: String
)

data class BalanceResponse(
    override var completionKey: String,
    override var data: BalanceData
) : Result<BalanceData>

data class BalanceData(
    var balance: String,
    var rawBalance: String,
    var decimals: Int,
    var tokens: List<TokenBalanceData>
)

data class TokenBalanceData(
    var balance: String,
    var decimals: Int,
    var name: String,
    var symbol: String,
    var address: String,
    var rawBalance: String
)

data class AccountInfoResponse(
    override var completionKey: String,
    override var data: AccountInfoData
): Result<AccountInfoData>

data class AccountInfoData(
    var accountAddress: String,
    var evmAddress: String,
    var calculatedEvmAddress: String,
    var publicKey: String,
    var stakingInfo: StakingInfo
)

data class StakingInfo(
    val pendingReward: Long,
    val stakedNodeId: Int?,
    val stakePeriodStart: String?
)

data class NodesResponse(
    override var completionKey: String,
    override var data: NodesData
): Result<NodesData>

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

data class CreatedAccountResponse(
    override var completionKey: String,
    override var data: CreatedAccountData
): Result<CreatedAccountData>

data class CreatedAccountData(
    var seedPhrase: String,
    var publicKey: String,
    var privateKey: String,
    var accountAddress: String?,
    var evmAddress: String,
    var status: String,
)

data class TransactionResponseResponse(
    override var completionKey: String,
    override var data: TransactionResponseData
): Result<TransactionResponseData>

data class TransactionResponseData(
    var transactionHash: String,
    var transactionId: String
)

data class TransactionReceiptResponse(
    override var completionKey: String,
    override var data: TransactionReceiptData
): Result<TransactionReceiptData>

data class TransactionReceiptData(
    var status: String,
    var contractAddress: String?,
    var topicSequenceNumber: String?,
    var totalSupply: String?,
    var serials: List<String>?,
    var transactionHash: String
)

data class TokenInfoResponse(
    override var completionKey: String,
    override var data: TokenInfoData
): Result<TokenInfoData>

data class TokenInfoData(
    val token: TokenInfo,
    val nft: NftInfo?,
    val metadata: NftMetadata?
)

data class ContractCallQueryRecordsResponse(
    override var completionKey: String,
    override var data: ContractCallQueryRecordsData
): Result<ContractCallQueryRecordsData>

data class ContractCallQueryRecordsData(
    var gasUsed: Int,
    var values: List<ContractCallQueryRecord>
)

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

data class PrivateKeyResponse(
    override var completionKey: String,
    override var data: PrivateKeyData
): Result<PrivateKeyData>

data class PrivateKeyData(
    var privateKey: String,
    var publicKey: String,
    var accounts: List<String>,
    var evmAddress: String
)

data class AccountPrivateResponse(
    override var completionKey: String,
    override var data: AccountPrivateData
): Result<AccountPrivateData>

data class AccountPrivateData(
    var accounts: List<AccountPrivateRecord>
)

data class AccountPrivateRecord(
    var privateKey: String,
    var publicKey: String,
    var evmAddress: String,
    var address: String,
    var path: String,
    val keyType: CryptoKeyType
)

data class SignMessageResponse(
    override var completionKey: String,
    override var data: SignMessageData
): Result<SignMessageData>

data class SignMessageData(
    var signedMessage: String
)

data class SignVerifyMessageResponse(
    override var completionKey: String,
    override var data: SignVerifyMessageData
): Result<SignVerifyMessageData>

data class SignVerifyMessageData(
    var valid: Boolean
)

data class SplitSignatureResponse(
    override var completionKey: String,
    override var data: SplitSignatureData
): Result<SplitSignatureData>

data class SplitSignatureData(
    var v: Int,
    var r: String,
    var s: String,
)

data class TransactionsHistoryResponse(
    override var completionKey: String,
    override var data: TransactionsHistoryData
): Result<TransactionsHistoryData>

data class TransactionsHistoryData(
    var nextPage: String?,
    var transactions: List<TransactionHistoryDetail>
)

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

data class TransactionHistoryPlainData(
    var type: String,
    var token_id: String,
    var amount: Double,
    var senders: List<String>,
    var receivers: List<String>
)

data class TransactionHistoryTransfer(
    var account: String,
    var amount: Double,
    var is_approval: Boolean,
    var token_id: String?
)

data class TransactionHistoryNftTransfer(
    var is_approval: Boolean,
    var receiver_account_id: String,
    var sender_account_id: String?,
    var serial_number: Int,
    var token_id: String
)

data class IntegrationUrlResponse(
    override var completionKey: String,
    override var data: IntegrationUrlData
): Result<IntegrationUrlData>

data class IntegrationUrlData(
    var url: String?,
)

data class SwapQuotesResponse(
    override var completionKey: String,
    override var data: SwapQuotesData
): Result<SwapQuotesData>

data class SwapQuotesData(
    var quotes: List<ExchangeQuote>
)

data class ExchangeQuote(
    var service: ICryptoFlowQuoteService,
    var source: IAssetQuote,
    var target: IAssetQuote,
    var rate: Double?,
    var widgetUrl: String?,
    var paymentMethods: List<String>?
)

data class ICryptoFlowQuoteService(
    var id: String,
    var name: String,
    var logo: String,
    var description: String?
)
data class IAssetQuote(
    var asset: ExchangeAsset,
    var amountExpected: Double,
    var totalFee: Double?,
)

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

data class ResultResponse(
    override var completionKey: String,
    override var data: ResultData
): Result<ResultData>

data class ResultData(
    var success: Boolean
)

data class CreateTokenResponse(
    override var completionKey: String,
    override var data: CreateTokenData
) : Result<CreateTokenData>

data class CreateTokenData(
    var tokenId: String
)

data class RemoteConfig(
    var fpApiKey: String
)

data class CoinListResponse(
    override var completionKey: String,
    override var data: CoinListData
) : Result<CoinListData>

data class CoinListData(
    var coins: List<CoinItem>
)

data class CoinItem (
    var id: String,
    var symbol: String,
    var name: String,
    var platforms: List<CoinGeckoPlatform>
)

data class CoinGeckoPlatform (
    var name: String,
    var address: String
)

data class CoinInfoResponse(
    override var completionKey: String,
    override var data: CoinInfoData
) : Result<CoinInfoData>

data class CoinInfoData(
    var coin: CoinData,
    var priceUsd: Double,
    var price: Double?,
    var currency: String
)

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

data class CoinDataDescription(
    val en: String
)

data class CoinDataImage(
    val thumb: String,
    val small: String,
    val large: String
)

data class CoinDataMarket(
    val current_price: Map<String, Double>
)

data class KeyRecord(
    val privateKey: String,
    val type: KeyType
)

data class NFTStorageConfig(
    val provider: NFTStorageProvider,
    val apiKey: String
)

data class TokenDropResponse(
    override var completionKey: String,
    override var data: TokenDropData
) : Result<TokenDropData>

data class TokenDropData(
    var status: String,
    var statusCode: Int,
    var timestamp: String,
    var executionStatus: String,
    var requestId: String,
    var accountId: String,
    var redirectUrl: String
)

abstract class ScheduleTransactionTransfer(
    var type: ScheduleTransferType,
    sender: String,
    receiver: String,
    value: Int,
    tokenId: String = "",
    serial: Int
)

data class ScheduleTransactionTransferHbar(
    var sender: String,
    var receiver: String,
    var value: Int,
) : ScheduleTransactionTransfer(ScheduleTransferType.HBAR, sender, receiver, value, "", 0)

data class ScheduleTransactionTransferToken(
    var sender: String,
    var receiver: String,
    var tokenId: String,
    var value: Int,
) : ScheduleTransactionTransfer(ScheduleTransferType.FT, sender, receiver, value, tokenId, 0)

data class ScheduleTransactionTransferNFT(
    var sender: String,
    var receiver: String,
    var tokenId: String,
    var serial: Int
) : ScheduleTransactionTransfer(ScheduleTransferType.NFT, sender, receiver, 0, tokenId, serial)

data class CreateScheduleResponse(
    override var completionKey: String,
    override var data: CreateScheduleData
) : Result<CreateScheduleData>

data class CreateScheduleData(
    var scheduleId: String
)

enum class NFTStorageProvider(val value: String) {
    nftStorage("nftStorage");

    companion object {
        fun fromValue(value: String): NFTStorageProvider? {
            return NFTStorageProvider.values().find { it.value == value }
        }
    }
}

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

enum class CryptoKeyType(val value: String) {
    ECDSA_SECP256K1("ECDSA_SECP256K1"),
    ED25519("ED25519");

    companion object {
        fun fromValue(value: String): CryptoKeyType? {
            return CryptoKeyType.values().find { it.value == value }
        }
    }
}

enum class ScheduleTransactionType(val value: String) {
    TRANSFER("TRANSFER");
    // SUBMIT_MESSAGE("SUBMIT_MESSAGE"),
    // APPROVE_ALLOWANCE("APPROVE_ALLOWANCE"),
    // TOKEN_MINT("TOKEN_MINT"),
    // TOKEN_BURN("TOKEN_BURN");
}

enum class ScheduleTransferType(val value: String) {
    HBAR("HBAR"),
    FT("FT"),
    NFT("NFT")
}

enum class SupportedEncoding(val value: String) {
    base64("base64"),
    hex("hex"),
    utf8("utf8")
}