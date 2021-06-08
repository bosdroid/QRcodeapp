package com.expert.qrgenerator.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
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
        const val BASE_URL = "http://pages.qrmagicapp.com/"
        var generatedImage:Bitmap?=null
        var finalQrImageUri:Uri?=null
        var isLogin:String = "is_login"
        var user:String = "user"
        var userData:User?=null
        var mService:Drive?=null
        var sheetService: Sheets?=null

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
            return list
        }

        // THIS FUNCTION WILL SHOW THE DIALOG LAYOUT
        private var dialogAlert: AlertDialog? = null
        private var encodedData: String = ""
        private var completeListener: OnCompleteAction? = null
        private var wifiSecurity = "WPA"
        fun getLayout(context: Context, position: Int,layoutContainer:FrameLayout,nextBtn:MaterialTextView) {
            completeListener = context as OnCompleteAction
            val builder = MaterialAlertDialogBuilder(context)
            builder.setCancelable(false)
            when (position) {
                0 -> {
                    val textView =
                        LayoutInflater.from(context).inflate(R.layout.text_dialog_layout, null)
                    val textInputBox =
                        textView!!.findViewById<TextInputEditText>(R.id.text_input_field)
                    if (layoutContainer.childCount > 0){
                        layoutContainer.removeAllViews()
                        layoutContainer.addView(textView)
                    }
                    else
                    {
                        layoutContainer.addView(textView)
                    }

                    nextBtn.setOnClickListener {
                        if (textInputBox.text.toString().isNotEmpty()){
                            encodedData = textInputBox.text.toString()
                            completeListener!!.onTypeSelected(encodedData, 0,"text")
                        }
                        else
                        {
                            BaseActivity.showAlert(
                                context,
                                "Please enter the required input data!"
                            )
                        }
                    }
                }
                1 -> {
                    val websiteView =
                        LayoutInflater.from(context).inflate(R.layout.website_dialog_layout, null)
                    val heading = websiteView!!.findViewById<MaterialTextView>(R.id.dialog_heading)
                    heading.text = context.getString(R.string.static_link_text)
                    val websiteInputBox =
                        websiteView.findViewById<TextInputEditText>(R.id.website_input_field)
                    if (layoutContainer.childCount > 0){
                        layoutContainer.removeAllViews()
                        layoutContainer.addView(websiteView)
                    }
                    else
                    {
                        layoutContainer.addView(websiteView)
                    }

                    nextBtn.setOnClickListener {
                        if (websiteInputBox.text.toString().isNotEmpty()) {

                            encodedData = websiteInputBox.text.toString()
                            completeListener!!.onTypeSelected(encodedData, 1,"link")

                        } else {
                            BaseActivity.showAlert(
                                context,
                                "Please enter the required input data!"
                            )
                        }
                    }
                }
                2 -> {
                    val websiteView =
                        LayoutInflater.from(context).inflate(R.layout.website_dialog_layout, null)
                    val heading = websiteView!!.findViewById<MaterialTextView>(R.id.dialog_heading)
                    heading.text = context.getString(R.string.dynamic_link_text)
                    val websiteInputBox =
                        websiteView.findViewById<TextInputEditText>(R.id.website_input_field)
                    if (layoutContainer.childCount > 0){
                        layoutContainer.removeAllViews()
                        layoutContainer.addView(websiteView)
                    }
                    else
                    {
                        layoutContainer.addView(websiteView)
                    }
                    nextBtn.setOnClickListener {
                        if (websiteInputBox.text.toString().isNotEmpty()) {

                            encodedData = websiteInputBox.text.toString()
                            completeListener!!.onTypeSelected(encodedData, 2,"link")

                        } else {
                            BaseActivity.showAlert(
                                context,
                                "Please enter the required input data!"
                            )
                        }
                    }
                }
                3 -> {
                    val contactView =
                        LayoutInflater.from(context).inflate(R.layout.contact_dialog_layout, null)
                    val contactNameInputBox =
                        contactView!!.findViewById<TextInputEditText>(R.id.contact_name_input_field)
                    val contactPhoneNumberInputBox =
                        contactView.findViewById<TextInputEditText>(R.id.contact_phone_input_field)
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
                    if (layoutContainer.childCount > 0){
                        layoutContainer.removeAllViews()
                        layoutContainer.addView(contactView)
                    }
                    else
                    {
                        layoutContainer.addView(contactView)
                    }

                    nextBtn.setOnClickListener {
                        if (!TextUtils.isEmpty(contactNameInputBox.text.toString())&&
                                !TextUtils.isEmpty(contactPhoneNumberInputBox.text.toString())) {
                            encodedData =
                                "BEGIN:VCARD\nVERSION:4.0\nN:${
                                    contactNameInputBox.text.toString().trim()
                                }\nTEL:${
                                    contactPhoneNumberInputBox.text.toString().trim()
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
                        }
                        else
                        {
                            BaseActivity.showAlert(
                                context,
                                "Please enter the required contact name and phone input data!"
                            )
                        }
                    }
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
                    if (layoutContainer.childCount > 0){
                        layoutContainer.removeAllViews()
                        layoutContainer.addView(wifiView)
                    }
                    else
                    {
                        layoutContainer.addView(wifiView)
                    }

                    nextBtn.setOnClickListener {
                        if (!TextUtils.isEmpty(wifiNetWorkName.text.toString()) && !TextUtils.isEmpty(wifiPassword.text.toString())) {
                            encodedData =
                                "WIFI:T:$wifiSecurity;S:${wifiNetWorkName.text.toString().trim()};P:${wifiPassword.text.toString().trim()};;"
                            completeListener!!.onTypeSelected(encodedData, 4, "wifi")
                        }
                        else
                        {
                            BaseActivity.showAlert(
                                context,
                                "Please enter the required input data!"
                            )
                        }
                    }
                }
                5 -> {
                    val phoneView =
                        LayoutInflater.from(context).inflate(R.layout.phone_dialog_layout, null)
                    val phoneInputBox =
                        phoneView!!.findViewById<TextInputEditText>(R.id.phone_input_field)
                    if (layoutContainer.childCount > 0){
                        layoutContainer.removeAllViews()
                        layoutContainer.addView(phoneView)
                    }
                    else
                    {
                        layoutContainer.addView(phoneView)
                    }

                    nextBtn.setOnClickListener {
                        if (!TextUtils.isEmpty(phoneInputBox.text.toString())) {
                            encodedData = "tel:${phoneInputBox.text.toString().trim()}"
                            completeListener!!.onTypeSelected(encodedData, 5, "phone")
                        }
                        else
                        {
                            BaseActivity.showAlert(
                                context,
                                "Please enter the required input data!"
                            )
                        }
                    }
                }
                6 -> {
                    val smsView =
                        LayoutInflater.from(context).inflate(R.layout.sms_dialog_layout, null)
                    val smsRecipientInputBox =
                        smsView!!.findViewById<TextInputEditText>(R.id.sms_recipient_input_field)
                    val smsMessageInputBox =
                        smsView.findViewById<TextInputEditText>(R.id.sms_message_input_field)
                    if (layoutContainer.childCount > 0){
                        layoutContainer.removeAllViews()
                        layoutContainer.addView(smsView)
                    }
                    else
                    {
                        layoutContainer.addView(smsView)
                    }

                    nextBtn.setOnClickListener {
                        if (!TextUtils.isEmpty(smsRecipientInputBox.text.toString()) && !TextUtils.isEmpty(smsMessageInputBox.text.toString())) {

                            encodedData = "smsto:${
                                smsRecipientInputBox.text.toString().trim()
                            }:${smsMessageInputBox.text.toString().trim()}"
                            completeListener!!.onTypeSelected(encodedData, 6, "sms")
                        }
                        else
                        {
                            BaseActivity.showAlert(
                                context,
                                "Please enter the required input data!"
                            )
                        }
                    }
                }
                7 -> {
                    val instagramView =
                        LayoutInflater.from(context).inflate(R.layout.instagram_dialog_layout, null)
                    val instagramInputBox =
                        instagramView!!.findViewById<TextInputEditText>(R.id.instagram_input_field)
                    if (layoutContainer.childCount > 0){
                        layoutContainer.removeAllViews()
                        layoutContainer.addView(instagramView)
                    }
                    else
                    {
                        layoutContainer.addView(instagramView)
                    }

                    nextBtn.setOnClickListener {
                        if (!TextUtils.isEmpty(instagramInputBox.text.toString())) {
                            encodedData =
                                "instagram://user?username=${instagramInputBox.text.toString().trim()}"
                            completeListener!!.onTypeSelected(encodedData, 7, "instagram")
                        }
                        else
                        {
                            BaseActivity.showAlert(
                                context,
                                "Please enter the required input data!"
                            )
                        }
                    }
                }
                8 -> {
                    val whatsappView =
                        LayoutInflater.from(context).inflate(R.layout.whatsapp_dialog_layout, null)
                    val whatsappInputBox =
                        whatsappView!!.findViewById<TextInputEditText>(R.id.whatsapp_input_field)
                    if (layoutContainer.childCount > 0){
                        layoutContainer.removeAllViews()
                        layoutContainer.addView(whatsappView)
                    }
                    else
                    {
                        layoutContainer.addView(whatsappView)
                    }

                    nextBtn.setOnClickListener {
                        if (!TextUtils.isEmpty(whatsappInputBox.text.toString())) {
                            val phone = whatsappInputBox.text.toString()
                            if (phone.substring(0, 1) == "+") {
                                encodedData =
                                    "whatsapp://send?phone=${whatsappInputBox.text.toString().trim()}"
                                completeListener!!.onTypeSelected(encodedData, 8,"whatsapp")
                            } else {
                                BaseActivity.showAlert(
                                    context,
                                    "Please enter the correct phone number with country code!"
                                )
                            }
                        }
                        else
                        {
                            BaseActivity.showAlert(
                                context,
                                "Please enter the required input data!"
                            )
                        }
                    }
                }
                else -> {

                }
            }

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