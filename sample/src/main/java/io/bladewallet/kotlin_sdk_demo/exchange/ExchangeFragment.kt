package io.bladewallet.kotlin_sdk_demo.exchange

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.bladewallet.bladesdk.Blade
import io.bladewallet.bladesdk.models.CryptoFlowServiceStrategy
import io.bladewallet.kotlin_sdk_demo.Config
import io.bladewallet.kotlin_sdk_demo.databinding.FragmentExchangeBinding
import kotlinx.coroutines.launch

class ExchangeFragment : Fragment() {

    private var _binding: FragmentExchangeBinding? = null
    private var startOperation: Long = System.currentTimeMillis()
    private val binding get() = _binding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        _binding = FragmentExchangeBinding.inflate(inflater, container, false)
        return binding!!.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fun toggleElements(enable: Boolean): Boolean {
            binding!!.buttonQuotes.isEnabled = enable
            binding!!.editTextSource.isEnabled = enable
            binding!!.editTextAmount.isEnabled = enable
            binding!!.editTextTarget.isEnabled = enable
            binding!!.strategySpinner.isEnabled = enable
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

        if (binding != null) {
            val strategies = arrayOf("Buy", "Sell", "Swap")
            object : ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                strategies
            ) {
            }.also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding!!.strategySpinner.adapter = it
                binding!!.strategySpinner.setSelection(0)

                binding!!.strategySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                        when (position) {
                            0 -> { // buy
                                binding?.editTextSource?.setText("EUR")
                                binding?.editTextAmount?.setText("50")
                                binding?.editTextTarget?.setText("HBAR")
                            }
                            1 -> { // sell
                                binding?.editTextSource?.setText("USDC")
                                binding?.editTextAmount?.setText("30")
                                binding?.editTextTarget?.setText("PHP")
                            }
                            2 -> { // swap
                                binding?.editTextSource?.setText("HBAR")
                                binding?.editTextAmount?.setText("2")
                                binding?.editTextTarget?.setText("KARATE")
                            }
                        }

                        binding?.serviceSpinner?.isEnabled = false
                        binding?.buttonBuy?.isEnabled = false
                        binding?.buttonSell?.isEnabled = false
                        binding?.buttonSwap?.isEnabled = false
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }

        binding!!.buttonQuotes.setOnClickListener {
            output("")

            val strategy = CryptoFlowServiceStrategy.fromValue(binding?.strategySpinner?.selectedItem as String)
            if (strategy != null) {
                Blade.exchangeGetQuotes(
                    sourceCode = binding?.editTextSource?.text.toString(),
                    sourceAmount = binding?.editTextAmount?.text.toString().toDouble(),
                    targetCode = binding?.editTextTarget?.text.toString(),
                    strategy = strategy
                ) { result, bladeJSError ->
                    lifecycleScope.launch {
                        output("${result ?: bladeJSError}")

                        binding?.buttonBuy?.isEnabled = false
                        binding?.buttonSell?.isEnabled = false
                        binding?.buttonSwap?.isEnabled = false
                        binding?.serviceSpinner?.isEnabled = true

                        if (result != null && binding != null) {
                            var services = arrayOf<String>()
                            for (quote in result.quotes) {
                                services += quote.service.id
                            }

                            object : ArrayAdapter<String>(
                                requireContext(),
                                android.R.layout.simple_spinner_item,
                                services
                            ) {
                            }.also {
                                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                binding!!.serviceSpinner.adapter = it
                                binding!!.serviceSpinner.setSelection(0)

                                binding!!.serviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                        val strategy = CryptoFlowServiceStrategy.fromValue(binding!!.strategySpinner.selectedItem as String) ?: ""
                                        when (strategy) {
                                            CryptoFlowServiceStrategy.BUY -> {
                                                binding?.buttonBuy?.isEnabled = true
                                            }
                                            CryptoFlowServiceStrategy.SELL -> {
                                                binding?.buttonSell?.isEnabled = true
                                            }
                                            CryptoFlowServiceStrategy.SWAP -> {
                                                binding?.buttonSwap?.isEnabled = true
                                            }
                                        }
                                    }
                                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                                }
                            }
                        }
                    }
                }
            }
        }

        binding!!.buttonBuy.setOnClickListener {
            output("")
            Blade.getTradeUrl(
                strategy = CryptoFlowServiceStrategy.BUY,
                accountAddress = Config.accountAddress,
                sourceCode = binding?.editTextSource?.text.toString(),
                sourceAmount = binding?.editTextAmount?.text.toString().toDouble(),
                targetCode = binding?.editTextTarget?.text.toString(),
                slippage = 0.5,
                serviceId = binding!!.serviceSpinner.selectedItem as String
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${result ?: bladeJSError}")

                    if (result != null) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result.url))
                        startActivity(intent)
                    }
                }
            }
        }

        binding!!.buttonSell.setOnClickListener {
            output("")
            Blade.getTradeUrl(
                strategy = CryptoFlowServiceStrategy.SELL,
                accountAddress = Config.accountAddress,
                sourceCode = binding?.editTextSource?.text.toString(),
                sourceAmount = binding?.editTextAmount?.text.toString().toDouble(),
                targetCode = binding?.editTextTarget?.text.toString(),
                slippage = 0.5,
                serviceId = binding!!.serviceSpinner.selectedItem as String
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${result ?: bladeJSError}")

                    if (result != null) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result.url))
                        startActivity(intent)
                    }
                }
            }
        }

        binding!!.buttonSwap.setOnClickListener {
            output("")
            Blade.swapTokens(
                sourceCode = binding?.editTextSource?.text.toString(),
                sourceAmount = binding?.editTextAmount?.text.toString().toDouble(),
                targetCode = binding?.editTextTarget?.text.toString(),
                slippage = 0.5,
                serviceId = binding?.serviceSpinner?.selectedItem as String
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${result ?: bladeJSError}")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}