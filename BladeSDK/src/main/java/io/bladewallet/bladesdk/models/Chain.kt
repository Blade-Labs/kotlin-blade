package io.bladewallet.bladesdk.models
import com.google.gson.annotations.SerializedName

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