package com.expert.qrgenerator.view.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.StrictMode
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.*
import com.expert.qrgenerator.interfaces.OnCompleteAction
import com.expert.qrgenerator.model.Fonts
import com.expert.qrgenerator.model.QREntity
import com.expert.qrgenerator.model.QRTypes
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.ImageManager
import com.expert.qrgenerator.utils.QRGenerator
import com.expert.qrgenerator.utils.RuntimePermissionHelper
import com.expert.qrgenerator.viewmodel.MainActivityViewModel
import com.expert.qrgenerator.viewmodelfactory.ViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView


class MainActivity : BaseActivity(), View.OnClickListener, OnCompleteAction {

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
    private lateinit var dynamicModeSwitch: SwitchMaterial
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
    private var imagePreviousPosition = -1
    private var logoPreviousPosition = -1
    private var encodedTextData: String = " "
    private var secondaryInputText: String? = null
    private lateinit var viewModel: MainActivityViewModel
    private var intentType: String? = null
    private var bAlert: AlertDialog? = null
    private var lAlert: AlertDialog? = null
    private var isBackgroundSet: Boolean = false
    private lateinit var appViewModel:AppViewModel


    companion object{
        var isDynamicActive:Boolean = false
    }


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
            ViewModelFactory(MainActivityViewModel()).createFor()
        )[MainActivityViewModel::class.java]

        appViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(this.application)
        ).get(AppViewModel::class.java)

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
        dynamicModeSwitch = findViewById(R.id.dynamic_switch_button)
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

        // GENERATE DEFAULT QR IMAGE WHEN APP IS OPEN
        qrImage = QRGenerator.generatorQRImage(context, encodedTextData, "000000", "", "")
        qrGeneratedImage.setImageBitmap(qrImage)
        qrSignTextView.visibility = View.VISIBLE
        if (shareBtn.visibility == View.INVISIBLE) {
            shareBtn.visibility = View.VISIBLE
        }

        // START HERE TO DYNAMIC MODE SWITCH LISTENER
        dynamicModeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                isDynamicActive = true
                typesAdapter.disableEnableViews(true)
                Toast.makeText(
                    context,
                    "Dynamic Code may content Only Web Links for now",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                isDynamicActive = false
                typesAdapter.disableEnableViews(false)
            }
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
        when (v!!.id) {
            // TYPES BTN WILL HANDLE THE QR CODE TYPE
            R.id.types_btn -> {
                viewVisibleInvisible(0)
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
                        viewVisibleInvisible(5)
                        ImageManager.shareImage(context, qrImageWrapperLayout)
                    }
                    .create().show()
            }
            // COLOR BTN WILL HANDLE THE COLOR LIST
            R.id.color_btn -> {
                viewVisibleInvisible(1)
            }
            // BACKGROUND BTN WILL HANDLE THE BACKGROUND IMAGE LIST
            R.id.background_btn -> {
                viewVisibleInvisible(2)
            }
            // BACKGROUND BTN WILL HANDLE THE LOGO IMAGE LIST
            R.id.logo_btn -> {
                viewVisibleInvisible(3)
            }
            // TEXT BTN WILL HANDLE THE TEXT WITH FONT LAYOUT
            R.id.text_btn -> {
                viewVisibleInvisible(4)
            }
            else -> {

            }
        }
    }

    // THIS FUNCTION WILL USE THE VIEW VISIBLE AND INVISIBLE
    private fun viewVisibleInvisible(code: Int) {
        when (code) {
            0 -> {
                backgroundImageRecyclerView.visibility = View.GONE
                logoImageRecyclerView.visibility = View.GONE
                textLayoutWrapper.visibility = View.GONE
                colorsRecyclerView.visibility = View.GONE

                if (qrTypesRecyclerView.visibility == View.VISIBLE) {
                    qrTypesRecyclerView.visibility = View.GONE
                } else {
                    qrTypesRecyclerView.visibility = View.VISIBLE
                }
            }
            1 -> {
                qrTypesRecyclerView.visibility = View.GONE
                backgroundImageRecyclerView.visibility = View.GONE
                logoImageRecyclerView.visibility = View.GONE
                textLayoutWrapper.visibility = View.GONE

                if (colorsRecyclerView.visibility == View.VISIBLE) {
                    colorsRecyclerView.visibility = View.GONE
                } else {
                    colorsRecyclerView.visibility = View.VISIBLE
                }
            }
            2 -> {
                qrTypesRecyclerView.visibility = View.GONE
                colorsRecyclerView.visibility = View.GONE
                logoImageRecyclerView.visibility = View.GONE
                textLayoutWrapper.visibility = View.GONE

                if (backgroundImageRecyclerView.visibility == View.VISIBLE) {
                    backgroundImageRecyclerView.visibility = View.GONE
                } else {
                    backgroundImageRecyclerView.visibility = View.VISIBLE
                }
            }
            3 -> {
                qrTypesRecyclerView.visibility = View.GONE
                backgroundImageRecyclerView.visibility = View.GONE
                colorsRecyclerView.visibility = View.GONE
                textLayoutWrapper.visibility = View.GONE

                if (logoImageRecyclerView.visibility == View.VISIBLE) {
                    logoImageRecyclerView.visibility = View.GONE
                } else {
                    logoImageRecyclerView.visibility = View.VISIBLE
                }
            }
            4 -> {
                qrTypesRecyclerView.visibility = View.GONE
                backgroundImageRecyclerView.visibility = View.GONE
                colorsRecyclerView.visibility = View.GONE
                logoImageRecyclerView.visibility = View.GONE

                if (textLayoutWrapper.visibility == View.VISIBLE) {
                    textLayoutWrapper.visibility = View.GONE
                } else {
                    textLayoutWrapper.visibility = View.VISIBLE
                }
            }
            else -> {
                qrTypesRecyclerView.visibility = View.GONE
                backgroundImageRecyclerView.visibility = View.GONE
                colorsRecyclerView.visibility = View.GONE
                logoImageRecyclerView.visibility = View.GONE
                textLayoutWrapper.visibility = View.GONE
            }
        }
    }

    // THIS FUNCTION WILL DISPLAY THE HORIZONTAL QR TYPES LIST
    private fun renderQRTypesRecyclerview() {
        // THIS LINE OF CODE WILL SET THE RECYCLERVIEW ORIENTATION (HORIZONTAL OR VERTICAL)
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

    // THIS FUNCTION WILL DISPLAY THE HORIZONTAL BACKGROUND IMAGE LIST
    private fun renderBackgroundImageRecyclerview() {
        // THIS LINE OF CODE WILL SET THE RECYCLERVIEW ORIENTATION (HORIZONTAL OR VERTICAL)
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
        viewModel.getBackgroundImages().observe(this, { list ->
            if (list != null) {
                imageList.addAll(imageList.size, list)
                imageAdapter.notifyItemRangeInserted(imageList.size, list.size)
            }
        })

        // CLICK ON EACH IMAGE ITEM
        imageAdapter.setOnItemClickListener(object : ImageAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                if (imagePreviousPosition != position || !isBackgroundSet) {
                    imagePreviousPosition = position
                    qrImage = QRGenerator.generatorQRImage(
                        context,
                        encodedTextData,
                        "", imageList[position], ""
                    )
                    qrGeneratedImage.setImageBitmap(qrImage)
                    isBackgroundSet = qrImage != null
                }
            }

            // CLICK ON ADD BUTTON TO GET CUSTOM BACKGROUND IMAGE
            override fun onAddItemClick(position: Int) {
                intentType = "background"

                val backgroundImageDialogView = LayoutInflater.from(context).inflate(
                    R.layout.background_image_hint_layout,
                    null
                )
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
        var previousPosition = -1
        // THIS LINE OF CODE WILL SET THE RECYCLERVIEW ORIENTATION (HORIZONTAL OR VERTICAL)
        colorsRecyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL,
            false
        )
        colorsRecyclerView.hasFixedSize()
        val customColorList = ImageManager.readColorFile(context)
        if (customColorList.isNotEmpty()) {
            val localColorList = customColorList.trim().split(" ")
            colorList.addAll(localColorList)
        }
        colorAdapter = ColorAdapter(context, colorList)
        colorsRecyclerView.adapter = colorAdapter

        viewModel.callColorList(context)
        viewModel.getColorList().observe(this, { colors ->
            if (colors != null) {
                colorList.addAll(colorList.size, colors)
                colorAdapter.notifyItemRangeInserted(colorList.size, colors.size)
            }
        })

        // CLICK ON EACH COLOR ITEM
        colorAdapter.setOnItemClickListener(object : ColorAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                if (isBackgroundSet) {
                    colorAdapter.updateIcon(true)
                    if (previousPosition != position) {
                        previousPosition = position

                        qrImage = QRGenerator.generatorQRImage(
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

            }

            // CLICK ON ADD BUTTON TO GET CUSTOM COLOR INPUT
            override fun onAddItemClick(position: Int) {
                val colorDialogView = LayoutInflater.from(context).inflate(
                    R.layout.color_input_dialog,
                    null
                )

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
                                previousPosition += 1
                                colorAdapter.updateAdapter(0)
                                ImageManager.writeColorValueToFile("$inputText ", context)
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
        // THIS LINE OF CODE WILL SET THE RECYCLERVIEW ORIENTATION (HORIZONTAL OR VERTICAL)
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
        viewModel.getLogoImages().observe(this, { list ->
            if (list != null) {
                logoList.addAll(logoList.size, list)
                logoAdapter.notifyItemRangeInserted(logoList.size, list.size)
            }
        })

        // CLICK ON EACH IMAGE ITEM
        logoAdapter.setOnItemClickListener(object : LogoAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                if (isBackgroundSet) {
                    logoAdapter.updateIcon(true)
                    if (logoPreviousPosition != position || isBackgroundSet) {
                        logoPreviousPosition = position
                        qrImage = QRGenerator.generatorQRImage(
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
            }

            // CLICK ON ADD BUTTON TO GET CUSTOM LOGO IMAGE
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
        var previousPosition = -1
        // THIS LINE OF CODE WILL SET THE RECYCLERVIEW ORIENTATION (HORIZONTAL OR VERTICAL)
        fontRecyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL,
            false
        )
        fontRecyclerView.hasFixedSize()
        fontAdapter = FontAdapter(context, fontList)
        fontRecyclerView.adapter = fontAdapter

        viewModel.callFontList(context)
        viewModel.getFontList().observe(this, { list ->
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
                    if (previousPosition != position) {
                        previousPosition = position
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
        val fileIntent = Intent(Intent.ACTION_PICK)
        fileIntent.type = "image/*"
        resultLauncher.launch(fileIntent)
    }

    // THIS RESULT LAUNCHER WILL CALL THE ACTION PICK FROM FILES FOR BACKGROUND AND LOGO IMAGE
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // THIS LINE OF CODE WILL CHECK ALERT DIALOG AND TYPE FOR DISMISS THE BACKGROUND IMAGE DIALOG
            if (bAlert != null && intentType == "background") {
                bAlert!!.dismiss()
            }
            // THIS LINE OF CODE WILL CHECK ALERT DIALOG AND TYPE FOR DISMISS THE LOGO IMAGE DIALOG
            if (lAlert != null && intentType == "logo") {
                lAlert!!.dismiss()
            }
            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {

                val data: Intent? = result.data
                val size = ImageManager.getImageWidthHeight(context, data!!.data!!)
                val imageWidth = size.split(",")[0].toInt()
                val imageHeight = size.split(",")[1].toInt()

                if (intentType.equals("background")) {
                    if (imageWidth > 800 && imageHeight > 800) {
                        showAlert(
                            context,
                            "Please select the background image having size 800x800!"
                        )
                    } else {
                        // THIS CODE WILL GET THE CUSTOM BACKGROUND IMAGE PATH AND SAVE INTO imageList
                        val filePath = ImageManager.saveImageInLocalStorage(
                            context,
                            data.data!!,
                            "background"
                        )
                        imageList.add(0, filePath)
                        imagePreviousPosition += 1
                        imageAdapter.updateAdapter(0)
                    }
                } else {
                    if (imageWidth > 500 && imageHeight > 500) {
                        showAlert(
                            context,
                            "Please select the logo image having size 500x500!"
                        )
                    } else {
                        // THIS CODE WILL GET THE CUSTOM LOGO IMAGE PATH AND SAVE INTO logoList
                        val filePath = ImageManager.saveImageInLocalStorage(
                            context,
                            data.data!!,
                            "logo"
                        )
                        logoList.add(0, filePath)
                        logoPreviousPosition += 1
                        logoAdapter.updateAdapter(0)
                    }

                }

            }
        }

    // THIS FUNCTION WILL HANDLE THE RUNTIME PERMISSION RESULT
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
    override fun onTypeSelected(data: String,position:Int) {
        var url = ""
        if (isDynamicActive && position == 1)
        {

            val hashMap = hashMapOf<String, String>()
            hashMap["login"] = "sattar"
            hashMap["qrId"] = System.currentTimeMillis().toString()
            hashMap["userUrl"] = data
            hashMap["userType"] = "free"

            startLoading(context)
            viewModel.createDynamicQrCode(context,hashMap)
            viewModel.getDynamicQrCode().observe(this, Observer { response ->
                dismiss()
                if (response != null){
                    url = response.get("generatedUrl").asString
                    url = if (url.contains(":8990")) {
                        url.replace(":8990","")
                    } else {
                        url
                    }
                    val qrEntity = QREntity(
                        hashMap["login"]!!,
                        hashMap["qrId"]!!,
                        hashMap["userUrl"]!!,
                        hashMap["userType"]!!,
                        url
                    )
                        encodedTextData = url
                        qrImage = QRGenerator.generatorQRImage(
                            context,
                            encodedTextData,
                            "",
                            "",
                            ""
                        )
                        qrGeneratedImage.setImageBitmap(qrImage)

                    appViewModel.insert(qrEntity)
                }
                else{
                    showAlert(context,"Something went wrong, please try again!")
                }
            })
        }
        else
        {
            encodedTextData = data
            qrImage = QRGenerator.generatorQRImage(
                context,
                encodedTextData,
                "",
                "",
                ""
            )
            qrGeneratedImage.setImageBitmap(qrImage)
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.barcode_history -> {
                startActivity(Intent(context,BarcodeHistoryActivity::class.java))
                true
            }
            R.id.dynamic_qr -> {
                startActivity(Intent(context,DynamicQrActivity::class.java))
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    }

}