package io.bladewallet.bladesdk

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
    var network: String,
    var visitorId: String,
    var sdkEnvironment: String,
    var sdkVersion: String,
    var nonce: Int
)

data class BalanceResponse(
    override var completionKey: String,
    override var data: BalanceData
) : Result<BalanceData>

data class BalanceData(
    var hbars: Double,
    var tokens: List<BalanceDataToken>
)

data class BalanceDataToken(
    var balance: Double,
    var tokenId: String
)

data class AccountInfoResponse(
    override var completionKey: String,
    override var data: AccountInfoData
): Result<AccountInfoData>

data class AccountInfoData(
    var accountId: String,
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
    var accountId: String?,
    var evmAddress: String,
    var transactionId: String?,
    var status: String,
    var queueNumber: Int?
)

data class TransactionReceiptResponse(
    override var completionKey: String,
    override var data: TransactionReceiptData
): Result<TransactionReceiptData>

data class TransactionReceiptData(
    var status: String,
    var contractId: String?,
    var topicSequenceNumber: String?,
    var totalSupply: String?,
    var serials: List<String>?
)

data class ContractQueryResponse(
    override var completionKey: String,
    override var data: ContractQueryData
): Result<ContractQueryData>

data class ContractQueryData(
    var gasUsed: Int,
    var values: List<ContractQueryRecord>
)

data class ContractQueryRecord(
    var type: String,
    var value: String
)

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
    var quotes: List<ICryptoFlowQuote>
)

data class ICryptoFlowQuote(
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
    var asset: ICryptoFlowAsset,
    var amountExpected: Double,
    var totalFee: Double?,
)

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
    CI("CI")
}

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

enum class CryptoKeyType(val value: String) {
    ECDSA_SECP256K1("ECDSA_SECP256K1"),
    ED25519("ED25519");

    companion object {
        fun fromValue(value: String): CryptoKeyType? {
            return CryptoKeyType.values().find { it.value == value }
        }
    }
}