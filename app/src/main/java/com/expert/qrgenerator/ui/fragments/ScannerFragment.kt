package com.expert.qrgenerator.ui.fragments

import android.Manifest
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.content.DialogInterface
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
import androidx.activity.viewModels
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.budiyev.android.codescanner.*
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.BarcodeDetailItemRowBinding
import com.expert.qrgenerator.databinding.FragmentScannerBinding
import com.expert.qrgenerator.databinding.QuickLinksDialogLayoutBinding
import com.expert.qrgenerator.databinding.QuickLinksItemNotFoundDialogBinding
import com.expert.qrgenerator.databinding.ScanResultDialogBinding
import com.expert.qrgenerator.databinding.ScanResultTableRowLayoutBinding
import com.expert.qrgenerator.interfaces.LoginCallback
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.Sheet
import com.expert.qrgenerator.model.TableObject
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.singleton.DriveService
import com.expert.qrgenerator.utils.*
import com.expert.qrgenerator.ui.activities.BaseActivity
import com.expert.qrgenerator.ui.activities.BaseActivity.Companion.rateUs
import com.expert.qrgenerator.ui.activities.CodeDetailActivity
import com.expert.qrgenerator.ui.activities.MainActivity
import com.expert.qrgenerator.ui.activities.TablesActivity
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
import dagger.hilt.android.AndroidEntryPoint
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

@AndroidEntryPoint
class ScannerFragment : Fragment() {

    // View Binding for the FragmentScanner layout
    private lateinit var binding: FragmentScannerBinding

    // Firebase Analytics instance for tracking events
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    // List to store string data
    private var arrayList = mutableListOf<String>()

    // Path to the current photo
    var currentPhotoPath: String? = null

    // CodeScanner instance for scanning barcodes
    private var codeScanner: CodeScanner? = null

    // ViewModel for app-related data
    private val appViewModel: AppViewModel by viewModels()

    // Object for generating tables
    private lateinit var tableGenerator: TableGenerator

    // Table name used in operations
    private var tableName: String = ""

    // Lists to store references to TextInputEditText and Spinner views by their IDs
    private var textInputIdsList = mutableListOf<Pair<String, TextInputEditText>>()
    private var spinnerIdsList = mutableListOf<Pair<String, AppCompatSpinner>>()

    // Application settings configuration
    private lateinit var appSettings: AppSettings

    // Path to the image on the drive
    private var imageDrivePath = ""

    // Flag to check if a file is selected
    private var isFileSelected = false

    // Interface listener for scanner events
    private var listener: ScannerInterface? = null

    // Futures and executors for camera operations
    private var cameraProviderFuture: ListenableFuture<*>? = null
    private var cameraExecutor: ExecutorService? = null

    // Context for accessing activity-specific functions
    private var mContext: AppCompatActivity? = null

    // Image analyzer for processing scanned images
    private var imageAnalyzer: MyImageAnalyzer? = null

    // Flag to check if the flashlight is on
    private var isFlashOn = false

    // Camera instance for capturing images
    private lateinit var cam: Camera

    // Tag for logging purposes
    private val TAG = ScannerFragment::class.java.name

    // List to store sheets information
    private var sheetsList = mutableListOf<Sheet>()

    // Type of user recoverable authentication required
    private var userRecoverableAuthType = 0

    // ID and name of the selected sheet
    private var selectedSheetId: String = ""
    private var selectedSheetName: String = ""

    // URL used in various operations
    var url = ""

    // Alert dialog for displaying messages
    private lateinit var alert: AlertDialog

    // View Binding for ScanResultDialog layout
    private lateinit var scanResultDialogBinding: ScanResultDialogBinding

    // List to store uploaded URLs
    var uploadedUrlList = mutableListOf<String>()

    // View Bindings for QuickLinks dialogs
    private lateinit var quickLinksDialogLayoutBinding: QuickLinksDialogLayoutBinding
    private lateinit var quickLinksItemNotFoundDialogBinding: QuickLinksItemNotFoundDialogBinding

    // List to store barcode edit information
    var barcodeEditList = mutableListOf<Triple<AppCompatImageView, String, String>>()

    // Counter for tracking operations or items
    private var counter: Int = 0

    // View Binding for BarcodeDetailItemRow layout
    private lateinit var barcodeDetailItemRowBinding: BarcodeDetailItemRowBinding

    // List to store multiple image URLs
    var multiImagesList = mutableListOf<String>()


    interface ScannerInterface {
        fun login(callback: LoginCallback)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // Ensure the context is an instance of AppCompatActivity
        if (context is AppCompatActivity) {
            mContext = context
        } else {
            throw ClassCastException("$context must be an instance of AppCompatActivity")
        }

        // Ensure the context implements ScannerInterface
        if (context is ScannerInterface) {
            listener = context
        } else {
            throw ClassCastException("$context must implement ScannerInterface")
        }

        // Initialize AppSettings and TableGenerator
        appSettings = AppSettings(requireActivity())
        tableGenerator = TableGenerator(requireActivity())
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the fragment's layout using ViewBinding
        binding = FragmentScannerBinding.inflate(inflater, container, false)

        // Initialize the views and setup any necessary listeners or data
        initViews()

        // Return the root view of the fragment
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve stored date from preferences
        val prefDate = DialogPrefs.getDate(requireContext())

        // If no date is stored, set the current date
        if (prefDate == null) {
            DialogPrefs.setDate(requireContext(), DateUtils.getCurrentDate())
        }

        // Retrieve scan count and shared QR status from preferences
        val scans = DialogPrefs.getSuccessScan(requireContext())
        val isSharedQr = DialogPrefs.getShared(requireContext())

        // Check if conditions are met to trigger the rate us dialog
        val shouldRateUs = getDateDifference() >= 3 && (scans >= 2 || isSharedQr)

        // Trigger the rate us dialog if the conditions are met
        if (shouldRateUs) {
            mContext?.let {
                rateUs(it)
            }
        }
    }


    private fun getDateDifference(): Int {
        // Initialize days to 0
        var days = 0

        // Define the date format for parsing
        val dateFormat = SimpleDateFormat(DateUtils.DATE_FORMAT, Locale.getDefault())

        // Get the current date and preferences date
        val currentDate = DateUtils.getCurrentDate()
        val prefsDate = DialogPrefs.getDate(requireContext())

        // Parse the current date and preferences date
        val parsedCurrentDate = dateFormat.parse(currentDate)
        val parsedPrefsDate = prefsDate?.let { dateFormat.parse(it) }

        // Check if both dates were successfully parsed
        if (parsedCurrentDate != null && parsedPrefsDate != null) {
            // Calculate the difference in milliseconds
            val differenceInMillis = parsedCurrentDate.time - parsedPrefsDate.time

            // Convert milliseconds to days
            days = TimeUnit.DAYS.convert(differenceInMillis, TimeUnit.MILLISECONDS).toInt()
        }

        // Log the date difference and parsed dates for debugging
        Log.d(TAG, "getDateDifference: $days days, Current Date: $currentDate, Preferences Date: $prefsDate")

        return days
    }


    /**
     * Initializes the views and sets up the click listeners.
     */
    private fun initViews() {

        // Set up a click listener for the "Add New Table" button.
        binding.addNewTableBtn.setOnClickListener {
            // Start the TablesActivity when the button is clicked.
            val intent = Intent(requireActivity(), TablesActivity::class.java)
            startActivity(intent)
        }

        // Set up a click listener for the "Connect to Google Sheets" TextView.
        binding.connectGoogleSheetsTextView.setOnClickListener {
            // Trigger the login process through the listener.
            listener?.login(object : LoginCallback {
                override fun onSuccess() {
                    // Log success message when login is successful.
                    Log.d("TEST199", "Login successful")
                    // Refresh the fragment or activity after successful login.
                    onResume()
                }
            })
        }
    }


    private fun getModeList() {
        // Retrieve the list of modes from resources
        val modeList = requireActivity().resources.getStringArray(R.array.mode_list)

        // Create an ArrayAdapter for the spinner with the mode list
        val adapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item,
            modeList
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Set the adapter to the spinner
        binding.modesSpinner.adapter = adapter

        // Retrieve the previously selected mode from shared preferences
        val savedModeIndex = appSettings.getString(requireActivity().getString(R.string.key_mode))?.toIntOrNull() ?: 0

        // Set the spinner selection based on the saved mode
        binding.modesSpinner.setSelection(
            modeList.indexOfFirst { it.toString() == savedModeIndex.toString() }.takeIf { it != -1 } ?: 0
        )

        // Update shared preferences when the selection changes
        binding.modesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {
                // No action needed when nothing is selected
            }

            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Save the selected mode index to shared preferences
                appSettings.putString(requireActivity().getString(R.string.key_mode), position.toString())
            }
        }
    }


    /**
     * Populates a spinner with a list of database table names and sets up a listener
     * to handle table selection changes. It also restores the previously selected
     * table if available in the app settings.
     */
    private fun getTableList() {
        // Retrieve the list of database tables
        val tablesList = tableGenerator.getAllDatabaseTables().toMutableList()

        // Check if there are tables to display
        if (tablesList.isNotEmpty()) {
            // Set the initial table name to the first table in the list
            tableName = tablesList[0]

            // Create and configure the adapter for the spinner
            val adapter = ArrayAdapter(
                requireActivity(),
                android.R.layout.simple_spinner_item,
                tablesList
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            binding.tablesSpinner.adapter = adapter

            // Restore the previously selected table from app settings
            val selectedTable = appSettings.getString("SCAN_SELECTED_TABLE")
            if (selectedTable!!.isNotEmpty()) {
                val position = tablesList.indexOf(selectedTable)
                if (position != -1) {
                    binding.tablesSpinner.setSelection(position)
                    tableName = selectedTable
                }
            }

            // Set up the spinner item selection listener
            binding.tablesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // No action needed when no item is selected
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    // Update the selected table name and save it in app settings
                    tableName = parent?.getItemAtPosition(position).toString()
                    appSettings.putString("SCAN_SELECTED_TABLE", tableName)
                }
            }
        }
    }


    private fun initMlScanner() {
        // Set up the initial visibility of UI elements
        binding.container.visibility = View.VISIBLE
        binding.scannerView.visibility = View.GONE

        // Handle flash toggle
        binding.flashImg.setOnClickListener { view: View? ->
            cam?.let {
                if (it.cameraInfo.hasFlashUnit()) {
                    isFlashOn = !isFlashOn
                    binding.flashImg.setImageResource(if (isFlashOn) R.drawable.ic_flash_on else R.drawable.ic_flash_off)
                    it.cameraControl.enableTorch(isFlashOn)
                    Log.d("TAG", "Flash toggled: $isFlashOn")
                }
            }
        }

        // Initialize the image analyzer and camera executor
        imageAnalyzer = MyImageAnalyzer(requireActivity().supportFragmentManager)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Set up the camera provider
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        (cameraProviderFuture as ListenableFuture<ProcessCameraProvider>).addListener({
            try {
                // Get the camera provider instance and bind the preview
                val processCameraProvider = (cameraProviderFuture as ListenableFuture<ProcessCameraProvider>).get()
                bindPreview(processCameraProvider)
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }


    private fun startScanner() {
        // Check if the camera permission is granted
        if (RuntimePermissionHelper.checkCameraPermission(
                requireActivity(),
                Constants.CAMERA_PERMISSION
            )
        ) {
            // Initialize CodeScanner if it is null
            if (codeScanner == null) {
                codeScanner = CodeScanner(requireActivity(), binding.scannerView)
            }

            codeScanner?.apply {
                // Set camera to use for scanning (back camera by default)
                camera = CodeScanner.CAMERA_BACK // or CodeScanner.CAMERA_FRONT or specific camera id

                // Set the barcode formats to scan (ALL_FORMATS will scan all supported formats)
                formats = CodeScanner.ALL_FORMATS

                // Set autofocus mode (SAFE recommended for better stability)
                autoFocusMode = AutoFocusMode.SAFE // or AutoFocusMode.CONTINUOUS

                // Set scanning mode (SINGLE to scan once, CONTINUOUS to keep scanning)
                scanMode = ScanMode.SINGLE // or ScanMode.CONTINUOUS or ScanMode.PREVIEW

                // Enable or disable auto focus
                isAutoFocusEnabled = true

                // Enable or disable the flash
                isFlashEnabled = false

                // Callback for when a barcode is successfully decoded
                decodeCallback = DecodeCallback { result ->
                    requireActivity().runOnUiThread {
                        // Search for the item in the table
                        val isFound = tableGenerator.searchItem(tableName, result.text)

                        // Check if the item is found and the mode is "0"
                        if (isFound && appSettings.getString(getString(R.string.key_mode)) == "0") {
                            // Get and update the scan quantity
                            val quantity = tableGenerator.getScanQuantity(tableName, result.text)
                            val qty = quantity.toInt() + 1
                            val isUpdate = tableGenerator.updateScanQuantity(tableName, result.text, qty)

                            // Show success message if the quantity was updated
                            if (isUpdate) {
                                Toast.makeText(
                                    requireActivity(),
                                    getString(R.string.scan_quantity_increase_success_text),
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Restart preview after a short delay
                                Handler(Looper.myLooper()!!).postDelayed({
                                    startPreview()
                                }, 2000)
                            }
                        } else {
                            // Show dialog if the item is not found or the mode is not "0"
                            displayDataSubmitDialog(result, "")
                        }
                    }
                }

                // Callback for errors during scanning
                errorCallback = ErrorCallback {
                    // Re-check camera permission and initialize ML scanner if necessary
                    if (RuntimePermissionHelper.checkCameraPermission(
                            requireActivity(),
                            Constants.CAMERA_PERMISSION
                        )
                    ) {
                        initMlScanner()
                    }
                    // Handle the error (currently empty)
                    requireActivity().runOnUiThread {
                        // Optionally handle error here
                    }
                }

                // Set onClickListener to restart the preview when the scanner view is clicked
                binding.scannerView.setOnClickListener {
                    startPreview()
                }

                // Start the scanning preview
                startPreview()
            }
        }
    }

    @SuppressLint("InvalidAnalyticsName")
    private fun displayDataSubmitDialog(result: Result?, scanText: String) {
        // Determine the text to use based on the presence of a result
        val text = result?.text ?: scanText

        // Trigger sound and vibration feedback
        playSound(true)
        generateVibrate()

        // Retrieve the current mode from app settings
        val mode = appSettings.getString(getString(R.string.key_mode))

        when (mode) {
            "1" -> handleModeOne(text)
            "2" -> handleModeTwo(text)
            else -> handleDefaultMode(text, result)
        }
    }

    private fun handleModeOne(text: String) {
        // Check if the item exists in the table
        if (tableGenerator.searchItem(tableName, text)) {
            // Update or delete item based on quantity
            val quantity = tableGenerator.getScanQuantity(tableName, text)
            var qty = quantity.toInt()

            if (qty > 0) {
                // Decrease quantity and update the item
                qty -= 1
                val isUpdated = tableGenerator.updateScanQuantity(tableName, text, qty)
                if (isUpdated) {
                    showToast(getString(R.string.scan_quantity_update_success_text) + " " +
                            getString(R.string.scan_quantity_remaining_text) + " $qty")
                    restartScannerAfterDelay(2000)
                }
            } else {
                // Delete item if quantity is zero
                val isDeleted = tableGenerator.deleteItem(tableName, text)
                if (isDeleted) {
                    showToast(getString(R.string.scan_item_delete_success_text))
                    restartScannerAfterDelay(2000)
                }
            }
        } else {
            showAlert(requireActivity(), getString(R.string.scan_item_not_found_text))
        }
    }

    private fun handleModeTwo(text: String) {
        // Retrieve item from the table
        val searchTableObject = tableGenerator.getScanItem(tableName, text)
        if (searchTableObject != null) {
            renderQuickLinksDialog(searchTableObject)
        } else {
            displayItemNotFoundDialog(text)
        }
    }

    private fun handleDefaultMode(text: String, result: Result?) {
        copyToClipBoard(text)

        // Check for valid barcode formats or non-empty scanText
        if (CodeScanner.ONE_DIMENSIONAL_FORMATS.contains(result?.barcodeFormat) || text.isNotEmpty()) {
            if (tableName.isEmpty()) {
                showAlert(requireActivity(), text)
            } else {
                setupScanResultDialog(text)
            }
        } else {
            logScanEvent(text)
            handleScanTextType(text,result)
        }
    }

    private fun setupScanResultDialog(text: String) {
        // Inflate and set up the scan result dialog view
        scanResultDialogBinding = ScanResultDialogBinding.inflate(
            LayoutInflater.from(requireActivity()),
            binding.root.parent as ViewGroup,
            false
        )

        setupCheckboxListener()
        setupImageClickListeners()

        val columns = tableGenerator.getTableColumns(tableName)
        columns?.forEach { value ->
            setupColumnView(value,text)
        }

        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setView(scanResultDialogBinding.root)
        builder.setCancelable(false)
        alert = builder.create()
        alert.show()

        showTooltipIfNeeded()
        scanResultDialogBinding.scanResultDialogSubmitBtn.setOnClickListener {
            alert.dismiss()
            saveToDriveAppFolder()
        }

        scanResultDialogBinding.scanResultDialogCancelBtn.setOnClickListener {
            alert.dismiss()
            restartScannerAfterDelay(500)
        }
    }

    private fun setupCheckboxListener() {
        scanResultDialogBinding.addImageCheckbox.setOnCheckedChangeListener { _, isChecked ->
            val visibility = if (isChecked) View.VISIBLE else View.GONE
            scanResultDialogBinding.severalImagesHintView.visibility = visibility
            scanResultDialogBinding.imageSourcesLayout.visibility = visibility
            scanResultDialogBinding.filePath.visibility = visibility

            if (isChecked && Constants.userData == null) {
                scanResultDialogBinding.addImageCheckbox.isChecked = false
                showLoginDialog { dialog, which ->
                    dialog?.dismiss()
                    scanResultDialogBinding.severalImagesHintView.visibility = View.GONE
                    scanResultDialogBinding.imageSourcesLayout.visibility = View.GONE
                    scanResultDialogBinding.filePath.visibility = View.GONE
                }
            }
        }
    }

    private fun showLoginDialog(dialogListener:DialogInterface.OnClickListener) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(getString(R.string.alert_text))
            .setMessage(getString(R.string.login_error_text))
            .setNegativeButton(getString(R.string.later_text)) { dialog, which ->
                dialogListener.onClick(dialog,which)
            }
            .setPositiveButton(getString(R.string.login_text)) { dialog, _ ->
                dialog.dismiss()
                listener?.login(object : LoginCallback {
                    override fun onSuccess() {
                        onResume()
                    }
                })
            }
            .create()
            .show()
    }

    private fun setupImageClickListeners() {
        scanResultDialogBinding.cameraImageView.setOnClickListener {
            if (RuntimePermissionHelper.checkCameraPermission(requireActivity(), Constants.CAMERA_PERMISSION)) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraResultLauncher.launch(cameraIntent)
            }
        }

        scanResultDialogBinding.imagesImageView.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getImageFromGallery()
            } else {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), Constants.READ_STORAGE_REQUEST_CODE)
            }
        }
    }

    private fun setupColumnView(value: String,scanText: String) {
        // Inflate table row layout and set up views
        val tableRowBinding = ScanResultTableRowLayoutBinding.inflate(
            LayoutInflater.from(requireContext()),
            binding.root.parent as ViewGroup,
            false
        )

        tableRowBinding.tableColumnName.text = value
        val pair = tableGenerator.getFieldList(value, tableName)

        when {
            value in listOf("id", "quantity") -> return
            value == "code_data" -> {
                textInputIdsList.add(Pair(value, tableRowBinding.tableColumnValue))
                tableRowBinding.tableColumnValue.setText(scanText)
            }
            pair != null -> handleFieldList(pair, value, tableRowBinding)
            value == "image" -> {
                textInputIdsList.add(Pair(value, tableRowBinding.tableColumnValue))
            }
            else -> setupDefaultField(value, tableRowBinding)
        }

        scanResultDialogBinding.tableDetailLayoutWrapper.addView(tableRowBinding.root)
    }

    private fun handleFieldList(pair: Pair<String, String>, value: String, tableRowBinding: ScanResultTableRowLayoutBinding) {
        val values = pair.first.split(",")
        val isListWithValues = pair.second == "listWithValues"

        if (values.isNotEmpty() && isListWithValues) {
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, values)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            tableRowBinding.tableColumnDropdown.adapter = adapter
            tableRowBinding.tableColumnDropdownLayout.visibility = View.VISIBLE
            tableRowBinding.tableColumnValue.visibility = View.GONE
            spinnerIdsList.add(Pair(value, tableRowBinding.tableColumnDropdown))
        } else {
            tableRowBinding.tableColumnDropdownLayout.visibility = View.GONE
            tableRowBinding.tableColumnValue.visibility = View.VISIBLE
            tableRowBinding.tableColumnValue.setText(pair.first)
            tableRowBinding.tableColumnValue.isEnabled = false
            tableRowBinding.tableColumnValue.isFocusable = false
            tableRowBinding.tableColumnValue.isFocusableInTouchMode = false
        }
    }

    private fun setupDefaultField(value: String, tableRowBinding: ScanResultTableRowLayoutBinding) {
        tableRowBinding.tableColumnDropdownLayout.visibility = View.GONE
        tableRowBinding.tableColumnValue.visibility = View.VISIBLE

        if (value == "date") {
            tableRowBinding.tableColumnValue.setText(BaseActivity.getDateTimeFromTimeStamp(System.currentTimeMillis()))
            tableRowBinding.tableColumnValue.isEnabled = false
            tableRowBinding.tableColumnValue.isFocusable = false
            tableRowBinding.tableColumnValue.isFocusableInTouchMode = false
        } else {
            tableRowBinding.tableColumnValue.isEnabled = true
            tableRowBinding.tableColumnValue.isFocusable = true
            tableRowBinding.tableColumnValue.isFocusableInTouchMode = true
            tableRowBinding.tableColumnValue.setText("")
        }
        textInputIdsList.add(Pair(value, tableRowBinding.tableColumnValue))
    }

    private fun showTooltipIfNeeded() {
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            val duration = appSettings.getLong("tt2")
            if (duration == 0L || System.currentTimeMillis() - duration > TimeUnit.DAYS.toMillis(1)) {
                SimpleTooltip.Builder(requireActivity())
                    .anchorView(scanResultDialogBinding.root)
                    .text(getString(R.string.after_scan_result_tip_text))
                    .gravity(Gravity.BOTTOM)
                    .animated(true)
                    .transparentOverlay(false)
                    .onDismissListener { tooltip ->
                        tooltip.dismiss()
                        appSettings.putLong("tt2", System.currentTimeMillis())
                        openAddImageTooltip(scanResultDialogBinding.addImageCheckbox, scanResultDialogBinding.scanResultDialogSubmitBtn)
                    }
                    .build()
                    .show()
            }
        }
    }

    private fun logScanEvent(text: String) {
        val bundle = Bundle().apply {
            putString("second scanner", "triggers")
        }
        mFirebaseAnalytics?.logEvent("scanner", bundle)
    }

    private fun handleScanTextType(text: String,result: Result?) {
        val type = when {
            text.contains("http") || text.contains("https") || text.contains("www") -> "link"
            text.isDigitsOnly() -> "number"
            text.contains("VCARD") || text.contains("vcard") -> "contact"
            text.contains("WIFI:") || text.contains("wifi:") -> "wifi"
            text.contains("tel:") -> "phone"
            text.contains("smsto:") || text.contains("sms:") -> "sms"
            text.contains("instagram") -> "instagram"
            text.contains("whatsapp") -> "whatsapp"
            else -> "text"
        }

        if (text.isNotEmpty()) {
            val qrHistory = CodeHistory(
                "qrmagicapp",
                System.currentTimeMillis().toString(),
                text,
                type,
                "free",
                if (CodeScanner.ONE_DIMENSIONAL_FORMATS.contains(result?.barcodeFormat)) "barcode" else "qr",
                "scan",
                "",
                "0",
                "",
                System.currentTimeMillis().toString(),
                ""
            )
            appViewModel.insert(qrHistory)
            saveSuccessScans()
            showToast(getString(R.string.scan_data_save_success_text))
            Handler(Looper.myLooper()!!).postDelayed({
                val intent = Intent(context, CodeDetailActivity::class.java)
                intent.putExtra("HISTORY_ITEM", qrHistory)
                requireActivity().startActivity(intent)
            }, 2000)
        }
    }

    private fun restartScannerAfterDelay(time: Long) {
        Handler(Looper.myLooper()!!).postDelayed({
            codeScanner?.startPreview()
        }, time)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }

   // START FROM HERE
   private fun renderQuickLinksDialog(searchTableObject: TableObject) {
       // Inflate the dialog layout using ViewBinding
       val dialogBinding = QuickLinksDialogLayoutBinding.inflate(
           LayoutInflater.from(requireActivity()),
           binding.root.parent as ViewGroup,
           false
       )

       // Set up the clipboard copy functionality
       dialogBinding.quickLinksCodeDetailClipboardCopyView.setOnClickListener {
           val clipboard = requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
           val clip = ClipData.newPlainText(
               // Use the existing label from the clipboard's primary clip description
               clipboard.primaryClipDescription?.label ?: "Copied Text",
               dialogBinding.quickLinksCodeDetailEncodeData.text.toString()
           )
           clipboard.setPrimaryClip(clip)
           Toast.makeText(
               requireActivity(),
               getString(R.string.text_saved_clipboard),
               Toast.LENGTH_SHORT
           ).show()
       }

       // Display the barcode details using the provided searchTableObject
       displayBarcodeDetail(searchTableObject)

       // Create and show the dialog
       val dialog = MaterialAlertDialogBuilder(requireActivity())
           .setView(dialogBinding.root)
           .setCancelable(false)
           .create()
       dialog.show()

       // Set up the cancel button to dismiss the dialog and restart the scanner preview
       dialogBinding.quickLinksCodeDetailCancel.setOnClickListener {
           dialog.dismiss()
           codeScanner?.startPreview()
       }

       // Set up the "More" button to open the CodeDetailActivity with the relevant data
       dialogBinding.quickLinksCodeDetailMoreButton.setOnClickListener {
           dialog.dismiss()
           val intent = Intent(requireActivity(), CodeDetailActivity::class.java).apply {
               putExtra("TABLE_NAME", tableName)
               putExtra("TABLE_ITEM", searchTableObject)
           }
           requireActivity().startActivity(intent)
       }
   }



    private fun displayItemNotFoundDialog(text: String) {
        // Inflate the dialog layout using ViewBinding
        val dialogBinding = QuickLinksItemNotFoundDialogBinding.inflate(
            LayoutInflater.from(requireActivity()),
            binding.root.parent as ViewGroup,
            false
        )

        // Fetch all database tables and populate the spinner with table names
        val tablesList = tableGenerator.getAllDatabaseTables().toMutableList()
        if (tablesList.isNotEmpty()) {
            tableName = tablesList[0] // Set default table name

            // Create an ArrayAdapter to manage the spinner's data
            val adapter = ArrayAdapter(
                requireActivity(),
                android.R.layout.simple_spinner_item,
                tablesList
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            dialogBinding.quickLinksTablesSpinner.adapter = adapter

            // Set spinner selection based on saved preference
            appSettings.getString("SCAN_SELECTED_TABLE")?.let { selectedTable ->
                if (selectedTable.isNotEmpty()) {
                    val selectedIndex = tablesList.indexOf(selectedTable)
                    if (selectedIndex >= 0) {
                        dialogBinding.quickLinksTablesSpinner.setSelection(selectedIndex)
                        tableName = selectedTable
                    }
                }
            }
        }

        // Handle spinner item selection events
        dialogBinding.quickLinksTablesSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(adapterView: AdapterView<*>?) {
                    // No action required
                }

                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    i: Int,
                    l: Long
                ) {
                    tableName = adapterView?.getItemAtPosition(i).toString()
                    appSettings.putString("SCAN_SELECTED_TABLE", tableName)
                }
            }

        // Create and display the alert dialog
        val alertDialog = MaterialAlertDialogBuilder(requireActivity())
            .setView(dialogBinding.root)
            .create()

        alertDialog.show()

        // Handle the "Select" button click event
        dialogBinding.quickLinksDialogSelectBtn.setOnClickListener {
            alertDialog.dismiss()
            val searchObj = tableGenerator.getScanItem(tableName, text)
            if (searchObj != null) {
                renderQuickLinksDialog(searchObj)
            } else {
                displayItemNotFoundDialog(text) // Recursively call the dialog if the item is not found
            }
        }

        // Handle the "Cancel" button click event
        dialogBinding.quickLinksDialogCancelBtn.setOnClickListener {
            alertDialog.dismiss()
            codeScanner?.startPreview() // Restart the scanner preview
        }
    }



    private fun displayBarcodeDetail(tableObject: TableObject) {
        // Clear previous views if any
        quickLinksDialogLayoutBinding.quickLinksBarcodeDetailWrapperLayout.removeAllViews()

        // Helper function to inflate a new row and add it to the layout
        fun addDetailRow(value: String, name: String) {
            val rowBinding = BarcodeDetailItemRowBinding.inflate(
                LayoutInflater.from(requireActivity()),
                quickLinksDialogLayoutBinding.root.parent as ViewGroup,
                false
            )
            counter += 1
            rowBinding.bcdEditView.id = counter
            rowBinding.bcdEditView.visibility = View.GONE
            rowBinding.bcdTableColumnValue.text = value
            rowBinding.bcdTableColumnName.text = name
            quickLinksDialogLayoutBinding.root.addView(rowBinding.root)

            // Add view to the list for further editing if needed
            barcodeEditList.add(
                Triple(
                    rowBinding.bcdEditView,
                    value,
                    name
                )
            )
        }

        // Adding ID row
        addDetailRow(tableObject.id.toString(), "id")

        // Adding Code Data row
        addDetailRow(tableObject.code_data, "code_data")

        // Adding Date row
        addDetailRow(tableObject.date, "date")

        // Adding Image row
        addDetailRow(tableObject.image, "image")

        // Adding dynamic columns
        for ((name, value) in tableObject.dynamicColumns) {
            addDetailRow(value, name)
        }

        // Reset the counter
        counter = 0
    }


    private fun saveToDriveAppFolder() {
        if (!BaseActivity.isNetworkAvailable(requireActivity())) {
            showNetworkErrorDialog()
            return
        }

        BaseActivity.dismiss()
        BaseActivity.startLoading(requireActivity())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val params = mutableListOf<Pair<String, String>>()

                // Check if the "Add Image" checkbox is checked and the file path is not empty
                if (scanResultDialogBinding.addImageCheckbox.isChecked && scanResultDialogBinding.filePath.text.toString().isNotEmpty()) {
                    // Upload the image to drive
                    val isUpload = uploadImageOnDrive()

                    if (isUpload && url.isNotEmpty()) {
                        prepareParamsWithImage(params)
                        saveDataToTable(params)
                        resetUIAfterSave()
                    }
                } else {
                    // Save data without an image
                    prepareParamsWithoutImage(params)
                    saveDataToTable(params)
                    resetUIAfterSave()
                }
            } catch (e: Exception) {
                logScanFailure(e)
            }
        }
    }

    /**
     * Shows a dialog when there is no network available.
     */
    private fun showNetworkErrorDialog() {
        MaterialAlertDialogBuilder(requireActivity())
            .setCancelable(true)
            .setTitle(requireActivity().resources.getString(R.string.alert_text))
            .setMessage(requireActivity().resources.getString(R.string.image_upload_internet_error_text))
            .setNegativeButton(requireActivity().resources.getString(R.string.close_text)) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(requireActivity().resources.getString(R.string.save_without_image_text)) { dialog, _ ->
                dialog.dismiss()
                alert.dismiss()
                val params = mutableListOf<Pair<String, String>>()
                prepareParamsWithoutImage(params)
                saveDataToTable(params)
                resetUIAfterSave()
            }
            .show()
            .getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireActivity(), R.color.purple_700))
    }

    /**
     * Prepares parameters for saving data including the image URL.
     */
    private fun prepareParamsWithImage(params: MutableList<Pair<String, String>>) {
        multiImagesList.clear()
        params.clear()

        // Add data from dynamically generated EditTexts
        for (pair in textInputIdsList) {
            val value = if (pair.first == "image") url else pair.second.text.toString().trim()
            params.add(Pair(pair.first, value))
        }

        // Add data from dynamically generated Dropdowns
        for (pair in spinnerIdsList) {
            params.add(Pair(pair.first, pair.second.selectedItem.toString()))
        }
    }

    /**
     * Prepares parameters for saving data without the image URL.
     */
    private fun prepareParamsWithoutImage(params: MutableList<Pair<String, String>>) {
        // Add data from dynamically generated EditTexts
        for (pair in textInputIdsList) {
            params.add(Pair(pair.first, pair.second.text.toString().trim()))
        }

        // Add data from dynamically generated Dropdowns
        for (pair in spinnerIdsList) {
            params.add(Pair(pair.first, pair.second.selectedItem.toString()))
        }
    }

    /**
     * Saves data to the specified table.
     */
    private fun saveDataToTable(params: List<Pair<String, String>>) {
        tableGenerator.insertData(tableName, params)
    }

    /**
     * Resets the UI after successfully saving the data.
     */
    private fun resetUIAfterSave() {
        CoroutineScope(Dispatchers.Main).launch {
            Handler(Looper.myLooper()!!).postDelayed({
                isFileSelected = false
                BaseActivity.dismiss()
                saveSuccessScans()
                Toast.makeText(requireActivity(), requireActivity().resources.getString(R.string.scan_data_save_success_text), Toast.LENGTH_SHORT).show()
                textInputIdsList.clear()
                spinnerIdsList.clear()
                scanResultDialogBinding.tableDetailLayoutWrapper.removeAllViews()
                scanResultDialogBinding.filePath.setText("")
                codeScanner?.startPreview()
                openHistoryBtnTip()
                mFirebaseAnalytics?.logEvent("scanner", Bundle().apply { putString("success", "success") })
            }, 1000)
        }
    }

    /**
     * Logs the scan failure event and exception.
     */
    private fun logScanFailure(e: Exception) {
        mFirebaseAnalytics?.logEvent("scanner", Bundle().apply { putString("failure", "Error: ${e.message}") })
        e.printStackTrace()
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
                    .anchorView((requireActivity() as MainActivity).contentBinding.historyBtn)
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


    @SuppressLint("InvalidAnalyticsName")
    private fun uploadImageOnDrive(): Boolean {
        var isUploadingSuccess = false
        val imageList = scanResultDialogBinding.filePath.text.toString()

        // Split image paths if there are multiple paths separated by commas
        val imagePaths = if (imageList.contains(",")) {
            imageList.split(",")
        } else {
            listOf(imageList)
        }

        for (imagePath in imagePaths) {
            // Log event to Firebase Analytics before starting the upload process
            logFirebaseEvent("upload image", "starts")

            try {
                // Create metadata for the image file
                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = "Image_${System.currentTimeMillis()}.jpg"
                }

                // Create a File object for the image and prepare it for upload
                val filePath = File(imagePath)
                val mediaContent = FileContent("image/jpeg", filePath)

                // Upload the file to Google Drive
                val uploadedFile = DriveService.getDriveInstance()!!.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()

                // Generate the URL for the uploaded file and add it to the list
                val url = "https://drive.google.com/file/d/${uploadedFile.id}/view?usp=sharing"
                uploadedUrlList.add(url)

                // Log a success event to Firebase Analytics
                logFirebaseEvent("upload image", "success")
                isUploadingSuccess = true

            } catch (e: UserRecoverableAuthIOException) {
                // Handle UserRecoverableAuthIOException and launch the recovery intent
                userRecoverableAuthType = 0
                userAuthLauncher.launch(e.intent)

                logFirebaseEvent("upload image", "UserRecoverableAuthIOException", e.message)
                isUploadingSuccess = false

            } catch (e: GoogleJsonResponseException) {
                // Handle GoogleJsonResponseException and show an alert dialog
                logFirebaseEvent("upload image", "GoogleJsonResponseException", e.message)
                showAlert(requireActivity(), e.details.message)
                isUploadingSuccess = false
            }
        }

        // If uploading was successful, join URLs into a single string and clear the list
        if (isUploadingSuccess) {
            url = uploadedUrlList.joinToString(",")
            uploadedUrlList.clear()
        }

        return isUploadingSuccess
    }

    // Helper function to log events to Firebase Analytics
    private fun logFirebaseEvent(eventName: String, key: String, value: String? = null) {
        val bundle = Bundle().apply {
            putString(key, value ?: key)
        }
        mFirebaseAnalytics?.logEvent(eventName, bundle)
    }


    // THIS GOOGLE LAUNCHER WILL HANDLE RESULT
    // Initialize the launcher for activity result handling
    private val userAuthLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // Check if the result is OK
        if (result.resultCode == Activity.RESULT_OK) {
            // Launch the account chooser using the MainActivity's credential
            val mainActivity = requireActivity() as MainActivity
            val chooseAccountIntent = mainActivity.credential?.newChooseAccountIntent()

            // Ensure the intent is not null before launching
            chooseAccountIntent?.let {
                chooseAccountLauncher.launch(it)
            }
        }
    }



    private var chooseAccountLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        // Check if the result is OK
        if (result.resultCode == Activity.RESULT_OK) {
            // Extract the account name from the result data
            val accountName: String? = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)

            // If the account name is not null, proceed with the following operations
            accountName?.let {
                // Set the selected account name in the credential
                (requireActivity() as MainActivity).credential?.selectedAccountName = it

                // Save the account name in the app settings
                appSettings.putString("ACCOUNT_NAME", it)

                // Depending on the userRecoverableAuthType value, perform the appropriate action
                if (userRecoverableAuthType == 0) {
                    saveToDriveAppFolder()
                } else {
                    // getAllSheets() can be invoked here if needed
                }
            }
        }
    }


    private fun createImageFile(bitmap: Bitmap) {
        // Save the bitmap as an image file and get the file path
        currentPhotoPath = ImageManager.readWriteImage(requireActivity(), bitmap).absolutePath

        // Add the file path to the list of selected images
        multiImagesList.add(currentPhotoPath!!)

        // Display the list of image file paths in the dialog's TextView, separated by commas
        scanResultDialogBinding.filePath.text = multiImagesList.joinToString(",")

        // Set the flag indicating that a file has been selected
        isFileSelected = true
    }


    // Function to launch the gallery intent for selecting multiple images
    private fun getImageFromGallery() {
        // Create an intent to pick images from the gallery
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*" // Restrict to image files only
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Allow multiple image selection
        }
        // Launch the intent using the result launcher
        resultLauncher.launch(intent)
    }




    // Registering a result launcher for handling image selection
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // Check if the image selection was successful
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val clipData: ClipData? = data?.clipData

                // If multiple images are selected
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val imageUri = clipData.getItemAt(i).uri
                        // Add the real path of the image to the list
                        multiImagesList.add(
                            ImageManager.getRealPathFromUri(
                                requireActivity(),
                                imageUri
                            ) ?: continue // Continue if the path is null
                        )
                    }
                } else {
                    // If a single image is selected
                    data?.data?.let { imageUri ->
                        multiImagesList.add(
                            ImageManager.getRealPathFromUri(
                                requireActivity(),
                                imageUri
                            ) ?: return@let // Return if the path is null
                        )
                    }
                }

                // Update the file path text with the selected image paths
                scanResultDialogBinding.filePath.text = multiImagesList.joinToString(",")
                isFileSelected = true // Mark that a file has been selected
            }
        }


    // THIS RESULT LAUNCHER WILL CALL THE ACTION PICK FROM FILES FOR BACKGROUND AND LOGO IMAGE
    private val cameraResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        // Check if the image capture was successful
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data

            // Check if data is not null and contains the image
            data?.extras?.get("data")?.let { imageData ->
                val bitmap = imageData as Bitmap
                // Create an image file from the bitmap
                createImageFile(bitmap)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Initialize or restart necessary components
        startScanner()
        getTableList()
        getModeList()
        // Uncomment if needed
        // getAllSheets()

        // Retrieve the flag from app settings and update UI accordingly
        val tipsEnabled = appSettings.getBoolean(getString(R.string.key_tips))
        updateTipsSwitch(tipsEnabled)

        // Set up listener for the switch
        binding.homeTipsSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Update switch text and save new state in app settings
            updateTipsSwitch(isChecked)
            appSettings.putBoolean(getString(R.string.key_tips), isChecked)
        }
    }

    /**
     * Updates the text of the tips switch based on its state.
     *
     * @param isEnabled Boolean value indicating whether tips are enabled or not.
     */
    private fun updateTipsSwitch(isEnabled: Boolean) {
        val switchText = if (isEnabled) {
            getString(R.string.tip_switch_on_text)
        } else {
            getString(R.string.tip_switch_off_text)
        }
        // Set text and checked state of the switch
        binding.homeTipsSwitch.setText(switchText)
        binding.homeTipsSwitch.isChecked = isEnabled
    }



    override fun onPause() {
        super.onPause()  // Call the superclass's onPause() method first

        // Safely release resources if codeScanner is not null
        codeScanner?.releaseResources()
    }


    // THIS FUNCTION WILL HANDLE THE RUNTIME PERMISSION RESULT
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Handle the result of the permission requests
        when (requestCode) {
            Constants.CAMERA_REQUEST_CODE -> {
                handlePermissionResult(
                    grantResults,
                    Constants.CAMERA_PERMISSION,
                    R.string.camera_permission_failed_text,
                    ::startScanner
                )
            }

            Constants.READ_STORAGE_REQUEST_CODE -> {
                handlePermissionResult(
                    grantResults,
                    Constants.READ_STORAGE_PERMISSION,
                    R.string.external_storage_permission_error2,
                    ::getImageFromGallery
                )
            }

            else -> {
                // Handle other permissions if necessary
            }
        }
    }

    /**
     * Handles the result of permission requests.
     *
     * @param grantResults The grant results for the corresponding permissions.
     * @param permission The permission string to check.
     * @param errorMessageResId The resource ID of the error message string.
     * @param onPermissionGranted Action to perform if the permission is granted.
     */
    private fun handlePermissionResult(
        grantResults: IntArray,
        permission: String,
        errorMessageResId: Int,
        onPermissionGranted: () -> Unit
    ) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, execute the corresponding action
            onPermissionGranted()
        } else {
            // Permission denied, show appropriate message
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission)) {
                RuntimePermissionHelper.checkStoragePermission(requireActivity(), permission)
            } else {
                MaterialAlertDialogBuilder(requireActivity())
                    .setMessage(requireActivity().getString(errorMessageResId))
                    .setCancelable(false)
                    .setPositiveButton(requireActivity().getString(R.string.ok_text)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create().show()
            }
        }
    }

    private var player: MediaPlayer? = null

    /**
     * Plays a sound based on the success flag.
     * @param isSuccess A boolean flag indicating whether to play the success sound or error sound.
     */
    private fun playSound(isSuccess: Boolean) {
        // Retrieve the sound setting from app settings
        val isSounding = appSettings.getBoolean(requireContext().getString(R.string.key_sound))

        // Check if sound is enabled in settings
        if (isSounding) {
            // Release the previous MediaPlayer instance if it exists
            player?.release()

            // Initialize MediaPlayer with the appropriate sound resource
            player = MediaPlayer.create(requireContext(),
                if (isSuccess) R.raw.succes_beep else R.raw.error_beep)

            // Start playing the sound
            player?.start()

            // Optionally, release the MediaPlayer after the sound has finished playing
            player?.setOnCompletionListener {
                player?.release()
                player = null
            }
        }
    }


    // This function triggers vibration based on the user's app settings.
// It uses ViewBinding to access the context and settings.

    private fun generateVibrate() {
        // Retrieve vibration setting from app preferences
        val isVibrate = appSettings.getBoolean(getString(R.string.key_vibration))

        // Check if vibration is enabled
        if (isVibrate) {
            // Get the Vibrator service from the system
            val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            // Check the Android version and apply appropriate vibration method
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // For Android O and above, use VibrationEffect for more control over vibration
                val vibrationEffect = VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                // For Android versions below O, use the legacy vibrate method
                vibrator.vibrate(150)
            }
        }
    }


    // Function to copy content to the clipboard and show a toast message
    private fun copyToClipBoard(content: String) {
        // Check if copying to clipboard is allowed based on app settings
        val isAllowCopy = appSettings.getBoolean(getString(R.string.key_clipboard))
        if (isAllowCopy) {
            // Get the ClipboardManager service
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // Create a new ClipData object with the content to be copied
            val clip = ClipData.newPlainText("Scan code", content)
            // Set the primary clip to the clipboard
            clipboard.setPrimaryClip(clip)
            // Show a toast message indicating that the content has been copied
            Toast.makeText(requireContext(), "Copied", Toast.LENGTH_LONG).show()
        }
    }

    private fun bindPreview(processCameraProvider: ProcessCameraProvider) {
        // Create a Preview use case for displaying camera preview.
        val preview = Preview.Builder().build().apply {
            // Set the surface provider for the preview use case.
            setSurfaceProvider(binding.previewview.surfaceProvider)
        }

        // Define the CameraSelector to use the back camera.
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        // Create an ImageCapture use case for capturing photos.
        val imageCapture = ImageCapture.Builder().build()

        // Create an ImageAnalysis use case for analyzing images from the camera.
        val imageAnalysis = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ImageAnalysis.Builder()
                .setTargetResolution(Size(1200, 720)) // Set target resolution for analysis.
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // Handle backpressure strategy.
                .build().apply {
                    // Set the analyzer if the camera executor and analyzer are available.
                    cameraExecutor?.let { executor ->
                        imageAnalyzer?.let { analyzer ->
                            setAnalyzer(executor, analyzer)
                        }
                    }
                }
        } else {
            TODO("VERSION.SDK_INT < LOLLIPOP") // Placeholder for SDK versions below Lollipop.
        }

        // Unbind all use cases before rebinding.
        processCameraProvider.unbindAll()

        // Bind the use cases to the lifecycle of this activity/fragment.
        cam = processCameraProvider.bindToLifecycle(
            this, // LifecycleOwner (Activity/Fragment)
            cameraSelector,
            preview,
            imageCapture,
            imageAnalysis
        )
    }


    inner class MyImageAnalyzer(private val supportFragmentManager: FragmentManager) : ImageAnalysis.Analyzer {

        // Keeps track of the number of barcodes processed
        private var count = 0

        // ViewModel instance
        private val appViewModel: AppViewModel by lazy {
            ViewModelProvider(requireActivity()).get(AppViewModel::class.java)
        }

        // Initialize the FragmentManager
        init {
            // No additional initialization required for now
        }

        // Analyze the image for barcodes
        override fun analyze(image: ImageProxy) {
            scanBarCode(image)
        }

        // Function to process barcode scanning
        private fun scanBarCode(image: ImageProxy) {
            @SuppressLint("UnsafeOptInUsageError") val mediaImage = image.image!!
            val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)

            // Set up barcode scanner options
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

            // Initialize the barcode scanner client
            val scanner = BarcodeScanning.getClient(options)

            // Process the image for barcodes
            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    // Handle successful barcode scan
                    readBarCodeData(barcodes)
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                    Log.e("MyImageAnalyzer", "Barcode scan failed", exception)
                }
                .addOnCompleteListener {
                    // Close the image after processing
                    image.close()
                }
        }

        // Function to handle barcode data
        private fun readBarCodeData(barcodes: List<Barcode>) {
            if (barcodes.isNotEmpty()) {
                count++
                if (count == 1) {
                    // Extract raw value and log it
                    val rawValue = barcodes[0].rawValue
                    Log.d("TEST199", "readBarCodeData: $rawValue")

                    // Display dialog with the barcode data
                    if (rawValue != null) {
                        displayDataSubmitDialog(null, rawValue)
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
                    .anchorView(binding.addNewTableBtn)
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

    // Function to increment and save the count of successful scans
    private fun saveSuccessScans() {
        // Retrieve the current count of successful scans from shared preferences
        var scans = DialogPrefs.getSuccessScan(requireActivity())

        // Check if the retrieved value is non-negative
        if (scans >= 0) {
            // Increment the count
            scans += 1

            // Save the updated count back to shared preferences
            DialogPrefs.setSuccessScan(requireActivity(), scans)
        }

        // Log the updated scan count for debugging purposes
        Log.d("TAG", "ScanCount: $scans")
    }

//    private fun getAllSheets() {
//        if (Constants.userData != null) {
//
//            CoroutineScope(Dispatchers.IO).launch {
//                try {
//                    val result: FileList = DriveService.getDriveInstance()!!.files().list()
//                        .setQ("mimeType='application/vnd.google-apps.spreadsheet'")
//                        .execute()
//
//                    val files = result.files
//
//                    if (files != null) {
//                        if (files.size > 0) {
//                            sheetsList.clear()
//                        }
//                        for (file in files) {
//                            sheetsList.add(Sheet(file.id, file.name))
//                        }
//
//                        CoroutineScope(Dispatchers.Main).launch {
//                            if (sheetsList.isNotEmpty()) {
//                                Constants.sheetsList.addAll(sheetsList)
//                                displaySheetSpinner()
//                            }
//                        }
//                    }
//                } catch (userRecoverableException: UserRecoverableAuthIOException) {
//                    userRecoverableAuthType = 1
//                    userAuthLauncher.launch(userRecoverableException.intent)
//                }
//            }
//        } else {
//
//        }
//    }

//    private fun displaySheetSpinner() {
//        if (sheetsList.isNotEmpty()) {
//            selectedSheetId = sheetsList[0].id
//            selectedSheetId = sheetsList[0].name
//            val adapter = ArrayAdapter(
//                requireActivity(),
//                android.R.layout.simple_spinner_item,
//                sheetsList
//            )
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            binding.sheetsSpinner.adapter = adapter
//
//            if (appSettings.getString("SELECTED_SHEET")!!.isNotEmpty()) {
//                for (i in 0 until sheetsList.size) {
//                    val value = sheetsList[i].id
//                    if (value == appSettings.getString("SELECTED_SHEET")) {
//                        binding.sheetsSpinner.setSelection(i)
//                        selectedSheetId = value
//                        selectedSheetName = sheetsList[i].name
//                        break
//                    }
//                }
//            }
//        }
//
//        binding.sheetsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onNothingSelected(adapterView: AdapterView<*>?) {
//
//            }
//
//            override fun onItemSelected(
//                adapterView: AdapterView<*>?,
//                view: View?,
//                i: Int,
//                l: Long
//            ) {
//                selectedSheetId = sheetsList[i].id//adapterView!!.getItemAtPosition(i).toString()
//                selectedSheetName = sheetsList[i].name
//                appSettings.putString("SELECTED_SHEET", selectedSheetId)
//            }
//        }
//    }

//    var values_JSON = JSONArray()
//    private fun sendRequest() {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//
//                val sr: StringRequest = object : StringRequest(
//                    Method.POST,
//                    Constants.googleAppScriptUrl,
//                    object : Response.Listener<String?> {
//                        override fun onResponse(response: String?) {
//                            CoroutineScope(Dispatchers.Main).launch {
//                                if (response!!.toLowerCase(Locale.ENGLISH).contains("success")) {
//                                    Log.d("TEST199", "sheet data success")
//                                } else {
//                                    val permissionDeniedLayout = LayoutInflater.from(context)
//                                        .inflate(
//                                            R.layout.spreadsheet_permission_failed_dialog,
//                                            null
//                                        )
//                                    val builder = MaterialAlertDialogBuilder(requireActivity())
//                                    builder.setCancelable(false)
//                                    builder.setView(permissionDeniedLayout)
//                                    builder.setPositiveButton("Ok") { dialog, which ->
//                                        dialog.dismiss()
//                                    }
//                                    val alert = builder.create()
//                                    alert.show()
//                                }
//                                values_JSON = JSONArray()
//
//                            }
//                        }
//                    },
//                    object : Response.ErrorListener {
//                        override fun onErrorResponse(error: VolleyError?) {
//                            Toast.makeText(context, error!!.toString(), Toast.LENGTH_SHORT).show()
//                            BaseActivity.dismiss()
//                        }
//                    }) {
//
//                    override fun getBodyContentType(): String {
//                        return "application/x-www-form-urlencoded"
//                    }
//
//                    override fun getParams(): Map<String, String> {
//                        val params: MutableMap<String, String> = HashMap()
//                        params["sheetName"] = selectedSheetName
//                        params["number"] = "${values_JSON.length()}"
//                        params["id"] = selectedSheetId
//                        params["value"] = "$values_JSON"
//                        return params
//                    }
//
//                }
//                sr.setRetryPolicy(
//                    DefaultRetryPolicy(
//                        10000,
//                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//                    )
//                )
//                VolleySingleton(requireActivity()).addToRequestQueue(sr)
//
//            } catch (e: UserRecoverableAuthIOException) {
//                e.printStackTrace()
//            } catch (e: java.lang.Exception) {
//                e.printStackTrace()
//            }
//        }
//    }

    fun restart() {
        onResume()
    }

}