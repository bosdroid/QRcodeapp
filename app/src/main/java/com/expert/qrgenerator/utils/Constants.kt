package com.expert.qrgenerator.utils

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import com.expert.qrgenerator.R
import com.expert.qrgenerator.interfaces.OnCompleteAction
import com.expert.qrgenerator.model.QRTypes
import com.expert.qrgenerator.view.activities.BaseActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import java.io.File

class Constants {

    // HERE WE WILL CREATE ALL THE CONSTANT DATA
    companion object {
        const val firebaseBackgroundImages = "backgroundImages"
        const val firebaseLogoImages = "logoImages"
        const val firebaseFonts = "fonts"
        const val READ_STORAGE_REQUEST_CODE = 100
        const val READ_STORAGE_PERMISSION = "android.permission.READ_EXTERNAL_STORAGE"
        private const val BACKGROUND_IMAGE_PATH = "BackgroundImages"
        private const val LOGO_IMAGE_PATH = "LogoImages"
        const val BASE_URL = "http://pages.qrmagicapp.com/"

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
        fun getQRTypes(): List<QRTypes> {
            val list = mutableListOf<QRTypes>()
            list.add(QRTypes(R.drawable.ic_text, "Text", 0))
            list.add(QRTypes(R.drawable.ic_link, "Website", 1))
            list.add(QRTypes(R.drawable.ic_person, "Contact", 2))
            list.add(QRTypes(R.drawable.ic_wifi, "WIFI", 3))
            list.add(QRTypes(R.drawable.ic_phone, "Phone", 4))
            list.add(QRTypes(R.drawable.ic_sms, "SMS", 5))
            list.add(QRTypes(R.drawable.instagram, "Instagram", 6))
            list.add(QRTypes(R.drawable.whatsapp, "Whatsapp", 7))
            list.add(QRTypes(R.drawable.ic_coupon, "Coupon", 8))
            return list
        }

        // THIS FUNCTION WILL SHOW THE DIALOG LAYOUT
        private var dialogAlert: AlertDialog? = null
        private var encodedData: String = ""
        private var completeListener: OnCompleteAction? = null
        private var wifiSecurity = "WPA"
        fun getLayout(context: Context, position: Int) {
            completeListener = context as OnCompleteAction
            val builder = MaterialAlertDialogBuilder(context)
            builder.setCancelable(false)
            when (position) {
                0 -> {
                    val textView =
                        LayoutInflater.from(context).inflate(R.layout.text_dialog_layout, null)
                    val textInputBox =
                        textView!!.findViewById<TextInputEditText>(R.id.text_input_field)

                    builder.setView(textView)
                    dialogAlert = builder.create()
                    dialogAlert!!.show()
                    textView.findViewById<MaterialButton>(R.id.dialog_cancel_btn)
                        .setOnClickListener { dialogAlert!!.dismiss() }
                    textView.findViewById<MaterialButton>(R.id.dialog_add_btn)
                        .setOnClickListener {
                            encodedData = textInputBox.text.toString()
                            completeListener!!.onTypeSelected(encodedData, 0)
                            dialogAlert!!.dismiss()
                        }
                }
                1 -> {
                    val websiteView =
                        LayoutInflater.from(context).inflate(R.layout.website_dialog_layout, null)
                    val websiteInputBox =
                        websiteView!!.findViewById<TextInputEditText>(R.id.website_input_field)

                    builder.setView(websiteView)
                    dialogAlert = builder.create()
                    dialogAlert!!.show()
                    websiteView.findViewById<MaterialButton>(R.id.dialog_cancel_btn)
                        .setOnClickListener { dialogAlert!!.dismiss() }
                    websiteView.findViewById<MaterialButton>(R.id.dialog_add_btn)
                        .setOnClickListener {

                            if (websiteInputBox.text.toString()
                                    .contains("http") || websiteInputBox.text.toString().contains(
                                    "https"
                                )
                                || websiteInputBox.text.toString().contains("www")
                            ) {

                                encodedData = websiteInputBox.text.toString()
                                completeListener!!.onTypeSelected(encodedData, 1)
                                dialogAlert!!.dismiss()

                            } else {
                                BaseActivity.showAlert(
                                    context,
                                    "Please enter the correct format of url!"
                                )
                            }
                        }
                }
                2 -> {
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
                    builder.setView(contactView)
                    dialogAlert = builder.create()
                    dialogAlert!!.show()
                    contactView.findViewById<MaterialButton>(R.id.dialog_cancel_btn)
                        .setOnClickListener { dialogAlert!!.dismiss() }
                    contactView.findViewById<MaterialButton>(R.id.dialog_add_btn)
                        .setOnClickListener {
                            encodedData =
                                "BEGIN:VCARD\nVERSION:4.0\nN:${contactNameInputBox.text.toString()}\nTEL:${contactPhoneNumberInputBox.text.toString()}\nTITLE:${contactJobTitleInputBox.text.toString()}\nEMAIL:${contactEmailInputBox.text.toString()}\nORG:${contactCompanyInputBox.text.toString()}\nADR;TYPE=HOME;PREF=1;LABEL:;;${contactAddressInputBox.text.toString()};;;;\nNOTE:${contactDetailInputBox.text.toString()}\nEND:VCARD"
                            completeListener!!.onTypeSelected(encodedData, 2)
                            dialogAlert!!.dismiss()
                        }
                }
                3 -> {
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
                    builder.setView(wifiView)
                    dialogAlert = builder.create()
                    dialogAlert!!.show()
                    wifiView.findViewById<MaterialButton>(R.id.dialog_cancel_btn)
                        .setOnClickListener { dialogAlert!!.dismiss() }
                    wifiView.findViewById<MaterialButton>(R.id.dialog_add_btn)
                        .setOnClickListener {
                            encodedData =
                                "WIFI:T:$wifiSecurity;S:${wifiNetWorkName.text.toString()};P:${wifiPassword.text.toString()};;"
                            completeListener!!.onTypeSelected(encodedData, 3)
                            dialogAlert!!.dismiss()
                        }
                }
                4 -> {
                    val phoneView =
                        LayoutInflater.from(context).inflate(R.layout.phone_dialog_layout, null)
                    val phoneInputBox =
                        phoneView!!.findViewById<TextInputEditText>(R.id.phone_input_field)

                    builder.setView(phoneView)
                    dialogAlert = builder.create()
                    dialogAlert!!.show()
                    phoneView.findViewById<MaterialButton>(R.id.dialog_cancel_btn)
                        .setOnClickListener { dialogAlert!!.dismiss() }
                    phoneView.findViewById<MaterialButton>(R.id.dialog_add_btn)
                        .setOnClickListener {
                            encodedData = "tel:${phoneInputBox.text.toString()}"
                            completeListener!!.onTypeSelected(encodedData, 4)
                            dialogAlert!!.dismiss()
                        }
                }
                5 -> {
                    val smsView =
                        LayoutInflater.from(context).inflate(R.layout.sms_dialog_layout, null)
                    val smsRecipientInputBox =
                        smsView!!.findViewById<TextInputEditText>(R.id.sms_recipient_input_field)
                    val smsMessageInputBox =
                        smsView.findViewById<TextInputEditText>(R.id.sms_message_input_field)
                    builder.setView(smsView)
                    dialogAlert = builder.create()
                    dialogAlert!!.show()
                    smsView.findViewById<MaterialButton>(R.id.dialog_cancel_btn)
                        .setOnClickListener { dialogAlert!!.dismiss() }
                    smsView.findViewById<MaterialButton>(R.id.dialog_add_btn)
                        .setOnClickListener {
                            encodedData =
                                "smsto:${smsRecipientInputBox.text.toString()}:${smsMessageInputBox.text.toString()}"
                            completeListener!!.onTypeSelected(encodedData, 5)
                            dialogAlert!!.dismiss()
                        }
                }
                6 -> {
                    val instagramView =
                        LayoutInflater.from(context).inflate(R.layout.instagram_dialog_layout, null)
                    val instagramInputBox =
                        instagramView!!.findViewById<TextInputEditText>(R.id.instagram_input_field)

                    builder.setView(instagramView)
                    dialogAlert = builder.create()
                    dialogAlert!!.show()
                    instagramView.findViewById<MaterialButton>(R.id.dialog_cancel_btn)
                        .setOnClickListener { dialogAlert!!.dismiss() }
                    instagramView.findViewById<MaterialButton>(R.id.dialog_add_btn)
                        .setOnClickListener {
                            encodedData =
                                "instagram://user?username=${instagramInputBox.text.toString()}"
                            completeListener!!.onTypeSelected(encodedData, 6)
                            dialogAlert!!.dismiss()
                        }
                }
                7 -> {
                    val whatsappView =
                        LayoutInflater.from(context).inflate(R.layout.whatsapp_dialog_layout, null)
                    val whatsappInputBox =
                        whatsappView!!.findViewById<TextInputEditText>(R.id.whatsapp_input_field)

                    builder.setView(whatsappView)
                    dialogAlert = builder.create()
                    dialogAlert!!.show()
                    whatsappView.findViewById<MaterialButton>(R.id.dialog_cancel_btn)
                        .setOnClickListener { dialogAlert!!.dismiss() }
                    whatsappView.findViewById<MaterialButton>(R.id.dialog_add_btn)
                        .setOnClickListener {
                            if (!TextUtils.isEmpty(whatsappInputBox.text.toString())) {
                                val phone = whatsappInputBox.text.toString()
                                if (phone.substring(0, 1) == "+") {
                                    encodedData =
                                        "whatsapp://send?phone=${whatsappInputBox.text.toString()}"
                                    completeListener!!.onTypeSelected(encodedData, 7)
                                    dialogAlert!!.dismiss()
                                } else {
                                    BaseActivity.showAlert(
                                        context,
                                        "Please enter the correct phone number with country code!"
                                    )
                                }
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