package com.expert.qrgenerator.ui.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.ActivityShareBinding
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.DialogPrefs
import com.expert.qrgenerator.utils.GeneratorManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShareActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityShareBinding
    // Context of the activity, initialized using lazy delegation
    private val context: Context by lazy { this }
    private var imageShareUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShareBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views and set up toolbar
        initViews()
        setUpToolbar()
    }

    private fun initViews() {

        // Set click listeners for buttons
        binding.shareBtn.setOnClickListener(this)
        binding.startNew.setOnClickListener(this)

        // Load the QR image URI if it is available
        imageShareUri = Constants.finalQrImageUri
        imageShareUri?.let {
            binding.shareQrGeneratedImg.setImageURI(it)
        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.share_qr_image)
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.share_btn -> handleShareButtonClick()
            R.id.start_new -> handleStartNewButtonClick()
        }
    }

    private fun handleShareButtonClick() {
        DialogPrefs.setShared(context, true)
        MaterialAlertDialogBuilder(context)
            .setMessage(getString(R.string.qr_image_share_warning_text))
            .setCancelable(false)
            .setNegativeButton(getString(R.string.cancel_text)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.share_text)) { _, _ ->
                shareImage()
            }
            .create()
            .show()
    }

    private fun shareImage() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            imageShareUri?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                putExtra(Intent.EXTRA_STREAM, it)
            }
        }
        shareResultLauncher.launch(
            Intent.createChooser(shareIntent, "Share with")
        )
    }

    private fun handleStartNewButtonClick() {
        GeneratorManager.resetQRGenerator()
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("KEY", "generator")
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    // This launcher handles the result after sharing the QR image
    private val shareResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Handle the result if needed
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
