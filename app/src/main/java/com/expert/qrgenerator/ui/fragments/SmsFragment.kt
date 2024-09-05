package com.expert.qrgenerator.ui.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentSmsBinding
import com.expert.qrgenerator.ui.activities.BaseActivity
import com.expert.qrgenerator.utils.Constants.Companion.openKeyboard
import com.expert.qrgenerator.utils.GeneratorManager


class SmsFragment : Fragment() {

    // Late initialization for the view binding
    private lateinit var binding: FragmentSmsBinding
    private var encodedData: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using ViewBinding
        binding = FragmentSmsBinding.inflate(inflater, container, false)

        // Set up the click listener for the "Next Step" button
        binding.nextStepBtn.setOnClickListener {
            // Check if all required input fields are filled
            if (areInputsValid()) {
                // Hide the soft keyboard
                BaseActivity.hideSoftKeyboard(requireActivity(), binding.smsLayoutWrapper)

                // Construct the phone number and encoded data
                val phoneNumber = getFormattedPhoneNumber()
                encodedData = "smsto:$phoneNumber:${binding.smsMessageInputField.text.toString().trim()}"

                // Generate the QR code with the encoded SMS data
                GeneratorManager.generateQRCode(requireActivity(), encodedData, "sms")
            } else {
                // Show an alert if any input field is empty
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.required_data_input_error)
                )
            }
        }

        // Request focus on the phone country code input field and open the keyboard
        binding.smsPhoneCcInputField.requestFocus()
        openKeyboard(requireActivity())

        return binding.root
    }

    // Function to check if all input fields are filled
    private fun areInputsValid(): Boolean {
        return !TextUtils.isEmpty(binding.smsPhoneCcInputField.text.toString())
                && !TextUtils.isEmpty(binding.smsPhoneStartNumberInputField.text.toString())
                && !TextUtils.isEmpty(binding.smsPhoneNumberInputField.text.toString())
                && !TextUtils.isEmpty(binding.smsMessageInputField.text.toString())
    }

    // Function to format the phone number from input fields
    private fun getFormattedPhoneNumber(): String {
        return "+${binding.smsPhoneCcInputField.text.toString()}" +
                "${binding.smsPhoneStartNumberInputField.text.toString()}" +
                "${binding.smsPhoneNumberInputField.text.toString()}"
    }

}
