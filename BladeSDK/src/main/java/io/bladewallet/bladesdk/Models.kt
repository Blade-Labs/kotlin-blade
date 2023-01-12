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
    public var balance: Double,
    public var tokenId: String
)