package io.bladewallet.bladesdk.models
import com.google.gson.annotations.SerializedName

enum class KnownChainIds(val value: String) {
    @SerializedName("1")
    ETHEREUM_MAINNET("1"),

    @SerializedName("11155111")
    ETHEREUM_SEPOLIA("11155111"),

    @SerializedName("295")
    HEDERA_MAINNET("295"),

    @SerializedName("296")
    HEDERA_TESTNET("296");

    companion object {
        fun fromString(value: String): KnownChainIds {
            return values().find { it.value == value }
                ?: throw IllegalArgumentException("Unknown chain ID: $value")
        }

        fun fromKey(key: String): KnownChainIds {
            return values().find { it.name == key }
                ?: throw IllegalArgumentException("Unknown KnownChainIds key: $key")
        }
    }
}