package com.expert.qrgenerator.view.fragments

import android.content.*
import android.content.Context.VIBRATOR_SERVICE
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.budiyev.android.codescanner.*
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
    private lateinit var prefs: SharedPreferences
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
        prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity())
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
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(), "Camera initialization error: ${it.message}",
                            Toast.LENGTH_LONG
                        ).show()
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
}