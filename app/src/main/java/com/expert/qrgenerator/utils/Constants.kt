package com.expert.qrgenerator.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.expert.qrgenerator.R
import com.expert.qrgenerator.model.QRItem
import com.expert.qrgenerator.model.QRTypes
import com.expert.qrgenerator.model.Sheet
import com.expert.qrgenerator.model.User
import com.google.api.services.drive.Drive
import com.google.api.services.sheets.v4.Sheets
import java.io.File
import java.util.regex.Pattern


class Constants {

    // HERE WE WILL CREATE ALL THE CONSTANT DATA
    companion object {
        const val PRIVACY_POLICY_URL = "http://qrmagicapp.com/privacy-policy-2/"
        const val FIREBASE_BACKGROUND_IMAGES = "backgroundImages"
        const val FIREBASE_LOGO_IMAGES = "logoImages"
        const val FIREBASE_FONTS = "fonts"
        const val FIREBASE_USER_FEEDBACKS = "USER_FEEDBACKS"
        const val READ_STORAGE_REQUEST_CODE = 100
        const val CAMERA_REQUEST_CODE = 101
        const val READ_STORAGE_PERMISSION = "android.permission.READ_EXTERNAL_STORAGE"
        const val CAMERA_PERMISSION = "android.permission.CAMERA"
        private const val BACKGROUND_IMAGE_PATH = "BackgroundImages"
        private const val LOGO_IMAGE_PATH = "LogoImages"
        const val BASE_URL = "https://qrmagicapp.com/"
        const val GOOGLE_APP_SCRIPT_URL = "https://script.google.com/macros/s/AKfycbw4-8R85cCh9C5JXD6BTl0q89NNOTFkYfZO1Sp2LRrVA-mCv06LRYu2PqCPUaKab26-/exec"
        var generatedImage: Bitmap? = null
        var tipsValue :Boolean = true
        var finalQrImageUri: Uri? = null
        var isLogin: String = "is_login"
        var user: String = "user"
        var email: String = "email"
        var userData: User? = null
        var mService: Drive? = null
        var sheetService: Sheets? = null
        var captureImagePath: String? = null
        var sheetsList = mutableListOf<Sheet>()
        val LAST_SHOWN_DATE_KEY = "lastShownDate"
        val FIREBASE_TRACKABLE_SCANS = "TRACKABLE_SCANS"
        var isOpenVcardScreen = false
        var chatGptApiKey = ""
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
        fun getQRTypes(context: Context): List<QRItem> {
            val list = mutableListOf<QRItem>()
            // Digital business card
            list.add(QRItem.Header("Digital Business Card"))
//            list.add(QRItem.QRType(R.drawable.link, context.getString(R.string.static_link_text), 1))
            list.add(QRItem.QRType(R.drawable.application, context.getString(R.string.v_card_text), 32))
            list.add(QRItem.QRType(R.drawable.wifi, context.getString(R.string.wifi_text), 3))
            list.add(QRItem.QRType(R.drawable.telephone, context.getString(R.string.phone_call_text), 4))
            list.add(QRItem.QRType(R.drawable.sms, context.getString(R.string.sms_text), 5))
            list.add(QRItem.QRType(R.drawable.text_format, context.getString(R.string.text_text), 0))
            list.add(QRItem.QRType(R.drawable.contact_book, context.getString(R.string.contact_text), 2))
            list.add(QRItem.QRType(R.drawable.map, context.getString(R.string.map_text), 8))
            list.add(QRItem.QRType(R.drawable.email, context.getString(R.string.email_text), 12))
            // social networks
            list.add(QRItem.Header("Social Networks"))
            list.add(QRItem.QRType(R.drawable.instagram, context.getString(R.string.instagram_text), 6))
            list.add(QRItem.QRType(R.drawable.whatsapp, context.getString(R.string.whatsapp_text), 7))
            list.add(QRItem.QRType(R.drawable.ic_facebook, context.getString(R.string.facebook_text), 9))
            list.add(QRItem.QRType(R.drawable.ic_youtube, context.getString(R.string.youtube_text), 10))
            list.add(QRItem.QRType(R.drawable.ic_telegram, context.getString(R.string.telegram_text), 11))
            list.add(QRItem.QRType(R.drawable.tiktok, context.getString(R.string.tiktok_text), 13))
            list.add(QRItem.QRType(R.drawable.ic_twitter, context.getString(R.string.twitter_text), 15))
            list.add(QRItem.QRType(R.drawable.snapchat, context.getString(R.string.snapchat_text), 16))
            // others
            list.add(QRItem.Header("Others"))
            list.add(QRItem.QRType(R.drawable.google_forms, context.getString(R.string.google_forms_text), 14))
            list.add(QRItem.QRType(R.drawable.spotify, context.getString(R.string.spotify_text), 17))
            list.add(QRItem.QRType(R.drawable.docs, context.getString(R.string.google_docs_text), 18))
            list.add(QRItem.QRType(R.drawable.google_review, context.getString(R.string.google_review_text), 19))
            list.add(QRItem.QRType(R.drawable.sheets, context.getString(R.string.google_sheets_text), 20))
            list.add(QRItem.QRType(R.drawable.payment, context.getString(R.string.payment_text), 21))
            list.add(QRItem.QRType(R.drawable.office, context.getString(R.string.office_365_text), 22))
            list.add(QRItem.QRType(R.drawable.key, context.getString(R.string.shaped_text), 23))
            list.add(QRItem.QRType(R.drawable.paypal, context.getString(R.string.paypal_text), 24))
            list.add(QRItem.QRType(R.drawable.etsy, context.getString(R.string.etsy_text), 25))
            list.add(QRItem.QRType(R.drawable.linkedin, context.getString(R.string.linkedin_text), 26))
            list.add(QRItem.QRType(R.drawable.bitcoin, context.getString(R.string.crypto_payment_text), 27))
            list.add(QRItem.QRType(R.drawable.calendar, context.getString(R.string.calendar_text), 28))
            list.add(QRItem.QRType(R.drawable.group, context.getString(R.string.social_media_text), 29))
            list.add(QRItem.QRType(R.drawable.reddit, context.getString(R.string.reddit_text), 30))
            list.add(QRItem.QRType(R.drawable.application, context.getString(R.string.play_market_app_store_text), 31))
//            list.add(QRItem.QRType(R.drawable.application, context.getString(R.string.tackable_text), 33))
            list.add(QRItem.QRType(R.drawable.application, context.getString(R.string.utm), 34))
//            list.add(QRTypes(R.drawable.text_format, context.getString(R.string.text_text), 0))
//            list.add(QRTypes(R.drawable.link, context.getString(R.string.static_link_text), 1))
//            list.add(QRTypes(R.drawable.contact_book, context.getString(R.string.contact_text), 2))
//            list.add(QRTypes(R.drawable.wifi, context.getString(R.string.wifi_text), 3))
//            list.add(QRTypes(R.drawable.telephone, context.getString(R.string.phone_call_text), 4))
//            list.add(QRTypes(R.drawable.sms, context.getString(R.string.sms_text), 5))
//            list.add(QRTypes(R.drawable.instagram, context.getString(R.string.instagram_text), 6))
//            list.add(QRTypes(R.drawable.whatsapp, context.getString(R.string.whatsapp_text), 7))
//            // NEW TYPES ADDED
//            list.add(QRTypes(R.drawable.map, context.getString(R.string.map_text), 8))
//            list.add(QRTypes(R.drawable.ic_facebook, context.getString(R.string.facebook_text), 9))
//            list.add(QRTypes(R.drawable.ic_youtube, context.getString(R.string.youtube_text), 10))
//            list.add(QRTypes(R.drawable.ic_telegram, context.getString(R.string.telegram_text), 11))
//            list.add(QRTypes(R.drawable.email, context.getString(R.string.email_text), 12))
//            list.add(QRTypes(R.drawable.tiktok, context.getString(R.string.tiktok_text), 13))
//            list.add(QRTypes(R.drawable.google_forms, context.getString(R.string.google_forms_text), 14))
//            list.add(QRTypes(R.drawable.ic_twitter, context.getString(R.string.twitter_text), 15))
//            list.add(QRTypes(R.drawable.snapchat, context.getString(R.string.snapchat_text), 16))
//            list.add(QRTypes(R.drawable.spotify, context.getString(R.string.spotify_text), 17))
//            list.add(QRTypes(R.drawable.docs, context.getString(R.string.google_docs_text), 18))
//            list.add(QRTypes(R.drawable.google_review, context.getString(R.string.google_review_text), 19))
//            list.add(QRTypes(R.drawable.sheets, context.getString(R.string.google_sheets_text), 20))
//            list.add(QRTypes(R.drawable.payment, context.getString(R.string.payment_text), 21))
//            list.add(QRTypes(R.drawable.office, context.getString(R.string.office_365_text), 22))
//            list.add(QRTypes(R.drawable.key, context.getString(R.string.shaped_text), 23))
//            list.add(QRTypes(R.drawable.paypal, context.getString(R.string.paypal_text), 24))
//            list.add(QRTypes(R.drawable.etsy, context.getString(R.string.etsy_text), 25))
//            list.add(QRTypes(R.drawable.linkedin, context.getString(R.string.linkedin_text), 26))
//            list.add(QRTypes(R.drawable.bitcoin, context.getString(R.string.crypto_payment_text), 27))
//            list.add(QRTypes(R.drawable.calendar, context.getString(R.string.calendar_text), 28))
//            list.add(QRTypes(R.drawable.group, context.getString(R.string.social_media_text), 29))
//            list.add(QRTypes(R.drawable.reddit, context.getString(R.string.reddit_text), 30))
//            list.add(QRTypes(R.drawable.application, context.getString(R.string.play_market_app_store_text), 31))
//            list.add(QRTypes(R.drawable.application, context.getString(R.string.v_card_text), 32))
            return list
        }

        fun openKeyboard(context: Context) {
            // Obtain the InputMethodManager system service
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

            // Check if the InputMethodManager instance is not null
            imm?.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,       // Show the keyboard
                InputMethodManager.HIDE_IMPLICIT_ONLY // Hide it only if it's implicitly visible
            ) ?: run {
                // Optionally, handle the case where InputMethodManager is null
                Log.e("KeyboardUtil", "Failed to get InputMethodManager")
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