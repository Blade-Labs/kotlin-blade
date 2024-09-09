# Usage

Before call any method of Blade object you need to call `initialize`. It creates invisible web-view, and attach all required handlers to interact. 
Also, it will initialize fingerprint library and retrieve visitorId, which is required for BladeAPI.    

```kotlin
import io.bladewallet.bladesdk.Blade

Blade.initialize("api-key", KnownChains.HEDERA_TESTNET, "dAppCode", BladeEnv.Prod, requireContext(), false) { infoData, error ->
    if (infoData != null) {
        println("BladeInit success: $infoData")
    } else {
        println("BladeInit fail: $error")
    }
}

// After initialization, you can call any public method, e.g.: getBalance
// Get balance by hedera account id
Blade.getBalance("0.0.49177063") { balanceData, error ->
    if (data != null) {
        println(balanceData ?: error)
    }
}
```

