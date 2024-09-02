package com.expert.qrgenerator.ui.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentPhoneGeneratorBinding
import com.expert.qrgenerator.ui.activities.BaseActivity
import com.expert.qrgenerator.utils.Constants.Companion.openKeyboard
import com.expert.qrgenerator.utils.GeneratorManager


class PhoneGeneratorFragment : Fragment() {

    // ViewBinding instance for this fragment
    private lateinit var binding: FragmentPhoneGeneratorBinding
    // Variable to hold the encoded phone number data
    private var encodedData: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment and initialize the binding
        binding = FragmentPhoneGeneratorBinding.inflate(inflater, container, false)

        // Set up the click listener for the 'Next Step' button
        binding.nextStepBtn.setOnClickListener {
            // Check if all phone number input fields are filled
            if (!TextUtils.isEmpty(binding.phoneCcInputField.text.toString())
                && !TextUtils.isEmpty(binding.phoneStartNumberInputField.text.toString())
                && !TextUtils.isEmpty(binding.phoneNumberInputField.text.toString())
            ) {
                // Hide the soft keyboard
                BaseActivity.hideSoftKeyboard(requireActivity(), binding.wrapperLayout)

                // Construct the full phone number by concatenating input fields
                val phoneNumber =
                    "+${binding.phoneCcInputField.text.toString()}${binding.phoneStartNumberInputField.text.toString()}${binding.phoneNumberInputField.text.toString()}"

                // Encode the phone number into a tel URI format
                encodedData = "tel:$phoneNumber"

                // Generate a QR code with the encoded phone number
                GeneratorManager.generateQRCode(requireActivity(), encodedData, "phone")

            } else {
                // Show an alert if any of the input fields are empty
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.required_data_input_error)
                )
            }
        }

        // Set focus to the country code input field
        binding.phoneCcInputField.requestFocus()
        // Open the keyboard for the user to start typing
        openKeyboard(requireActivity())

        // Return the root view of the fragment
        return binding.root
    }

}