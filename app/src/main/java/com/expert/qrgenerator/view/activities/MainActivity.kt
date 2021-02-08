package com.expert.qrgenerator.view.activities

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
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.ColorAdapter
import com.expert.qrgenerator.adapters.FontAdapter
import com.expert.qrgenerator.adapters.ImageAdapter
import com.expert.qrgenerator.adapters.LogoAdapter
import com.expert.qrgenerator.model.Fonts
import com.expert.qrgenerator.utils.ImageManager
import com.expert.qrgenerator.viewmodel.MainActivityViewModel
import com.expert.qrgenerator.viewmodelfactory.ViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : BaseActivity(), View.OnClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var primaryInputBoxView: TextInputEditText
    private lateinit var secondaryInputBoxView:TextInputEditText
    private lateinit var generateBtn: MaterialButton
    private lateinit var colorBtn: MaterialButton
    private lateinit var logoBtn:MaterialButton
    private lateinit var textBtn:MaterialButton
    private lateinit var backgroundImageBtn: MaterialButton
    private lateinit var colorsRecyclerView: RecyclerView
    private lateinit var backgroundImageRecyclerView: RecyclerView
    private lateinit var qrGeneratedImage: AppCompatImageView
    private lateinit var shareBtn: FloatingActionButton
    private lateinit var qrTextView: MaterialTextView
    private lateinit var qrImageWrapperLayout: RelativeLayout
    private lateinit var textLayoutWrapper: LinearLayout
    private lateinit var logoImageRecyclerView: RecyclerView
    private lateinit var fontRecyclerView: RecyclerView
    private var qrImage: Bitmap? = null
    private lateinit var colorAdapter: ColorAdapter
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var logoAdapter: LogoAdapter
    private lateinit var fontAdapter: FontAdapter
    private var colorList = mutableListOf<String>()
    private var imageList = mutableListOf<String>()
    private var logoList = mutableListOf<String>()
    private var fontList = mutableListOf<Fonts>()
    private var primaryInputText: String? = null
    private var secondaryInputText: String? = null
    private lateinit var viewModel: MainActivityViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        initViews()
        setUpToolbar()
        renderColorsRecyclerview()
        renderBackgroundImageRecyclerview()
        renderLogoImagesRecyclerview()
        renderFontRecyclerview()
    }

    // THIS FUNCTION WILL INITIALIZE ALL THE VIEWS AND REFERENCE OF OBJECTS
    private fun initViews() {
        context = this

        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(MainActivityViewModel()).createFor<ViewModel>()
        )[MainActivityViewModel::class.java]

        toolbar = findViewById(R.id.toolbar)
        primaryInputBoxView = findViewById(R.id.primary_input_text_box)
        secondaryInputBoxView = findViewById(R.id.secondary_input_text_box)
        generateBtn = findViewById(R.id.generate_btn)
        generateBtn.setOnClickListener(this)
        backgroundImageBtn = findViewById(R.id.background_btn)
        backgroundImageBtn.setOnClickListener(this)
        colorBtn = findViewById(R.id.color_btn)
        colorBtn.setOnClickListener(this)
        logoBtn = findViewById(R.id.logo_btn)
        logoBtn.setOnClickListener(this)
        textBtn = findViewById(R.id.text_btn)
        textBtn.setOnClickListener(this)
        colorsRecyclerView = findViewById(R.id.colors_recycler_view)
        backgroundImageRecyclerView = findViewById(R.id.background_images_recycler_view)
        logoImageRecyclerView = findViewById(R.id.logo_images_recycler_view)
        fontRecyclerView = findViewById(R.id.fonts_recycler_view)
        qrGeneratedImage = findViewById(R.id.qr_generated_img)
        shareBtn = findViewById(R.id.share_btn)
        shareBtn.setOnClickListener(this)
        qrTextView = findViewById(R.id.qr_text)
        qrImageWrapperLayout = findViewById(R.id.qr_image_wrapper_layout)
        textLayoutWrapper = findViewById(R.id.text_font_layout_wrapper)

        // START THE TEXT BOX LISTENER FOR SAVING UPDATED TEXT IN primaryInputText VARIABLE
        primaryInputBoxView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.isNotEmpty()) {
                    primaryInputText = s.toString()
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        // START THE TEXT BOX LISTENER FOR SAVING UPDATED TEXT IN secondaryInputText VARIABLE
        secondaryInputBoxView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.isNotEmpty()) {
                    secondaryInputText = s.toString()
                    qrTextView.text = secondaryInputText
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
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
                if (!TextUtils.isEmpty(primaryInputText)) {

                    qrImage = generateQRWithBackgroundImage(context, primaryInputText!!, "", "", "")
                    qrGeneratedImage.setImageBitmap(qrImage)

                    if (shareBtn.visibility == View.INVISIBLE) {
                        shareBtn.visibility = View.VISIBLE
                    }

                }
            }
            // SHARE BTN WILL CALL THE SHARE IMAGE FUNCTION
            R.id.share_btn -> {
                MaterialAlertDialogBuilder(context)
                    .setMessage("Are you sure you want to share this QR Image?")
                    .setCancelable(false)
                    .setNegativeButton("Cancel") { dialog, which ->
                        dialog.dismiss()
                    }
                    .setPositiveButton("Share") { dialog, which ->
                        shareImage()
                    }
                    .create().show()
            }
            // COLOR BTN WILL HANDLE THE COLOR LIST
            R.id.color_btn -> {
                if (backgroundImageRecyclerView.visibility == View.VISIBLE) {
                    backgroundImageRecyclerView.visibility = View.GONE
                }
                if (logoImageRecyclerView.visibility == View.VISIBLE) {
                    logoImageRecyclerView.visibility = View.GONE
                }
                if (textLayoutWrapper.visibility == View.VISIBLE) {
                    textLayoutWrapper.visibility = View.GONE
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
                if (logoImageRecyclerView.visibility == View.VISIBLE) {
                    logoImageRecyclerView.visibility = View.GONE
                }
                if (textLayoutWrapper.visibility == View.VISIBLE) {
                    textLayoutWrapper.visibility = View.GONE
                }
                if (backgroundImageRecyclerView.visibility == View.VISIBLE) {
                    backgroundImageRecyclerView.visibility = View.GONE
                } else {
                    backgroundImageRecyclerView.visibility = View.VISIBLE
                }
            }
            // BACKGROUND BTN WILL HANDLE THE LOGO IMAGE LIST
            R.id.logo_btn -> {
                if (backgroundImageRecyclerView.visibility == View.VISIBLE) {
                    backgroundImageRecyclerView.visibility = View.GONE
                }
                if (colorsRecyclerView.visibility == View.VISIBLE) {
                    colorsRecyclerView.visibility = View.GONE
                }
                if (textLayoutWrapper.visibility == View.VISIBLE) {
                    textLayoutWrapper.visibility = View.GONE
                }
                if (logoImageRecyclerView.visibility == View.VISIBLE) {
                    logoImageRecyclerView.visibility = View.GONE
                } else {
                    logoImageRecyclerView.visibility = View.VISIBLE
                }
            }
            // TEXT BTN WILL HANDLE THE TEXT WITH FONT LAYOUT
            R.id.text_btn -> {

                if (backgroundImageRecyclerView.visibility == View.VISIBLE) {
                    backgroundImageRecyclerView.visibility = View.GONE
                }
                if (colorsRecyclerView.visibility == View.VISIBLE) {
                    colorsRecyclerView.visibility = View.GONE
                }
                if (logoImageRecyclerView.visibility == View.VISIBLE) {
                    logoImageRecyclerView.visibility = View.GONE
                }
                if (textLayoutWrapper.visibility == View.VISIBLE) {
                    textLayoutWrapper.visibility = View.GONE
                } else {
                    textLayoutWrapper.visibility = View.VISIBLE
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
        if (logoImageRecyclerView.visibility == View.VISIBLE) {
            logoImageRecyclerView.visibility = View.GONE
        }
        if (textLayoutWrapper.visibility == View.VISIBLE) {
            textLayoutWrapper.visibility = View.GONE
        }

        val layout_bitmap_image = ImageManager.loadBitmapFromView(context, qrImageWrapperLayout)
        val fileName = "final_qr_image_" + System.currentTimeMillis() + ".jpg"
        val mediaStorageDir = File(externalCacheDir.toString(), fileName)

        try {
            val outputStream = FileOutputStream(mediaStorageDir.toString(), false)
            layout_bitmap_image!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // HERE START THE SHARE INTENT
        val waIntent = Intent(Intent.ACTION_SEND)
        val imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            waIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            FileProvider.getUriForFile(
                this,
                applicationContext.packageName + ".fileprovider", mediaStorageDir
            )

        } else {
            Uri.fromFile(mediaStorageDir)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            waIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
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

        viewModel.callColorList(context)
        viewModel.getColorList().observe(this, Observer { colors ->
            if (colors != null) {
                colorList.addAll(colors)
                colorAdapter.notifyDataSetChanged()
            }
        })

        // CLICK ON EACH COLOR ITEM
        colorAdapter.setOnItemClickListener(object : ColorAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                if (!TextUtils.isEmpty(primaryInputText)) {

                    if (previous_position != position) {
                        previous_position = position

                        qrImage = generateQRWithBackgroundImage(
                            context,
                            primaryInputText!!,
                            colorList[position], "", ""
                        )
                        qrGeneratedImage.setImageBitmap(qrImage)
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

        viewModel.callBackgroundImages(context)
        viewModel.getBackgroundImages().observe(this, Observer { list ->
            if (list != null) {
                imageList.addAll(list)
                imageAdapter.notifyDataSetChanged()
            }
        })

        // CLICK ON EACH IMAGE ITEM
        imageAdapter.setOnItemClickListener(object : ImageAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                if (!TextUtils.isEmpty(primaryInputText)) {
                    if (previous_position != position) {
                        previous_position = position
                        qrImage = generateQRWithBackgroundImage(
                            context,
                            primaryInputText!!,
                            "", imageList[position], ""
                        )
                        qrGeneratedImage.setImageBitmap(qrImage)
                    }
                }
            }
        })
    }

    // THIS FUNCTION WILL DISPLAY THE HORIZONTAL LOGO IMAGE LIST
    private fun renderLogoImagesRecyclerview() {
        var previous_position = -1
        logoImageRecyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL,
            false
        )
        logoImageRecyclerView.hasFixedSize()
        logoAdapter = LogoAdapter(context, logoList)
        logoImageRecyclerView.adapter = logoAdapter

        viewModel.callLogoImages(context)
        viewModel.getLogoImages().observe(this, Observer { list ->
            if (list != null) {
                logoList.addAll(list)
                logoAdapter.notifyDataSetChanged()
            }
        })

        // CLICK ON EACH IMAGE ITEM
        logoAdapter.setOnItemClickListener(object : LogoAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                if (!TextUtils.isEmpty(primaryInputText)) {
                    if (previous_position != position) {
                        previous_position = position
                        qrImage = generateQRWithBackgroundImage(
                            context,
                            primaryInputText!!,
                            "", "", logoList[position]
                        )
                        qrGeneratedImage.setImageBitmap(qrImage)
                    }
                }
            }
        })
    }

    // THIS FUNCTION WILL DISPLAY THE HORIZONTAL FONT LIST
    private fun renderFontRecyclerview() {
        var previous_position = -1
        fontRecyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL,
            false
        )
        fontRecyclerView.hasFixedSize()
        fontAdapter = FontAdapter(context, fontList)
        fontRecyclerView.adapter = fontAdapter

        viewModel.callFontList(context)
        viewModel.getFontList().observe(this, Observer { list ->
            if (list != null) {
                if(fontList.size > 0)
                {
                    fontList.clear()
                }
                fontList.addAll(list)
                fontAdapter.notifyDataSetChanged()
            }
        })

        // CLICK ON EACH FONT ITEM
        fontAdapter.setOnItemClickListener(object : FontAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val font = fontList[position]
                if (previous_position != position) {
                    previous_position = position
                    if (!TextUtils.isEmpty(secondaryInputBoxView.text.toString())) {
                        setFontFamily(context, qrTextView, font.fontFile)
                    }
                }
            }
        })
    }
}