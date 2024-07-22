package io.bladewallet.kotlin_sdk_demo.examples

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.bladewallet.bladesdk.Blade
import io.bladewallet.bladesdk.ContractFunctionParameters
import io.bladewallet.bladesdk.models.CreatedAccountData
import io.bladewallet.bladesdk.models.SupportedEncoding
import io.bladewallet.kotlin_sdk_demo.Config
import io.bladewallet.kotlin_sdk_demo.databinding.FragmentExamplesBinding
import kotlinx.coroutines.launch

class ExamplesFragment : Fragment() {

    private var _binding: FragmentExamplesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        var startOperation: Long = System.currentTimeMillis()
        var temporaryAccount: CreatedAccountData? = null

        _binding = FragmentExamplesBinding.inflate(inflater, container, false)
        
        val root: View = binding!!.root 

        fun toggleElements(enable: Boolean): Boolean {
            binding!!.editAccountId.isEnabled = enable
            binding!!.buttonBalance.isEnabled = enable
            binding!!.buttonTransactions.isEnabled = enable
            binding!!.buttonCreateAccount.isEnabled = enable
            binding!!.editMnemonicMessageSignature.isEnabled = enable
            binding!!.buttonGetFromMnemonic.isEnabled = enable
            binding!!.buttonSign.isEnabled = enable
            binding!!.buttonVerify.isEnabled = enable
            binding!!.buttonContractCall.isEnabled = enable
            binding!!.buttonContractQuery.isEnabled = enable
            binding!!.buttonTransferHbars.isEnabled = enable
            binding!!.buttonTransferTokens.isEnabled = enable
            return enable
        }

        @SuppressLint("SetTextI18n")
        fun output(text: String) {
            if (text == "") {
                startOperation = System.currentTimeMillis()
                binding?.textTitleOutput?.text = "Output:"
                binding?.progressBar?.visibility = View.VISIBLE
            } else {
                println(text)
                binding?.textTitleOutput?.text = "Output (${System.currentTimeMillis() - startOperation}ms):"
                binding?.progressBar?.visibility = View.GONE
            }
            binding?.outputTextView?.text = text
        }

        Blade.getInfo { infoData, bladeJSError ->
            lifecycleScope.launch {
                if (infoData != null) {
                    toggleElements(true)
                    output("$infoData")
                } else {
                    toggleElements(false)
                    output("$bladeJSError")
                }
            }
        }

        binding!!.editAccountId.setText(Config.accountId)
        binding!!.editMnemonicMessageSignature.setText(Config.mnemonic)
        binding!!.editTextReceiver.setText(Config.accountId2)
        binding!!.editTextAmount.setText("1")

        binding!!.buttonBalance.setOnClickListener {
            output("")
            Blade.getBalance(
                binding?.editAccountId?.text.toString()
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${ result ?: bladeJSError}")
                }
            }
        }

        binding!!.buttonTransactions.setOnClickListener {
            output("")
            Blade.getTransactions(
                accountId = binding?.editAccountId?.text.toString(),
                transactionType = "",
                nextPage = "",
                transactionsLimit = 15
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${ result ?: bladeJSError}")
                }
            }
        }

        binding!!.buttonCreateAccount.setOnClickListener {
            output("")
            Blade.createAccount(privateKey = "", deviceId = "") { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${ result ?: bladeJSError}")
                    if (result != null) {
                        temporaryAccount = result
                        binding?.buttonDeleteAccount?.isEnabled = true
                        binding?.buttonDropTokens?.isEnabled = true
                    }
                }
            }
        }

        binding!!.buttonDropTokens.setOnClickListener {
            output("")
            Blade.dropTokens(
                secretNonce = Config.nonce,
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${ result ?: bladeJSError}")
                }
            }
        }

        binding!!.buttonDeleteAccount.setOnClickListener {
            output("")
            Blade.deleteAccount(
                deleteAccountId = temporaryAccount?.accountAddress.toString(),
                deletePrivateKey = temporaryAccount?.privateKey.toString(),
                transferAccountId = binding?.editAccountId?.text.toString(),
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${ result ?: bladeJSError}")
                    if (result != null) {
                        temporaryAccount = null
                        binding?.buttonDeleteAccount?.isEnabled = false
                        binding?.buttonDropTokens?.isEnabled = false
                    }
                }
            }
        }

        binding!!.buttonGetFromMnemonic.setOnClickListener {
            output("")
            Blade.searchAccounts(
                keyOrMnemonic = binding?.editMnemonicMessageSignature?.text.toString(),
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${ result ?: bladeJSError}")
                }
            }
        }

        binding!!.buttonSign.setOnClickListener {
            output("")
            val encodedString = binding?.editMnemonicMessageSignature?.text.toString()

            Blade.sign(
                encodedString,
                encoding = SupportedEncoding.utf8,
                likeEthers = false
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${ result ?: bladeJSError}")
                }
            }
        }

        binding!!.buttonVerify.setOnClickListener {
            output("")
            val encodedString = Config.message // hello

            Blade.verify(
                encodedString,
                encoding = SupportedEncoding.utf8,
                signature = binding?.editMnemonicMessageSignature?.text.toString(),
                addressOrPublicKey = Config.publicKey
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${ result ?: bladeJSError}")
                }
            }
        }

        binding!!.buttonContractCall.setOnClickListener {
            output("")
            val parameters = ContractFunctionParameters().addString("${binding?.editMnemonicMessageSignature?.text} ${System.currentTimeMillis()}")

            Blade.contractCallFunction(
                contractAddress = Config.contractId,
                functionName = "set_message",
                params = parameters,
                gas = 155000,
                usePaymaster = false
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${ result ?: bladeJSError}")
                }
            }
        }

        binding!!.buttonContractQuery.setOnClickListener {
            output("")
            val parameters = ContractFunctionParameters()

            Blade.contractCallQueryFunction(
                contractAddress = Config.contractId,
                functionName = "get_message",
                params = parameters,
                gas = 55000,
                usePaymaster = false,
                returnTypes = listOf("string", "int32")
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${ result ?: bladeJSError}")
                }
            }
        }


        binding!!.buttonTransferHbars.setOnClickListener {
            output("")
            Blade.transferBalance(
                receiverAddress = binding?.editTextReceiver?.text.toString(),
                amount = binding?.editTextAmount?.text.toString(),
                memo = "Test HBar transfer from Kotlin-Blade SDK"
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${ result ?: bladeJSError}")
                }
            }
        }

        binding!!.buttonTransferTokens.setOnClickListener {
            output("")
            Blade.transferTokens(
                tokenAddress = Config.tokenId,
                receiverAddress = binding?.editTextReceiver?.text.toString(),
                amountOrSerial = binding?.editTextAmount?.text.toString(),
                memo = "Test token transfer from Kotlin-Blade SDK"
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${ result ?: bladeJSError}")
                }
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}