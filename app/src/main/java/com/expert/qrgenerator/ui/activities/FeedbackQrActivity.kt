package com.expert.qrgenerator.ui.activities

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.ActivityFeedbackQrBinding
import com.expert.qrgenerator.databinding.TextUpdateDialogBinding
import com.expert.qrgenerator.databinding.TextWithColorUpdateDialogBinding
import com.expert.qrgenerator.utils.GeneratorManager
import com.expert.qrgenerator.viewmodel.FeedbackQrViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import top.defaults.colorpicker.ColorPickerPopup

@AndroidEntryPoint
class FeedbackQrActivity : BaseActivity(), View.OnClickListener {

    // ViewBinding instance for the activity layout
    private lateinit var binding: ActivityFeedbackQrBinding

    // Context of the activity, initialized using lazy delegation
    private val context: Context by lazy { this }

    // Feedback-related properties
    private var feedbackTitleText: String = ""
    private var feedbackTitleBackgroundColor: String = ""
    private var feedbackInnerTitleText: String = ""
    private var feedbackInnerDescriptionText: String = ""
    private var feedbackSendButtonText: String = ""
    private var feedbackSendButtonColor: String = ""
    private var feedbackOwnerEmail: String = ""

    // QR-related properties
    private var qrId: String = ""

    // ViewModel for handling business logic
    private val viewModel: FeedbackQrViewModel by viewModels()

    // Type of update operation
    private var updateType: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        logCustomEvent(eventName = "screen_feedback_opened")

        // Inflate the layout using ViewBinding
        binding = ActivityFeedbackQrBinding.inflate(layoutInflater)

        // Set the content view to the root of the binding layout
        setContentView(binding.root)

        // Initialize views (e.g., set up listeners, default values, etc.)
        initViews()

        // Set up the toolbar (e.g., configure title, navigation, etc.)
        setUpToolbar()
    }

    override fun onResume() {
        super.onResume()
        logEvent()
    }

    private fun logEvent() {
        val mainAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        // Log the custom event
        mainAnalytics.logEvent("screen_feedback_opened", bundle)
    }

    // THIS FUNCTION WILL INITIALIZE ALL THE VIEWS AND REFERENCE OF OBJECTS
    private fun initViews() {
        // Set up click listeners for the buttons
        // Button to proceed to the next step
        binding.nextStepBtn.setOnClickListener(this)

        // Button to edit the title of feedback
        binding.feedbackTitleTextEditBtn.setOnClickListener(this)

        // Button to edit the inner text of feedback
        binding.feedbackInnerTextEditBtn.setOnClickListener(this)

        // Button to edit the inner description of feedback
        binding.feedbackInnerDescriptionEditBtn.setOnClickListener(this)

        // Button to send feedback after editing
        binding.feedbackSendButtonEditBtn.setOnClickListener(this)
    }

    // THIS FUNCTION WILL RENDER THE ACTION BAR/TOOLBAR
    private fun setUpToolbar() {
        // Set the toolbar as the support action bar
        setSupportActionBar(binding.toolbar)

        // Get the ActionBar and check if it's not null
        supportActionBar?.apply {
            // Set the title of the action bar
            title = getString(R.string.feedback_qr)

            // Enable the back button in the action bar
            setDisplayHomeAsUpEnabled(true)
        }

        // Set the title text color of the toolbar
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.black))
    }

    // THIS FUNCTION WILL HANDLE THE ON BACK ARROW CLICK EVENT
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Check if the selected item is the home button
        if (item.itemId == android.R.id.home) {
            // Determine if there are any unsaved changes
            val hasUnsavedChanges = feedbackTitleText.isNotEmpty() ||
                    feedbackTitleBackgroundColor.isNotEmpty() ||
                    feedbackInnerTitleText.isNotEmpty() ||
                    feedbackInnerDescriptionText.isNotEmpty() ||
                    feedbackSendButtonText.isNotEmpty() ||
                    feedbackSendButtonColor.isNotEmpty()

            if (hasUnsavedChanges) {
                // Show a dialog asking the user to confirm leaving with unsaved changes
                MaterialAlertDialogBuilder(context)
                    .setMessage(getString(R.string.changes_saved_alert_text))
                    .setNegativeButton(getString(R.string.cancel_text)) { dialog, _ ->
                        // Dismiss the dialog if the user chooses to cancel
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.leave_text)) { _, _ ->
                        // Handle leaving the activity when the user confirms
                        onBackPressed()
                    }
                    .create()
                    .show()
            } else {
                // Simply go back if there are no unsaved changes
                onBackPressed()
            }
            return true
        }
        // For other menu items, use the default behavior
        return super.onOptionsItemSelected(item)
    }

    // THIS FUNCTION WILL HANDLE THE ALL BUTTONS CLICK EVENT
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.next_step_btn -> {
                if (validation()) {
                    // Prepare the data to be sent to ViewModel
                    val hashMap = hashMapOf<String, String>().apply {
                        qrId = "${System.currentTimeMillis()}"
                        put("feedback_title_text", feedbackTitleText)
                        put("feedback_title_background_color", feedbackTitleBackgroundColor)
                        put("feedback_inner_title_text", feedbackInnerTitleText)
                        put("feedback_inner_description_text", feedbackInnerDescriptionText)
                        put("feedback_send_button_text", feedbackSendButtonText)
                        put("feedback_send_button_color", feedbackSendButtonColor)
                        put("feedback_owner_email", feedbackOwnerEmail)
                        put("feedback_qr_id", qrId)
                    }

                    // Start loading indicator
                    startLoading(context)

                    // Launch coroutine to create feedback QR code
                    lifecycleScope.launch {
                        viewModel.createFeedbackQrCode(hashMap)

                        // Observe the response from ViewModel
                        viewModel.feedbackQrCodeResponse.observe(this@FeedbackQrActivity) { response ->
                            // Check if response is not null
                            if (response != null) {
                                val url = response.get("generatedUrl").asString

                                GeneratorManager.generateQRCode(this@FeedbackQrActivity,url,"feedback")

                            } else {
                                // Show error message if response is null
                                showAlert(context, getString(R.string.something_wrong_error))
                            }

                            // Dismiss loading indicator
                            dismiss()
                        }
                    }
                }
            }

            // Handle feedback title text edit button click
            R.id.feedback_title_text_edit_btn -> {
                updateType = "feedback_title"
                updateTextAndColor(binding.feedbackSendButton, 1)
            }

            // Handle feedback inner title text edit button click
            R.id.feedback_inner_text_edit_btn -> {
                updateType = "inner_title"
                updateText(binding.feedbackInnerTitleText, 0)
            }

            // Handle feedback inner description text edit button click
            R.id.feedback_inner_description_edit_btn -> {
                updateType = "inner_description"
                updateText(binding.feedbackInnerDescriptionText, 0)
            }

            // Handle feedback send button edit button click
            R.id.feedback_send_button_edit_btn -> {
                updateType = "feedback_send_btn"
                updateTextAndColor(binding.feedbackSendButton, 1)
            }

            else -> {
                // Handle other cases or do nothing
            }
        }
    }

    // THIS FUNCTION WILL VALIDATE ALL THE COUPON INPUT DATA
    private fun validation(): Boolean {
        // Check if the title is empty
        if (feedbackTitleText.isEmpty()) {
            showAlert(context, getString(R.string.feedback_text_background_color_error_text))
            return false
        }

        // Check if the inner title is empty
        if (feedbackInnerTitleText.isEmpty()) {
            showAlert(context, getString(R.string.feedback_inner_title_error_text))
            return false
        }

        // Check if the inner description is empty
        if (feedbackInnerDescriptionText.isEmpty()) {
            showAlert(context, getString(R.string.feedback_inner_description_error_text))
            return false
        }

        // Check if the send button text is empty
        if (feedbackSendButtonText.isEmpty()) {
            showAlert(context, getString(R.string.feedback_send_button_text_color_error_text))
            return false
        }

        // All fields are valid
        return true
    }

    // THIS FUNCTION WILL OPEN AND UPDATE TEXT
    private fun updateText(view: MaterialTextView, type: Int) {
        // Inflate the dialog layout using ViewBinding
        val updateBinding = TextUpdateDialogBinding.inflate(LayoutInflater.from(context))

        // Initialize views from the ViewBinding
        val textColorLayout = updateBinding.textTopLayout
        val cancelBtn = updateBinding.couponDialogCancelBtn
        val updateBtn = updateBinding.couponDialogUpdateBtn
        val inputBox = updateBinding.couponTextInputField
        val colorBtnView = updateBinding.textColorBtn
        val colorTextField = updateBinding.textColorTf

        var selectedColor = ""

        // Pre-fill the input box based on the update type
        when (updateType) {
            "inner_title" -> inputBox.setText(feedbackInnerTitleText.takeIf { it.isNotEmpty() })
            "inner_description" -> inputBox.setText(feedbackInnerDescriptionText.takeIf { it.isNotEmpty() })
        }

        // Show or hide the color layout based on the type
        textColorLayout.visibility = if (type == 1) View.VISIBLE else View.GONE

        // Create and show the dialog
        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(updateBinding.root)
        builder.setCancelable(false)
        val alert = builder.create()
        alert.show()

        // Handle cancel button click
        cancelBtn.setOnClickListener {
            alert.dismiss()
        }

        // Handle update button click
        updateBtn.setOnClickListener {
            val value = inputBox.text.toString().trim()
            if (value.isNotEmpty()) {
                // Update the view text and color
                view.text = value
                if (type == 1 && selectedColor.isNotEmpty()) {
                    view.setTextColor(Color.parseColor(selectedColor))
                }

                // Update the appropriate feedback text
                when (updateType) {
                    "inner_title" -> {
                        feedbackInnerTitleText = value
                        binding.feedbackInnerTitleText.setTextColor(Color.BLACK)
                        binding.lavFeedbackInnerTextEditBtn.visibility = View.GONE
                        binding.feedbackInnerTextEditBtn.setImageResource(R.drawable.green_checked_icon)
                    }
                    "inner_description" -> {
                        feedbackInnerDescriptionText = value
                        binding.feedbackInnerDescriptionText.setTextColor(Color.BLACK)
                        binding.lavFeedbackInnerTextEditBtn.visibility = View.GONE
                        binding.feedbackInnerDescriptionEditBtn.setImageResource(R.drawable.green_checked_icon)
                    }
                }
                alert.dismiss()
            } else {
                // Show error message if the input is empty
                showAlert(context, getString(R.string.empty_text_error))
            }
        }

        // Handle color button click
        colorBtnView.setOnClickListener {
            ColorPickerPopup.Builder(this)
                .initialColor(Color.RED) // Set initial color
                .enableBrightness(true) // Enable brightness slider
                .enableAlpha(true) // Enable alpha slider
                .okTitle(getString(R.string.chose_text))
                .cancelTitle(getString(R.string.cancel_text))
                .showIndicator(true)
                .showValue(true)
                .build()
                .show(colorBtnView, object : ColorPickerPopup.ColorPickerObserver() {
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
        // Inflate the dialog layout using View Binding
        val dialogBinding = TextWithColorUpdateDialogBinding.inflate(LayoutInflater.from(context))

        // Extract views from the binding
        val cancelBtn = dialogBinding.textWithColorDialogCancelBtn
        val updateBtn = dialogBinding.textWithColorDialogUpdateBtn
        val inputBox = dialogBinding.textWithColorTextInputField
        val saleBadgeWrapperLayout = dialogBinding.textWithColorSaleBadgeWrapper
        val saleBadgeSpinner = dialogBinding.textWithColorSaleBadgeSelector
        val customSaleBadgeView = dialogBinding.textWithColorCustomSaleBadge
        val colorBtnView = dialogBinding.textWithColorColorBtn
        val colorTextField = dialogBinding.textWithColorColorTf

        // Variable to store the selected color
        var selectedColor = ""

        // Set initial values based on updateType
        when (updateType) {
            "feedback_title" -> {
                if (feedbackTitleText.isNotEmpty()) {
                    inputBox.setText(feedbackTitleText)
                }
                selectedColor = if (feedbackTitleBackgroundColor.isEmpty()) {
                    colorTextField.text.toString()
                } else {
                    feedbackTitleBackgroundColor.also {
                        colorTextField.setText(it)
                        colorBtnView.setBackgroundColor(Color.parseColor(it))
                    }
                }
            }
            "feedback_send_btn" -> {
                if (feedbackSendButtonText.isNotEmpty()) {
                    inputBox.setText(feedbackSendButtonText)
                }
                selectedColor = if (feedbackSendButtonColor.isEmpty()) {
                    colorTextField.text.toString()
                } else {
                    feedbackSendButtonColor.also {
                        colorTextField.setText(it)
                        colorBtnView.setBackgroundColor(Color.parseColor(it))
                    }
                }
            }
            else -> {
                // Handle other cases if needed
            }
        }

        // Show/hide elements based on the type parameter
        inputBox.visibility = if (type == 0) View.GONE else View.VISIBLE
        saleBadgeWrapperLayout.visibility = if (type == 0) View.VISIBLE else View.GONE

        // Create and show the dialog
        val alert = MaterialAlertDialogBuilder(context)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
        alert.show()

        // Handle color button click to open color picker
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
                .show(colorBtnView, object : ColorPickerPopup.ColorPickerObserver() {
                    override fun onColorPicked(color: Int) {
                        val hexColor = "#" + Integer.toHexString(color).substring(2)
                        colorBtnView.setBackgroundColor(Color.parseColor(hexColor))
                        colorTextField.setText(hexColor)
                        selectedColor = hexColor
                    }

                    fun onColor(color: Int, fromUser: Boolean) {
                        // Handle color changes if needed
                    }
                })
        }

        // Handle cancel button click
        cancelBtn.setOnClickListener { alert.dismiss() }

        // Handle update button click
        updateBtn.setOnClickListener {
            val value = inputBox.text.toString().trim()
            if (value.isNotEmpty()) {
                when (updateType) {
                    "feedback_title" -> {
                        feedbackTitleText = value
                        feedbackTitleBackgroundColor = selectedColor
                        binding.feedbackTitleTextLayout.setBackgroundColor(Color.parseColor(selectedColor))
                        binding.feedbackTitleText.text = value
                        binding.feedbackTitleText.setTextColor(Color.WHITE)
                        binding.lavFeedbackInnerTextEditBtn.visibility = View.GONE
                        binding.feedbackTitleTextEditBtn.setImageResource(R.drawable.green_checked_icon)
                    }
                    "feedback_send_btn" -> {
                        feedbackSendButtonText = value
                        feedbackSendButtonColor = selectedColor
                        view.setBackgroundColor(Color.parseColor(selectedColor))
                        view.text = value
                        if (value.isNotEmpty()) {
                            binding.feedbackSendEditHint.visibility = View.GONE
                        }
                        binding.feedbackSendButtonEditBtn.visibility = View.GONE
                        binding.feedbackSendButtonEditBtn.setImageResource(R.drawable.green_checked_icon)
                    }
                    else -> {
                        // Handle other cases if needed
                    }
                }
                alert.dismiss()
            } else {
                showAlert(context, getString(R.string.empty_text_error))
            }
        }
    }

}