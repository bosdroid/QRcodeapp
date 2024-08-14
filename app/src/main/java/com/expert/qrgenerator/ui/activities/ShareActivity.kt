package com.expert.qrgenerator.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.ActivityShareBinding
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.DialogPrefs
import com.expert.qrgenerator.utils.QRGenerator
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShareActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding:ActivityShareBinding
    private lateinit var context: Context
    private var imageShareUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShareBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setUpToolbar()
    }

    private fun initViews() {
        context = this

        binding.shareBtn.setOnClickListener(this)
        binding.startNew.setOnClickListener(this)

        if (Constants.finalQrImageUri != null) {
            imageShareUri = Constants.finalQrImageUri
            binding.shareQrGeneratedImg.setImageURI(Constants.finalQrImageUri)
        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = getString(R.string.share_qr_image)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            // SHARE BTN WILL CALL THE SHARE IMAGE FUNCTION
            R.id.share_btn -> {
                DialogPrefs.setShared(context, true)
                MaterialAlertDialogBuilder(context)
                    .setMessage(getString(R.string.qr_image_share_warning_text))
                    .setCancelable(false)
                    .setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.share_text)) { dialog, which ->
//                      ImageManager.shareImage(context,imageShareUri)
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        if (imageShareUri != null) {
                            shareIntent.type = "image/*"
                            shareIntent.putExtra(Intent.EXTRA_STREAM, imageShareUri)
                            shareResultLauncher.launch(
                                Intent.createChooser(
                                    shareIntent,
                                    "Share with"
                                )
                            )
                        }
                    }
                    .create().show()
            }
            R.id.start_new -> {
                QRGenerator.resetQRGenerator()
                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("KEY","generator")
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            else -> {

            }
        }
    }

    // THIS SHARE IMAGE LAUNCHER WILL HANDLE AFTER SHARING QR IMAGE
    private var shareResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
//                val intent = Intent(context,MainActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
//                startActivity(intent)
//                finish()
//            }
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    }
}