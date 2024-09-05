package com.expert.qrgenerator.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentCalendarBinding
import com.expert.qrgenerator.databinding.FragmentEmailBinding
import com.expert.qrgenerator.ui.activities.BaseActivity
import com.expert.qrgenerator.utils.Constants.Companion.openKeyboard
import com.expert.qrgenerator.utils.GeneratorManager


class EmailFragment : Fragment() {

    private lateinit var binding:FragmentEmailBinding

    // Variable to hold the encoded data for QR code generation
    private var encodedData: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentEmailBinding.inflate(inflater, container, false)

        // Set up click listener for the 'Next Step' button
        binding.nextStepBtn.setOnClickListener {

            val recipient = binding.emailRecipientInputField.text.toString().trim()
            val subject = binding.subjectInputField.text.toString().trim()
            val body = binding.bodyTextInputField.text.toString().trim()


            if (recipient.isEmpty()) {
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.required_data_input_error)
                )
            }
            else if (subject.isEmpty()) {
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.required_data_input_error)
                )
            }
            else if (body.isEmpty()) {
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.required_data_input_error)
                )
            }
            else {
                encodedData = "mailto:$recipient?subject=$subject&body=$body"
                GeneratorManager.generateQRCode(requireActivity(), encodedData, "email")
            }
        }

        binding.emailRecipientInputField.requestFocus()
        openKeyboard(requireActivity())

        return binding.root
    }

}