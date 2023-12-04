package com.expert.qrgenerator.ui.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.airbnb.lottie.LottieAnimationView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.ActivityFeedbackQrBinding
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.viewmodel.FeedbackQrViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import top.defaults.colorpicker.ColorPickerPopup

@AndroidEntryPoint
class FeedbackQrActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding:ActivityFeedbackQrBinding
    private lateinit var context: Context

    private var feedbackTitleText: String = ""
    private var feedbackTitleBackgroundColor: String = ""
    private var feedbackInnerTitleText: String = ""
    private var feedbackInnerDescriptionText: String = ""
    private var feedbackSendButtonText: String = ""
    private var feedbackSendButtonColor: String = ""
    private var feedbackOwnerEmail: String = ""

    private var qrId: String = ""
    private val viewModel: FeedbackQrViewModel by viewModels()
    private var updateType = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackQrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setUpToolbar()

    }

    // THIS FUNCTION WILL INITIALIZE ALL THE VIEWS AND REFERENCE OF OBJECTS
    private fun initViews() {
        context = this

        binding.nextStepBtn.setOnClickListener(this)

        binding.feedbackTitleTextEditBtn.setOnClickListener(this)

        binding.feedbackInnerTextEditBtn.setOnClickListener(this)

        binding.feedbackInnerDescriptionEditBtn.setOnClickListener(this)

        binding.feedbackSendButtonEditBtn.setOnClickListener(this)


    }

    // THIS FUNCTION WILL RENDER THE ACTION BAR/TOOLBAR
    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = getString(R.string.feedback_qr)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    // THIS FUNCTION WILL HANDLE THE ON BACK ARROW CLICK EVENT
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            if (feedbackTitleText.isEmpty()
                && feedbackTitleBackgroundColor.isEmpty()
                && feedbackInnerTitleText.isEmpty()
                && feedbackInnerDescriptionText.isEmpty()
                && feedbackSendButtonText.isEmpty()
                && feedbackSendButtonColor.isEmpty()
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


                    if (validation()) {

                        val hashMap = hashMapOf<String, String>()
                        qrId = "${System.currentTimeMillis()}"
                        hashMap["feedback_title_text"] = feedbackTitleText
                        hashMap["feedback_title_background_color"] = feedbackTitleBackgroundColor
                        hashMap["feedback_inner_title_text"] = feedbackInnerTitleText
                        hashMap["feedback_inner_description_text"] = feedbackInnerDescriptionText
                        hashMap["feedback_send_button_text"] = feedbackSendButtonText
                        hashMap["feedback_send_button_color"] = feedbackSendButtonColor
                        hashMap["feedback_owner_email"] = feedbackOwnerEmail
                        hashMap["feedback_qr_id"] = qrId

                        startLoading(context)
                        viewModel.createFeedbackQrCode(hashMap)
                        viewModel.getFeedbackQrCode().observe(this, { response ->
                            var url = ""
                            dismiss()
                            if (response != null) {
                                url = response.get("generatedUrl").asString

                                // SETUP QR DATA HASMAP FOR HISTORY
                                val qrData = hashMapOf<String, String>()
                                qrData["login"] = "qrmagicapp"
                                qrData["qrId"] = qrId
                                qrData["userType"] = "free"

                                val qrHistory = CodeHistory(
                                    qrData["login"]!!,
                                    qrData["qrId"]!!,
                                    url,
                                    "feedback",
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
//                }

            }
            R.id.feedback_title_text_edit_btn -> {
                updateType = "feedback_title"
                updateTextAndColor(binding.feedbackSendButton,1)

            }
            R.id.feedback_inner_text_edit_btn -> {
                updateType = "inner_title"
                updateText(binding.feedbackInnerTitleText,0)
            }
            R.id.feedback_inner_description_edit_btn -> {
                updateType = "inner_description"
                updateText(binding.feedbackInnerDescriptionText,0)
            }
            R.id.feedback_send_button_edit_btn -> {
                updateType = "feedback_send_btn"
                updateTextAndColor(binding.feedbackSendButton,1)
            }
            else -> {

            }
        }
    }

    // THIS FUNCTION WILL VALIDATE ALL THE COUPON INPUT DATA
    private fun validation(): Boolean {
        if (feedbackTitleText.isEmpty()) {
            showAlert(context, getString(R.string.feedback_text_background_color_error_text))
            return false
        } else if (feedbackInnerTitleText.isEmpty()) {
            showAlert(context, getString(R.string.feedback_inner_title_error_text))
            return false
        } else if (feedbackInnerDescriptionText.isEmpty()) {
            showAlert(context, getString(R.string.feedback_inner_description_error_text))
            return false
        } else if (feedbackSendButtonText.isEmpty()) {
            showAlert(context, getString(R.string.feedback_send_button_text_color_error_text))
            return false
        }
        return true
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

        var selectedColor = ""
        when (updateType) {
            "inner_title" -> {
                if (feedbackInnerTitleText.isNotEmpty()) {
                    inputBox.setText(feedbackInnerTitleText)
                }
            }
            "inner_description" -> {
                if (feedbackInnerDescriptionText.isNotEmpty()) {
                    inputBox.setText(feedbackInnerDescriptionText)
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
                .show(colorBtnView, object : ColorPickerPopup.ColorPickerObserver() {
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
            "feedback_title" -> {
                if (feedbackTitleText.isNotEmpty()) {
                    inputBox.setText(feedbackTitleText)
                }
                if (feedbackTitleBackgroundColor.isEmpty()) {
                    selectedColor = colorTextField.text.toString()
                } else {
                    selectedColor = feedbackTitleBackgroundColor
                    colorTextField.setText(selectedColor)
                    colorBtnView.setBackgroundColor(Color.parseColor(selectedColor))
                }
            }
            "feedback_send_btn" -> {
                if (feedbackSendButtonText.isNotEmpty()) {
                    inputBox.setText(feedbackSendButtonText)
                }
                if (feedbackSendButtonColor.isEmpty()) {
                    selectedColor = colorTextField.text.toString()
                } else {
                    selectedColor = feedbackSendButtonColor
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
                .show(colorBtnView, object : ColorPickerPopup.ColorPickerObserver() {
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

        cancelBtn.setOnClickListener { alert.dismiss() }
        updateBtn.setOnClickListener {

            val value = inputBox.text.toString().trim()
            if (value.isNotEmpty()) {
                when (updateType) {
                    "feedback_title" -> {
                        feedbackTitleText = value
                        feedbackTitleBackgroundColor = selectedColor
                        binding.feedbackTitleTextLayout.setBackgroundColor(Color.parseColor(selectedColor))
                        if (value.isNotEmpty()) {
                            binding.feedbackTitleText.text = value
                        }
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

                    }
                }

                alert.dismiss()
            }
            else{
                showAlert(context,getString(R.string.empty_text_error))
            }
        }
    }
}