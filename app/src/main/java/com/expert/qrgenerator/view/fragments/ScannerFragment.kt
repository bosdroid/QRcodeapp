package com.expert.qrgenerator.view.fragments

import android.Manifest
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
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
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.budiyev.android.codescanner.*
import com.expert.qrgenerator.R
import com.expert.qrgenerator.interfaces.LoginCallback
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.Sheet
import com.expert.qrgenerator.model.TableObject
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.singleton.DriveService
import com.expert.qrgenerator.utils.*
import com.expert.qrgenerator.view.activities.BaseActivity
import com.expert.qrgenerator.view.activities.BaseActivity.Companion.rateUs
import com.expert.qrgenerator.view.activities.CodeDetailActivity
import com.expert.qrgenerator.view.activities.MainActivity
import com.expert.qrgenerator.view.activities.TablesActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.FileContent
import com.google.api.services.drive.model.FileList
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.Result
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import com.android.volley.RetryPolicy





class ScannerFragment : Fragment() {

    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    private var arrayList = mutableListOf<String>()
    private var filePathView: MaterialTextView? = null
    var currentPhotoPath: String? = null
    private var codeScanner: CodeScanner? = null
    private lateinit var scannerView: CodeScannerView
    private lateinit var appViewModel: AppViewModel
    private lateinit var tableGenerator: TableGenerator
    private lateinit var tipsSwitchBtn: SwitchMaterial
    private var tableName: String = ""
    private lateinit var tablesSpinner: AppCompatSpinner
    private lateinit var sheetsSpinner: AppCompatSpinner
    private lateinit var modesSpinner: AppCompatSpinner
    private var textInputIdsList = mutableListOf<Pair<String, TextInputEditText>>()
    private var spinnerIdsList = mutableListOf<Pair<String, AppCompatSpinner>>()
    private lateinit var addNewTableBtn: MaterialButton
    private lateinit var appSettings: AppSettings
    private var imageDrivePath = ""
    private var isFileSelected = false
    private var listener: ScannerInterface? = null
    private var cameraProviderFuture: ListenableFuture<*>? = null
    private var cameraExecutor: ExecutorService? = null
    private var mContext: AppCompatActivity? = null

    //private var previewView: PreviewView? = null
    private var imageAnalyzer: MyImageAnalyzer? = null
    private var isFlashOn = false
    private lateinit var cam: Camera
    private lateinit var container: FrameLayout
    private lateinit var previewView: PreviewView
    private lateinit var flashImg: ImageView
    private val TAG = ScannerFragment::class.java.name
    private var sheetsList = mutableListOf<Sheet>()
    private var userRecoverableAuthType = 0
    private var selectedSheetId: String = ""
    private var selectedSheetName: String = ""
    private lateinit var connectGoogleSheetsTextView:MaterialTextView
    private lateinit var sheetsTopLayout:LinearLayout

    interface ScannerInterface {
        fun login(callback: LoginCallback)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context as AppCompatActivity
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefDate = DialogPrefs.getDate(requireContext())
        if (prefDate == null) {
            DialogPrefs.setDate(requireContext(), DateUtils.getCurrentDate())
        }
        val scans = DialogPrefs.getSuccessScan(requireContext())
        val isSharedQr = DialogPrefs.getShared(requireContext())
        if ((getDateDifference() >= 3 && scans >= 2) || (getDateDifference() >= 3 && isSharedQr)) {
            mContext.let {
                if (it != null) {
                    rateUs(it)
                }
            }
        }
    }

    private fun getDateDifference(): Int {
        var days = 0
        val myFormat = SimpleDateFormat(DateUtils.DATE_FORMAT)
        val currentDate = DateUtils.getCurrentDate()
//        val currentDate = "2021-07-19"
        val prefsDate = DialogPrefs.getDate(requireContext())
        val dateCurrent = myFormat.parse(currentDate)
        if (prefsDate != null) {
            val datePrefs = myFormat.parse(prefsDate)
            val timeCurrent = dateCurrent?.time
            val timePrefs = datePrefs?.time
            if (timeCurrent != null && timePrefs != null) {
                val difference = timeCurrent - timePrefs
                days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS).toInt()
            }
            Log.d(TAG, "getDateDifference: $days, $currentDate, $datePrefs")
        }
        return days
    }

    private fun initViews(view: View) {
        scannerView = view.findViewById(R.id.scanner_view)
        tablesSpinner = view.findViewById(R.id.tables_spinner)
        sheetsSpinner = view.findViewById(R.id.sheets_spinner)
        modesSpinner = view.findViewById(R.id.modes_spinner)
        addNewTableBtn = view.findViewById(R.id.add_new_table_btn)
        container = view.findViewById(R.id.container)
        previewView = view.findViewById(R.id.previewview)
        tipsSwitchBtn = view.findViewById(R.id.home_tips_switch)
        connectGoogleSheetsTextView = view.findViewById(R.id.connect_google_sheets_text_view)
        sheetsTopLayout = view.findViewById(R.id.sheets_top_layout)
        flashImg = view.findViewById(R.id.flashImg)
        addNewTableBtn.setOnClickListener {
            startActivity(Intent(requireActivity(), TablesActivity::class.java))
        }

        connectGoogleSheetsTextView.setOnClickListener {
            listener!!.login(object : LoginCallback {
                override fun onSuccess() {
                    Log.d("TEST199", "success")
                    onResume()
                }

            })
        }

    }

    private fun getModeList() {
        val modeList = requireActivity().resources.getStringArray(R.array.mode_list)
        val adapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item,
            modeList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modesSpinner.adapter = adapter

        if (appSettings.getString(requireActivity().getString(R.string.key_mode))!!.isNotEmpty()) {
            for (i in modeList.indices) {
                val value = modeList[i]
                if (value == appSettings.getString(requireActivity().getString(R.string.key_mode))) {
                    modesSpinner.setSelection(i)
                    break
                }
            }
        }

        modesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }

            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                val mode = adapterView!!.getItemAtPosition(i).toString()
                appSettings.putString(requireActivity().getString(R.string.key_mode), mode)
            }
        }

    }

    private fun getTableList() {
        val tablesList = mutableListOf<String>()
        tablesList.addAll(tableGenerator.getAllDatabaseTables())
        if (tablesList.isNotEmpty()) {
            tableName = tablesList[0]
            val adapter = ArrayAdapter(
                requireActivity(),
                android.R.layout.simple_spinner_item,
                tablesList
            )
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
                        val isFound = tableGenerator.searchItem(tableName, it.text)
                        if (isFound && appSettings.getString(getString(R.string.key_mode)) == "Inventory"){
                          val quantity = tableGenerator.getScanQuantity(tableName, it.text)
                          val qty:Int = quantity.toInt()+1
                          val isUpdate = tableGenerator.updateScanQuantity(tableName, it.text, qty)
                          if (isUpdate){
                              //showAlert(requireActivity(),requireActivity().getString(R.string.scan_quantity_increase_success_text))
                              Toast.makeText(
                                  requireActivity(),
                                  requireActivity().getString(R.string.scan_quantity_increase_success_text),
                                  Toast.LENGTH_SHORT
                              ).show()
                              Handler(Looper.myLooper()!!).postDelayed({
                                  startPreview()
                              }, 2000)

						  }
                        }
                        else{
                            displayDataSubmitDialog(it, "")
                        }

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

    private lateinit var alert: AlertDialog
    private lateinit var addImageCheckBox: MaterialCheckBox
    private lateinit var tableDetailLayoutWrapper: LinearLayout
    private lateinit var barcodeDetailParentLayout: LinearLayout
    private fun displayDataSubmitDialog(it: Result?, scanText: String) {
        var text = ""
        text = if (it == null) {
            scanText
        } else {
            it.text
        }
        playSound(true)
        generateVibrate()

        if (appSettings.getString(getString(R.string.key_mode)) == "Seller") {
            val isFound = tableGenerator.searchItem(tableName, text)
            if (isFound) {
                val quantity = tableGenerator.getScanQuantity(tableName, text)
                var qty = quantity.toInt()
                if (qty != -1){
                    if (qty > 0 ){
                        qty -= 1
                        val isUpdate = tableGenerator.updateScanQuantity(tableName, text, qty)
                        if (isUpdate){
                            Toast.makeText(
                                requireActivity(),
                                "${getString(R.string.scan_quantity_update_success_text)} ${
                                    getString(
                                        R.string.scan_quantity_remaining_text
                                    )
                                } $qty",
                                Toast.LENGTH_SHORT
                            ).show()
                            Handler(Looper.myLooper()!!).postDelayed({
                                codeScanner!!.startPreview()
                            }, 2000)
                        }
                    }
                    else{
                        val isSuccess = tableGenerator.deleteItem(tableName, text)
                        if (isSuccess) {
                            Toast.makeText(
                                requireActivity(),
                                requireActivity().getString(R.string.scan_item_delete_success_text),
                                Toast.LENGTH_SHORT
                            ).show()
                            Handler(Looper.myLooper()!!).postDelayed({
                                codeScanner!!.startPreview()
                            }, 2000)

                        }
                    }
                }

            } else {
                showAlert(
                    requireActivity(),
                    getString(R.string.scan_item_not_found_text)
                )
            }
        } else if (appSettings.getString(getString(R.string.key_mode)) == "Search") {
            val searchTableObject = tableGenerator.getScanItem(tableName, text)

            if (searchTableObject != null) {
                renderQuickLinksDialog(searchTableObject)

            } else {
                displayItemNotFoundDialog(text)
            }

        } else {
            copyToClipBoard(text)
            if (CodeScanner.ONE_DIMENSIONAL_FORMATS.contains(it!!.barcodeFormat) || scanText.isNotEmpty()) {

                if (tableName.isEmpty()) {
                    showAlert(requireActivity(), text)
                } else {
                    val columns = tableGenerator.getTableColumns(tableName)
                    val scanResultLayout = LayoutInflater.from(requireActivity())
                        .inflate(R.layout.scan_result_dialog, null)
                    val codeDataTInputView =
                        scanResultLayout.findViewById<TextInputEditText>(R.id.scan_result_dialog_code_data)
                    tableDetailLayoutWrapper =
                        scanResultLayout.findViewById<LinearLayout>(R.id.table_detail_layout_wrapper)
                    val submitBtn =
                        scanResultLayout.findViewById<MaterialButton>(R.id.scan_result_dialog_submit_btn)
                    addImageCheckBox =
                        scanResultLayout.findViewById<MaterialCheckBox>(R.id.add_image_checkbox)
                    val severalImagesHintView =
                        scanResultLayout.findViewById<MaterialTextView>(R.id.several_images_hint_view)
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
                                        listener!!.login(object : LoginCallback {
                                            override fun onSuccess() {
                                                Log.d("TEST199", "success")
                                                onResume()
                                            }

                                        })
                                    }
                                    .create().show()
                            } else {
                                severalImagesHintView.visibility = View.VISIBLE
                                imageSourcesWrapperLayout.visibility = View.VISIBLE
                                filePathView!!.visibility = View.VISIBLE
                            }

                        } else {
                            severalImagesHintView.visibility = View.GONE
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
                        if (ContextCompat.checkSelfPermission(
                                requireActivity(),
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_GRANTED) {
                            getImageFromGallery()
                        }
                        else{
                            requestPermissions(
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                Constants.READ_STORAGE_REQUEST_CODE
                            )
                        }
                    }

                    for (i in columns!!.indices) {
                        val value = columns[i]
                        if (value == "id" || value == "quantity") {
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
                    alert = builder.create()
                    alert.show()
                    if (appSettings.getBoolean(getString(R.string.key_tips))) {
                        val duration = appSettings.getLong("tt2")
                        if (duration.compareTo(0) == 0 || System.currentTimeMillis() - duration > TimeUnit.DAYS.toMillis(
                                1
                            )
                        ) {
                            SimpleTooltip.Builder(requireActivity())
                                .anchorView(scanResultLayout)
                                .text(getString(R.string.after_scan_result_tip_text))
                                .gravity(Gravity.BOTTOM)
                                .animated(true)
                                .transparentOverlay(false)
                                .onDismissListener { tooltip ->
                                    tooltip.dismiss()
                                    appSettings.putLong("tt2", System.currentTimeMillis())
                                    openAddImageTooltip(addImageCheckBox, submitBtn)
                                }
                                .build()
                                .show()
                        }
                    }
                    submitBtn.setOnClickListener {
                        alert.dismiss()
                        saveToDriveAppFolder()

                    }
                }
            } else {
                val bundle = Bundle()
                bundle.putString("second scanner", "triggers")
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
                            System.currentTimeMillis().toString(),
                            ""
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
                            System.currentTimeMillis().toString(),
                            ""
                        )
                        appViewModel.insert(qrHistory)
                    }
                    saveSuccessScans()
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
    }

    private fun renderQuickLinksDialog(searchTableObject: TableObject) {

        val quickLinksLayout =
            LayoutInflater.from(requireActivity()).inflate(R.layout.quick_links_dialog_layout, null)
        val typeTextHeading =
            quickLinksLayout.findViewById<MaterialTextView>(R.id.quick_links_code_detail_type_text_heading)
        typeTextHeading.text = getString(R.string.barcode_text_data_heding)
        val encodeDataTextView =
            quickLinksLayout.findViewById<MaterialTextView>(R.id.quick_links_code_detail_encode_data)
        encodeDataTextView.text = searchTableObject.code_data
        val clipboardCopyView =
            quickLinksLayout.findViewById<MaterialTextView>(R.id.quick_links_code_detail_clipboard_copy_view)
        clipboardCopyView.setOnClickListener {
            val clipboard: ClipboardManager =
                requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(
                clipboard.primaryClipDescription!!.label,
                encodeDataTextView.text.toString()
            )
            clipboard.setPrimaryClip(clip)
            Toast.makeText(
                requireActivity(),
                getString(R.string.text_saved_clipboard),
                Toast.LENGTH_SHORT
            ).show()
        }
        barcodeDetailParentLayout =
            quickLinksLayout.findViewById(R.id.quick_links_barcode_detail_wrapper_layout)
        displayBarcodeDetail(searchTableObject)

        val cancelButton =
            quickLinksLayout.findViewById<AppCompatImageView>(R.id.quick_links_code_detail_cancel)
        val moreDetailButton =
            quickLinksLayout.findViewById<AppCompatImageButton>(R.id.quick_links_code_detail_more_button)
        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setView(quickLinksLayout)
        builder.setCancelable(false)
        val alertdialog = builder.create()
        alertdialog.show()
        cancelButton.setOnClickListener {
            alertdialog.dismiss()
            codeScanner!!.startPreview()
        }

        moreDetailButton.setOnClickListener {
            alertdialog.dismiss()
            val intent = Intent(requireActivity(), CodeDetailActivity::class.java)
            intent.putExtra("TABLE_NAME", tableName)
            intent.putExtra("TABLE_ITEM", searchTableObject)
            requireActivity().startActivity(intent)
        }

    }

    private fun displayItemNotFoundDialog(text: String){
        val itemNotFoundLayout = LayoutInflater.from(requireActivity())
            .inflate(R.layout.quick_links_item_not_found_dialog, null)
        val qcTableSpinner =
            itemNotFoundLayout.findViewById<AppCompatSpinner>(R.id.quick_links_tables_spinner)
        val cancelButton =
            itemNotFoundLayout.findViewById<MaterialButton>(R.id.quick_links_dialog_cancel_btn)
        val selectButton =
            itemNotFoundLayout.findViewById<MaterialButton>(R.id.quick_links_dialog_select_btn)
        val tablesList = mutableListOf<String>()
        tablesList.addAll(tableGenerator.getAllDatabaseTables())
        if (tablesList.isNotEmpty()) {
            tableName = tablesList[0]
            val adapter = ArrayAdapter(
                requireActivity(),
                android.R.layout.simple_spinner_item,
                tablesList
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            qcTableSpinner.adapter = adapter

            if (appSettings.getString("SCAN_SELECTED_TABLE")!!.isNotEmpty()) {
                for (i in 0 until tablesList.size) {
                    val value = tablesList[i]
                    if (value == appSettings.getString("SCAN_SELECTED_TABLE")) {
                        qcTableSpinner.setSelection(i)
                        tableName = value
                        break
                    }
                }
            }
        }

        qcTableSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

        val builder1 = MaterialAlertDialogBuilder(requireActivity())
        builder1.setView(itemNotFoundLayout)
        val alertdialog1 = builder1.create()
        alertdialog1.show()
        selectButton.setOnClickListener {
            alertdialog1.dismiss()
            val searchObj = tableGenerator.getScanItem(tableName, text)
            if (searchObj != null){
                renderQuickLinksDialog(searchObj)
            }
            else{
                displayItemNotFoundDialog(text)
            }
        }
        cancelButton.setOnClickListener { alertdialog1.dismiss()
        codeScanner!!.startPreview()
        }
    }

    var barcodeEditList = mutableListOf<Triple<AppCompatImageView, String, String>>()
    private var counter: Int = 0
    private fun displayBarcodeDetail(tableObject: TableObject) {


        if (barcodeDetailParentLayout.childCount > 0) {
            barcodeDetailParentLayout.removeAllViews()
        }

        barcodeEditList.add(
            Triple(
                AppCompatImageView(requireActivity()),
                tableObject.id.toString(),
                "id"
            )
        )
        val codeDataLayout = LayoutInflater.from(requireActivity())
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
                tableObject.code_data,
                "code_data"
            )
        )
        codeDataColumnEditView.visibility = View.GONE
        codeDataColumnValue.text = tableObject.code_data
        codeDataColumnName.text = "code_data"
        barcodeDetailParentLayout.addView(codeDataLayout)
        val dateLayout = LayoutInflater.from(requireActivity())
            .inflate(R.layout.barcode_detail_item_row, barcodeDetailParentLayout, false)
        val dateColumnValue =
            dateLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_value)
        val dateColumnName =
            dateLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_name)
        val dateColumnEditView = dateLayout.findViewById<AppCompatImageView>(R.id.bcd_edit_view)
        counter += 1
        dateColumnEditView.id = counter
        barcodeEditList.add(Triple(dateColumnEditView, tableObject.date, "date"))
        dateColumnEditView.visibility = View.GONE
        dateColumnValue.text = tableObject.date
        dateColumnName.text = "date"
        barcodeDetailParentLayout.addView(dateLayout)
        val imageLayout = LayoutInflater.from(requireActivity())
            .inflate(R.layout.barcode_detail_item_row, barcodeDetailParentLayout, false)
        val imageColumnValue =
            imageLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_value)
        val imageColumnName =
            imageLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_name)
        val imageColumnEditView =
            imageLayout.findViewById<AppCompatImageView>(R.id.bcd_edit_view)
        counter += 1
        imageColumnEditView.id = counter
        barcodeEditList.add(Triple(imageColumnEditView, tableObject.image, "image"))
        imageColumnEditView.visibility = View.GONE
        imageColumnValue.text = tableObject.image
        imageColumnName.text = "image"
        barcodeDetailParentLayout.addView(imageLayout)

        for (i in 0 until tableObject.dynamicColumns.size) {
            val item = tableObject.dynamicColumns[i]

            val layout = LayoutInflater.from(requireActivity())
                .inflate(R.layout.barcode_detail_item_row, barcodeDetailParentLayout, false)
            val columnValue = layout.findViewById<MaterialTextView>(R.id.bcd_table_column_value)
            val columnName = layout.findViewById<MaterialTextView>(R.id.bcd_table_column_name)
            val columnEditView = layout.findViewById<AppCompatImageView>(R.id.bcd_edit_view)
            counter += 1
            columnEditView.id = counter
            barcodeEditList.add(Triple(columnEditView, item.second, item.first))
            columnEditView.visibility = View.GONE
            columnValue.text = item.second
            columnName.text = item.first
            barcodeDetailParentLayout.addView(layout)

        }
        counter = 0
    }

    private fun saveToDriveAppFolder() {
        if (BaseActivity.isNetworkAvailable(requireActivity())) {
            BaseActivity.dismiss()
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
                            if (multiImagesList.isNotEmpty()) {
                                multiImagesList.clear()
                            }
                            if (params.size > 0){
                                params.clear()
                            }
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
                                        saveSuccessScans()
                                        Toast.makeText(
                                            requireActivity(),
                                            requireActivity().resources.getString(R.string.scan_data_save_success_text),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        textInputIdsList.clear()
                                        spinnerIdsList.clear()
                                        params.clear()
                                        tableDetailLayoutWrapper.removeAllViews()
                                        filePathView!!.setText("")
                                        val bundle = Bundle()
                                        bundle.putString("success", "success")
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
                                saveSuccessScans()
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
                                filePathView!!.setText("")
                                openHistoryBtnTip()
                            }, 1000)
                        }
                    }

//                    for (i in 0 until params.size){
//                        val pair = params[i]
//                        values_JSON.put(pair.second)
//                    }
//                    if (values_JSON.length() > 0){
//                        sendRequest()
//                    }

                } catch (e: Exception) {
                    val bundle = Bundle()
                    bundle.putString("failure", "Error" + e.message)
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
                            saveSuccessScans()
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
                            openHistoryBtnTip()
                            val bundle = Bundle()
                            bundle.putString("success", "success")
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

    // THIS FUNCTION WILL ALERT THE DIFFERENT MESSAGES
    fun showAlert(context: Context, message: String) {
        MaterialAlertDialogBuilder(context)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Ok") { dialog, which ->
                dialog.dismiss()
                codeScanner!!.startPreview()
            }
            .create().show()
    }

    private fun openAddImageTooltip(addImageBox: MaterialCheckBox, submitBtn: MaterialButton) {
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            val duration = appSettings.getLong("tt3")
            if (duration.compareTo(0) == 0 || System.currentTimeMillis() - duration > TimeUnit.DAYS.toMillis(
                    1
                )
            ) {
                SimpleTooltip.Builder(requireActivity())
                    .anchorView(addImageBox)
                    .text(getString(R.string.add_image_tip_text))
                    .gravity(Gravity.BOTTOM)
                    .animated(true)
                    .transparentOverlay(false)
                    .onDismissListener { tooltip ->
                        tooltip.dismiss()
                        appSettings.putLong("tt3", System.currentTimeMillis())
                        openSubmitBtnTip(submitBtn)
                    }
                    .build()
                    .show()
            }
        }
    }

    private fun openSubmitBtnTip(submitBtn: MaterialButton) {
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            val duration = appSettings.getLong("tt4")
            if (duration.compareTo(0) == 0 || System.currentTimeMillis() - duration > TimeUnit.DAYS.toMillis(
                    1
                )
            ) {
                SimpleTooltip.Builder(requireActivity())
                    .anchorView(submitBtn)
                    .text(getString(R.string.submit_btn_tip_text))
                    .gravity(Gravity.BOTTOM)
                    .animated(true)
                    .transparentOverlay(false)
                    .onDismissListener { tooltip ->
                        appSettings.putLong("tt4", System.currentTimeMillis())
                        tooltip.dismiss()
                    }
                    .build()
                    .show()
            }
        }
    }

    private fun openHistoryBtnTip() {
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            val duration = appSettings.getLong("tt5")
            if (duration.compareTo(0) == 0 || System.currentTimeMillis() - duration > TimeUnit.DAYS.toMillis(
                    1
                )
            ) {
                SimpleTooltip.Builder(requireActivity())
                    .anchorView(MainActivity.historyBtn)
                    .text(getString(R.string.history_btn_tip_text))
                    .gravity(Gravity.BOTTOM)
                    .animated(true)
                    .transparentOverlay(false)
                    .onDismissListener { tooltip ->
                        appSettings.putLong("tt5", System.currentTimeMillis())
                        tooltip.dismiss()
                    }
                    .build()
                    .show()
            }
        }
    }

    var uploadedUrlList = mutableListOf<String>()
    private fun uploadImageOnDrive(): Boolean {
        var isUploadingSuccess = false
        val imageList = filePathView!!.text.toString()
        if (imageList.contains(",")) {
            val array = imageList.split(",")
            for (i in array.indices) {
                val imagePath = array[i]

                val bundle = Bundle()
                bundle.putString("starts", "starts")
                mFirebaseAnalytics?.logEvent("upload image", bundle)
                try {

                    val fileMetadata =
                        com.google.api.services.drive.model.File()
                    fileMetadata.name =
                        "Image_${System.currentTimeMillis()}.jpg"
                    val filePath: File =
                        File(imagePath)
                    val mediaContent = FileContent("image/jpeg", filePath)

                    val file: com.google.api.services.drive.model.File =
                        DriveService.instance!!.files()
                            .create(fileMetadata, mediaContent)
                            .setFields("id")
                            .execute()
                    // WHEN EXECUTE FUNCTION RUN SUCCESSFULLY THEN IT RETURN FILE OBJECT HAVING ID
                    // SO, WE MAKE THE DYNAMIC PATH OF IMAGE USING FILE ID LIKE BELOW
                    url = "https://drive.google.com/file/d/" + file.id + "/view?usp=sharing"
                    uploadedUrlList.add(url)
                    val bundle = Bundle()
                    bundle.putString("success", "success")
                    mFirebaseAnalytics?.logEvent("upload image", bundle)
                    isUploadingSuccess = true
                } catch (e: UserRecoverableAuthIOException) {
//            Log.d("TEST199",e.localizedMessage!!)
                    userRecoverableAuthType = 0
                    userAuthLauncher.launch(e.intent)
                    val bundle = Bundle()
                    bundle.putString("UserRecoverableAuthIOException", "Error: " + e.message)
                    mFirebaseAnalytics?.logEvent("upload image", bundle)
                    isUploadingSuccess = false
                } catch (e: GoogleJsonResponseException) {
                    val bundle = Bundle()
                    bundle.putString("GoogleJsonResponseException", "Error: " + e.message)
                    mFirebaseAnalytics?.logEvent("upload image", bundle)
                    showAlert(
                        requireActivity(),
                        e.details.message
                    )
                    isUploadingSuccess = false
                }
            }
            return if (isUploadingSuccess) {
                url = uploadedUrlList.joinToString(",")
                uploadedUrlList.clear()
                true
            } else {
                false
            }
        } else {
            val bundle = Bundle()
            bundle.putString("starts", "starts")
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
                bundle.putString("success", "success")
                mFirebaseAnalytics?.logEvent("upload image", bundle)
                return true
            } catch (e: UserRecoverableAuthIOException) {
//            Log.d("TEST199",e.localizedMessage!!)
                userRecoverableAuthType = 0
                userAuthLauncher.launch(e.intent)

                val bundle = Bundle()
                bundle.putString("UserRecoverableAuthIOException", "Error: " + e.message)
                mFirebaseAnalytics?.logEvent("upload image", bundle)
                return false
            } catch (e: GoogleJsonResponseException) {
                val bundle = Bundle()
                bundle.putString("GoogleJsonResponseException", "Error: " + e.message)
                mFirebaseAnalytics?.logEvent("upload image", bundle)
                showAlert(
                    requireActivity(),
                    e.details.message
                )
                return false
            }
        }

//        val bundle = Bundle()
//        bundle.putString("starts", "starts")
//        mFirebaseAnalytics?.logEvent("upload image", bundle)
//        try {
//
//            val fileMetadata =
//                com.google.api.services.drive.model.File()
//            fileMetadata.name =
//                "Image_${System.currentTimeMillis()}.jpg"
//            val filePath: File =
//                File(filePathView!!.text.toString())
//            val mediaContent = FileContent("image/jpeg", filePath)
//
//            val file: com.google.api.services.drive.model.File =
//                DriveService.instance!!.files()
//                    .create(fileMetadata, mediaContent)
//                    .setFields("id")
//                    .execute()
//            // WHEN EXECUTE FUNCTION RUN SUCCESSFULLY THEN IT RETURN FILE OBJECT HAVING ID
//            // SO, WE MAKE THE DYNAMIC PATH OF IMAGE USING FILE ID LIKE BELOW
//            url = "https://drive.google.com/file/d/" + file.id + "/view?usp=sharing"
//            val bundle = Bundle()
//            bundle.putString("success", "success")
//            mFirebaseAnalytics?.logEvent("upload image", bundle)
//            return true
//        } catch (e: UserRecoverableAuthIOException) {
////            Log.d("TEST199",e.localizedMessage!!)
////            userAuthLauncher.launch(e.intent)
//            val bundle = Bundle()
//            bundle.putString("UserRecoverableAuthIOException", "Error: " + e.message)
//            mFirebaseAnalytics?.logEvent("upload image", bundle)
//            return false
//        } catch (e: GoogleJsonResponseException) {
//            val bundle = Bundle()
//            bundle.putString("GoogleJsonResponseException", "Error: " + e.message)
//            mFirebaseAnalytics?.logEvent("upload image", bundle)
//            showAlert(
//                requireActivity(),
//                e.details.message
//            )
//            return false
//        }
    }

    // THIS GOOGLE LAUNCHER WILL HANDLE RESULT
    private var userAuthLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                chooseAccountLauncher.launch(MainActivity.credential!!.newChooseAccountIntent())
            }
        }


    private var chooseAccountLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val accountName: String? =
                    result.data!!.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    //MainActivity.credential!!.backOff = ExponentialBackOff()
                    MainActivity.credential!!.selectedAccountName = accountName
                    appSettings.putString("ACCOUNT_NAME", accountName)
                    if (userRecoverableAuthType == 0){
                        saveToDriveAppFolder()
                    }
                    else{
//                      getAllSheets()
                    }

                }

            }
        }


    private fun createImageFile(bitmap: Bitmap) {
        currentPhotoPath = ImageManager.readWriteImage(requireActivity(), bitmap).absolutePath
        //Constants.captureImagePath = currentPhotoPath
        multiImagesList.add(currentPhotoPath!!)
        filePathView!!.text = multiImagesList.joinToString(",")
        isFileSelected = true
    }

    private fun getImageFromGallery() {
        val fileIntent = Intent(Intent.ACTION_PICK)
        fileIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        fileIntent.type = "image/*"
        resultLauncher.launch(fileIntent)
    }


    var multiImagesList = mutableListOf<String>()
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {
//                val path: String? = null
                val data: Intent? = result.data
                val clipData: ClipData? = data!!.clipData

                if (clipData != null) {
                    if (clipData.itemCount > 0) {
                        for (i in 0 until clipData.itemCount) {
                            val imageUri = clipData.getItemAt(i).uri
                            multiImagesList.add(
                                ImageManager.getRealPathFromUri(
                                    requireActivity(),
                                    imageUri
                                )!!
                            )
                        }
                        filePathView!!.text = multiImagesList.joinToString(",")
                        isFileSelected = true
                        //Log.d("TEST199",multiImagesList.toString())
                    }
                } else {
                    if (data.data != null) {
                        val imageUri = data.data!!
                        multiImagesList.add(
                            ImageManager.getRealPathFromUri(
                                requireActivity(),
                                imageUri
                            )!!
                        )
                        filePathView!!.text = multiImagesList.joinToString(",")
                        isFileSelected = true
                    }
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
        getModeList()
//        getAllSheets()
        val flag = appSettings.getBoolean(requireActivity().getString(R.string.key_tips))
        if (flag) {
            tipsSwitchBtn.setText(requireActivity().getString(R.string.tip_switch_on_text))
        } else {
            tipsSwitchBtn.setText(requireActivity().getString(R.string.tip_switch_off_text))
        }
        tipsSwitchBtn.isChecked = flag

        tipsSwitchBtn.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if (isChecked) {
                    tipsSwitchBtn.setText(requireActivity().getString(R.string.tip_switch_on_text))
                } else {
                    tipsSwitchBtn.setText(requireActivity().getString(R.string.tip_switch_off_text))
                }
                appSettings.putBoolean(requireActivity().getString(R.string.key_tips), isChecked)
            }
        })
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
            Constants.READ_STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImageFromGallery()
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            Constants.READ_STORAGE_PERMISSION
                        )
                    ) {
                        RuntimePermissionHelper.checkStoragePermission(
                            requireActivity(),
                            Constants.READ_STORAGE_PERMISSION
                        )
                    } else {
                        MaterialAlertDialogBuilder(requireActivity())
                            .setMessage(requireActivity().resources.getString(R.string.external_storage_permission_error2))
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
        val isAllowCopy = appSettings.getBoolean(requireContext().getString(R.string.key_clipboard))
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
        var count = 0
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
                    Log.d("TEST199", "readBarCodeData: $rawValue")
                    if (barcodes[0].rawValue != null) {
                        displayDataSubmitDialog(null, rawValue!!)
                    }
                }

            }
        }
    }

    fun showTableSelectTip() {
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            val duration = appSettings.getLong("tt10")
            if (duration.compareTo(0) == 0 || System.currentTimeMillis() - duration > TimeUnit.DAYS.toMillis(
                    1
                )
            ) {
                SimpleTooltip.Builder(requireActivity())
                    .anchorView(addNewTableBtn)
                    .text(getString(R.string.table_selector_tip_text))
                    .gravity(Gravity.BOTTOM)
                    .animated(true)
                    .transparentOverlay(false)
                    .onDismissListener { tooltip ->
                        appSettings.putLong("tt10", System.currentTimeMillis())
                        tooltip.dismiss()
                    }
                    .build()
                    .show()
            }
        }
    }

    private fun saveSuccessScans() {
        var scans = DialogPrefs.getSuccessScan(requireActivity())
        if (scans >= 0) {
            scans += 1
            DialogPrefs.setSuccessScan(requireActivity(), scans)
        }
        Log.d("TAG", "ScanCount: $scans")
    }

    private fun getAllSheets() {
         if (Constants.userData != null) {
//             connectGoogleSheetsTextView.visibility =View.GONE
//             sheetsTopLayout.visibility = View.VISIBLE

             CoroutineScope(Dispatchers.IO).launch {
                 try {
                     val result: FileList = DriveService.instance!!.files().list()
                         .setQ("mimeType='application/vnd.google-apps.spreadsheet'")
                         .execute()

                     val files = result.files

                     if (files != null) {
                         if (files.size > 0){
                             sheetsList.clear()
                         }
                         for (file in files) {
                             sheetsList.add(Sheet(file.id, file.name))
                         }

                         CoroutineScope(Dispatchers.Main).launch {
                             if (sheetsList.isNotEmpty()) {
                                 Constants.sheetsList.addAll(sheetsList)
                                 displaySheetSpinner()
                             }
                         }
                     }
                 } catch (userRecoverableException: UserRecoverableAuthIOException) {
                     userRecoverableAuthType = 1
                     userAuthLauncher.launch(userRecoverableException.intent)
                 }
             }
         }
        else{
//            sheetsTopLayout.visibility = View.GONE
//            connectGoogleSheetsTextView.visibility = View.VISIBLE
         }
    }

    private fun displaySheetSpinner(){
        if (sheetsList.isNotEmpty()) {
            selectedSheetId = sheetsList[0].id
            selectedSheetId = sheetsList[0].name
            val adapter = ArrayAdapter(
                requireActivity(),
                android.R.layout.simple_spinner_item,
                sheetsList
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sheetsSpinner.adapter = adapter

            if (appSettings.getString("SELECTED_SHEET")!!.isNotEmpty()) {
                for (i in 0 until sheetsList.size) {
                    val value = sheetsList[i].id
                    if (value == appSettings.getString("SELECTED_SHEET")) {
                        sheetsSpinner.setSelection(i)
                        selectedSheetId = value
                        selectedSheetName = sheetsList[i].name
                        break
                    }
                }
            }
        }

        sheetsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }

            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                selectedSheetId = sheetsList[i].id//adapterView!!.getItemAtPosition(i).toString()
                selectedSheetName = sheetsList[i].name
                appSettings.putString("SELECTED_SHEET", selectedSheetId)
            }
        }
    }

    var values_JSON = JSONArray()
    private fun sendRequest() {
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val sr: StringRequest = object : StringRequest(
                    Method.POST,
                    Constants.googleAppScriptUrl,
                    object : Response.Listener<String?> {
                        override fun onResponse(response: String?) {
                            CoroutineScope(Dispatchers.Main).launch {
                                if (response!!.toLowerCase(Locale.ENGLISH).contains("success")) {
                                    Log.d("TEST199", "sheet data success")
                                } else {
                                    val permissionDeniedLayout = LayoutInflater.from(context)
                                        .inflate(
                                            R.layout.spreadsheet_permission_failed_dialog,
                                            null
                                        )
                                    val builder = MaterialAlertDialogBuilder(requireActivity())
                                    builder.setCancelable(false)
                                    builder.setView(permissionDeniedLayout)
                                    builder.setPositiveButton("Ok") { dialog, which ->
                                        dialog.dismiss()
                                    }
                                    val alert = builder.create()
                                    alert.show()
                                }
                                values_JSON = JSONArray()

                            }
                        }
                    },
                    object : Response.ErrorListener {
                        override fun onErrorResponse(error: VolleyError?) {
                            Toast.makeText(context, error!!.toString(), Toast.LENGTH_SHORT).show()
                            BaseActivity.dismiss()
                        }
                    }) {

                    override fun getBodyContentType(): String {
                        return "application/x-www-form-urlencoded"
                    }

                    override fun getParams(): Map<String, String> {
                        val params: MutableMap<String, String> = HashMap()
                        params["sheetName"] = selectedSheetName
                        params["number"] = "${values_JSON.length()}"
                        params["id"] = selectedSheetId
                        params["value"] = "$values_JSON"
                        return params
                    }

                }
                sr.setRetryPolicy(DefaultRetryPolicy(
                    10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                )
                VolleySingleton(requireActivity()).addToRequestQueue(sr)

            } catch (e: UserRecoverableAuthIOException) {
                e.printStackTrace()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    fun restart(){
        onResume()
    }

}