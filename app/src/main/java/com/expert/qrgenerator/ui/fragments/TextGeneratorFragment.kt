package com.expert.qrgenerator.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentTextGeneratorBinding
import com.expert.qrgenerator.ui.activities.BaseActivity
import com.expert.qrgenerator.utils.Constants.Companion.openKeyboard
import com.expert.qrgenerator.utils.GeneratorManager


class TextGeneratorFragment : Fragment() {

    // View binding instance for the fragment
    private lateinit var binding: FragmentTextGeneratorBinding

    // Variable to store the encoded data
    private var encodedData: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using view binding
        binding = FragmentTextGeneratorBinding.inflate(inflater, container, false)

        // Set up the click listener for the 'Next Step' button
        binding.nextStepBtn.setOnClickListener {
            // Check if the text input field is not empty
            if (binding.textInputField.text.toString().isNotEmpty()) {
                // Hide the soft keyboard
                BaseActivity.hideSoftKeyboard(requireActivity(), binding.textInputField.rootView)

                // Get the text from the input field and store it in encodedData
                encodedData = binding.textInputField.text.toString()

                // Generate QR code with the input text
                GeneratorManager.generateQRCode(requireActivity(), encodedData, "text")
            } else {
                // Show an alert if the text input field is empty
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.required_data_input_error)
                )
            }
        }

        // Request focus for the text input field to show the keyboard
        binding.textInputField.requestFocus()

        // Open the soft keyboard
        openKeyboard(requireActivity())

        // Return the root view of the fragment
        return binding.root
    }

}
