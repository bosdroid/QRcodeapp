package com.expert.qrgenerator.view.fragments

import android.annotation.SuppressLint
import android.content.*
import android.content.Context.VIBRATOR_SERVICE
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.budiyev.android.codescanner.*
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentScannerBinding
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.RuntimePermissionHelper
import com.expert.qrgenerator.view.activities.CodeDetailActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class ScannerFragment : Fragment() {

    private var codeScanner: CodeScanner? = null
    private lateinit var scannerView: CodeScannerView
    private lateinit var appViewModel: AppViewModel
    private lateinit var prefs: SharedPreferences

    //
    private var cameraProviderFuture: ListenableFuture<*>? = null
    private var cameraExecutor: ExecutorService? = null
    private var previewView: PreviewView? = null
    private var imageAnalyzer: MyImageAnalyzer? = null
    private var isFlashOn = false
    private lateinit var mBinding: FragmentScannerBinding
    private lateinit var cam: Camera

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(AppViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentScannerBinding.inflate(inflater, container, false);
//        val v = inflater.inflate(R.layout.fragment_scanner, container, false)
        prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        initViews()

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        if (RuntimePermissionHelper.checkCameraPermission(
//                requireActivity(),
//                Constants.CAMERA_PERMISSION
//            )
//        ) {
//            initMlScanner()
//        }

    }

    private fun initMlScanner() {
        requireActivity().window.setFlags(1024, 1024)
        mBinding.container.visibility = View.VISIBLE
        mBinding.scannerView.visibility = View.GONE
        mBinding.flashImg.setOnClickListener { view: View? ->
            if (cam != null) {
                Log.d("TAG", "initMlScanner: ")
                if (cam.cameraInfo.hasFlashUnit()) {
                    if (isFlashOn) {
                        isFlashOn = false
                        mBinding.flashImg.setImageResource(R.drawable.ic_flash_off)
                        cam.cameraControl.enableTorch(isFlashOn)
                    } else {

                        isFlashOn = true
                        mBinding.flashImg.setImageResource(R.drawable.ic_flash_on)
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


    private fun initViews() {
        mBinding.container.visibility = View.GONE
        mBinding.scannerView.visibility = View.VISIBLE
//        scannerView = view.findViewById(R.id.scanner_view)
    }

    private fun startScanner() {
        if (RuntimePermissionHelper.checkCameraPermission(
                requireActivity(),
                Constants.CAMERA_PERMISSION
            )
        ) {

            if (codeScanner == null) {
                codeScanner = CodeScanner(requireActivity(), mBinding.scannerView)
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
                        var qrHistory: CodeHistory? = null
                        val type =
                            if (text.contains("http") || text.contains("https") || text.contains("www")) {
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
                                    0,
                                    "",
                                    System.currentTimeMillis()
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
                                    0,
                                    "",
                                    System.currentTimeMillis()
                                )
                                appViewModel.insert(qrHistory)
                            }
                            playSound(true)
                            generateVibrate()
                            copyToClipBoard(text)
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

                        if (it.text == null) {

                            playSound(false)
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
                        Toast.makeText(
                            requireContext(), "Camera initialization error: ${it.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                mBinding.scannerView.setOnClickListener {
                    startPreview()
                }
                startPreview()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startScanner()

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

    private fun playSound(isSuccess: Boolean) {
        val isSounding = prefs.getBoolean(requireContext().getString(R.string.key_sound), false)
        if (isSounding) {
            var player: MediaPlayer? = null
            if (isSuccess) {
                player = MediaPlayer.create(requireContext(), R.raw.succes_beep)
            } else {
                player = MediaPlayer.create(requireContext(), R.raw.error_beep)

            }
            player.start()
        }
    }

    @SuppressLint("MissingPermission")
    private fun generateVibrate() {
        val isVibrate = prefs.getBoolean(requireContext().getString(R.string.key_vibration), false)
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
            prefs.getBoolean(requireContext().getString(R.string.key_clipboard), false)
        if (isAllowCopy) {
            val clipboard = requireContext()
                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Scan code", content)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "copied", Toast.LENGTH_LONG).show()
        }
    }

    //
    private fun bindPreview(processCameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(mBinding.previewview.surfaceProvider)
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        val imageCapture = ImageCapture.Builder().build()


        val imageAnalysis =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
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

                        var qrHistory: CodeHistory? = null
                        qrHistory = CodeHistory(
                            "sattar",
                            "${System.currentTimeMillis()}",
                            rawValue,
                            valueType.toString(),
                            "free",
                            "qr",
                            "scan",
                            "",
                            0,
                            "",
                            System.currentTimeMillis()
                        )
                        appViewModel.insert(qrHistory)
                        playSound(true)
                        generateVibrate()
                        copyToClipBoard(rawValue)
                        Toast.makeText(
                            requireActivity(),
                            "Scan data saved successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        Handler(Looper.myLooper()!!).postDelayed({

                            val intent = Intent(context, CodeDetailActivity::class.java)
                            intent.putExtra("HISTORY_ITEM", qrHistory)
                            requireActivity().startActivity(intent)
                            count = 0
                        }, 3000)

                    }
                }

            }
        }
    }
}
