package com.expert.qrgenerator.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentCalendarBinding
import com.expert.qrgenerator.databinding.FragmentMapBinding
import com.expert.qrgenerator.ui.activities.BaseActivity
import com.expert.qrgenerator.utils.Constants.Companion.openKeyboard
import com.expert.qrgenerator.utils.GeneratorManager


class MapFragment : Fragment() {

    private lateinit var binding:FragmentMapBinding

    // Variable to hold the encoded data for QR code generation
    private var encodedData: String = ""

    private var baseUrl = "https://www.google.com/maps/place/@"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentMapBinding.inflate(inflater, container, false)

        // Set up click listener for the 'Next Step' button
        binding.nextStepBtn.setOnClickListener {

            val locationUrl = binding.googleMapLinkInputField.text.toString().trim()
            val latitude = binding.latitudeInputField.text.toString().trim()
            val longitude = binding.longitudeInputField.text.toString().trim()


            if (latitude.isEmpty()) {
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.required_data_input_error)
                )
            }
            else if (longitude.isEmpty()) {
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.required_data_input_error)
                )
            }
            else {
                encodedData = locationUrl
                GeneratorManager.generateQRCode(requireActivity(), encodedData, "map")
            }
        }

        binding.latitudeInputField.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(binding.longitudeInputField.text.toString().isNotEmpty()){
                    binding.googleMapLinkInputField.setText("$baseUrl${binding.latitudeInputField.text.toString()},${binding.longitudeInputField.text.toString()},8z")
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.longitudeInputField.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(binding.latitudeInputField.text.toString().isNotEmpty()){
                    binding.googleMapLinkInputField.setText("$baseUrl${binding.latitudeInputField.text.toString()},${binding.longitudeInputField.text.toString()},8z")
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.latitudeInputField.requestFocus()
        openKeyboard(requireActivity())

        return binding.root
    }

}