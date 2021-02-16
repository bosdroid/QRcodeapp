package com.expert.qrgenerator.view.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
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
import com.expert.qrgenerator.adapters.*
import com.expert.qrgenerator.interfaces.OnCompleteAction
import com.expert.qrgenerator.model.Fonts
import com.expert.qrgenerator.model.QRTypes
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.ImageManager
import com.expert.qrgenerator.utils.RuntimePermissionHelper
import com.expert.qrgenerator.viewmodel.MainActivityViewModel
import com.expert.qrgenerator.viewmodelfactory.ViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import java.io.*
import java.util.Observer


class MainActivity : BaseActivity(), View.OnClickListener,OnCompleteAction {
    //TODO Please remove all unused code and imports

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var secondaryInputBoxView: TextInputEditText
    private lateinit var typesBtn: LinearLayout
    private lateinit var colorBtn: LinearLayout
    private lateinit var logoBtn: LinearLayout
    private lateinit var textBtn: LinearLayout
    private lateinit var backgroundImageBtn: LinearLayout
    private lateinit var colorsRecyclerView: RecyclerView
    private lateinit var backgroundImageRecyclerView: RecyclerView
    private lateinit var qrTypesRecyclerView: RecyclerView
    private lateinit var qrGeneratedImage: AppCompatImageView
    private lateinit var shareBtn: FloatingActionButton
    private lateinit var qrTextView: MaterialTextView
    private lateinit var qrSignTextView: MaterialTextView
    private lateinit var qrImageWrapperLayout: RelativeLayout
    private lateinit var textLayoutWrapper: LinearLayout
    private lateinit var logoImageRecyclerView: RecyclerView
    private lateinit var fontRecyclerView: RecyclerView
    private var qrImage: Bitmap? = null
    private lateinit var colorAdapter: ColorAdapter
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var logoAdapter: LogoAdapter
    private lateinit var fontAdapter: FontAdapter
    private lateinit var typesAdapter: TypesAdapter
    private var colorList = mutableListOf<String>()
    private var imageList = mutableListOf<String>()
    private var logoList = mutableListOf<String>()
    private var fontList = mutableListOf<Fonts>()
    private var qrTypeList = mutableListOf<QRTypes>()
    private var image_previous_position = -1
    private var logo_previous_position = -1
    private var encodedTextData: String = " "
    private var secondaryInputText: String? = null
    private lateinit var viewModel: MainActivityViewModel
    private var intentType: String? = null
    private var bAlert: AlertDialog? = null
    private var lAlert: AlertDialog? = null
    private var isBackgroundSet: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        initViews()
        setUpToolbar()
        renderQRTypesRecyclerview()
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
        secondaryInputBoxView = findViewById(R.id.secondary_input_text_box)
        backgroundImageBtn = findViewById(R.id.background_btn)
        backgroundImageBtn.setOnClickListener(this)
        typesBtn = findViewById(R.id.types_btn)
        typesBtn.setOnClickListener(this)
        colorBtn = findViewById(R.id.color_btn)
        colorBtn.setOnClickListener(this)
        logoBtn = findViewById(R.id.logo_btn)
        logoBtn.setOnClickListener(this)
        textBtn = findViewById(R.id.text_btn)
        textBtn.setOnClickListener(this)
        qrTypesRecyclerView = findViewById(R.id.types_recycler_view)
        colorsRecyclerView = findViewById(R.id.colors_recycler_view)
        backgroundImageRecyclerView = findViewById(R.id.background_images_recycler_view)
        logoImageRecyclerView = findViewById(R.id.logo_images_recycler_view)
        fontRecyclerView = findViewById(R.id.fonts_recycler_view)
        qrGeneratedImage = findViewById(R.id.qr_generated_img)
        shareBtn = findViewById(R.id.share_btn)
        shareBtn.setOnClickListener(this)
        qrTextView = findViewById(R.id.qr_text)
        qrSignTextView = findViewById(R.id.qr_sign_text)
        qrImageWrapperLayout = findViewById(R.id.qr_image_wrapper_layout)
        textLayoutWrapper = findViewById(R.id.text_font_layout_wrapper)


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

        // GENERATE DEFAULT QR IMAGE
        qrImage = generateQRWithBackgroundImage(context, encodedTextData, "000000", "", "")
        qrGeneratedImage.setImageBitmap(qrImage)
        qrSignTextView.visibility = View.VISIBLE
        if (shareBtn.visibility == View.INVISIBLE) {
            shareBtn.visibility = View.VISIBLE
        }
    }

    // THIS FUNCTION WILL RENDER THE ACTION BAR/TOOLBAR
    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.app_name)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    // THIS FUNCTION WILL HANDLE ALL THE VIEWS CLICK LISTENER
    override fun onClick(v: View?) {
        //TODO Need to create function that will manipulate visibility depends on parameters
        //TODO Too much code duplication
        when (v!!.id) {
            R.id.types_btn -> {
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
                }
                if (qrTypesRecyclerView.visibility == View.VISIBLE) {
                    qrTypesRecyclerView.visibility = View.GONE
                } else {
                    qrTypesRecyclerView.visibility = View.VISIBLE
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
                if (qrTypesRecyclerView.visibility == View.VISIBLE) {
                    qrTypesRecyclerView.visibility = View.GONE
                }
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
                if (qrTypesRecyclerView.visibility == View.VISIBLE) {
                    qrTypesRecyclerView.visibility = View.GONE
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
                if (backgroundImageRecyclerView.visibility == View.VISIBLE) {
                    backgroundImageRecyclerView.visibility = View.GONE
                } else {
                    backgroundImageRecyclerView.visibility = View.VISIBLE
                }
            }
            // BACKGROUND BTN WILL HANDLE THE LOGO IMAGE LIST
            R.id.logo_btn -> {
                if (qrTypesRecyclerView.visibility == View.VISIBLE) {
                    qrTypesRecyclerView.visibility = View.GONE
                }
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
                if (qrTypesRecyclerView.visibility == View.VISIBLE) {
                    qrTypesRecyclerView.visibility = View.GONE
                }
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
        //TODO Create separate handler for sharing process

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

        //TODO Please explain how this part works
        val layout_bitmap_image = ImageManager.loadBitmapFromView(context, qrImageWrapperLayout)
        val fileName = "final_qr_image_" + getDateTimeFromTimeStamp(System.currentTimeMillis()) + ".jpg"
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

    // THIS FUNCTION WILL DISPLAY THE HORZIZONTAL QR TYPES LIST
    private fun renderQRTypesRecyclerview() {
        //TODO What is the purpose?
        qrTypesRecyclerView.layoutManager = LinearLayoutManager(
                context,
                RecyclerView.HORIZONTAL,
                false
        )
        qrTypesRecyclerView.hasFixedSize()
        qrTypeList.addAll(Constants.getQRTypes())
        typesAdapter = TypesAdapter(context, qrTypeList)
        qrTypesRecyclerView.adapter = typesAdapter
        typesAdapter.setOnItemClickListener(object : TypesAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val qrType = qrTypeList[position]
                Constants.getLayout(context, position)
            }
        })
    }

    // THIS FUNCTION WILL RE GENERATE THE QR IMAGE AFTER CHANGE TYPE OF INPUTS
    private fun regenerateQrImage(text:String) {
        //TODO Put all generator related logic in separate QRGenerator class
        encodedTextData = text
        qrImage = generateQRWithBackgroundImage(
                context,
                encodedTextData,
                "",
                "",
                ""
        )
        qrGeneratedImage.setImageBitmap(qrImage)
    }

    // THIS FUNCTION WILL DISPLAY THE HORIZONTAL BACKGROUND IMAGE LIST
    private fun renderBackgroundImageRecyclerview() {
        //TODO What is the purpose?
        backgroundImageRecyclerView.layoutManager = LinearLayoutManager(
                context,
                RecyclerView.HORIZONTAL,
                false
        )
        backgroundImageRecyclerView.hasFixedSize()
        val localBackgroundImageList = Constants.getAllBackgroundImages(context)
        imageList.addAll(localBackgroundImageList)
        imageAdapter = ImageAdapter(context, imageList)
        backgroundImageRecyclerView.adapter = imageAdapter

        viewModel.callBackgroundImages(context)
        viewModel.getBackgroundImages().observe(this, Observer { list ->
            if (list != null) {
                imageList.addAll(imageList.size, list)
                imageAdapter.notifyItemRangeInserted(imageList.size, list.size)
            }
        })

        // CLICK ON EACH IMAGE ITEM
        imageAdapter.setOnItemClickListener(object : ImageAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
//                if (!TextUtils.isEmpty(primaryInputText)) {
                if (image_previous_position != position) {
                    image_previous_position = position
                    qrImage = generateQRWithBackgroundImage(
                            context,
                            encodedTextData,
                            "", imageList[position], ""
                    )
                    qrGeneratedImage.setImageBitmap(qrImage)
                    isBackgroundSet = qrImage != null
                }
//                }
            }

            override fun onAddItemClick(position: Int) {
                intentType = "background"

                val backgroundImageDialogView = LayoutInflater.from(context)
                        .inflate(R.layout.background_image_hint_layout, null)
                val builder = MaterialAlertDialogBuilder(context)
                builder.setCancelable(false)
                builder.setView(backgroundImageDialogView)
                bAlert = builder.create()
                bAlert!!.show()

                val cancelBtn =
                        backgroundImageDialogView.findViewById<MaterialButton>(R.id.custom_image_cancel_btn)
                val choseBtn =
                        backgroundImageDialogView.findViewById<MaterialButton>(R.id.custom_image_add_btn)
                cancelBtn.setOnClickListener { bAlert!!.dismiss() }
                choseBtn.setOnClickListener {
                    if (RuntimePermissionHelper.checkPermission(
                                    context,
                                    Constants.READ_STORAGE_PERMISSION
                            )
                    ) {
                        getImageFromLocalStorage()
                    }
                }
            }
        })
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
        val customColorList = Constants.readColorFile(context)
        if (customColorList.isNotEmpty()) {
            val localColorList = customColorList.trim().split(" ")
            colorList.addAll(localColorList)
        }
        colorAdapter = ColorAdapter(context, colorList)
        colorsRecyclerView.adapter = colorAdapter

        viewModel.callColorList(context)
        viewModel.getColorList().observe(this, Observer { colors ->
            if (colors != null) {
                colorList.addAll(colorList.size, colors)
                colorAdapter.notifyItemRangeInserted(colorList.size, colors.size)
            }
        })

        // CLICK ON EACH COLOR ITEM
        colorAdapter.setOnItemClickListener(object : ColorAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
//                if (!TextUtils.isEmpty(primaryInputText)) {
                if (isBackgroundSet) {
                    colorAdapter.updateIcon(true)
                    if (previous_position != position) {
                        previous_position = position

                        qrImage = generateQRWithBackgroundImage(
                                context,
                                encodedTextData,
                                colorList[position], "", ""
                        )
                        qrGeneratedImage.setImageBitmap(qrImage)
                    }
                } else {
                    colorAdapter.updateIcon(false)
                    showAlert(context, "Please select the background Image first!")
                }
//                }

            }

            // CLICK ON ADD BUTTON TO GET CUSTOM COLOR INPUT
            override fun onAddItemClick(position: Int) {
                val colorDialogView =
                        LayoutInflater.from(context).inflate(R.layout.color_input_dialog, null)

                val colorInputBox =
                        colorDialogView.findViewById<TextInputEditText>(R.id.custom_color_input_box)
                val cancelBtn =
                        colorDialogView.findViewById<MaterialButton>(R.id.custom_color_cancel_btn)
                val addBtn = colorDialogView.findViewById<MaterialButton>(R.id.custom_color_add_btn)

                val builder = MaterialAlertDialogBuilder(context)
                builder.setCancelable(false)
                builder.setView(colorDialogView)
                val alert = builder.create()
                alert.show()

                cancelBtn.setOnClickListener { alert.dismiss() }
                addBtn.setOnClickListener {
                    val inputText = colorInputBox.text.toString()
                    if (!TextUtils.isEmpty(inputText)) {
                        if (inputText.contains("#")) {
                            Toast.makeText(
                                    context,
                                    "Please enter the Color Value without having #",
                                    Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            if (inputText.length == 6) {
                                colorList.add(0, inputText)
                                previous_position += 1
                                colorAdapter.updateAdapter(0)
                                Constants.writeColorValueToFile("$inputText ", context)
                                alert.dismiss()
                            } else {
                                Toast.makeText(
                                        context,
                                        "Please enter the valid Color Value!",
                                        Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Please enter the Color Value!", Toast.LENGTH_SHORT)
                                .show()
                    }
                }
            }
        })

    }

    // THIS FUNCTION WILL DISPLAY THE HORIZONTAL LOGO IMAGE LIST
    private fun renderLogoImagesRecyclerview() {
        logoImageRecyclerView.layoutManager = LinearLayoutManager(
                context,
                RecyclerView.HORIZONTAL,
                false
        )
        logoImageRecyclerView.hasFixedSize()
        val localLogoImageList = Constants.getAllLogoImages(context)
        logoList.addAll(localLogoImageList)
        logoAdapter = LogoAdapter(context, logoList)
        logoImageRecyclerView.adapter = logoAdapter

        viewModel.callLogoImages(context)
        viewModel.getLogoImages().observe(this, Observer { list ->
            if (list != null) {
                logoList.addAll(logoList.size, list)
                logoAdapter.notifyItemRangeInserted(logoList.size, list.size)
            }
        })

        // CLICK ON EACH IMAGE ITEM
        logoAdapter.setOnItemClickListener(object : LogoAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
//                if (!TextUtils.isEmpty(primaryInputText)) {
                if (isBackgroundSet) {
                    logoAdapter.updateIcon(true)
                    if (logo_previous_position != position) {
                        logo_previous_position = position
                        qrImage = generateQRWithBackgroundImage(
                                context,
                                encodedTextData,
                                "", "", logoList[position]
                        )
                        qrGeneratedImage.setImageBitmap(qrImage)
                    }
                } else {
                    logoAdapter.updateIcon(false)
                    showAlert(context, "Please select the background Image first!")
                }

//                }
            }

            override fun onAddItemClick(position: Int) {
                intentType = "logo"

                val logoImageDialogView =
                        LayoutInflater.from(context).inflate(R.layout.logo_image_hint_layout, null)
                val builder = MaterialAlertDialogBuilder(context)
                builder.setCancelable(false)
                builder.setView(logoImageDialogView)
                lAlert = builder.create()
                lAlert!!.show()

                val cancelBtn =
                        logoImageDialogView.findViewById<MaterialButton>(R.id.custom_image_cancel_btn)
                val choseBtn =
                        logoImageDialogView.findViewById<MaterialButton>(R.id.custom_image_add_btn)
                cancelBtn.setOnClickListener { lAlert!!.dismiss() }
                choseBtn.setOnClickListener {
                    if (RuntimePermissionHelper.checkPermission(
                                    context,
                                    Constants.READ_STORAGE_PERMISSION
                            )
                    ) {
                        getImageFromLocalStorage()
                    }
                }
            }
        })
    }

    // THIS FUNCTION WILL DISPLAY THE HORIZONTAL FONT LIST
    private fun renderFontRecyclerview() {
        //TODO Can we optimize these render methods in some recyclerFactory to avoid code duplicates?
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
                if (fontList.size > 0) {
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
                if (isBackgroundSet) {
                    fontAdapter.updateIcon(true)
                    if (previous_position != position) {
                        previous_position = position
                        if (!TextUtils.isEmpty(secondaryInputBoxView.text.toString())) {
                            setFontFamily(context, qrTextView, font.fontFile)
                        }
                    }
                } else {
                    fontAdapter.updateIcon(false)
                    showAlert(context, "Please select the background Image first!")
                }

            }
        })
    }

    // THIS FUNCTION WILL CALL THE IMAGE INTENT
    private fun getImageFromLocalStorage() {
        //TODO put in separate storageHelper class
        val fileIntent = Intent(Intent.ACTION_PICK)
        fileIntent.type = "image/*"
        resultLauncher.launch(fileIntent)
    }

    // THIS RESULT LAUNCHER WILL CALL THE ACTION PICK FROM FILES FOR BACKGROUND AND LOGO IMAGE
    var resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (bAlert!!.isShowing) {
                    bAlert!!.dismiss()
                }
                if (lAlert!!.isShowing) {
                    lAlert!!.dismiss()
                }
                if (result.resultCode == Activity.RESULT_OK) {

                    val data: Intent? = result.data
                    val size = ImageManager.getImageWidthHeight(context, data!!.data!!)
                    val imageWidth = size.split(",")[0].toInt()
                    val imageHeight = size.split(",")[1].toInt()
                    //TODO Please add some comments what happens here
                    if (intentType.equals("background")) {
                        if (imageWidth > 800 && imageHeight > 800) {
                            showAlert(
                                    context,
                                    "Please select the background image having size 800x800!"
                            )
                        } else {
                            saveImageInLocalStorage(data.data!!)
                        }
                    } else {
                        if (imageWidth > 500 && imageHeight > 500) {
                            showAlert(
                                    context,
                                    "Please select the logo image having size 500x500!"
                            )
                        } else {
                            saveImageInLocalStorage(data.data!!)
                        }

                    }

                }
            }

    // THIS FUNCTION WILL SAVE THE IMAGE IN LOCAL STORAGE
    private fun saveImageInLocalStorage(uri: Uri) {
        //TODO put in separate storageHelper class
        //TODO Need comments to understand whats happens here
        var filePath: String? = null
        var fileName: String? = null

        if (intentType.equals("background")) {
            filePath = context.externalCacheDir.toString() + "/BackgroundImages"
            fileName = "qr_background_image_" + getDateTimeFromTimeStamp(System.currentTimeMillis()) + ".jpg"
        } else {
            filePath = context.externalCacheDir.toString() + "/LogoImages"
            fileName = "qr_logo_image_" + getDateTimeFromTimeStamp(System.currentTimeMillis()) + ".png"
        }
        val dir = File(filePath)
        dir.mkdir()

        val newFile = File(dir, fileName)

        val realPath = ImageManager.getRealPathFromUri(context, uri)
        if (intentType.equals("background")) {
            imageList.add(0, realPath!!)
            image_previous_position += 1
            imageAdapter.updateAdapter(0)
        } else {
            logoList.add(0, realPath!!)
            logo_previous_position += 1
            logoAdapter.updateAdapter(0)
        }

        val selectImageBitmap = getBitmapFromURL(context, realPath)
        try {
            val out = FileOutputStream(newFile)
            selectImageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            Constants.READ_STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImageFromLocalStorage()
                } else {
                    MaterialAlertDialogBuilder(context)
                            .setMessage("Please allow the READ EXTERNAL STORAGE permission for use own Image in QR Image.")
                            .setCancelable(false)
                            .setPositiveButton("Ok") { dialog, which ->
                                dialog.dismiss()
                            }
                            .create().show()
                }
            }
            else -> {

            }
        }
    }

    // THIS METHOD WILL CALL AFTER SELECT THE QR TYPE WITH INPUT DATA
    override fun onTypeSelected(data: String) {
        regenerateQrImage(data)
    }
}