package com.expert.qrgenerator.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentUtmBuilderBinding
import com.expert.qrgenerator.ui.activities.BaseActivity
import com.expert.qrgenerator.utils.Constants.Companion.openKeyboard
import com.expert.qrgenerator.utils.GeneratorManager
import java.util.regex.Pattern


class UtmBuilderFragment : Fragment() {

    private lateinit var binding: FragmentUtmBuilderBinding

    // Variable to hold the encoded data for QR code generation
    private var encodedData: String = ""
    private var websiteUrl: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUtmBuilderBinding.inflate(inflater, container, false)

        binding.nextStepBtn.setOnClickListener {
            val websiteUrl = binding.websiteUrlInputField.text.toString()
            val utmSource = binding.utmSourceInputField.text.toString()
            val utmMedium = binding.utmMediumInputField.text.toString()
            val utmCampaign = binding.utmCampaignInputField.text.toString()

            if (validation(websiteUrl, utmSource, utmMedium, utmCampaign)) {
                encodedData =
                    "${websiteUrl}/?utm_source=${utmSource}&utm_medium=${utmMedium}&utm_campaign=${utmCampaign}"

                GeneratorManager.generateQRCode(requireActivity(), encodedData, "utm")

            }
        }

        binding.websiteUrlInputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                websiteUrl = s.toString()
                if (!websiteUrl.endsWith("/")) {
                    websiteUrl = "$websiteUrl/"
                }
                else{
                    websiteUrl = "${s.toString()}/"
                }

                if (binding.utmSourceInputField.text.toString().isNotEmpty() && binding.utmMediumInputField.text.toString()
                        .isNotEmpty() && binding.utmCampaignInputField.text.toString().isNotEmpty()
                ) {
                    encodedData =
                        "${websiteUrl}?utm_source=${binding.utmSourceInputField.text.toString()}&utm_medium=${binding.utmMediumInputField.text.toString()}&utm_campaign=${binding.utmCampaignInputField.text.toString()}"
                    binding.utmGeneratedUrlInputField.setText(encodedData)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.utmSourceInputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (websiteUrl.isNotEmpty() && binding.utmMediumInputField.text.toString()
                        .isNotEmpty() && binding.utmCampaignInputField.text.toString().isNotEmpty()
                ) {
                    encodedData =
                        "${websiteUrl}?utm_source=${binding.utmSourceInputField.text.toString()}&utm_medium=${binding.utmMediumInputField.text.toString()}&utm_campaign=${s.toString()}"
                    binding.utmGeneratedUrlInputField.setText(encodedData)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.utmMediumInputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (websiteUrl.isNotEmpty() && binding.utmSourceInputField.text.toString()
                        .isNotEmpty() && binding.utmCampaignInputField.text.toString().isNotEmpty()
                ) {
                    encodedData =
                        "${websiteUrl}?utm_source=${binding.utmSourceInputField.text.toString()}&utm_medium=${binding.utmMediumInputField.text.toString()}&utm_campaign=${s.toString()}"
                    binding.utmGeneratedUrlInputField.setText(encodedData)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.utmCampaignInputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                encodedData =
                    "${websiteUrl}?utm_source=${binding.utmSourceInputField.text.toString()}&utm_medium=${binding.utmMediumInputField.text.toString()}&utm_campaign=${s.toString()}"
                binding.utmGeneratedUrlInputField.setText(encodedData)
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        // Request focus for the website input field and open the keyboard
        binding.websiteUrlInputField.requestFocus()
        openKeyboard(requireActivity())

        return binding.root
    }

    private fun validation(
        websiteUrl: String,
        utmSource: String,
        utmMedium: String,
        utmCampaign: String
    ): Boolean {
        if (websiteUrl.isEmpty() || utmSource.isEmpty() || utmMedium.isEmpty() || utmCampaign.isEmpty()) {
            BaseActivity.showAlert(
                requireActivity(),
                requireActivity().getString(R.string.required_data_input_error)
            )
            return false
        }
        // Validate the URL format using a regular expression
        else if (!Pattern.compile("^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?\$")
                .matcher(websiteUrl).find()
        ) {
            BaseActivity.showAlert(
                requireActivity(),
                requireActivity().resources.getString(R.string.valid_website_error)
            )
        }
        return true
    }
}