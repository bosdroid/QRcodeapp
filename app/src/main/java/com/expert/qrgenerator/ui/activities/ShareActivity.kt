package com.expert.qrgenerator.ui.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.ActivityShareBinding
import com.expert.qrgenerator.repository.DataRepository
import com.expert.qrgenerator.utils.AppSettings
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.DialogPrefs
import com.expert.qrgenerator.utils.GeneratorManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class ShareActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityShareBinding
    // Context of the activity, initialized using lazy delegation
    private val context: Context by lazy { this }
    private var imageShareUri: Uri? = null
    private var type:String = ""
    private var data:String = ""
    private lateinit var appSettings: AppSettings
    private lateinit var feedbackHandler: Handler
    private lateinit var feedbackRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShareBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        logCustomEvent(eventName = "screen_share_opened")

        // Initialize views and set up toolbar
        initViews()
        setUpToolbar()
    }

    override fun onResume() {
        super.onResume()
        logEvent()
    }

    private fun logEvent() {
        val mainAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        // Log the custom event
        mainAnalytics.logEvent("screen_share_opened", bundle)
    }

    private fun initViews() {
        feedbackHandler = Handler(Looper.getMainLooper())
         appSettings = AppSettings(this)
        // Set click listeners for buttons
        binding.shareBtn.setOnClickListener(this)
        binding.startNew.setOnClickListener(this)

        // Load the QR image URI if it is available
        imageShareUri = Constants.finalQrImageUri
        imageShareUri?.let {
            binding.shareQrGeneratedImg.setImageURI(it)
        }
        intent?.let {
            if (it.hasExtra("TYPE")) {
                type = it.getStringExtra("TYPE") as String
            }
            if (it.hasExtra("DATA")) {
                data = it.getStringExtra("DATA") as String
            }
        }

        if (type == "vcard"){
            binding.digitalCardLinkViewWrapper.visibility = View.VISIBLE
            binding.digitalCardLinkView.text = data
            binding.copyLinkBtn.setOnClickListener {
                copyToClipboard(context,data)
            }
        }

        feedbackRunnable = Runnable {
            if (shouldShowDialog()) {
                showPopUpFeedback()
                appSettings.putLong(Constants.LAST_SHOWN_DATE_KEY, Calendar.getInstance().timeInMillis)
            }
        }

        // Post the delayed task
        feedbackHandler.postDelayed(feedbackRunnable, 10000)

    }

    private fun showPopUpFeedback() {
        val view = layoutInflater.inflate(R.layout.layout_dialog_rate_us_with_comment, null)
        val builder = AlertDialog.Builder(context)
            .setCancelable(false)
            .setView(view)

        val later = view.findViewById<AppCompatTextView>(R.id.laterTv)
        val ratingBar = view.findViewById<AppCompatRatingBar>(R.id.ratingBar)
        val commentBox = view.findViewById<TextInputEditText>(R.id.text_input_field)
        val messageTv = view.findViewById<AppCompatTextView>(R.id.messageTv)
        val submitBtn = view.findViewById<AppCompatTextView>(R.id.submitTv)

        val alertDialog = builder.show()
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            if (rating <= 3.0) {
                commentBox.visibility = View.VISIBLE
                messageTv.visibility = View.VISIBLE
                submitBtn.visibility = View.VISIBLE
            } else {
                commentBox.visibility = View.GONE
                messageTv.visibility = View.GONE
                submitBtn.visibility = View.GONE
                alertDialog.dismiss()
                rateAppOnPlay()
            }
//            alertDialog.dismiss()
        }

        submitBtn.setOnClickListener {
            val comment = commentBox.text.toString().trim()
            if(comment.isNotEmpty()){
                startLoading(context)
              DataRepository.addUserFeedback(comment){response->
                  dismiss()
                  if(response == "success")
                  {
                      appSettings.putString("POPUP_FEEDBACK","done")
                      alertDialog.dismiss()
                      showAlert(context,getString(R.string.feedback_success_message))
                  }
                  else{
                      showAlert(context,getString(R.string.something_wrong_error))
                  }
              }
            }
        }

        later.setOnClickListener {
            DialogPrefs.clearPreferences(context)
            alertDialog.dismiss()
        }
    }

    private fun shouldShowDialog(): Boolean {
        val lastShownDate = appSettings.getLong(Constants.LAST_SHOWN_DATE_KEY)
        val currentDate = Calendar.getInstance().timeInMillis

        // 7 days in milliseconds
        val oneWeekInMillis = 7 * 24 * 60 * 60 * 1000
        val popUpFeedbackStatus = appSettings.getString("POPUP_FEEDBACK") as String

        return ((currentDate - lastShownDate) >= oneWeekInMillis) && (popUpFeedbackStatus.isEmpty() || popUpFeedbackStatus != "done")
    }

    // Opens Play Store to rate the app
    private fun rateAppOnPlay() {
        val rateIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${packageName}"))
       startActivity(rateIntent)
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.share_qr_image)
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.share_btn -> handleShareButtonClick()
            R.id.start_new -> handleStartNewButtonClick()
        }
    }

    private fun handleShareButtonClick() {
        DialogPrefs.setShared(context, true)
        MaterialAlertDialogBuilder(context)
            .setMessage(getString(R.string.qr_image_share_warning_text))
            .setCancelable(false)
            .setNegativeButton(getString(R.string.cancel_text)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.share_text)) { _, _ ->
                shareImage()
            }
            .create()
            .show()
    }

    private fun shareImage() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            imageShareUri?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                putExtra(Intent.EXTRA_STREAM, it)
            }
        }
        shareResultLauncher.launch(
            Intent.createChooser(shareIntent, "Share with")
        )
    }

    private fun handleStartNewButtonClick() {
        GeneratorManager.resetQRGenerator()
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("KEY", "generator")
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    // This launcher handles the result after sharing the QR image
    private val shareResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Handle the result if needed
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        feedbackHandler.removeCallbacks(feedbackRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        feedbackHandler.removeCallbacks(feedbackRunnable)
    }
}
