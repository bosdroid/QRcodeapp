package com.expert.qrgenerator.ui.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentWifiBinding
import com.expert.qrgenerator.ui.activities.BaseActivity
import com.expert.qrgenerator.utils.Constants.Companion.openKeyboard
import com.expert.qrgenerator.utils.GeneratorManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WifiFragment : Fragment() {

    // ViewBinding instance for accessing the fragment's layout views
    private lateinit var binding: FragmentWifiBinding

    // Variables to hold the encoded Wi-Fi data and security type
    private var encodedData: String = ""
    private var wifiSecurity = "WPA"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment and initialize the ViewBinding
        binding = FragmentWifiBinding.inflate(inflater, container, false)

        // Set up a listener for changes in the security options radio group
        binding.securityGroup.setOnCheckedChangeListener { group, checkedId ->
            // Update the security type based on the selected radio button
            when (checkedId) {
                R.id.wpa -> {
                    wifiSecurity = "WPA"
                }
                R.id.wep -> {
                    wifiSecurity = "WEP"
                }
                R.id.none -> {
                    wifiSecurity = "nopass"
                }
                else -> {
                    // Handle any unexpected cases (not needed here)
                }
            }
        }

        // Set up a click listener for the "Next Step" button
        binding.nextStepBtn.setOnClickListener {
            // Check if both Wi-Fi name and password fields are not empty
            if (!TextUtils.isEmpty(binding.wifiNameInputField.text.toString()) && !TextUtils.isEmpty(
                    binding.wifiPasswordInputField.text.toString()
                )
            ) {
                // Hide the soft keyboard and generate the QR code
                BaseActivity.hideSoftKeyboard(requireActivity(), binding.wifiLayoutWrapper)
                encodedData =
                    "WIFI:T:$wifiSecurity;S:${
                        binding.wifiNameInputField.text.toString().trim()
                    };P:${binding.wifiPasswordInputField.text.toString().trim()};;"
                GeneratorManager.generateQRCode(requireActivity(), encodedData, "wifi")
            } else {
                // Show an alert if either the Wi-Fi name or password is missing
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.required_data_input_error)
                )
            }
        }

        // Request focus on the Wi-Fi name input field and open the keyboard
        binding.wifiNameInputField.requestFocus()
        openKeyboard(requireActivity())

        // Return the root view of the fragment
        return binding.root
    }

}
