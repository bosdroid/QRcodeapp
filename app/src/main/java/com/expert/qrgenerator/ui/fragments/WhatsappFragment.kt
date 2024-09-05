package com.expert.qrgenerator.ui.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentWhatsappBinding
import com.expert.qrgenerator.ui.activities.BaseActivity
import com.expert.qrgenerator.utils.Constants.Companion.openKeyboard
import com.expert.qrgenerator.utils.GeneratorManager


class WhatsappFragment : Fragment() {

    // ViewBinding for accessing UI components
    private lateinit var binding: FragmentWhatsappBinding
    // Variable to hold the encoded WhatsApp URL
    private var encodedData: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using ViewBinding
        binding = FragmentWhatsappBinding.inflate(inflater, container, false)

        // Set an OnClickListener for the "Next Step" button
        binding.nextStepBtn.setOnClickListener {
            // Check if all phone number input fields are not empty
            if (!TextUtils.isEmpty(binding.whatsappPhoneCcInputField.text.toString())
                && !TextUtils.isEmpty(binding.whatsappPhoneStartNumberInputField.text.toString())
                && !TextUtils.isEmpty(binding.whatsappPhoneNumberInputField.text.toString())
            ) {
                // Hide the soft keyboard
                BaseActivity.hideSoftKeyboard(requireActivity(), binding.wrapperLayout)

                // Concatenate the phone number parts to form a complete phone number
                val phone =
                    "+${binding.whatsappPhoneCcInputField.text.toString()}${binding.whatsappPhoneStartNumberInputField.text.toString()}${binding.whatsappPhoneNumberInputField.text.toString()}"

                // Check if the phone number starts with a "+"
                if (phone.substring(0, 1) == "+") {
                    // Encode the phone number into a WhatsApp URL
                    encodedData = "whatsapp://send?phone=$phone"
                    // Generate a QR code for the WhatsApp URL
                    GeneratorManager.generateQRCode(requireActivity(), encodedData, "whatsapp")
                } else {
                    // Show an error alert if the phone number does not start with "+"
                    BaseActivity.showAlert(
                        requireActivity(),
                        requireActivity().resources.getString(R.string.country_code_data_input_error)
                    )
                }
            } else {
                // Show an error alert if any input field is empty
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.required_data_input_error)
                )
            }
        }

        // Request focus on the country code input field
        binding.whatsappPhoneCcInputField.requestFocus()
        // Open the keyboard to allow user input
        openKeyboard(requireActivity())

        // Return the root view of the fragment
        return binding.root
    }
}
