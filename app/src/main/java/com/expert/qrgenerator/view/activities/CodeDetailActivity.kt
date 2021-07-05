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
import com.expert.qrgenerator.R
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.TableObject
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.RuntimePermissionHelper
import com.expert.qrgenerator.viewmodel.DynamicQrViewModel
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
    private lateinit var barcodeDataView: FrameLayout
    private lateinit var barcodeImageView: CardView
    private lateinit var updateDynamicLinkInput: TextInputEditText
    private lateinit var updateDynamicButton: AppCompatButton
    private lateinit var protocolGroup: RadioGroup
    private lateinit var appViewModel: AppViewModel
    private lateinit var viewModel: DynamicQrViewModel
    private lateinit var barcodeDetailParentLayout: LinearLayout
    private lateinit var dialogSubHeading:MaterialTextView
    var bitmap: Bitmap? = null
    private val pageWidth = 500
    private val pageHeight = 500
    private var pdfFile: File? = null
    private var isShareAfterCreated: Boolean = false
    var selectedProtocol = ""


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
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(DynamicQrViewModel()).createFor()
        )[DynamicQrViewModel::class.java]
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
                typeTextHeading.text = "Barcode Text Data"
                typeImageHeading.text = "Barcode Image"
                typeImageView.setImageResource(R.drawable.barcode)
            } else {
                topImageCodeType.setImageResource(R.drawable.ic_qr_code)
                typeTextHeading.text = "QR Text Data"
                typeImageHeading.text = "QR Image"
                typeImageView.setImageResource(R.drawable.qrcode)
            }
            encodeDataTextView.text = codeHistory!!.data
            codeSequenceView.text = "Code ${codeHistory!!.id}"
            dateTimeView.text = getFormattedDate(context, codeHistory!!.createdAt.toLong())

            if (codeHistory!!.isDynamic.toInt() == 1) {
                dynamicLinkUpdateLayout.visibility = View.VISIBLE
                dialogSubHeading.text = "Current link: ${codeHistory!!.data}"
//                if (codeHistory!!.data.contains("http://"))
//                {
//                    protocolGroup.check(R.id.http_protocol_rb)
//                    selectedProtocol = "http://"
//                    updateDynamicLinkInput.setText(codeHistory!!.data.removePrefix("http://"))
//                } else if(codeHistory!!.data.contains("https://")){
//                    selectedProtocol = "https://"
//                    protocolGroup.check(R.id.https_protocol_rb)
//                    updateDynamicLinkInput.setText(codeHistory!!.data.removePrefix("https://"))
//                }
//                else{
//                    updateDynamicLinkInput.setText(codeHistory!!.data)
//                }
            } else {
                dynamicLinkUpdateLayout.visibility = View.GONE

            }

        } else {

            if (tableObject != null) {
                barcodeDetailWrapperLayout.visibility = View.VISIBLE
                encodeDataTextView.text = tableObject!!.code_data
                codeSequenceView.text = "Code ${tableObject!!.id}"
                dateTimeView.text = tableObject!!.date
                displayBarcodeDetail()
            }
        }

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
                Toast.makeText(context, "Text saved in clipboard", Toast.LENGTH_SHORT).show()
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
                        "Please select the URL protocol!"
                    )
                } else if (value.isEmpty()) {

                    showAlert(
                        context,
                        "Please enter the required input data!"
                    )

                } else if (value.contains("http://") || value.contains("https://")
                ) {
                    showAlert(
                        context,
                        "Please enter the URL without http:// or https://"
                    )
                }
                else if (!Pattern.compile("^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?\$").matcher(value).find()) {
                    showAlert(
                        context,
                        "Please enter the valid website URL"
                    )
                }
                else {
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
                            dialogSubHeading.text = "Current link: $selectedProtocol$value"
                            encodeDataTextView.text = "$selectedProtocol$value"
                            appViewModel.update("$selectedProtocol$value", url, codeHistory!!.id)
                            showAlert(context, "Dynamic Url update Successfully!")
                        } else {
                            showAlert(context, "Something went wrong, please try again!")
                        }
                    })
                }
            }
            else -> {

            }
        }
    }

    private fun displayBarcodeDetail() {
        if (tableObject != null) {
            val codeDataLayout = LayoutInflater.from(context)
                .inflate(R.layout.barcode_detail_item_row, barcodeDetailParentLayout, false)
            val codeDataColumnValue =
                codeDataLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_value)
            val codeDataColumnName =
                codeDataLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_name)
            val codeDataColumnEditView =
                codeDataLayout.findViewById<AppCompatImageView>(R.id.bcd_edit_view)

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
                columnValue.text = item.second
                columnName.text = item.first
                barcodeDetailParentLayout.addView(layout)
            }

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
            Toast.makeText(this, "Pdf created and saved successfully!", Toast.LENGTH_SHORT).show()
            if (isShareAfterCreated) {
                sharePdfFile()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Something wrong: $e", Toast.LENGTH_SHORT).show()
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
                        .setMessage("Please allow the READ/WRITE EXTERNAL STORAGE permission for use create and save the pdf file.")
                        .setCancelable(false)
                        .setPositiveButton("Ok") { dialog, which ->
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
                showAlert(context, "This Pdf file does not exist or created!")
            }
        }
    }
}