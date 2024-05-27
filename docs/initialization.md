# Usage

Before call any method of Blade object you need to call `initialize`. It creates invisible web-view, and attach all required handlers to interact. 
Also, it will initialize fingerprint library and retrieve visitorId, which is required for BladeAPI.    

```kotlin
import io.bladewallet.bladesdk.Blade
import io.bladewallet.bladesdk.BalanceData
import io.bladewallet.bladesdk.BladeJSError

Blade.initialize("api-key", "dAppCode", "Testnet", BladeEnv.Prod, requireContext(), false) { infoData, bladeJSError ->
    if (infoData != null) {
        println("BladeInit success: $infoData")
    } else {
        println("BladeInit fail: ${bladeJSError}")
    }
}
```

After initialization, you can call any public method, e.g.:

```kotlin
// Get balance by hedera account id
Blade.getBalance("0.0.49177063") { data: BalanceData?, error: BladeJSError? ->
    if (data != null) {
        println(data ?: error)
    }
}
```
