package com.expert.qrgenerator.view.fragments

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.budiyev.android.codescanner.*
import com.expert.qrgenerator.R
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.utils.*
import com.expert.qrgenerator.view.activities.BaseActivity
import com.expert.qrgenerator.view.activities.CodeDetailActivity
import com.expert.qrgenerator.view.activities.TablesActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class ScannerFragment : Fragment() {

    private var filePathView: MaterialTextView? = null
    var currentPhotoPath: String? = null
    private var codeScanner: CodeScanner? = null
    private lateinit var scannerView: CodeScannerView
    private lateinit var appViewModel: AppViewModel
    private lateinit var tableGenerator: TableGenerator
    private var tableName: String = ""
    private lateinit var tablesSpinner: AppCompatSpinner
    private var textInputIdsList = mutableListOf<Pair<String, TextInputEditText>>()
    private var spinnerIdsList = mutableListOf<Pair<String, AppCompatSpinner>>()
    private lateinit var addNewTableBtn: MaterialButton
    private lateinit var appSettings: AppSettings
    private var mService: Drive? = null
    private var imageDrivePath = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appSettings = AppSettings(requireActivity())
        appViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(AppViewModel::class.java)
        tableGenerator = TableGenerator(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_scanner, container, false)

        initViews(v)

        return v
    }


    private fun initViews(view: View) {
        scannerView = view.findViewById(R.id.scanner_view)
        tablesSpinner = view.findViewById(R.id.tables_spinner)
        addNewTableBtn = view.findViewById(R.id.add_new_table_btn)
        addNewTableBtn.setOnClickListener {
            startActivity(Intent(requireActivity(), TablesActivity::class.java))
        }
    }

    private fun getTableList() {
        val tablesList = mutableListOf<String>()
        tablesList.addAll(tableGenerator.getAllDatabaseTables())
        if (tablesList.isNotEmpty()) {
            tableName = tablesList[0]
            val adapter =
                ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, tablesList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            tablesSpinner.adapter = adapter

            if (appSettings.getString("SCAN_SELECTED_TABLE")!!.isNotEmpty()) {
                for (i in 0 until tablesList.size) {
                    val value = tablesList[i]
                    if (value == appSettings.getString("SCAN_SELECTED_TABLE")) {
                        tablesSpinner.setSelection(i)
                        tableName = value
                        break
                    }
                }
            }
        }

        tablesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }

            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                tableName = adapterView!!.getItemAtPosition(i).toString()
                appSettings.putString("SCAN_SELECTED_TABLE", tableName)
            }
        }
    }

    private fun startScanner() {
        if (RuntimePermissionHelper.checkCameraPermission(
                requireActivity(),
                Constants.CAMERA_PERMISSION
            )
        ) {

            if (codeScanner == null) {
                codeScanner = CodeScanner(requireActivity(), scannerView)
            }

            // Parameters (default values)
            codeScanner!!.apply {
                camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
                formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
                // ex. listOf(BarcodeFormat.QR_CODE)
                autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
                scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
                isAutoFocusEnabled = true // Whether to enable auto focus or not
                isFlashEnabled = false // Whether to enable flash or not

                // Callbacks
                decodeCallback = DecodeCallback {
                    requireActivity().runOnUiThread {
                        val text = it.text
                        if (CodeScanner.ONE_DIMENSIONAL_FORMATS.contains(it.barcodeFormat)) {
//                            if (Constants.userData == null) {
//                                tableGenerator.insertDefaultTable(
//                                    text,
//                                    BaseActivity.getDateTimeFromTimeStamp(System.currentTimeMillis())
//                                )
//                            } else {
                            if (tableName.isEmpty()) {
                                BaseActivity.showAlert(requireActivity(), text)
                            } else {
                                val columns = tableGenerator.getTableColumns(tableName)
                                val scanResultLayout = LayoutInflater.from(requireActivity())
                                    .inflate(R.layout.scan_result_dialog, null)
                                val codeDataTInputView =
                                    scanResultLayout.findViewById<TextInputEditText>(R.id.scan_result_dialog_code_data)
                                val tableDetailLayoutWrapper =
                                    scanResultLayout.findViewById<LinearLayout>(R.id.table_detail_layout_wrapper)
                                val submitBtn =
                                    scanResultLayout.findViewById<MaterialButton>(R.id.scan_result_dialog_submit_btn)
                                val addImageCheckBox =
                                    scanResultLayout.findViewById<MaterialCheckBox>(R.id.add_image_checkbox)
                                val imageSourcesWrapperLayout =
                                    scanResultLayout.findViewById<LinearLayout>(R.id.image_sources_layout)
                                filePathView =
                                    scanResultLayout.findViewById<MaterialTextView>(R.id.filePath)

                                addImageCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
                                    if (isChecked) {
                                        imageSourcesWrapperLayout.visibility = View.VISIBLE
                                        filePathView!!.visibility = View.VISIBLE
                                    } else {
                                        imageSourcesWrapperLayout.visibility = View.GONE
                                        filePathView!!.visibility = View.GONE
                                    }
                                }

                                val cameraImageView =
                                    scanResultLayout.findViewById<AppCompatImageView>(R.id.camera_image_view)
                                val imagesImageView =
                                    scanResultLayout.findViewById<AppCompatImageView>(R.id.images_image_view)

                                cameraImageView.setOnClickListener {
                                    if (RuntimePermissionHelper.checkCameraPermission(
                                            requireActivity(),
                                            Constants.CAMERA_PERMISSION
                                        )
                                    ) {
                                        dispatchTakePictureIntent()
                                    }
                                }

                                imagesImageView.setOnClickListener {
                                    if (RuntimePermissionHelper.checkStoragePermission(
                                            requireActivity(),
                                            Constants.READ_STORAGE_PERMISSION
                                        )
                                    ) {
                                        getImageFromGallery()
                                    }
                                }

                                for (i in columns!!.indices) {
                                    val value = columns[i]
                                    if (value == "id") {
                                        continue
                                    } else if (value == "code_data") {
                                        textInputIdsList.add(Pair(value, codeDataTInputView))
                                        codeDataTInputView.setText(text)
                                    } else {
                                        val tableRowLayout =
                                            LayoutInflater.from(requireContext())
                                                .inflate(
                                                    R.layout.scan_result_table_row_layout,
                                                    null
                                                )
                                        val columnName =
                                            tableRowLayout.findViewById<MaterialTextView>(R.id.table_column_name)
                                        val columnValue =
                                            tableRowLayout.findViewById<TextInputEditText>(R.id.table_column_value)
                                        val columnDropdown =
                                            tableRowLayout.findViewById<AppCompatSpinner>(R.id.table_column_dropdown)
                                        val columnDropDwonLayout =
                                            tableRowLayout.findViewById<LinearLayout>(R.id.table_column_dropdown_layout)
                                        columnName.text = value
                                        val fieldList =
                                            tableGenerator.getFieldList(value, tableName)

                                        if (fieldList.isNotEmpty()) {
                                            if (fieldList.contains(",")) {
                                                val arrayList = fieldList.split(",")
                                                columnValue.visibility = View.GONE
                                                columnDropDwonLayout.visibility = View.VISIBLE
                                                val adapter = ArrayAdapter(
                                                    requireContext(),
                                                    android.R.layout.simple_spinner_item,
                                                    arrayList
                                                )
                                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                                columnDropdown.adapter = adapter
                                                spinnerIdsList.add(Pair(value, columnDropdown))
                                            } else {
                                                columnDropDwonLayout.visibility = View.GONE
                                                columnValue.visibility = View.VISIBLE
                                                columnValue.setText(
                                                    fieldList
                                                )
                                                columnValue.isEnabled = false
                                                columnValue.isFocusable = false
                                                columnValue.isFocusableInTouchMode = false
                                            }

                                        } else {
                                            if (value == "image") {
                                                textInputIdsList.add(Pair(value, columnValue))
                                                continue
                                            }
                                            columnDropDwonLayout.visibility = View.GONE
                                            columnValue.visibility = View.VISIBLE

                                            if (value == "date") {
                                                columnValue.setText(
                                                    BaseActivity.getDateTimeFromTimeStamp(
                                                        System.currentTimeMillis()
                                                    )
                                                )
                                                columnValue.isEnabled = false
                                                columnValue.isFocusable = false
                                                columnValue.isFocusableInTouchMode = false
                                            } else {
                                                columnValue.isEnabled = true
                                                columnValue.isFocusable = true
                                                columnValue.isFocusableInTouchMode = true
                                                columnValue.setText("")
                                            }
                                            textInputIdsList.add(Pair(value, columnValue))
                                        }
                                        tableDetailLayoutWrapper.addView(tableRowLayout)
                                    }
                                }

                                val builder = MaterialAlertDialogBuilder(requireActivity())
                                builder.setView(scanResultLayout)
                                builder.setCancelable(false)
                                val alert = builder.create()
                                alert.show()

                                submitBtn.setOnClickListener {
                                    alert.dismiss()
//
                                    BaseActivity.startLoading(requireActivity())

                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val params = mutableListOf<Pair<String, String>>()
                                            if (addImageCheckBox.isChecked && filePathView!!.text.toString()
                                                    .isNotEmpty()
                                            ) {
                                                if (Constants.mService != null) {
                                                    mService = Constants.mService
                                                }
                                                if (mService != null) {
                                                    val fileMetadata =
                                                        com.google.api.services.drive.model.File()
                                                    fileMetadata.name =
                                                        "Image_${System.currentTimeMillis()}.jpg"
                                                    val filePath: File =
                                                        File(filePathView!!.text.toString())
                                                    val mediaContent =
                                                        FileContent("image/jpeg", filePath)
                                                    val file: com.google.api.services.drive.model.File =
                                                        mService!!.files()
                                                            .create(fileMetadata, mediaContent)
                                                            .setFields("id")
                                                            .execute()
                                                    val url =
                                                        "https://drive.google.com/file/d/" + file.id + "/view?usp=sharing"
//                                                    imageDrivePath = url

                                                    for (i in 0 until textInputIdsList.size) {
                                                        val pair = textInputIdsList[i]
                                                        if (pair.first == "image") {
                                                            params.add(
                                                                Pair(
                                                                    pair.first,
                                                                    url
                                                                )
                                                            )
                                                        } else {
                                                            params.add(
                                                                Pair(
                                                                    pair.first,
                                                                    pair.second.text.toString()
                                                                        .trim()
                                                                )
                                                            )
                                                        }
                                                    }
                                                    for (j in 0 until spinnerIdsList.size) {
                                                        val pair = spinnerIdsList[j]
                                                        params.add(
                                                            Pair(
                                                                pair.first,
                                                                pair.second.selectedItem.toString()
                                                            )
                                                        )
                                                    }
//                                                    Log.e("File ID: ", file.id)
//                                                    Log.d("TEST199", "https://drive.google.com/file/d/" + file.id + "/view?usp=sharing")
                                                }

                                            } else {
                                                for (i in 0 until textInputIdsList.size) {
                                                    val pair = textInputIdsList[i]
                                                    if (pair.first == "image") {
                                                        params.add(
                                                            Pair(
                                                                pair.first,
                                                                pair.second.text.toString().trim()
                                                            )
                                                        )
                                                    } else {
                                                        params.add(
                                                            Pair(
                                                                pair.first,
                                                                pair.second.text.toString().trim()
                                                            )
                                                        )
                                                    }
                                                }
                                                for (j in 0 until spinnerIdsList.size) {
                                                    val pair = spinnerIdsList[j]
                                                    params.add(
                                                        Pair(
                                                            pair.first,
                                                            pair.second.selectedItem.toString()
                                                        )
                                                    )
                                                }
                                            }
                                            tableGenerator.insertData(tableName, params)
                                            CoroutineScope(Dispatchers.Main).launch {
                                                Handler(Looper.myLooper()!!).postDelayed({
                                                    BaseActivity.dismiss()
                                                    Toast.makeText(
                                                        requireActivity(),
                                                        "Scan data saved successfully!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    textInputIdsList.clear()
                                                    spinnerIdsList.clear()
                                                    params.clear()
                                                    tableDetailLayoutWrapper.removeAllViews()

                                                }, 1000)
                                            }

                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
//                            }
                        } else {

                            var qrHistory: CodeHistory? = null
                            val type =
                                if (text.contains("http") || text.contains("https") || text.contains(
                                        "www"
                                    )
                                ) {
                                    "link"
                                } else if (text.isDigitsOnly()) {
                                    "number"
                                } else if (text.contains("VCARD") || text.contains("vcard")) {
                                    "contact"
                                } else if (text.contains("WIFI:") || text.contains("wifi:")) {
                                    "wifi"
                                } else if (text.contains("tel:")) {
                                    "phone"
                                } else if (text.contains("smsto:") || text.contains("sms:")) {
                                    "sms"
                                } else if (text.contains("instagram")) {
                                    "instagram"
                                } else if (text.contains("whatsapp")) {
                                    "whatsapp"
                                } else {
                                    "text"
                                }
                            if (text.isNotEmpty()) {

                                if (CodeScanner.ONE_DIMENSIONAL_FORMATS.contains(it.barcodeFormat)) {
                                    qrHistory = CodeHistory(
                                        "sattar",
                                        "${System.currentTimeMillis()}",
                                        text,
                                        "code",
                                        "free",
                                        "barcode",
                                        "scan",
                                        "",
                                        "0",
                                        "",
                                        System.currentTimeMillis().toString()
                                    )

                                    appViewModel.insert(qrHistory)

                                } else {
                                    qrHistory = CodeHistory(
                                        "sattar",
                                        "${System.currentTimeMillis()}",
                                        text,
                                        type,
                                        "free",
                                        "qr",
                                        "scan",
                                        "",
                                        "0",
                                        "",
                                        System.currentTimeMillis().toString()
                                    )
                                    appViewModel.insert(qrHistory)
                                }
                                Toast.makeText(
                                    requireActivity(),
                                    "Scan data saved successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Handler(Looper.myLooper()!!).postDelayed({
                                    val intent = Intent(context, CodeDetailActivity::class.java)
                                    intent.putExtra("HISTORY_ITEM", qrHistory)
                                    requireActivity().startActivity(intent)
                                }, 2000)
                            }
                        }
                    }
                }
                errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
                    requireActivity().runOnUiThread {
//                Toast.makeText(this, "Camera initialization error: ${it.message}",
//                    Toast.LENGTH_LONG).show()
                    }
                }

                scannerView.setOnClickListener {
                    startPreview()
                }
                startPreview()
            }
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val storageDir: File =
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
            Constants.captureImagePath = currentPhotoPath
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireActivity(),
                        requireActivity().applicationContext.packageName + ".fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    cameraResultLauncher.launch(takePictureIntent)
                }
            }
        }
    }

    private fun getImageFromGallery() {
        val fileIntent = Intent(Intent.ACTION_PICK)
        fileIntent.type = "image/*"
        resultLauncher.launch(fileIntent)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
//                val path: String? = null
                val data: Intent? = result.data
                if (data!!.data != null) {
                    val imageUri = data.data!!
                    filePathView!!.text =
                        ImageManager.getRealPathFromUri(requireActivity(), imageUri)
                }
            }
        }

    // THIS RESULT LAUNCHER WILL CALL THE ACTION PICK FROM FILES FOR BACKGROUND AND LOGO IMAGE
    private var cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (currentPhotoPath != null) {
                    filePathView!!.text = currentPhotoPath
                } else {
                    if (Constants.captureImagePath != null) {
                        currentPhotoPath = Constants.captureImagePath
                        filePathView!!.text = currentPhotoPath
                    }
                }
            }
        }

    override fun onResume() {
        super.onResume()
        startScanner()
        getTableList()
    }

    override fun onPause() {
        if (codeScanner != null) {
            codeScanner!!.releaseResources()
        }
        super.onPause()
    }

    // THIS FUNCTION WILL HANDLE THE RUNTIME PERMISSION RESULT
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            Constants.CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScanner()
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            Constants.CAMERA_PERMISSION
                        )
                    ) {
                        RuntimePermissionHelper.checkStoragePermission(
                            requireActivity(),
                            Constants.CAMERA_PERMISSION
                        )
                    } else {
                        MaterialAlertDialogBuilder(requireActivity())
                            .setMessage("Please allow the Camera permission to use the scanner to scan Image.")
                            .setCancelable(false)
                            .setPositiveButton("Ok") { dialog, which ->
                                dialog.dismiss()
                            }
                            .create().show()
                    }
                }
            }
            else -> {

            }
        }
    }

}