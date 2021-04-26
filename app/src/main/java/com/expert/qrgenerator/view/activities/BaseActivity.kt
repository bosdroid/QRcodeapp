package com.expert.qrgenerator.view.activities

import android.content.Context
import android.graphics.Typeface
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.request.DownloadRequest
import com.expert.qrgenerator.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


open class BaseActivity : AppCompatActivity() {

    companion object {
        private var prDownloader: DownloadRequest? = null
        var alert: AlertDialog? = null

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

        fun startLoading(context: Context)
        {
            val builder = MaterialAlertDialogBuilder(context)
            val layout = LayoutInflater.from(context).inflate(R.layout.custom_loading, null)
            builder.setView(layout)
            builder.setCancelable(false)
            alert = builder.create()
            alert!!.show()
        }

        fun dismiss()
        {
            if (alert != null)
            {
                alert!!.dismiss()
            }
        }

        fun getDateFromTimeStamp(timeStamp: Long):String
        {
            val c: Date = Date(timeStamp)
            val df = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return df.format(c).toUpperCase(Locale.ENGLISH)
        }

        fun getFormattedDate(context: Context?, smsTimeInMilis: Long): String {
            val smsTime = Calendar.getInstance()
            smsTime.timeInMillis = smsTimeInMilis
            val now = Calendar.getInstance()
            val timeFormatString = "h:mm:ss"
            val dateTimeFormatString = "EEEE, MMMM d, h:mm:ss"
            val HOURS = (60 * 60 * 60).toLong()
            return if (now[Calendar.DATE] == smsTime[Calendar.DATE]) {
                "Today " + DateFormat.format(timeFormatString, smsTime)
            } else if (now[Calendar.DATE] - smsTime[Calendar.DATE] == 1) {
                "Yesterday " + DateFormat.format(timeFormatString, smsTime)
            } else if (now[Calendar.YEAR] == smsTime[Calendar.YEAR]) {
                DateFormat.format(dateTimeFormatString, smsTime).toString()
            } else {
                DateFormat.format("MMMM dd yyyy, h:mm:ss", smsTime).toString()
            }
        }

    }

}