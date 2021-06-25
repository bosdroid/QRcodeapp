package com.expert.qrgenerator.view.activities

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.request.DownloadRequest
import com.expert.qrgenerator.R
import com.expert.qrgenerator.utils.Constants.Companion.EMAIL_ADDRESS_PATTERN
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
        fun getDateTimeFromTimeStamp(timeStamp: Long): String {
            val c = Date(timeStamp)
            val df = SimpleDateFormat("yyyy-MM-dd kk:mm a", Locale.getDefault())
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

        fun startLoading(context: Context) {
            val builder = MaterialAlertDialogBuilder(context)
            val layout = LayoutInflater.from(context).inflate(R.layout.custom_loading, null)
            builder.setView(layout)
            builder.setCancelable(false)
            alert = builder.create()
            alert!!.show()
        }

        fun dismiss() {
            if (alert != null) {
                alert!!.dismiss()
            }
        }

        fun getDateFromTimeStamp(timeStamp: Long): String {
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

        fun rateUs(context: AppCompatActivity) {
            val inflater = context.layoutInflater
            val view = inflater.inflate(R.layout.layout_dialog_rate_us, null)
            val builder = AlertDialog.Builder(context)
                .setCancelable(false)
                .setView(view)

            val later = view.findViewById<AppCompatTextView>(R.id.laterTv)
            val ratingBar = view.findViewById<AppCompatRatingBar>(R.id.ratingBar)

            val alertDialog = builder.show()
            ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                if (rating <= 4.0) {
                    contactSupport(context)
                    alertDialog.dismiss()
                } else {
                    rateAppOnPlay(context)
                    alertDialog.dismiss()
                }
            }
            later.setOnClickListener {
                alertDialog.dismiss()
            }
        }

        private fun rateAppOnPlay(context: AppCompatActivity) {
            val rateIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + context.packageName)
                )
            context.startActivity(rateIntent)
        }

        fun contactSupport(context: AppCompatActivity) {
            val intent = Intent(Intent.ACTION_SENDTO)
            // only email apps should handle this
            intent.type = "message/rfc822"
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(context.getString(R.string.support_email)))
            intent.putExtra(Intent.EXTRA_SUBJECT, "")
            try {
                context.startActivity(Intent.createChooser(intent, "Send Mail..."))
            } catch (e: Exception) {
                Toast.makeText(context, "No app found to handle this intent", Toast.LENGTH_LONG)
                    .show()
            }

        }

        fun setUpToolbar(context: AppCompatActivity, toolbar: Toolbar, title: String) {
            context.setSupportActionBar(toolbar)
            context.supportActionBar!!.title = title
            context.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
        }

        fun checkEmail(email: String): Boolean {
            return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
        }

        fun showSoftKeyboard(context: Context,view: View) {
            if (view.requestFocus()) {
                val imm: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        fun hideSoftKeyboard(context: Context,view: View) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

    }

}