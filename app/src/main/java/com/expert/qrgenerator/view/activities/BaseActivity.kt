package com.expert.qrgenerator.view.activities

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.telephony.mbms.DownloadRequest
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.request.DownloadRequest
import com.expert.qrgenerator.utils.ImageManager
import com.github.sumimakito.awesomeqr.AwesomeQrRenderer
import com.github.sumimakito.awesomeqr.option.RenderOption
import com.github.sumimakito.awesomeqr.option.background.StillBackground
import com.github.sumimakito.awesomeqr.option.logo.Logo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


open class BaseActivity : AppCompatActivity() {
    //TODO Remove all unsused code and imports

    companion object {
        var previoudBackgroundImage: Bitmap? = null
        var previousColor: Int? = null
        var previousLogo: Bitmap? = null
        private var prDownloader: DownloadRequest? = null

        //THIS FUNCTION WILL GENERATE THE FINAL QR IMAGE
        fun generateQRWithBackgroundImage(
            context: Context,
            text: String,
            col: String,
            bgImage: String,
            logoUrl: String
        ): Bitmap? {
            val renderOption = RenderOption()

            renderOption.content = text // content to encode
            renderOption.size = 800 // size of the final QR code image
            renderOption.borderWidth = 20 // width of the empty space around the QR code
//            renderOption.ecl = ErrorCorrectionLevel.M
            renderOption.patternScale = 0.35f // (optional) specify a scale for patterns
            renderOption.roundedPatterns = true // (optional) if true, blocks will be drawn as dots instead
            renderOption.clearBorder =
                true // if set to true, the background will NOT be drawn on the border area
            val color = com.github.sumimakito.awesomeqr.option.color.Color()
            if (col.isNotEmpty()) {
                previousColor = Color.parseColor("#$col")
                color.dark = previousColor!!
                renderOption.color = color // set a color palette for the QR code
            } else {
                if (previousColor != null) {
                    color.dark = previousColor!!
                    renderOption.color = color
                }
            }
            val background = StillBackground()
            if (bgImage.isNotEmpty()) {
                previoudBackgroundImage = getBitmapFromURL(context, bgImage)
                background.bitmap = previoudBackgroundImage
                renderOption.background = background // set a background
            } else {
                if (previoudBackgroundImage != null) {
                    background.bitmap = previoudBackgroundImage
                    renderOption.background = background // set a background
                }
            }

            val logo = Logo()
            if (logoUrl.isNotEmpty()) {
                previousLogo = getBitmapFromURL(context, logoUrl)
                logo.bitmap = previousLogo
                renderOption.logo = logo // set a logo
            } else {
                if (previousLogo != null) {
                    logo.bitmap = previousLogo
                    renderOption.logo = logo // set a logo
                }
            }

            try {
                val result = AwesomeQrRenderer.render(renderOption)
                return if (result.bitmap != null) {
                    // play with the bitmap
                    result.bitmap!!
                } else {
                    // Oops, something gone wrong.
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Oops, something gone wrong.
                return null
            }
        }


        // THIS FUNCTION WILL CONVERT THE DRAWABLE IMAGE TO BITMAP
        private fun getDrawableToBitmap(context: Context, image: Int): Bitmap {

            return BitmapFactory.decodeResource(
                context.resources,
                image
            )
        }

        // THIS FUNCTION WILL RETURN THE DATE TIME STRING FROM TIMESTAMP
        fun getDateTimeFromTimeStamp(timeStamp: Long):String
        {
            val c: Date = Date(timeStamp)
            val df = SimpleDateFormat("yyyy-MM-dd-k:mm", Locale.getDefault())
            return df.format(c).toUpperCase(Locale.ENGLISH)
        }

        //TODO Does it have to be in base activity?

        // THIS FUNCTION WILL DOWNLOAD IMAGE FROM URL AND CONVERT INTO BITMAP FOR BACKGROUND
        fun getBitmapFromURL(context: Context, src: String?): Bitmap? {
            if (src!!.contains("http") || src.contains("https")) {
                return try {
                    val url = URL(src)
                    val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()
                    val input: InputStream = connection.inputStream
                    BitmapFactory.decodeStream(input)
                } catch (e: IOException) {
                    // Log exception
                    null
                }
            } else {

                return try {
                    val input: InputStream =
                        context.contentResolver.openInputStream(Uri.fromFile(File(src)))!!
                    val bitmap = BitmapFactory.decodeStream(input)
                    input.close()
                    val degree: Int =
                        ImageManager.getBitmapDegree(Uri.fromFile(File(src)).toString())
                    return ImageManager.rotateBitmapByDegree(bitmap, degree)
                } catch (e: IOException) {
                    e.printStackTrace()
                    null
                }
            }
        }

        //TODO Does it have to be in base activity?
        // THIS FUNCTION WILL SET THE FONT FAMILY
        fun setFontFamily(context: Context, view: MaterialTextView, path: String) {
            if (path.contains("http") || path.contains("https")) {
                val extension = path.substring(path.lastIndexOf("."), path.indexOf("?"))
                val fileName = "tempFont$extension"
                val filePath = context.externalCacheDir.toString() + "/fonts"
                val downloadFile = File(filePath, fileName)
                if (downloadFile.exists()) {
                    downloadFile.delete()
                }

                prDownloader = PRDownloader.download(path, filePath, fileName)
                    .build()
                    .setOnStartOrResumeListener {

                    }
                prDownloader!!.start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        val face = Typeface.createFromFile(downloadFile)
                        view.typeface = face
                    }

                    override fun onError(error: Error?) {
                        Log.d("TEST199", error.toString())
                    }
                })
            } else {
                MaterialAlertDialogBuilder(context)
                    .setMessage("Something went wrong, please check the font file exists and the URL is correct!")
                    .setCancelable(false)
                    .setPositiveButton("Ok") { dialog, which ->
                        dialog.dismiss()
                    }
                    .create().show()
            }

        }

        // THIS FUNCTION WILL ALERT THE DIFFERENT MESSAGES
        fun showAlert(context: Context, message: String) {
            MaterialAlertDialogBuilder(context)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok") { dialog, which ->
                    dialog.dismiss()
                }
                .create().show()
        }

    }

}