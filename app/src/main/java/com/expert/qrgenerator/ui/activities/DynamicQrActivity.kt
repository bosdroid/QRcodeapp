package com.expert.qrgenerator.ui.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.expert.qrgenerator.adapters.DynamicQrAdapter
import com.expert.qrgenerator.databinding.ActivityDynamicQrBinding
import com.expert.qrgenerator.databinding.UpdateDynamicUrlDialogLayoutBinding
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.viewmodel.DynamicQrViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DynamicQrActivity : BaseActivity(), DynamicQrAdapter.OnItemClickListener {

    // View binding instance to access views efficiently
    private lateinit var binding: ActivityDynamicQrBinding

    // Context of the activity, initialized using lazy delegation
    private val context: Context by lazy { this }

    // Adapter for displaying dynamic QR codes
    private lateinit var adapter: DynamicQrAdapter

    // List to hold dynamic QR code data
    private var dynamicQrList = mutableListOf<CodeHistory>()

    // ViewModels for managing UI-related data
    private val appViewModel: AppViewModel by viewModels()
    private val viewModel: DynamicQrViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using ViewBinding
        binding = ActivityDynamicQrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views and set up UI components
        initViews()

        // Set up the toolbar with necessary configurations
        setUpToolbar()

        // Display dynamic QR codes
        displayDynamicQrCodes()
    }

    // THIS FUNCTION WILL INITIALIZE ALL THE VIEWS AND REFERENCE OF OBJECTS
    private fun initViews() {

        // Initialize RecyclerView with a LinearLayoutManager for vertical scrolling.
        binding.dynamicQrRecyclerview.layoutManager = LinearLayoutManager(this)

        // Indicates that the size of the RecyclerView will not change, which helps with performance.
        binding.dynamicQrRecyclerview.setHasFixedSize(true)

        // Initialize the adapter with the dynamic QR list and set it to the RecyclerView.
        // Ensure dynamicQrList is cast to ArrayList if it's not already one.
        adapter = DynamicQrAdapter(dynamicQrList as ArrayList<CodeHistory>)
        binding.dynamicQrRecyclerview.adapter = adapter

        // Set the click listener for the adapter to handle item clicks.
        adapter.setOnClickListener(this)
    }

    // THIS FUNCTION WILL SET UP THE TOP ACTIONBAR
    private fun setUpToolbar() {
        // Set the toolbar as the action bar for this activity.
        setSupportActionBar(binding.toolbar)

        // Enable the home button (up navigation) in the toolbar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set the title of the action bar.
        supportActionBar?.title = "Dynamic QR Codes"

        // Set the title text color of the toolbar.
        binding.toolbar.setTitleTextColor(Color.BLACK) // Using Color.BLACK for clarity
    }


    // THIS FUNCTION WILL DISPLAY THE LIST OF CREATED DYNAMIC QR CODE
    private fun displayDynamicQrCodes() {
        // Observe changes to the list of dynamic QR codes
        appViewModel.dynamicQrCodes.observe(this, Observer { list ->
            // Check if the list is null or empty
            if (list == null || list.isEmpty()) {
                // Hide the RecyclerView and show the empty view
                binding.dynamicQrRecyclerview.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
            } else {
                // Update the list and notify the adapter if the list is not empty
                dynamicQrList.clear()
                dynamicQrList.addAll(list)

                // Hide the empty view and show the RecyclerView
                binding.emptyView.visibility = View.GONE
                binding.dynamicQrRecyclerview.visibility = View.VISIBLE

                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged()
            }
        })
    }


    // THIS FUNCTION WILL HANDLE THE ON BACK ARROW CLICK EVENT
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Check if the selected menu item is the home button (up navigation)
        return when (item.itemId) {
            android.R.id.home -> {
                // Handle the back navigation
                onBackPressed()
                true // Indicate that the event has been handled
            }
            else -> {
                // For other menu items, pass the event to the superclass
                super.onOptionsItemSelected(item)
            }
        }
    }

    /**
     * Handles the click event for editing an item at the specified position.
     * Updates the URL of the selected dynamic QR code.
     *
     * @param position The position of the item in the list that was clicked.
     */
    override fun onItemEditClick(position: Int) {
        // Check if the position is within the valid range of the dynamicQrList
        if (position in dynamicQrList.indices) {
            // Retrieve the dynamic QR object at the given position
            val dynamicQr = dynamicQrList[position]

            // Update the URL of the selected dynamic QR object
            updateDynamicQrUrl(dynamicQr)
        } else {
            // Optionally handle the case where the position is out of range
            Log.e("onItemEditClick", "Invalid position: $position")
        }
    }

    // THIS FUNCTION WILL HANDLE THE DYNAMIC QR CODE LIST FOR UPDATE
    override fun onItemClick(position: Int) {
        // Ensure the position is within the bounds of the list
        if (position in dynamicQrList.indices) {
            // Retrieve the item from the list based on the position
            val dynamicQr = dynamicQrList[position]

            // Create an intent to start CodeDetailActivity
            val intent = Intent(context, CodeDetailActivity::class.java).apply {
                // Add the dynamicQr item to the intent extras
                putExtra("HISTORY_ITEM", dynamicQr)
            }

            // Start the activity with the created intent
            startActivity(intent)
        } else {
            // Handle the case where the position is out of bounds, if needed
            Log.e("onItemClick", "Invalid position: $position")
        }
    }

    // THIS FUNCTION WILL POP UP WITH EXISTING URL FOR INPUT NEW UPDATED URL
    private fun updateDynamicQrUrl(selectedDynamicUrl: CodeHistory) {
        // Initialize ViewBinding for the dialog layout
        val dynamicBinding = UpdateDynamicUrlDialogLayoutBinding.inflate(LayoutInflater.from(context))

        // Set initial protocol and input box text based on the selected URL
        var selectedProtocol = ""
        val url = selectedDynamicUrl.data
        when {
            url.startsWith("http://") -> {
                selectedProtocol = "http://"
                dynamicBinding.dynamicUrlUpdateInputField.setText(url.removePrefix("http://"))
                dynamicBinding.httpProtocolRb.isChecked = true
            }
            url.startsWith("https://") -> {
                selectedProtocol = "https://"
                dynamicBinding.dynamicUrlUpdateInputField.setText(url.removePrefix("https://"))
                dynamicBinding.httpsProtocolRb.isChecked = true
            }
            else -> {
                dynamicBinding.dynamicUrlUpdateInputField.setText(url)
            }
        }

        // Create and configure the dialog
        val builder = MaterialAlertDialogBuilder(context)
            .setCancelable(false)
            .setView(dynamicBinding.root)

        val alert = builder.create()
        alert.show()

        // Set listeners for the dialog buttons
        dynamicBinding.dialogCancelBtn.setOnClickListener {
            alert.dismiss()
        }

        dynamicBinding.dialogUpdateBtn.setOnClickListener {
            // Get and validate the input data
            val value = dynamicBinding.dynamicUrlUpdateInputField.text.toString().trim()
            when {
                selectedProtocol.isEmpty() -> {
                    showAlert(context, "Please select the URL protocol!")
                }
                value.isEmpty() -> {
                    showAlert(context, "Please enter the required input data!")
                }
                value.contains("http://") || value.contains("https://") -> {
                    showAlert(context, "Please enter the URL without http:// or https://")
                }
                !value.contains(".com") -> {
                    showAlert(context, "Please enter a valid URL")
                }
                else -> {
                    // Prepare data for updating the dynamic QR code
                    val hashMap = hashMapOf<String, String>().apply {
                        put("login", selectedDynamicUrl.login)
                        put("qrId", selectedDynamicUrl.qrId)
                        put("userUrl", "$selectedProtocol$value")
                        put("userType", selectedDynamicUrl.userType)
                    }
                    alert.dismiss()
                    startLoading(context)
                    lifecycleScope.launch {
                        viewModel.createDynamicQrCode(hashMap)
                    }
                    viewModel.dynamicQrCodeResponse.observe(this, Observer { response ->
                        response?.let {
                            val genUrl = it.get("generatedUrl").asString
//                            url = url.replace(":8990", "")
                            appViewModel.update("$selectedProtocol$value", genUrl, selectedDynamicUrl.id)
                            showAlert(context, "Dynamic URL updated successfully!")
                        } ?: run {
                            showAlert(context, "Something went wrong, please try again!")
                        }
                    })
                }
            }
        }
    }

}