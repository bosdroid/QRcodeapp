package com.expert.qrgenerator.view.activities

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.expert.qrgenerator.R
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.RuntimePermissionHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ScannerActivity : BaseActivity() {

    private lateinit var context: Context
    private var codeScanner: CodeScanner?=null
    private lateinit var scannerView:CodeScannerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        initViews()

        if (RuntimePermissionHelper.checkCameraPermission(
                context,
                Constants.CAMERA_PERMISSION
            )
        ) {
            startScanner()
        }
    }

    private fun initViews(){
        context = this
        scannerView = findViewById(R.id.scanner_view)
    }

    private fun startScanner(){
        codeScanner = CodeScanner(this, scannerView)

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
                runOnUiThread {
//                    Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_LONG).show()
                    showAlert(context,"Scan result: ${it.text}")
                    startPreview()
                }
            }
            errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
                runOnUiThread {
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

    override fun onResume() {
        super.onResume()
//            codeScanner.startPreview()

    }

    override fun onPause() {
        if (codeScanner != null){
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
                   if (ActivityCompat.shouldShowRequestPermissionRationale(this,Constants.CAMERA_PERMISSION)){
                      RuntimePermissionHelper.checkPermission(context,Constants.CAMERA_PERMISSION)
                   }
                    else{
                       MaterialAlertDialogBuilder(context)
                           .setMessage("Please allow the Camera permission to use the scanner to scan Image.")
                           .setCancelable(false)
                           .setPositiveButton("Ok") { dialog, which ->
                               dialog.dismiss()
                               onBackPressed()
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