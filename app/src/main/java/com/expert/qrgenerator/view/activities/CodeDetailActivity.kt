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
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.expert.qrgenerator.R
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.DialogPrefs
import com.expert.qrgenerator.utils.RuntimePermissionHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLEncoder
import java.util.*


class CodeDetailActivity : BaseActivity(), View.OnClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private var codeHistory: CodeHistory? = null
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
    var bitmap: Bitmap? = null
    private val pageWidth = 500
    private val pageHeight = 500
    private var pdfFile: File? = null
    private var isShareAfterCreated: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code_detail)
        initViews()
        var scans = DialogPrefs.getSuccessScan(context)
        if (scans >= 0) {
            scans += 1
            DialogPrefs.setSuccessScan(context, scans)
        }
        setUpToolbar()
        displayCodeDetails()

    }

    // THIS FUNCTION WILL INITIALIZE ALL THE VIEWS AND REFERENCE OF OBJECTS
    private fun initViews() {
        context = this
        toolbar = findViewById(R.id.toolbar)
        if (intent != null && intent.hasExtra("HISTORY_ITEM")) {
            codeHistory = intent.getSerializableExtra("HISTORY_ITEM") as CodeHistory
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
            dateTimeView.text = getFormattedDate(context, codeHistory!!.createdAt)
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
            else -> {

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
                getFormattedDate(context, codeHistory!!.createdAt),
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