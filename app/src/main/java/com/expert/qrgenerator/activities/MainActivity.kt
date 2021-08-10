package com.expert.qrgenerator.activities

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView

import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.ColorAdapter
import com.expert.qrgenerator.utils.ColorManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : BaseActivity(), View.OnClickListener {
    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var inputBox: TextInputEditText
    private lateinit var generateBtn: MaterialButton
    private lateinit var colorBtn: MaterialButton
    private lateinit var colorsRecyclerView: RecyclerView
    private lateinit var qrGeneratedImage: AppCompatImageView
    private lateinit var shareBtn: MaterialButton
    private var qr_image: Bitmap? = null
    private lateinit var adapter: ColorAdapter
    private var colorList = mutableListOf<String>()


    var textView: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setUpToolbar()
        renderColorsRecyclerview()

        textView!!.setOnClickListener {
          //  val intent = Intent(this, com.expert.qrgenerator.activities.VideosList);

            //Toast.makeText(mContext,"clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, com.expert.qrgenerator.activities.VideosList::class.java)
            startActivity(intent)


//            val intent = Intent(this, com.expert.qrgenerator.activities.VideoPlayerScreen::class.java)
//            startActivity(intent)

//            val  intent = Intent("com.expert.qrgenerator.activities.VideosList");
//            startActivity(intent)
        }



    }

    private fun initViews() {
        context = this
        toolbar = findViewById(R.id.toolbar)
        textView = findViewById(R.id.youtubevideo)

        colorList.addAll(ColorManager.getColors(context))
        Log.d("TEST199", colorList.toString())
        inputBox = findViewById(R.id.input_text_box)
        generateBtn = findViewById(R.id.generate_btn)
        generateBtn.setOnClickListener(this)
        colorBtn = findViewById(R.id.color_btn)
        colorBtn.setOnClickListener(this)

        colorsRecyclerView = findViewById(R.id.colors_recycler_view)
        qrGeneratedImage = findViewById(R.id.qr_generated_img)
        shareBtn = findViewById(R.id.share_btn)
        shareBtn.setOnClickListener(this)
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle(getString(R.string.app_name))
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.white))
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.generate_btn -> {
                if (!TextUtils.isEmpty(inputBox.text.toString())) {
                    val text = inputBox.text.toString()
                    qr_image = generateQRCode(text)
                    qrGeneratedImage.setImageBitmap(qr_image)
                    if (qr_image != null) {

                        try {
                            val cacheStoragePath = File(context.cacheDir, "images")
                            cacheStoragePath.mkdirs()
                            val stream =
                                FileOutputStream("$cacheStoragePath/qr_generated_image.jpg")
                            qr_image!!.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                            stream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    shareBtn.visibility = View.VISIBLE

                }
            }
            R.id.share_btn -> {
                shareImage()
            }
            R.id.color_btn -> {
                if (colorsRecyclerView.visibility == View.VISIBLE) {
                    colorsRecyclerView.visibility = View.INVISIBLE
                } else {
                    colorsRecyclerView.visibility = View.VISIBLE
                }
            }
            else -> {

            }
        }
    }

    private fun shareImage() {
        //Save the image inside the APPLICTION folder
        val mediaStorageDir = File(externalCacheDir.toString() + "Image.png")

        try {
            val outputStream = FileOutputStream(mediaStorageDir.toString())
            qr_image!!.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val imageUri = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName.toString() + ".provider",
            mediaStorageDir
        )
        if (imageUri != null) {
            val waIntent = Intent(Intent.ACTION_SEND)
            waIntent.type = "image/*"
            waIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
            startActivity(Intent.createChooser(waIntent, "Share with"))
        }

    }

    var previous_position = -1
    private fun renderColorsRecyclerview() {
        colorsRecyclerView.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        colorsRecyclerView.hasFixedSize()
        adapter = ColorAdapter(context, colorList)
        colorsRecyclerView.adapter = adapter
        adapter.setOnItemClickListener(object : ColorAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                if (previous_position != position) {
                    previous_position = position
                    Log.d("TEST199COLOR", colorList[position])
                    if (!TextUtils.isEmpty(inputBox.text.toString())) {
                        val text = inputBox.text.toString()
                        qr_image = generateQRCode(text, colorList[position])
                        qrGeneratedImage.setImageBitmap(qr_image)
                        if (qr_image != null) {
                            try {
                                val cacheStoragePath = File(context.cacheDir, "images")
                                cacheStoragePath.mkdirs()
                                val stream =
                                    FileOutputStream("$cacheStoragePath/qr_generated_image.jpg")
                                qr_image!!.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                                stream.close()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

            }
        })

    }


}