package com.expert.qrgenerator.view.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.expert.qrgenerator.R
import com.expert.qrgenerator.utils.Constants
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ShareActivity : BaseActivity(),View.OnClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var shareQrImage: AppCompatImageView
    private lateinit var shareBtn:AppCompatButton
    private var imageShareUri:Uri?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        initViews()
        setUpToolbar()
    }

    private fun initViews(){
        context = this
        toolbar = findViewById(R.id.toolbar)
        shareQrImage = findViewById(R.id.share_qr_generated_img)
        shareBtn = findViewById(R.id.share_btn)
        shareBtn.setOnClickListener(this)

        if (Constants.finalQrImageUri != null){
            imageShareUri = Constants.finalQrImageUri
            shareQrImage.setImageURI(Constants.finalQrImageUri)
        }
    }

    private fun setUpToolbar(){
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.share_qr_image)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            // SHARE BTN WILL CALL THE SHARE IMAGE FUNCTION
            R.id.share_btn -> {
                MaterialAlertDialogBuilder(context)
                    .setMessage("Are you sure you want to share this QR Image?")
                    .setCancelable(false)
                    .setNegativeButton("Cancel") { dialog, which ->
                        dialog.dismiss()
                    }
                    .setPositiveButton("Share") { dialog, which ->
//                      ImageManager.shareImage(context,imageShareUri)
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        if (imageShareUri != null) {
                            shareIntent.type = "image/*"
                            shareIntent.putExtra(Intent.EXTRA_STREAM, imageShareUri)
                            shareResultLauncher.launch(Intent.createChooser(shareIntent, "Share with"))
                        }
                    }
                    .create().show()
            }
            else->{

            }
        }
    }

    // THIS SHARE IMAGE LAUNCHER WILL HANDLE AFTER SHARING QR IMAGE
    private var shareResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val intent = Intent(context,MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
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