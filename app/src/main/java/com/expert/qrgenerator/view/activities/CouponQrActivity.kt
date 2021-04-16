package com.expert.qrgenerator.view.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.expert.qrgenerator.R
import com.expert.qrgenerator.model.Coupon
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.ImageManager
import com.expert.qrgenerator.utils.RuntimePermissionHelper
import com.expert.qrgenerator.viewmodel.CouponQrViewModel
import com.expert.qrgenerator.viewmodel.DynamicQrViewModel
import com.expert.qrgenerator.viewmodelfactory.ViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import top.defaults.colorpicker.ColorPickerPopup
import top.defaults.colorpicker.ColorPickerPopup.ColorPickerObserver
import java.util.*


class CouponQrActivity : BaseActivity(), View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var couponParentLayout: ConstraintLayout
    private lateinit var initialDesignLayout: ConstraintLayout
    private lateinit var nextDesignLayout: ConstraintLayout
    private lateinit var couponSaleImageView: AppCompatImageView
    private lateinit var couponCodeCloseBtnView: AppCompatImageView
    private lateinit var companyNameView: MaterialTextView
    private lateinit var headlineView: MaterialTextView
    private lateinit var descriptionView: MaterialTextView
    private lateinit var nextBtnView: MaterialTextView
    private lateinit var couponCodeView: MaterialTextView
    private lateinit var couponCodeEditBtn: AppCompatImageView
    private lateinit var saleBadgeBtn: AppCompatButton
    private lateinit var getCouponBtn: AppCompatButton
    private var selectedSaleBadgeText: String = "SALE"
    private var selectedRedeemButtonText: String = "Redeem Now"
    private var updateType = ""
    private lateinit var companyNameEditBtn: AppCompatImageView
    private lateinit var backgroundColorEditBtn: AppCompatImageView
    private lateinit var headerImageEditBtn: AppCompatImageView
    private lateinit var saleBadgeEditBtn: AppCompatImageView
    private lateinit var headlineTextEditBtn: AppCompatImageView
    private lateinit var descriptionTextEditBtn: AppCompatImageView
    private lateinit var getCouponButtonEditBtn: AppCompatImageView
    private lateinit var redeemNowEditBtn: AppCompatImageView
    private lateinit var redeemNowBtn: AppCompatButton
    private lateinit var couponValidTillView: MaterialTextView
    private lateinit var couponValidTillEditBtn: AppCompatImageView
    private lateinit var termsConditionsEditBtn: AppCompatImageView
    private lateinit var termsConditionsDisplayLayout: LinearLayout
    private lateinit var termsConditionsDisplayView: MaterialTextView
    private lateinit var termsConditionsTextBtn: MaterialTextView
    private lateinit var createCouponBtn: MaterialButton
    private var couponDetail: Coupon? = null
    private var coupon_company_name: String = ""
    private var coupon_background_color: String = ""
    private var coupon_header_image: String = ""
    private var coupon_sale_badge_button_text: String = ""
    private var coupon_sale_badge_button_color: String = ""
    private var coupon_headline_text: String = ""
    private var coupon_description_text: String = ""
    private var coupon_get_button_text: String = ""
    private var coupon_get_button_color: String = ""
    private var coupon_code_text: String = ""
    private var coupon_code_text_color: String = ""
    private var coupon_valid_date: String = ""
    private var coupon_terms_condition_text: String = ""
    private var coupon_redeem_button_text: String = ""
    private var coupon_redeem_button_color: String = ""
    private var coupon_redeem_website_url: String = ""
    private lateinit var viewModel: CouponQrViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coupon_qr)

        initViews()
        setUpToolbar()
    }

    // THIS FUNCTION WILL INITIALIZE ALL THE VIEWS AND REFERENCE OF OBJECTS
    private fun initViews() {
        context = this
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(CouponQrViewModel()).createFor()
        )[CouponQrViewModel::class.java]
        toolbar = findViewById(R.id.toolbar)
        couponParentLayout = findViewById(R.id.coupon_wrapper_layout)
        initialDesignLayout = findViewById(R.id.coupon_design_layout)
        nextDesignLayout = findViewById(R.id.coupon_next_design_layout)
        nextBtnView = findViewById(R.id.next_btn)
        nextBtnView.setOnClickListener(this)
        nextBtnView.paintFlags = nextBtnView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        getCouponBtn = findViewById(R.id.get_coupon_btn)
        couponSaleImageView = findViewById(R.id.coupon_sale_image)
        couponCodeCloseBtnView = findViewById(R.id.coupon_code_layout_close_btn)
        couponCodeCloseBtnView.setOnClickListener(this)
        companyNameView = findViewById(R.id.coupon_company_name)
        headlineView = findViewById(R.id.coupon_content_headline)
        descriptionView = findViewById(R.id.coupon_content_description)
        saleBadgeBtn = findViewById(R.id.coupon_sale_badge)
        companyNameEditBtn = findViewById(R.id.company_name_edit_btn)
        companyNameEditBtn.setOnClickListener(this)
        backgroundColorEditBtn = findViewById(R.id.background_color_edit_btn)
        backgroundColorEditBtn.setOnClickListener(this)
        headerImageEditBtn = findViewById(R.id.header_image_edit_btn)
        headerImageEditBtn.setOnClickListener(this)
        saleBadgeEditBtn = findViewById(R.id.sale_badge_edit_btn)
        saleBadgeEditBtn.setOnClickListener(this)
        headlineTextEditBtn = findViewById(R.id.headline_text_edit_btn)
        headlineTextEditBtn.setOnClickListener(this)
        descriptionTextEditBtn = findViewById(R.id.description_text_edit_btn)
        descriptionTextEditBtn.setOnClickListener(this)
        getCouponButtonEditBtn = findViewById(R.id.get_coupon_edit_btn)
        getCouponButtonEditBtn.setOnClickListener(this)
        redeemNowEditBtn = findViewById(R.id.redeem_now_edit_btn)
        redeemNowEditBtn.setOnClickListener(this)
        redeemNowBtn = findViewById(R.id.redeem_now_btn)
        couponCodeView = findViewById(R.id.coupon_code_text_view)
        couponCodeEditBtn = findViewById(R.id.coupon_code_edit_btn)
        couponCodeEditBtn.setOnClickListener(this)
        couponValidTillView = findViewById(R.id.coupon_valid_till_text_view)
        couponValidTillEditBtn = findViewById(R.id.coupon_valid_till_edit_btn)
        couponValidTillEditBtn.setOnClickListener(this)
        termsConditionsEditBtn = findViewById(R.id.coupon_terms_condition_edit_btn)
        termsConditionsEditBtn.setOnClickListener(this)
        termsConditionsTextBtn = findViewById(R.id.coupon_terms_condition)
        termsConditionsTextBtn.setOnClickListener(this)
        termsConditionsDisplayLayout = findViewById(R.id.terms_condition_display_wrapper_layout)
        termsConditionsDisplayView = findViewById(R.id.coupon_terms_condition_display_text_view)
        createCouponBtn = findViewById(R.id.create_coupon_btn)
        createCouponBtn.setOnClickListener(this)

    }

    // THIS FUNCTION WILL RENDER THE ACTION BAR/TOOLBAR
    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.coupon_qr)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    // THIS FUNCTION WILL HANDLE THE ON BACK ARROW CLICK EVENT
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    // THIS FUNCTION WILL HANDLE THE ALL BUTTONS CLICK EVENT
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.create_coupon_btn -> {

                if (validation()) {
                    val hashMap = hashMapOf<String, String>()
                    hashMap["coupon_company_name"] = coupon_company_name
                    hashMap["coupon_background_color"] = coupon_background_color
                    hashMap["coupon_header_image"] = coupon_header_image
                    hashMap["coupon_sale_badge_button_text"] = coupon_sale_badge_button_text
                    hashMap["coupon_sale_badge_button_color"] = coupon_sale_badge_button_color
                    hashMap["coupon_headline_text"] = coupon_headline_text
                    hashMap["coupon_description_text"] = coupon_description_text
                    hashMap["coupon_get_button_text"] = coupon_get_button_text
                    hashMap["coupon_get_button_color"] = coupon_get_button_color
                    hashMap["coupon_code_text"] = coupon_code_text
                    hashMap["coupon_code_text_color"] = coupon_code_text_color
                    hashMap["coupon_valid_date"] = coupon_valid_date
                    hashMap["coupon_terms_condition_text"] = coupon_terms_condition_text
                    hashMap["coupon_redeem_button_text"] = coupon_redeem_button_text
                    hashMap["coupon_redeem_button_color"] = coupon_redeem_button_color
                    hashMap["coupon_redeem_website_url"] = coupon_redeem_website_url

                    startLoading(context)
                    viewModel.createCouponQrCode(context, hashMap)
                    viewModel.getCouponQrCode().observe(this, { response ->
                        var url = ""
                        dismiss()
                        if (response != null) {
                            url = response.get("generatedUrl").asString
//                            url = if (url.contains(":8990")) {
//                                url.replace(":8990","")
//                            } else {
//                                url
//                            }
                            val returnIntent = Intent()
                            returnIntent.putExtra("COUPON_URL", url)
                            setResult(RESULT_OK, returnIntent)
                            finish()
                        } else {
                            showAlert(context, "Something went wrong, please try again!")
                        }
                    })

                }


//                couponDetail = Coupon(
//                    coupon_company_name,
//                    coupon_background_color,
//                    coupon_header_image,
//                    coupon_sale_badge_button_text,
//                    coupon_sale_badge_button_color,
//                    coupon_headline_text,
//                    coupon_description_text,
//                    coupon_get_button_text,
//                    coupon_get_button_color,
//                    coupon_code_text,
//                    coupon_code_text_color,
//                    coupon_valid_date,
//                    coupon_terms_condition_text,
//                    coupon_redeem_button_text,
//                    coupon_redeem_button_color,
//                    coupon_redeem_website_url
//                )
                //val json = Gson().toJson(couponDetail)
            }
            R.id.next_btn -> {
                if (nextBtnView.text.toString().toLowerCase(Locale.ENGLISH) == "next") {
                    initialDesignLayout.visibility = View.GONE
                    nextDesignLayout.visibility = View.VISIBLE
                    nextBtnView.text = "Back"
                } else {
                    nextDesignLayout.visibility = View.GONE
                    initialDesignLayout.visibility = View.VISIBLE
                    nextBtnView.text = "Next"
                }
            }
            R.id.coupon_code_layout_close_btn -> {
                nextDesignLayout.visibility = View.GONE
                initialDesignLayout.visibility = View.VISIBLE
                nextBtnView.text = "Next"
            }
            R.id.company_name_edit_btn -> {
                updateType = "company"
                updateText(companyNameView, 0)
            }
            R.id.background_color_edit_btn -> {
                updateType = "background_color"
                openColorDialog(couponParentLayout)
            }
            R.id.header_image_edit_btn -> {
                if (RuntimePermissionHelper.checkPermission(
                        context,
                        Constants.READ_STORAGE_PERMISSION
                    )
                ) {
                    getImageFromLocalStorage()
                }
            }
            R.id.sale_badge_edit_btn -> {
                updateType = "sale_badge"
                updateTextAndColor(saleBadgeBtn, 0)
            }
            R.id.headline_text_edit_btn -> {
                updateType = "headline"
                updateText(headlineView, 0)
            }
            R.id.description_text_edit_btn -> {
                updateType = "description"
                updateText(descriptionView, 0)
            }
            R.id.get_coupon_edit_btn -> {
                updateType = "get_coupon_btn"
                updateTextAndColor(getCouponBtn, 1)
            }
            R.id.redeem_now_edit_btn -> {
                updateRedeemButton(redeemNowBtn)
            }
            R.id.coupon_code_edit_btn -> {
                updateType = "coupon_code"
                updateText(couponCodeView, 1)
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
                updateText(termsConditionsDisplayView, 0)
            }
            R.id.coupon_terms_condition -> {
                if (termsConditionsDisplayLayout.visibility == View.GONE) {
                    termsConditionsDisplayLayout.visibility = View.VISIBLE
                } else {
                    termsConditionsDisplayLayout.visibility = View.GONE
                }
            }
            else -> {

            }
        }
    }

    // THIS FUNCTION WILL VALIDATE ALL THE COUPON INPUT DATA
    private fun validation(): Boolean {
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
                        "Please select the header image having size 640 x 360!"
                    )
                } else {
                    coupon_header_image = ImageManager.convertImageToBase64(context, data.data!!)
                    Log.d("TEST199", coupon_header_image)
                    couponSaleImageView.setImageURI(data.data)
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
                        .setMessage("Please allow the READ EXTERNAL STORAGE permission for use own Image in QR Image.")
                        .setCancelable(false)
                        .setPositiveButton("Ok") { dialog, which ->
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
        var selectedColor = colorTextField.text.toString()
        redeemCustomInputBox.setText(view.text.toString())
        selectedRedeemButtonText = redeemNowBtn.text.toString()
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
                showAlert(context, "Please enter the website URL without https:// or http://")
            } else {
                coupon_redeem_button_text = selectedRedeemButtonText
                coupon_redeem_button_color = selectedColor
                coupon_redeem_website_url = redeemWebsiteUrl.text.toString()
                view.setBackgroundColor(Color.parseColor(selectedColor))
                view.text = selectedRedeemButtonText
                alert.dismiss()
            }

        }

        colorBtnView.setOnClickListener {
            colorBtnView.setOnClickListener {
                ColorPickerPopup.Builder(this)
                    .initialColor(Color.RED) // Set initial color
                    .enableBrightness(true) // Enable brightness slider or not
                    .enableAlpha(true) // Enable alpha slider or not
                    .okTitle("Choose")
                    .cancelTitle("Cancel")
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
                            selectedRedeemButtonText = s.toString()
                        }

                        override fun afterTextChanged(s: Editable?) {

                        }

                    })
                } else {
                    redeemCustomInputBox.visibility = View.GONE
                    selectedRedeemButtonText = selectedItemText
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
        inputBox.setText(view.text.toString())
        var selectedColor = ""
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
            val value = inputBox.text.toString()
            view.text = value
            if (type == 1) {
                if (selectedColor.isNotEmpty()) {
                    view.setTextColor(Color.parseColor(selectedColor))

                }
            }
            when (updateType) {
                "company" -> {
                    coupon_company_name = value
                }
                "headline" -> {
                    coupon_headline_text = value
                }
                "description" -> {
                    coupon_description_text = value
                }
                "coupon_code" -> {
                    coupon_code_text = value
                    coupon_code_text_color = selectedColor
                }
                "terms_conditions" -> {
                    coupon_terms_condition_text = value
                }
                else -> {

                }
            }

            alert.dismiss()
        }

        colorBtnView.setOnClickListener {
            ColorPickerPopup.Builder(this)
                .initialColor(Color.RED) // Set initial color
                .enableBrightness(true) // Enable brightness slider or not
                .enableAlpha(true) // Enable alpha slider or not
                .okTitle("Choose")
                .cancelTitle("Cancel")
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
        if (type == 0) {
            inputBox.visibility = View.GONE
            saleBadgeWrapperLayout.visibility = View.VISIBLE
        } else {
            saleBadgeWrapperLayout.visibility = View.GONE
            inputBox.visibility = View.VISIBLE
        }
        inputBox.setText(view.text.toString())
        selectedSaleBadgeText = saleBadgeBtn.text.toString()
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
                .okTitle("Choose")
                .cancelTitle("Cancel")
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
                            selectedSaleBadgeText = s.toString()
                        }

                        override fun afterTextChanged(s: Editable?) {

                        }

                    })
                } else {
                    customSaleBadgeView.visibility = View.GONE
                    selectedSaleBadgeText = selectedItemText
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

            when (updateType) {
                "sale_badge" -> {
                    coupon_sale_badge_button_text = view.text.toString()
                    coupon_sale_badge_button_color = selectedColor
                }
                "get_coupon_btn" -> {
                    coupon_get_button_text = view.text.toString()
                    coupon_get_button_color = selectedColor
                }
                else -> {

                }
            }
            alert.dismiss()
        }
    }

    // THIS FUNCTION WILL OPEN AND TAKE INPUT BASIC INFORMATION FOR COUPON
//    private fun openCouponBasicInformationDialog() {
//        val biLayoutView = LayoutInflater.from(context).inflate(
//            R.layout.coupon_basic_information_dialog,
//            null
//        )
//        val cbiCompanyView = biLayoutView.findViewById<TextInputEditText>(R.id.cbi_company_name)
//        cbiCompanyView.setText(companyNameView.text.toString())
//        val cbiHeadlineView = biLayoutView.findViewById<TextInputEditText>(R.id.cbi_headline)
//        cbiHeadlineView.setText(headlineView.text.toString())
//        val cbiDescriptionView = biLayoutView.findViewById<TextInputEditText>(R.id.cbi_description)
//        cbiDescriptionView.setText(descriptionView.text.toString())
//        val cbiSaleBadgeSpinner =
//            biLayoutView.findViewById<AppCompatSpinner>(R.id.cbi_sale_badge_selector)
//        val cbiCustomSaleBadgeView =
//            biLayoutView.findViewById<TextInputEditText>(R.id.cbi_custom_sale_badge)
//        val cbiCancelBtn = biLayoutView.findViewById<MaterialButton>(R.id.cbi_cancel_btn)
//        val cbiSaveBtn = biLayoutView.findViewById<MaterialButton>(R.id.cbi_save_btn)
//        selectedSaleBadgeText = saleBadgeBtn.text.toString()
////        if (getPositionFromText(selectedSaleBadgeText) == -1) {
////            cbiCustomSaleBadgeView.visibility = View.VISIBLE
////        } else {
////            cbiCustomSaleBadgeView.visibility = View.GONE
////        }
////        cbiSaleBadgeSpinner.setSelection(getPositionFromText(selectedSaleBadgeText))
//
//        biBuilder = MaterialAlertDialogBuilder(context)
//        biBuilder.setCancelable(false)
//        biBuilder.setView(biLayoutView)
//        biAlert = biBuilder.create()
//        biAlert.show()
//
//        // SALE BADGE SPINNER
//        cbiSaleBadgeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onNothingSelected(adapterView: AdapterView<*>?) {
//
//            }
//
//            override fun onItemSelected(
//                adapterView: AdapterView<*>?,
//                view: View?,
//                i: Int,
//                l: Long
//            ) {
//                val selectedItemText = adapterView!!.getItemAtPosition(i).toString()
//                if (selectedItemText.toLowerCase(Locale.ENGLISH) == "custom") {
//                    cbiCustomSaleBadgeView.visibility = View.VISIBLE
//
//                    cbiCustomSaleBadgeView.addTextChangedListener(object : TextWatcher {
//                        override fun beforeTextChanged(
//                            s: CharSequence?,
//                            start: Int,
//                            count: Int,
//                            after: Int
//                        ) {
//
//                        }
//
//                        override fun onTextChanged(
//                            s: CharSequence?,
//                            start: Int,
//                            before: Int,
//                            count: Int
//                        ) {
//                            selectedSaleBadgeText = s.toString()
//                        }
//
//                        override fun afterTextChanged(s: Editable?) {
//
//                        }
//
//                    })
//                } else {
//                    cbiCustomSaleBadgeView.visibility = View.GONE
//                    selectedSaleBadgeText = selectedItemText
//                }
//
//            }
//        }
//        cbiCancelBtn.setOnClickListener { biAlert.dismiss() }
//        cbiSaveBtn.setOnClickListener {
//            companyNameView.text = cbiCompanyView.text.toString()
//            headlineView.text = cbiHeadlineView.text.toString()
//            descriptionView.text = cbiDescriptionView.text.toString()
//            saleBadgeBtn.text = selectedSaleBadgeText
//            biAlert.dismiss()
//        }
//    }

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
            .okTitle("Choose")
            .cancelTitle("Cancel")
            .showIndicator(true)
            .showValue(true)
            .build()
            .show(view, object : ColorPickerObserver() {
                override fun onColorPicked(color: Int) {
                    val hexColor = "#" + Integer.toHexString(color).substring(2)
                    couponParentLayout.setBackgroundColor(Color.parseColor(hexColor))
                    coupon_background_color = hexColor
                }

                fun onColor(color: Int, fromUser: Boolean) {

                }
            })
    }


    // THIS FUNCTION WILL OPEN THE COLOR PICKER FOR PERSONALIZE THE COUPON PAGE
//    private fun openColorPicker() {
//        val colorPickerView = LayoutInflater.from(context).inflate(
//            R.layout.coupon_color_picker_dialog,
//            null
//        )
//        val primaryColorBtnView =
//            colorPickerView.findViewById<AppCompatButton>(R.id.coupon_primary_color_btn)
//        val primaryColorTextField =
//            colorPickerView.findViewById<TextInputEditText>(R.id.coupon_primary_color_tf)
//        val buttonColorBtnView =
//            colorPickerView.findViewById<AppCompatButton>(R.id.coupon_button_color_btn)
//        val buttonColorTextField =
//            colorPickerView.findViewById<TextInputEditText>(R.id.coupon_button_color_tf)
//        val colorCancelBtn =
//            colorPickerView.findViewById<MaterialButton>(R.id.coupon_color_cancel_btn)
//        val saveColorBtn = colorPickerView.findViewById<MaterialButton>(R.id.coupon_color_save_btn)
//
//        if (selectedPrimaryColor.isNotEmpty()) {
//            primaryColorTextField.setText(selectedPrimaryColor)
//            primaryColorBtnView.setBackgroundColor(Color.parseColor(selectedPrimaryColor))
//        }
//        if (selectedButtonColor.isNotEmpty()) {
//            buttonColorTextField.setText(selectedButtonColor)
//            buttonColorBtnView.setBackgroundColor(Color.parseColor(selectedButtonColor))
//        }
//
//        applySelectedColors(
//            primaryColorTextField.text.toString(),
//            buttonColorTextField.text.toString()
//        )
//        colorPickerBuilder = MaterialAlertDialogBuilder(context)
//        colorPickerBuilder.setCancelable(false)
//        colorPickerBuilder.setView(colorPickerView)
//        colorPickerAlert = colorPickerBuilder.create()
//        colorPickerAlert.show()
//
//        primaryColorBtnView.setOnClickListener {
//            ColorPickerPopup.Builder(this)
//                .initialColor(Color.RED) // Set initial color
//                .enableBrightness(true) // Enable brightness slider or not
//                .enableAlpha(true) // Enable alpha slider or not
//                .okTitle("Choose")
//                .cancelTitle("Cancel")
//                .showIndicator(true)
//                .showValue(true)
//                .build()
//                .show(primaryColorBtnView, object : ColorPickerObserver() {
//                    override fun onColorPicked(color: Int) {
//                        val hexColor = "#" + Integer.toHexString(color).substring(2)
//                        primaryColorBtnView.setBackgroundColor(Color.parseColor(hexColor))
//                        primaryColorTextField.setText(hexColor)
//                        selectedPrimaryColor = hexColor
//
//                    }
//
//                    fun onColor(color: Int, fromUser: Boolean) {
//
//                    }
//                })
//        }
//
//        buttonColorBtnView.setOnClickListener {
//            ColorPickerPopup.Builder(this)
//                .initialColor(Color.RED) // Set initial color
//                .enableBrightness(true) // Enable brightness slider or not
//                .enableAlpha(true) // Enable alpha slider or not
//                .okTitle("Choose")
//                .cancelTitle("Cancel")
//                .showIndicator(true)
//                .showValue(true)
//                .build()
//                .show(buttonColorBtnView, object : ColorPickerObserver() {
//                    override fun onColorPicked(color: Int) {
//                        val hexColor = "#" + Integer.toHexString(color).substring(2)
//                        buttonColorBtnView.setBackgroundColor(Color.parseColor(hexColor))
//                        buttonColorTextField.setText(hexColor)
//                        selectedButtonColor = hexColor
//                    }
//
//                    fun onColor(color: Int, fromUser: Boolean) {}
//                })
//        }
//
//        colorCancelBtn.setOnClickListener { colorPickerAlert.dismiss() }
//        saveColorBtn.setOnClickListener {
//            applySelectedColors(
//                primaryColorTextField.text.toString(),
//                buttonColorTextField.text.toString()
//            )
//            colorPickerAlert.dismiss()
//        }
//
//    }

    private fun applySelectedColors(primaryColor: String, buttonColor: String) {
        couponParentLayout.setBackgroundColor(Color.parseColor(primaryColor))
        saleBadgeBtn.setBackgroundColor(Color.parseColor(primaryColor))
        getCouponBtn.setBackgroundColor(Color.parseColor(buttonColor))
    }

    override fun onDateSet(picker: DatePicker?, year: Int, month: Int, day: Int) {
        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, year)
        c.set(Calendar.MONTH, month)
        c.set(Calendar.DAY_OF_MONTH, day)
        val selectedDate = getDateFromTimeStamp(c.timeInMillis)
        coupon_valid_date = selectedDate
        couponValidTillView.text = selectedDate
    }

}