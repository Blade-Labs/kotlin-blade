package io.bladewallet.bladesdk.models

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

data class NftMetadata(
    val name: String,
    val type: String,
    val creator: String,
    val author: String,
    val properties: Map<String, Any?>,
    val image: String
)

data class HederaKey(
    val _type: CryptoKeyType,
    val key: String,
)