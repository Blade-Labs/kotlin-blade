package io.bladewallet.kotlin_sdk_demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.bladewallet.bladesdk.*
import io.bladewallet.kotlin_sdk_demo.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apiKey = "FG9dUBQcBaBAPgCHz7DqmNZzrJyhewAMJytjwp3VFIEMFTXQyVSIDq6wRvtPcSAt"
        val dAppCode = "unitysdktest"
        val accountId = "0.0.346533"
        val accountId2 = "0.0.346530"
        val contractId = "0.0.416245"
        val tokenId = "0.0.433870"
        val publicKey = "302d300706052b8104000a032200029dc73991b0d9cdbb59b2cd0a97a0eaff6de801726cb39804ea9461df6be2dd30"
        val privateKey =
            "302d300706052b8104000a032200029dc73991b0d9cdbb59b2cd0a97a0eaff6de801726cb39804ea9461df6be2dd30"
        val message = "hello"
        binding.buttonSecond.isEnabled = false
        binding.buttonThird.isEnabled = false

        binding.buttonFirst.setOnClickListener {
            Blade.initialize(apiKey, dAppCode, "Testnet", BladeEnv.CI, requireContext()) { infoData, bladeJSError ->
                if (infoData != null) {
                    println("BladeInit success: $infoData")
                    lifecycleScope.launch {
                        binding.buttonFirst.isEnabled = false
                        binding.buttonSecond.isEnabled = true
                        binding.buttonThird.isEnabled = true
                    }
                } else {
                    println("BladeInit fail: ${bladeJSError}")
                }
            }
        }

        binding.buttonSecond.setOnClickListener {

//        // QUOTES SWAP
//        Blade.exchangeGetQuotes("HBAR", 2.0, "KARATE", CryptoFlowServiceStrategy.SWAP) { data, error ->
//            println("res: ${data ?: error}");
//            // SwapQuotesData(quotes=[ICryptoFlowQuote(service=ICryptoFlowQuoteService(id=saucerswap, name=Saucerswap, logo=https://img.bld-dev.bladewallet.io/crypto-flow-img/saucerswap.svg, description=null), source=IAssetQuote(asset=ICryptoFlowAsset(name=WHBAR [new], code=HBAR, type=crypto, address=0.0.1456986, chainId=295, decimals=8, minAmount=null, maxAmount=null, symbol=null, imageUrl=null), amountExpected=2.0, totalFee=null), target=IAssetQuote(asset=ICryptoFlowAsset(name=Karate, code=KARATE, type=crypto, address=0.0.2283230, chainId=295, decimals=8, minAmount=null, maxAmount=null, symbol=null, imageUrl=null), amountExpected=161.67068371, totalFee=null), rate=null, widgetUrl=null, paymentMethods=null)])
//        }
//
//        // QUOTES BUY
//        Blade.exchangeGetQuotes("EUR", 50.0, "HBAR", CryptoFlowServiceStrategy.BUY) { data, error ->
//            println(data ?: error);
//            // SwapQuotesData(quotes=[ICryptoFlowQuote(service=ICryptoFlowQuoteService(id=moonpay, name=Moonpay, logo=https://img.bld-dev.bladewallet.io/crypto-flow-img/moonpay.svg, description=null), source=IAssetQuote(asset=ICryptoFlowAsset(name=Euro, code=EUR, type=fiat, address=null, chainId=null, decimals=null, minAmount=null, maxAmount=null, symbol=eur, imageUrl=null), amountExpected=50.0, totalFee=1.0300000000000011), target=IAssetQuote(asset=ICryptoFlowAsset(name=Hedera Hashgraph, code=HBAR, type=crypto, address=, chainId=295, decimals=null, minAmount=20.0, maxAmount=2000.0, symbol=null, imageUrl=null), amountExpected=1071.0, totalFee=null), rate=0.045702478947599066, widgetUrl=https://buy.moonpay.com/?apiKey=pk_live_2uZEjEOa31JWcga7QGg5Lq8Klx7mEXUj&enabledPayments=credit_debit_card%2Capple_pay%2Cgoogle_pay%2Csamsung_pay%2Csepa_bank_transfer%2Cgbp_bank_transfer%2Cgbp_open_banking_payment&colorCode=%23EF6345&theme=dark&showOnlyCurrencies=hbar%2Cusdc_hedera&defaultCurrencyCode=hbar&baseCurrencyCode=eur&baseCurrencyAmount=50&walletAddress=undefined, paymentMethods=null), ICryptoFlowQuote(service=ICryptoFlowQuoteService(id=c14, name=C14, logo=https://img.bld-dev.bladewallet.io/crypto-flow-img/c14.svg, description=null), source=IAssetQuote(asset=ICryptoFlowAsset(name=EUR, code=EUR, type=fiat, address=null, chainId=null, decimals=null, minAmount=null, maxAmount=null, symbol=EUR, imageUrl=null), amountExpected=50.0, totalFee=1.51), target=IAssetQuote(asset=ICryptoFlowAsset(name=Hedera, code=HBAR, type=crypto, address=0.0.0, chainId=295, decimals=null, minAmount=null, maxAmount=null, symbol=null, imageUrl=null), amountExpected=1083.53, totalFee=null), rate=null, widgetUrl=https://pay.c14.money/?clientId=00ce2e0a-ee66-4971-a0e9-b9d627d106b0&targetAssetId=d9b45743-e712-4088-8a31-65ee6f371022&targetAssetIdLock=false&sourceCurrencyCode=EUR&sourceAmount=50&quoteAmountLock=false&targetAddress=undefined&targetAddressLock=false, paymentMethods=null)])
//        }
//
//        // QUOTES SELL
//        Blade.exchangeGetQuotes("USDC", 50.0, "PHP", CryptoFlowServiceStrategy.SELL) { data, error ->
//            println(data ?: error);
//            // SwapQuotesData(quotes=[ICryptoFlowQuote(service=ICryptoFlowQuoteService(id=onmeta, name=OnMeta, logo=https://img.bld-dev.bladewallet.io/crypto-flow-img/onmeta.svg, description=null), source=IAssetQuote(asset=ICryptoFlowAsset(name=USD Coin, code=USDC, type=crypto, address=0.0.456858, chainId=295, decimals=6, minAmount=null, maxAmount=null, symbol=null, imageUrl=https://www.saucerswap.finance/images/tokens/usdc.svg), amountExpected=50.500008, totalFee=null), target=IAssetQuote(asset=ICryptoFlowAsset(name=PHP, code=PHP, type=fiat, address=null, chainId=null, decimals=null, minAmount=null, maxAmount=null, symbol=₱, imageUrl=null), amountExpected=2766.3926570000003, totalFee=27.663926570000005), rate=55.32784438, widgetUrl=https://platform.onmeta.in/?apiKey=347ba290-a100-423a-9ff0-1e6b6152eb47&chainId=295&tokenSymbol=USDC&fiatAmount=2766.3926570000003&fiatType=php&walletAddress=undefined&onRamp=disabled&offRamp=enabled, paymentMethods=null)])
//        }
//
//        // SWAP HBAR TO KARATE
//        Blade.swapTokens("0.0.832167", "3030020100300706052b8104000.....", "HBAR", 1.0, "KARATE", 0.5, "saucerswap") { data, error ->
//            println(data ?: error);
//            // ResultData(success=true)
//            // or
//            // BladeJSError(name=StatusError, reason=transaction 0.0.832167@1697713937.103782570 failed precheck with status INSUFFICIENT_PAYER_BALANCE)
//        }
//
//        // BUY URL
//        Blade.getTradeUrl(CryptoFlowServiceStrategy.BUY, "0.0.2625650", "EUR", 50.0, "HBAR", 0.5, "moonpay") { data, error ->
//            println(data ?: error);
//            // IntegrationUrlData(url=https://buy.moonpay.com/?apiKey=pk_live_2uZEjEOa31JWcga7QGg5Lq8Klx7mEXUj&enabledPayments=credit_debit_card%2Capple_pay%2Cgoogle_pay%2Csamsung_pay%2Csepa_bank_transfer%2Cgbp_bank_transfer%2Cgbp_open_banking_payment&colorCode=%23EF6345&theme=dark&showOnlyCurrencies=hbar%2Cusdc_hedera&defaultCurrencyCode=hbar&baseCurrencyCode=eur&baseCurrencyAmount=50&walletAddress=0.0.2625650)
//        }
//
//        // SELL URL
//        Blade.getTradeUrl(CryptoFlowServiceStrategy.SELL, "0.0.2625650", "USDC", 50.0, "PHP", 0.5, "onmeta") { data, error ->
//            println(data ?: error);
//            // IntegrationUrlData(url=https://platform.onmeta.in/?apiKey=347ba290-a100-423a-9ff0-1e6b6152eb47&chainId=295&tokenSymbol=USDC&fiatAmount=2766.3926570000003&fiatType=php&walletAddress=0.0.2625650&onRamp=disabled&offRamp=enabled)
//        }



//            Blade.getTransactions(
//                accountId,
//                "CRYPTOTRANSFERTOKEN",
//                "",
//                10
//            ) { data: TransactionsHistoryData?, error: BladeJSError? ->
//                println(data)
//                println(error)
//            }

//            Blade.transferHbars(accountId, privateKey, accountId2, 123.2, "tansaction ' memo") { transactionResult, bladeJSError ->
//                println(transactionResult)
//                println(bladeJSError)
//            }
//
//            return@setOnClickListener
//
//            Blade.createHederaAccount("android device id") { createdAccountData, bladeJSError ->
//                println(createdAccountData)
//                println(bladeJSError)
//            }
//            return@setOnClickListener
//
            Blade.getBalance(accountId) { data: BalanceData?, error: BladeJSError? ->
                if (data != null) {
                    println(data)
                }
                if (error != null) {
                    println(error)
                }
            }
            return@setOnClickListener
//
//            println("contract call")
//            var params = ContractFunctionParameters().addString(message)
//            Blade.contractCallFunction(contractId, "set_message", params, accountId, privateKey, 55000, false) { data, error: BladeJSError? ->
//                println("=== SET  CONTRACT ===")
//                if (data != null) {
//                    println(data)
//                }
//                if (error != null) {
//                    println(error)
//                }
//            }

//            Blade.getC14url("hbar", accountId, "444") { data, error: BladeJSError? ->
//                println("result url:")
//                if (data != null) {
//                    println(data)
//                }
//                if (error != null) {
//                    println(error)
//                }
//            }



//            Blade.createHederaAccount { createdAccountData, bladeJSError ->
//                println(createdAccountData)
//                println(bladeJSError)
//            }





//            val tuple1 = ContractFunctionParameters()
//                .addInt64(5)
//                .addInt64(10)
//            ;
//
//            val tuple2 = ContractFunctionParameters()
//                .addInt64(50)
//                .addTuple(tuple1)
//            ;
//
//            val params = ContractFunctionParameters()
//                .addString("Hello, Backend")
//                .addBytes32(listOf(
//                    0x00u, 0x01u, 0x02u, 0x03u, 0x04u, 0x05u, 0x06u, 0x07u,
//                    0x08u, 0x09u, 0x0Au, 0x0Bu, 0x0Cu, 0x0Du, 0x0Eu, 0x0Fu,
//                    0x10u, 0x11u, 0x12u, 0x13u, 0x14u, 0x15u, 0x16u, 0x17u,
//                    0x18u, 0x19u, 0x1Au, 0x1Bu, 0x1Cu, 0x1Du, 0x1Eu, 0x1Fu))
//                .addAddressArray(listOf("0.0.48738539", "0.0.48738538", "0.0.48738537"))
//                .addAddress("0.0.48850466")
//                .addAddress("0.0.499326")
//                .addAddress("0.0.48801688")
//                .addInt64(1)
//
//                .addUInt8(123u)
//                .addUInt64Array(listOf(1u, 2u, 3u))
//                .addUInt256Array(listOf(BigInteger("1"), BigInteger("2"), BigInteger("3")))
//                .addTuple(tuple2)
//                .addTupleArray(listOf(tuple1, tuple1))
//                .addAddress("0.0.12345")
//                .addUInt64(ULong.MAX_VALUE)
//                .addUInt256(BigInteger("12345"))
//
//            Blade.contractCallFunction(
//                contractId,
//                "set_message",
//                params,
//                accountId,
//                privateKey
//            ) { data: TransactionReceiptData?, error: BladeJSError? ->
//                println(data);
//                println(error);
//            }
        }

        binding.buttonThird.setOnClickListener {
            val params = ContractFunctionParameters()
            Blade.contractCallQueryFunction(contractId, "get_message", params, accountId, privateKey, 55000, false, listOf("string", "int32")) { data, error: BladeJSError? ->
                println("=== GET  CONTRACT ===")
                if (data != null) {
                    println(data)
                }
                if (error != null) {
                    println(error)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}