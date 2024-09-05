package com.expert.qrgenerator.ui.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentInstagramBinding
import com.expert.qrgenerator.ui.activities.BaseActivity
import com.expert.qrgenerator.utils.Constants.Companion.openKeyboard
import com.expert.qrgenerator.utils.GeneratorManager


class InstagramFragment : Fragment() {

    // ViewBinding instance to access the layout's views
    private lateinit var binding: FragmentInstagramBinding

    // Variable to store the encoded data for the Instagram URL
    private var encodedData: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment and initialize the ViewBinding
        binding = FragmentInstagramBinding.inflate(inflater, container, false)

        // Set an OnClickListener for the 'Next Step' button
        binding.nextStepBtn.setOnClickListener {
            // Hide the soft keyboard
            BaseActivity.hideSoftKeyboard(requireActivity(), binding.instagramInputField.rootView)

            // Check if the Instagram input field is not empty
            if (!TextUtils.isEmpty(binding.instagramInputField.text.toString())) {
                // Construct the Instagram URL with the provided username
                encodedData = "instagram://user?username=${binding.instagramInputField.text.toString().trim()}"

                // Generate a QR code using the constructed URL and a specified type
                GeneratorManager.generateQRCode(requireActivity(), encodedData, "instagram")
            } else {
                // Show an alert if the input field is empty
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.required_data_input_error)
                )
            }
        }

        // Request focus for the Instagram input field and open the keyboard
        binding.instagramInputField.requestFocus()
        openKeyboard(requireActivity())

        // Return the root view of the fragment
        return binding.root
    }
}
