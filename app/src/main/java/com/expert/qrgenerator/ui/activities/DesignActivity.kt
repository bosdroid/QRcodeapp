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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.ColorAdapter
import com.expert.qrgenerator.adapters.FontAdapter
import com.expert.qrgenerator.adapters.ImageAdapter
import com.expert.qrgenerator.adapters.LogoAdapter
import com.expert.qrgenerator.databinding.ActivityDesignBinding
import com.expert.qrgenerator.databinding.BackgroundImageHintLayoutBinding
import com.expert.qrgenerator.databinding.ColorInputDialogBinding
import com.expert.qrgenerator.databinding.LogoImageHintLayoutBinding
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.Fonts
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.GeneratorManager
import com.expert.qrgenerator.utils.ImageManager
import com.expert.qrgenerator.utils.RuntimePermissionHelper
import com.expert.qrgenerator.viewmodel.DesignActivityViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

@AndroidEntryPoint
class DesignActivity : BaseActivity(), View.OnClickListener {
    // View binding instance for accessing views in the layout
    private lateinit var binding: ActivityDesignBinding

    // Context of the activity, initialized using lazy delegation
    private val context: Context by lazy { this }

    // Bitmap for holding QR code image
    private var qrImage: Bitmap? = null

    // Adapters for various lists in the design activity
    private lateinit var colorAdapter: ColorAdapter
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var logoAdapter: LogoAdapter
    private lateinit var fontAdapter: FontAdapter

    // Lists to hold colors, images, logos, and fonts
    private var colorList = mutableListOf<String>()
    private var imageList = mutableListOf<String>()
    private var logoList = mutableListOf<String>()
    private var fontList = mutableListOf<Fonts>()

    // Variables to track previously selected positions for images and logos
    private var imagePreviousPosition = -1
    private var logoPreviousPosition = -1
    private var colorPreviousPosition = -1

    // Text data for encoding and secondary input text
    private var encodedTextData: String = " "
    private var secondaryInputText: String? = null

    // Type of intent used to determine the activity's behavior
    private var intentType: String? = null

    // AlertDialog instances for displaying alerts
    private var bAlert: AlertDialog? = null
    private var lAlert: AlertDialog? = null

    // Flag to check if the background is set
    private var isBackgroundSet: Boolean = false

    // ViewModels for managing UI-related data
    private val appViewModel: AppViewModel by viewModels()
    private val viewModel: DesignActivityViewModel by viewModels()

    // Object to hold QR code history data
    private var qrHistory: CodeHistory? = null

    private var fileName:String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityDesignBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views and set up the activity
        initViews()  // Setup any custom views or listeners

        // Configure toolbar
        setUpToolbar()  // Initialize and configure the toolbar for the activity

        // Render different types of content in RecyclerViews
        renderColorsRecyclerView()  // Set up RecyclerView for color options
        renderBackgroundImageRecyclerView()  // Set up RecyclerView for background images
        renderLogoImagesRecyclerView()  // Set up RecyclerView for logo images
        renderFontRecyclerView()  // Set up RecyclerView for font options
    }


    // Initialize all views and set up listeners
    private fun initViews() {

        // Set up button click listeners
        binding.nextStepBtn.setOnClickListener(this)
        binding.backgroundBtn.setOnClickListener(this)
        binding.colorBtn.setOnClickListener(this)
        binding.logoBtn.setOnClickListener(this)
        binding.textBtn.setOnClickListener(this)

        // Retrieve and cast the QR history if it exists in the intent
        intent?.let {
            if (it.hasExtra("QR_HISTORY")) {
                qrHistory = it.getSerializableExtra("QR_HISTORY") as? CodeHistory
            }

            // Retrieve the encoded text and generate QR code if it exists in the intent
            if (it.hasExtra("ENCODED_TEXT")) {
                encodedTextData = it.getStringExtra("ENCODED_TEXT") ?: ""
                Log.d("TEST199", encodedTextData)
                CoroutineScope(Dispatchers.Main).launch {
                    qrImage = GeneratorManager.generatorQRImage(
                        context,
                        encodedTextData,
                        "",
                        "",
                        ""
                    )
                    binding.qrGeneratedImg.setImageBitmap(qrImage)
                }
            }

            if (it.hasExtra("FILE_NAME")){
                fileName = it.getStringExtra("FILE_NAME") as String
            }
        }

        // Set up the text watcher for the secondary input text box
        binding.secondaryInputTextBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed before text changes
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Update the secondary input text and QR text if input is not empty
                if (s?.isNotEmpty() == true) {
                    secondaryInputText = s.toString()
                    binding.qrText.text = secondaryInputText
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // No action needed after text changes
            }
        })

        // Make QR sign text visible
        binding.qrSignText.visibility = View.VISIBLE
    }


    /**
     * Sets up the Action Bar/Toolbar with custom settings.
     */
    private fun setUpToolbar() {
        // Set the Toolbar as the Action Bar
        setSupportActionBar(binding.toolbar)

        // Get the support Action Bar and configure its properties
        supportActionBar?.apply {
            title = getString(R.string.design_customization)  // Set the title of the Action Bar
            setDisplayHomeAsUpEnabled(true)  // Enable the Up button (back navigation)
        }

        // Set the color of the Toolbar title text
        binding.toolbar.setTitleTextColor(
            ContextCompat.getColor(context, R.color.black)
        )
    }


    override fun onClick(v: View?) {
        // Ensure the view is not null
        v?.let { view ->
            when (view.id) {
                // Handle click event for "Next Step" button
                R.id.next_step_btn -> {
                    // Load the bitmap from the view
                    val file = ImageManager.loadBitmapFromView(context, binding.qrImageWrapperLayout)
                    val bitmap = ImageManager.getBitmapFromURL(context, file.absolutePath)

                    // Check if bitmap is not null
                    if (bitmap != null) {
                        // Extract text from QR image
                        val encodedText = ImageManager.getTextFromQRImage(context, bitmap)

                        // Check if text was successfully extracted
                        if (encodedText.isNotEmpty()) {
                            // Share the image and update URI
                            val uri = ImageManager.shareImage(context, binding.qrImageWrapperLayout)
                            Constants.finalQrImageUri = uri
                            qrHistory?.localImagePath = uri.toString()

                            // Insert QR history into the ViewModel
                            appViewModel.insert(qrHistory!!)
                            if (qrHistory!!.type == "vcard"){

                             startLoading(context)
                             viewModel.uploadQrImage(context,bitmap,fileName)
                             viewModel.uploadImageResponse.observe(this@DesignActivity, Observer { response->
                                 dismiss()
                                 // Start ShareActivity
                                 val intent = Intent(context, ShareActivity::class.java)
                                 startActivity(intent)
                             })
                            }
                            else{
                                // Start ShareActivity
                                val intent = Intent(context, ShareActivity::class.java)
                                startActivity(intent)
                            }

                        } else {
                            // Show error if QR code text is empty
                            showAlert(context, getString(R.string.qr_code_not_recognizeable_error_text))
                        }
                    }
                }
                // Handle click event for "Color" button
                R.id.color_btn -> viewVisibleInvisible(1)

                // Handle click event for "Background" button
                R.id.background_btn -> viewVisibleInvisible(2)

                // Handle click event for "Logo" button
                R.id.logo_btn -> viewVisibleInvisible(3)

                // Handle click event for "Text" button
                R.id.text_btn -> viewVisibleInvisible(4)

                // Handle other cases
                else -> { /* No action needed */ }
            }
        }
    }


    /**
     * Toggles the visibility of different views based on the given code.
     *
     * @param code An integer that determines which view's visibility should be toggled.
     *              1 - Toggles colorsRecyclerView
     *              2 - Toggles backgroundImagesRecyclerView
     *              3 - Toggles logoImagesRecyclerView
     *              4 - Toggles textFontLayoutWrapper
     */
    private fun viewVisibleInvisible(code: Int) {
        // Hide all views initially
        binding.backgroundImagesRecyclerView.visibility = View.GONE
        binding.colorsRecyclerView.visibility = View.GONE
        binding.logoImagesRecyclerView.visibility = View.GONE
        binding.textFontLayoutWrapper.visibility = View.GONE

        // Determine which view to toggle based on the input code
        when (code) {
            1 -> toggleViewVisibility(binding.colorsRecyclerView)
            2 -> toggleViewVisibility(binding.backgroundImagesRecyclerView)
            3 -> toggleViewVisibility(binding.logoImagesRecyclerView)
            4 -> toggleViewVisibility(binding.textFontLayoutWrapper)
        }
    }

    /**
     * Toggles the visibility of a view between VISIBLE and GONE.
     *
     * @param view The view whose visibility should be toggled.
     */
    private fun toggleViewVisibility(view: View) {
        view.visibility = if (view.visibility == View.VISIBLE) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    // THIS FUNCTION WILL DISPLAY THE HORIZONTAL BACKGROUND IMAGE LIST
    private fun renderBackgroundImageRecyclerView() {
        // Set the orientation of RecyclerView to horizontal
        binding.backgroundImagesRecyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL,
            false
        )
        // Improve performance by avoiding unnecessary measurements
        binding.backgroundImagesRecyclerView.setHasFixedSize(true)

        // Initialize the list of background images
        val localBackgroundImageList = Constants.getAllBackgroundImages(context)
        imageList.addAll(localBackgroundImageList)

        // Set up the ImageAdapter with the list of images
        imageAdapter = ImageAdapter(context, imageList)
        binding.backgroundImagesRecyclerView.adapter = imageAdapter

        // Observe the LiveData from the ViewModel for background images
        viewModel.callBackgroundImages()
        viewModel.backgroundImageList.observe(this@DesignActivity) { list ->
            list?.let {
                // Add new images to the list and notify the adapter
                val startIndex = imageList.size
                imageList.addAll(it)
                imageAdapter.notifyItemRangeInserted(startIndex, it.size)
            }
        }

        // Handle clicks on image items
        imageAdapter.setOnItemClickListener(object : ImageAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                // Check if the clicked item is different from the previously selected one
                if (imagePreviousPosition != position) {
                    imagePreviousPosition = position
                    // Generate QR image based on selected background image
                   CoroutineScope(Dispatchers.Main).launch {
                       qrImage = GeneratorManager.generatorQRImage(
                           context,
                           encodedTextData,
                           "",
                           imageList[position],
                           ""
                       )
                       binding.qrGeneratedImg.setImageBitmap(qrImage)
                   }
                    // Optionally update `isBackgroundSet` if needed
                    // isBackgroundSet = qrImage != null
                }
            }

            override fun onAddItemClick(position: Int) {
                // Start activity to add a custom background image
                showCustomBackgroundImageDialog()
            }
        })
    }

    /**
     * Displays a dialog to add a custom background image.
     */
    private fun showCustomBackgroundImageDialog() {
        // Inflate the custom dialog layout using ViewBinding
        val dialogbinding = BackgroundImageHintLayoutBinding.inflate(LayoutInflater.from(context))

        // Build and show the dialog
        val builder = MaterialAlertDialogBuilder(context)
            .setCancelable(false)
            .setView(dialogbinding.root)
        val bAlert = builder.create()
        bAlert.show()

        // Set up click listeners for the dialog buttons using ViewBinding
        dialogbinding.customImageCancelBtn.setOnClickListener { bAlert.dismiss() }
        dialogbinding.customImageAddBtn.setOnClickListener {
            if (RuntimePermissionHelper.checkStoragePermission(
                    context,
                    Constants.READ_STORAGE_PERMISSION
                )
            ) {
                getImageFromLocalStorage()
            }
        }
    }

    // THIS FUNCTION WILL DISPLAY THE HORIZONTAL COLORS LIST
    private fun renderColorsRecyclerView() {

        // Set up RecyclerView with horizontal orientation
        binding.colorsRecyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL,
            false
        )

        // Ensure the RecyclerView has a fixed size for better performance
        binding.colorsRecyclerView.setHasFixedSize(true)

        // Load custom color list from file and add to colorList
        val customColorList = ImageManager.readColorFile(context)
        if (customColorList.isNotEmpty()) {
            colorList.addAll(customColorList.trim().split(" "))
        }

        // Initialize ColorAdapter with the current colorList
        colorAdapter = ColorAdapter(colorList)
        binding.colorsRecyclerView.adapter = colorAdapter

        // Observe the color list from ViewModel
        viewModel.callColorList(context)
        viewModel.colorList.observe(this@DesignActivity) { colors ->
            // Add new colors to the list and notify the adapter
            colors?.let {
                val startIndex = colorList.size
                colorList.addAll(it)
                colorAdapter.notifyItemRangeInserted(startIndex, it.size)
            }
        }

        // Handle color item clicks
        colorAdapter.setOnItemClickListener(object : ColorAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                // Update icon for the selected color item
                colorAdapter.updateIcon(true)

                // Generate QR image for the selected color
                if (colorPreviousPosition != position) {
                    colorPreviousPosition = position
                    CoroutineScope(Dispatchers.Main).launch {
                        qrImage = GeneratorManager.generatorQRImage(
                            context,
                            encodedTextData,
                            colorList[position], "", ""
                        )
                        binding.qrGeneratedImg.setImageBitmap(qrImage)
                    }
                }
            }

            // Handle add button clicks to input custom color
            override fun onAddItemClick(position: Int) {
                // Inflate and set up the custom color input dialog
                val colorDialogBinding = ColorInputDialogBinding.inflate(
                    LayoutInflater.from(context),
                    null,
                    false
                )

                // Initialize dialog components using ViewBinding
                val colorInputBox = colorDialogBinding.customColorInputBox
                val cancelBtn = colorDialogBinding.customColorCancelBtn
                val addBtn = colorDialogBinding.customColorAddBtn

                // Create and show the dialog
                val builder = MaterialAlertDialogBuilder(context)
                builder.setCancelable(false)
                builder.setView(colorDialogBinding.root)
                val alert = builder.create()
                alert.show()

                // Handle cancel button click
                cancelBtn.setOnClickListener { alert.dismiss() }

                // Handle add button click
                addBtn.setOnClickListener {
                    val inputText = colorInputBox.text.toString()
                    when {
                        inputText.isEmpty() -> {
                            Toast.makeText(context, getString(R.string.color_empty_value_error_text), Toast.LENGTH_SHORT).show()
                        }
                        inputText.contains("#") -> {
                            Toast.makeText(context, getString(R.string.color_value_error_text), Toast.LENGTH_SHORT).show()
                        }
                        inputText.length != 6 -> {
                            Toast.makeText(context, getString(R.string.color_valid_value_error_text), Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            // Add valid color to the list and update the adapter
                            colorList.add(0, inputText)
                            colorPreviousPosition += 1
                            colorAdapter.updateAdapter(0)
                            ImageManager.writeColorValueToFile("$inputText ", context)
                            alert.dismiss()
                        }
                    }
                }
            }
        })
    }


    // THIS FUNCTION WILL DISPLAY THE HORIZONTAL LOGO IMAGE LIST
    private fun renderLogoImagesRecyclerView() {
        // Set up RecyclerView with horizontal orientation
        binding.logoImagesRecyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL,
            false
        )
        binding.logoImagesRecyclerView.setHasFixedSize(true)

        // Initialize logo list and adapter
        val localLogoImageList = Constants.getAllLogoImages(context)
        logoList.addAll(localLogoImageList)
        logoAdapter = LogoAdapter(context, logoList)
        binding.logoImagesRecyclerView.adapter = logoAdapter

        // Observe changes in logo image list from ViewModel
        viewModel.callLogoImages()
        viewModel.logoImageList.observe(this@DesignActivity) { list ->
            list?.let {
                val startPosition = logoList.size
                logoList.addAll(it)
                logoAdapter.notifyItemRangeInserted(startPosition, it.size)
            }
        }

        // Set up item click listener for images
        logoAdapter.setOnItemClickListener(object : LogoAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                // Update icon and QR code image if the position changes
                if (logoPreviousPosition != position) {
                    logoPreviousPosition = position
                    logoAdapter.updateIcon(true)
                    CoroutineScope(Dispatchers.Main).launch {
                        qrImage = GeneratorManager.generatorQRImage(
                            context,
                            encodedTextData,
                            "", "",
                            logoList[position]
                        )
                        binding.qrGeneratedImg.setImageBitmap(qrImage)
                    }
                }
            }

            override fun onAddItemClick(position: Int) {
                // Show dialog to select custom logo image
                showAddLogoImageDialog()
            }
        })
    }

    /**
     * Shows a dialog to add a custom logo image.
     */
    private fun showAddLogoImageDialog() {
        // Inflate the custom image dialog view using view binding
        val dialogBinding = LogoImageHintLayoutBinding.inflate(LayoutInflater.from(context))

        // Create and show the dialog
        val builder = MaterialAlertDialogBuilder(context)
            .setCancelable(false)
            .setView(dialogBinding.root)
        val lAlert = builder.create()
        lAlert.show()

        // Set up dialog buttons using view binding
        dialogBinding.customImageCancelBtn.setOnClickListener { lAlert.dismiss() }
        dialogBinding.customImageAddBtn.setOnClickListener {
            if (RuntimePermissionHelper.checkStoragePermission(
                    context,
                    Constants.READ_STORAGE_PERMISSION
                )
            ) {
                getImageFromLocalStorage()
            }
        }
    }


    // THIS FUNCTION WILL DISPLAY THE HORIZONTAL FONT LIST
    private fun renderFontRecyclerView() {
        var previousPosition = -1

        // Set the RecyclerView orientation to horizontal
        binding.fontsRecyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL,
            false
        )

        // Initialize and set the adapter for the RecyclerView
        fontAdapter = FontAdapter(context, fontList)
        binding.fontsRecyclerView.adapter = fontAdapter

        // Observe font list changes from the ViewModel
        viewModel.fontList.observe(this@DesignActivity) { list ->
            // Update the font list and notify the adapter of changes
            fontList.clear()
            fontList.addAll(list)
            fontAdapter.notifyDataSetChanged()
        }

        // Request font list from ViewModel
        viewModel.callFontList()

        // Handle font item clicks
        fontAdapter.setOnItemClickListener(object : FontAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val font = fontList[position]

                // Update the icon to reflect the selected state
                fontAdapter.updateIcon(true)

                // Update font only if the clicked item is different from the previous one
                if (previousPosition != position) {
                    previousPosition = position
                    // Apply the selected font to the text box if it's not empty
                    if (!TextUtils.isEmpty(binding.secondaryInputTextBox.text)) {
                        setFontFamily(context, binding.qrText, font.fontFile)
                    }
                }
            }
        })
    }

    /**
     * Launches an intent to pick an image from local storage.
     * This function initiates the process to select an image file from the device's gallery.
     */
    private fun getImageFromLocalStorage() {
        // Create an intent to pick an image
        val fileIntent = Intent(Intent.ACTION_PICK).apply {
            // Set the MIME type to 'image/*' to filter image files
            type = "image/*"
        }

        // Launch the intent using the result launcher
        resultLauncher.launch(fileIntent)
    }


    // THIS RESULT LAUNCHER WILL CALL THE ACTION PICK FROM FILES FOR BACKGROUND AND LOGO IMAGE
    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // Check if an image selection was successful
        if (result.resultCode == Activity.RESULT_OK) {
            // Retrieve the result data
            val data: Intent? = result.data
            data?.data?.let { imageUri ->
                // Get image dimensions
                val size = ImageManager.getImageWidthHeight(context, imageUri)
                val (imageWidth, imageHeight) = size.split(",").map { it.toInt() }

                // Determine the type of image and handle accordingly
                when (intentType) {
                    "background" -> {
                        // Dismiss the background alert dialog if it's not null
                        bAlert?.dismiss()

                        // Check if image dimensions are within acceptable range
                        if (imageWidth > 800 && imageHeight > 800) {
                            showAlert(context, getString(R.string.background_image_size_error_text))
                        } else {
                            // Save the image and update the image list
                            val filePath = ImageManager.saveImageInLocalStorage(context, imageUri, "background")
                            imageList.add(0, filePath)
                            imagePreviousPosition += 1
                            imageAdapter.updateAdapter(0)
                        }
                    }
                    "logo" -> {
                        // Dismiss the logo alert dialog if it's not null
                        lAlert?.dismiss()

                        // Check if image dimensions are within acceptable range
                        if (imageWidth > 500 && imageHeight > 500) {
                            showAlert(context, getString(R.string.logo_image_size_error_text))
                        } else {
                            // Save the image and update the logo list
                            val filePath = ImageManager.saveImageInLocalStorage(context, imageUri, "logo")
                            logoList.add(0, filePath)
                            logoPreviousPosition += 1
                            logoAdapter.updateAdapter(0)
                        }
                    }
                }
            } ?: run {
                // Handle the case where image data is null
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
                // Check if the permission request was granted
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with accessing storage
                    getImageFromLocalStorage()
                } else {
                    // Permission denied, show an alert dialog
                    showPermissionDeniedDialog()
                }
            }
            else -> {
                // Handle other request codes if needed
            }
        }
    }

    /**
     * Shows a dialog informing the user that permission was denied.
     */
    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(context)
            .setMessage(getString(R.string.external_storage_permission_error1))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.ok_text)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }


    /**
     * Displays an alert dialog to confirm quitting without saving changes.
     * If confirmed, resets the QR generator and navigates back.
     */
    private fun quitWithoutSaveChanges() {
        // Create and configure the MaterialAlertDialogBuilder
        MaterialAlertDialogBuilder(context).apply {
            // Set the dialog message
            setMessage(getString(R.string.changes_design_saved_alert_text))

            // Disable canceling the dialog by touching outside of it
            setCancelable(false)

            // Set the negative button with a cancel action
            setNegativeButton(getString(R.string.cancel_text)) { dialog, _ ->
                // Dismiss the dialog
                dialog.dismiss()
            }

            // Set the positive button with a confirmation action
            setPositiveButton(getString(R.string.leave_text)) { dialog, _ ->
                // Reset the QR generator
                GeneratorManager.resetQRGenerator()

                // Navigate back to the previous screen
                super.onBackPressed()
            }
        }.create().show() // Create and show the dialog
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Check which menu item was selected
        return when (item.itemId) {
            android.R.id.home -> {
                // Handle the home button click
                quitWithoutSaveChanges() // Call function to handle quitting without saving changes
                true // Indicate that the event was handled
            }
            else -> {
                // For other menu items, use the default behavior
                super.onOptionsItemSelected(item)
            }
        }
    }


    override fun onBackPressed() {
        // Call a custom method to handle the back press action
        // This method is responsible for quitting the activity without saving changes
        quitWithoutSaveChanges()
    }

}