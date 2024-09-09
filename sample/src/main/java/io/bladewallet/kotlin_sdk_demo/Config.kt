package io.bladewallet.kotlin_sdk_demo

import io.bladewallet.bladesdk.models.AccountProvider
import io.bladewallet.bladesdk.models.BladeEnv
import io.bladewallet.bladesdk.models.KnownChains

object Config {
    var apiKey = "FG9dUBQcBaBAPgCHz7DqmNZzrJyhewAMJytjwp3VFIEMFTXQyVSIDq6wRvtPcSAt"
    var dAppCode = "unitysdktest"
    var chain: KnownChains = KnownChains.HEDERA_TESTNET
//    var chainId: KnownChainIds = KnownChainIds.ETHEREUM_SEPOLIA
    var bladeEnv: BladeEnv = BladeEnv.CI
    var accountProvider: AccountProvider = AccountProvider.PrivateKey
//    var accountProvider: AccountProvider = AccountProvider.Magic
    var magicEmail = "the.gary.du+sdk2@gmail.com"

    var message = "hello"
    var coinSearch = "hbar"
    var tokenName = "Super NFT"
    var tokenSymbol = "B++"
    var tokenIdToMint = ""
    var stakedNodeId: Int? = null
    var nonce: String = "unity_test"

    var accountAddress = ""
    var accountPrivateKey = ""
    var accountPublicKey = ""
    var accountAddress2 = ""
    var accountPrivateKey2 = ""
    var accountMnemonic = ""
    var contractAddress = ""
    var tokenAddress = ""

    private var hederaAccountId1 = "0.0.1443"
    private var hederaPrivateKey1 = "3030020100300706052b8104000a04220420ebccecef769bb5597d0009123a0fd96d2cdbe041c2a2da937aaf8bdc8731799b"
    private var hederaPublicKey1 = "302d300706052b8104000a032200029dc73991b0d9cdbb59b2cd0a97a0eaff6de801726cb39804ea9461df6be2dd30"
    private var hederaContractId = "0.0.4437600"
    private var hederaTokenAddress = "0.0.2216053"
    private var hederaAccountId2 = "0.0.1881"
    private var hederaPrivateKey2 = "3030020100300706052b8104000a042204200e65ab47c5f66cd0db9d1517f43c415e13f16dc1bcf30d85da1c73e58fc5366d"
    private var hederaMnemonic = "purity slab doctor swamp tackle rebuild summer bean craft toddler blouse switch"

    private var ethereumAddress = "0x11f8D856FF2aF6700CCda4999845B2ed4502d8fB"
    private var ethereumPrivateKey = "ebccecef769bb5597d0009123a0fd96d2cdbe041c2a2da937aaf8bdc8731799b"
    private var ethereumPublicKey = ""
    private var ethereumAddress2 = "0x085946E373353Eb190794CA7EF4d7b9a60D0A4B0"
    private var ethereumPrivateKey2 = ""
    private var ethereumMnemonic = "boring slice orange recycle crew aunt fat meadow solid quality wasp visual"
    private var ethereumContractAddress = "0xd36b5b8b408bad51009bd2748c3bc130c68948d2"
    private var ethereumTokenAddress = "0xc72073559B3430ed97ecaCa7111B25d7CBa4E91A"

    fun setChain(chain: KnownChains, force: Boolean = false) {
        if (this.chain != chain || force) {
            this.chain = chain;
            when (chain) {
                KnownChains.HEDERA_TESTNET -> {
                    accountAddress = hederaAccountId1
                    accountPrivateKey = hederaPrivateKey1
                    accountPublicKey = hederaPublicKey1
                    accountAddress2 = hederaAccountId2
                    accountPrivateKey2 = hederaPrivateKey2
                    accountMnemonic = hederaMnemonic
                    contractAddress = hederaContractId
                    tokenAddress = hederaTokenAddress
                }
                KnownChains.ETHEREUM_SEPOLIA -> {
                    accountAddress = ethereumAddress
                    accountPrivateKey = ethereumPrivateKey
                    accountPublicKey = ethereumPublicKey
                    accountAddress2 = ethereumAddress2
                    accountPrivateKey2 = ethereumPrivateKey2
                    accountMnemonic = ethereumMnemonic
                    contractAddress = ethereumContractAddress
                    tokenAddress = ethereumTokenAddress
                }
                else -> {
                    accountAddress = ""
                    accountPrivateKey = ""
                    accountPublicKey = ""
                    accountAddress2 = ""
                    accountPrivateKey2 = ""
                    accountMnemonic = ""
                    contractAddress = ""
                    tokenAddress = ""
                }
            }
        }
    }
}