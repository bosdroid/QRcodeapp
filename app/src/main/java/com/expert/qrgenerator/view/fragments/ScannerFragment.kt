package com.expert.qrgenerator.view.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.budiyev.android.codescanner.*
import com.expert.qrgenerator.R
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.singleton.DriveService
import com.expert.qrgenerator.utils.*
import com.expert.qrgenerator.view.activities.BaseActivity
import com.expert.qrgenerator.view.activities.CodeDetailActivity
import com.expert.qrgenerator.view.activities.TablesActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.FileContent
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class ScannerFragment : Fragment() {

    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    private var arrayList = mutableListOf<String>()
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
    private var imageDrivePath = ""
    private var isFileSelected = false
    private var listener: ScannerInterface? = null
    private var cameraProviderFuture: ListenableFuture<*>? = null
    private var cameraExecutor: ExecutorService? = null
    //private var previewView: PreviewView? = null
    private var imageAnalyzer: MyImageAnalyzer? = null
    private var isFlashOn = false
    private lateinit var cam: Camera
    private lateinit var container:FrameLayout
    private lateinit var previewView: PreviewView
    private lateinit var flashImg:ImageView

    interface ScannerInterface {
        fun login()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as ScannerInterface
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
        container = view.findViewById(R.id.container)
        previewView = view.findViewById(R.id.previewview)
        flashImg = view.findViewById(R.id.flashImg)
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

    private fun initMlScanner() {
        //requireActivity().window.setFlags(1024, 1024)
       container.visibility = View.VISIBLE
        scannerView.visibility = View.GONE
        flashImg.setOnClickListener { view: View? ->
            if (cam != null) {
                Log.d("TAG", "initMlScanner: ")
                if (cam.cameraInfo.hasFlashUnit()) {
                    if (isFlashOn) {
                        isFlashOn = false
                        flashImg.setImageResource(R.drawable.ic_flash_off)
                        cam.cameraControl.enableTorch(isFlashOn)
                    } else {

                        isFlashOn = true
                        flashImg.setImageResource(R.drawable.ic_flash_on)
                        cam.cameraControl.enableTorch(isFlashOn)
                    }
                }
            }
        }
        imageAnalyzer = MyImageAnalyzer(requireActivity().supportFragmentManager)
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        (cameraProviderFuture as ListenableFuture<ProcessCameraProvider>).addListener(Runnable {
            try {
                val processCameraProvider =
                    (cameraProviderFuture as ListenableFuture<ProcessCameraProvider>).get() as ProcessCameraProvider
                bindPreview(processCameraProvider)
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(requireContext()))

    }

    var url = " "
    private fun startScanner() {
        if (RuntimePermissionHelper.checkCameraPermission(
                requireActivity(),
                Constants.CAMERA_PERMISSION
            )
        ) {

            if (codeScanner == null) {
                codeScanner = CodeScanner(requireActivity(), scannerView)
            }
//            if (!isFileSelected){
//                codeScanner!!.startPreview()
//            }
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
                        displayDataSubmitDialog(it,"")
                    }
                }
                errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
                    if (RuntimePermissionHelper.checkCameraPermission(
                            requireActivity(),
                            Constants.CAMERA_PERMISSION
                        )
                    ) {
                        initMlScanner()
                    }
                    requireActivity().runOnUiThread {
//                        Toast.makeText(
//                            requireContext(), "Camera initialization error: ${it.message}",
//                            Toast.LENGTH_LONG
//                        ).show()
                    }
                }

                scannerView.setOnClickListener {
                    startPreview()
                }
                startPreview()
            }
        }
    }

    private fun displayDataSubmitDialog(it: Result?,scanText:String) {
        var text = ""
        text = if (it == null){
            scanText
        } else{
            it.text
        }
        playSound(true)
        generateVibrate()
        copyToClipBoard(text)
        if (CodeScanner.ONE_DIMENSIONAL_FORMATS.contains(it!!.barcodeFormat) || scanText.isNotEmpty()) {

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
                        if (Constants.userData == null) {
                            addImageCheckBox.isChecked = false
                            MaterialAlertDialogBuilder(requireActivity())
                                .setTitle(requireActivity().resources.getString(R.string.alert_text))
                                .setMessage(requireActivity().resources.getString(R.string.login_error_text))
                                .setNegativeButton(requireActivity().resources.getString(R.string.later_text)) { dialog, which ->
                                    dialog.dismiss()
                                }
                                .setPositiveButton(requireActivity().resources.getString(R.string.login_text)) { dialog, which ->
                                    dialog.dismiss()
                                    listener!!.login()
                                }
                                .create().show()
                        } else {
                            imageSourcesWrapperLayout.visibility = View.VISIBLE
                            filePathView!!.visibility = View.VISIBLE
                        }

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
                        //dispatchTakePictureIntent()
                        val cameraIntent =
                            Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        cameraResultLauncher.launch(cameraIntent)
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
                        val pair = tableGenerator.getFieldList(value, tableName)

                        if (pair != null) {
                            arrayList = mutableListOf()
                            if (!pair.first.contains(",") && pair.second == "listWithValues") {
                                arrayList.add(pair.first)

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
                            } else if (pair.first.contains(",") && pair.second == "listWithValues") {

                                arrayList.addAll(pair.first.split(","))

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
                                    pair.first
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

                    if (BaseActivity.isNetworkAvailable(requireActivity())) {
                        alert.dismiss()
                        BaseActivity.startLoading(requireActivity())
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val params = mutableListOf<Pair<String, String>>()

                                // THIS IF PART WILL RUN WHEN ADD IMAGE CHECK BOX IS CHECKED
                                if (addImageCheckBox.isChecked && filePathView!!.text.toString()
                                        .isNotEmpty()
                                ) {
                                    val isUpload = uploadImageOnDrive()
                                    // IF isUpload IS TRUE THEN DATA SAVE WITH IMAGE URL
                                    // ELSE DISPLAY THE EXCEPTION MESSAGE WITHOUT DATA SAVING
                                    if (isUpload && url.isNotEmpty()) {

                                        // THIS LOOP WILL GET ALL THE DATA FROM DYNAMICALLY GENERATED EDIT TEXT
                                        for (i in 0 until textInputIdsList.size) {
                                            val pair = textInputIdsList[i]
                                            // THIS WILL CHECK IF TEXTINPUTIDSLIST HAVE IMAGE PARAMETER THEN SET THE URL
                                            // WITH COLUMN IMAGE ELSE MAP THE OTHER TEXTINPUTIDS LIST OBJECTS
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

                                        // THIS LOOP WILL GET ALL THE DATA FROM DYNAMICALLY GENERATED DROPDOWNS
                                        for (j in 0 until spinnerIdsList.size) {
                                            val pair = spinnerIdsList[j]
                                            params.add(
                                                Pair(
                                                    pair.first,
                                                    pair.second.selectedItem.toString()
                                                )
                                            )
                                        }
                                        tableGenerator.insertData(tableName, params)
                                        CoroutineScope(Dispatchers.Main).launch {
                                            Handler(Looper.myLooper()!!).postDelayed(
                                                {
                                                    isFileSelected = false
                                                    BaseActivity.dismiss()
                                                    Toast.makeText(
                                                        requireActivity(),
                                                        requireActivity().resources.getString(R.string.scan_data_save_success_text),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    textInputIdsList.clear()
                                                    spinnerIdsList.clear()
                                                    params.clear()
                                                    tableDetailLayoutWrapper.removeAllViews()
                                                    val bundle = Bundle()
                                                    bundle.putString("success","success")
                                                    mFirebaseAnalytics?.logEvent("scanner", bundle)
                                                },
                                                1000
                                            )
                                        }
                                    }

                                }
                                // THIS ELSE PART WILL RUN WHEN ADD IMAGE CHECK BOX IS UN-CHECKED
                                else {
                                    // THIS LOOP WILL GET ALL THE DATA FROM DYNAMICALLY GENERATED EDIT TEXT
                                    for (i in 0 until textInputIdsList.size) {
                                        val pair = textInputIdsList[i]
                                        // THIS WILL CHECK IF TEXTINPUTIDSLIST HAVE IMAGE PARAMETER THEN SET THE URL
                                        // WITH COLUMN IMAGE ELSE MAP THE OTHER TEXTINPUTIDS LIST OBJECTS
                                        if (pair.first == "image") {
                                            params.add(
                                                Pair(
                                                    pair.first,
                                                    pair.second.text.toString()
                                                        .trim()
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
                                    // THIS LOOP WILL GET ALL THE DATA FROM DYNAMICALLY GENERATED DROPDOWNS
                                    for (j in 0 until spinnerIdsList.size) {
                                        val pair = spinnerIdsList[j]
                                        params.add(
                                            Pair(
                                                pair.first,
                                                pair.second.selectedItem.toString()
                                            )
                                        )
                                    }
                                    tableGenerator.insertData(tableName, params)
                                    CoroutineScope(Dispatchers.Main).launch {
                                        Handler(Looper.myLooper()!!).postDelayed({
                                            isFileSelected = false
                                            BaseActivity.dismiss()
                                            Toast.makeText(
                                                requireActivity(),
                                                requireActivity().resources.getString(R.string.scan_data_save_success_text),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            textInputIdsList.clear()
                                            spinnerIdsList.clear()
                                            params.clear()
                                            tableDetailLayoutWrapper.removeAllViews()
                                            codeScanner!!.startPreview()
                                        }, 1000)
                                    }
                                }

                            } catch (e: Exception) {
                                val bundle = Bundle()
                                bundle.putString("failure","Error" + e.message)
                                mFirebaseAnalytics?.logEvent("scanner", bundle)
                                e.printStackTrace()
                            }
                        }
                    } else {

                        val b = MaterialAlertDialogBuilder(requireActivity())
                            .setCancelable(true)
                            .setTitle(requireActivity().resources.getString(R.string.alert_text))
                            .setMessage(requireActivity().resources.getString(R.string.image_upload_internet_error_text))
                            .setNegativeButton(requireActivity().resources.getString(R.string.close_text)) { dialog, which ->
                                dialog.dismiss()
                            }
                            .setPositiveButton(requireActivity().resources.getString(R.string.save_without_image_text)) { dialog, which ->
                                dialog.dismiss()
                                alert.dismiss()
                                val params = mutableListOf<Pair<String, String>>()
                                for (i in 0 until textInputIdsList.size) {
                                    val pair = textInputIdsList[i]
                                    // THIS WILL CHECK IF TEXTINPUTIDSLIST HAVE IMAGE PARAMETER THEN SET THE URL
                                    // WITH COLUMN IMAGE ELSE MAP THE OTHER TEXTINPUTIDS LIST OBJECTS
                                    if (pair.first == "image") {
                                        params.add(
                                            Pair(
                                                pair.first,
                                                pair.second.text.toString()
                                                    .trim()
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
                                // THIS LOOP WILL GET ALL THE DATA FROM DYNAMICALLY GENERATED DROPDOWNS
                                for (j in 0 until spinnerIdsList.size) {
                                    val pair = spinnerIdsList[j]
                                    params.add(
                                        Pair(
                                            pair.first,
                                            pair.second.selectedItem.toString()
                                        )
                                    )
                                }
                                tableGenerator.insertData(tableName, params)
                                CoroutineScope(Dispatchers.Main).launch {
                                    Handler(Looper.myLooper()!!).postDelayed({
                                        isFileSelected = false
                                        BaseActivity.dismiss()
                                        Toast.makeText(
                                            requireActivity(),
                                            requireActivity().resources.getString(R.string.scan_data_save_success_text),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        textInputIdsList.clear()
                                        spinnerIdsList.clear()
                                        params.clear()
                                        tableDetailLayoutWrapper.removeAllViews()
                                        codeScanner!!.startPreview()
                                        val bundle = Bundle()
                                        bundle.putString("success","success")
                                        mFirebaseAnalytics?.logEvent("scanner", bundle)
                                    }, 1000)
                                }
                            }
                        val iAlert = b.create()
                        iAlert.show()
                        iAlert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.purple_700
                            )
                        )
                    }
                }
            }
//                            }
        } else {
            val bundle = Bundle()
            bundle.putString("second scanner","triggers")
            mFirebaseAnalytics?.logEvent("scanner", bundle)
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
                        "qrmagicapp",
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
                        "qrmagicapp",
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
                    requireActivity().resources.getString(R.string.scan_data_save_success_text),
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


    private fun uploadImageOnDrive(): Boolean {
        val bundle = Bundle()
        bundle.putString("starts","starts")
        mFirebaseAnalytics?.logEvent("upload image", bundle)
        try {

            val fileMetadata =
                com.google.api.services.drive.model.File()
            fileMetadata.name =
                "Image_${System.currentTimeMillis()}.jpg"
            val filePath: File =
                File(filePathView!!.text.toString())
            val mediaContent = FileContent("image/jpeg", filePath)

            val file: com.google.api.services.drive.model.File =
                DriveService.instance!!.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
            // WHEN EXECUTE FUNCTION RUN SUCCESSFULLY THEN IT RETURN FILE OBJECT HAVING ID
            // SO, WE MAKE THE DYNAMIC PATH OF IMAGE USING FILE ID LIKE BELOW
            url = "https://drive.google.com/file/d/" + file.id + "/view?usp=sharing"
            val bundle = Bundle()
            bundle.putString("success","success")
            mFirebaseAnalytics?.logEvent("upload image", bundle)
            return true
        } catch (e: UserRecoverableAuthIOException) {
//            Log.d("TEST199",e.localizedMessage!!)
//            userAuthLauncher.launch(e.intent)
            val bundle = Bundle()
            bundle.putString("UserRecoverableAuthIOException","Error: " + e.message)
            mFirebaseAnalytics?.logEvent("upload image", bundle)
            return false
        } catch (e: GoogleJsonResponseException) {
            val bundle = Bundle()
            bundle.putString("GoogleJsonResponseException","Error: " + e.message)
            mFirebaseAnalytics?.logEvent("upload image", bundle)
            BaseActivity.showAlert(
                requireActivity(),
                e.details.message
            )
            return false
        }
    }

    private fun createImageFile(bitmap: Bitmap) {
        currentPhotoPath = ImageManager.readWriteImage(requireActivity(), bitmap).absolutePath
        Constants.captureImagePath = currentPhotoPath
        filePathView!!.text = currentPhotoPath
        isFileSelected = true
    }

    private fun getImageFromGallery() {
        val fileIntent = Intent(Intent.ACTION_PICK)
        fileIntent.type = "image/*"
        resultLauncher.launch(fileIntent)
    }

//    private var userAuthLauncher =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//
//            if (result.resultCode == Activity.RESULT_OK) {
//
//            }
//        }

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
                    isFileSelected = true
                }
            }
        }

    // THIS RESULT LAUNCHER WILL CALL THE ACTION PICK FROM FILES FOR BACKGROUND AND LOGO IMAGE
    private var cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val bitmap = data!!.extras!!.get("data") as Bitmap
                createImageFile(bitmap)
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
                            .setMessage(requireActivity().resources.getString(R.string.camera_permission_failed_text))
                            .setCancelable(false)
                            .setPositiveButton(requireActivity().resources.getString(R.string.ok_text)) { dialog, which ->
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

    private fun playSound(isSuccess: Boolean) {
        val isSounding = appSettings.getBoolean(requireContext().getString(R.string.key_sound))
        if (isSounding) {
            var player: MediaPlayer? = null
            player = if (isSuccess) {
                MediaPlayer.create(requireContext(), R.raw.succes_beep)
            } else {
                MediaPlayer.create(requireContext(), R.raw.error_beep)

            }
            player.start()
        }
    }

    private fun generateVibrate() {
        val isVibrate = appSettings.getBoolean(requireContext().getString(R.string.key_vibration))
        if (isVibrate) {
            if (Build.VERSION.SDK_INT >= 26) {
                (requireContext().getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(
                    VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                (requireContext().getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(150)
            }
        }
    }

    private fun copyToClipBoard(content: String) {
        val isAllowCopy =
            appSettings.getBoolean(requireContext().getString(R.string.key_clipboard))
        if (isAllowCopy) {
            val clipboard = requireContext()
                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Scan code", content)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "copied", Toast.LENGTH_LONG).show()
        }
    }

    private fun bindPreview(processCameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        val imageCapture = ImageCapture.Builder().build()


        val imageAnalysis =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ImageAnalysis.Builder()
                    .setTargetResolution(Size(1200, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build().also {
                        cameraExecutor?.let { it1 ->
                            imageAnalyzer?.let { it2 ->
                                it.setAnalyzer(
                                    it1,
                                    it2
                                )
                            }
                        }
                    }
            } else {
                TODO("VERSION.SDK_INT < LOLLIPOP")
            }

        processCameraProvider.unbindAll()
        cam = processCameraProvider.bindToLifecycle(
            this,
            cameraSelector,
            preview,
            imageCapture,
            imageAnalysis
        )
    }

    inner class MyImageAnalyzer(supportFragmentManager: FragmentManager) : ImageAnalysis.Analyzer {
        var count = 0;
        private var fragmentManager: FragmentManager? = null
        val appViewModel = ViewModelProvider(requireActivity()).get(AppViewModel::class.java)
//        var bottomDialog: BottomDialog? = null

        init {
            this.fragmentManager = supportFragmentManager
//            bottomDialog = BottomDialog()
        }

        override fun analyze(image: ImageProxy) {
            scanBarCode(image)
        }


        private fun scanBarCode(image: ImageProxy) {
            @SuppressLint("UnsafeOptInUsageError") val image1 = image.image!!
            val inputImage = InputImage.fromMediaImage(image1, image.imageInfo.rotationDegrees)
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_CODE_128, Barcode.FORMAT_CODE_39,
                    Barcode.FORMAT_CODE_93, Barcode.FORMAT_CODABAR,
                    Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_ITF, Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E, Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_PDF417, Barcode.FORMAT_AZTEC,
                    Barcode.FORMAT_DATA_MATRIX
                )
                .build()
            val scanner = BarcodeScanning.getClient(options)
            val result = scanner.process(inputImage)
                .addOnSuccessListener { barcodes -> // Task completed successfully

                    readBarCodeData(barcodes)
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    // ...
                }.addOnCompleteListener { barcodes ->
                    image.close()
                }

        }

        private fun readBarCodeData(barcodes: List<Barcode>) {
            if (barcodes.isNotEmpty()) {
                count++
                if (count == 1) {
//                val bounds = barcodes[0].boundingBox
//                val corners = barcodes[0].cornerPoints
                    val rawValue = barcodes[0].rawValue
                    val valueType = barcodes[0].valueType
                    // See API reference for complete list of supported types
                    Log.d("TAG", "readBarCodeData: $rawValue")
                    if (barcodes[0].rawValue != null) {
                           displayDataSubmitDialog(null,rawValue!!)
//                        var qrHistory: CodeHistory? = null
//                        qrHistory = CodeHistory(
//                            "sattar",
//                            "${System.currentTimeMillis()}",
//                            rawValue,
//                            valueType.toString(),
//                            "free",
//                            "qr",
//                            "scan",
//                            "",
//                            0,
//                            "",
//                            System.currentTimeMillis()
//                        )
////                        appViewModel.insert(qrHistory)
//                        playSound(true)
//                        generateVibrate()
//                        copyToClipBoard(rawValue)
//                        Toast.makeText(
//                            requireActivity(),
//                            "Scan data saved successfully!",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        Handler(Looper.myLooper()!!).postDelayed({
//
////                            val intent = Intent(context, CodeDetailActivity::class.java)
////                            intent.putExtra("HISTORY_ITEM", qrHistory)
////                            requireActivity().startActivity(intent)
//                            count = 0
//                        }, 3000)

                    }
                }

            }
        }
    }

}