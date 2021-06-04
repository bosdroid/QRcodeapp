package com.expert.qrgenerator.view.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.app.ActivityCompat
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class ScannerFragment : Fragment() {

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
    private var mService:Drive?=null

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
//            filePicker()
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

            if (appSettings.getString("SCAN_SELECTED_TABLE")!!.isNotEmpty()){
                for (i in 0 until tablesList.size){
                    val value = tablesList[i]
                    if (value == appSettings.getString("SCAN_SELECTED_TABLE")){
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
                            if (Constants.userData == null) {
                                tableGenerator.insertDefaultTable(
                                    text,
                                    BaseActivity.getDateTimeFromTimeStamp(System.currentTimeMillis())
                                )
                            } else {
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

                                    for (i in columns!!.indices) {
                                        val value = columns[i]
                                        if (value == "id") {
                                            continue
                                        } else if (value == "code_data") {
                                            textInputIdsList.add(Pair(value, codeDataTInputView))
                                            codeDataTInputView.setText(text)
                                        }
                                        else {
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
                                            val columnDropdown = tableRowLayout.findViewById<AppCompatSpinner>(R.id.table_column_dropdown)
                                            val columnDropDwonLayout = tableRowLayout.findViewById<LinearLayout>(R.id.table_column_dropdown_layout)
                                            columnName.text = value
                                            val fieldList = tableGenerator.getFieldList(value,tableName)

                                            if (fieldList.isNotEmpty()){
                                                val arrayList = fieldList.split(",")
                                                columnValue.visibility = View.GONE
                                                columnDropDwonLayout.visibility = View.VISIBLE
                                                val adapter = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_item,arrayList)
                                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                                columnDropdown.adapter = adapter
                                                spinnerIdsList.add(Pair(value,columnDropdown))
                                            }
                                            else{
                                                columnDropDwonLayout.visibility = View.GONE
                                                columnValue.visibility = View.VISIBLE

                                                if (value == "date"){
                                                    columnValue.setText(
                                                        BaseActivity.getDateTimeFromTimeStamp(
                                                            System.currentTimeMillis()
                                                        )
                                                    )
                                                }
                                                else{
                                                    columnValue.setText("")
                                                }
                                            }

                                            textInputIdsList.add(Pair(value, columnValue))
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
                                        BaseActivity.startLoading(requireActivity())
                                        val params = mutableListOf<Pair<String, String>>()
                                        for (i in 0 until textInputIdsList.size) {
                                            val pair = textInputIdsList[i]
                                            params.add(
                                                Pair(
                                                    pair.first,
                                                    pair.second.text.toString().trim()
                                                )
                                            )
                                        }
                                        tableGenerator.insertData(tableName, params)
                                        Handler(Looper.myLooper()!!).postDelayed({
                                            BaseActivity.dismiss()
                                            Toast.makeText(
                                                requireActivity(),
                                                "Scan data saved successfully!",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                        }, 1000)
                                    }
                                }
                            }
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

    fun filePicker() {
        val intent: Intent
        val chooseFile = Intent(Intent.ACTION_PICK)
        chooseFile.type = "image/*"
        intent = Intent.createChooser(chooseFile, "Choose a file")
        resultLauncher.launch(intent)
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            val data: Intent? = result.data
            val fileUri: Uri = data!!.data!!
            val filePath = ImageManager.getRealPathFromUri(requireActivity(),fileUri)

            uploadOnDrive(filePath!!)
        }
    }

    private fun uploadOnDrive(path: String){
        if (Constants.mService != null){
            mService = Constants.mService
        }
         CoroutineScope(Dispatchers.IO).launch {
             if (mService != null){
                 val fileMetadata = com.google.api.services.drive.model.File()
                 fileMetadata.name = "Image_${System.currentTimeMillis()}.jpg"
                 val filePath: File = File(path)
                 val mediaContent = FileContent("image/jpeg", filePath)
                 val file: com.google.api.services.drive.model.File =
                     mService!!.files().create(fileMetadata, mediaContent)
                         .setFields("id")
                         .execute()
                 Log.e("File ID: ", file.id)
                 Log.d("TEST199", "https://drive.google.com/file/d/" + file.id + "/view?usp=sharing")
             }
         }

    }

}