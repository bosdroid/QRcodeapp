package com.expert.qrgenerator.view.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.expert.qrgenerator.R
import com.expert.qrgenerator.model.QRHistory
import com.expert.qrgenerator.viewmodel.CouponQrViewModel
import com.expert.qrgenerator.viewmodel.FeedbackQrViewModel
import com.expert.qrgenerator.viewmodelfactory.ViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import top.defaults.colorpicker.ColorPickerPopup
import java.util.*

class FeedbackQrActivity : BaseActivity(), View.OnClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var nextBtnView: MaterialTextView
    private lateinit var feedbackTitleTextLayout:LinearLayout
    private lateinit var feedbackTitleEditBtn: AppCompatImageView
    private lateinit var feedbackInnerTitleEditBtn: AppCompatImageView
    private lateinit var feedbackInnerDescriptionEditBtn: AppCompatImageView
    private lateinit var feedbackSendButtonEditBtn: AppCompatImageView
    private lateinit var feedbackTitleTextView: MaterialTextView
    private lateinit var feedbackInnerTitleTextView: MaterialTextView
    private lateinit var feedbackInnerDescriptionTextView: MaterialTextView
    private lateinit var feedbackSendBtn: AppCompatButton
    private var feedbackTitleText: String = ""
    private var feedbackTitleBackgroundColor: String = ""
    private var feedbackInnerTitleText: String = ""
    private var feedbackInnerDescriptionText: String = ""
    private var feedbackSendButtonText: String = ""
    private var feedbackSendButtonColor: String = ""
    private var qrId: String = ""
    private lateinit var viewModel: FeedbackQrViewModel
    private var updateType = ""
    private lateinit var feedbackSendEditHint:MaterialTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback_qr)

        initViews()
        setUpToolbar()

    }

    // THIS FUNCTION WILL INITIALIZE ALL THE VIEWS AND REFERENCE OF OBJECTS
    private fun initViews() {
        context = this
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(FeedbackQrViewModel()).createFor()
        )[FeedbackQrViewModel::class.java]
        toolbar = findViewById(R.id.toolbar)
        nextBtnView = findViewById(R.id.next_step_btn)
        nextBtnView.setOnClickListener(this)
        feedbackTitleTextLayout = findViewById(R.id.feedback_title_text_layout)
        feedbackTitleEditBtn = findViewById(R.id.feedback_title_text_edit_btn)
        feedbackTitleEditBtn.setOnClickListener(this)
        feedbackInnerTitleEditBtn = findViewById(R.id.feedback_inner_text_edit_btn)
        feedbackInnerTitleEditBtn.setOnClickListener(this)
        feedbackInnerDescriptionEditBtn = findViewById(R.id.feedback_inner_description_edit_btn)
        feedbackInnerDescriptionEditBtn.setOnClickListener(this)
        feedbackSendButtonEditBtn = findViewById(R.id.feedback_send_button_edit_btn)
        feedbackSendButtonEditBtn.setOnClickListener(this)
        feedbackTitleTextView = findViewById(R.id.feedback_title_text)
        feedbackInnerTitleTextView = findViewById(R.id.feedback_inner_title_text)
        feedbackInnerDescriptionTextView = findViewById(R.id.feedback_inner_description_text)
        feedbackSendBtn = findViewById(R.id.feedback_send_button)
        feedbackSendEditHint = findViewById(R.id.feedback_send_edit_hint)

    }

    // THIS FUNCTION WILL RENDER THE ACTION BAR/TOOLBAR
    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.feedback_qr)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
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
                    .setMessage("Changes you made may not be saved.")
                    .setNegativeButton("Cancel") { dialog, which ->
                        dialog.dismiss()
                    }
                    .setPositiveButton("Leave") { dialog, which ->
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
                    hashMap["feedback_qr_id"] = qrId

                    startLoading(context)
                    viewModel.createFeedbackQrCode(context, hashMap)
                    viewModel.getFeedbackQrCode().observe(this, { response ->
                        var url = ""
                        dismiss()
                        if (response != null) {
                            url = response.get("generatedUrl").asString

                            // SETUP QR DATA HASMAP FOR HISTORY
                            val qrData = hashMapOf<String, String>()
                            qrData["login"] = "sattar"
                            qrData["qrId"] = qrId
                            qrData["userType"] = "free"

                            val qrHistory = QRHistory(
                                qrData["login"]!!,
                                qrData["qrId"]!!,
                                url,
                                "feedback",
                                qrData["userType"]!!,
                                "",
                                0,
                                "",
                                System.currentTimeMillis()
                            )

                            val intent = Intent(context, DesignActivity::class.java)
                            intent.putExtra("ENCODED_TEXT", url)
                            intent.putExtra("QR_HISTORY", qrHistory)
                            startActivity(intent)
                        } else {
                            showAlert(context, "Something went wrong, please try again!")
                        }


                    })
                }
            }
            R.id.feedback_title_text_edit_btn -> {
                updateType = "feedback_title"
                updateTextAndColor(feedbackSendBtn,1)

            }
            R.id.feedback_inner_text_edit_btn -> {
                updateType = "inner_title"
                updateText(feedbackInnerTitleTextView,0)
            }
            R.id.feedback_inner_description_edit_btn -> {
                updateType = "inner_description"
                updateText(feedbackInnerDescriptionTextView,0)
            }
            R.id.feedback_send_button_edit_btn -> {
                updateType = "feedback_send_btn"
                updateTextAndColor(feedbackSendBtn,1)
            }
            else -> {

            }
        }
    }

    // THIS FUNCTION WILL VALIDATE ALL THE COUPON INPUT DATA
    private fun validation(): Boolean {
        if (feedbackTitleText.isEmpty()) {
            showAlert(context, "Please edit the text and background color!")
            return false
        } else if (feedbackInnerTitleText.isEmpty()) {
            showAlert(context, "Please edit the title text!")
            return false
        } else if (feedbackInnerDescriptionText.isEmpty()) {
            showAlert(context, "Please edit the description text!")
            return false
        } else if (feedbackSendButtonText.isEmpty()) {
            showAlert(context, "Please edit the button text and color!")
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
        //inputBox.setText(view.text.toString())
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
            view.text = value
            if (type == 1) {
                if (selectedColor.isNotEmpty()) {
                    view.setTextColor(Color.parseColor(selectedColor))

                }
            }
            when (updateType) {
                "inner_title" -> {
                    feedbackInnerTitleText = value
                    feedbackInnerTitleTextView.setTextColor(Color.BLACK)
                }
                "inner_description" -> {
                    feedbackInnerDescriptionText = value
                    feedbackInnerDescriptionTextView.setTextColor(Color.BLACK)
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
                .okTitle("Choose")
                .cancelTitle("Cancel")
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
            when (updateType) {
                "feedback_title" -> {
                    feedbackTitleText = value
                    feedbackTitleBackgroundColor = selectedColor
                    feedbackTitleTextLayout.setBackgroundColor(Color.parseColor(selectedColor))
                    if (value.isNotEmpty()){
                        feedbackTitleTextView.text = value
                    }
                    feedbackTitleTextView.setTextColor(Color.WHITE)
                }
                "feedback_send_btn" -> {
                    feedbackSendButtonText = value
                    feedbackSendButtonColor = selectedColor
                    view.setBackgroundColor(Color.parseColor(selectedColor))
                    view.text = value
                    if (value.isNotEmpty()){
                        feedbackSendEditHint.visibility = View.GONE
                    }
                }
                else -> {

                }
            }

            alert.dismiss()
        }
    }
}