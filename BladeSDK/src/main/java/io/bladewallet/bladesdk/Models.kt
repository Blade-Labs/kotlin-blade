data class BladeJSError(
    var name: String,
    var reason: String
)

data class Response(
    var completionKey: String?,
    var error: BladeJSError?
)

data class BalanceResponse(
    var completionKey: String,
    var data: BalanceDataResponse
)

data class BalanceDataResponse(
    var hbars: Double,
    var tokens: List<BalanceDataResponseToken>
)

data class BalanceDataResponseToken(
    var balance: Double,
    var tokenId: String
)

data class TransferResponse(
    var completionKey: String,
    var data: TransferDataResponse
)

data class TransferDataResponse(
    var nodeId: String,
    var transactionHash: String,
    var transactionId: String,
)

data class CreatedAccountResponse(
    var completionKey: String,
    var data: CreatedAccountDataResponse
)

data class CreatedAccountDataResponse(
    var seedPhrase: String,
    var publicKey: String,
    var privateKey: String,
    var accountId: String,
    var evmAddress: String,
)

data class TransactionReceiptResponse(
    var completionKey: String,
    var data: TransactionReceipt
)

data class TransactionReceipt(
    var status: String,
    var contractId: String?,
    var topicSequenceNumber: String?,
    var totalSupply: String?,
    var serials: List<String>?
)

data class PrivateKeyResponse(
    var completionKey: String,
    var data: PrivateKeyDataResponse
)

data class PrivateKeyDataResponse(
    var privateKey: String,
    var publicKey: String,
    var accounts: List<String>,
    var evmAddress: String
)

data class SignMessageDataResponse(
    var signedMessage: String
)

data class SignMessageResponse(
    var completionKey: String,
    var data: SignMessageDataResponse
)

data class SignVerifyMessageResponse(
    var completionKey: String,
    var data: SignVerifyMessageDataResponse
)

data class SignVerifyMessageDataResponse(
    var valid: Boolean
)