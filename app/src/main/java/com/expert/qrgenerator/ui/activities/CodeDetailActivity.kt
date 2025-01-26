package com.expert.qrgenerator.ui.activities

import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.FeedbackAdapter
import com.expert.qrgenerator.adapters.TimestampAdapter
import com.expert.qrgenerator.databinding.ActivityCodeDetailBinding
import com.expert.qrgenerator.databinding.BarcodeDetailItemRowBinding
import com.expert.qrgenerator.databinding.UpdateBarcodeDetailDialogBinding
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.Feedback
import com.expert.qrgenerator.model.TableObject
import com.expert.qrgenerator.model.TrackableScan
import com.expert.qrgenerator.repository.DataRepository
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.ui.fragments.YouTubeDialogFragment
import com.expert.qrgenerator.utils.AppSettings
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.DialogPrefs
import com.expert.qrgenerator.utils.ImageManager
import com.expert.qrgenerator.utils.TableGenerator
import com.expert.qrgenerator.viewmodel.CodeDetailViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.expert.qrgenerator.ui.activities.BaseActivity.Companion.safeSubstring
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLEncoder
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

@AndroidEntryPoint
class CodeDetailActivity : BaseActivity(), View.OnClickListener {

    // Binding for Activity Code Detail layout
    private lateinit var binding: ActivityCodeDetailBinding

    // Context of the activity, initialized using lazy delegation
    private val context: Context by lazy { this }

    // Holds code history data
    private var codeHistory: CodeHistory? = null

    // Holds table object information
    private var tableObject: TableObject? = null

    // Generator for creating tables
    private lateinit var tableGenerator: TableGenerator

    // ViewModels for the activity
    private val appViewModel: AppViewModel by viewModels()
    private val viewModel: CodeDetailViewModel by viewModels()

    // Table name used for various operations
    private lateinit var tableName: String

    // Variables related to PDF generation
    var bitmap: Bitmap? = null
    private val pageWidth = 500 // Width of the PDF page
    private val pageHeight = 500 // Height of the PDF page
    private var pdfFile: File? = null
    private var isShareAfterCreated: Boolean =
        false // Flag to check if the PDF should be shared after creation

    // Protocol selection and barcode edit list
    var selectedProtocol: String = ""
    var barcodeEditList = mutableListOf<Triple<AppCompatImageView, String, String>>()

    // Counter for various operations
    private var counter: Int = 0

    // A mutable list to hold feedback items. This list starts empty and will be populated later.
    var feedbacksList: MutableList<Feedback> = mutableListOf()

    // Adapter for displaying feedback items in a RecyclerView or similar UI component.
// Initialized later when the data is ready to be bound to the UI.
    lateinit var feedbackAdapter: FeedbackAdapter

    private var timestampList = mutableListOf<TrackableScan>()
    private lateinit var timestampAdapter: TimestampAdapter

    private var scanDateList = mutableListOf<String>()
    private var totalScans = 0
    private lateinit var feedbackHandler: Handler
    private lateinit var feedbackRunnable: Runnable
    private lateinit var appSettings: AppSettings
    val tipList = listOf("seven", "eight", "nine", "ten", "eleven")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout and set the content view
        binding = ActivityCodeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views
        initViews()

        // Set up the toolbar
        setUpToolbar()

        // Display code details
        displayCodeDetails()

        feedbackRunnable = Runnable {
            if (shouldShowDialog()) {
                showPopUpFeedback()
                appSettings.putLong(
                    Constants.LAST_SHOWN_DATE_KEY,
                    Calendar.getInstance().timeInMillis
                )
            }
        }

        // Post the delayed task
        feedbackHandler.postDelayed(feedbackRunnable, 10000)
    }

    private fun shouldShowDialog(): Boolean {
        val lastShownDate = appSettings.getLong(Constants.LAST_SHOWN_DATE_KEY)
        val currentDate = Calendar.getInstance().timeInMillis

        // 7 days in milliseconds
        val oneWeekInMillis = 7 * 24 * 60 * 60 * 1000
        val popUpFeedbackStatus = appSettings.getString("POPUP_FEEDBACK") as String

        return ((currentDate - lastShownDate) >= oneWeekInMillis) && (popUpFeedbackStatus.isEmpty() || popUpFeedbackStatus != "done")
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
            if (comment.isNotEmpty()) {
                startLoading(context)
                DataRepository.addUserFeedback(comment) { response ->
                    dismiss()
                    if (response == "success") {
                        appSettings.putString("POPUP_FEEDBACK", "done")
                        alertDialog.dismiss()
                        showAlert(context, getString(R.string.feedback_success_message))
                    } else {
                        showAlert(context, getString(R.string.something_wrong_error))
                    }
                }
            }
        }

        later.setOnClickListener {
            DialogPrefs.clearPreferences(context)
            alertDialog.dismiss()
        }
    }

    // Opens Play Store to rate the app
    private fun rateAppOnPlay() {
        val rateIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${packageName}"))
        startActivity(rateIntent)
    }

    // This function initializes all views and references to objects
    private fun initViews() {
        feedbackHandler = Handler(Looper.getMainLooper())
        appSettings = AppSettings(this)
        tableGenerator = TableGenerator(context)

        // Retrieve data from the intent if available
        if (intent != null && intent.hasExtra("HISTORY_ITEM")) {
            codeHistory = intent.getSerializableExtra("HISTORY_ITEM") as CodeHistory
        }

        if (intent != null && intent.hasExtra("TABLE_ITEM")) {
            tableObject = intent.getSerializableExtra("TABLE_ITEM") as TableObject
        }
        if (intent != null && intent.hasExtra("TABLE_NAME")) {
            tableName = intent.getStringExtra("TABLE_NAME") as String
        }


        // Set up onClick listeners for various buttons
        binding.apply {
            codeDetailClipboardCopyView.setOnClickListener(this@CodeDetailActivity)
            codeDetailTextSearchButton.setOnClickListener(this@CodeDetailActivity)
            codeDetailTextShareButton.setOnClickListener(this@CodeDetailActivity)
            codeDetailPdfSaveButton.setOnClickListener(this@CodeDetailActivity)
            codeDetailPdfShareButton.setOnClickListener(this@CodeDetailActivity)
            dynamicLinkUpdateBtn.setOnClickListener(this@CodeDetailActivity)
            updateNotesBtn.setOnClickListener(this@CodeDetailActivity)
        }

        // Set up the HTTP protocol radio buttons
        binding.httpProtocolGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedProtocol = when (checkedId) {
                R.id.http_protocol_rb -> "http://"
                R.id.https_protocol_rb -> "https://"
                else -> ""
            }
        }

        manageTipsSequentially(tipList)
    }

    private fun manageTipsSequentially(
        keys: List<String>
    ) {
        // Start with the first unhidden tip
//        for (key in keys) {
//            if (!appSettings.getBoolean("${key}_status")) {
        // Show the tip for the current key
        when (val key = keys[currentTipIndex]) {
            "seven" -> showTip(binding.infoImageView, key)
            "eight" -> showTip(binding.infoImageView1, key)
            "nine" -> showTip(binding.infoImageView2, key)
            "ten" -> showTip(binding.infoImageView3, key)
            "eleven" -> showTip(binding.infoImageView4, key)
        }
        return // Stop once a tip is shown
//            }
//        }
    }

    private var currentTipIndex = 0
    private fun showTip(view: AppCompatImageView, value: String) {
        view.setOnClickListener {
            if (currentTipIndex != tipList.size - 1) {
                currentTipIndex++
            }
            Constants.clearShakeAnimation(view)
//            view.visibility = View.GONE
            appSettings.putBoolean("${value}_status", true) // Mark as hidden
            openTipDialog(value)

            // Trigger the next tip display
            manageTipsSequentially(tipList)
        }

        if (appSettings.getBoolean("${value}_status")) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.VISIBLE
            Constants.startShakeAnimation(view)
        }
    }

    private fun openTipDialog(key: String) {
        val tip = Constants.getTip(key)
        if (tip != null) {
            logCustomEvent(context,"code_detail_screen","event",
                tip.description.safeSubstring(160))
            val dialog = YouTubeDialogFragment(tip)
            dialog.show(supportFragmentManager, "YouTubeDialogFragment")
        }
    }

    override fun onPause() {
        super.onPause()
        // Remove the runnable or cancel the task when the activity is paused
        feedbackHandler.removeCallbacks(feedbackRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources, cancel any ongoing tasks if necessary
        feedbackHandler.removeCallbacks(feedbackRunnable)
    }

    // Sets up the ActionBar/Toolbar for the activity
    private fun setUpToolbar() {
        // Set the toolbar as the ActionBar
        setSupportActionBar(binding.toolbar)

        // Check if the ActionBar is not null
        supportActionBar?.let { actionBar ->
            // Set the title for the ActionBar
            actionBar.title = getString(R.string.code_detail_text)

            // Enable the Up button to navigate back
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        // Set the title text color of the Toolbar
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }


    /**
     * Binds and displays details of the code history.
     * Depending on the code type, it updates the UI components with appropriate data and visibility settings.
     */
    private fun displayCodeDetails() {
        // Check if codeHistory is not null
        if (codeHistory != null) {
            // Make the notes section visible
            binding.codeDetailNotes.visibility = View.VISIBLE
            binding.currentQrCodeIdView.text = codeHistory!!.qrId

            binding.updateQrIdBtn.setOnClickListener {
                if (codeHistory!!.type == "trackable") {
                    logCustomEvent(context, "trackable_type_qr_id_update")
                }

                if (binding.qrCodeIdInputField.text.toString().isNotEmpty()) {
                    codeHistory!!.qrId = binding.qrCodeIdInputField.text.toString()
                    appViewModel.updateHistory(codeHistory!!)
                    binding.currentQrCodeIdView.text = codeHistory!!.qrId
                    binding.qrCodeIdInputField.setText("")
                }
            }

            binding.qrCodeIdInputField.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(editable: Editable?) {
                    editable?.let {
                        // Replace spaces with underscores
                        var result = it.toString().replace(" ", "_")

                        // Remove special characters by keeping only allowed characters
                        result = result.replace(Constants.allowedCharactersRegex, "")

                        // Update the EditText only if the result is different
                        if (result != it.toString()) {
                            binding.qrCodeIdInputField.setText(result)
                            binding.qrCodeIdInputField.setSelection(result.length) // Move the cursor to the end
                        }
                    }
                }
            })

            // Update UI based on the code type
            when (codeHistory!!.codeType) {
                "barcode" -> {
                    updateUIForBarcode()
                }

                else -> {
                    updateUIForQRCode()
                }
            }

            // Set data to UI components
            binding.codeDetailEncodeData.text = codeHistory!!.data
            binding.codeDetailCodeSequenceView.text =
                "${getString(R.string.code_text)} ${codeHistory!!.id}"
            binding.codeDetailDateTimeView.text =
                getFormattedDate(context, codeHistory!!.createdAt.toLong())

            // Display notes or set hint if notes are empty
            binding.qrCodeHistoryNotesInputField.apply {
                if (codeHistory!!.notes.isEmpty()) {
                    hint = getString(R.string.description_text)
                } else {
                    setText(codeHistory!!.notes)
                }
            }

            // Handle dynamic links and feedbacks
            handleDynamicLinks()
            if (codeHistory!!.type == "feedback") {
                displayFeedbacksDetail(codeHistory!!.qrId)
            }
        } else {
            // If codeHistory is null, check for tableObject and display barcode detail
            if (tableObject != null) {
                binding.codeDetailNotes.visibility = View.GONE
                displayBarcodeDetail()
            }
        }
    }

    /**
     * Updates UI components for barcode type.
     */
    private fun updateUIForBarcode() {
        binding.codeDetailTopImageType.setImageResource(R.drawable.barcode)
        binding.codeDetailTypeTextHeading.text = getString(R.string.barcode_text_data_heding)
        binding.codeDetailTypeImageHeading.text = getString(R.string.barcode_image_heading)
        binding.codeDetailImageType.setImageResource(R.drawable.barcode)
    }

    /**
     * Updates UI components for QR code type.
     */
    private fun updateUIForQRCode() {
        binding.codeDetailTopImageType.setImageResource(R.drawable.ic_qr_code)
        binding.codeDetailTypeTextHeading.text = getString(R.string.qr_text_data_heading)
        binding.codeDetailTypeImageHeading.text = getString(R.string.qr_image_heading)
//        binding.codeDetailImageType.setImageResource(R.drawable.qrcode)
        Glide.with(context)
            .load(codeHistory!!.localImagePath)
            .error(R.drawable.qrcode)
            .override(200, 200) // Set the desired width and height
            .into(binding.codeDetailImageType)

        if (codeHistory!!.type == "trackable" || codeHistory!!.type == "advance") {
            binding.aiRecommendationLayout.visibility = View.VISIBLE
            binding.conversionRevenueLayout.visibility = View.VISIBLE
            binding.conversionInputField.setText("${codeHistory!!.conversion}")
            binding.revenueInputField.setText("${codeHistory!!.revenue}")
            binding.expensesInputField.setText("${codeHistory!!.expenses}")
            binding.scanHistoryLayout.visibility = View.VISIBLE
            binding.scansHistoryRecyclerview.layoutManager = LinearLayoutManager(context)
            timestampAdapter = TimestampAdapter(timestampList)
            binding.scansHistoryRecyclerview.adapter = timestampAdapter
            val dividerItemDecoration =
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            binding.scansHistoryRecyclerview.addItemDecoration(dividerItemDecoration)
            getTrackableScanHistory()

        } else {
            binding.conversionRevenueLayout.visibility = View.GONE
            binding.aiRecommendationLayout.visibility = View.GONE
            binding.scanHistoryLayout.visibility = View.GONE
        }

        binding.scanListRefreshBtn.setOnClickListener {
            getTrackableScanHistory()
        }

        binding.aiRecommendationBtn.setOnClickListener {
            if (codeHistory!!.type == "trackable" || codeHistory!!.type == "advance") {
                logCustomEvent(context, "trackable_type_ai_button_click")
            }
            if (scanDateList.size >= 10) {
                binding.aiRecommendationBtn.isEnabled = false
                val conversion = binding.conversionInputField.text.toString()
                val revenue = binding.revenueInputField.text.toString()
                val expenses = binding.expensesInputField.text.toString()

                if (conversion.isNotEmpty() ||
                    revenue.isNotEmpty()
                    || expenses.isNotEmpty()
                ) {


                    val prompt = generateQrAnalysisMessage(
                        codeHistory!!.qrId, totalScans, scanDateList,
                        if (conversion.isEmpty()) {
                            "0".toFloat()
                        } else {
                            conversion.toFloat()
                        },
                        if (revenue.isEmpty()) {
                            "0".toFloat()
                        } else {
                            revenue.toFloat()
                        },
                        if (expenses.isEmpty()) {
                            "0".toFloat()
                        } else {
                            expenses.toFloat()
                        }
                    )

                    codeHistory!!.conversion = conversion
                    codeHistory!!.revenue = revenue
                    codeHistory!!.expenses = expenses
                    appViewModel.updateHistory(codeHistory!!)
                    startLoading(context)
                    lifecycleScope.launch {
                        viewModel.callAiRecommendationRequest(prompt) { result ->
                            dismiss()
                            binding.aiRecommendationView.text = result
                            binding.aiRecommendationBtn.isEnabled = true
                        }
                    }

                } else {
                    showAlert(context, getString(R.string.conversion_revenue_expense_field_empty))
                    binding.aiRecommendationBtn.isEnabled = true
                }
            } else {
                showAlert(context, getString(R.string.ai_analysis_limit_error))
            }

        }

        binding.updateConversionRevenueExpenseBtn.setOnClickListener {
            val conversionInput = binding.conversionInputField.text.toString().trim()
            val revenueInput = binding.revenueInputField.text.toString().trim()
            val expensesInput = binding.expensesInputField.text.toString().trim()
            if (conversionInput.isNotEmpty() && conversionInput.toInt() > 100) {
                showAlert(context, "Conversion will not more than 100%")
            } else {
                codeHistory!!.conversion = conversionInput
                codeHistory!!.revenue = revenueInput
                codeHistory!!.expenses = expensesInput
                appViewModel.updateHistory(codeHistory!!)
                showAlert(context, "Results parameters has been updated!")
            }
        }
    }

    private fun getTrackableScanHistory() {

        // Observe the LiveData from the ViewModel for trackable scans
        viewModel.callTrackableScans(codeHistory!!.qrId)
        viewModel.trackableScanList.observe(this@CodeDetailActivity) { list ->
            list?.let {
                binding.totalScansView.text = "Total: ${list.size}"
                totalScans = list.size
                if (list.isNotEmpty()) {
                    binding.emptyHistoryTextview.visibility = View.GONE
                    binding.scansHistoryRecyclerview.visibility = View.VISIBLE
                    timestampList.clear()
                    timestampList.addAll(list)
                    timestampAdapter.notifyDataSetChanged()
                    scanDateList.clear()

                    for (element in list) {
                        scanDateList.add(getDateTimeFromTimeStamp1(element.timestamp!!))
                    }
                } else {
                    binding.emptyHistoryTextview.visibility = View.VISIBLE
                    binding.scansHistoryRecyclerview.visibility = View.GONE
                }
            }
        }
    }

//    private fun generateQrAnalysisMessage(
//        qrCodeId: String,
//        numberOfScans: Int,
//        dateList: List<String>,
//        conversions: Int,
//        profit: Double,
//        expenses: Double
//    ): String {
//        val dateString = dateList.joinToString(", ") // Converts the date list to a comma-separated string
//
//        return """
//        Analyze the following QR code data and provide actionable suggestions to improve conversions and profits (limit response to 500 characters). Return the result as a readable list:
//
//        QR Code ID: $qrCodeId
//
//        Number of Scans: $numberOfScans
//
//        Scan Dates: $dateString
//
//        User Input: Conversions: $conversions, Profit: $profit, Expenses: $expenses
//
//
//        Focus on identifying patterns and offering practical recommendations.
//    """.trimIndent()
//    }

    private fun generateQrAnalysisMessage(
        qrCodeId: String,
        numberOfScans: Int,
        dateList: List<String>,
        conversions: Float,
        profit: Float,
        expenses: Float
    ): String {
        val dateString =
            dateList.joinToString(", ") // Converts the date list to a comma-separated string

        // Create a list to hold non-empty lines
        val messageLines = mutableListOf<String>()

        // Always add the QR Code ID
        messageLines.add("QR Code ID: $qrCodeId")

        // Add number of scans if it's greater than 0
        if (numberOfScans > 0) {
            messageLines.add("Number of Scans: $numberOfScans")
        }

        // Add scan dates if the list is not empty
        if (dateList.isNotEmpty()) {
            messageLines.add("Scan Dates: $dateString")
        }

        // Add user input only if the values are greater than 0
        if (conversions > 0) {
            messageLines.add("Conversions: $conversions")
        }
        if (profit > 0) {
            messageLines.add("Profit: $profit")
        }
        if (expenses > 0) {
            messageLines.add("Expenses: $expenses")
        }
        val qrData = messageLines.joinToString("\n")
        // Generate the final message with the focused recommendations
        val promptTemplate = """
        ${Constants.singlePrompt}
    """.trimIndent()
        Log.d("TEST1000", promptTemplate.replace("{messageLines}", qrData))
        return promptTemplate.replace("{messageLines}", qrData)
    }


    /**
     * Handles the visibility and content for dynamic links.
     */
    private fun handleDynamicLinks() {
        if (codeHistory!!.isDynamic.toInt() == 1 || codeHistory!!.type == "advance") {
            binding.codeDetailDynamicLinkUpdateLayout.visibility = View.VISIBLE
            binding.dialogSubHeading.text =
                "${getString(R.string.current_link_text)} ${codeHistory!!.data}"
        } else {
            binding.codeDetailDynamicLinkUpdateLayout.visibility = View.GONE
        }
    }

    private fun displayFeedbacksDetail(qrId: String) {
        // Set up RecyclerView
        binding.codeDetailFeedbackRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true) // Use setHasFixedSize for better performance if layout size is fixed
            adapter = feedbackAdapter // Set adapter after initializing
        }

        // Set up CSV export button click listener
        binding.codeDetailFeedbackCsvExportImage.setOnClickListener {
            exportCsv() // Export feedbacks to CSV
        }

        // Show loading indicator
        startLoading(context)

        // Fetch feedbacks asynchronously
        lifecycleScope.launch {
            viewModel.callFeedbacks(qrId)
        }

        // Observe feedbacks response from the ViewModel
        viewModel.feedbackResponse.observe(this@CodeDetailActivity) { response ->
            dismiss() // Hide loading indicator

            response?.let {
                // Add new feedbacks to the list
                feedbacksList.addAll(it.feedbacks)

                // Update UI based on feedbacks availability
                if (feedbacksList.isNotEmpty()) {
                    binding.codeDetailFeedbackLayout.visibility = View.VISIBLE
                    feedbackAdapter.notifyItemRangeChanged(
                        0,
                        feedbacksList.size
                    ) // Notify adapter of new items

                    // Set item click listener for feedbacks
                    feedbackAdapter.setOnItemClickListener(object :
                        FeedbackAdapter.OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            val item = feedbacksList[position]
                            val sharingText =
                                "Feedback: ${item.comment}\nEmail: ${item.email}\nPhone: ${item.phone}\nStars: ${item.rating}\n ${
                                    getString(R.string.qr_sign)
                                }"
                            MaterialAlertDialogBuilder(context)
                                .setMessage(sharingText)
                                .setNegativeButton(getString(R.string.cancel_text)) { dialog, _ ->
                                    dialog.dismiss() // Dismiss dialog on cancel
                                }
                                .setPositiveButton(getString(R.string.share_text)) { dialog, _ ->
                                    dialog.dismiss() // Dismiss dialog on share
                                    shareFeedback(sharingText) // Share feedback
                                }
                                .create()
                                .show() // Show the dialog
                        }
                    })
                } else {
                    binding.codeDetailFeedbackLayout.visibility =
                        View.GONE // Hide layout if no feedbacks
                }
            }
        }
    }

    /**
     * Exports the feedbacks list to a CSV file and shares it via an Intent.
     */
    private fun exportCsv() {
        // Check if the feedbacks list is not empty
        if (feedbacksList.isNotEmpty()) {
            // Start loading indication (e.g., progress bar)
            startLoading(context)

            // Create a StringBuilder to build the CSV content
            val builder = StringBuilder()

            // Append CSV header
            builder.append("id,qrId,comment,email,phone,rating")

            // Iterate through feedbacks list and append each item to the CSV content
            for (data in feedbacksList) {
                builder.append("\n${data.id},${data.qrId},${data.comment},${data.email},${data.phone},${data.rating}")
            }

            try {
                // Define the file name and create the file
                val fileName = "feedbacks_${feedbacksList[0].qrId}.csv"
                val out = openFileOutput(fileName, Context.MODE_PRIVATE)
                out.write(builder.toString().toByteArray())
                out.close()

                // Create a File object and get its URI
                val file = File(filesDir, fileName)
                val path =
                    FileProvider.getUriForFile(context, "com.expert.qrgenerator.fileprovider", file)

                // Dismiss loading indication
                dismiss()

                // Create an Intent to share the CSV file
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(Intent.EXTRA_STREAM, path)
                }

                // Start the share activity
                startActivity(Intent.createChooser(intent, getString(R.string.share_using)))
            } catch (e: Exception) {
                // Print stack trace for debugging
                e.printStackTrace()
            }
        } else {
            // Show an alert if the feedbacks list is empty
            showAlert(context, getString(R.string.table_export_error_text))
        }
    }

    /**
     * Launches an intent to share feedback text using available sharing options.
     *
     * @param sharingText The text to be shared.
     */
    private fun shareFeedback(sharingText: String) {
        // Create an intent with the ACTION_SEND action to share text content
        val intent = Intent(Intent.ACTION_SEND).apply {
            // Set the MIME type to "text/plain" to specify the type of data being shared
            type = "text/plain"
            // Add the text to be shared as an extra in the intent
            putExtra(Intent.EXTRA_TEXT, sharingText)
        }

        // Start an activity with a chooser to allow the user to select their preferred sharing method
        startActivity(Intent.createChooser(intent, getString(R.string.share_using)))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        menu!!.findItem(R.id.create).isVisible = true
//        menu.findItem(R.id.compare).isVisible = true
        menu.findItem(R.id.history).isVisible = true
        menu.findItem(R.id.analytics).isVisible = true
        return true
    }

    /**
     * Handles the item selection in the options menu.
     *
     * @param item The menu item that was selected.
     * @return True if the event was handled, false otherwise.
     */

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            R.id.analytics -> {
                startActivity(Intent(context, AnalyticsActivity::class.java))
                true
            }

            R.id.create -> {
                startActivity(Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                true
            }

            R.id.history -> {
                startActivity(Intent(context, BarcodeHistoryActivity::class.java))
                true
            }

//            R.id.compare -> {
//                startActivity(Intent(context, CodeComparisonActivity::class.java))
//                true
//            }

            else -> {
                // Pass the event to the superclass to handle other menu items
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.code_detail_clipboard_copy_view -> {
                val clipboard: ClipboardManager =
                    getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(
                    clipboard.primaryClipDescription!!.label,
                    binding.codeDetailEncodeData.text.toString()
                )
                clipboard.setPrimaryClip(clip)
                Toast.makeText(
                    context,
                    getString(R.string.text_saved_clipboard),
                    Toast.LENGTH_SHORT
                ).show()
            }

            R.id.code_detail_text_search_button -> {
                val escapedQuery: String = URLEncoder.encode(
                    binding.codeDetailEncodeData.text.toString().trim(), "UTF-8"
                )
                val intent = Intent(Intent.ACTION_WEB_SEARCH)
                intent.putExtra(SearchManager.QUERY, escapedQuery)
                startActivity(intent)
            }

            R.id.code_detail_text_share_button -> {
                textShare()
            }

            R.id.code_detail_pdf_save_button -> {
                if (codeHistory!!.type == "trackable" || codeHistory!!.type == "advance") {
                    logCustomEvent(context, "trackable_type_save_pdf")
                }
//                if (RuntimePermissionHelper.checkStoragePermission(
//                        context,
//                        Constants.READ_STORAGE_PERMISSION
//                    )
//                ) {
//                    createPdf(false)
//                }
                saveImageToGallery(codeHistory!!.localImagePath)
//                Toast.makeText(this, getString(R.string.image_saved_success_text), Toast.LENGTH_SHORT).show()
            }

            R.id.code_detail_pdf_share_button -> {
                isShareAfterCreated = true
                if (codeHistory!!.type == "trackable" || codeHistory!!.type == "advance") {
                    logCustomEvent(context, "trackable_type_save_and_share_pdf")
                }

                shareImage(codeHistory!!.localImagePath)
//                if (RuntimePermissionHelper.checkStoragePermission(
//                        context,
//                        Constants.READ_STORAGE_PERMISSION
//                    )
//                ) {
//                    if (pdfFile == null) {
//                        createPdf(true)
//                    } else {
//                        sharePdfFile()
//                    }
//                }

            }

            R.id.dynamic_link_update_btn -> {
                val value = binding.qrCodeHistoryDynamicLinkInputField.text.toString().trim()
                if (selectedProtocol.isEmpty()) {
                    showAlert(
                        context,
                        getString(R.string.protocol_error)
                    )
                } else if (value.isEmpty()) {

                    showAlert(
                        context,
                        getString(R.string.required_data_input_error)
                    )

                } else if (value.contains("http://") || value.contains("https://")
                ) {
                    showAlert(
                        context,
                        getString(R.string.without_protocol_error)
                    )
                } else if (!Pattern.compile("^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?\$")
                        .matcher(value).find()
                ) {
                    showAlert(
                        context,
                        getString(R.string.valid_website_error)
                    )
                } else {
                    val hashMap = hashMapOf<String, String>()
                    hashMap["login"] = codeHistory!!.login
                    hashMap["qrId"] = codeHistory!!.qrId
                    hashMap["userUrl"] = "$selectedProtocol$value"
                    hashMap["userType"] = codeHistory!!.userType

                    startLoading(context)
                    lifecycleScope.launch {
                        viewModel.createDynamicQrCode(hashMap)
                    }
                    viewModel.dynamicQrCodeResponse.observe(this) { response ->
                        var url = ""
                        dismiss()
                        if (response != null) {
                            url = response.get("generatedUrl").asString
                            binding.dialogSubHeading.text =
                                "${getString(R.string.current_link_text)} $selectedProtocol$value"
                            binding.codeDetailEncodeData.text = "$selectedProtocol$value"
                            appViewModel.update("$selectedProtocol$value", url, codeHistory!!.id)
                            showAlert(context, getString(R.string.dynamic_update_success_text))
                        } else {
                            showAlert(context, getString(R.string.something_wrong_error))
                        }
                    }
                }
            }

            R.id.update_notes_btn -> {
                if (codeHistory!!.type == "trackable" || codeHistory!!.type == "advance") {
                    logCustomEvent(context, "trackable_type_notes_update")
                }
                val notesText = binding.qrCodeHistoryNotesInputField.text.toString().trim()
                if (notesText.isNotEmpty()) {
                    codeHistory!!.notes = notesText
                    appViewModel.updateHistory(codeHistory!!)

                    Toast.makeText(
                        context,
                        getString(R.string.description_update_success_text),
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.qrCodeHistoryNotesInputField.clearFocus()
//                    binding.qrCodeHistoryNotesInputField.setText("")
                    hideSoftKeyboard(context, binding.qrCodeHistoryNotesInputField)

                } else {
                    showAlert(context, getString(R.string.empty_text_error))
                }
            }

            else -> {
                val position = v.id
                val id = barcodeEditList[0].second.toInt()
                val triple = barcodeEditList[position + 1]

                updateBarcodeDetail(id, triple)
            }
        }
    }

    private fun saveImageToGallery(imagePath: String) {
        // Create a File object from the image path
        val imageFile = File(imagePath)

        if (imageFile.exists()) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, imageFile.name) // Use original file name
                put(
                    MediaStore.Images.Media.MIME_TYPE,
                    "image/jpeg"
                ) // Adjust MIME type if necessary
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES
                ) // Save to Pictures
            }

            // Insert the image into the MediaStore
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            uri?.let {
                try {
                    // Use FileInputStream to read the image
                    val inputStream = FileInputStream(imageFile)
                    val outputStream = contentResolver.openOutputStream(it)

                    // Copy the image to the output stream
                    inputStream.copyTo(outputStream!!)
                    outputStream.close()
                    inputStream.close()
                    Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "File does not exist", Toast.LENGTH_SHORT).show()
        }
    }


    private fun shareImage(imagePath: String) {
        val imageFile = File(imagePath)
        if (imageFile.exists()) {
            val imageUri: Uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                imageFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                imageUri.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    putExtra(Intent.EXTRA_STREAM, it)
                }
            }
            shareResultLauncher.launch(
                Intent.createChooser(shareIntent, "Share with")
            )
        } else {
            showAlert(context, getString(R.string.image_file_not_exist))
        }
    }

    // This launcher handles the result after sharing the QR image
    private val shareResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Handle the result if needed
        }

    private fun updateBarcodeDetail(id: Int, triple: Triple<AppCompatImageView, String, String>) {
        // Inflate the dialog layout using View Binding
        val updateBarcodeLayoutBinding =
            UpdateBarcodeDetailDialogBinding.inflate(LayoutInflater.from(context))

        // Access views directly through the binding
        val updateInputBox = updateBarcodeLayoutBinding.updateBarcodeDetailTextInputField
        val cleanBrushView = updateBarcodeLayoutBinding.updateBarcodeDetailCleaningTextView
        val cancelBtn = updateBarcodeLayoutBinding.updateBarcodeDetailDialogCancelBtn
        val updateBtn = updateBarcodeLayoutBinding.updateBarcodeDetailDialogUpdateBtn

        // Build and show the dialog
        val builder = MaterialAlertDialogBuilder(context)
            .setView(updateBarcodeLayoutBinding.root)
            .setCancelable(false)

        val alert = builder.create()
        alert.show()

        // Set up click listeners
        cancelBtn.setOnClickListener {
            hideSoftKeyboard(context, cancelBtn)
            alert.dismiss()
        }

        cleanBrushView.setOnClickListener {
            updateInputBox.setText("")
        }

        updateBtn.setOnClickListener {
            val value = updateInputBox.text.toString().trim()
            if (value.isNotEmpty()) {
                hideSoftKeyboard(context, updateBtn)
                alert.dismiss()
                // Update the barcode detail
                val isUpdate =
                    tableGenerator.updateBarcodeDetail(tableName, triple.third, value, id)
                if (isUpdate) {
                    // Refresh the barcode details
                    tableObject = tableGenerator.getUpdateBarcodeDetail(tableName, id)
                    displayBarcodeDetail()
                }
            } else {
                Toast.makeText(context, getString(R.string.empty_text_error), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Set initial text and open keyboard
        updateInputBox.setText(triple.second)
        updateInputBox.setSelection(updateInputBox.text!!.length)
        updateInputBox.requestFocus()
        Constants.openKeyboard(context)
    }


    private fun displayBarcodeDetail() {
        // Check if tableObject is not null
        tableObject?.let { tableObj ->
            // Make the barcode detail layout visible
            binding.barcodeDetailWrapperLayout.visibility = View.VISIBLE

            // Set static details in the views
            binding.codeDetailEncodeData.text = tableObj.code_data
            binding.codeDetailCodeSequenceView.text =
                "${getString(R.string.code_text)} ${tableObj.id}"
            binding.codeDetailDateTimeView.text = tableObj.date

            // Clear previous child views if any
            binding.barcodeDetailWrapperLayout.removeAllViews()

            // Initialize counter for dynamic view IDs
            var counter = 0

            // Add static details to the layout
            addDetailView("id", tableObj.id.toString(), counter++)
            addDetailView("code_data", tableObj.code_data, counter++)
            addDetailView("date", tableObj.date, counter++)
            addDetailView("image", tableObj.image, counter++)

            // Add dynamic columns to the layout
            tableObj.dynamicColumns.forEach { item ->
                addDetailView(item.first, item.second, counter++)
            }
        }
    }

    // Helper function to add a detail view to the layout
    private fun addDetailView(columnName: String, columnValue: String, viewId: Int) {
        val itemRowBinding = BarcodeDetailItemRowBinding.inflate(
            LayoutInflater.from(context),
            binding.barcodeDetailWrapperLayout as ViewGroup,
            false
        )

        // Set view ID and data
        itemRowBinding.bcdEditView.id = viewId
        itemRowBinding.bcdEditView.setOnClickListener(this)
        itemRowBinding.bcdTableColumnName.text = columnName
        itemRowBinding.bcdTableColumnValue.text = columnValue

        // Add the view to the layout
        binding.barcodeDetailWrapperLayout.addView(itemRowBinding.root)
    }

    // Function to share code text with others
    private fun textShare() {
        // Create an intent for sharing text
        val intent = Intent(Intent.ACTION_SEND).apply {
            // Set the MIME type for text sharing
            type = "text/plain"

            // Construct the body of the message
            val shareBody = "${getString(R.string.app_name)} \n ${
                binding.codeDetailEncodeData.text.toString().trim()
            }"

            // Add the message body to the intent
            putExtra(Intent.EXTRA_TEXT, shareBody)
        }

        // Start the share activity, allowing the user to choose an app to share with
        startActivity(Intent.createChooser(intent, getString(R.string.share_using)))
    }

    // Function to create a PDF file from code detail and optionally share it
    private fun createPdf(isShareAfterCreated: Boolean) {
        // Determine the appropriate bitmap based on the code type
//        val bitmapResId = if (codeHistory!!.codeType == "qr") R.drawable.qrcode else R.drawable.barcode
//        val bitmap = BitmapFactory.decodeResource(resources, bitmapResId)
        val bitmap = ImageManager.getBitmapFromURL(context, codeHistory!!.localImagePath) as Bitmap

        // Define dimensions based on code type
        val (codeWidth, codeHeight) = if (codeHistory!!.codeType == "qr") {
            Pair(200, 200)
        } else {
            Pair(400, 200)
        }

        // Scale the bitmap to the desired dimensions
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, codeWidth, codeHeight, false)

        try {
            // Define the PDF file path and ensure the directory exists
            val pdfDir = File("${applicationContext.getExternalFilesDir("")}/PDF")
            if (!pdfDir.exists()) pdfDir.mkdirs()
            pdfFile = File(pdfDir, "pdf_${codeHistory!!.createdAt}.pdf")

            // Initialize PDF document and output stream
            FileOutputStream(pdfFile).use { fOut ->
                val document = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                // Paint objects for different text and drawing styles
                val titlePaint = Paint().apply {
                    textSize = 50f
                    textAlign = Paint.Align.CENTER
                    color = Color.RED
                }
                val dataPaint = Paint().apply {
                    textAlign = Paint.Align.LEFT
                }
                val typePaint = Paint().apply {
                    textAlign = Paint.Align.RIGHT
                    color = Color.BLUE
                }
                val datePaint = Paint().apply {
                    textSize = 16f
                }

                // Draw app name at the top
                val appName = getString(R.string.app_name)
                val xTitlePos = (canvas.width / 2).toFloat()
                val yTitlePos = 40f
                canvas.drawText(appName, xTitlePos, yTitlePos, titlePaint)

                // Draw the bitmap (QR or Barcode)
                val xBitmapPos = (canvas.width / 2 - scaledBitmap.width / 2).toFloat()
                val yBitmapPos = 50f
                canvas.drawBitmap(scaledBitmap, xBitmapPos, yBitmapPos, Paint())

                // Draw encoded data
                canvas.drawText(
                    binding.codeDetailEncodeData.text.toString(),
                    30f,
                    280f,
                    dataPaint
                )

                // Draw code type
                val codeType = codeHistory!!.codeType.toUpperCase(Locale.ENGLISH)
                canvas.drawText(codeType, canvas.width - 30f, 260f, typePaint)

                // Draw creation date
                canvas.drawText(
                    getFormattedDate(context, codeHistory!!.createdAt.toLong()),
                    30f,
                    300f,
                    datePaint
                )

                // Finish the page and write to output
                document.finishPage(page)
                document.writeTo(fOut)
                document.close()

                // Notify the user
                Toast.makeText(this, getString(R.string.pdf_saved_success_text), Toast.LENGTH_SHORT)
                    .show()

                // Optionally share the PDF file
                if (isShareAfterCreated) {
                    sharePdfFile()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle the error, e.g., notify the user
            Toast.makeText(this, "Failed to create PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            Constants.READ_STORAGE_REQUEST_CODE -> {
                // Check if the permission request was granted
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Create PDF based on whether it should be shared immediately
                    createPdf(isShareAfterCreated)
                    isShareAfterCreated = false // Reset the flag after processing
                } else {
                    // Show an error dialog if the permission was denied
                    MaterialAlertDialogBuilder(context)
                        .setMessage(getString(R.string.external_storage_permission_error))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.ok_text)) { dialog, _ ->
                            dialog.dismiss() // Dismiss the dialog on button click
                        }
                        .create().show()
                }
            }

            else -> {
                // Handle other request codes if needed
            }
        }
    }

    /**
     * Shares a PDF file using an implicit intent.
     * If the file exists, it creates a URI for the file and starts an activity
     * to share it. If the file does not exist, it shows an alert message.
     */
    private fun sharePdfFile() {
        // Check if the PDF file is not null and exists
        pdfFile?.takeIf { it.exists() }?.let { file ->
            // Create URI for the file based on Android version
            val fileUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Use FileProvider for devices running Android Nougat and above
                FileProvider.getUriForFile(
                    context,
                    "${context.applicationContext.packageName}.fileprovider",
                    file
                )
            } else {
                // Use Uri.fromFile for devices below Android Nougat
                Uri.fromFile(file)
            }

            // Create an intent to share the file
            Intent(Intent.ACTION_SEND).apply {
                // Set MIME type for PDF
                type = "application/pdf"
                // Attach the file URI to the intent
                putExtra(Intent.EXTRA_STREAM, fileUri)
                // Grant read permissions to the recipient app
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                // Start the share intent
                startActivity(Intent.createChooser(this, "Share File"))
            }
        } ?: run {
            // If the file is null or does not exist, show an alert
            showAlert(context, getString(R.string.pdf_create_failed_error))
        }
    }

}