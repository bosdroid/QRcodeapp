package com.expert.qrgenerator.activities

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.ColorAdapter
import com.expert.qrgenerator.adapters.ImageAdapter
import com.expert.qrgenerator.utils.ColorManager
import com.expert.qrgenerator.utils.ImageManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.database.*
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
    private lateinit var backgroundImageBtn: MaterialButton
    private lateinit var colorsRecyclerView: RecyclerView
    private lateinit var backgroundImageRecyclerView: RecyclerView
    private lateinit var qrGeneratedImage: AppCompatImageView
    private lateinit var shareBtn: FloatingActionButton
    private lateinit var qrText: MaterialTextView
    private lateinit var qrImageWrapperLayout: RelativeLayout
    private var qr_image: Bitmap? = null
    private lateinit var colorAdapter: ColorAdapter
    private lateinit var imageAdapter: ImageAdapter
    private var colorList = mutableListOf<String>()
    private var imageList = mutableListOf<String>()
    private var inputText: String? = null
    private lateinit var databaseReference: DatabaseReference
    private var imageUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        initViews()
        setUpToolbar()
        renderColorsRecyclerview()
        renderBackgroundImageRecyclerview()

    }

    // THIS FUNCTION WILL INITIALIZE ALL THE VIEWS AND REFERENCE OF OBJECTS
    private fun initViews() {
        context = this
        toolbar = findViewById(R.id.toolbar)
        databaseReference = FirebaseDatabase.getInstance().reference
        colorList.addAll(ColorManager.createColorList(context))

        inputBox = findViewById(R.id.input_text_box)
        inputBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.isNotEmpty()) {
                    inputText = s.toString()
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        generateBtn = findViewById(R.id.generate_btn)
        generateBtn.setOnClickListener(this)
        backgroundImageBtn = findViewById(R.id.background_btn)
        backgroundImageBtn.setOnClickListener(this)
        colorBtn = findViewById(R.id.color_btn)
        colorBtn.setOnClickListener(this)
        colorsRecyclerView = findViewById(R.id.colors_recycler_view)
        backgroundImageRecyclerView = findViewById(R.id.background_images_recycler_view)
        qrGeneratedImage = findViewById(R.id.qr_generated_img)
        shareBtn = findViewById(R.id.share_btn)
        shareBtn.setOnClickListener(this)
        qrText = findViewById(R.id.qr_text)
        qrImageWrapperLayout = findViewById(R.id.qr_image_wrapper_layout)
    }

    // THIS FUNCTION WILL RENDER THE ACTION BAR/TOOLBAR
    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.app_name)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.white))
    }

    // THIS FUNCTION WILL HANDLE ALL THE VIEWS CLICK LISTENER
    override fun onClick(v: View?) {
        when (v!!.id) {
            // GENERATE BTN WILL MANIPULATE THE QR IMAGE
            R.id.generate_btn -> {
                if (!TextUtils.isEmpty(inputText)) {

                    qr_image = generateQRWithBackgroundImage(context, inputText!!, "", "")
                    qrText.text = inputText
                    qrGeneratedImage.setImageBitmap(qr_image)
                    shareBtn.visibility = View.VISIBLE

                }
            }
            // SHARE BTN WILL CALL THE SHARE IMAGE FUNCTION
            R.id.share_btn -> {
                shareImage()
            }
            // COLOR BTN WILL HANDLE THE COLOR LIST
            R.id.color_btn -> {
                if (backgroundImageRecyclerView.visibility == View.VISIBLE) {
                    backgroundImageRecyclerView.visibility = View.GONE
                }
                if (colorsRecyclerView.visibility == View.VISIBLE) {
                    colorsRecyclerView.visibility = View.GONE
                } else {
                    colorsRecyclerView.visibility = View.VISIBLE
                }
            }
            // BACKGROUND BTN WILL HANDLE THE BACKGROUND IMAGE LIST
            R.id.background_btn -> {
                if (colorsRecyclerView.visibility == View.VISIBLE) {
                    colorsRecyclerView.visibility = View.GONE
                }
                if (backgroundImageRecyclerView.visibility == View.VISIBLE) {
                    backgroundImageRecyclerView.visibility = View.GONE
                } else {
                    backgroundImageRecyclerView.visibility = View.VISIBLE
                }
            }
            else -> {

            }
        }
    }

    // THIS FUNCTION WILL SAVE AND SHARE THE FINAL QR IMAGE GETTING FROM CACHE DIRECTORY
    private fun shareImage() {
        if (backgroundImageRecyclerView.visibility == View.VISIBLE) {
            backgroundImageRecyclerView.visibility = View.GONE
        }
        if (colorsRecyclerView.visibility == View.VISIBLE) {
            colorsRecyclerView.visibility = View.GONE
        }
        qrImageWrapperLayout.gravity = Gravity.CENTER
        val layout_bitmap_image = ImageManager.loadBitmapFromView(context,qrImageWrapperLayout)

        val mediaStorageDir = File(externalCacheDir.toString() + "final_qr_image.png")

        try {
            val outputStream = FileOutputStream(mediaStorageDir.toString())
            layout_bitmap_image!!.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // HERE START THE SHARE INTENT
        val waIntent = Intent(Intent.ACTION_SEND)
        imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            waIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            FileProvider.getUriForFile(
                this,
                applicationContext.packageName + ".fileprovider", mediaStorageDir
            )

        } else {
            Uri.fromFile(mediaStorageDir)
        }
        if (imageUri != null) {
            waIntent.type = "image/*"
            waIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
            startActivity(Intent.createChooser(waIntent, "Share with"))
        }

    }

    // THIS FUNCTION WILL DISPLAY THE HORIZONTAL COLOR LIST
    private fun renderColorsRecyclerview() {
        var previous_position = -1
        colorsRecyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL,
            false
        )
        colorsRecyclerView.hasFixedSize()
        colorAdapter = ColorAdapter(context, colorList)
        colorsRecyclerView.adapter = colorAdapter

        // CLICK ON EACH COLOR ITEM
        colorAdapter.setOnItemClickListener(object : ColorAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                if (!TextUtils.isEmpty(inputText)) {

                    if (previous_position != position) {
                        previous_position = position

                        qr_image = generateQRWithBackgroundImage(
                            context,
                            inputText!!,
                            colorList[position], ""
                        )
                        qrGeneratedImage.setImageBitmap(qr_image)
                    }
                }

            }
        })

    }

    // THIS FUNCTION WILL DISPLAY THE HORIZONTAL BACKGROUND IMAGE LIST
    private fun renderBackgroundImageRecyclerview() {
        var previous_position = -1
        backgroundImageRecyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL,
            false
        )
        backgroundImageRecyclerView.hasFixedSize()
        imageAdapter = ImageAdapter(context, imageList)
        backgroundImageRecyclerView.adapter = imageAdapter

        // CLICK ON EACH COLOR ITEM
        imageAdapter.setOnItemClickListener(object : ImageAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                if (!TextUtils.isEmpty(inputText)) {
                    if (previous_position != position) {
                        previous_position = position
                        qr_image = generateQRWithBackgroundImage(
                            context,
                            inputText!!,
                            "", imageList[position]
                        )
                        qrGeneratedImage.setImageBitmap(qr_image)
                    }
                }
            }
        })

        databaseReference.child("backgroundImages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (postSnapshot in dataSnapshot.children) {
                            val url = postSnapshot.getValue(String::class.java)
                            imageList.add(url!!)
                        }
                        imageAdapter.notifyDataSetChanged()
                        Log.d("TEST199", imageList.toString())
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("TEST199", "loadPost:onCancelled", databaseError.toException())
                }
            })

    }
}