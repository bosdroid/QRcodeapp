package com.expert.qrgenerator.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.CategoriesAdapter
import com.expert.qrgenerator.adapters.QrCodeHistoryAdapter
import com.expert.qrgenerator.adapters.TagsAdapter
import com.expert.qrgenerator.databinding.ActivityBarcodeHistoryBinding
import com.expert.qrgenerator.databinding.DialogFolderLayoutBinding
import com.expert.qrgenerator.databinding.DialogSelectFolderBinding
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.Folder
import com.expert.qrgenerator.model.FolderWithCount
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.utils.Constants
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BarcodeHistoryActivity : BaseActivity() {

    // ViewBinding for the activity layout
    private lateinit var binding: ActivityBarcodeHistoryBinding

    // List to hold QR Code history items
    private var qrCodeHistoryList = mutableListOf<CodeHistory>()
    private val selectedItems = mutableListOf<Int>() // Track selected item positions
    private var categoriesList = mutableListOf<FolderWithCount>()

    // Adapter for displaying QR Code history
    private lateinit var adapter: QrCodeHistoryAdapter

    private lateinit var categoriesAdapter:CategoriesAdapter

    // ViewModel for handling data operations
    private val appViewModel: AppViewModel by viewModels()

    // Context of the activity, initialized using lazy delegation
    private val context: Context by lazy { this }

    private var selectedTag = "other"

    private var mainMenu:Menu?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        logCustomEvent(eventName = "screen_history_opened")

        binding = ActivityBarcodeHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views and setup components
        initViews()
        setUpToolbar()

    }

    override fun onResume() {
        super.onResume()
        logEvent()
        appViewModel.loadUpdateData() // Add this method in ViewModel if not already present

        getCategories()
    }

    private fun logEvent() {
        val mainAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        // Log the custom event
        mainAnalytics.logEvent("screen_history_opened", bundle)
    }

    /**
     * Initialize any views or variables.
     */
    private fun initViews() {
        if (intent != null && intent.hasExtra("TAG")){
            selectedTag = intent.getStringExtra("TAG") as String
        }
        // This is where you can initialize any other views or components if needed
        // Set up the RecyclerView with LinearLayoutManager and adapter
        binding.qrCodeHistoryRecyclerview.layoutManager = LinearLayoutManager(context)
        binding.qrCodeHistoryRecyclerview.setHasFixedSize(true) // Improve performance with fixed-size
        adapter = QrCodeHistoryAdapter(context, qrCodeHistoryList as ArrayList<CodeHistory>)
        binding.qrCodeHistoryRecyclerview.adapter = adapter

        // Set up the click listener for RecyclerView items
        adapter.setOnClickListener(object : QrCodeHistoryAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                // Handle item click event
                val historyItem = qrCodeHistoryList[position]
                if(historyItem.type == "vcard"){
//                    val intent = Intent(context, UpdateVCardActivity::class.java)
//                    startActivity(intent)
                    Constants.isOpenVcardScreen = true
                    finish()
                }
                else{
                    val intent = Intent(context, CodeDetailActivity::class.java)
                    intent.putExtra("HISTORY_ITEM", historyItem)
                    startActivity(intent)
                }
            }

            override fun onItemClickMore(item:CodeHistory,position: Int) {
                showAddTagsDialog(context) { tags ->
                    if (tags.isNotEmpty()){
                        item.tags = tags
                    }
                   appViewModel.updateHistory(item)
                   adapter.notifyItemChanged(position)
                }
            }

            override fun onSelectionChanged(position: Int, isSelected: Boolean) {
                if (isSelected) {
                    selectedItems.add(position)
                } else {
                    selectedItems.remove(position)
                }
            }

            override fun onMultiSelectModeEnabled() {
                mainMenu!!.findItem(R.id.add_folder).isVisible = false
                mainMenu!!.findItem(R.id.move_items).isVisible = true
//                Toast.makeText(this@BarcodeHistoryActivity, "Multi-select enabled", Toast.LENGTH_SHORT).show()
            }
        })

        binding.categoriesRecyclerview.layoutManager = GridLayoutManager(context,2)
        binding.categoriesRecyclerview.setHasFixedSize(true) // Improve performance with fixed-size
        categoriesAdapter = CategoriesAdapter(context, categoriesList as ArrayList<FolderWithCount>)
        binding.categoriesRecyclerview.adapter = categoriesAdapter

        categoriesAdapter.setOnItemClickListener(object : CategoriesAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
               val folder = categoriesList[position]
//                appViewModel.deleteFolder(folder)
               if (folder.codeHistoryCount > 0){
                   startActivity(Intent(this@BarcodeHistoryActivity,CategoryQrCodesActivity::class.java).apply {
                       putExtra("FOLDER",folder.name.lowercase())
                   })
               }
            }

        })
    }

    private fun clearSelections() {
        selectedItems.clear()
        adapter.multiSelectMode = false
        adapter.notifyDataSetChanged()
        mainMenu!!.findItem(R.id.add_folder).isVisible = true
        mainMenu!!.findItem(R.id.edit_mode).isVisible = true
        mainMenu!!.findItem(R.id.move_items).isVisible = false
    }

    fun showAddTagsDialog(context: Context, onTagsSaved: (String) -> Unit) {
        val dialogView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 20) // Adjust padding as needed
        }

        val inputField = EditText(context).apply {
            hint = "Enter tags separated by commas"
        }
        dialogView.addView(inputField)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Add Tags")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val inputText = inputField.text.toString().trim()
                if (!TextUtils.isEmpty(inputText)) {
                    // Split the input by commas, trim whitespace, and remove duplicates
                    val tagsList = inputText.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .distinct()

                    // Convert the list back to a comma-separated string
                    val commaSeparatedTags = tagsList.joinToString(",")
                    onTagsSaved(commaSeparatedTags)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }


    /**
     * Set up the toolbar with title and home button.
     */
    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.qr_code_history)
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    /**
     * Fetches and displays QR Code history data.
     */
    private fun getDisplayCreateHistory() {
        // Show loading indicator
        startLoading(context)

        // Observe the ViewModel's LiveData for QR Code history
        appViewModel.allCreateQRCodeHistory(null).observe(this@BarcodeHistoryActivity) { list ->
            // Dismiss loading indicator
            dismiss()

            // Update the UI based on the data received
            if (list.isNotEmpty()) {
                qrCodeHistoryList.clear()
                qrCodeHistoryList.addAll(list)
                adapter.notifyDataSetChanged()
                if(mainMenu != null){
                    mainMenu!!.findItem(R.id.edit_mode).isVisible = true
                }
                // Show RecyclerView and hide empty view
                binding.qrCodeHistoryRecyclerview.visibility = View.VISIBLE
                binding.emptyView.visibility = View.GONE
            } else {
                if (mainMenu != null){
                    mainMenu!!.findItem(R.id.edit_mode).isVisible = false
                }
                // Show empty view and hide RecyclerView
                binding.qrCodeHistoryRecyclerview.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
            }
        }
    }

    private fun getCategories() {

        // Observe the ViewModel's LiveData for QR Code history
        appViewModel.allFolders().observe(this@BarcodeHistoryActivity) { list ->

            // Update the UI based on the data received
            if (list.isNotEmpty()) {
                categoriesList.clear()
                categoriesList.addAll(list)
                categoriesAdapter.notifyDataSetChanged()

                binding.categoriesRecyclerview.visibility = View.VISIBLE
                binding.categoriesEmptyView.visibility = View.GONE
            } else {
                binding.categoriesRecyclerview.visibility = View.GONE
                binding.categoriesEmptyView.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        mainMenu = menu
        menuInflater.inflate(R.menu.main_menu,mainMenu)
        menu!!.findItem(R.id.create).isVisible = true
        menu.findItem(R.id.add_folder).isVisible = true
//        menu.findItem(R.id.edit_mode).isVisible = true
        menu.findItem(R.id.history).isVisible = false
        menu.findItem(R.id.analytics).isVisible = true
        getDisplayCreateHistory()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.analytics->{
                startActivity(Intent(context, AnalyticsActivity::class.java))
                true
            }
            R.id.create->{
                startActivity(Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                true
            }
            R.id.history->{
                startActivity(Intent(context, BarcodeHistoryActivity::class.java))
                true
            }
            R.id.edit_mode->{
                mainMenu!!.findItem(R.id.edit_mode).isVisible = false
                mainMenu!!.findItem(R.id.add_folder).isVisible = false
                mainMenu!!.findItem(R.id.move_items).isVisible = true
                adapter.multiSelectMode = true
                adapter.notifyDataSetChanged()
                true
            }
            R.id.add_folder->{
                createFolder()
                true
            }
            R.id.move_items->{
                if (selectedItems.isNotEmpty()){
                    showSelectFolderDialog(categoriesList){selectedFolder ->
//                        Toast.makeText(this, "Selected Folder: ${selectedFolder.name}", Toast.LENGTH_SHORT).show()
                       moveSelectedToFolder(selectedItems,selectedFolder.name.lowercase())
                    }
                }
                true
            }
            else -> {
                // Pass the event to the superclass to handle other menu items
                super.onOptionsItemSelected(item)
            }
        }
    }
    private fun moveSelectedToFolder(selectedItems: List<Int>, folderName: String) {
        startLoading(context)
        selectedItems.forEach { position ->
            val item = qrCodeHistoryList[position]
            item.folder = folderName
            appViewModel.updateHistory(item)
        }
        clearSelections()
        dismiss()
        adapter.notifyDataSetChanged()
    }

    private fun showSelectFolderDialog(folders: List<FolderWithCount>, onFolderSelected: (FolderWithCount) -> Unit) {
        // Create a ViewBinding instance for the dialog layout
        val binding = DialogSelectFolderBinding.inflate(LayoutInflater.from(this))

        // Create an ArrayAdapter for the Spinner
        val folderNames = folders.map { it.name } // Extract folder names
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, folderNames
        )

        // Set the spinner to use the adapter
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFolders.adapter = adapter

        // Create the AlertDialog
        val dialog = AlertDialog.Builder(this)
            .setTitle("Select Category")
            .setView(binding.root)
            .setPositiveButton("OK") { _, _ ->
                val selectedPosition = binding.spinnerFolders.selectedItemPosition
                if (selectedPosition != -1) {
                    val selectedFolder = folders[selectedPosition]
                    onFolderSelected(selectedFolder) // Pass the selected folder to the callback
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        // Show the dialog
        dialog.show()
    }


    private fun createFolder(folder: Folder? = null) {
        val dialogBinding = DialogFolderLayoutBinding.inflate(layoutInflater)
        val editTextFolderName = dialogBinding.folderInputField

        if (folder != null) {
            // If folder is passed, it's an update action, pre-fill the field
            editTextFolderName.setText(folder.name)
        }

        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle(if (folder == null) "Add Folder" else "Update Folder")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { dialog, _ ->
                val folderName = editTextFolderName.text.toString().trim()
                if (folderName.isEmpty()) {
                    Toast.makeText(this, "Folder name cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (folder == null) {
                    // Adding a new folder
                    val newFolder = Folder(name = folderName)
                    appViewModel.insertFolder(newFolder)
                    logCustomEvent(context,"barcode_history_screen","event","new folder created")
                    Toast.makeText(this, "Folder added", Toast.LENGTH_SHORT).show()
                } else {
                    // Updating an existing folder
                    val updatedFolder = folder.copy(name = folderName)
                    appViewModel.updateFolder(updatedFolder)
                    Toast.makeText(this, "Folder updated", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    override fun onBackPressed() {
        if (adapter.multiSelectMode){
            clearSelections()
        }
        else{
            super.onBackPressed()
        }

    }

}
