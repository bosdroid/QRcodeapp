package com.expert.qrgenerator.view.fragments

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModelProvider
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.expert.qrgenerator.R
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.RuntimePermissionHelper
import com.expert.qrgenerator.view.activities.CodeDetailActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class ScannerFragment : Fragment() {

    private var codeScanner: CodeScanner? = null
    private lateinit var scannerView: CodeScannerView
    private lateinit var appViewModel: AppViewModel

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
        val v = inflater.inflate(R.layout.fragment_scanner, container, false)

        initViews(v)

        return v
    }


    private fun initViews(view: View) {
        scannerView = view.findViewById(R.id.scanner_view)
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
                        var qrHistory:CodeHistory?=null
                        val type = if (text.contains("http") || text.contains("https") || text.contains("www")){
                            "link"
                        }
                        else if(text.isDigitsOnly()){
                            "number"
                        }
                        else if(text.contains("VCARD") || text.contains("vcard")){
                            "contact"
                        }
                        else if(text.contains("WIFI:") || text.contains("wifi:")){
                            "wifi"
                        }
                        else if(text.contains("tel:")){
                            "phone"
                        }
                        else if(text.contains("smsto:") || text.contains("sms:")){
                            "sms"
                        }
                        else if(text.contains("instagram")){
                            "instagram"
                        }
                        else if(text.contains("whatsapp")){
                            "whatsapp"
                        }
                        else{
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
                            Toast.makeText(
                                requireActivity(),
                                "Scan data saved successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            Handler(Looper.myLooper()!!).postDelayed({
                                val intent = Intent(context, CodeDetailActivity::class.java)
                                intent.putExtra("HISTORY_ITEM",qrHistory)
                                requireActivity().startActivity(intent)
                            },2000)
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