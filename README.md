# Kotlin Blade SDK

[![](https://jitpack.io/v/Blade-Labs/kotlin-blade.svg)](https://jitpack.io/#Blade-Labs/kotlin-blade)

## Install

See ["https://jitpack.io/#Blade-Labs/kotlin-blade"](https://jitpack.io/#Blade-Labs/kotlin-blade) for help on adding a package to your project.

```groovy
// ./settings.gradle
dependencyResolutionManagement {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

```groovy
// ./app/build.gradle
dependencies {
    ...
    implementation 'com.github.Blade-Labs:kotlin-blade:0.4.0'
}
```

```xml
<!-- ./app/src/main/AndroidManifest.xml-->
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" xmlns:tools="http://schemas.android.com/tools">
    ...
    <uses-permission android:name="android.permission.INTERNET" />
    ...
</manifest>
```

## Usage

```kotlin
import io.bladewallet.bladesdk.Blade
import BalanceDataResponse
import BladeJSError

Blade.initialize("API_KEY", "dAppCode", "Testnet", requireContext()) {
  // ready to use BladeSDK
  println("init complete")
}

// Get balance by hedera id
Blade.getBalance("0.0.49177063") { data: BalanceDataResponse?, error: BladeJSError? ->
    if (data != null) {
        println(data)
    }
}

```
