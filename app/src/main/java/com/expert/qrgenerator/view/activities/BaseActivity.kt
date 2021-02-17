package com.expert.qrgenerator.view.activities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.request.DownloadRequest
import com.github.sumimakito.awesomeqr.AwesomeQrRenderer
import com.github.sumimakito.awesomeqr.option.RenderOption
import com.github.sumimakito.awesomeqr.option.background.StillBackground
import com.github.sumimakito.awesomeqr.option.logo.Logo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


open class BaseActivity : AppCompatActivity() {

    companion object {
        private var prDownloader: DownloadRequest? = null

        // THIS FUNCTION WILL RETURN THE DATE TIME STRING FROM TIMESTAMP
        fun getDateTimeFromTimeStamp(timeStamp: Long):String
        {
            val c = Date(timeStamp)
            val df = SimpleDateFormat("yyyy-MM-dd-k:mm", Locale.getDefault())
            return df.format(c).toUpperCase(Locale.ENGLISH)
        }

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