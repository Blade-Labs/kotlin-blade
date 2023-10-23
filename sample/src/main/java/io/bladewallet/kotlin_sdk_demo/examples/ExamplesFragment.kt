package io.bladewallet.kotlin_sdk_demo.examples

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.bladewallet.bladesdk.AccountInfoData
import io.bladewallet.bladesdk.Blade
import io.bladewallet.bladesdk.CreatedAccountData
import io.bladewallet.kotlin_sdk_demo.Config
import io.bladewallet.kotlin_sdk_demo.databinding.FragmentExamplesBinding
import kotlinx.coroutines.launch
import java.util.Base64

class ExamplesFragment : Fragment() {

    private var _binding: FragmentExamplesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        var startOperation: Long = System.currentTimeMillis()
        var temporaryAccount: CreatedAccountData? = null

        _binding = FragmentExamplesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        fun toggleElements(enable: Boolean): Boolean {
            binding.editAccountId.isEnabled = enable
            binding.buttonBalance.isEnabled = enable
            binding.buttonTransactions.isEnabled = enable
            binding.buttonCreateAccount.isEnabled = enable
//            binding.buttonDeleteAccount.isEnabled = enable
            binding.editMnemonicMessageSignature.isEnabled = enable
            binding.buttonGetFromMnemonic.isEnabled = enable
            binding.buttonSign.isEnabled = enable
            binding.buttonVerify.isEnabled = enable
//            binding.initButton.isEnabled = enable
            return enable;
        }

        @SuppressLint("SetTextI18n")
        fun output(text: String) {
            if (text == "") {
                startOperation = System.currentTimeMillis();
                binding.textTitleOutput.setText("Output:");
                binding.progressBar.visibility = View.VISIBLE;
            } else {
                println(text)
                binding.textTitleOutput.setText("Output (${System.currentTimeMillis() - startOperation}ms):");
                binding.progressBar.visibility = View.GONE;
            }
            binding.outputTextView.setText(text)
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

        binding.editAccountId.setText(Config.accountId);
        binding.editMnemonicMessageSignature.setText(Config.mnemonic);

        binding.buttonBalance.setOnClickListener {
            output("")
            Blade.getBalance(
                binding.editAccountId.text.toString()
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${ result ?: bladeJSError}");
                }
            }
        }

        binding.buttonTransactions.setOnClickListener {
            output("")
            Blade.getTransactions(
                accountId = binding.editAccountId.text.toString(),
                transactionType = "",
                nextPage = "",
                transactionsLimit = 15
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${ result ?: bladeJSError}");
                }
            }
        }

        binding.buttonCreateAccount.setOnClickListener {
            output("")
            Blade.createHederaAccount(deviceId = "") { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${ result ?: bladeJSError}");
                    if (result != null) {
                        temporaryAccount = result
                        binding.buttonDeleteAccount.isEnabled = true
                    }
                }
            }
        }

        binding.buttonDeleteAccount.setOnClickListener {
            output("")
            Blade.deleteHederaAccount(
                deleteAccountId = temporaryAccount?.accountId.toString(),
                deletePrivateKey = temporaryAccount?.privateKey.toString(),
                transferAccountId = binding.editAccountId.text.toString(),
                operatorAccountId = binding.editAccountId.text.toString(),
                operatorPrivateKey = Config.privateKey,
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${ result ?: bladeJSError}");
                    if (result != null) {
                        temporaryAccount = null
                        binding.buttonDeleteAccount.isEnabled = false
                    }
                }
            }
        }

        binding.buttonGetFromMnemonic.setOnClickListener {
            output("")
            Blade.getKeysFromMnemonic(
                mnemonic = binding.editMnemonicMessageSignature.text.toString(),
                lookupNames = true
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${ result ?: bladeJSError}");
                }
            }
        }

        binding.buttonSign.setOnClickListener {
            output("")
            val originalString = binding.editMnemonicMessageSignature.text.toString()
            val encodedString: String = Base64.getEncoder().encodeToString(originalString.toByteArray())

            Blade.sign(
                messageString = encodedString,
                privateKey = Config.privateKey
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${ result ?: bladeJSError}");
                }
            }
        }

        binding.buttonVerify.setOnClickListener {
            output("")
            val originalString = Config.message // hello
            val encodedString: String = Base64.getEncoder().encodeToString(originalString.toByteArray())

            Blade.signVerify(
                messageString = encodedString,
                signature = binding.editMnemonicMessageSignature.text.toString(),
                // signature = "27cb9d51434cf1e76d7ac515b19442c619f641e6fccddbf4a3756b14466becb6992dc1d2a82268018147141fc8d66ff9ade43b7f78c176d070a66372d655f942",
                publicKey = Config.publicKey
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${ result ?: bladeJSError}");
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