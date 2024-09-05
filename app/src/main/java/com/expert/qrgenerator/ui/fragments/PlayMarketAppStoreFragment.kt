package com.expert.qrgenerator.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentCalendarBinding
import com.expert.qrgenerator.databinding.FragmentPlayMarketAppStoreBinding
import com.expert.qrgenerator.ui.activities.BaseActivity
import com.expert.qrgenerator.utils.Constants.Companion.openKeyboard
import com.expert.qrgenerator.utils.GeneratorManager


class PlayMarketAppStoreFragment : Fragment() {

    private lateinit var binding:FragmentPlayMarketAppStoreBinding

    // Variable to hold the encoded data for QR code generation
    private var encodedData: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentPlayMarketAppStoreBinding.inflate(inflater, container, false)

        // Set up click listener for the 'Next Step' button
        binding.nextStepBtn.setOnClickListener {

            val linkAppStore = binding.linkAppstoreInputField.text.toString().trim()
            val linkGooglePlay = binding.linkGooglePlayInputField.text.toString().trim()
            val linkOtherDevices = binding.linkOtherDevicesInputField.text.toString().trim()


//            if (linkAppStore.isEmpty()) {
//                BaseActivity.showAlert(
//                    requireActivity(),
//                    requireActivity().resources.getString(R.string.required_data_input_error)
//                )
//            }
//            else
                if (linkGooglePlay.isEmpty()) {
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.required_data_input_error)
                )
            }
//            else if (linkOtherDevices.isEmpty()) {
//                BaseActivity.showAlert(
//                    requireActivity(),
//                    requireActivity().resources.getString(R.string.required_data_input_error)
//                )
//            }
            else {
                encodedData = linkGooglePlay
                GeneratorManager.generateQRCode(requireActivity(), encodedData, "google-play")
            }
        }

        binding.linkAppstoreInputField.requestFocus()
        openKeyboard(requireActivity())

        return binding.root
    }

}