package io.bladewallet.kotlin_sdk_demo.other

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.bladewallet.bladesdk.Blade
import io.bladewallet.kotlin_sdk_demo.Config
import io.bladewallet.kotlin_sdk_demo.databinding.FragmentOtherBinding
import kotlinx.coroutines.launch

class OtherFragment : Fragment() {

    private var _binding: FragmentOtherBinding? = null
    var startOperation: Long = System.currentTimeMillis()
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        _binding = FragmentOtherBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fun toggleElements(enable: Boolean): Boolean {
            binding.buttonGetCoinList.isEnabled = enable
            binding.buttonGetCoinPrice.isEnabled = enable
            binding.coinIdSpinner.isEnabled = enable
            binding.coinSearchEditText.isEnabled = enable
            return enable;
        }

        binding.coinSearchEditText.setText(Config.coinSearch)

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

        binding.buttonGetCoinList.setOnClickListener {
            output("")

            Blade.getCoinList() { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${result ?: bladeJSError}");

                    if (result != null) {
                        var coinIds = arrayOf<String>()
                        for (coin in result.coins) {
                            coinIds += coin.id;
                        }

                        object : ArrayAdapter<String>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            coinIds
                        ) {
                        }.also {
                            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            binding.coinIdSpinner.adapter = it
                            binding.coinIdSpinner.setSelection(0)

                            binding.coinIdSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                    val coinId = binding.coinIdSpinner.selectedItem as String
                                    binding.coinSearchEditText.setText(coinId)
                                    Config.coinSearch = coinId
                                }
                                override fun onNothingSelected(parent: AdapterView<*>?) {}
                            }
                        }
                    }
                }
            }

        }



        binding.buttonGetCoinPrice.setOnClickListener {
            output("");
            Blade.getCoinPrice(
                search = binding.coinSearchEditText.text.toString()
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