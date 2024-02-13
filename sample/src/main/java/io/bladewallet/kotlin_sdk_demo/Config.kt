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
        "3030020100300706052b8104000a04220420ebccecef769bb5597d0009123a0fd96d2cdbe041c2a2da937aaf8bdc8731799b"
    var privateKey2 =
        "3030020100300706052b8104000a04220420ba9ae0a421111c07f6ccc6ea56ce01f06acd15c2ee50761befb23324d7a2438e"
    var privateKey2Account = "0.0.436560"
    var message = "hello"
    var mnemonic = "target waste stamp attend toss elephant cause citizen detail public click baby"
    var coinSearch = "hbar"
    var tokenName = "Super NFT"
    var tokenSymbol = "B++"
    var tokenIdToMint = ""
    var stakedNodeId: Int? = null
}