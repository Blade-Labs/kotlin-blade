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
    private var startOperation: Long = System.currentTimeMillis()
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
            binding.scheduleIdEditText.isEnabled = enable
            binding.buttonSignScheduledTx.isEnabled = enable
            return enable
        }

        binding.coinSearchEditText.setText(Config.coinSearch)

        @SuppressLint("SetTextI18n")
        fun output(text: String) {
            if (text == "") {
                startOperation = System.currentTimeMillis()
                binding.textTitleOutput.text = "Output:"
                binding.progressBar.visibility = View.VISIBLE
            } else {
                println(text)
                binding.textTitleOutput.text = "Output (${System.currentTimeMillis() - startOperation}ms):"
                binding.progressBar.visibility = View.GONE
            }
            binding.outputTextView.text = text
        }

        Blade.getInfo { infoData, bladeJSError ->
            lifecycleScope.launch {
                if (infoData != null) {
                    toggleElements(true)
                    output("$infoData")

                    Blade.getAccountInfo(Config.accountId) { accountInfoData, bladeJSError ->
                        lifecycleScope.launch {
                            if (accountInfoData != null) {
                                var res = "${accountInfoData.accountId}\nStaked? - "
                                res += if (accountInfoData.stakingInfo.stakedNodeId !== null) {
                                    "YES"
                                } else {
                                    "NO"
                                }
                                binding.accountStatusTitle.text = res
                                Config.stakedNodeId = accountInfoData.stakingInfo.stakedNodeId

                                Blade.getNodeList { nodeListData, bladeJSError ->
                                    lifecycleScope.launch {
                                        if (nodeListData != null) {
                                            binding.nodeSpinner.isEnabled = true
                                            binding.buttonUpdateAccount.isEnabled = true

                                            var nodes = arrayOf<String>()
                                            nodes += "-1: UNSTAKE"
                                            for (node in nodeListData.nodes) {
                                                nodes += "${node.node_id}: ${node.description}"
                                            }

                                            var activeNode = 0
                                            for ((i, node) in nodes.withIndex()) {
                                                val nodeId = node.substringBefore(":").trim().toInt()
                                                println("nodeId: ${nodeId}... Config.stakedNodeId: ${Config.stakedNodeId}")
                                                if (nodeId == (Config.stakedNodeId ?: -1)) {
                                                    println("activeNode: $i")
                                                    activeNode = i
                                                }
                                            }

                                            object : ArrayAdapter<String>(
                                                requireContext(),
                                                android.R.layout.simple_spinner_item,
                                                nodes
                                            ) {
                                            }.also {
                                                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                                                binding.nodeSpinner.adapter = it
                                                binding.nodeSpinner.setSelection(activeNode)

                                                binding.nodeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                                        Config.stakedNodeId = binding.nodeSpinner.selectedItem.toString().substringBefore(":").trim().toInt()
                                                    }
                                                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                                                }
                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    toggleElements(false)
                    output("$bladeJSError")
                }
            }
        }

        binding.buttonSignScheduledTx.setOnClickListener {
            output("")

            Blade.signScheduleId(
                scheduleId = binding.scheduleIdEditText.text.toString(),
                accountId = Config.accountId,
                accountPrivateKey = Config.privateKey
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${result ?: bladeJSError}")
                }
            }
        }

        binding.buttonGetCoinList.setOnClickListener {
            output("")

            Blade.getCoinList { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${result ?: bladeJSError}")

                    if (result != null) {
                        var coinIds = arrayOf<String>()
                        for (coin in result.coins) {
                            coinIds += coin.id
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
            output("")
            Blade.getCoinPrice(
                search = binding.coinSearchEditText.text.toString(),
                currency = "uah"
            ) { result, bladeJSError ->
                lifecycleScope.launch {
                    output("${result ?: bladeJSError}")
                }
            }
        }

        binding.buttonUpdateAccount.setOnClickListener {
            output("")

            Blade.stakeToNode(Config.accountId, Config.privateKey, Config.stakedNodeId ?: -1) { result, bladeJSError ->
                lifecycleScope.launch {
                    if (result != null) {
                        var res = "${Config.accountId}\nStaked? - "
                        res += if (Config.stakedNodeId === null || Config.stakedNodeId!!.toInt() < 0) {
                            "NO"
                        } else {
                            "YES"
                        }
                        binding.accountStatusTitle.text = res
                    }
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