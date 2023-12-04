package com.expert.qrgenerator.ui.activities

import android.app.Activity
import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.FeedbackAdapter
import com.expert.qrgenerator.databinding.ActivityCodeDetailBinding
import com.expert.qrgenerator.databinding.BarcodeDetailItemRowBinding
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.Feedback
import com.expert.qrgenerator.model.TableObject
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.RuntimePermissionHelper
import com.expert.qrgenerator.utils.TableGenerator
import com.expert.qrgenerator.viewmodel.CodeDetailViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLEncoder
import java.util.*
import java.util.regex.Pattern

@AndroidEntryPoint
class CodeDetailActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityCodeDetailBinding
    private lateinit var context: Context
    private var codeHistory: CodeHistory? = null
    private var tableObject: TableObject? = null
    private lateinit var tableGenerator: TableGenerator

    private val appViewModel: AppViewModel by viewModels()
    private lateinit var tableName: String
    private val viewModel: CodeDetailViewModel by viewModels()
    var bitmap: Bitmap? = null
    private val pageWidth = 500
    private val pageHeight = 500
    private var pdfFile: File? = null
    private var isShareAfterCreated: Boolean = false
    var selectedProtocol = ""
    var barcodeEditList = mutableListOf<Triple<AppCompatImageView, String, String>>()
    private var counter: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCodeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        setUpToolbar()
        displayCodeDetails()

    }

    // THIS FUNCTION WILL INITIALIZE ALL THE VIEWS AND REFERENCE OF OBJECTS
    private fun initViews() {
        context = this
        tableGenerator = TableGenerator(context)

        if (intent != null && intent.hasExtra("HISTORY_ITEM")) {
            codeHistory = intent.getSerializableExtra("HISTORY_ITEM") as CodeHistory
        }

        if (intent != null && intent.hasExtra("TABLE_ITEM")) {
            tableObject = intent.getSerializableExtra("TABLE_ITEM") as TableObject
        }
        if (intent != null && intent.hasExtra("TABLE_NAME")) {
            tableName = intent.getStringExtra("TABLE_NAME") as String
        }

        binding.codeDetailClipboardCopyView.setOnClickListener(this)

        binding.codeDetailTextSearchButton.setOnClickListener(this)

        binding.codeDetailTextShareButton.setOnClickListener(this)

        binding.codeDetailPdfSaveButton.setOnClickListener(this)

        binding.codeDetailPdfShareButton.setOnClickListener(this)

        binding.dynamicLinkUpdateBtn.setOnClickListener(this)

        binding.httpProtocolGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.http_protocol_rb -> {
                    selectedProtocol = "http://"
                }
                R.id.https_protocol_rb -> {
                    selectedProtocol = "https://"
                }
                else -> {

                }
            }
        }
        binding.updateNotesBtn.setOnClickListener(this)
    }

    // THIS FUNCTION WILL RENDER THE ACTION BAR/TOOLBAR
    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = getString(R.string.code_detail_text)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    // THIS FUNCTION WILL BIND THE HISTORY CODE DETAIL
    private fun displayCodeDetails() {
        if (codeHistory != null) {
            binding.codeDetailNotes.visibility = View.VISIBLE
            if (codeHistory!!.codeType == "barcode") {
                binding.codeDetailTopImageType.setImageResource(R.drawable.barcode)
                binding.codeDetailTypeTextHeading.text = getString(R.string.barcode_text_data_heding)
                binding.codeDetailTypeImageHeading.text = getString(R.string.barcode_image_heading)
                binding.codeDetailImageType.setImageResource(R.drawable.barcode)
            } else {
                binding.codeDetailTopImageType.setImageResource(R.drawable.ic_qr_code)
                binding.codeDetailTypeTextHeading.text = getString(R.string.qr_text_data_heading)
                binding.codeDetailTypeImageHeading.text = getString(R.string.qr_image_heading)
                binding.codeDetailImageType.setImageResource(R.drawable.qrcode)
            }
            binding.codeDetailEncodeData.text = codeHistory!!.data
            binding.codeDetailCodeSequenceView.text = "${getString(R.string.code_text)} ${codeHistory!!.id}"
            binding.codeDetailDateTimeView.text = getFormattedDate(context, codeHistory!!.createdAt.toLong())
            if (codeHistory!!.notes.isEmpty()) {
                binding.qrCodeHistoryNotesInputField.hint = getString(R.string.notes)
            } else {
                binding.qrCodeHistoryNotesInputField.setText(codeHistory!!.notes)

            }

            if (codeHistory!!.type == "feedback") {
                displayFeedbacksDetail(codeHistory!!.qrId)
            } else {

                if (codeHistory!!.isDynamic.toInt() == 1) {
                    binding.dynamicLinkUpdateBtn.visibility = View.VISIBLE
                    binding.dialogSubHeading.text =
                        "${getString(R.string.current_link_text)} ${codeHistory!!.data}"
                } else {
                    binding.codeDetailDynamicLinkUpdateLayout.visibility = View.GONE

                }

            }

        } else {
            if (tableObject != null) {
                binding.codeDetailNotes.visibility = View.GONE
                displayBarcodeDetail()
            }
        }

    }

    var feedbacksList = mutableListOf<Feedback>()
    lateinit var feedbackAdapter: FeedbackAdapter
    private fun displayFeedbacksDetail(qrId: String) {

        binding.codeDetailFeedbackRecyclerview.layoutManager = LinearLayoutManager(context)
        binding.codeDetailFeedbackRecyclerview.hasFixedSize()
        feedbackAdapter = FeedbackAdapter(context, feedbacksList as ArrayList<Feedback>)
        binding.codeDetailFeedbackRecyclerview.adapter = feedbackAdapter
        binding.codeDetailFeedbackCsvExportImage.setOnClickListener {
            exportCsv()
        }
        startLoading(context)
        viewModel.callFeedbacks(qrId)
        viewModel.getAllFeedbacks().observe(this, { response ->
            dismiss()
            if (response != null) {
                feedbacksList.addAll(response.feedbacks)
                if (feedbacksList.size > 0) {
                    binding.codeDetailFeedbackLayout.visibility = View.VISIBLE
                    feedbackAdapter.notifyItemRangeChanged(0, feedbacksList.size)
                    feedbackAdapter.setOnItemClickListener(object :
                        FeedbackAdapter.OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            val item = feedbacksList[position]
                            val sharingText =
                                "Feedback: ${item.comment}\nEmail: ${item.email}\nPhone: ${item.phone}\nStars: ${item.rating}\n ${
                                    getString(
                                        R.string.qr_sign
                                    )
                                }"
                            MaterialAlertDialogBuilder(context)
                                .setMessage(sharingText)
                                .setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                                    dialog.dismiss()
                                }
                                .setPositiveButton(getString(R.string.share_text)) { dialog, which ->
                                    dialog.dismiss()
                                    shareFeedback(sharingText)
                                }
                                .create().show()
                        }

                    })
                } else {
                    binding.codeDetailFeedbackLayout.visibility = View.GONE
                }
            }
        })

    }

    private fun exportCsv() {
        if (feedbacksList.isNotEmpty()) {
            startLoading(context)
            val builder = StringBuilder()
            builder.append("id,qrId,comment,email,phone,rating")

            for (j in 0 until feedbacksList.size) {

                val data = feedbacksList[j]

                builder.append("\n${data.id},${data.qrId},${data.comment},${data.email},${data.phone},${data.rating}")
            }

            try {
                val fileName = "feedbacks_${feedbacksList[0].qrId}.csv"
                val out = openFileOutput(fileName, Context.MODE_PRIVATE)
                out.write((builder.toString()).toByteArray())
                out.close()

                val file = File(filesDir, fileName)
                val path =
                    FileProvider.getUriForFile(context, "com.expert.qrgenerator.fileprovider", file)
                dismiss()
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/csv"
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.putExtra(Intent.EXTRA_STREAM, path)
                startActivity(Intent.createChooser(intent, getString(R.string.share_using)))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            showAlert(context, getString(R.string.table_export_error_text))
        }
    }

    private fun shareFeedback(sharingText: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, sharingText)
        startActivity(Intent.createChooser(intent, getString(R.string.share_using)))
    }

    // THIS FUNCTION WILL HANDLE THE ON BACK ARROW CLICK EVENT
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
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
                if (RuntimePermissionHelper.checkStoragePermission(
                        context,
                        Constants.READ_STORAGE_PERMISSION
                    )
                ) {
                    createPdf(false)
                }
            }
            R.id.code_detail_pdf_share_button -> {
                isShareAfterCreated = true
                if (RuntimePermissionHelper.checkStoragePermission(
                        context,
                        Constants.READ_STORAGE_PERMISSION
                    )
                ) {
                    if (pdfFile == null) {
                        createPdf(true)
                    } else {
                        sharePdfFile()
                    }
                }

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
                    viewModel.createDynamicQrCode(hashMap)
                    viewModel.getDynamicQrCode().observe(this, { response ->
                        var url = ""
                        dismiss()
                        if (response != null) {
                            url = response.get("generatedUrl").asString
                            url = if (url.contains(":8990")) {
                                url.replace(":8990", "")
                            } else {
                                url
                            }
                            binding.dialogSubHeading.text =
                                "${getString(R.string.current_link_text)} $selectedProtocol$value"
                            binding.codeDetailEncodeData.text = "$selectedProtocol$value"
                            appViewModel.update("$selectedProtocol$value", url, codeHistory!!.id)
                            showAlert(context, getString(R.string.dynamic_update_success_text))
                        } else {
                            showAlert(context, getString(R.string.something_wrong_error))
                        }
                    })
                }
            }
            R.id.update_notes_btn -> {
                val notesText = binding.qrCodeHistoryNotesInputField.text.toString().trim()
                if (notesText.isNotEmpty()) {
                    codeHistory!!.notes = notesText
                    appViewModel.updateHistory(codeHistory!!)

                    Toast.makeText(
                        context,
                        getString(R.string.notes_update_success_text),
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.qrCodeHistoryNotesInputField.clearFocus()
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

    private fun updateBarcodeDetail(id: Int, triple: Triple<AppCompatImageView, String, String>) {
        val updateBarcodeLayout =
            LayoutInflater.from(context).inflate(R.layout.update_barcode_detail_dialog, null)
        val updateInputBox =
            updateBarcodeLayout.findViewById<TextInputEditText>(R.id.update_barcode_detail_text_input_field)

        val cleanBrushView =
            updateBarcodeLayout.findViewById<AppCompatImageView>(R.id.update_barcode_detail_cleaning_text_view)
        val cancelBtn =
            updateBarcodeLayout.findViewById<MaterialButton>(R.id.update_barcode_detail_dialog_cancel_btn)
        val updateBtn =
            updateBarcodeLayout.findViewById<MaterialButton>(R.id.update_barcode_detail_dialog_update_btn)

        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(updateBarcodeLayout)
        builder.setCancelable(false)
        val alert = builder.create()
        alert.show()
        cancelBtn.setOnClickListener {
            hideSoftKeyboard(context, cancelBtn)
            alert.dismiss()
        }

        cleanBrushView.setOnClickListener { updateInputBox.setText("") }

        updateBtn.setOnClickListener {

            val value = updateInputBox.text.toString().trim()
            if (value.isNotEmpty()) {
                hideSoftKeyboard(context, updateBtn)
                alert.dismiss()
                val isUpdate = tableGenerator.updateBarcodeDetail(
                    tableName,
                    triple.third,
                    value,
                    id
                )
                if (isUpdate) {
                    tableObject = tableGenerator.getUpdateBarcodeDetail(tableName, id)
                    displayBarcodeDetail()
                }
            } else {
                Toast.makeText(context, getString(R.string.empty_text_error), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        updateInputBox.setText(triple.second)
        updateInputBox.setSelection(updateInputBox.text!!.length)
        updateInputBox.requestFocus()
        Constants.openKeyboar(context)

    }

    private fun displayBarcodeDetail() {
        if (tableObject != null) {

            binding.barcodeDetailWrapperLayout.visibility = View.VISIBLE
            binding.codeDetailEncodeData.text = tableObject!!.code_data
            binding.codeDetailCodeSequenceView.text = "${getString(R.string.code_text)} ${tableObject!!.id}"
            binding.codeDetailDateTimeView.text = tableObject!!.date

            if (binding.barcodeDetailWrapperLayout.childCount > 0) {
                binding.barcodeDetailWrapperLayout.removeAllViews()
            }

            barcodeEditList.add(
                Triple(
                    AppCompatImageView(context),
                    tableObject!!.id.toString(),
                    "id"
                )
            )
            var barcodeDetailItemRowBinding = BarcodeDetailItemRowBinding.inflate(LayoutInflater.from(context),binding.barcodeDetailWrapperLayout as ViewGroup, false)

            barcodeDetailItemRowBinding.bcdEditView.id = counter
            barcodeEditList.add(
                Triple(
                    barcodeDetailItemRowBinding.bcdEditView,
                    tableObject!!.code_data,
                    "code_data"
                )
            )
            barcodeDetailItemRowBinding.bcdEditView.setOnClickListener(this)
            barcodeDetailItemRowBinding.bcdTableColumnValue.text = tableObject!!.code_data
            barcodeDetailItemRowBinding.bcdTableColumnName.text = "code_data"
            binding.barcodeDetailWrapperLayout.addView(barcodeDetailItemRowBinding.root)
            barcodeDetailItemRowBinding = BarcodeDetailItemRowBinding.inflate(LayoutInflater.from(context),binding.barcodeDetailWrapperLayout as ViewGroup, false)

            counter += 1
            barcodeDetailItemRowBinding.bcdEditView.id = counter
            barcodeEditList.add(Triple(barcodeDetailItemRowBinding.bcdEditView, tableObject!!.date, "date"))
            barcodeDetailItemRowBinding.bcdEditView.setOnClickListener(this)
            barcodeDetailItemRowBinding.bcdTableColumnValue.text = tableObject!!.date
            barcodeDetailItemRowBinding.bcdTableColumnName.text = "date"
            binding.barcodeDetailWrapperLayout.addView(barcodeDetailItemRowBinding.root)
            barcodeDetailItemRowBinding = BarcodeDetailItemRowBinding.inflate(LayoutInflater.from(context),binding.barcodeDetailWrapperLayout as ViewGroup, false)


            counter += 1
            barcodeDetailItemRowBinding.bcdEditView.id = counter
            barcodeEditList.add(Triple(barcodeDetailItemRowBinding.bcdEditView, tableObject!!.image, "image"))
            barcodeDetailItemRowBinding.bcdEditView.setOnClickListener(this)
            barcodeDetailItemRowBinding.bcdTableColumnValue.text = tableObject!!.image
            barcodeDetailItemRowBinding.bcdTableColumnName.text = "image"
            binding.barcodeDetailWrapperLayout.addView(barcodeDetailItemRowBinding.root)

            for (i in 0 until tableObject!!.dynamicColumns.size) {
                val item = tableObject!!.dynamicColumns[i]
               val barcodeDetailItemRowBinding = BarcodeDetailItemRowBinding.inflate(LayoutInflater.from(context),binding.barcodeDetailWrapperLayout as ViewGroup, false)


                counter += 1
                barcodeDetailItemRowBinding.bcdEditView.id = counter
                barcodeEditList.add(Triple(barcodeDetailItemRowBinding.bcdEditView, item.second, item.first))
                barcodeDetailItemRowBinding.bcdEditView.setOnClickListener(this)
                barcodeDetailItemRowBinding.bcdTableColumnValue.text = item.second
                barcodeDetailItemRowBinding.bcdTableColumnName.text = item.first
                binding.barcodeDetailWrapperLayout.addView(barcodeDetailItemRowBinding.root)

            }
            counter = 0
        }
    }

    // THIS FUNCTION WILL SHARE THE CODE TEXT TO OTHERS
    private fun textShare() {
        val intent = Intent(Intent.ACTION_SEND)
        val shareBody =
            "${getString(R.string.app_name)} \n ${binding.codeDetailEncodeData.text.toString().trim()}"
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(intent, getString(R.string.share_using)))
    }

    // THIS FUNCTION WILL CREATE THE PDF FILE FROM CODE DETAIL
    private fun createPdf(isShareAfterCreated: Boolean) {
        bitmap = if (codeHistory!!.codeType == "qr") {
            BitmapFactory.decodeResource(resources, R.drawable.qrcode)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.barcode)
        }
        var codeWidth: Int? = null
        var codeHeight: Int? = null
        if (codeHistory!!.codeType == "qr") {
            codeWidth = 200
            codeHeight = 200
        } else {
            codeWidth = 400
            codeHeight = 200
        }
        bitmap = Bitmap.createScaledBitmap(bitmap!!, codeWidth, codeHeight, false)


        try {
            val path = "${applicationContext.getExternalFilesDir("")}/PDF"
            val dir = File(path)
            if (!dir.exists()) dir.mkdirs()
            val fileName = "pdf_${codeHistory!!.createdAt}.pdf"
            val file = File(dir, fileName)
            pdfFile = file
            val fOut = FileOutputStream(file)

            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()
            val titlePaint = Paint()
            val dataPaint = Paint()
            titlePaint.textSize = 50.toFloat()
            titlePaint.textAlign = Paint.Align.CENTER
            titlePaint.color = Color.RED
            val appName = getString(R.string.app_name)
            val xPos = (canvas.width / 2) - (appName.length) / 2
            val yPos =
                (canvas.height / 2 - (titlePaint.descent() + titlePaint.ascent()) / 2).toInt()
            canvas.drawText(appName, xPos.toFloat(), 40.toFloat(), titlePaint)
            paint.textAlign = Paint.Align.CENTER
            val xCodePos = (canvas.width / 2) - (bitmap!!.width) / 2
            canvas.drawBitmap(bitmap!!, xCodePos.toFloat(), 50.toFloat(), paint)
            canvas.drawText(
                binding.codeDetailEncodeData.text.toString(),
                30.toFloat(),
                280.toFloat(),
                dataPaint
            )
            val typePaint = Paint()
            typePaint.textAlign = Paint.Align.RIGHT
            typePaint.color = Color.BLUE
            typePaint.textAlign = Paint.Align.RIGHT
            val codeType = codeHistory!!.codeType.toUpperCase(Locale.ENGLISH)
            canvas.drawText(codeType, canvas.width / 2.toFloat(), 260.toFloat(), typePaint)
            val datePaint = Paint()
            datePaint.textSize = 16.toFloat()
            canvas.drawText(
                getFormattedDate(context, codeHistory!!.createdAt.toLong()),
                30.toFloat(),
                300.toFloat(),
                datePaint
            )

            document.finishPage(page)
            document.writeTo(fOut)
            document.close()
            Toast.makeText(this, getString(R.string.pdf_saved_success_text), Toast.LENGTH_SHORT)
                .show()
            if (isShareAfterCreated) {
                sharePdfFile()
            }
        } catch (e: IOException) {
            e.printStackTrace()
//            Toast.makeText(this, "Something wrong: $e", Toast.LENGTH_SHORT).show()
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
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (isShareAfterCreated) {
                        createPdf(true)
                        isShareAfterCreated = false
                    } else {
                        createPdf(false)
                    }
                } else {
                    MaterialAlertDialogBuilder(context)
                        .setMessage(getString(R.string.external_storage_permission_error))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.ok_text)) { dialog, which ->
                            dialog.dismiss()
                        }
                        .create().show()
                }
            }
            else -> {

            }
        }
    }


    // FUNCTION WILL SHARE THE PDF FILE
    private fun sharePdfFile() {
        if (pdfFile != null) {
            if (pdfFile!!.exists()) {
                val fileUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    FileProvider.getUriForFile(
                        context,
                        context.applicationContext.packageName + ".fileprovider", pdfFile!!
                    )

                } else {
                    Uri.fromFile(pdfFile)
                }
                val fileShareIntent = Intent(Intent.ACTION_SEND)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    fileShareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (fileUri != null) {

                    fileShareIntent.type = "application/pdf"
                    fileShareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
                    startActivity(Intent.createChooser(fileShareIntent, "Share File"))
                }
            } else {
                showAlert(context, getString(R.string.pdf_create_failed_error))
            }
        }
    }
}