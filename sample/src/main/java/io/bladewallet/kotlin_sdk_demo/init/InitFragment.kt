package io.bladewallet.kotlin_sdk_demo.init

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.bladewallet.bladesdk.Blade
import io.bladewallet.bladesdk.models.AccountProvider
import io.bladewallet.bladesdk.models.BladeEnv
import io.bladewallet.bladesdk.models.KnownChainIds
import io.bladewallet.kotlin_sdk_demo.Config
import io.bladewallet.kotlin_sdk_demo.databinding.FragmentInitBinding
import kotlinx.coroutines.launch

class InitFragment : Fragment() {

    private var _binding: FragmentInitBinding? = null

    private val binding get() = _binding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        var startOperation: Long = System.currentTimeMillis()

        _binding = FragmentInitBinding.inflate(inflater, container, false)
        val root: View = binding!!.root

        fun toggleElements(enable: Boolean): Boolean {
            binding!!.dAppCodeEditText.isEnabled = enable
            binding!!.apiTokenEditText.isEnabled = enable
            binding!!.chainSpinner.isEnabled = enable
            binding!!.bladeEnvSpinner.isEnabled = enable
            binding!!.initButton.isEnabled = enable
            return enable
        }

        fun toggleActiveUser(bladeInitialised: Boolean, activeUser: Boolean): Boolean {
            binding!!.accountIdEditText.isEnabled = !bladeInitialised || !activeUser
            binding!!.privateKeyEditText.isEnabled = !bladeInitialised || !activeUser
            binding!!.magicEmailEditText.isEnabled = !bladeInitialised || !activeUser
            binding!!.accountProviderSpinner.isEnabled = !bladeInitialised || !activeUser
            binding!!.setUserButton.isEnabled = bladeInitialised && !activeUser
            binding!!.resetUserButton.isEnabled = bladeInitialised && activeUser
            return activeUser;
        }

        @SuppressLint("SetTextI18n")
        fun output(text: String) {
            binding?.textTitleOutput?.text = "Output:"
            if (text == "") {
                startOperation = System.currentTimeMillis()
                binding?.progressBar?.visibility = View.VISIBLE
            } else {
                println(text)
                if (binding?.outputTextView?.text.toString() == "") {
                    binding?.textTitleOutput?.text = "Output (${System.currentTimeMillis() - startOperation}ms):"
                }
                binding?.progressBar?.visibility = View.GONE
            }
            binding?.outputTextView?.text = text
        }

        Blade.getInfo { infoData, bladeJSError ->
            lifecycleScope.launch {
                if (infoData != null) {
                    binding?.stopButton?.isEnabled = true
                    toggleElements(false)

                    if (infoData.user.userPublicKey != "") {
                        toggleActiveUser(true, true)
                    } else {
                        toggleActiveUser(true, false)
                    }

                    output("$infoData")
                } else {
                    toggleElements(true)
                    binding?.stopButton?.isEnabled = false
                    toggleActiveUser(false, false)
                    output("$bladeJSError")
                }
            }
        }

        binding!!.initButton.setOnClickListener {
            Config.dAppCode = binding?.dAppCodeEditText?.text.toString()
            Config.apiKey = binding?.apiTokenEditText?.text.toString()
            toggleElements(false)
            output("")

            Blade.initialize(
                Config.apiKey,
                Config.chainId,
                Config.dAppCode,
                Config.bladeEnv,
                requireContext(),
                false
            ) { infoData, bladeJSError ->
                lifecycleScope.launch {
                    if (infoData != null) {
                        binding?.stopButton?.isEnabled = true
                        toggleActiveUser(true, false)
                        output("$infoData")
                    } else {
                        toggleElements(true)
                        output("$bladeJSError")
                    }
                }
            }
        }

        binding!!.stopButton.setOnClickListener {
            Blade.cleanup()
            output("Blade stopped")
            binding?.stopButton?.isEnabled = !toggleElements(true)
            toggleActiveUser(false, false)
        }

        binding!!.setUserButton.setOnClickListener {
            Blade.setUser(
                Config.accountProvider,
                if (Config.accountProvider === AccountProvider.PrivateKey) Config.accountAddress else Config.magicEmail,
                if (Config.accountProvider === AccountProvider.PrivateKey) Config.accountPrivateKey  else "",
            ) { userInfoData, bladeJSError ->
                lifecycleScope.launch {
                    if (userInfoData != null && userInfoData.userPublicKey != "") {
                        toggleActiveUser(true, true)
                        output("$userInfoData")
                    } else {
                        toggleElements(true)
                        output("$bladeJSError")
                    }
                }
            }
        }

        binding!!.resetUserButton.setOnClickListener {
            Blade.resetUser { userInfoData, bladeJSError ->
                lifecycleScope.launch {
                    if (userInfoData != null && userInfoData.userPublicKey == "") {
                        toggleActiveUser(true, false)
                        output("$userInfoData")
                    } else {
                        toggleElements(true)
                        output("$bladeJSError")
                    }
                }
            }
        }

        if (binding != null) {
            val items = arrayOf("Select Chain").plus(
                KnownChainIds.values().map { it.name }
            )

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
                binding!!.chainSpinner.adapter = it
                binding!!.chainSpinner.setSelection(items.indexOf(Config.chainId.name).coerceAtLeast(0))

                binding!!.chainSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if (position > 0) {
                            Config.setChain(KnownChainIds.fromKey(items[position]))

                            binding!!.accountIdEditText.setText(Config.accountAddress)
                            binding!!.privateKeyEditText.setText(Config.accountPrivateKey)
                            binding!!.contractIdEditText.setText(Config.contractAddress)
                            binding!!.tokenIdEditText.setText(Config.tokenAddress)
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }

            val bladeEnvs = arrayOf("Select BladeEnv").plus(
                BladeEnv.values().map { it.name }
            )
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
                binding!!.bladeEnvSpinner.adapter = it
                binding!!.bladeEnvSpinner.setSelection(bladeEnvs.indexOf(Config.bladeEnv.name).coerceAtLeast(0))

                binding!!.bladeEnvSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if (position > 0) {
                            Config.bladeEnv = BladeEnv.fromKey(bladeEnvs[position])
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }



            val accountProviders = arrayOf("Select Account provider").plus(
                AccountProvider.values().map { it.name }
            )
            object : ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                accountProviders
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
                binding!!.accountProviderSpinner.adapter = it
                binding!!.accountProviderSpinner.setSelection(accountProviders.indexOf(Config.accountProvider.name).coerceAtLeast(0))

                binding!!.accountProviderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if (position > 0) {
                            Config.accountProvider = AccountProvider.fromKey(accountProviders[position])
                        }
                        if (Config.accountProvider === AccountProvider.PrivateKey) {
                            binding!!.accountProviderPrivateKey.visibility = VISIBLE
                            binding!!.accountProviderMagic.visibility = GONE
                        } else {
                            binding!!.accountProviderPrivateKey.visibility = GONE
                            binding!!.accountProviderMagic.visibility = VISIBLE
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }

        binding!!.dAppCodeEditText.setText(Config.dAppCode)
        binding!!.apiTokenEditText.setText(Config.apiKey)
        binding!!.magicEmailEditText.setText(Config.magicEmail)

        binding!!.accountIdEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    Config.accountAddress = s.toString();
                }
            }
        })

        binding!!.privateKeyEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    Config.accountPrivateKey = s.toString();
                }
            }
        })

        binding!!.magicEmailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    Config.magicEmail = s.toString();
                }
            }
        })

        binding!!.contractIdEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    Config.contractAddress = s.toString();
                }
            }
        })

        binding!!.tokenIdEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    Config.tokenAddress = s.toString();
                }
            }
        })

        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
