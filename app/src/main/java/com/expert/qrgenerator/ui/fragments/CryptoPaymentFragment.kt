package com.expert.qrgenerator.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentCalendarBinding
import com.expert.qrgenerator.databinding.FragmentCryptoPaymentBinding
import com.expert.qrgenerator.ui.activities.BaseActivity
import com.expert.qrgenerator.utils.Constants.Companion.openKeyboard
import com.expert.qrgenerator.utils.GeneratorManager


class CryptoPaymentFragment : Fragment() {

    private lateinit var binding:FragmentCryptoPaymentBinding

    // Variable to hold the encoded data for QR code generation
    private var encodedData: String = ""

    // Variable to store the selected cryptocurrency
    var cryptoCurrency = "bitcoin"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentCryptoPaymentBinding.inflate(inflater, container, false)

        // Set up click listener for the 'Next Step' button
        binding.nextStepBtn.setOnClickListener {

            val amount = binding.amountInputField.text.toString().trim()
            val address = binding.receiverCryptoAddressInputField.text.toString().trim()
            val message = binding.textMessageInputField.text.toString().trim()


            if (amount.isEmpty()) {
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.required_data_input_error)
                )
            }
            else if (address.isEmpty()) {
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.required_data_input_error)
                )
            }
            else if (message.isEmpty()) {
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.required_data_input_error)
                )
            }
            else {
                encodedData = "$cryptoCurrency:$address?amount=$amount&message=$message"
                GeneratorManager.generateQRCode(requireActivity(), encodedData, "crypto_payments")
            }
        }

        // Set up a listener for changes in the protocol selection
        binding.cryptoCurrencyGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.bitcoin_rb -> {
                    cryptoCurrency = "bitcoin"
                }
                R.id.ethereum_rb -> {
                    cryptoCurrency = "ethereum"
                }
                else -> {
                    // Handle other cases if necessary
                }
            }
        }

        // Request focus for the website input field and open the keyboard
        binding.amountInputField.requestFocus()
        openKeyboard(requireActivity())

        return binding.root
    }

}