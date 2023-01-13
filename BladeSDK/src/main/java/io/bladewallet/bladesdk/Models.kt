package io.bladewallet.bladesdk

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

data class BalanceResponse(
    override var completionKey: String,
    override var data: BalanceDataResponse
) : Result<BalanceDataResponse>

data class BalanceDataResponse(
    var hbars: Double,
    var tokens: List<BalanceDataResponseToken>
)

data class BalanceDataResponseToken(
    var balance: Double,
    var tokenId: String
)

data class TransferResponse(
    override var completionKey: String,
    override var data: TransferDataResponse
): Result<TransferDataResponse>

data class TransferDataResponse(
    var nodeId: String,
    var transactionHash: String,
    var transactionId: String,
)

data class CreatedAccountResponse(
    override var completionKey: String,
    override var data: CreatedAccountDataResponse
): Result<CreatedAccountDataResponse>

data class CreatedAccountDataResponse(
    var seedPhrase: String,
    var publicKey: String,
    var privateKey: String,
    var accountId: String,
    var evmAddress: String,
)

data class TransactionReceiptResponse(
    override var completionKey: String,
    override var data: TransactionReceiptDataResponse
): Result<TransactionReceiptDataResponse>

data class TransactionReceiptDataResponse(
    var status: String,
    var contractId: String?,
    var topicSequenceNumber: String?,
    var totalSupply: String?,
    var serials: List<String>?
)

data class PrivateKeyResponse(
    override var completionKey: String,
    override var data: PrivateKeyDataResponse
): Result<PrivateKeyDataResponse>

data class PrivateKeyDataResponse(
    var privateKey: String,
    var publicKey: String,
    var accounts: List<String>,
    var evmAddress: String
)

data class SignMessageResponse(
    override var completionKey: String,
    override var data: SignMessageDataResponse
): Result<SignMessageDataResponse>

data class SignMessageDataResponse(
    var signedMessage: String
)

data class SignVerifyMessageResponse(
    override var completionKey: String,
    override var data: SignVerifyMessageDataResponse
): Result<SignVerifyMessageDataResponse>

data class SignVerifyMessageDataResponse(
    var valid: Boolean
)

data class SplitedSignatureResponse(
    override var completionKey: String,
    override var data: SplitedSignature
): Result<SplitedSignature>

data class SplitedSignature(
    var v: Int,
    var r: String,
    var s: String,
)

data class TransactionsHistoryResponse(
    override var completionKey: String,
    override var data: TransactionsHistory
): Result<TransactionsHistory>

data class TransactionsHistory(
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
    var plainData: List<TransactionHistoryPlainData>?
)

data class TransactionHistoryPlainData(
    var type: String,
    var token_id: String,
    var account: String,
    var amount: Double
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