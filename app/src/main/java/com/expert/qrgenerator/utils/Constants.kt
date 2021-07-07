package com.expert.qrgenerator.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import com.expert.qrgenerator.R
import com.expert.qrgenerator.interfaces.OnCompleteAction
import com.expert.qrgenerator.model.QRTypes
import com.expert.qrgenerator.model.User
import com.expert.qrgenerator.view.activities.BaseActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.api.services.drive.Drive
import com.google.api.services.sheets.v4.Sheets
import java.io.File
import java.util.regex.Pattern


class Constants {

    // HERE WE WILL CREATE ALL THE CONSTANT DATA
    companion object {
        const val firebaseBackgroundImages = "backgroundImages"
        const val firebaseLogoImages = "logoImages"
        const val firebaseFonts = "fonts"
        const val READ_STORAGE_REQUEST_CODE = 100
        const val CAMERA_REQUEST_CODE = 101
        const val READ_STORAGE_PERMISSION = "android.permission.READ_EXTERNAL_STORAGE"
        const val CAMERA_PERMISSION = "android.permission.CAMERA"
        private const val BACKGROUND_IMAGE_PATH = "BackgroundImages"
        private const val LOGO_IMAGE_PATH = "LogoImages"
        const val BASE_URL = "https://pages.qrmagicapp.com/"
        var generatedImage: Bitmap? = null
        var intentFrom :String = ""
        var finalQrImageUri: Uri? = null
        var isLogin: String = "is_login"
        var user: String = "user"
        var userData: User? = null
        var mService: Drive? = null
        var sheetService: Sheets? = null
        var captureImagePath: String? = null
        val EMAIL_ADDRESS_PATTERN: Pattern = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )

        private fun getBackgroundImageFolderFile(context: Context): File {
            return File(context.externalCacheDir, BACKGROUND_IMAGE_PATH)
        }

        private fun getLogoImageFolderFile(context: Context): File {
            return File(context.externalCacheDir, LOGO_IMAGE_PATH)
        }


        // THIS FUNCTION WILL RETURN THE ALL THE EXTERNAL BACKGROUND IMAGES
        fun getAllBackgroundImages(context: Context): List<String> {
            return ImageManager.getFilesFromBackgroundImagesFolder(
                getBackgroundImageFolderFile(
                    context
                )
            )
        }

        // THIS FUNCTION WILL RETURN THE ALL THE EXTERNAL LOGO IMAGES
        fun getAllLogoImages(context: Context): List<String> {
            return ImageManager.getFilesFromLogoFolder(getLogoImageFolderFile(context))
        }

        // THIS FUNCTION WILL RETURN THE TYPES LIST
        fun getQRTypes(context: Context): List<QRTypes> {
            val list = mutableListOf<QRTypes>()
            list.add(QRTypes(R.drawable.ic_text, context.getString(R.string.text_text), 0))
            list.add(QRTypes(R.drawable.ic_link, context.getString(R.string.static_link_text), 1))
            list.add(QRTypes(R.drawable.ic_link, context.getString(R.string.dynamic_link_text), 2))
            list.add(QRTypes(R.drawable.ic_person, context.getString(R.string.contact_text), 3))
            list.add(QRTypes(R.drawable.ic_wifi, context.getString(R.string.wifi_text), 4))
            list.add(QRTypes(R.drawable.ic_phone, context.getString(R.string.phone_text), 5))
            list.add(QRTypes(R.drawable.ic_sms, context.getString(R.string.sms_text), 6))
            list.add(QRTypes(R.drawable.instagram, context.getString(R.string.instagram_text), 7))
            list.add(QRTypes(R.drawable.whatsapp, context.getString(R.string.whatsapp_text), 8))
            list.add(QRTypes(R.drawable.ic_coupon, context.getString(R.string.coupon_text), 9))
            list.add(QRTypes(R.drawable.ic_feedback, context.getString(R.string.feedback_text), 10))
            list.add(
                QRTypes(
                    R.drawable.ic_social_networks,
                    context.getString(R.string.social_networks_text),
                    11
                )
            )
            return list
        }

        // THIS FUNCTION WILL SHOW THE DIALOG LAYOUT
        private var dialogAlert: AlertDialog? = null
        private var encodedData: String = ""
        private var completeListener: OnCompleteAction? = null
        private var wifiSecurity = "WPA"
        fun getLayout(
            context: Context,
            position: Int,
            layoutContainer: FrameLayout
        ) {
            completeListener = context as OnCompleteAction
            val builder = MaterialAlertDialogBuilder(context)
            builder.setCancelable(false)
            when (position) {
                0 -> {
                    val textView =
                        LayoutInflater.from(context).inflate(R.layout.text_dialog_layout, null)
                    val textInputBox =
                        textView!!.findViewById<TextInputEditText>(R.id.text_input_field)
                    val generateBtn = textView.findViewById<MaterialTextView>(R.id.next_step_btn)
                    if (layoutContainer.childCount > 0) {
                        layoutContainer.removeAllViews()
                        layoutContainer.addView(textView)
                    } else {
                        layoutContainer.addView(textView)
                    }

                    generateBtn.setOnClickListener {

                        if (textInputBox.text.toString().isNotEmpty()) {
                            BaseActivity.hideSoftKeyboard(context, textView)
                            encodedData = textInputBox.text.toString()
                            completeListener!!.onTypeSelected(encodedData, 0, "text")
                        } else {
                            BaseActivity.showAlert(
                                context,
                                context.resources.getString(R.string.required_data_input_error)
                            )
                        }
                    }
                    textInputBox.requestFocus()
                    openKeyboar(context)

                }
                1 -> {
                    var selectedProtocol = ""
                    val websiteView = LayoutInflater.from(context).inflate(
                        R.layout.website_dialog_layout,
                        null
                    )
                    val heading = websiteView!!.findViewById<MaterialTextView>(R.id.dialog_heading)
                    val generateBtn = websiteView.findViewById<MaterialTextView>(R.id.next_step_btn)
                    heading.text = context.getString(R.string.static_link_text)
                    val websiteInputBox =
                        websiteView.findViewById<TextInputEditText>(R.id.website_input_field)
                    val protocolGroup =
                        websiteView.findViewById<RadioGroup>(R.id.http_protocol_group)
                    protocolGroup.setOnCheckedChangeListener { group, checkedId ->
                        when (checkedId) {
                            R.id.http_protocol_rb -> {
                                selectedProtocol = "http://"
                            }
                            R.id.https_protocol_rb -> {
                                selectedProtocol = "https://"
                            }
                            else -> {

                            }
                        }
                    }

                    if (layoutContainer.childCount > 0) {
                        layoutContainer.removeAllViews()
                        layoutContainer.addView(websiteView)
                    } else {
                        layoutContainer.addView(websiteView)
                    }

                    generateBtn.setOnClickListener {

                        val value = websiteInputBox.text.toString().trim()
                        if (selectedProtocol.isEmpty()) {
                            BaseActivity.hideSoftKeyboard(context, websiteView)
                            BaseActivity.showAlert(
                                context,
                                context.resources.getString(R.string.protocol_error)
                            )
                        } else if (value.isEmpty()) {

                            BaseActivity.showAlert(
                                context,
                                context.resources.getString(R.string.required_data_input_error)
                            )

                        } else if (value.contains("http://") || value.contains("https://")
                        ) {
                            BaseActivity.showAlert(
                                context,
                                context.resources.getString(R.string.without_protocol_error)
                            )
                        }
                        else if (!Pattern.compile("^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?\$").matcher(value).find()) {
                            BaseActivity.showAlert(
                                context,
                                context.resources.getString(R.string.valid_website_error)
                            )
                        }
                        else {
                            encodedData = "$selectedProtocol$value"
                            completeListener!!.onTypeSelected(encodedData, 1, "link")

                        }
                    }
                    websiteInputBox.requestFocus()
                    openKeyboar(context)
                }
                2 -> {
                    var selectedProtocol = ""
                    val websiteView =
                        LayoutInflater.from(context).inflate(R.layout.website_dialog_layout, null)
                    val heading = websiteView!!.findViewById<MaterialTextView>(R.id.dialog_heading)
                    heading.text = context.getString(R.string.dynamic_link_text)
                    val websiteInputBox =
                        websiteView.findViewById<TextInputEditText>(R.id.website_input_field)
                    val generateBtn = websiteView.findViewById<MaterialTextView>(R.id.next_step_btn)
                    val protocolGroup =
                        websiteView.findViewById<RadioGroup>(R.id.http_protocol_group)
                    protocolGroup.setOnCheckedChangeListener { group, checkedId ->
                        when (checkedId) {
                            R.id.http_protocol_rb -> {
                                selectedProtocol = "http://"
                            }
                            R.id.https_protocol_rb -> {
                                selectedProtocol = "https://"
                            }
                            else -> {

                            }
                        }
                    }

                    if (layoutContainer.childCount > 0) {
                        layoutContainer.removeAllViews()
                        layoutContainer.addView(websiteView)
                    } else {
                        layoutContainer.addView(websiteView)
                    }
                    generateBtn.setOnClickListener {

                        val value = websiteInputBox.text.toString().trim()
                        if (selectedProtocol.isEmpty()) {
                            BaseActivity.showAlert(
                                context,
                                context.resources.getString(R.string.protocol_error)
                            )
                        } else if (value.isEmpty()) {

                            BaseActivity.showAlert(
                                context,
                                context.resources.getString(R.string.required_data_input_error)
                            )

                        } else if (value.contains("http://") || value.contains("https://")
                        ) {
                            BaseActivity.showAlert(
                                context,
                                context.resources.getString(R.string.without_protocol_error)
                            )
                        }
                        else if (!Pattern.compile("^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?\$").matcher(value).find()) {
                            BaseActivity.showAlert(
                                context,
                                context.resources.getString(R.string.valid_website_error)
                            )
                        }
                        else {
                            BaseActivity.hideSoftKeyboard(context, websiteView)
                            encodedData = "$selectedProtocol$value"
                            completeListener!!.onTypeSelected(encodedData, 2, "link")

                        }
                    }
                    websiteInputBox.requestFocus()
                    openKeyboar(context)
                }
                3 -> {
                    val contactView =
                        LayoutInflater.from(context).inflate(R.layout.contact_dialog_layout, null)
                    val contactNameInputBox =
                        contactView!!.findViewById<TextInputEditText>(R.id.contact_name_input_field)
                    val contactPhoneCcInputBox =
                        contactView.findViewById<TextInputEditText>(R.id.contact_phone_cc_input_field)
                    val contactPhoneStartNumberInputBox =
                        contactView.findViewById<TextInputEditText>(R.id.contact_phone_start_number_input_field)
                    val contactPhoneNumberInputBox =
                        contactView.findViewById<TextInputEditText>(R.id.contact_phone_number_input_field)
                    val contactEmailInputBox =
                        contactView.findViewById<TextInputEditText>(R.id.contact_email_input_field)
                    val contactCompanyInputBox =
                        contactView.findViewById<TextInputEditText>(R.id.contact_company_input_field)
                    val contactJobTitleInputBox =
                        contactView.findViewById<TextInputEditText>(R.id.contact_job_input_field)
                    val contactAddressInputBox =
                        contactView.findViewById<TextInputEditText>(R.id.contact_address_input_field)
                    val contactDetailInputBox =
                        contactView.findViewById<TextInputEditText>(R.id.contact_detail_input_field)
                    val generateBtn = contactView.findViewById<MaterialTextView>(R.id.next_step_btn)
                    if (layoutContainer.childCount > 0) {
                        layoutContainer.removeAllViews()
                        layoutContainer.addView(contactView)
                    } else {
                        layoutContainer.addView(contactView)
                    }

                    generateBtn.setOnClickListener {

                        if (!TextUtils.isEmpty(contactNameInputBox.text.toString())
                            && !TextUtils.isEmpty(contactPhoneNumberInputBox.text.toString())
                            && !TextUtils.isEmpty(contactPhoneCcInputBox.text.toString())
                            && !TextUtils.isEmpty(contactPhoneStartNumberInputBox.text.toString())
                        ) {
                            BaseActivity.hideSoftKeyboard(context, contactView)
                            val phoneNumber =
                                "+${contactPhoneCcInputBox.text.toString()}${contactPhoneStartNumberInputBox.text.toString()}${contactPhoneNumberInputBox.text.toString()}"
                            encodedData =
                                "BEGIN:VCARD\nVERSION:4.0\nN:${
                                    contactNameInputBox.text.toString().trim()
                                }\nTEL:${
                                    phoneNumber
                                }\nTITLE:${
                                    contactJobTitleInputBox.text.toString().trim()
                                }\nEMAIL:${
                                    contactEmailInputBox.text.toString().trim()
                                }\nORG:${
                                    contactCompanyInputBox.text.toString().trim()
                                }\nADR;TYPE=HOME;PREF=1;LABEL:;;${
                                    contactAddressInputBox.text.toString().trim()
                                };;;;\nNOTE:${
                                    contactDetailInputBox.text.toString().trim()
                                }\nEND:VCARD"
                            completeListener!!.onTypeSelected(encodedData, 3, "contact")
                        } else {
                            BaseActivity.showAlert(
                                context,
                                context.resources.getString(R.string.required_data_input_error)
                            )
                        }
                    }
                    contactNameInputBox.requestFocus()
                    openKeyboar(context)
                }
                4 -> {
                    val wifiView =
                        LayoutInflater.from(context).inflate(R.layout.wifi_dialog_layout, null)
                    val wifiNetWorkName =
                        wifiView!!.findViewById<TextInputEditText>(R.id.wifi_name_input_field)
                    val wifiPassword =
                        wifiView.findViewById<TextInputEditText>(R.id.wifi_password_input_field)
                    val wifiSecurityGroup =
                        wifiView.findViewById<RadioGroup>(R.id.securityGroup)
                    val generateBtn = wifiView.findViewById<MaterialTextView>(R.id.next_step_btn)
                    wifiSecurityGroup.setOnCheckedChangeListener { group, checkedId ->
                        when (checkedId) {
                            R.id.wpa -> {
                                wifiSecurity = "WPA"
                            }
                            R.id.wep -> {
                                wifiSecurity = "WEP"
                            }
                            R.id.none -> {
                                wifiSecurity = "nopass"
                            }
                            else -> {

                            }
                        }
                    }
                    if (layoutContainer.childCount > 0) {
                        layoutContainer.removeAllViews()
                        layoutContainer.addView(wifiView)
                    } else {
                        layoutContainer.addView(wifiView)
                    }

                    generateBtn.setOnClickListener {

                        if (!TextUtils.isEmpty(wifiNetWorkName.text.toString()) && !TextUtils.isEmpty(
                                wifiPassword.text.toString()
                            )
                        ) {
                            BaseActivity.hideSoftKeyboard(context, wifiView)
                            encodedData =
                                "WIFI:T:$wifiSecurity;S:${
                                    wifiNetWorkName.text.toString().trim()
                                };P:${wifiPassword.text.toString().trim()};;"
                            completeListener!!.onTypeSelected(encodedData, 4, "wifi")
                        } else {
                            BaseActivity.showAlert(
                                context,
                                context.resources.getString(R.string.required_data_input_error)
                            )
                        }
                    }
                    wifiNetWorkName.requestFocus()
                    openKeyboar(context)
                }
                5 -> {
                    val phoneView =
                        LayoutInflater.from(context).inflate(R.layout.phone_dialog_layout, null)
                    val phoneCcInputBox =
                        phoneView.findViewById<TextInputEditText>(R.id.phone_cc_input_field)
                    val phoneStartNumberInputBox =
                        phoneView.findViewById<TextInputEditText>(R.id.phone_start_number_input_field)
                    val phoneNumberInputBox =
                        phoneView.findViewById<TextInputEditText>(R.id.phone_number_input_field)
                    val generateBtn = phoneView.findViewById<MaterialTextView>(R.id.next_step_btn)
                    if (layoutContainer.childCount > 0) {
                        layoutContainer.removeAllViews()
                        layoutContainer.addView(phoneView)
                    } else {
                        layoutContainer.addView(phoneView)
                    }

                    generateBtn.setOnClickListener {

                        if (!TextUtils.isEmpty(phoneCcInputBox.text.toString())
                            && !TextUtils.isEmpty(phoneStartNumberInputBox.text.toString())
                            && !TextUtils.isEmpty(phoneNumberInputBox.text.toString())
                        ) {
                            BaseActivity.hideSoftKeyboard(context, phoneView)
                            val phoneNumber =
                                "+${phoneCcInputBox.text.toString()}${phoneStartNumberInputBox.text.toString()}${phoneNumberInputBox.text.toString()}"
                            encodedData = "tel:$phoneNumber"
                            completeListener!!.onTypeSelected(encodedData, 5, "phone")
                        } else {
                            BaseActivity.showAlert(
                                context,
                                context.resources.getString(R.string.required_data_input_error)
                            )
                        }
                    }
                    phoneCcInputBox.requestFocus()
                    openKeyboar(context)
                }
                6 -> {
                    val smsView =
                        LayoutInflater.from(context).inflate(R.layout.sms_dialog_layout, null)
                    val smsCcInputBox =
                        smsView.findViewById<TextInputEditText>(R.id.sms_phone_cc_input_field)
                    val smsStartNumberInputBox =
                        smsView.findViewById<TextInputEditText>(R.id.sms_phone_start_number_input_field)
                    val smsNumberInputBox =
                        smsView.findViewById<TextInputEditText>(R.id.sms_phone_number_input_field)
                    val smsMessageInputBox =
                        smsView.findViewById<TextInputEditText>(R.id.sms_message_input_field)
                    val generateBtn = smsView.findViewById<MaterialTextView>(R.id.next_step_btn)
                    if (layoutContainer.childCount > 0) {
                        layoutContainer.removeAllViews()
                        layoutContainer.addView(smsView)
                    } else {
                        layoutContainer.addView(smsView)
                    }

                    generateBtn.setOnClickListener {

                        if (!TextUtils.isEmpty(smsCcInputBox.text.toString())
                            && !TextUtils.isEmpty(smsStartNumberInputBox.text.toString())
                            && !TextUtils.isEmpty(smsNumberInputBox.text.toString()) && !TextUtils.isEmpty(
                                smsMessageInputBox.text.toString()
                            )
                        ) {
                            BaseActivity.hideSoftKeyboard(context, smsView)
                            val phoneNumber =
                                "+${smsCcInputBox.text.toString()}${smsStartNumberInputBox.text.toString()}${smsNumberInputBox.text.toString()}"
                            encodedData =
                                "smsto:$phoneNumber:${smsMessageInputBox.text.toString().trim()}"
                            completeListener!!.onTypeSelected(encodedData, 6, "sms")
                        } else {
                            BaseActivity.showAlert(
                                context,
                                context.resources.getString(R.string.required_data_input_error)
                            )
                        }
                    }
                    smsCcInputBox.requestFocus()
                    openKeyboar(context)
                }
                7 -> {
                    val instagramView =
                        LayoutInflater.from(context).inflate(R.layout.instagram_dialog_layout, null)
                    val instagramInputBox =
                        instagramView!!.findViewById<TextInputEditText>(R.id.instagram_input_field)
                    val generateBtn = instagramView.findViewById<MaterialTextView>(R.id.next_step_btn)
                    if (layoutContainer.childCount > 0) {
                        layoutContainer.removeAllViews()
                        layoutContainer.addView(instagramView)
                    } else {
                        layoutContainer.addView(instagramView)
                    }

                    generateBtn.setOnClickListener {

                        if (!TextUtils.isEmpty(instagramInputBox.text.toString())) {
                            BaseActivity.hideSoftKeyboard(context, instagramView)
                            encodedData =
                                "instagram://user?username=${
                                    instagramInputBox.text.toString().trim()
                                }"
                            completeListener!!.onTypeSelected(encodedData, 7, "instagram")
                        } else {
                            BaseActivity.showAlert(
                                context,
                                context.resources.getString(R.string.required_data_input_error)
                            )
                        }
                    }
                    instagramInputBox.requestFocus()
                    openKeyboar(context)
                }
                8 -> {
                    val whatsappView =
                        LayoutInflater.from(context).inflate(R.layout.whatsapp_dialog_layout, null)
                    val whatsappCcInputBox =
                        whatsappView.findViewById<TextInputEditText>(R.id.whatsapp_phone_cc_input_field)
                    val whatsappStartNumberInputBox =
                        whatsappView.findViewById<TextInputEditText>(R.id.whatsapp_phone_start_number_input_field)
                    val whatsappNumberInputBox =
                        whatsappView.findViewById<TextInputEditText>(R.id.whatsapp_phone_number_input_field)
                    val generateBtn = whatsappView.findViewById<MaterialTextView>(R.id.next_step_btn)
                    if (layoutContainer.childCount > 0) {
                        layoutContainer.removeAllViews()
                        layoutContainer.addView(whatsappView)
                    } else {
                        layoutContainer.addView(whatsappView)
                    }

                    generateBtn.setOnClickListener {

                        if (!TextUtils.isEmpty(whatsappCcInputBox.text.toString())
                            && !TextUtils.isEmpty(whatsappStartNumberInputBox.text.toString())
                            && !TextUtils.isEmpty(whatsappNumberInputBox.text.toString())
                        ) {
                            BaseActivity.hideSoftKeyboard(context, whatsappView)
                            val phone =
                                "+${whatsappCcInputBox.text.toString()}${whatsappStartNumberInputBox.text.toString()}${whatsappNumberInputBox.text.toString()}"
                            if (phone.substring(0, 1) == "+") {
                                encodedData = "whatsapp://send?phone=$phone"
                                completeListener!!.onTypeSelected(encodedData, 8, "whatsapp")
                            } else {
                                BaseActivity.showAlert(
                                    context,
                                    context.resources.getString(R.string.country_code_data_input_error)
                                )
                            }
                        } else {
                            BaseActivity.showAlert(
                                context,
                                context.resources.getString(R.string.required_data_input_error)
                            )
                        }
                    }
                    whatsappCcInputBox.requestFocus()
                    openKeyboar(context)
                }
                else -> {

                }
            }

        }

        fun openKeyboar(context: Context){
            val imm: InputMethodManager? = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
        }

        fun d(TAG: String?, message: String) {
            val maxLogSize = 20000
            for (i in 0..message.length / maxLogSize) {
                val start = i * maxLogSize
                var end = (i + 1) * maxLogSize
                end = if (end > message.length) message.length else end
                Log.d(TAG, message.substring(start, end))
            }
        }
    }
}