package com.expert.qrgenerator.view.activities

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
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.FeedbackAdapter
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.Feedback
import com.expert.qrgenerator.model.TableObject
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.DialogPrefs
import com.expert.qrgenerator.utils.RuntimePermissionHelper
import com.expert.qrgenerator.utils.TableGenerator
import com.expert.qrgenerator.viewmodel.CodeDetailViewModel
import com.expert.qrgenerator.viewmodelfactory.ViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLEncoder
import java.util.*
import java.util.regex.Pattern


class CodeDetailActivity : BaseActivity(), View.OnClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private var codeHistory: CodeHistory? = null
    private var tableObject: TableObject? = null
    private lateinit var tableGenerator: TableGenerator
    private lateinit var topImageCodeType: AppCompatImageView
    private lateinit var typeTextHeading: MaterialTextView
    private lateinit var encodeDataTextView: MaterialTextView
    private lateinit var clipboardCopyView: MaterialTextView
    private lateinit var textSearchButton: AppCompatImageButton
    private lateinit var textShareButton: AppCompatImageButton
    private lateinit var typeImageHeading: MaterialTextView
    private lateinit var typeImageView: AppCompatImageView
    private lateinit var pdfSaveButton: AppCompatImageButton
    private lateinit var pdfShareButton: AppCompatImageButton
    private lateinit var codeSequenceView: MaterialTextView
    private lateinit var dateTimeView: MaterialTextView
    private lateinit var dynamicLinkUpdateLayout: CardView
    private lateinit var barcodeDetailWrapperLayout: CardView
    private lateinit var feedbackDetailWrapperLayout: CardView
    private lateinit var feedbackRecyclerView: RecyclerView
    private lateinit var barcodeDataView: FrameLayout
    private lateinit var barcodeImageView: CardView
    private lateinit var updateDynamicLinkInput: TextInputEditText
    private lateinit var updateDynamicButton: AppCompatButton
    private lateinit var protocolGroup: RadioGroup
    private lateinit var appViewModel: AppViewModel
    private lateinit var feedbackCsvExportImageView: AppCompatImageView

    //    private lateinit var viewModel: DynamicQrViewModel
    private lateinit var barcodeDetailParentLayout: LinearLayout
    private lateinit var dialogSubHeading: MaterialTextView
    private lateinit var tableName: String
    private lateinit var viewModel: CodeDetailViewModel
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
        setContentView(R.layout.activity_code_detail)


        initViews()
        setUpToolbar()
        displayCodeDetails()

    }

    // THIS FUNCTION WILL INITIALIZE ALL THE VIEWS AND REFERENCE OF OBJECTS
    private fun initViews() {
        context = this
        tableGenerator = TableGenerator(context)
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(CodeDetailViewModel()).createFor()
        )[CodeDetailViewModel::class.java]
        appViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(this.application)
        ).get(AppViewModel::class.java)
        toolbar = findViewById(R.id.toolbar)
        if (intent != null && intent.hasExtra("HISTORY_ITEM")) {
            codeHistory = intent.getSerializableExtra("HISTORY_ITEM") as CodeHistory
        }

        if (intent != null && intent.hasExtra("TABLE_ITEM")) {
            tableObject = intent.getSerializableExtra("TABLE_ITEM") as TableObject
        }
        if (intent != null && intent.hasExtra("TABLE_NAME")) {
            tableName = intent.getStringExtra("TABLE_NAME") as String
        }

        var scans = DialogPrefs.getSuccessScan(context)
        if (scans >= 0) {
            scans += 1
            DialogPrefs.setSuccessScan(context, scans)
        }

        topImageCodeType = findViewById(R.id.code_detail_top_image_type)
        typeTextHeading = findViewById(R.id.code_detail_type_text_heading)
        encodeDataTextView = findViewById(R.id.code_detail_encode_data)
        clipboardCopyView = findViewById(R.id.code_detail_clipboard_copy_view)
        clipboardCopyView.setOnClickListener(this)
        textSearchButton = findViewById(R.id.code_detail_text_search_button)
        textSearchButton.setOnClickListener(this)
        textShareButton = findViewById(R.id.code_detail_text_share_button)
        textShareButton.setOnClickListener(this)
        typeImageHeading = findViewById(R.id.code_detail_type_image_heading)
        typeImageView = findViewById(R.id.code_detail_image_type)
        pdfSaveButton = findViewById(R.id.code_detail_pdf_save_button)
        pdfSaveButton.setOnClickListener(this)
        pdfShareButton = findViewById(R.id.code_detail_pdf_share_button)
        pdfShareButton.setOnClickListener(this)
        codeSequenceView = findViewById(R.id.code_detail_code_sequence_view)
        dateTimeView = findViewById(R.id.code_detail_date_time_view)
        dynamicLinkUpdateLayout = findViewById(R.id.code_detail_dynamic_link_update_layout)
        barcodeDataView = findViewById(R.id.code_detail_top_framelayout)
        barcodeImageView = findViewById(R.id.barcode_image_detail_layout)
        barcodeDetailWrapperLayout = findViewById(R.id.code_detail_table_layout)
        feedbackDetailWrapperLayout = findViewById(R.id.code_detail_feedback_layout)
        feedbackRecyclerView = findViewById(R.id.code_detail_feedback_recyclerview)
        feedbackCsvExportImageView = findViewById(R.id.code_detail_feedback_csv_export_image)
        updateDynamicLinkInput = findViewById(R.id.qr_code_history_dynamic_link_input_field)
        updateDynamicButton = findViewById(R.id.dynamic_link_update_btn)
        updateDynamicButton.setOnClickListener(this)
        protocolGroup = findViewById(R.id.http_protocol_group)
        barcodeDetailParentLayout = findViewById(R.id.barcode_detail_wrapper_layout)
        dialogSubHeading = findViewById(R.id.dialog_sub_heading)
        protocolGroup.setOnCheckedChangeListener { group, checkedId ->
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
    }

    // THIS FUNCTION WILL RENDER THE ACTION BAR/TOOLBAR
    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.code_detail_text)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    // THIS FUNCTION WILL BIND THE HISTORY CODE DETAIL
    private fun displayCodeDetails() {
        if (codeHistory != null) {

            if (codeHistory!!.codeType == "barcode") {
                topImageCodeType.setImageResource(R.drawable.barcode)
                typeTextHeading.text = getString(R.string.barcode_text_data_heding)
                typeImageHeading.text = getString(R.string.barcode_image_heading)
                typeImageView.setImageResource(R.drawable.barcode)
            } else {
                topImageCodeType.setImageResource(R.drawable.ic_qr_code)
                typeTextHeading.text = getString(R.string.qr_text_data_heading)
                typeImageHeading.text = getString(R.string.qr_image_heading)
                typeImageView.setImageResource(R.drawable.qrcode)
            }
            encodeDataTextView.text = codeHistory!!.data
            codeSequenceView.text = "${getString(R.string.code_text)} ${codeHistory!!.id}"
            dateTimeView.text = getFormattedDate(context, codeHistory!!.createdAt.toLong())

            if (codeHistory!!.type == "feedback") {
                displayFeedbacksDetail(codeHistory!!.qrId)
            } else {

                if (codeHistory!!.isDynamic.toInt() == 1) {
                    dynamicLinkUpdateLayout.visibility = View.VISIBLE
                    dialogSubHeading.text =
                        "${getString(R.string.current_link_text)} ${codeHistory!!.data}"
                } else {
                    dynamicLinkUpdateLayout.visibility = View.GONE

                }

            }

        } else {

            if (tableObject != null) {
                displayBarcodeDetail()
            }
        }

    }

    var feedbacksList = mutableListOf<Feedback>()
    lateinit var feedbackAdapter: FeedbackAdapter
    private fun displayFeedbacksDetail(qrId: String) {

        feedbackRecyclerView.layoutManager = LinearLayoutManager(context)
        feedbackRecyclerView.hasFixedSize()
        feedbackAdapter = FeedbackAdapter(context, feedbacksList as ArrayList<Feedback>)
        feedbackRecyclerView.adapter = feedbackAdapter
        feedbackCsvExportImageView.setOnClickListener {
            exportCsv()
        }
        startLoading(context)
        viewModel.callFeedbacks(context, qrId)
        viewModel.getAllFeedbacks().observe(this, { response ->
            dismiss()
            if (response != null) {
                feedbacksList.addAll(response.feedbacks)
                if (feedbacksList.size > 0) {
                    feedbackDetailWrapperLayout.visibility = View.VISIBLE
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
                    feedbackDetailWrapperLayout.visibility = View.GONE
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
                    encodeDataTextView.text.toString()
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
                    encodeDataTextView.text.toString().trim(), "UTF-8"
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
                val value = updateDynamicLinkInput.text.toString().trim()
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
                    viewModel.createDynamicQrCode(context, hashMap)
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
                            dialogSubHeading.text =
                                "${getString(R.string.current_link_text)} $selectedProtocol$value"
                            encodeDataTextView.text = "$selectedProtocol$value"
                            appViewModel.update("$selectedProtocol$value", url, codeHistory!!.id)
                            showAlert(context, getString(R.string.dynamic_update_success_text))
                        } else {
                            showAlert(context, getString(R.string.something_wrong_error))
                        }
                    })
                }
            }
            else -> {
                val position = v.id
                val id = barcodeEditList[0].second.toInt()
                val triple = barcodeEditList[position + 1]
                //Toast.makeText(context, triple.second,Toast.LENGTH_SHORT).show()
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

            barcodeDetailWrapperLayout.visibility = View.VISIBLE
            encodeDataTextView.text = tableObject!!.code_data
            codeSequenceView.text = "${getString(R.string.code_text)} ${tableObject!!.id}"
            dateTimeView.text = tableObject!!.date

            if (barcodeDetailParentLayout.childCount > 0) {
                barcodeDetailParentLayout.removeAllViews()
            }

            barcodeEditList.add(
                Triple(
                    AppCompatImageView(context),
                    tableObject!!.id.toString(),
                    "id"
                )
            )
            val codeDataLayout = LayoutInflater.from(context)
                .inflate(R.layout.barcode_detail_item_row, barcodeDetailParentLayout, false)
            val codeDataColumnValue =
                codeDataLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_value)
            val codeDataColumnName =
                codeDataLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_name)
            val codeDataColumnEditView =
                codeDataLayout.findViewById<AppCompatImageView>(R.id.bcd_edit_view)
            codeDataColumnEditView.id = counter
            barcodeEditList.add(
                Triple(
                    codeDataColumnEditView,
                    tableObject!!.code_data,
                    "code_data"
                )
            )
            codeDataColumnEditView.setOnClickListener(this)
            codeDataColumnValue.text = tableObject!!.code_data
            codeDataColumnName.text = "code_data"
            barcodeDetailParentLayout.addView(codeDataLayout)
            val dateLayout = LayoutInflater.from(context)
                .inflate(R.layout.barcode_detail_item_row, barcodeDetailParentLayout, false)
            val dateColumnValue =
                dateLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_value)
            val dateColumnName =
                dateLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_name)
            val dateColumnEditView = dateLayout.findViewById<AppCompatImageView>(R.id.bcd_edit_view)
            counter += 1
            dateColumnEditView.id = counter
            barcodeEditList.add(Triple(dateColumnEditView, tableObject!!.date, "date"))
            dateColumnEditView.setOnClickListener(this)
            dateColumnValue.text = tableObject!!.date
            dateColumnName.text = "date"
            barcodeDetailParentLayout.addView(dateLayout)
            val imageLayout = LayoutInflater.from(context)
                .inflate(R.layout.barcode_detail_item_row, barcodeDetailParentLayout, false)
            val imageColumnValue =
                imageLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_value)
            val imageColumnName =
                imageLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_name)
            val imageColumnEditView =
                imageLayout.findViewById<AppCompatImageView>(R.id.bcd_edit_view)
            counter += 1
            imageColumnEditView.id = counter
            barcodeEditList.add(Triple(imageColumnEditView, tableObject!!.image, "image"))
            imageColumnEditView.setOnClickListener(this)
            imageColumnValue.text = tableObject!!.image
            imageColumnName.text = "image"
            barcodeDetailParentLayout.addView(imageLayout)

            for (i in 0 until tableObject!!.dynamicColumns.size) {
                val item = tableObject!!.dynamicColumns[i]
                val layout = LayoutInflater.from(context)
                    .inflate(R.layout.barcode_detail_item_row, barcodeDetailParentLayout, false)
                val columnValue = layout.findViewById<MaterialTextView>(R.id.bcd_table_column_value)
                val columnName = layout.findViewById<MaterialTextView>(R.id.bcd_table_column_name)
                val columnEditView = layout.findViewById<AppCompatImageView>(R.id.bcd_edit_view)
                counter += 1
                columnEditView.id = counter
                barcodeEditList.add(Triple(columnEditView, item.second, item.first))
                columnEditView.setOnClickListener(this)
                columnValue.text = item.second
                columnName.text = item.first
                barcodeDetailParentLayout.addView(layout)
            }
            counter = 0
        }
    }

    // THIS FUNCTION WILL SHARE THE CODE TEXT TO OTHERS
    private fun textShare() {
        val intent = Intent(Intent.ACTION_SEND)
        val shareBody =
            "${getString(R.string.app_name)} \n ${encodeDataTextView.text.toString().trim()}"
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
                encodeDataTextView.text.toString(),
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