package com.expert.qrgenerator.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.expert.qrgenerator.R
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
        const val READ_STORAGE_REQUEST_CODE = 100
        const val CAMERA_REQUEST_CODE = 101
        const val READ_STORAGE_PERMISSION = "android.permission.READ_EXTERNAL_STORAGE"
        const val CAMERA_PERMISSION = "android.permission.CAMERA"
        private const val BACKGROUND_IMAGE_PATH = "BackgroundImages"
        private const val LOGO_IMAGE_PATH = "LogoImages"
        const val BASE_URL = "https://pages.qrmagicapp.com/"
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
            list.add(QRTypes(R.drawable.ic_person, context.getString(R.string.contact_text), 2))
            list.add(QRTypes(R.drawable.ic_wifi, context.getString(R.string.wifi_text), 3))
            list.add(QRTypes(R.drawable.ic_phone, context.getString(R.string.phone_text), 4))
            list.add(QRTypes(R.drawable.ic_sms, context.getString(R.string.sms_text), 5))
            list.add(QRTypes(R.drawable.instagram, context.getString(R.string.instagram_text), 6))
            list.add(QRTypes(R.drawable.whatsapp, context.getString(R.string.whatsapp_text), 7))
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