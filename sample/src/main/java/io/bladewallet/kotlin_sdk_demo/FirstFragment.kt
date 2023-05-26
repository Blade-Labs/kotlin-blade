package io.bladewallet.kotlin_sdk_demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.bladewallet.bladesdk.*
import io.bladewallet.kotlin_sdk_demo.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contractId = "0.0.8316"
        val privateKey =
            "3030020100300706052b8104000a04220420ebccecef769bb5597d0009123a0fd96d2cdbe041c2a2da937aaf8bdc8731799b"
        val accountId = "0.0.8235"
        val message = "hello world"


        binding.buttonFirst.setOnClickListener {
            Blade.initialize("api-key", "dAppCode", "Testnet", BladeEnv.Prod, requireContext()) { infoData, bladeJSError ->
                println("Init done!!!")
                println(infoData)
                println(bladeJSError)
            }
        }

        binding.buttonSecond.setOnClickListener {

            Blade.transferHbars("accountid", "private key", "reciever id", 123.2, "tansaction ' memo") { transactionResult, bladeJSError ->
                println(transactionResult)
                println(bladeJSError)
            }

            return@setOnClickListener

            Blade.createHederaAccount("android device id") { createdAccountData, bladeJSError ->
                println(createdAccountData)
                println(bladeJSError)
            }
            return@setOnClickListener

            Blade.getBalance(accountId) { data: BalanceData?, error: BladeJSError? ->
                if (data != null) {
                    println(data)
                }
                if (error != null) {
                    println(error)
                }
            }
            return@setOnClickListener

            println("contract call")
            var params = ContractFunctionParameters().addString(message)
            Blade.contractCallFunction(contractId, "set_message", params, accountId, privateKey, 55000, false) { data, error: BladeJSError? ->
                println("=== SET  CONTRACT ===")
                if (data != null) {
                    println(data)
                }
                if (error != null) {
                    println(error)
                }
            }

//            Blade.getC14url("hbar", "0.0.1242322", "444") { data, error: BladeJSError? ->
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
//                "0.0.48915363",
//                "set_message",
//                params,
//                "0.0.48905075",
//                "302e020100300506032b65700422042042df7851440d10c1599999f28653ae31d59adc0bc4b7d18caaae777da38e385f"
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