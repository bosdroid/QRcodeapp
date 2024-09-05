package com.expert.qrgenerator.ui.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentContactBinding
import com.expert.qrgenerator.ui.activities.BaseActivity
import com.expert.qrgenerator.utils.Constants.Companion.openKeyboard
import com.expert.qrgenerator.utils.GeneratorManager


class ContactFragment : Fragment() {

    // Binding object for the fragment's layout
    private lateinit var binding: FragmentContactBinding

    // Variable to hold the encoded vCard data
    private var encodedData: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the fragment's layout and initialize the binding object
        binding = FragmentContactBinding.inflate(inflater, container, false)

        // Set up a click listener for the 'Next Step' button
        binding.nextStepBtn.setOnClickListener {

            // Check if all required input fields are filled
            if (!TextUtils.isEmpty(binding.contactNameInputField.text.toString())
                && !TextUtils.isEmpty(binding.contactPhoneNumberInputField.text.toString())
                && !TextUtils.isEmpty(binding.contactPhoneCcInputField.text.toString())
                && !TextUtils.isEmpty(binding.contactPhoneStartNumberInputField.text.toString())
            ) {
                // Hide the soft keyboard when the button is clicked
                BaseActivity.hideSoftKeyboard(requireActivity(), binding.contactLayoutWrapper)

                // Construct the phone number from separate fields
                val phoneNumber =
                    "+${binding.contactPhoneCcInputField.text.toString()}${binding.contactPhoneStartNumberInputField.text.toString()}${binding.contactPhoneNumberInputField.text.toString()}"

                // Encode the contact data in vCard format
                encodedData =
                    "BEGIN:VCARD\nVERSION:4.0\nN:${
                        binding.contactNameInputField.text.toString().trim()
                    }\nTEL:${
                        phoneNumber
                    }\nTITLE:${
                        binding.contactJobInputField.text.toString().trim()
                    }\nEMAIL:${
                        binding.contactEmailInputField.text.toString().trim()
                    }\nORG:${
                        binding.contactCompanyInputField.text.toString().trim()
                    }\nADR;TYPE=HOME;PREF=1;LABEL:;;${
                        binding.contactAddressInputField.text.toString().trim()
                    };;;;\nNOTE:${
                        binding.contactDetailInputField.text.toString().trim()
                    }\nEND:VCARD"

                // Generate a QR code for the contact data
                GeneratorManager.generateQRCode(requireActivity(), encodedData, "contact")

            } else {
                // Show an alert if any required field is empty
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.required_data_input_error)
                )
            }
        }

        // Set focus to the contact name input field and open the keyboard
        binding.contactNameInputField.requestFocus()
        openKeyboard(requireActivity())

        // Return the root view of the fragment's layout
        return binding.root
    }
}
