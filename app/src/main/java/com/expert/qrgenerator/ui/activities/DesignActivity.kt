package com.expert.qrgenerator.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.ColorAdapter
import com.expert.qrgenerator.adapters.FontAdapter
import com.expert.qrgenerator.adapters.ImageAdapter
import com.expert.qrgenerator.adapters.LogoAdapter
import com.expert.qrgenerator.databinding.ActivityDesignBinding
import com.expert.qrgenerator.model.Fonts
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.ImageManager
import com.expert.qrgenerator.utils.QRGenerator
import com.expert.qrgenerator.utils.RuntimePermissionHelper
import com.expert.qrgenerator.viewmodel.DesignActivityViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DesignActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding:ActivityDesignBinding
    private lateinit var context: Context
    private var qrImage: Bitmap? = null
    private lateinit var colorAdapter: ColorAdapter
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var logoAdapter: LogoAdapter
    private lateinit var fontAdapter: FontAdapter
    private var colorList = mutableListOf<String>()
    private var imageList = mutableListOf<String>()
    private var logoList = mutableListOf<String>()
    private var fontList = mutableListOf<Fonts>()
    private var imagePreviousPosition = -1
    private var logoPreviousPosition = -1
    private var encodedTextData: String = " "
    private var secondaryInputText: String? = null
    private var intentType: String? = null
    private var bAlert: AlertDialog? = null
    private var lAlert: AlertDialog? = null
    private var isBackgroundSet: Boolean = false
    private val appViewModel: AppViewModel by viewModels()
    private val viewModel: DesignActivityViewModel by viewModels()
    private var qrHistory: CodeHistory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDesignBinding.inflate(layoutInflater)
        setContentView(binding.root)


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

        binding.nextStepBtn.setOnClickListener(this)

        binding.backgroundBtn.setOnClickListener(this)

        binding.colorBtn.setOnClickListener(this)

        binding.logoBtn.setOnClickListener(this)

        binding.textBtn.setOnClickListener(this)


        if (intent != null && intent.hasExtra("QR_HISTORY")) {
            qrHistory = intent.getSerializableExtra("QR_HISTORY") as CodeHistory
        }


        // START THE TEXT BOX LISTENER FOR SAVING UPDATED TEXT IN secondaryInputText VARIABLE
        binding.secondaryInputTextBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.isNotEmpty()) {
                    secondaryInputText = s.toString()
                    binding.qrText.text = secondaryInputText
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        if (intent != null && intent.hasExtra("ENCODED_TEXT")) {
            encodedTextData = intent.getStringExtra("ENCODED_TEXT")!!
            Log.d("TEST199", encodedTextData)
            qrImage = QRGenerator.generatorQRImage(
                context,
                encodedTextData,
                "",
                "",
                ""
            )
            binding.qrGeneratedImg.setImageBitmap(qrImage)
        }
        binding.qrSignText.visibility = View.VISIBLE

    }

    // THIS FUNCTION WILL RENDER THE ACTION BAR/TOOLBAR
    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = getString(R.string.design_customization)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.next_step_btn -> {
                val file = ImageManager.loadBitmapFromView(context,binding.qrImageWrapperLayout)
                val bitmap = ImageManager.getBitmapFromURL(context,file.absolutePath)
                if (bitmap != null){
                    val encodedText = ImageManager.getTextFromQRImage(context,bitmap)
                    if (encodedText.isNotEmpty()){
                        val uri = ImageManager.shareImage(context, binding.qrImageWrapperLayout)
                        Constants.finalQrImageUri = uri
                        qrHistory!!.localImagePath = uri.toString()
                        appViewModel.insert(qrHistory!!)
                        val intent = Intent(context, ShareActivity::class.java)
                        startActivity(intent)
                    }
                    else{
                        showAlert(context,getString(R.string.qr_code_not_recognizeable_error_text))
                    }
                }

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
            1 -> {
                binding.backgroundImagesRecyclerView.visibility = View.GONE
                binding.logoImagesRecyclerView.visibility = View.GONE
                binding.textFontLayoutWrapper.visibility = View.GONE

                if (binding.colorsRecyclerView.visibility == View.VISIBLE) {
                    binding.colorsRecyclerView.visibility = View.GONE
                } else {
                    binding.colorsRecyclerView.visibility = View.VISIBLE
                }
            }
            2 -> {
                binding.colorsRecyclerView.visibility = View.GONE
                binding.logoImagesRecyclerView.visibility = View.GONE
                binding.textFontLayoutWrapper.visibility = View.GONE

                if (binding.backgroundImagesRecyclerView.visibility == View.VISIBLE) {
                    binding.backgroundImagesRecyclerView.visibility = View.GONE
                } else {
                    binding.backgroundImagesRecyclerView.visibility = View.VISIBLE
                }
            }
            3 -> {
                binding.backgroundImagesRecyclerView.visibility = View.GONE
                binding.colorsRecyclerView.visibility = View.GONE
                binding.textFontLayoutWrapper.visibility = View.GONE

                if (binding.logoImagesRecyclerView.visibility == View.VISIBLE) {
                    binding.logoImagesRecyclerView.visibility = View.GONE
                } else {
                    binding.logoImagesRecyclerView.visibility = View.VISIBLE
                }
            }
            4 -> {
                binding.backgroundImagesRecyclerView.visibility = View.GONE
                binding.colorsRecyclerView.visibility = View.GONE
                binding.logoImagesRecyclerView.visibility = View.GONE

                if (binding.textFontLayoutWrapper.visibility == View.VISIBLE) {
                    binding.textFontLayoutWrapper.visibility = View.GONE
                } else {
                    binding.textFontLayoutWrapper.visibility = View.VISIBLE
                }
            }
            else -> {
                binding.backgroundImagesRecyclerView.visibility = View.GONE
                binding.colorsRecyclerView.visibility = View.GONE
                binding.logoImagesRecyclerView.visibility = View.GONE
                binding.textFontLayoutWrapper.visibility = View.GONE
            }
        }
    }

    // THIS FUNCTION WILL DISPLAY THE HORIZONTAL BACKGROUND IMAGE LIST
    private fun renderBackgroundImageRecyclerview() {
        // THIS LINE OF CODE WILL SET THE RECYCLERVIEW ORIENTATION (HORIZONTAL OR VERTICAL)
        binding.backgroundImagesRecyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL,
            false
        )
        binding.backgroundImagesRecyclerView.hasFixedSize()
        val localBackgroundImageList = Constants.getAllBackgroundImages(context)
        imageList.addAll(localBackgroundImageList)
        imageAdapter = ImageAdapter(context, imageList)
        binding.backgroundImagesRecyclerView.adapter = imageAdapter

        viewModel.callBackgroundImages()
        viewModel.getBackgroundImages().observe(this, { list ->
            if (list != null) {
                imageList.addAll(imageList.size, list)
                imageAdapter.notifyItemRangeInserted(imageList.size, list.size)
            }
        })

        // CLICK ON EACH IMAGE ITEM
        imageAdapter.setOnItemClickListener(object : ImageAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                if (imagePreviousPosition != position) {
                    imagePreviousPosition = position
                    qrImage = QRGenerator.generatorQRImage(
                        context,
                        encodedTextData,
                        "", imageList[position], ""
                    )
                    binding.qrGeneratedImg.setImageBitmap(qrImage)
//                    isBackgroundSet = qrImage != null
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
                    if (RuntimePermissionHelper.checkStoragePermission(
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
        binding.colorsRecyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL,
            false
        )
        binding.colorsRecyclerView.hasFixedSize()
        val customColorList = ImageManager.readColorFile(context)
        if (customColorList.isNotEmpty()) {
            val localColorList = customColorList.trim().split(" ")
            colorList.addAll(localColorList)
        }
        colorAdapter = ColorAdapter(context, colorList)
        binding.colorsRecyclerView.adapter = colorAdapter

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

                    colorAdapter.updateIcon(true)
                    if (previousPosition != position) {
                        previousPosition = position

                        qrImage = QRGenerator.generatorQRImage(
                            context,
                            encodedTextData,
                            colorList[position], "", ""
                        )
                        binding.qrGeneratedImg.setImageBitmap(qrImage)
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
                                getString(R.string.color_value_error_text),
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
                                    getString(R.string.color_valid_value_error_text),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, getString(R.string.color_empty_value_error_text), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        })

    }

    // THIS FUNCTION WILL DISPLAY THE HORIZONTAL LOGO IMAGE LIST
    private fun renderLogoImagesRecyclerview() {
        // THIS LINE OF CODE WILL SET THE RECYCLERVIEW ORIENTATION (HORIZONTAL OR VERTICAL)
        binding.logoImagesRecyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL,
            false
        )
        binding.logoImagesRecyclerView.hasFixedSize()
        val localLogoImageList = Constants.getAllLogoImages(context)
        logoList.addAll(localLogoImageList)
        logoAdapter = LogoAdapter(context, logoList)
        binding.logoImagesRecyclerView.adapter = logoAdapter

        viewModel.callLogoImages()
        viewModel.getLogoImages().observe(this, { list ->
            if (list != null) {
                logoList.addAll(logoList.size, list)
                logoAdapter.notifyItemRangeInserted(logoList.size, list.size)
            }
        })

        // CLICK ON EACH IMAGE ITEM
        logoAdapter.setOnItemClickListener(object : LogoAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {

                    logoAdapter.updateIcon(true)
                    if (logoPreviousPosition != position) {
                        logoPreviousPosition = position
                        qrImage = QRGenerator.generatorQRImage(
                            context,
                            encodedTextData,
                            "", "", logoList[position]
                        )
                        binding.qrGeneratedImg.setImageBitmap(qrImage)
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
                    if (RuntimePermissionHelper.checkStoragePermission(
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
        binding.fontsRecyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL,
            false
        )
        binding.fontsRecyclerView.hasFixedSize()
        fontAdapter = FontAdapter(context, fontList)
        binding.fontsRecyclerView.adapter = fontAdapter

        viewModel.callFontList()
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

                    fontAdapter.updateIcon(true)
                    if (previousPosition != position) {
                        previousPosition = position
                        if (!TextUtils.isEmpty(binding.secondaryInputTextBox.text.toString())) {
                            setFontFamily(context, binding.qrText, font.fontFile)
                        }
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
                            getString(R.string.background_image_size_error_text)
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
                            getString(R.string.logo_image_size_error_text)
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
                        .setMessage(getString(R.string.external_storage_permission_error1))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.ok_text)) { dialog, which ->
                            dialog.dismiss()
                        }
                        .create().show()
                }
            }
            else -> {

            }
        }
    }

    private fun quitWithoutSaveChanges() {

            MaterialAlertDialogBuilder(context)
                .setMessage(getString(R.string.changes_design_saved_alert_text))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton(getString(R.string.leave_text)) { dialog, which ->
                    QRGenerator.resetQRGenerator()
                    super.onBackPressed()
                }
                .create().show()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                quitWithoutSaveChanges()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    }

    override fun onBackPressed() {
        quitWithoutSaveChanges()
    }
}