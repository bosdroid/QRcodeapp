package com.expert.qrgenerator.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentDynamicBinding
import com.expert.qrgenerator.databinding.FragmentStaticLinkBinding
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.ui.activities.BaseActivity
import com.expert.qrgenerator.ui.activities.BaseActivity.Companion.dismiss
import com.expert.qrgenerator.ui.activities.BaseActivity.Companion.showAlert
import com.expert.qrgenerator.ui.activities.BaseActivity.Companion.startLoading
import com.expert.qrgenerator.ui.activities.DesignActivity
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.Constants.Companion.openKeyboard
import com.expert.qrgenerator.utils.GeneratorManager
import com.expert.qrgenerator.viewmodel.DynamicQrViewModel
import com.expert.qrgenerator.viewmodel.VCardViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.regex.Pattern

@AndroidEntryPoint
class DynamicFragment : Fragment() {

    // ViewBinding instance for accessing views
    private lateinit var binding: FragmentDynamicBinding

    private val viewModel: DynamicQrViewModel by viewModels()
    private val appViewModel: AppViewModel by viewModels()

    // Variable to hold the encoded data for QR code generation
    private var encodedData: String = ""

    // Variable to store the selected URL protocol
    var selectedProtocol = "https://"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDynamicBinding.inflate(inflater, container, false)

        // Set up click listener for the 'Next Step' button
        binding.nextStepBtn.setOnClickListener {
            // Get and trim the input value from the website input field
            val value = binding.inputField.text.toString().trim()

            // Check if a protocol is selected
            if (selectedProtocol.isEmpty()) {
                BaseActivity.hideSoftKeyboard(requireActivity(), binding.inputField.rootView)
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
                val qrId = System.currentTimeMillis()
                val userId = Constants.userData?.personId
                val hashMap = hashMapOf<String, String>().apply {
                    put("login", "$userId")
                    put("qrId", "$qrId")
                    put("userUrl", encodedData)
                    put("userType", "free")
                }

                startLoading(requireActivity())
                lifecycleScope.launch {
                    viewModel.createDynamicQrCode(hashMap)
                }
                viewModel.dynamicQrCodeResponse.observe(requireActivity(), Observer { response ->
                    dismiss()
                    response?.let {
                        val genUrl = it.get("generatedUrl").asString
                        val qrHistory = CodeHistory(
                            "$userId",
                            "$qrId",
                            encodedData,
                            "dynamic",
                            "free",
                            "qr",
                            "create",
                            "",
                            "1",
                            genUrl,
                            System.currentTimeMillis().toString(),
                            ""
                        )
                        val insertedId = appViewModel.insert(qrHistory)
                        qrHistory.id = insertedId.toInt()
                        val intent = Intent(context, DesignActivity::class.java).apply {
                            // Add encoded data and QR history to the intent extras
                            putExtra("ENCODED_TEXT", genUrl)
                            putExtra("QR_HISTORY", qrHistory)
                        }

                        // Start the DesignActivity with the intent
                        startActivity(intent)
                    } ?: run {
                        showAlert(requireActivity(), "Something went wrong, please try again!")
                    }
                })
            }
        }

        // Request focus for the website input field and open the keyboard
        binding.inputField.requestFocus()
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