package io.bladewallet.kotlin_sdk_demo.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.bladewallet.kotlin_sdk_demo.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
                ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textDashboard
//        dashboardViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }

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

            binding.networkSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedItem = items[position]
                    Toast.makeText(requireContext(), "Selected: $selectedItem", Toast.LENGTH_SHORT).show()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Handle the case where nothing is selected
                }
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

            binding.bladeEnvSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedItem = bladeEnvs[position]
                    Toast.makeText(requireContext(), "Selected: $selectedItem", Toast.LENGTH_SHORT).show()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Handle the case where nothing is selected
                }
            }
        }




//
//        ArrayAdapter(
//            requireContext(),
//            android.R.layout.simple_spinner_dropdown_item,
//            arrayOf("Select BladeEnv", "Prod", "CI"),
//        ).also { adapter ->
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            binding.bladeEnvSpinner.adapter = adapter
//
//
//
//        }



        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
