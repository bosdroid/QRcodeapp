package com.expert.qrgenerator.ui.activities

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.ActivityUpdateVcardBinding
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.GeneratorManager
import com.expert.qrgenerator.viewmodel.VCardViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class UpdateVCardActivity : BaseActivity() {

    private lateinit var binding:ActivityUpdateVcardBinding
    private lateinit var context: Context
    private val viewModel: VCardViewModel by viewModels()
    var existingVCard = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateVcardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views and set up toolbar
        initViews()
        setUpToolbar()

    }

    private fun initViews(){
        context = this
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

        startLoading(context)
        lifecycleScope.launch {
            viewModel.existVCard(hashMap)
            viewModel.existVCardResponse.observe(this@UpdateVCardActivity) { event ->
                event.getContentIfNotHandled()?.let { response ->
                    dismiss()
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
                        existingVCard = true
                    } else {
                        existingVCard = false
                    }
                }
            }
        }


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

                startLoading(context)
                lifecycleScope.launch {
                    viewModel.createVCard(hashMap)
                    viewModel.vCardResponse.observe(this@UpdateVCardActivity) { event ->
                        event.getContentIfNotHandled()?.let { response ->
                            dismiss()
                            val status = response.get("status")?.asString
                            if (status == "success") {
                                val url = response.get("url")?.asString
                                Log.d("TEST1999", url as String)
                                GeneratorManager.generateQRCode(context, url, "vcard",binding.filenameInputField.text.toString().trim(), vcardExist = existingVCard.toString())
                            } else {
                                val message = response.get("message")?.asString
                                showAlert(context, message!!)
                            }
                        }
                    }
                }


            }
        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.update_vcard_text)
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    private fun openDatePickerDialog() {
        // Get current date
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create a DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            context,
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
                context,
                resources.getString(R.string.first_name_input_error)
            )
            return false
        }
        else if(binding.lastNameInputField.text.toString().isEmpty()){
            showAlert(
                context,
                resources.getString(R.string.last_name_input_error)
            )
            return false
        }
        else if(binding.filenameInputField.text.toString().isEmpty()){
            showAlert(
               context,
                resources.getString(R.string.file_name_input_error)
            )
            return false
        }
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}