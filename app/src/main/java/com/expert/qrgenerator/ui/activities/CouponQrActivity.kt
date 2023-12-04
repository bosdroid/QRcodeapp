package com.expert.qrgenerator.ui.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.DatePicker
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.ActivityCouponQrBinding
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.singleton.DriveService
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.ImageManager
import com.expert.qrgenerator.utils.RuntimePermissionHelper
import com.expert.qrgenerator.viewmodel.CouponQrViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.api.client.http.FileContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.defaults.colorpicker.ColorPickerPopup
import top.defaults.colorpicker.ColorPickerPopup.ColorPickerObserver
import java.io.File
import java.util.*

@AndroidEntryPoint
class CouponQrActivity : BaseActivity(), View.OnClickListener, DatePickerDialog.OnDateSetListener {
    private lateinit var binding:ActivityCouponQrBinding
    private lateinit var context: Context

    private var selectedSaleBadgeText: String = "SALE"
    private var selectedRedeemButtonText: String = "Redeem Now"
    private var updateType = ""

    private var couponCompanyNameText: String = ""
    private var couponCompanyNameTextColor: String = ""
    private var couponBackgroundColor: String = ""
    private var couponHeaderImage: String = ""
    private var couponSaleBadgeButtonText: String = ""
    private var couponSaleBadgeButtonColor: String = ""
    private var couponOfferTitleText: String = ""
    private var couponOfferTitleTextColor: String = ""
    private var couponOfferDescriptionText: String = ""
    private var couponOfferDescriptionTextColor: String = ""
    private var couponGetButtonText: String = ""
    private var couponGetButtonColor: String = ""
    private var couponCodeText: String = ""
    private var couponCodeTextColor: String = ""
    private var couponValidDate: String = ""
    private var couponTermsConditionText: String = ""
    private var couponRedeemButtonText: String = ""
    private var couponRedeemButtonColor: String = ""
    private var couponRedeemWebsiteUrl: String = ""
    private val viewModel: CouponQrViewModel by viewModels()
    private var page = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCouponQrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setUpToolbar()
    }

    // THIS FUNCTION WILL INITIALIZE ALL THE VIEWS AND REFERENCE OF OBJECTS
    private fun initViews() {
        context = this

        binding.nextBtn.setOnClickListener(this)
        binding.nextBtn.paintFlags = binding.nextBtn.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        binding.couponCodeLayoutCloseBtn.setOnClickListener(this)

        binding.companyNameEditBtn.setOnClickListener(this)

        binding.backgroundColorEditBtn.setOnClickListener(this)

        binding.headerImageEditBtn.setOnClickListener(this)

        binding.saleBadgeEditBtn.setOnClickListener(this)

        binding.headlineTextEditBtn.setOnClickListener(this)

        binding.descriptionTextEditBtn.setOnClickListener(this)

        binding.getCouponEditBtn.setOnClickListener(this)

        binding.redeemNowEditBtn.setOnClickListener(this)


        binding.couponCodeEditBtn.setOnClickListener(this)

        binding.couponValidTillEditBtn.setOnClickListener(this)

        binding.couponTermsConditionEditBtn.setOnClickListener(this)

        binding.couponTermsCondition.setOnClickListener(this)

        binding.nextStepBtn.setOnClickListener(this)


    }

    // THIS FUNCTION WILL RENDER THE ACTION BAR/TOOLBAR
    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = getString(R.string.coupon_qr)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    // THIS FUNCTION WILL HANDLE THE ON BACK ARROW CLICK EVENT
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            if (couponBackgroundColor.isEmpty()
                && couponCompanyNameText.isEmpty()
                && couponHeaderImage.isEmpty()
                && couponSaleBadgeButtonText.isEmpty()
                && couponOfferTitleText.isEmpty()
                && couponOfferDescriptionText.isEmpty()
                && couponGetButtonText.isEmpty()
            ) {
                onBackPressed()
            } else {


                    MaterialAlertDialogBuilder(context)
                        .setMessage(getString(R.string.changes_saved_alert_text))
                        .setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                            dialog.dismiss()
                        }
                        .setPositiveButton(getString(R.string.leave_text)) { dialog, which ->
                            onBackPressed()
                        }
                        .create().show()

            }
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    // THIS FUNCTION WILL HANDLE THE ALL BUTTONS CLICK EVENT
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.next_step_btn -> {

                if (couponBackgroundColor.isNotEmpty()
                    && couponCompanyNameText.isNotEmpty()
                    && couponHeaderImage.isNotEmpty()
                    && couponSaleBadgeButtonText.isNotEmpty()
                    && couponOfferTitleText.isNotEmpty()
                    && couponOfferDescriptionText.isNotEmpty()
                    && couponGetButtonText.isNotEmpty() && page == 1
                ) {
                    binding.couponDesignLayout.visibility = View.GONE
                    binding.couponNextDesignLayout.visibility = View.VISIBLE
                    binding.nextBtn.visibility = View.VISIBLE
                    page = 2
                } else if (page == 2) {
                    if (validation()) {
                        val hashMap = hashMapOf<String, String>()
                        hashMap["coupon_company_name"] = couponCompanyNameText
                        hashMap["coupon_company_name_color"] = couponCompanyNameTextColor
                        hashMap["coupon_background_color"] = couponBackgroundColor
                        hashMap["coupon_header_image"] = couponHeaderImage
                        hashMap["coupon_sale_badge_button_text"] = couponSaleBadgeButtonText
                        hashMap["coupon_sale_badge_button_color"] = couponSaleBadgeButtonColor
                        hashMap["coupon_headline_text"] = couponOfferTitleText
                        hashMap["coupon_headline_text_color"] = couponOfferTitleTextColor
                        hashMap["coupon_description_text"] = couponOfferDescriptionText
                        hashMap["coupon_description_text_color"] = couponOfferDescriptionTextColor
                        hashMap["coupon_get_button_text"] = couponGetButtonText
                        hashMap["coupon_get_button_color"] = couponGetButtonColor
                        hashMap["coupon_code_text"] = couponCodeText
                        hashMap["coupon_code_text_color"] = couponCodeTextColor
                        hashMap["coupon_valid_date"] = couponValidDate
                        hashMap["coupon_terms_condition_text"] = couponTermsConditionText
                        hashMap["coupon_redeem_button_text"] = couponRedeemButtonText
                        hashMap["coupon_redeem_button_color"] = couponRedeemButtonColor
                        hashMap["coupon_redeem_website_url"] = couponRedeemWebsiteUrl

                        startLoading(context)
                        viewModel.createCouponQrCode(hashMap)
                        viewModel.getCouponQrCode().observe(this, { response ->
                            var url = ""
                            dismiss()
                            if (response != null) {
                                Log.d("TEST199", response.toString())
                                url = response.get("generatedUrl").asString

                                // SETUP QR DATA HASMAP FOR HISTORY
                                val qrData = hashMapOf<String, String>()
                                qrData["login"] = "qrmagicapp"
                                qrData["qrId"] = "${System.currentTimeMillis()}"
                                qrData["userType"] = "free"

                                val qrHistory = CodeHistory(
                                    qrData["login"]!!,
                                    qrData["qrId"]!!,
                                    url,
                                    "coupon",
                                    qrData["userType"]!!,
                                    "qr",
                                    "create",
                                    "",
                                    "0",
                                    "",
                                    System.currentTimeMillis().toString(),
                                    ""
                                )

                                val intent = Intent(context, DesignActivity::class.java)
                                intent.putExtra("ENCODED_TEXT", url)
                                intent.putExtra("QR_HISTORY", qrHistory)
                                startActivity(intent)

                            } else {
                                showAlert(context, getString(R.string.something_wrong_error))
                            }
                        })

                    }
                } else {
                    showAlert(context, getString(R.string.fields_marked_with_sign_text))
                }

            }
            R.id.next_btn -> {

                binding.couponNextDesignLayout.visibility = View.GONE
                binding.couponDesignLayout.visibility = View.VISIBLE
                binding.nextBtn.visibility = View.GONE
                page = 1


            }
            R.id.coupon_code_layout_close_btn -> {
                binding.couponNextDesignLayout.visibility = View.GONE
                binding.couponDesignLayout.visibility = View.VISIBLE
                binding.nextBtn.visibility = View.GONE
                page = 1
            }
            R.id.company_name_edit_btn -> {
                updateType = "company"
                updateText(binding.couponCompanyName, 1)
            }
            R.id.background_color_edit_btn -> {
                updateType = "background_color"
                openColorDialog(binding.couponWrapperLayout)
            }
            R.id.header_image_edit_btn -> {
                if (RuntimePermissionHelper.checkStoragePermission(
                        context,
                        Constants.READ_STORAGE_PERMISSION
                    )
                ) {
                    getImageFromLocalStorage()
                }
            }
            R.id.sale_badge_edit_btn -> {
                updateType = "sale_badge"
                updateTextAndColor(binding.couponSaleBadge, 0)
            }
            R.id.headline_text_edit_btn -> {
                updateType = "headline"
                updateText(binding.couponContentHeadline, 1)
            }
            R.id.description_text_edit_btn -> {
                updateType = "description"
                updateText(binding.couponContentDescription, 1)
            }
            R.id.get_coupon_edit_btn -> {
                updateType = "get_coupon_btn"
                updateTextAndColor(binding.getCouponBtn, 1)
            }
            R.id.redeem_now_edit_btn -> {
                updateRedeemButton(binding.redeemNowBtn)
            }
            R.id.coupon_code_edit_btn -> {
                updateType = "coupon_code"
                updateText(binding.couponCodeTextView, 1)
            }
            R.id.coupon_valid_till_edit_btn -> {
                val c = Calendar.getInstance()
                val year = c[Calendar.YEAR]
                val month = c[Calendar.MONTH]
                val day = c[Calendar.DAY_OF_MONTH]

                DatePickerDialog(context, this, year, month, day).show()
            }
            R.id.coupon_terms_condition_edit_btn -> {
                updateType = "terms_conditions"
                updateText(binding.couponTermsConditionDisplayTextView, 0)
            }
            R.id.coupon_terms_condition -> {
                if (binding.termsConditionDisplayWrapperLayout.visibility == View.GONE) {
                    binding.termsConditionDisplayWrapperLayout.visibility = View.VISIBLE
                } else {
                    binding.termsConditionDisplayWrapperLayout.visibility = View.GONE
                }
            }
            else -> {

            }
        }
    }

    // THIS FUNCTION WILL VALIDATE ALL THE COUPON INPUT DATA
    private fun validation(): Boolean {
        if (couponCompanyNameText.isEmpty()) {
            showAlert(context, getString(R.string.company_name_error_text))
            return false
        } else if (couponBackgroundColor.isEmpty()) {
            showAlert(context, getString(R.string.background_color_error_text))
            return false
        } else if (couponHeaderImage.isEmpty()) {
            showAlert(context, getString(R.string.header_image_error_text))
            return false
        } else if (couponSaleBadgeButtonText.isEmpty()) {
            showAlert(context, getString(R.string.sale_badge_text_error_text))
            return false
        } else if (couponSaleBadgeButtonColor.isEmpty()) {
            showAlert(context, getString(R.string.sale_badge_button_color_error_text))
            return false
        } else if (couponOfferTitleText.isEmpty()) {
            showAlert(context, getString(R.string.coupon_headline_error_text))
            return false
        } else if (couponOfferDescriptionText.isEmpty()) {
            showAlert(context, getString(R.string.coupon_description_error_text))
            return false
        } else if (couponGetButtonText.isEmpty()) {
            showAlert(context, getString(R.string.coupon_get_button_text_error_text))
            return false
        } else if (couponGetButtonColor.isEmpty()) {
            showAlert(context, getString(R.string.coupon_get_button_color_error_text))
            return false
        } else if (couponCodeText.isEmpty()) {
            showAlert(context, getString(R.string.coupon_code_text_error_text))
            return false
        } else if (couponCodeTextColor.isEmpty()) {
            showAlert(context, getString(R.string.coupon_code_text_color_error_text))
            return false
        } else if (couponValidDate.isEmpty()) {
            showAlert(context, getString(R.string.coupon_valid_date_error_text))
            return false
        } else if (couponTermsConditionText.isEmpty()) {
            showAlert(context, getString(R.string.coupon_terms_condition_error_text))
            return false
        } else if (couponRedeemButtonText.isEmpty()) {
            showAlert(context, getString(R.string.redeem_button_text_error_text))
            return false
        } else if (couponRedeemButtonColor.isEmpty()) {
            showAlert(context, getString(R.string.redeem_button_color_error_text))
            return false
        } else if (couponRedeemWebsiteUrl.isEmpty()) {
            showAlert(context, getString(R.string.redeem_target_website_error_text))
            return false
        }
        return true
    }

    // THIS FUNCTION WILL CALL THE IMAGE INTENT
    private fun getImageFromLocalStorage() {
        val fileIntent = Intent(Intent.ACTION_PICK)
        fileIntent.type = "image/*"
        resultLauncher.launch(fileIntent)
    }

    // THIS RESULT LAUNCHER WILL CALL THE ACTION PICK FROM FILES FOR BACKGROUND AND LOGO IMAGE
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {

                val data: Intent? = result.data
                val size = ImageManager.getImageWidthHeight(context, data!!.data!!)
                val imageWidth = size.split(",")[0].toInt()
                val imageHeight = size.split(",")[1].toInt()
                if (imageWidth > 640 && imageHeight > 360) {
                    showAlert(
                        context,
                        getString(R.string.header_image_size_error_text)
                    )
                } else {
                    couponHeaderImage = ImageManager.convertImageToBase64(context, data.data!!)
                    val path = ImageManager.getRealPathFromUri(context, data.data!!)
                    //uploadOnDrive(path!!)
                    // THIS LINES OF CODE WILL RE SCALED THE IMAGE WITH ASPECT RATION AND SIZE 640 X 360
                    val bitmapImage = BitmapFactory.decodeFile(
                        ImageManager.getRealPathFromUri(
                            context,
                            data.data!!
                        )
                    )
                    val nh = (bitmapImage.height * (640.0 / bitmapImage.width)).toInt()
                    val scaled = Bitmap.createScaledBitmap(bitmapImage, 640, nh, true)
                    binding.couponSaleImage.setImageBitmap(scaled)
                    binding.headerImageEditBtn.visibility = View.GONE
                    binding.lavHeaderImageEditBtn.visibility = View.GONE
                    binding.headerImageEditBtn.setImageResource(R.drawable.green_checked_icon)
                }

            }
        }

    private fun uploadOnDrive(path: String) {

        CoroutineScope(Dispatchers.IO).launch {

            if (DriveService.instance != null) {
                val fileMetadata = com.google.api.services.drive.model.File()
                fileMetadata.name = "Image_${System.currentTimeMillis()}.jpg"
                val filePath: File = File(path)
                val mediaContent = FileContent("image/jpeg", filePath)
                val file: com.google.api.services.drive.model.File =
                    DriveService.instance!!.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute()
                Log.e("File ID: ", file.id)
                Log.d("TEST199", "https://drive.google.com/file/d/" + file.id + "/view?usp=sharing")
            }
        }

    }

    // THIS FUNCTION WILL HANDLE THE RUNTIME PERMISSION RESULT
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            Constants.READ_STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImageFromLocalStorage()
                } else {
                    MaterialAlertDialogBuilder(context)
                        .setMessage(getString(R.string.external_storage_permission_error1))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.ok_text)) { dialog, which ->
                            dialog.dismiss()
                        }
                        .create().show()
                }
            }
            else -> {

            }
        }
    }

    // THIS FUNCTION WILL HANDLE THE REDEEM BUTTON DATA
    private fun updateRedeemButton(view: AppCompatButton) {
        val redeemLayout = LayoutInflater.from(context).inflate(R.layout.redeem_button_dialog, null)
        val cancelBtn = redeemLayout.findViewById<MaterialButton>(R.id.redeem_dialog_cancel_btn)
        val updateBtn = redeemLayout.findViewById<MaterialButton>(R.id.redeem_dialog_update_btn)
        val colorBtnView = redeemLayout.findViewById<AppCompatButton>(R.id.redeem_color_btn)
        val colorTextField = redeemLayout.findViewById<TextInputEditText>(R.id.redeem_color_tf)
        val redeemTextSpinner =
            redeemLayout.findViewById<AppCompatSpinner>(R.id.redeem_text_selector)
        val redeemCustomInputBox =
            redeemLayout.findViewById<TextInputEditText>(R.id.redeem_text_input_field)
        val redeemWebsiteUrl = redeemLayout.findViewById<TextInputEditText>(R.id.redeem_website_url)
        var selectedColor = ""
        if (couponRedeemButtonColor.isEmpty()) {
            selectedColor = colorTextField.text.toString()
        } else {
            selectedColor = couponRedeemButtonColor
            colorTextField.setText(selectedColor)
            colorBtnView.setBackgroundColor(Color.parseColor(selectedColor))
        }
        if (couponRedeemButtonText.isNotEmpty()) {
            redeemCustomInputBox.setText(couponRedeemButtonText)
        }
        if (couponRedeemWebsiteUrl.isNotEmpty()) {
            redeemWebsiteUrl.setText(couponRedeemWebsiteUrl)
        }
        selectedRedeemButtonText = binding.redeemNowBtn.text.toString()
        val listOptions = resources.getStringArray(R.array.redeem_options)
        if (getPositionFromText(listOptions, selectedSaleBadgeText) == -1) {
            redeemCustomInputBox.visibility = View.VISIBLE
        } else {
            redeemCustomInputBox.visibility = View.GONE
        }
        redeemTextSpinner.setSelection(getPositionFromText(listOptions, selectedRedeemButtonText))
        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(redeemLayout)
        builder.setCancelable(false)
        val alert = builder.create()
        alert.show()

        cancelBtn.setOnClickListener { alert.dismiss() }
        updateBtn.setOnClickListener {
            if (redeemWebsiteUrl.text.toString().toLowerCase(Locale.ENGLISH).contains("https://")
                || redeemWebsiteUrl.text.toString().toLowerCase(Locale.ENGLISH).contains("http://")
            ) {
                showAlert(context, getString(R.string.without_protocol_error))
            } else {
                couponRedeemButtonText = selectedRedeemButtonText
                couponRedeemButtonColor = selectedColor
                couponRedeemWebsiteUrl = redeemWebsiteUrl.text.toString().trim()
                view.setBackgroundColor(Color.parseColor(selectedColor))
                view.text = selectedRedeemButtonText
                binding.redeemNowEditHint.visibility = View.GONE
                binding.lavRedeemNowEditBtn.visibility = View.GONE
                binding.redeemNowEditBtn.setImageResource(R.drawable.green_checked_icon)
                alert.dismiss()
            }

        }

        colorBtnView.setOnClickListener {
            colorBtnView.setOnClickListener {
                ColorPickerPopup.Builder(this)
                    .initialColor(Color.RED) // Set initial color
                    .enableBrightness(true) // Enable brightness slider or not
                    .enableAlpha(true) // Enable alpha slider or not
                    .okTitle(getString(R.string.chose_text))
                    .cancelTitle(getString(R.string.cancel_text))
                    .showIndicator(true)
                    .showValue(true)
                    .build()
                    .show(colorBtnView, object : ColorPickerObserver() {
                        override fun onColorPicked(color: Int) {
                            val hexColor = "#" + Integer.toHexString(color).substring(2)
                            colorBtnView.setBackgroundColor(Color.parseColor(hexColor))
                            colorTextField.setText(hexColor)
                            selectedColor = hexColor

                        }

                        fun onColor(color: Int, fromUser: Boolean) {

                        }
                    })
            }
        }
        // REDEEM BUTTON TEXT SPINNER
        redeemTextSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }

            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                val selectedItemText = adapterView!!.getItemAtPosition(i).toString()
                if (selectedItemText.toLowerCase(Locale.ENGLISH) == "custom") {
                    redeemCustomInputBox.visibility = View.VISIBLE

                    redeemCustomInputBox.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {

                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                            selectedRedeemButtonText = s.toString().trim()
                        }

                        override fun afterTextChanged(s: Editable?) {

                        }

                    })
                } else {
                    redeemCustomInputBox.visibility = View.GONE
                    selectedRedeemButtonText = selectedItemText.trim()
                }

            }
        }

    }

    // THIS FUNCTION WILL OPEN AND UPDATE TEXT
    private fun updateText(view: MaterialTextView, type: Int) {
        val dialogLayout = LayoutInflater.from(context).inflate(R.layout.text_update_dialog, null)
        val textColorLayout = dialogLayout.findViewById<LinearLayout>(R.id.text_top_layout)
        val cancelBtn = dialogLayout.findViewById<MaterialButton>(R.id.coupon_dialog_cancel_btn)
        val updateBtn = dialogLayout.findViewById<MaterialButton>(R.id.coupon_dialog_update_btn)
        val inputBox = dialogLayout.findViewById<TextInputEditText>(R.id.coupon_text_input_field)
        val colorBtnView = dialogLayout.findViewById<AppCompatButton>(R.id.text_color_btn)
        val colorTextField = dialogLayout.findViewById<TextInputEditText>(R.id.text_color_tf)
        //inputBox.setText(view.text.toString())
        var selectedColor = ""
        when (updateType) {
            "company" -> {
                if (couponCompanyNameText.isNotEmpty()) {
                    inputBox.setText(couponCompanyNameText)
                }
                if (couponCompanyNameTextColor.isEmpty()) {
                    selectedColor = colorTextField.text.toString()
                } else {
                    selectedColor = couponCompanyNameTextColor
                    colorTextField.setText(selectedColor)
                    colorBtnView.setBackgroundColor(Color.parseColor(selectedColor))
                }
            }
            "headline" -> {
                if (couponOfferTitleText.isNotEmpty()) {
                    inputBox.setText(couponOfferTitleText)
                }
                if (couponOfferTitleTextColor.isEmpty()) {
                    selectedColor = colorTextField.text.toString()
                } else {
                    selectedColor = couponOfferTitleTextColor
                    colorTextField.setText(selectedColor)
                    colorBtnView.setBackgroundColor(Color.parseColor(selectedColor))
                }
            }
            "description" -> {
                if (couponOfferDescriptionText.isNotEmpty()) {
                    inputBox.setText(couponOfferDescriptionText)
                }
                if (couponOfferDescriptionTextColor.isEmpty()) {
                    selectedColor = colorTextField.text.toString()
                } else {
                    selectedColor = couponOfferDescriptionTextColor
                    colorTextField.setText(selectedColor)
                    colorBtnView.setBackgroundColor(Color.parseColor(selectedColor))
                }
            }
            "coupon_code" -> {
                if (couponCodeText.isNotEmpty()) {
                    inputBox.setText(couponCodeText)
                }
                if (couponCodeTextColor.isEmpty()) {
                    selectedColor = colorTextField.text.toString()
                } else {
                    selectedColor = couponCodeTextColor
                    colorTextField.setText(selectedColor)
                    colorBtnView.setBackgroundColor(Color.parseColor(selectedColor))
                }
            }
            "terms_conditions" -> {
                if (couponTermsConditionText.isNotEmpty()) {
                    inputBox.setText(couponTermsConditionText)
                    binding.couponTermsCondition.text = getString(R.string.terms_conditions1)
                    binding.couponTermsCondition.paintFlags =
                        binding.couponTermsCondition.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                }
            }
            else -> {

            }
        }

        if (type == 1) {
            textColorLayout.visibility = View.VISIBLE
        } else {
            textColorLayout.visibility = View.GONE
        }
        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(dialogLayout)
        builder.setCancelable(false)
        val alert = builder.create()
        alert.show()

        cancelBtn.setOnClickListener {
            alert.dismiss()
        }
        updateBtn.setOnClickListener {
            val value = inputBox.text.toString().trim()
            if (value.isNotEmpty()) {
                view.text = value
                if (type == 1) {
                    if (selectedColor.isNotEmpty()) {
                        view.setTextColor(Color.parseColor(selectedColor))

                    }
                }
                when (updateType) {
                    "company" -> {
                        couponCompanyNameText = value
                        couponCompanyNameTextColor = selectedColor
                        binding.lavCompanyNameEditBtn.visibility = View.GONE
                        binding.companyNameEditBtn.setImageResource(R.drawable.green_checked_icon)
                    }
                    "headline" -> {
                        couponOfferTitleText = value
                        couponOfferTitleTextColor = selectedColor
                        binding.lavCompanyNameEditBtn.visibility = View.GONE
                        binding.headlineTextEditBtn.setImageResource(R.drawable.green_checked_icon)
                    }
                    "description" -> {
                        couponOfferDescriptionText = value
                        couponOfferDescriptionTextColor = selectedColor
                        binding.lavDescriptionTextEditBtn.visibility = View.GONE
                        binding.descriptionTextEditBtn.setImageResource(R.drawable.green_checked_icon)
                    }
                    "coupon_code" -> {
                        couponCodeText = value
                        couponCodeTextColor = selectedColor
                        binding.lavCouponCodeEditBtn.visibility = View.GONE
                        binding.couponCodeEditBtn.setImageResource(R.drawable.green_checked_icon)
                    }
                    "terms_conditions" -> {
                        couponTermsConditionText = value
                        binding.couponTermsCondition.text = getString(R.string.terms_conditions1)
                        binding.couponTermsCondition.paintFlags =
                            binding.couponTermsCondition.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                        binding.couponTermsCondition.visibility = View.GONE
                        binding.couponTermsConditionEditBtn.setImageResource(R.drawable.green_checked_icon)
                    }
                    else -> {

                    }
                }

                alert.dismiss()
            }
            else{
                showAlert(context,getString(R.string.empty_text_error))
            }
        }

        colorBtnView.setOnClickListener {
            ColorPickerPopup.Builder(this)
                .initialColor(Color.RED) // Set initial color
                .enableBrightness(true) // Enable brightness slider or not
                .enableAlpha(true) // Enable alpha slider or not
                .okTitle(getString(R.string.chose_text))
                .cancelTitle(getString(R.string.cancel_text))
                .showIndicator(true)
                .showValue(true)
                .build()
                .show(colorBtnView, object : ColorPickerObserver() {
                    override fun onColorPicked(color: Int) {
                        val hexColor = "#" + Integer.toHexString(color).substring(2)
                        colorBtnView.setBackgroundColor(Color.parseColor(hexColor))
                        colorTextField.setText(hexColor)
                        selectedColor = hexColor

                    }

                    fun onColor(color: Int, fromUser: Boolean) {

                    }
                })
        }

    }

    // THIS FUNCTION WILL UPDATE TEXT AND COLOR
    private fun updateTextAndColor(view: AppCompatButton, type: Int) {
        val dialogLayout =
            LayoutInflater.from(context).inflate(R.layout.text_with_color_update_dialog, null)
        val cancelBtn =
            dialogLayout.findViewById<MaterialButton>(R.id.text_with_color_dialog_cancel_btn)
        val updateBtn =
            dialogLayout.findViewById<MaterialButton>(R.id.text_with_color_dialog_update_btn)
        val inputBox =
            dialogLayout.findViewById<TextInputEditText>(R.id.text_with_color_text_input_field)
        val saleBadgeWrapperLayout =
            dialogLayout.findViewById<LinearLayout>(R.id.text_with_color_sale_badge_wrapper)
        val saleBadgeSpinner =
            dialogLayout.findViewById<AppCompatSpinner>(R.id.text_with_color_sale_badge_selector)
        val customSaleBadgeView =
            dialogLayout.findViewById<TextInputEditText>(R.id.text_with_color_custom_sale_badge)
        val colorBtnView =
            dialogLayout.findViewById<AppCompatButton>(R.id.text_with_color_color_btn)
        val colorTextField =
            dialogLayout.findViewById<TextInputEditText>(R.id.text_with_color_color_tf)
        var selectedColor = ""
        when (updateType) {
            "sale_badge" -> {
                if (couponSaleBadgeButtonText.isNotEmpty()) {
                    inputBox.setText(couponSaleBadgeButtonText)
                }
                if (couponSaleBadgeButtonColor.isEmpty()) {
                    selectedColor = colorTextField.text.toString()
                } else {
                    selectedColor = couponSaleBadgeButtonColor
                    colorTextField.setText(selectedColor)
                    colorBtnView.setBackgroundColor(Color.parseColor(selectedColor))
                }
            }
            "get_coupon_btn" -> {
                if (couponGetButtonText.isNotEmpty()) {
                    inputBox.setText(couponGetButtonText)
                }
                if (couponGetButtonColor.isEmpty()) {
                    selectedColor = colorTextField.text.toString()
                } else {
                    selectedColor = couponGetButtonColor
                    colorTextField.setText(selectedColor)
                    colorBtnView.setBackgroundColor(Color.parseColor(selectedColor))
                }
            }
            else -> {

            }
        }
        if (type == 0) {
            inputBox.visibility = View.GONE
            saleBadgeWrapperLayout.visibility = View.VISIBLE
        } else {
            saleBadgeWrapperLayout.visibility = View.GONE
            inputBox.visibility = View.VISIBLE
        }

        selectedSaleBadgeText = binding.couponSaleBadge.text.toString()
        val listOptions = resources.getStringArray(R.array.sale_badge_options)
        if (getPositionFromText(listOptions, selectedSaleBadgeText) == -1) {
            customSaleBadgeView.visibility = View.VISIBLE
        } else {
            customSaleBadgeView.visibility = View.GONE
        }
        saleBadgeSpinner.setSelection(getPositionFromText(listOptions, selectedSaleBadgeText))
        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(dialogLayout)
        builder.setCancelable(false)
        val alert = builder.create()
        alert.show()

        colorBtnView.setOnClickListener {
            ColorPickerPopup.Builder(this)
                .initialColor(Color.RED) // Set initial color
                .enableBrightness(true) // Enable brightness slider or not
                .enableAlpha(true) // Enable alpha slider or not
                .okTitle(getString(R.string.chose_text))
                .cancelTitle(getString(R.string.cancel_text))
                .showIndicator(true)
                .showValue(true)
                .build()
                .show(colorBtnView, object : ColorPickerObserver() {
                    override fun onColorPicked(color: Int) {
                        val hexColor = "#" + Integer.toHexString(color).substring(2)
                        colorBtnView.setBackgroundColor(Color.parseColor(hexColor))
                        colorTextField.setText(hexColor)
                        selectedColor = hexColor

                    }

                    fun onColor(color: Int, fromUser: Boolean) {

                    }
                })
        }

        // SALE BADGE SPINNER
        saleBadgeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }

            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                val selectedItemText = adapterView!!.getItemAtPosition(i).toString()
                if (selectedItemText.toLowerCase(Locale.ENGLISH) == "custom") {
                    customSaleBadgeView.visibility = View.VISIBLE

                    customSaleBadgeView.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {

                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                            selectedSaleBadgeText = s.toString().trim()
                        }

                        override fun afterTextChanged(s: Editable?) {

                        }

                    })
                } else {
                    customSaleBadgeView.visibility = View.GONE
                    selectedSaleBadgeText = selectedItemText.trim()
                }

            }
        }
        cancelBtn.setOnClickListener { alert.dismiss() }
        updateBtn.setOnClickListener {

            if (type == 0) {
                view.text = selectedSaleBadgeText
                if (selectedColor.isNotEmpty()) {
                    view.setBackgroundColor(Color.parseColor(selectedColor))
                }
            } else {
                view.text = inputBox.text.toString()
                if (selectedColor.isNotEmpty()) {
                    view.setBackgroundColor(Color.parseColor(selectedColor))
                }
            }
            val value = view.text.toString().trim()
            when (updateType) {
                "sale_badge" -> {
                    couponSaleBadgeButtonText = value
                    couponSaleBadgeButtonColor = selectedColor
                    binding.saleBadgeHint.visibility = View.GONE
                    binding.lavSaleBadgeEditBtn.visibility = View.GONE
                    binding.saleBadgeEditBtn.setImageResource(R.drawable.green_checked_icon)
                }
                "get_coupon_btn" -> {
                    couponGetButtonText = value
                    couponGetButtonColor = selectedColor
                    binding.getCouponEditHint.visibility = View.GONE
                    binding.lavGetCouponEditBtn.visibility = View.GONE
                    binding.lavGetCouponEditBtn.setImageResource(R.drawable.green_checked_icon)
                }
                else -> {

                }
            }

            alert.dismiss()
        }
    }

    // THIS FUNCTION WILL RETURN THE INDEX OF SALE BADGE TEXT FROM LIST
    private fun getPositionFromText(listOptions: Array<String>, text: String): Int {
        val list = mutableListOf<String>()
        var position = -1
        list.addAll(listOptions)
        if (list.size > 0) {
            for (item: String in list) {
                if (item.toLowerCase(Locale.ENGLISH) == "custom") {
                    continue
                } else {
                    position = list.indexOf(text)
                }
            }
        }

        return position
    }

    private fun openColorDialog(view: View) {
        ColorPickerPopup.Builder(this)
            .initialColor(Color.RED) // Set initial color
            .enableBrightness(true) // Enable brightness slider or not
            .enableAlpha(true) // Enable alpha slider or not
            .okTitle(getString(R.string.chose_text))
            .cancelTitle(getString(R.string.cancel_text))
            .showIndicator(true)
            .showValue(true)
            .build()
            .show(view, object : ColorPickerObserver() {
                override fun onColorPicked(color: Int) {
                    val hexColor = "#" + Integer.toHexString(color).substring(2)
                    binding.couponWrapperLayout.setBackgroundColor(Color.parseColor(hexColor))
                    couponBackgroundColor = hexColor
                    binding.backgroundColorEditHint.visibility = View.GONE
                    binding.lavBackgroundColorEditBtn.visibility = View.GONE
                    binding.backgroundColorEditBtn.setImageResource(R.drawable.green_checked_icon)
                }

                fun onColor(color: Int, fromUser: Boolean) {

                }
            })
    }

    override fun onDateSet(picker: DatePicker?, year: Int, month: Int, day: Int) {
        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, year)
        c.set(Calendar.MONTH, month)
        c.set(Calendar.DAY_OF_MONTH, day)
        val selectedDate = getDateFromTimeStamp(c.timeInMillis)
        couponValidDate = selectedDate
        binding.couponValidTillTextView.text = selectedDate
        binding.lavCouponValidTillEditBtn.visibility = View.GONE
        binding.couponValidTillEditBtn.setImageResource(R.drawable.green_checked_icon)
    }

}