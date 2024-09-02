package com.expert.qrgenerator.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.SNIconsAdapter
import com.expert.qrgenerator.adapters.SocialNetworkAdapter
import com.expert.qrgenerator.databinding.ActivitySocialNetworksQrBinding
import com.expert.qrgenerator.databinding.SnIconsLayoutDialogBinding
import com.expert.qrgenerator.databinding.SnUpdateDialogLayoutBinding
import com.expert.qrgenerator.databinding.TextWithColorUpdateDialogBinding
import com.expert.qrgenerator.model.SNPayload
import com.expert.qrgenerator.model.SocialNetwork
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.GeneratorManager
import com.expert.qrgenerator.utils.ImageManager
import com.expert.qrgenerator.utils.RuntimePermissionHelper
import com.expert.qrgenerator.viewmodel.SocialNetworkQrViewModel
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import top.defaults.colorpicker.ColorPickerPopup
import java.util.Locale

@AndroidEntryPoint
class SocialNetworksQrActivity : BaseActivity(), View.OnClickListener,
    SocialNetworkAdapter.OnItemClickListener {

    // View Binding for ActivitySocialNetworksQr layout
    private lateinit var binding: ActivitySocialNetworksQrBinding

    // Context of the activity, initialized using lazy delegation
    private val context: Context by lazy { this }

    // Adapter for social network items
    private lateinit var adapter: SocialNetworkAdapter

    // List of social network objects
    private var socialNetworkList = mutableListOf<SocialNetwork>()

    // Type of update to be performed
    private var updateType: String = ""

    // Banner image URL for social network
    private var snBannerImage: String = ""

    // Background color for social network content detail
    private var snContentDetailBackgroundColor: String = ""

    // Title text for social network
    private var snTitleText: String = ""

    // Title text color for social network
    private var snTitleTextColor: String = ""

    // Description text for social network
    private var snDescriptionText: String = ""

    // Description text color for social network
    private var snDescriptionTextColor: String = ""

    // Selected social network identifier
    private var snSelectedSocialNetwork: String = ""

    // ViewModel for handling social network QR data
    private val viewModel: SocialNetworkQrViewModel by viewModels()

    // ViewModel for application-wide data
    private val appViewModel: AppViewModel by viewModels()

    // List of icons represented as pairs of string (icon name) and integer (icon resource ID)
    val iconsList = mutableListOf<Pair<String, Int>>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivitySocialNetworksQrBinding.inflate(layoutInflater)

        // Set the content view using the root of the ViewBinding
        setContentView(binding.root)

        // Initialize view components and set up any necessary configurations
        initViews()

        // Set up the toolbar with any required settings or customization
        setUpToolbar()

        // Generate and display the list of social networks
        generateSocialNetworkList()
    }

    // THIS FUNCTION WILL INITIALIZE ALL THE VIEWS AND REFERENCE OF OBJECTS
    private fun initViews() {

        // Set click listeners for various buttons
        binding.snHeaderImageEditBtn.setOnClickListener(this)
        binding.snDetailsBackgroundColorEditBtn.setOnClickListener(this)
        binding.snTextEditBtn.setOnClickListener(this)
        binding.snDescriptionEditBtn.setOnClickListener(this)
        binding.nextStepBtn.setOnClickListener(this)

        // Set up RecyclerView with LinearLayoutManager
        binding.snListRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true) // Improve performance for fixed-size items
            adapter = SocialNetworkAdapter(socialNetworkList as ArrayList<SocialNetwork>)
            // Set item click listener for the adapter
            (adapter as? SocialNetworkAdapter)?.setOnItemClickListener(this@SocialNetworksQrActivity)
        }
    }

    // THIS FUNCTION WILL RENDER THE ACTION BAR/TOOLBAR
    /**
     * Sets up the toolbar with the appropriate title and styling.
     */
    private fun setUpToolbar() {
        // Set the toolbar as the action bar
        setSupportActionBar(binding.toolbar)

        // Set the title of the action bar
        supportActionBar?.title = getString(R.string.social_networks_qr)

        // Enable the display of the "up" button (back navigation) in the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set the title text color of the toolbar
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.black))
    }


    /**
     * Generates a list of social network objects and updates the adapter.
     */
    private fun generateSocialNetworkList() {
        // List of social network data to add
        val socialNetworks = listOf(
            SocialNetwork("facebook", R.drawable.facebook, "Facebook", "facebook", "www.your-url.com", 1),
            SocialNetwork("www", R.drawable.www, "Visit us online", "www", "www.your-website.com", 0),
            SocialNetwork("youtube", R.drawable.youtube, "YouTube", "youtube", "www.your-url.com", 0),
            SocialNetwork("instagram", R.drawable.instagram_sn, "Instagram", "instagram", "www.your-url.com", 0),
            SocialNetwork("twitter", R.drawable.twitter, "Twitter", "twitter", "www.your-url.com", 0),
            SocialNetwork("vk", R.drawable.vk, "VK", "vk", "www.your-url.com", 0),
            SocialNetwork("telegram", R.drawable.telegram, "Telegram", "telegram", "www.your-url.com", 0)
        )

        // Add all social networks to the list
        socialNetworkList.addAll(socialNetworks)

        // Notify the adapter if the list is not empty
        if (socialNetworkList.isNotEmpty()) {
            adapter.notifyDataSetChanged()
        }
    }


    // THIS FUNCTION WILL HANDLE THE ON BACK ARROW CLICK EVENT
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Check if the selected item is the "home" button
        return when (item.itemId) {
            android.R.id.home -> {
                // Handle the "home" button click
                onBackPressed()
                true
            }
            else -> {
                // Handle other menu items
                super.onOptionsItemSelected(item)
            }
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.sn_header_image_edit_btn -> handleImageEdit()
            R.id.sn_details_background_color_edit_btn -> handleColorEdit()
            R.id.sn_text_edit_btn -> handleTextEdit("sn_title", binding.snTitleText)
            R.id.sn_description_edit_btn -> handleTextEdit("sn_description", binding.snDescriptionText)
            R.id.next_step_btn -> handleNextStep()
            else -> {
                // Handle unexpected view ids if necessary
            }
        }
    }

    // Handle image edit button click
    private fun handleImageEdit() {
        if (RuntimePermissionHelper.checkStoragePermission(
                context,
                Constants.READ_STORAGE_PERMISSION
            )
        ) {
            getImageFromLocalStorage()
        }
    }

    // Handle background color edit button click
    private fun handleColorEdit() {
        openColorDialog(binding.snContentWrapperLayout)
    }

    // Handle text edit button click
    private fun handleTextEdit(updateType: String, textView: MaterialTextView) {
        this.updateType = updateType
        updateTextAndColor(textView)
    }

    // Handle the next step button click
    private fun handleNextStep() {
        if (validation()) {
            // Collect active social network items
            val selectedList = socialNetworkList.filter { it.isActive == 1 }

            // Create the payload object for the request
            val requestJsonObject = SNPayload(
                snBannerImage,
                snContentDetailBackgroundColor,
                snTitleText,
                snTitleTextColor,
                snDescriptionText,
                snDescriptionTextColor,
                ArrayList(selectedList)
            )

            // Start loading state
            startLoading(context)

            // Launch coroutine for network operation
            lifecycleScope.launch {
                viewModel.createSnQrCode(requestJsonObject)
            }

            // Observe the response
            viewModel.snQrCodeResponse.observe(this, Observer { response ->
                if (response != null) {
                    Log.d("TEST199", response.toString())
                    val url = response.get("generatedUrl").asString
                    GeneratorManager.generateQRCode(this@SocialNetworksQrActivity,url,"sn")
                }
            })
        }
    }

    /**
     * Validates the input fields for the form.
     *
     * @return true if all validations pass, false otherwise.
     */
    private fun validation(): Boolean {
        // Check if banner image is empty
        if (snBannerImage.isEmpty()) {
            showAlert(context, getString(R.string.sn_banner_image_error_text))
            return false
        }

        // Check if background color is empty
        if (snContentDetailBackgroundColor.isEmpty()) {
            showAlert(context, getString(R.string.sn_background_color_error_text))
            return false
        }

        // Check if title text is empty
        if (snTitleText.isEmpty()) {
            showAlert(context, getString(R.string.sn_title_error_text))
            return false
        }

        // Check if description text is empty
        if (snDescriptionText.isEmpty()) {
            showAlert(context, getString(R.string.sn_description_error_text))
            return false
        }

        // Check if social network list is empty
        if (socialNetworkList.isEmpty()) {
            showAlert(context, getString(R.string.sn_list_empty_error_text))
            return false
        }

        // All validations passed
        return true
    }


    // THIS FUNCTION WILL CALL THE IMAGE INTENT
    private fun getImageFromLocalStorage() {
        // Create an intent to pick an image from the device's storage
        val fileIntent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"  // Specify that we want to pick images
        }

        // Launch the intent using the result launcher
        resultLauncher.launch(fileIntent)
    }

    // THIS RESULT LAUNCHER WILL CALL THE ACTION PICK FROM FILES FOR BACKGROUND AND LOGO IMAGE
    // Register an ActivityResultLauncher to handle result from image selection
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // Check if the result code indicates a successful operation
            if (result.resultCode == Activity.RESULT_OK) {
                // Retrieve the selected image data from the Intent
                val data: Intent? = result.data
                data?.data?.let { uri ->

                    // Get image dimensions
                    val size = ImageManager.getImageWidthHeight(context, uri)
                    val (imageWidth, imageHeight) = size.split(",").map { it.toInt() }

                    // Convert image to Base64 format
                    snBannerImage = ImageManager.convertImageToBase64(context, uri)

                    // Decode the image file into a Bitmap
                    val realPath = ImageManager.getRealPathFromUri(context, uri)
                    val bitmapImage = BitmapFactory.decodeFile(realPath)

                    // Calculate new height to maintain aspect ratio (640 x 360)
                    val newHeight = (bitmapImage.height * (640.0 / bitmapImage.width)).toInt()

                    // Scale the image to the desired size while maintaining aspect ratio
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmapImage, 640, newHeight, true)

                    // Update UI with the scaled image
                    binding.snBannerImage.setImageBitmap(scaledBitmap)
                    binding.snHeaderImageEditHint.visibility = View.GONE
                    binding.lavSnHeaderImageEditBtn.visibility = View.GONE
                    binding.snHeaderImageEditBtn.setImageResource(R.drawable.green_checked_icon)
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

        // Handle the result of the permission request
        when (requestCode) {
            Constants.READ_STORAGE_REQUEST_CODE -> {
                // Check if the permission request was granted
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with accessing local storage
                    getImageFromLocalStorage()
                } else {
                    // Permission denied, show an alert dialog with an error message
                    MaterialAlertDialogBuilder(context)
                        .setMessage(getString(R.string.external_storage_permission_error1))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.ok_text)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
            }
            // Handle other request codes if needed
            else -> {
                // Optionally handle other permission requests here
            }
        }
    }


    private fun openColorDialog(view: View) {
        // Build and configure the ColorPickerPopup
        ColorPickerPopup.Builder(this)
            .initialColor(Color.RED) // Set the initial color to red
            .enableBrightness(true) // Enable the brightness slider
            .enableAlpha(true) // Enable the alpha (transparency) slider
            .okTitle(getString(R.string.chose_text)) // Set the title for the OK button
            .cancelTitle(getString(R.string.cancel_text)) // Set the title for the Cancel button
            .showIndicator(true) // Show the color indicator
            .showValue(true) // Show the color value
            .build()
            .show(view, object : ColorPickerPopup.ColorPickerObserver() {
                /**
                 * Called when a color is picked.
                 *
                 * @param color The selected color.
                 */
                override fun onColorPicked(color: Int) {
                    // Convert the color to a hexadecimal string
                    val hexColor = "#" + Integer.toHexString(color).substring(2).toUpperCase()

                    // Update the background color of the layout
                    binding.snContentWrapperLayout.setBackgroundColor(Color.parseColor(hexColor))

                    // Update the stored color value
                    snContentDetailBackgroundColor = hexColor

                    // Hide the hint text and edit button
                    binding.snDetailsBackgroundColorHintText.visibility = View.GONE
                    binding.lavSnDetailsBackgroundColorEditBtn.visibility = View.GONE

                    // Update the edit button image resource to indicate the color is selected
                    binding.snDetailsBackgroundColorEditBtn.setImageResource(R.drawable.green_checked_icon)
                }

                // Optional: This method is not used but can be implemented if needed
                fun onColor(color: Int, fromUser: Boolean) {
                    // Add implementation if needed
                }
            })
    }

    // THIS FUNCTION WILL UPDATE TEXT AND COLOR
    private fun updateTextAndColor(view: MaterialTextView) {
        // Inflate the dialog layout using ViewBinding
        val dialogBinding = TextWithColorUpdateDialogBinding.inflate(LayoutInflater.from(context))

        // Initialize the views from the binding
        val cancelBtn = dialogBinding.textWithColorDialogCancelBtn
        val updateBtn = dialogBinding.textWithColorDialogUpdateBtn
        val inputBox = dialogBinding.textWithColorTextInputField
        val saleBadgeWrapperLayout = dialogBinding.textWithColorSaleBadgeWrapper
        val saleBadgeSpinner = dialogBinding.textWithColorSaleBadgeSelector
        val customSaleBadgeView = dialogBinding.textWithColorCustomSaleBadge
        val colorBtnView = dialogBinding.textWithColorColorBtn
        val colorTextField = dialogBinding.textWithColorColorTf

        // Set initial values based on updateType
        var selectedColor = ""
        when (updateType) {
            "sn_title" -> {
                if (snTitleText.isNotEmpty()) {
                    inputBox.setText(snTitleText)
                }
                selectedColor = if (snTitleTextColor.isEmpty()) {
                    colorTextField.text.toString()
                } else {
                    colorTextField.setText(snTitleTextColor)
                    colorBtnView.setBackgroundColor(Color.parseColor(snTitleTextColor))
                    snTitleTextColor
                }
            }
            "sn_description" -> {
                if (snDescriptionText.isNotEmpty()) {
                    inputBox.setText(snDescriptionText)
                }
                selectedColor = if (snDescriptionTextColor.isEmpty()) {
                    colorTextField.text.toString()
                } else {
                    colorTextField.setText(snDescriptionTextColor)
                    colorBtnView.setBackgroundColor(Color.parseColor(snDescriptionTextColor))
                    snDescriptionTextColor
                }
            }
            else -> { /* No action needed for other cases */ }
        }

        // Hide unnecessary views and show inputBox
        saleBadgeWrapperLayout.visibility = View.GONE
        inputBox.visibility = View.VISIBLE

        // Build and show the alert dialog
        val alert = MaterialAlertDialogBuilder(context)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
        alert.show()

        // Handle color button click to open color picker
        colorBtnView.setOnClickListener {
            ColorPickerPopup.Builder(this)
                .initialColor(Color.RED) // Set initial color
                .enableBrightness(true) // Enable brightness slider
                .enableAlpha(true) // Enable alpha slider
                .okTitle(getString(R.string.chose_text))
                .cancelTitle(getString(R.string.cancel_text))
                .showIndicator(true)
                .showValue(true)
                .build()
                .show(colorBtnView, object : ColorPickerPopup.ColorPickerObserver() {
                    override fun onColorPicked(color: Int) {
                        val hexColor = "#" + Integer.toHexString(color).substring(2)
                        colorBtnView.setBackgroundColor(Color.parseColor(hexColor))
                        colorTextField.setText(hexColor)
                        selectedColor = hexColor
                    }
                })
        }

        // Handle cancel button click to dismiss the dialog
        cancelBtn.setOnClickListener { alert.dismiss() }

        // Handle update button click to update the view
        updateBtn.setOnClickListener {
            val value = view.text.toString().trim()
            if (value.isNotEmpty()) {
                view.text = value
                if (selectedColor.isNotEmpty()) {
                    view.setTextColor(Color.parseColor(selectedColor))
                }

                // Update the text and color values based on updateType
                when (updateType) {
                    "sn_title" -> {
                        snTitleText = value
                        snTitleTextColor = selectedColor
                        binding.lavSnTextEditBtn.visibility = View.GONE
                        binding.snTextEditBtn.setImageResource(R.drawable.green_checked_icon)
                    }
                    "sn_description" -> {
                        snDescriptionText = value
                        snDescriptionTextColor = selectedColor
                        binding.lavSnDescriptionEditBtn.visibility = View.GONE
                        binding.snDescriptionEditBtn.setImageResource(R.drawable.green_checked_icon)
                    }
                    else -> { /* No action needed for other cases */ }
                }
                alert.dismiss()
            } else {
                // Show an error if the text is empty
                showAlert(context, getString(R.string.empty_text_error))
            }
        }
    }

    override fun onItemClick(position: Int) {
        // Get the item at the clicked position
        val item = socialNetworkList[position]

        // Inflate the dialog layout using ViewBinding
        val dialogBinding = SnUpdateDialogLayoutBinding.inflate(LayoutInflater.from(context))

        // Set initial values for the EditTexts
        dialogBinding.snTitleInputField.setText(item.title)
        dialogBinding.snDescriptionInputField.hint = item.url

        // Create and show the dialog
        val alert = MaterialAlertDialogBuilder(context)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        alert.show()

        // Set up listeners for the buttons
        dialogBinding.dialogCancelBtn.setOnClickListener { alert.dismiss() }

        dialogBinding.dialogUpdateBtn.setOnClickListener {
            val title = dialogBinding.snTitleInputField.text.toString().trim()
            val url = dialogBinding.snDescriptionInputField.text.toString().trim()

            // Validate input
            if (title.isNotEmpty() && url.isNotEmpty()) {
                val lowerCaseUrl = url.toLowerCase(Locale.ENGLISH)

                if (lowerCaseUrl.contains("http://") || lowerCaseUrl.contains("https://")) {
                    showAlert(context, getString(R.string.without_protocol_error))
                } else {
                    // Update the item and refresh the list
                    item.title = title
                    item.url = url
                    socialNetworkList[position] = item
                    adapter.notifyItemChanged(position)

                    alert.dismiss()
                    Toast.makeText(context, getString(R.string.list_item_update_success_text), Toast.LENGTH_SHORT).show()
                }
            } else {
                showAlert(context, getString(R.string.sn_title_description_error_text))
            }
        }
    }

    override fun onItemCheckClick(position: Int, isChecked: Boolean) {
        // Retrieve the item at the specified position
        val item = socialNetworkList[position]

        // Check if the item status needs to be updated
        if (item.isActive != if (isChecked) 1 else 0) {
            // Update the item's active status
            item.isActive = if (isChecked) 1 else 0

            // Notify adapter of the changes
            // Update the list and notify adapter of data changes
            socialNetworkList[position] = item
            adapter.notifyDataSetChanged()
        }
    }

    override fun onItemEditIconClick(position: Int, checkBox: MaterialCheckBox) {
        // Check if the checkbox is checked
        if (checkBox.isChecked) {
            // Get the selected item from the list
            val item = socialNetworkList[position]

            // Generate the list of icons
            generateIconsList()

            // Inflate the dialog layout using ViewBinding
            val snIconsBinding = SnIconsLayoutDialogBinding.inflate(LayoutInflater.from(context))
            val snIconsRecyclerView = snIconsBinding.snIconsRecyclerview

            // Set up the RecyclerView with GridLayoutManager and adapter
            snIconsRecyclerView.layoutManager = GridLayoutManager(context, 3)
            snIconsRecyclerView.setHasFixedSize(true)
            val iconsAdapter = SNIconsAdapter(iconsList as ArrayList<Pair<String, Int>>)
            snIconsRecyclerView.adapter = iconsAdapter

            // Create and show the dialog using MaterialAlertDialogBuilder
            val builder = MaterialAlertDialogBuilder(context)
            builder.setView(snIconsBinding.root)
            val alert = builder.create()
            alert.show()

            // Set item click listener for the icons adapter
            iconsAdapter.setOnItemClickListener(object : SNIconsAdapter.OnItemClickListener {
                override fun onItemClick(pos: Int) {
                    // Get the selected icon
                    val pair = iconsList[pos]

                    // Update the item with the selected icon details
                    item.icon = pair.second
                    item.iconName = pair.first
                    item.description = pair.first

                    // Update the list and notify the adapter
                    socialNetworkList[position] = item
                    adapter.notifyItemChanged(position)

                    // Dismiss the dialog
                    alert.dismiss()
                }
            })
        }
    }


    private fun generateIconsList() {
        // Check if iconsList is not empty and clear it
        if (iconsList.isNotEmpty()) {
            iconsList.clear()
        }

        // Populate iconsList with pairs of icon names and drawable resources
        iconsList.apply {
            add(Pair("facebook", R.drawable.facebook))
            add(Pair("www", R.drawable.www))
            add(Pair("youtube", R.drawable.youtube))
            add(Pair("instagram", R.drawable.instagram_sn))
            add(Pair("twitter", R.drawable.twitter))
            add(Pair("vk", R.drawable.vk))
            add(Pair("telegram", R.drawable.telegram))
        }
    }

}