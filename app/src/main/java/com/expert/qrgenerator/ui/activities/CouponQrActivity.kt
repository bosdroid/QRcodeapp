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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.ActivityCouponQrBinding
import com.expert.qrgenerator.databinding.RedeemButtonDialogBinding
import com.expert.qrgenerator.databinding.TextUpdateDialogBinding
import com.expert.qrgenerator.databinding.TextWithColorUpdateDialogBinding
import com.expert.qrgenerator.singleton.DriveService
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.GeneratorManager
import com.expert.qrgenerator.utils.ImageManager
import com.expert.qrgenerator.utils.RuntimePermissionHelper
import com.expert.qrgenerator.viewmodel.CouponQrViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.api.client.http.FileContent
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.defaults.colorpicker.ColorPickerPopup
import top.defaults.colorpicker.ColorPickerPopup.ColorPickerObserver
import java.io.File
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class CouponQrActivity : BaseActivity(), View.OnClickListener, DatePickerDialog.OnDateSetListener {
    // Binding for Activity layout
    private lateinit var binding: ActivityCouponQrBinding

    // Context of the activity, initialized using lazy delegation
    private val context: Context by lazy { this }

    // Default texts and colors for the coupon
    private var selectedSaleBadgeText: String = "SALE"
    private var selectedRedeemButtonText: String = "Redeem Now"

    // Update type variable
    private var updateType: String = ""

    // Coupon details
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

    // ViewModel for managing coupon QR data
    private val viewModel: CouponQrViewModel by viewModels()

    // Current page for pagination or similar purposes
    private var page: Int = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using ViewBinding
        binding = ActivityCouponQrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views and set up toolbar
        initViews()
        setUpToolbar()
    }

    // Initialize all views and set up click listeners for each interactive element
    private fun initViews() {

        // Set up the click listener for the 'next' button and apply underline styling
        with(binding) {
            nextBtn.apply {
                setOnClickListener(this@CouponQrActivity) // Replace 'YourActivity' with your actual class name
                paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
            }

            // Set up click listeners for all edit buttons
            arrayOf(
                couponCodeLayoutCloseBtn,
                companyNameEditBtn,
                backgroundColorEditBtn,
                headerImageEditBtn,
                saleBadgeEditBtn,
                headlineTextEditBtn,
                descriptionTextEditBtn,
                getCouponEditBtn,
                redeemNowEditBtn,
                couponCodeEditBtn,
                couponValidTillEditBtn,
                couponTermsConditionEditBtn,
                couponTermsCondition,
                nextStepBtn
            ).forEach { it.setOnClickListener(this@CouponQrActivity) } // Replace 'YourActivity' with your actual class name
        }
    }

    // This function sets up the toolbar with the appropriate title and back button
    private fun setUpToolbar() {
        // Set the toolbar as the ActionBar for this activity
        setSupportActionBar(binding.toolbar)

        // Set the title of the ActionBar
        supportActionBar?.apply {
            title = getString(R.string.coupon_qr)
            setDisplayHomeAsUpEnabled(true) // Show the back button
        }

        // Set the text color of the toolbar title
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle the item selection
        return when (item.itemId) {
            android.R.id.home -> {
                // Check if any coupon fields have been modified
                if (couponBackgroundColor.isEmpty()
                    && couponCompanyNameText.isEmpty()
                    && couponHeaderImage.isEmpty()
                    && couponSaleBadgeButtonText.isEmpty()
                    && couponOfferTitleText.isEmpty()
                    && couponOfferDescriptionText.isEmpty()
                    && couponGetButtonText.isEmpty()
                ) {
                    // If no changes, just go back to the previous activity
                    onBackPressed()
                } else {
                    // If there are changes, show a confirmation dialog
                    MaterialAlertDialogBuilder(context)
                        .setMessage(getString(R.string.changes_saved_alert_text))
                        .setNegativeButton(getString(R.string.cancel_text)) { dialog, _ ->
                            dialog.dismiss() // Dismiss the dialog and stay on the current screen
                        }
                        .setPositiveButton(getString(R.string.leave_text)) { _, _ ->
                            onBackPressed() // Proceed with going back to the previous activity
                        }
                        .create()
                        .show() // Display the dialog
                }
                true // Indicate that the event was handled
            }
            else -> super.onOptionsItemSelected(item) // Handle other menu items using the superclass method
        }
    }

    // This function handles all button click events
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.next_step_btn -> handleNextStepButtonClick()
            R.id.next_btn -> handleNextButtonClick()
            R.id.coupon_code_layout_close_btn -> handleCouponCodeLayoutCloseButtonClick()
            R.id.company_name_edit_btn -> handleCompanyNameEditButtonClick()
            R.id.background_color_edit_btn -> handleBackgroundColorEditButtonClick()
            R.id.header_image_edit_btn -> handleHeaderImageEditButtonClick()
            R.id.sale_badge_edit_btn -> handleSaleBadgeEditButtonClick()
            R.id.headline_text_edit_btn -> handleHeadlineTextEditButtonClick()
            R.id.description_text_edit_btn -> handleDescriptionTextEditButtonClick()
            R.id.get_coupon_edit_btn -> handleGetCouponEditButtonClick()
            R.id.redeem_now_edit_btn -> handleRedeemNowEditButtonClick()
            R.id.coupon_code_edit_btn -> handleCouponCodeEditButtonClick()
            R.id.coupon_valid_till_edit_btn -> handleCouponValidTillEditButtonClick()
            R.id.coupon_terms_condition_edit_btn -> handleCouponTermsConditionEditButtonClick()
            R.id.coupon_terms_condition -> handleCouponTermsConditionVisibilityToggle()
            else -> {
                // Handle unknown button clicks if necessary
            }
        }
    }

    // Handles the "Next Step" button click
    private fun handleNextStepButtonClick() {
        if (isCouponDetailsValid() && page == 1) {
            showNextDesignLayout()
            page = 2
        } else if (page == 2) {
            if (validation()) {
                submitCouponData()
            }
        } else {
            showAlert(context, getString(R.string.fields_marked_with_sign_text))
        }
    }

    // Checks if coupon details are valid
    private fun isCouponDetailsValid(): Boolean {
        return couponBackgroundColor.isNotEmpty() &&
                couponCompanyNameText.isNotEmpty() &&
                couponHeaderImage.isNotEmpty() &&
                couponSaleBadgeButtonText.isNotEmpty() &&
                couponOfferTitleText.isNotEmpty() &&
                couponOfferDescriptionText.isNotEmpty() &&
                couponGetButtonText.isNotEmpty()
    }

    // Shows the next design layout and hides the current layout
    private fun showNextDesignLayout() {
        binding.couponDesignLayout.visibility = View.GONE
        binding.couponNextDesignLayout.visibility = View.VISIBLE
        binding.nextBtn.visibility = View.VISIBLE
    }

    // Submits coupon data and handles the response
    private fun submitCouponData() {
        val hashMap = hashMapOf<String, String>(
            "coupon_company_name" to couponCompanyNameText,
            "coupon_company_name_color" to couponCompanyNameTextColor,
            "coupon_background_color" to couponBackgroundColor,
            "coupon_header_image" to couponHeaderImage,
            "coupon_sale_badge_button_text" to couponSaleBadgeButtonText,
            "coupon_sale_badge_button_color" to couponSaleBadgeButtonColor,
            "coupon_headline_text" to couponOfferTitleText,
            "coupon_headline_text_color" to couponOfferTitleTextColor,
            "coupon_description_text" to couponOfferDescriptionText,
            "coupon_description_text_color" to couponOfferDescriptionTextColor,
            "coupon_get_button_text" to couponGetButtonText,
            "coupon_get_button_color" to couponGetButtonColor,
            "coupon_code_text" to couponCodeText,
            "coupon_code_text_color" to couponCodeTextColor,
            "coupon_valid_date" to couponValidDate,
            "coupon_terms_condition_text" to couponTermsConditionText,
            "coupon_redeem_button_text" to couponRedeemButtonText,
            "coupon_redeem_button_color" to couponRedeemButtonColor,
            "coupon_redeem_website_url" to couponRedeemWebsiteUrl
        )

        startLoading(context)
        lifecycleScope.launch {
            viewModel.createCouponQrCode(hashMap)
        }
        viewModel.couponQrCodeResponse.observe(this, { response ->
            handleCouponQrCodeResponse(response)
        })
    }

    // Handles the response from the coupon QR code generation
    private fun handleCouponQrCodeResponse(response: JsonObject?) {
        var url = ""
        dismiss()
        if (response != null) {
            Log.d("TEST199", response.toString())
            url = response.get("generatedUrl").asString

            GeneratorManager.generateQRCode(context,url,"coupon")

        } else {
            showAlert(context, getString(R.string.something_wrong_error))
        }
    }

    // Handles the "Next" button click
    private fun handleNextButtonClick() {
        binding.couponNextDesignLayout.visibility = View.GONE
        binding.couponDesignLayout.visibility = View.VISIBLE
        binding.nextBtn.visibility = View.GONE
        page = 1
    }

    // Handles the close button in the coupon code layout
    private fun handleCouponCodeLayoutCloseButtonClick() {
        binding.couponNextDesignLayout.visibility = View.GONE
        binding.couponDesignLayout.visibility = View.VISIBLE
        binding.nextBtn.visibility = View.GONE
        page = 1
    }

    // Handles editing the company name
    private fun handleCompanyNameEditButtonClick() {
        updateType = "company"
        updateText(binding.couponCompanyName, 1)
    }

    // Handles editing the background color
    private fun handleBackgroundColorEditButtonClick() {
        updateType = "background_color"
        openColorDialog(binding.couponWrapperLayout)
    }

    // Handles editing the header image
    private fun handleHeaderImageEditButtonClick() {
        if (RuntimePermissionHelper.checkStoragePermission(context, Constants.READ_STORAGE_PERMISSION)) {
            pickImageFromGallery()
        }
    }

    // Handles editing the sale badge
    private fun handleSaleBadgeEditButtonClick() {
        updateType = "sale_badge"
        updateTextAndColor(binding.couponSaleBadge, 0)
    }

    // Handles editing the headline text
    private fun handleHeadlineTextEditButtonClick() {
        updateType = "headline"
        updateText(binding.couponContentHeadline, 1)
    }

    // Handles editing the description text
    private fun handleDescriptionTextEditButtonClick() {
        updateType = "description"
        updateText(binding.couponContentDescription, 1)
    }

    // Handles editing the get coupon button text
    private fun handleGetCouponEditButtonClick() {
        updateType = "get_coupon_btn"
        updateTextAndColor(binding.getCouponBtn, 1)
    }

    // Handles editing the redeem now button
    private fun handleRedeemNowEditButtonClick() {
        updateRedeemButton(binding.redeemNowBtn)
    }

    // Handles editing the coupon code
    private fun handleCouponCodeEditButtonClick() {
        updateType = "coupon_code"
        updateText(binding.couponCodeTextView, 1)
    }

    // Handles editing the coupon valid till date
    private fun handleCouponValidTillEditButtonClick() {
        val c = Calendar.getInstance()
        val year = c[Calendar.YEAR]
        val month = c[Calendar.MONTH]
        val day = c[Calendar.DAY_OF_MONTH]

        DatePickerDialog(context, this, year, month, day).show()
    }

    // Handles editing the terms and conditions
    private fun handleCouponTermsConditionEditButtonClick() {
        updateType = "terms_conditions"
        updateText(binding.couponTermsConditionDisplayTextView, 0)
    }

    // Toggles the visibility of the terms and conditions section
    private fun handleCouponTermsConditionVisibilityToggle() {
        binding.termsConditionDisplayWrapperLayout.visibility =
            if (binding.termsConditionDisplayWrapperLayout.visibility == View.GONE) View.VISIBLE else View.GONE
    }


    // Function to validate all coupon input data
    private fun validation(): Boolean {
        // Define a map of validation checks with the corresponding error messages
        val validationMap = mapOf(
            couponCompanyNameText to R.string.company_name_error_text,
            couponBackgroundColor to R.string.background_color_error_text,
            couponHeaderImage to R.string.header_image_error_text,
            couponSaleBadgeButtonText to R.string.sale_badge_text_error_text,
            couponSaleBadgeButtonColor to R.string.sale_badge_button_color_error_text,
            couponOfferTitleText to R.string.coupon_headline_error_text,
            couponOfferDescriptionText to R.string.coupon_description_error_text,
            couponGetButtonText to R.string.coupon_get_button_text_error_text,
            couponGetButtonColor to R.string.coupon_get_button_color_error_text,
            couponCodeText to R.string.coupon_code_text_error_text,
            couponCodeTextColor to R.string.coupon_code_text_color_error_text,
            couponValidDate to R.string.coupon_valid_date_error_text,
            couponTermsConditionText to R.string.coupon_terms_condition_error_text,
            couponRedeemButtonText to R.string.redeem_button_text_error_text,
            couponRedeemButtonColor to R.string.redeem_button_color_error_text,
            couponRedeemWebsiteUrl to R.string.redeem_target_website_error_text
        )

        // Iterate through the map and validate each field
        for ((input, errorMessageResId) in validationMap) {
            if (input.isEmpty()) {
                // Show alert with the appropriate error message
                showAlert(context, getString(errorMessageResId))
                return false // Return false if validation fails
            }
        }

        return true // Return true if all validations pass
    }

    // Function to launch an intent to pick an image from local storage
    private fun pickImageFromGallery() {
        // Create an intent to open the gallery and allow the user to select an image
        val pickImageIntent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*" // Set the type to image files only
        }
        resultLauncher.launch(pickImageIntent) // Launch the intent using the result launcher
    }

    // Launcher for selecting an image from files
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // Check if the image selection was successful
            if (result.resultCode == Activity.RESULT_OK) {

                val data: Intent? = result.data
                val uri = data?.data ?: return@registerForActivityResult

                // Get the image dimensions
                val size = ImageManager.getImageWidthHeight(context, uri)
                val (imageWidth, imageHeight) = size.split(",").map { it.toInt() }

                // Check if the image exceeds the allowed dimensions
                if (imageWidth > 640 && imageHeight > 360) {
                    showAlert(context, getString(R.string.header_image_size_error_text))
                } else {
                    // Convert the image to Base64
                    couponHeaderImage = ImageManager.convertImageToBase64(context, uri)

                    // Get the real file path of the image (optional, depending on your use case)
                    val path = ImageManager.getRealPathFromUri(context, uri)
                    // uploadOnDrive(path!!) // Uncomment if you need to upload the image

                    // Decode the image file to a Bitmap
                    val bitmapImage = BitmapFactory.decodeFile(path)

                    // Scale the image while maintaining aspect ratio
                    val scaledHeight = (bitmapImage.height * (640.0 / bitmapImage.width)).toInt()
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmapImage, 640, scaledHeight, true)

                    // Set the scaled image in the ImageView
                    binding.couponSaleImage.setImageBitmap(scaledBitmap)

                    // Update the visibility and image resource of the edit buttons
                    binding.headerImageEditBtn.apply {
                        visibility = View.GONE
                        setImageResource(R.drawable.green_checked_icon)
                    }
                    binding.lavHeaderImageEditBtn.visibility = View.GONE
                }
            }
        }

    private fun uploadOnDrive(path: String) {
        // Launch a coroutine on the IO dispatcher to handle background tasks
        CoroutineScope(Dispatchers.IO).launch {

            // Get an instance of the Drive service
            val driveService = DriveService.getDriveInstance()

            // Check if the Drive service instance is not null
            if (driveService != null) {
                // Create metadata for the file to be uploaded
                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = "Image_${System.currentTimeMillis()}.jpg" // Set the file name with a timestamp
                }

                // Create a File object for the file to be uploaded
                val filePath = File(path)

                // Prepare media content for the file (specify MIME type)
                val mediaContent = FileContent("image/jpeg", filePath)

                // Upload the file to Google Drive and retrieve the file ID
                val file = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id") // Request only the file ID in the response
                    .execute()

                // Log the file ID
                Log.e("File ID: ", file.id)

                // Log the Google Drive file link
                Log.d("Drive Link", "https://drive.google.com/file/d/${file.id}/view?usp=sharing")
            } else {
                Log.e("DriveService", "Drive instance is null")
            }
        }
    }


    // This function handles the result of the runtime permission request.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            // Check if the request code matches the one for reading storage.
            Constants.READ_STORAGE_REQUEST_CODE -> {
                // If permission is granted, proceed to get the image from local storage.
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery()
                } else {
                    // If permission is denied, show an alert dialog with an appropriate message.
                    MaterialAlertDialogBuilder(context)
                        .setMessage(getString(R.string.external_storage_permission_error1))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.ok_text)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
            // Handle other request codes if needed.
            else -> {
                // No specific action needed for other request codes in this context.
            }
        }
    }

    // This function handles the data for the Redeem button
    private fun updateRedeemButton(view: AppCompatButton) {
        // Inflate the redeem button dialog layout
        val reedMeBinding = RedeemButtonDialogBinding.inflate(LayoutInflater.from(context))

        // Initialize variables for selected color and button text
        var selectedColor = couponRedeemButtonColor.ifEmpty { reedMeBinding.redeemColorTf.text.toString() }
        var selectedRedeemButtonText = binding.redeemNowBtn.text.toString()

        // Set initial values for color, text, and website URL if available
        if (couponRedeemButtonColor.isNotEmpty()) {
            reedMeBinding.redeemColorTf.setText(selectedColor)
            reedMeBinding.redeemColorBtn.setBackgroundColor(Color.parseColor(selectedColor))
        }

        if (couponRedeemButtonText.isNotEmpty()) {
            reedMeBinding.redeemTextInputField.setText(couponRedeemButtonText)
        }

        if (couponRedeemWebsiteUrl.isNotEmpty()) {
            reedMeBinding.redeemWebsiteUrl.setText(couponRedeemWebsiteUrl)
        }

        // Show custom input box if the selected text is not in the spinner options
        val listOptions = resources.getStringArray(R.array.redeem_options)
        if (getRedeemButtonTextPosition(listOptions, selectedRedeemButtonText) == -1) {
            reedMeBinding.redeemTextInputField.visibility = View.VISIBLE
        } else {
            reedMeBinding.redeemTextInputField.visibility = View.GONE
        }

        // Set the spinner to the selected redeem button text
        reedMeBinding.redeemTextSelector.setSelection(getRedeemButtonTextPosition(listOptions, selectedRedeemButtonText))

        // Create and display the alert dialog
        val alert = MaterialAlertDialogBuilder(context)
            .setView(reedMeBinding.root)
            .setCancelable(false)
            .create()
        alert.show()

        // Handle cancel button click
        reedMeBinding.redeemDialogCancelBtn.setOnClickListener { alert.dismiss() }

        // Handle update button click
        reedMeBinding.redeemDialogUpdateBtn.setOnClickListener {
            val urlText = reedMeBinding.redeemWebsiteUrl.text.toString().trim().toLowerCase(Locale.ENGLISH)
            if (urlText.contains("https://") || urlText.contains("http://")) {
                showAlert(context, getString(R.string.without_protocol_error))
            } else {
                // Update the redeem button details
                couponRedeemButtonText = selectedRedeemButtonText
                couponRedeemButtonColor = selectedColor
                couponRedeemWebsiteUrl = reedMeBinding.redeemWebsiteUrl.text.toString().trim()

                view.setBackgroundColor(Color.parseColor(selectedColor))
                view.text = selectedRedeemButtonText

                binding.redeemNowEditHint.visibility = View.GONE
                binding.lavRedeemNowEditBtn.visibility = View.GONE
                binding.redeemNowEditBtn.setImageResource(R.drawable.green_checked_icon)

                alert.dismiss()
            }
        }

        // Handle color button click to show color picker
        reedMeBinding.redeemColorBtn.setOnClickListener {
            ColorPickerPopup.Builder(context)
                .initialColor(Color.RED) // Set initial color
                .enableBrightness(true) // Enable brightness slider
                .enableAlpha(true) // Enable alpha slider
                .okTitle(getString(R.string.chose_text))
                .cancelTitle(getString(R.string.cancel_text))
                .showIndicator(true)
                .showValue(true)
                .build()
                .show(reedMeBinding.redeemColorBtn, object : ColorPickerObserver() {
                    override fun onColorPicked(color: Int) {
                        val hexColor = "#" + Integer.toHexString(color).substring(2)
                        reedMeBinding.redeemColorBtn.setBackgroundColor(Color.parseColor(hexColor))
                        reedMeBinding.redeemColorTf.setText(hexColor)
                        selectedColor = hexColor
                    }

                })
        }

        // Handle redeem text spinner item selection
        reedMeBinding.redeemTextSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}

            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                val selectedItemText = adapterView?.getItemAtPosition(i).toString().trim()

                if (selectedItemText.equals("custom", ignoreCase = true)) {
                    reedMeBinding.redeemTextInputField.visibility = View.VISIBLE
                    reedMeBinding.redeemTextInputField.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            selectedRedeemButtonText = s.toString().trim()
                        }
                        override fun afterTextChanged(s: Editable?) {}
                    })
                } else {
                    reedMeBinding.redeemTextInputField.visibility = View.GONE
                    selectedRedeemButtonText = selectedItemText
                }
            }
        }
    }

    // Helper function to get the position of the text in the spinner options
    private fun getRedeemButtonTextPosition(options: Array<String>, text: String): Int {
        return options.indexOfFirst { it.equals(text, ignoreCase = true) }
    }

    // THIS FUNCTION WILL UPDATE TEXT AND COLOR
    private fun updateText(view: MaterialTextView, type: Int) {
        // Inflate the dialog layout using ViewBinding
        val dialogBinding = TextUpdateDialogBinding.inflate(LayoutInflater.from(context))

        // Set up references to views within the dialog
        val textColorLayout = dialogBinding.textTopLayout
        val cancelBtn = dialogBinding.couponDialogCancelBtn
        val updateBtn = dialogBinding.couponDialogUpdateBtn
        val inputBox = dialogBinding.couponTextInputField
        val colorBtnView = dialogBinding.textColorBtn
        val colorTextField = dialogBinding.textColorTf

        // Initialize selectedColor variable
        var selectedColor = ""

        // Pre-fill input and color fields based on the update type
        when (updateType) {
            "company" -> {
                if (couponCompanyNameText.isNotEmpty()) inputBox.setText(couponCompanyNameText)
                selectedColor = if (couponCompanyNameTextColor.isEmpty()) {
                    colorTextField.text.toString()
                } else {
                    couponCompanyNameTextColor.also {
                        colorTextField.setText(it)
                        colorBtnView.setBackgroundColor(Color.parseColor(it))
                    }
                }
            }
            "headline" -> {
                if (couponOfferTitleText.isNotEmpty()) inputBox.setText(couponOfferTitleText)
                selectedColor = if (couponOfferTitleTextColor.isEmpty()) {
                    colorTextField.text.toString()
                } else {
                    couponOfferTitleTextColor.also {
                        colorTextField.setText(it)
                        colorBtnView.setBackgroundColor(Color.parseColor(it))
                    }
                }
            }
            "description" -> {
                if (couponOfferDescriptionText.isNotEmpty()) inputBox.setText(couponOfferDescriptionText)
                selectedColor = if (couponOfferDescriptionTextColor.isEmpty()) {
                    colorTextField.text.toString()
                } else {
                    couponOfferDescriptionTextColor.also {
                        colorTextField.setText(it)
                        colorBtnView.setBackgroundColor(Color.parseColor(it))
                    }
                }
            }
            "coupon_code" -> {
                if (couponCodeText.isNotEmpty()) inputBox.setText(couponCodeText)
                selectedColor = if (couponCodeTextColor.isEmpty()) {
                    colorTextField.text.toString()
                } else {
                    couponCodeTextColor.also {
                        colorTextField.setText(it)
                        colorBtnView.setBackgroundColor(Color.parseColor(it))
                    }
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
        }

        // Show or hide the text color layout based on the type parameter
        textColorLayout.visibility = if (type == 1) View.VISIBLE else View.GONE

        // Create and show the dialog
        val alert = MaterialAlertDialogBuilder(context)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
        alert.show()

        // Handle cancel button click
        cancelBtn.setOnClickListener { alert.dismiss() }

        // Handle update button click
        updateBtn.setOnClickListener {
            val value = inputBox.text.toString().trim()
            if (value.isNotEmpty()) {
                view.text = value
                if (type == 1 && selectedColor.isNotEmpty()) {
                    view.setTextColor(Color.parseColor(selectedColor))
                }

                // Update the corresponding text and color variables
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
                }
                alert.dismiss()
            } else {
                showAlert(context, getString(R.string.empty_text_error))
            }
        }

        // Handle color picker button click
        colorBtnView.setOnClickListener {
            ColorPickerPopup.Builder(context)
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
                })
        }
    }

    // THIS FUNCTION WILL UPDATE TEXT AND COLOR
    private fun updateTextAndColor(view: AppCompatButton, type: Int) {
        // Inflate the dialog layout using ViewBinding
        val dialogBinding = TextWithColorUpdateDialogBinding.inflate(LayoutInflater.from(context))

        var selectedColor = ""

        // Set initial values based on the update type
        when (updateType) {
            "sale_badge" -> {
                if (couponSaleBadgeButtonText.isNotEmpty()) {
                    dialogBinding.textWithColorTextInputField.setText(couponSaleBadgeButtonText)
                }
                selectedColor = if (couponSaleBadgeButtonColor.isEmpty()) {
                    dialogBinding.textWithColorColorTf.text.toString()
                } else {
                    couponSaleBadgeButtonColor.also {
                        dialogBinding.textWithColorColorTf.setText(it)
                        dialogBinding.textWithColorColorBtn.setBackgroundColor(Color.parseColor(it))
                    }
                }
            }
            "get_coupon_btn" -> {
                if (couponGetButtonText.isNotEmpty()) {
                    dialogBinding.textWithColorTextInputField.setText(couponGetButtonText)
                }
                selectedColor = if (couponGetButtonColor.isEmpty()) {
                    dialogBinding.textWithColorColorTf.text.toString()
                } else {
                    couponGetButtonColor.also {
                        dialogBinding.textWithColorColorTf.setText(it)
                        dialogBinding.textWithColorColorBtn.setBackgroundColor(Color.parseColor(it))
                    }
                }
            }
        }

        // Toggle visibility of elements based on the type parameter
        if (type == 0) {
            dialogBinding.textWithColorTextInputField.visibility = View.GONE
            dialogBinding.textWithColorSaleBadgeWrapper.visibility = View.VISIBLE
        } else {
            dialogBinding.textWithColorSaleBadgeWrapper.visibility = View.GONE
            dialogBinding.textWithColorTextInputField.visibility = View.VISIBLE
        }

        // Setup sale badge spinner and handle custom badge input visibility
        selectedSaleBadgeText = binding.couponSaleBadge.text.toString()
        val listOptions = resources.getStringArray(R.array.sale_badge_options)
        val position = getPositionFromText(listOptions, selectedSaleBadgeText)
        dialogBinding.textWithColorSaleBadgeSelector.setSelection(position)
        dialogBinding.textWithColorCustomSaleBadge.visibility = if (position == -1) View.VISIBLE else View.GONE

        // Create and show the dialog
        val alert = MaterialAlertDialogBuilder(context)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
        alert.show()

        // Handle color selection
        dialogBinding.textWithColorColorBtn.setOnClickListener {
            ColorPickerPopup.Builder(this)
                .initialColor(Color.RED)
                .enableBrightness(true)
                .enableAlpha(true)
                .okTitle(getString(R.string.chose_text))
                .cancelTitle(getString(R.string.cancel_text))
                .showIndicator(true)
                .showValue(true)
                .build()
                .show(dialogBinding.textWithColorColorBtn, object : ColorPickerObserver() {
                    override fun onColorPicked(color: Int) {
                        val hexColor = "#" + Integer.toHexString(color).substring(2)
                        dialogBinding.textWithColorColorBtn.setBackgroundColor(Color.parseColor(hexColor))
                        dialogBinding.textWithColorColorTf.setText(hexColor)
                        selectedColor = hexColor
                    }
                })
        }

        // Handle sale badge selection
        dialogBinding.textWithColorSaleBadgeSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}

            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                val selectedItemText = adapterView!!.getItemAtPosition(i).toString()
                dialogBinding.textWithColorCustomSaleBadge.visibility = if (selectedItemText.toLowerCase(Locale.ENGLISH) == "custom") {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                selectedSaleBadgeText = selectedItemText.trim()

                dialogBinding.textWithColorCustomSaleBadge.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        selectedSaleBadgeText = s.toString().trim()
                    }
                    override fun afterTextChanged(s: Editable?) {}
                })
            }
        }

        // Handle dialog cancel button click
        dialogBinding.textWithColorDialogCancelBtn.setOnClickListener { alert.dismiss() }

        // Handle dialog update button click
        dialogBinding.textWithColorDialogUpdateBtn.setOnClickListener {
            // Update button text and color
            view.text = if (type == 0) selectedSaleBadgeText else dialogBinding.textWithColorTextInputField.text.toString()
            if (selectedColor.isNotEmpty()) {
                view.setBackgroundColor(Color.parseColor(selectedColor))
            }

            // Update global variables based on update type
            when (updateType) {
                "sale_badge" -> {
                    couponSaleBadgeButtonText = view.text.toString().trim()
                    couponSaleBadgeButtonColor = selectedColor
                    binding.saleBadgeHint.visibility = View.GONE
                    binding.lavSaleBadgeEditBtn.visibility = View.GONE
                    binding.saleBadgeEditBtn.setImageResource(R.drawable.green_checked_icon)
                }
                "get_coupon_btn" -> {
                    couponGetButtonText = view.text.toString().trim()
                    couponGetButtonColor = selectedColor
                    binding.getCouponEditHint.visibility = View.GONE
                    binding.lavGetCouponEditBtn.visibility = View.GONE
                    binding.lavGetCouponEditBtn.setImageResource(R.drawable.green_checked_icon)
                }
            }
            alert.dismiss()
        }
    }

    // THIS FUNCTION WILL RETURN THE INDEX OF SALE BADGE TEXT FROM LIST
    private fun getPositionFromText(listOptions: Array<String>, text: String): Int {
        // Convert the array to a list
        val list = listOptions.toList()

        // Initialize the position to -1 (not found)
        var position = -1

        // Loop through the list to find the matching text
        for (item in list) {
            // Skip the iteration if the item is "custom" (case-insensitive)
            if (item.equals("custom", ignoreCase = true)) {
                continue
            } else if (item.equals(text, ignoreCase = true)) {
                // If the item matches the text, get the index and break the loop
                position = list.indexOf(item)
                break
            }
        }

        // Return the found position or -1 if not found
        return position
    }

    // THIS FUNCTION WILL OPEN FOR COLOR SELECTION
    private fun openColorDialog(view: View) {
        // Create a ColorPickerPopup instance with builder pattern
        ColorPickerPopup.Builder(this)
            .initialColor(Color.RED) // Set the initial color to red
            .enableBrightness(true) // Enable brightness slider
            .enableAlpha(true) // Enable alpha (transparency) slider
            .okTitle(getString(R.string.chose_text)) // Set OK button title
            .cancelTitle(getString(R.string.cancel_text)) // Set Cancel button title
            .showIndicator(true) // Show color indicator
            .showValue(true) // Show color value
            .build()
            .show(view, object : ColorPickerObserver() {
                // Override onColorPicked to handle color selection
                override fun onColorPicked(color: Int) {
                    // Convert color to hex string and update the background color
                    val hexColor = String.format("#%06X", (0xFFFFFF and color))
                    binding.couponWrapperLayout.setBackgroundColor(Color.parseColor(hexColor))
                    couponBackgroundColor = hexColor

                    // Update visibility of UI elements based on the color selection
                    binding.backgroundColorEditHint.visibility = View.GONE
                    binding.lavBackgroundColorEditBtn.visibility = View.GONE
                    binding.backgroundColorEditBtn.setImageResource(R.drawable.green_checked_icon)
                }

            })
    }

    // THIS FUNCTION WILL USED FOR CALLBACK OF SELECTED DATE
    override fun onDateSet(picker: DatePicker?, year: Int, month: Int, day: Int) {
        // Get an instance of the Calendar class
        val calendar = Calendar.getInstance().apply {
            // Set the calendar to the selected date
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }

        // Convert the selected date to a formatted string
        val selectedDate = getDateFromTimeStamp(calendar.timeInMillis)

        // Update the coupon valid date and UI elements
        couponValidDate = selectedDate
        with(binding) {
            couponValidTillTextView.text = selectedDate
            // Hide the edit button and show the checked icon
            lavCouponValidTillEditBtn.visibility = View.GONE
            couponValidTillEditBtn.setImageResource(R.drawable.green_checked_icon)
        }
    }


}