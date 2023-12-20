package io.bladewallet.kotlin_sdk_demo.init

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
import io.bladewallet.bladesdk.BladeEnv
import io.bladewallet.kotlin_sdk_demo.Config
import io.bladewallet.kotlin_sdk_demo.databinding.FragmentInitBinding
import kotlinx.coroutines.launch

class InitFragment : Fragment() {

    private var _binding: FragmentInitBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        var startOperation: Long = System.currentTimeMillis();

        _binding = FragmentInitBinding.inflate(inflater, container, false)
        val root: View = binding.root

        fun toggleElements(enable: Boolean): Boolean {
            binding.dAppCodeEditText.isEnabled = enable
            binding.apiTokenEditText.isEnabled = enable
            binding.networkSpinner.isEnabled = enable
            binding.bladeEnvSpinner.isEnabled = enable
            binding.accountIdEditText.isEnabled = enable
            binding.privateKeyEditText.isEnabled = enable
            binding.publicKeyEditText.isEnabled = enable
            binding.contractIdEditText.isEnabled = enable
            binding.tokenIdEditText.isEnabled = enable
            binding.initButton.isEnabled = enable
            return enable;
        }

        @SuppressLint("SetTextI18n")
        fun output(text: String) {
            binding.textTitleOutput.setText("Output:");
            if (text == "") {
                startOperation = System.currentTimeMillis();
                binding.progressBar.visibility = View.VISIBLE;
            } else {
                println(text)
                if (binding.outputTextView.text.toString() == "") {
                    binding.textTitleOutput.setText("Output (${System.currentTimeMillis() - startOperation}ms):");
                }
                binding.progressBar.visibility = View.GONE;
            }
            binding.outputTextView.setText(text)
        }

        Blade.getInfo { infoData, bladeJSError ->
            lifecycleScope.launch {
                if (infoData != null) {
                    binding.stopButton.isEnabled = true
                    toggleElements(false)
                    output("$infoData")
                } else {
                    toggleElements(true)
                    binding.stopButton.isEnabled = false
                    output("$bladeJSError")
                }
            }
        }

        binding.initButton.setOnClickListener {
            Config.dAppCode = binding.dAppCodeEditText.text.toString();
            Config.apiKey = binding.apiTokenEditText.text.toString();
            Config.accountId = binding.accountIdEditText.text.toString();
            Config.privateKey = binding.privateKeyEditText.text.toString();
            Config.publicKey = binding.publicKeyEditText.text.toString();
            Config.contractId = binding.contractIdEditText.text.toString();
            Config.tokenId = binding.tokenIdEditText.text.toString();

            toggleElements(false);
            output("")

            Blade.initialize(
                Config.apiKey,
                Config.dAppCode,
                Config.network,
                Config.bladeEnv,
                requireContext(),
                false
            ) { infoData, bladeJSError ->
                lifecycleScope.launch {
                    if (infoData != null) {
                        binding.stopButton.isEnabled = true
                        output("$infoData")
                    } else {
                        toggleElements(true)
                        output("$bladeJSError")
                    }
                }
            }
        }

        binding.stopButton.setOnClickListener {
            Blade.cleanup()
            output("Blade stopped")
            binding.stopButton.isEnabled = !toggleElements(true)
        }

        val items = arrayOf("Select Network", "Mainnet", "Testnet")
        object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            items
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                if (position == 0) {
                    view.alpha = 0.5f
                }
                return view
            }
        }.also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.networkSpinner.adapter = it
            binding.networkSpinner.setSelection(items.indexOf(Config.network).coerceAtLeast(0))

            binding.networkSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    Config.network = items[position]
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }



        val bladeEnvs = arrayOf("Select BladeEnv", "Prod", "CI")
        object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            bladeEnvs
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                if (position == 0) {
                    view.alpha = 0.5f
                }
                return view
            }
        }.also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.bladeEnvSpinner.adapter = it
            binding.bladeEnvSpinner.setSelection(bladeEnvs.indexOf(Config.bladeEnv.value).coerceAtLeast(0))

            binding.bladeEnvSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    Config.bladeEnv = if (bladeEnvs[position] === BladeEnv.Prod.toString()) BladeEnv.Prod else BladeEnv.CI
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        binding.dAppCodeEditText.setText(Config.dAppCode);
        binding.apiTokenEditText.setText(Config.apiKey);
        binding.accountIdEditText.setText(Config.accountId);
        binding.privateKeyEditText.setText(Config.privateKey);
        binding.publicKeyEditText.setText(Config.publicKey);
        binding.contractIdEditText.setText(Config.contractId);
        binding.tokenIdEditText.setText(Config.tokenId);
        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
