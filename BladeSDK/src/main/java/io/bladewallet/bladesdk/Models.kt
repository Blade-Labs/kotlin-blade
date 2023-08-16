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
)
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
    var calculatedEvmAddress: String
)

data class TransferResponse(
    override var completionKey: String,
    override var data: TransferData
): Result<TransferData>

data class TransferData(
    var nodeId: String,
    var transactionHash: String,
    var transactionId: String,
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
    var is_approval: Boolean
)

data class TransactionHistoryNftTransfer(
    var is_approval: Boolean,
    var receiver_account_id: String,
    var sender_account_id: String,
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

data class RemoteConfig(
    var fpApiKey: String
)

enum class BladeEnv(val value: String) {
    Prod("Prod"),
    CI("CI")
}