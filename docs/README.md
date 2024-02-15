# 🚀 Getting Started

### Kotlin Blade SDK

[![](https://jitpack.io/v/Blade-Labs/kotlin-blade.svg)](https://jitpack.io/#Blade-Labs/kotlin-blade)

### Requirements

* Android 8.0+ (API level 26, Aug 2017)

### Example app

This repository contains [sample app](../sample/README.md), please check 

### Install

See ["https://jitpack.io/#Blade-Labs/kotlin-blade"](https://jitpack.io/#Blade-Labs/kotlin-blade) for help on adding a package to your project.

```groovy
// ./settings.gradle
dependencyResolutionManagement {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
        maven { url 'https://maven.fpregistry.io/releases' }
    }
}
```

```groovy
// ./app/build.gradle
dependencies {
    ...
    implementation 'com.github.Blade-Labs:kotlin-blade:0.6.16'
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
