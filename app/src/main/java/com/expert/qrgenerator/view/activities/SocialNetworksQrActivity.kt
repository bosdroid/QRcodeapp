package com.expert.qrgenerator.view.activities

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
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.SNIconsAdapter
import com.expert.qrgenerator.adapters.SocialNetworkAdapter
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.SNPayload
import com.expert.qrgenerator.model.SocialNetwork
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.ImageManager
import com.expert.qrgenerator.utils.RuntimePermissionHelper
import com.expert.qrgenerator.viewmodel.SocialNetworkQrViewModel
import com.expert.qrgenerator.viewmodelfactory.ViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import top.defaults.colorpicker.ColorPickerPopup
import java.util.*
import kotlin.collections.ArrayList


class SocialNetworksQrActivity : BaseActivity(), View.OnClickListener,
    SocialNetworkAdapter.OnItemClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var snHeaderImageEditHint: MaterialTextView
    private lateinit var snHeaderImageEditBtn: AppCompatImageView
    private lateinit var snBannerImageView: AppCompatImageView
    private lateinit var nextStepBtn: MaterialTextView
    private lateinit var snDetailsBackgroundColorEditBtn: AppCompatImageView
    private lateinit var snContentWrapperLayout: LinearLayout
    private lateinit var snTitleTextView: MaterialTextView
    private lateinit var snTextEditBtn: AppCompatImageView
    private lateinit var snDescriptionTextView: MaterialTextView
    private lateinit var snDescriptionEditBtn: AppCompatImageView
    private lateinit var snRecyclerView: RecyclerView
    private lateinit var adapeter: SocialNetworkAdapter

    //    private lateinit var shareFabBtn: FloatingActionButton
    private var socialNetworkList = mutableListOf<SocialNetwork>()
    private var updateType = ""
    private var snBannerImage: String = ""
    private var snContentDetailBackgroundColor = ""
    private var snTitleText = ""
    private var snTitleTextColor = ""
    private var snDescriptionText = ""
    private var snDescriptionTextColor = ""
    private var snSelectedSocialNetwork = ""
    private lateinit var viewModel: SocialNetworkQrViewModel
    private lateinit var appViewModel: AppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_social_networks_qr)

        initViews()
        setUpToolbar()
        generateSocialNetworkList()

    }

    // THIS FUNCTION WILL INITIALIZE ALL THE VIEWS AND REFERENCE OF OBJECTS
    private fun initViews() {
        context = this
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(SocialNetworkQrViewModel()).createFor()
        )[SocialNetworkQrViewModel::class.java]
        appViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(this.application)
        ).get(AppViewModel::class.java)
        toolbar = findViewById(R.id.toolbar)
        snHeaderImageEditHint = findViewById(R.id.sn_header_image_edit_hint)
        snHeaderImageEditBtn = findViewById(R.id.sn_header_image_edit_btn)
        snHeaderImageEditBtn.setOnClickListener(this)
        snBannerImageView = findViewById(R.id.sn_banner_image)
        snDetailsBackgroundColorEditBtn = findViewById(R.id.sn_details_background_color_edit_btn)
        snDetailsBackgroundColorEditBtn.setOnClickListener(this)
        snContentWrapperLayout = findViewById(R.id.sn_content_wrapper_layout)
        snTitleTextView = findViewById(R.id.sn_title_text)
        snTextEditBtn = findViewById(R.id.sn_text_edit_btn)
        snTextEditBtn.setOnClickListener(this)
        snDescriptionTextView = findViewById(R.id.sn_description_text)
        snDescriptionEditBtn = findViewById(R.id.sn_description_edit_btn)
        snDescriptionEditBtn.setOnClickListener(this)
        nextStepBtn = findViewById(R.id.next_step_btn)
        nextStepBtn.setOnClickListener(this)
        snRecyclerView = findViewById(R.id.sn_list_recyclerview)
        snRecyclerView.layoutManager = LinearLayoutManager(context)
        snRecyclerView.hasFixedSize()
        adapeter = SocialNetworkAdapter(context, socialNetworkList as ArrayList<SocialNetwork>)
        snRecyclerView.adapter = adapeter
        adapeter.setOnItemClickListener(this)
    }

    // THIS FUNCTION WILL RENDER THE ACTION BAR/TOOLBAR
    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.social_networks_qr)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    private fun generateSocialNetworkList() {
        socialNetworkList.add(
            SocialNetwork(
                "facebook",
                R.drawable.facebook,
                "Facebook",
                "facebook",
                "www.your-url.com",
                1
            )
        )
        socialNetworkList.add(
            SocialNetwork(
                "www",
                R.drawable.www,
                "Visit us online",
                "www",
                "www.your-website.com",
                0
            )
        )
        socialNetworkList.add(
            SocialNetwork(
                "youtube",
                R.drawable.youtube,
                "Youtube",
                "youtube",
                "www.your-url.com",
                0
            )
        )
        socialNetworkList.add(
            SocialNetwork(
                "instagram",
                R.drawable.instagram_sn,
                "Instagram",
                "instagram",
                "www.your-url.com",
                0
            )
        )
        socialNetworkList.add(
            SocialNetwork(
                "twitter",
                R.drawable.twitter,
                "Twitter",
                "twitter",
                "www.your-url.com",
                0
            )
        )
        socialNetworkList.add(
            SocialNetwork(
                "vk",
                R.drawable.vk,
                "VK",
                "vk",
                "www.your-url.com",
                0
            )
        )
        socialNetworkList.add(
            SocialNetwork(
                "telegram",
                R.drawable.telegram,
                "Telegram",
                "telegram",
                "www.your-url.com",
                0
            )
        )

        if (socialNetworkList.isNotEmpty()) {
            adapeter.notifyDataSetChanged()
        }
    }


    // THIS FUNCTION WILL HANDLE THE ON BACK ARROW CLICK EVENT
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.sn_header_image_edit_btn -> {
                if (RuntimePermissionHelper.checkStoragePermission(
                        context,
                        Constants.READ_STORAGE_PERMISSION
                    )
                ) {
                    getImageFromLocalStorage()
                }
            }
            R.id.sn_details_background_color_edit_btn -> {
                openColorDialog(snContentWrapperLayout)
            }
            R.id.sn_text_edit_btn -> {
                updateType = "sn_title"
                updateTextAndColor(snTitleTextView)
            }
            R.id.sn_description_edit_btn -> {
                updateType = "sn_description"
                updateTextAndColor(snDescriptionTextView)
            }
            R.id.next_step_btn -> {
                if (validation()) {
                    val selectedList = mutableListOf<SocialNetwork>()
                    for (i in 0 until socialNetworkList.size) {
                        val item = socialNetworkList[i]
                        if (item.isActive == 1) {
                            selectedList.add(item)
                        }
                    }

                    val requestJsonObject = SNPayload(
                        snBannerImage,
                        snContentDetailBackgroundColor,
                        snTitleText,
                        snTitleTextColor,
                        snDescriptionText,
                        snDescriptionTextColor,
                        selectedList as ArrayList<SocialNetwork>
                    )

                    startLoading(context)
                    viewModel.createSnQrCode(context, requestJsonObject)
                    viewModel.getSnQrCode().observe(this, Observer { response ->
                        var url = ""
                        dismiss()
                        if (response != null) {
                            Log.d("TEST199", response.toString())
                            url = response.get("generatedUrl").asString

                            // SETUP QR DATA HASMAP FOR HISTORY
                            val qrData = hashMapOf<String, String>()
                            qrData["login"] = "sattar"
                            qrData["qrId"] = "${System.currentTimeMillis()}"
                            qrData["userType"] = "free"

                            val qrHistory = CodeHistory(
                                qrData["login"]!!,
                                qrData["qrId"]!!,
                                url,
                                "sn",
                                qrData["userType"]!!,
                                "qr",
                                "create",
                                "",
                                "0",
                                "",
                                System.currentTimeMillis().toString()
                            )
//                            appViewModel.insert(qrHistory)
//                            val intent = Intent(context, MainActivity::class.java)
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//                            startActivity(intent)
//                            finish()
                            val intent = Intent(context, DesignActivity::class.java)
                            intent.putExtra("ENCODED_TEXT", url)
                            intent.putExtra("QR_HISTORY", qrHistory)
                            startActivity(intent)

                        } else {
                            showAlert(context, "Something went wrong, please try again!")
                        }
                    })
                }
            }
            else -> {

            }
        }
    }

    private fun validation(): Boolean {
        if (snBannerImage.isEmpty()) {
            showAlert(context, "Please select the banner image")
            return false
        } else if (snContentDetailBackgroundColor.isEmpty()) {
            showAlert(context, "Please select the background color")
            return false
        } else if (snTitleText.isEmpty()) {
            showAlert(context, "Please enter the title")
            return false
        } else if (snDescriptionText.isEmpty()) {
            showAlert(context, "Please enter the description")
            return false
        } else if (socialNetworkList.size == 0) {
            showAlert(context, "Please select the at least one social network from list")
            return false
        }
        return true
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

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {

                val data: Intent? = result.data
                val size = ImageManager.getImageWidthHeight(context, data!!.data!!)
                val imageWidth = size.split(",")[0].toInt()
                val imageHeight = size.split(",")[1].toInt()
                snBannerImage = ImageManager.convertImageToBase64(context, data.data!!)
                //val path = ImageManager.getRealPathFromUri(context, data.data!!)

                // THIS LINES OF CODE WILL RE SCALED THE IMAGE WITH ASPECT RATION AND SIZE 640 X 360
                val bitmapImage =
                    BitmapFactory.decodeFile(ImageManager.getRealPathFromUri(context, data.data!!))
                val nh = (bitmapImage.height * (640.0 / bitmapImage.width)).toInt()
                val scaled = Bitmap.createScaledBitmap(bitmapImage, 640, nh, true)
                snBannerImageView.setImageBitmap(scaled)
                snHeaderImageEditHint.visibility = View.GONE

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

    private fun openColorDialog(view: View) {
//        var defaultColor:Int?=null
//        if (snContentDetailBackgroundColor.isNotEmpty()){
//            defaultColor = Integer.parseInt(snContentDetailBackgroundColor.replaceFirst("#", ""), 16)
//        }
//        else{
//            defaultColor = Color.RED
//        }
        ColorPickerPopup.Builder(this)
            .initialColor(Color.RED) // Set initial color
            .enableBrightness(true) // Enable brightness slider or not
            .enableAlpha(true) // Enable alpha slider or not
            .okTitle("Choose")
            .cancelTitle("Cancel")
            .showIndicator(true)
            .showValue(true)
            .build()
            .show(view, object : ColorPickerPopup.ColorPickerObserver() {
                override fun onColorPicked(color: Int) {
                    val hexColor = "#" + Integer.toHexString(color).substring(2)
                    snContentWrapperLayout.setBackgroundColor(Color.parseColor(hexColor))
                    snContentDetailBackgroundColor = hexColor
                }

                fun onColor(color: Int, fromUser: Boolean) {

                }
            })
    }

    // THIS FUNCTION WILL UPDATE TEXT AND COLOR
    private fun updateTextAndColor(view: MaterialTextView) {
        val dialogLayout =
            LayoutInflater.from(context).inflate(R.layout.text_with_color_update_dialog, null)
        val cancelBtn =
            dialogLayout.findViewById<MaterialButton>(R.id.text_with_color_dialog_cancel_btn)
        val updateBtn =
            dialogLayout.findViewById<MaterialButton>(R.id.text_with_color_dialog_update_btn)
        val inputBox =
            dialogLayout.findViewById<TextInputEditText>(R.id.text_with_color_text_input_field)
        val saleBadgeWrapperLayout =
            dialogLayout.findViewById<LinearLayout>(R.id.text_with_color_sale_badge_wrapper)
        val saleBadgeSpinner =
            dialogLayout.findViewById<AppCompatSpinner>(R.id.text_with_color_sale_badge_selector)
        val customSaleBadgeView =
            dialogLayout.findViewById<TextInputEditText>(R.id.text_with_color_custom_sale_badge)
        val colorBtnView =
            dialogLayout.findViewById<AppCompatButton>(R.id.text_with_color_color_btn)
        val colorTextField =
            dialogLayout.findViewById<TextInputEditText>(R.id.text_with_color_color_tf)
        var selectedColor = ""
        when (updateType) {
            "sn_title" -> {
                if (snTitleText.isNotEmpty()) {
                    inputBox.setText(snTitleText)
                }
                if (snTitleTextColor.isEmpty()) {
                    selectedColor = colorTextField.text.toString()
                } else {
                    selectedColor = snTitleTextColor
                    colorTextField.setText(selectedColor)
                    colorBtnView.setBackgroundColor(Color.parseColor(selectedColor))
                }
            }
            "sn_description" -> {
                if (snDescriptionText.isNotEmpty()) {
                    inputBox.setText(snDescriptionText)
                }
                if (snDescriptionTextColor.isEmpty()) {
                    selectedColor = colorTextField.text.toString()
                } else {
                    selectedColor = snDescriptionTextColor
                    colorTextField.setText(selectedColor)
                    colorBtnView.setBackgroundColor(Color.parseColor(selectedColor))
                }
            }
            else -> {

            }
        }

        saleBadgeWrapperLayout.visibility = View.GONE
        inputBox.visibility = View.VISIBLE

        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(dialogLayout)
        builder.setCancelable(false)
        val alert = builder.create()
        alert.show()

        colorBtnView.setOnClickListener {
            ColorPickerPopup.Builder(this)
                .initialColor(Color.RED) // Set initial color
                .enableBrightness(true) // Enable brightness slider or not
                .enableAlpha(true) // Enable alpha slider or not
                .okTitle("Choose")
                .cancelTitle("Cancel")
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

                    fun onColor(color: Int, fromUser: Boolean) {

                    }
                })
        }

        cancelBtn.setOnClickListener { alert.dismiss() }
        updateBtn.setOnClickListener {


            view.text = inputBox.text.toString()
            if (selectedColor.isNotEmpty()) {
                view.setTextColor(Color.parseColor(selectedColor))
            }

            val value = view.text.toString().trim()
            when (updateType) {
                "sn_title" -> {
                    snTitleText = value
                    snTitleTextColor = selectedColor
                }
                "sn_description" -> {
                    snDescriptionText = value
                    snDescriptionTextColor = selectedColor
                }
                else -> {

                }
            }

            alert.dismiss()
        }
    }

    override fun onItemClick(position: Int) {
        val item = socialNetworkList[position]

        val snLayout =
            LayoutInflater.from(context).inflate(R.layout.sn_update_dialog_layout, null)
        val snTitleEditText =
            snLayout.findViewById<TextInputEditText>(R.id.sn_title_input_field)
        snTitleEditText.setText(item.title)
        val snDescriptionEditText =
            snLayout.findViewById<TextInputEditText>(R.id.sn_description_input_field)
        snDescriptionEditText.setText(item.url)
        val cancelBtn = snLayout.findViewById<MaterialButton>(R.id.dialog_cancel_btn)
        val updateBtn = snLayout.findViewById<MaterialButton>(R.id.dialog_update_btn)

        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(snLayout)
        builder.setCancelable(false)
        val alert = builder.create()
        alert.show()

        cancelBtn.setOnClickListener { alert.dismiss() }
        updateBtn.setOnClickListener {
            if (snTitleEditText.text.toString().trim().isNotEmpty()
                && snDescriptionEditText.text.toString().trim().isNotEmpty()
            ) {

                val value = snDescriptionEditText.text.toString().trim().toLowerCase(Locale.ENGLISH)
                if (value.substring(0, 7).contains("http://") || value.substring(0, 8).contains("https://")
                    || value.contains("www") || value.contains(".com")) {
                    showAlert(
                        context,
                        "Please enter the plain text!"
                    )
                } else {
                    item.title = snTitleEditText.text.toString().trim()
                    item.url = snDescriptionEditText.text.toString().trim()
                    socialNetworkList.removeAt(position)
                    socialNetworkList.add(position, item)
                    adapeter.notifyItemChanged(position)
                    alert.dismiss()
                    Toast.makeText(context, "List Item updated successfully!", Toast.LENGTH_SHORT)
                        .show()
                }

            } else {
                showAlert(context, "Please enter the social network title and description.")
            }
        }

    }

    override fun onItemCheckClick(position: Int, isChecked: Boolean) {
        val item = socialNetworkList[position]
        if (item.isActive == 0 && isChecked) {
            item.isActive = 1
            socialNetworkList.removeAt(position)
            socialNetworkList.add(position, item)
            adapeter.notifyDataSetChanged()
        } else if (item.isActive == 1 && !isChecked) {
            item.isActive = 0
            socialNetworkList.removeAt(position)
            socialNetworkList.add(position, item)
            adapeter.notifyDataSetChanged()
        }
    }

    val iconsList = mutableListOf<Pair<String, Int>>()
    override fun onItemEditIconClick(position: Int, checkBox: MaterialCheckBox) {
        if (checkBox.isChecked) {
            val item = socialNetworkList[position]
            generateIconsList()
            val snIconsLayout =
                LayoutInflater.from(context).inflate(R.layout.sn_icons_layout_dialog, null)
            val snIconsRecyclerview =
                snIconsLayout.findViewById<RecyclerView>(R.id.sn_icons_recyclerview)
            snIconsRecyclerview.layoutManager = GridLayoutManager(context, 3)
            snIconsRecyclerview.hasFixedSize()
            val iconsAdapter = SNIconsAdapter(context, iconsList as ArrayList<Pair<String, Int>>)
            snIconsRecyclerview.adapter = iconsAdapter

            val builder = MaterialAlertDialogBuilder(context)
            builder.setView(snIconsLayout)
            val alert = builder.create()
            alert.show()

            iconsAdapter.setOnItemClickListener(object : SNIconsAdapter.OnItemClickListener {
                override fun onItemClick(pos: Int) {
                    val pair = iconsList[pos]
                    item.icon = pair.second
                    item.iconName = pair.first
                    item.description = pair.first

                    socialNetworkList.removeAt(position)
                    socialNetworkList.add(position, item)
                    adapeter.notifyItemChanged(position)
                    alert.dismiss()
                }
            })
        }
    }

    private fun generateIconsList() {
        if (iconsList.isNotEmpty()) {
            iconsList.clear()
        }
        iconsList.add(Pair("facebook", R.drawable.facebook))
        iconsList.add(Pair("www", R.drawable.www))
        iconsList.add(Pair("youtube", R.drawable.youtube))
        iconsList.add(Pair("instagram", R.drawable.instagram_sn))
        iconsList.add(Pair("twitter", R.drawable.twitter))
        iconsList.add(Pair("vk", R.drawable.vk))
        iconsList.add(Pair("telegram", R.drawable.telegram))
    }
}