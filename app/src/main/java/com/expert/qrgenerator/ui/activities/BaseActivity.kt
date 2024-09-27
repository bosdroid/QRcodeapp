package com.expert.qrgenerator.ui.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.text.format.DateFormat
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
import com.expert.qrgenerator.R
import com.expert.qrgenerator.utils.Constants.Companion.EMAIL_ADDRESS_PATTERN
import com.expert.qrgenerator.utils.DialogPrefs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.DownloadTask.Builder
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import com.liulishuo.okdownload.core.listener.DownloadListener1
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


open class BaseActivity : AppCompatActivity() {

    companion object {
        private var downloadTask: DownloadTask? = null
        var alert: AlertDialog? = null

        // Checks if network connection is available
        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                capabilities?.let {
                    it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                } ?: false
            } else {
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                activeNetworkInfo?.isConnected == true
            }
        }

        // Converts timestamp to formatted date-time string
        fun getDateTimeFromTimeStamp(timeStamp: Long): String {
            val date = Date(timeStamp)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd kk:mm a", Locale.getDefault())
            return dateFormat.format(date).toUpperCase(Locale.ENGLISH)
        }

        // Sets custom font from a URL
        fun setFontFamily(context: Context, view: MaterialTextView, path: String) {
            if (path.startsWith("http://") || path.startsWith("https://")) {
                val fileName = "tempFont${path.substringAfterLast('.', "")}"
                val filePath = context.externalCacheDir?.resolve("fonts")?.toString()
                val downloadFile = File(filePath, fileName)

                downloadFile.takeIf { it.exists() }?.delete()

                downloadTask = Builder(path, File(filePath))
                    .setFilename(fileName)
                    .setMinIntervalMillisCallbackProcess(100) // Update every 100ms
                    .setPassIfAlreadyCompleted(false)
                    .build()
                downloadTask?.enqueue(object : DownloadListener1() {
                    override fun taskStart(task: DownloadTask, model: Listener1Assist.Listener1Model) {}

                    override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?, model: Listener1Assist.Listener1Model) {
                        val typeface = Typeface.createFromFile(downloadFile)
                        view.typeface = typeface
                    }

                    override fun retry(task: DownloadTask, cause: ResumeFailedCause) {}

                    override fun connected(task: DownloadTask, blockCount: Int, currentOffset: Long, totalLength: Long) {}

                    override fun progress(task: DownloadTask, currentOffset: Long, totalLength: Long) {}
                })
            } else {
                MaterialAlertDialogBuilder(context)
                    .setMessage(context.getString(R.string.font_file_error_text))
                    .setCancelable(false)
                    .setPositiveButton(context.getString(R.string.ok_text)) { dialog, _ -> dialog.dismiss() }
                    .create().show()
            }
        }

        // Hides the keyboard
        fun hideKeyboard(context: Context, activity: AppCompatActivity) {
            val view = activity.currentFocus
            view?.let {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(it.windowToken, 0)
            }
        }

        // Displays an alert with a message
        fun showAlert(context: Context, message: String) {
            MaterialAlertDialogBuilder(context)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
                .create().show()
        }

        // Shows a loading dialog
        fun startLoading(context: Context) {
            val builder = MaterialAlertDialogBuilder(context)
            val layout = LayoutInflater.from(context).inflate(R.layout.custom_loading, null)
            builder.setView(layout)
            builder.setCancelable(false)
            alert = builder.create()
            alert?.show()
        }

        // Dismisses the loading dialog
        fun dismiss() {
            alert?.dismiss()
        }

        // Converts timestamp to formatted date string
        fun getDateFromTimeStamp(timeStamp: Long): String {
            val date = Date(timeStamp)
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return dateFormat.format(date).toUpperCase(Locale.ENGLISH)
        }

        // Formats date and time with conditions for today, yesterday, or specific date
        fun getFormattedDate(context: Context?, smsTimeInMillis: Long): String {
            val smsTime = Calendar.getInstance().apply { timeInMillis = smsTimeInMillis }
            val now = Calendar.getInstance()
            val timeFormatString = "h:mm:ss"
            val dateTimeFormatString = "EEEE, MMMM d, h:mm:ss"

            return when {
                now[Calendar.DATE] == smsTime[Calendar.DATE] -> "Today " + DateFormat.format(timeFormatString, smsTime)
                now[Calendar.DATE] - smsTime[Calendar.DATE] == 1 -> "Yesterday " + DateFormat.format(timeFormatString, smsTime)
                now[Calendar.YEAR] == smsTime[Calendar.YEAR] -> DateFormat.format(dateTimeFormatString, smsTime).toString()
                else -> DateFormat.format("MMMM dd yyyy, h:mm:ss", smsTime).toString()
            }
        }

        // Prompts user to rate the app
        fun rateUs(context: AppCompatActivity) {
            val view = context.layoutInflater.inflate(R.layout.layout_dialog_rate_us, null)
            val builder = AlertDialog.Builder(context)
                .setCancelable(false)
                .setView(view)

            val later = view.findViewById<AppCompatTextView>(R.id.laterTv)
            val ratingBar = view.findViewById<AppCompatRatingBar>(R.id.ratingBar)

            val alertDialog = builder.show()
            ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
                if (rating <= 4.0) {
                    contactSupport(context)
                } else {
                    rateAppOnPlay(context)
                }
                alertDialog.dismiss()
            }

            later.setOnClickListener {
                DialogPrefs.clearPreferences(context)
                alertDialog.dismiss()
            }
        }

        // Opens Play Store to rate the app
        private fun rateAppOnPlay(context: AppCompatActivity) {
            val rateIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
            context.startActivity(rateIntent)
        }

        // Opens email client to contact support
        fun contactSupport(context: AppCompatActivity) {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                type = "message/rfc822"
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(context.getString(R.string.support_email)))
                putExtra(Intent.EXTRA_SUBJECT, "")
            }
            try {
                context.startActivity(Intent.createChooser(intent, "Send Mail..."))
            } catch (e: Exception) {
                // No email client installed
            }
        }

        // Sets up toolbar with title and back button
        fun setUpToolbar(context: AppCompatActivity, toolbar: Toolbar, title: String) {
            context.setSupportActionBar(toolbar)
            context.supportActionBar?.apply {
                this.title = title
                setDisplayHomeAsUpEnabled(true)
            }
            toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
        }

        // Validates email address
        fun checkEmail(email: String): Boolean {
            return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
        }

        // Shows the soft keyboard
        fun showSoftKeyboard(context: Context, view: View) {
            if (view.requestFocus()) {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        // Hides the soft keyboard
        fun hideSoftKeyboard(context: Context, view: View) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun copyToClipboard(context: Context,text:String){
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("label", text)
            clipboardManager.setPrimaryClip(clipData)

            // Optionally, show a toast message to notify the user
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }
}
