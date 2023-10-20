package io.bladewallet.kotlin_sdk_demo

import io.bladewallet.bladesdk.BladeEnv

object Config {
    var apiKey = "FG9dUBQcBaBAPgCHz7DqmNZzrJyhewAMJytjwp3VFIEMFTXQyVSIDq6wRvtPcSAt"
    var dAppCode = "unitysdktest"
    var network: String = "Testnet"
    var bladeEnv: BladeEnv = BladeEnv.CI

    var accountId = "0.0.346533"
    var accountId2 = "0.0.346530"
    var contractId = "0.0.416245"
    var tokenId = "0.0.433870"
    var publicKey = "302d300706052b8104000a032200029dc73991b0d9cdbb59b2cd0a97a0eaff6de801726cb39804ea9461df6be2dd30"
    var privateKey =
        "302d300706052b8104000a032200029dc73991b0d9cdbb59b2cd0a97a0eaff6de801726cb39804ea9461df6be2dd30"
    var message = "hello"

    fun doSomething() {
        // Implement your logic here
    }
}