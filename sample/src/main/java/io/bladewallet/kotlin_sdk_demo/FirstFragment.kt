package io.bladewallet.kotlin_sdk_demo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.bladewallet.bladesdk.Blade
import io.bladewallet.bladesdk.BalanceDataResponse
import io.bladewallet.bladesdk.BladeJSError
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
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            Blade.initialize("1NpEy10UxlZ7AeqkuiCws3zJLPehQqvm7ahefmNF6wREULFGlm6rNtY/dKG6tmM", "karatecombat", "Testnet", requireContext()) {
                println("Init done!!!")
            }
        }

        binding.buttonSecond.setOnClickListener {
            Blade.getBalance("0.0.49177063") { data: BalanceDataResponse?, error: BladeJSError? ->
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