package com.expert.qrgenerator.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentCalendarBinding
import com.expert.qrgenerator.databinding.FragmentGoogleReviewBinding
import com.expert.qrgenerator.ui.activities.BaseActivity
import com.expert.qrgenerator.utils.Constants.Companion.openKeyboard
import com.expert.qrgenerator.utils.GeneratorManager
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern

@AndroidEntryPoint
class GoogleReviewFragment : Fragment() {

    private lateinit var binding:FragmentGoogleReviewBinding

    // Variable to hold the encoded data for QR code generation
    private var encodedData: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentGoogleReviewBinding.inflate(inflater, container, false)

        // Set up click listener for the 'Next Step' button
        binding.nextStepBtn.setOnClickListener {
            // Get and trim the input value from the website input field
            val value = binding.textInputField.text.toString().trim()

            // Check if the input field is empty
            if (value.isEmpty()) {
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.required_data_input_error)
                )
            }
            // Validate the URL format using a regular expression
            else if (!Pattern.compile("^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?\$").matcher(value).find()) {
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.valid_website_error)
                )
            }
            // If all validations pass, encode the data and generate a QR code
            else {
                encodedData = value
                GeneratorManager.generateQRCode(requireActivity(), encodedData, "google-review")
            }
        }

        // Request focus for the website input field and open the keyboard
        binding.textInputField.requestFocus()
        openKeyboard(requireActivity())

        return binding.root
    }

}