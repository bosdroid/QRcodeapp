package com.expert.qrgenerator.ui.fragments

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentVCardBinding
import com.expert.qrgenerator.ui.activities.BaseActivity
import com.expert.qrgenerator.ui.activities.BaseActivity.Companion.copyToClipboard
import com.expert.qrgenerator.ui.activities.BaseActivity.Companion.dismiss
import com.expert.qrgenerator.ui.activities.BaseActivity.Companion.showAlert
import com.expert.qrgenerator.ui.activities.BaseActivity.Companion.startLoading
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.GeneratorManager
import com.expert.qrgenerator.utils.ImageManager
import com.expert.qrgenerator.viewmodel.DynamicQrViewModel
import com.expert.qrgenerator.viewmodel.VCardViewModel
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class VCardFragment : Fragment() {

    private lateinit var binding:FragmentVCardBinding

    private val viewModel: VCardViewModel by viewModels()

    val regex = "^[a-zA-Z0-9-_]+$".toRegex()

    var existingVCard = false
    var shortUrl:String = ""
    var imageUrl:String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentVCardBinding.inflate(inflater, container, false)

        binding.dobInputField.setOnClickListener {
            openDatePickerDialog()
        }

        binding.nextStepBtn.setOnClickListener {

            if(validation()){


                val hashMap = hashMapOf<String, String>().apply {
                    put("user_id", Constants.userData!!.personId)
                    put("type", if(existingVCard) {"update"}else{"vcard"})
                    put("first_name", binding.firstNameInputField.text.toString())
                    put("last_name", binding.lastNameInputField.text.toString())
                    put("company_name", binding.companyNameInputField.text.toString())
                    put("job_title", binding.jobTitleInputField.text.toString())
                    put("dob", binding.dobInputField.text.toString())
                    put("phone", binding.phoneNumberInputField.text.toString())
                    put("email", binding.emailInputField.text.toString())
                    put("website", binding.websiteInputField.text.toString())
                    put("address", binding.addressInputField.text.toString())
                    put("file_name", binding.filenameInputField.text.toString())
                }

                startLoading(requireActivity())
                lifecycleScope.launch {
                    viewModel.createVCard(hashMap)
                    viewModel.vCardResponse.observe(viewLifecycleOwner) { event ->
                        event.getContentIfNotHandled()?.let { response ->
                            dismiss()
                            val status = response.get("status")?.asString
                            if (status == "success") {
                                val url = response.get("url")?.asString
                                Log.d("TEST1999", url as String)
                                GeneratorManager.generateQRCode(requireActivity(), url, "vcard",binding.filenameInputField.text.toString().trim(), vcardExist = existingVCard.toString())
                            } else {
                                val message = response.get("message")?.asString
                                showAlert(requireActivity(), message!!)
                            }
                        }
                    }
                }


            }
        }

        binding.editBtn.setOnClickListener {
            binding.vcardDesignLayoutWrapper.visibility = View.GONE
            binding.vcardCreateLayoutWrapper.visibility = View.VISIBLE
        }

        val hashMap = hashMapOf<String, String>().apply {
            put("user_id", Constants.userData!!.personId)
            put("type", "check")
            put("first_name", "")
            put("last_name", "")
            put("company_name", "")
            put("job_title", "")
            put("dob", "")
            put("phone", "")
            put("email", "")
            put("website", "")
            put("address", "")
            put("file_name", "")
        }

        startLoading(requireActivity())
        lifecycleScope.launch {
            viewModel.existVCard(hashMap)
            viewModel.existVCardResponse.observe(viewLifecycleOwner) { event ->
                event.getContentIfNotHandled()?.let { response ->
                    val status = response.get("status")?.asString
                    if (status == "success") {
                        val vcard = response.get("vcard")?.asJsonObject
                        binding.filenameInputField.isEnabled = false
                        binding.firstNameInputField.setText(vcard?.get("first_name")?.asString)
                        binding.lastNameInputField.setText(vcard?.get("last_name")?.asString)
                        binding.companyNameInputField.setText(vcard?.get("company_name")?.asString)
                        binding.jobTitleInputField.setText(vcard?.get("job_title")?.asString)
                        binding.dobInputField.setText(vcard?.get("dob")?.asString)
                        binding.phoneNumberInputField.setText(vcard?.get("phone")?.asString)
                        binding.emailInputField.setText(vcard?.get("email")?.asString)
                        binding.websiteInputField.setText(vcard?.get("website")?.asString)
                        binding.addressInputField.setText(vcard?.get("address")?.asString)
                        binding.filenameInputField.setText(vcard?.get("short_code")?.asString)

                        binding.firstNameView.text = vcard?.get("first_name")?.asString
                        binding.lastNameView.text = vcard?.get("last_name")?.asString
                        binding.companyNameView.text = vcard?.get("company_name")?.asString
                        binding.jobTitleView.text = vcard?.get("job_title")?.asString
                        binding.dateOfBirthView.text = vcard?.get("dob")?.asString
                        binding.phoneNumberView.text = vcard?.get("phone")?.asString
                        binding.emailTextView.text = vcard?.get("email")?.asString
                        binding.websiteTextView.text = vcard?.get("website")?.asString
                        binding.addressTextView.text = vcard?.get("address")?.asString
                        shortUrl = "${Constants.BASE_URL}${vcard?.get("short_code")?.asString}"
                        imageUrl = "${Constants.BASE_URL}vcards/images/${vcard?.get("short_code")?.asString}.jpg"
                        Glide.with(requireActivity())
                            .load(imageUrl) // image url
                            .placeholder(R.drawable.loader) // any placeholder to load at start
                            .override(120, 120) // resizing
                            .centerCrop()
                            .signature(ObjectKey(System.currentTimeMillis()))
                            .into(binding.qrCodeImageView)  // imageview object
                        dismiss()
                        existingVCard = true
                        binding.addressInputField.setImeOptions(EditorInfo.IME_ACTION_DONE)
                        binding.vcardDesignLayoutWrapper.visibility = View.VISIBLE
                        binding.vcardCreateLayoutWrapper.visibility = View.GONE
                    } else {
                        dismiss()
                        binding.addressInputField.setImeOptions(EditorInfo.IME_ACTION_NEXT)
                        binding.vcardDesignLayoutWrapper.visibility = View.GONE
                        binding.vcardCreateLayoutWrapper.visibility = View.VISIBLE
                        existingVCard = false
                    }
                }
            }
        }


        binding.copyShortUtlBtn.setOnClickListener {
            if(shortUrl.isNotEmpty()){
                copyToClipboard(requireActivity(),shortUrl)
            }
        }

        binding.shareQrImageBtn.setOnClickListener {
            startLoading(requireActivity())
            Picasso.get().load(imageUrl)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into(object : com.squareup.picasso.Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    if (bitmap != null) {
                        val imageFile = ImageManager.readWriteImage(requireActivity(),bitmap)
                        val imageUri: Uri = FileProvider.getUriForFile(
                            requireActivity(),
                            "${requireActivity().applicationContext.packageName}.fileprovider",
                            imageFile
                        )
                        dismiss()
                        shareImage(imageUri)
                    }
                }

                override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: android.graphics.drawable.Drawable?) {
                    // Handle the error
                }

                override fun onPrepareLoad(placeHolderDrawable: android.graphics.drawable.Drawable?) {
                    // Handle placeholder if needed
                }
            })
        }

        return binding.root
    }

    private fun shareImage(imageShareUri:Uri?) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            imageShareUri?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                putExtra(Intent.EXTRA_STREAM, it)
            }
        }
        shareResultLauncher.launch(
            Intent.createChooser(shareIntent, "Share with")
        )
    }

    // This launcher handles the result after sharing the QR image
    private val shareResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Handle the result if needed
        }

    private fun openDatePickerDialog() {
        // Get current date
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create a DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            requireActivity(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format the date and set it to the TextView
                val formattedDate = "${selectedDay}/${selectedMonth + 1}/$selectedYear"
                binding.dobInputField.setText(formattedDate)
            },
            year,
            month,
            day
        )

        // Show the DatePickerDialog
        datePickerDialog.show()
    }

    private fun validation():Boolean{

        if(binding.firstNameInputField.text.toString().isEmpty()){
            showAlert(
                requireActivity(),
                requireActivity().resources.getString(R.string.first_name_input_error)
            )
            return false
        }
        else if(binding.lastNameInputField.text.toString().isEmpty()){
            showAlert(
                requireActivity(),
                requireActivity().resources.getString(R.string.last_name_input_error)
            )
            return false
        }
        else if(binding.filenameInputField.text.toString().isEmpty()){
            showAlert(
                requireActivity(),
                requireActivity().resources.getString(R.string.file_name_input_error)
            )
            return false
        }
        else if(!regex.matches(binding.filenameInputField.text.toString())){
            showAlert(
                requireActivity(),
                requireActivity().resources.getString(R.string.file_name_valid_error)
            )
            return false
        }
        return true

    }

}