package com.expert.qrgenerator.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentStaticLinkGeneratorBinding
import com.expert.qrgenerator.ui.activities.BaseActivity
import com.expert.qrgenerator.utils.Constants.Companion.openKeyboard
import com.expert.qrgenerator.utils.GeneratorManager
import java.util.regex.Pattern


class StaticLinkGeneratorFragment : Fragment() {

    // ViewBinding instance for accessing views
    private lateinit var binding: FragmentStaticLinkGeneratorBinding

    // Variable to hold the encoded data for QR code generation
    private var encodedData: String = ""

    // Variable to store the selected URL protocol
    var selectedProtocol = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStaticLinkGeneratorBinding.inflate(inflater, container, false)

        // Set the heading text for the dialog
        binding.dialogHeading.text = requireActivity().getString(R.string.generator_type_description_static_link)

        // Set up click listener for the 'Next Step' button
        binding.nextStepBtn.setOnClickListener {
            // Get and trim the input value from the website input field
            val value = binding.websiteInputField.text.toString().trim()

            // Check if a protocol is selected
            if (selectedProtocol.isEmpty()) {
                BaseActivity.hideSoftKeyboard(requireActivity(), binding.websiteInputField.rootView)
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.protocol_error)
                )
            }
            // Check if the input field is empty
            else if (value.isEmpty()) {
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.required_data_input_error)
                )
            }
            // Check if the input contains 'http://' or 'https://'
            else if (value.contains("http://") || value.contains("https://")) {
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.without_protocol_error)
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
                encodedData = "$selectedProtocol$value"
                GeneratorManager.generateQRCode(requireActivity(), encodedData, "link")
            }
        }

        // Request focus for the website input field and open the keyboard
        binding.websiteInputField.requestFocus()
        openKeyboard(requireActivity())

        // Set up a listener for changes in the protocol selection
        binding.httpProtocolGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.http_protocol_rb -> {
                    selectedProtocol = "http://"
                }
                R.id.https_protocol_rb -> {
                    selectedProtocol = "https://"
                }
                else -> {
                    // Handle other cases if necessary
                }
            }
        }

        return binding.root
    }
}
