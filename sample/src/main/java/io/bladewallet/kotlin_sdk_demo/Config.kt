package io.bladewallet.kotlin_sdk_demo

import io.bladewallet.bladesdk.BladeEnv

object Config {
    var apiKey = "FG9dUBQcBaBAPgCHz7DqmNZzrJyhewAMJytjwp3VFIEMFTXQyVSIDq6wRvtPcSAt"
    var dAppCode = "unitysdktest"
    var network: String = "Testnet"
    var bladeEnv: BladeEnv = BladeEnv.CI

    var accountId = "0.0.1443"
    var accountId2 = "0.0.1767"
    var contractId = "0.0.2215872"
    var tokenId = "0.0.2216053"
    var publicKey = "302d300706052b8104000a032200029dc73991b0d9cdbb59b2cd0a97a0eaff6de801726cb39804ea9461df6be2dd30"
    var privateKey =
        "3030020100300706052b8104000a04220420ebccecef769bb5597d0009123a0fd96d2cdbe041c2a2da937aaf8bdc8731799b"
    var privateKey2 =
        "3030020100300706052b8104000a042204200e65ab47c5f66cd0db9d1517f43c415e13f16dc1bcf30d85da1c73e58fc5366d"
    var privateKey2Account = "0.0.1881"
    var message = "hello"
    var mnemonic = "purity slab doctor swamp tackle rebuild summer bean craft toddler blouse switch"
    var coinSearch = "hbar"
    var tokenName = "Super NFT"
    var tokenSymbol = "B++"
    var tokenIdToMint = ""
    var stakedNodeId: Int? = null
    var nonce: String = "unity_test"
}