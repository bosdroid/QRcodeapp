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
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.CategoriesAdapter
import com.expert.qrgenerator.adapters.QrCodeHistoryAdapter
import com.expert.qrgenerator.adapters.TagsAdapter
import com.expert.qrgenerator.databinding.ActivityCategoryQrCodesBinding
import com.expert.qrgenerator.databinding.DialogSelectFolderBinding
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.FolderWithCount
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class CategoryQrCodesActivity : BaseActivity() {

    private lateinit var binding: ActivityCategoryQrCodesBinding
    private val context: Context by lazy { this }
    private val appViewModel: AppViewModel by viewModels()
    private var qrCodeHistoryList = mutableListOf<CodeHistory>()
    private lateinit var adapter: QrCodeHistoryAdapter
    private var selectedFolder = "other"
    private var mainMenu: Menu?=null
    private val selectedItems = mutableListOf<Int>() // Track selected item positions
    private var categoriesList = mutableListOf<FolderWithCount>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryQrCodesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setUpToolbar()

    }

    override fun onResume() {
        super.onResume()
        appViewModel.loadUpdateData() // Add this method in ViewModel if not already present
        getDisplayCreateHistory()
        getCategories()
    }

    private fun initViews() {
        if (intent != null && intent.hasExtra("FOLDER")){
            selectedFolder = intent.getStringExtra("FOLDER") as String
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
//                mainMenu!!.findItem(R.id.add_folder).isVisible = false
                mainMenu!!.findItem(R.id.move_items).isVisible = true
//                Toast.makeText(this@BarcodeHistoryActivity, "Multi-select enabled", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun clearSelections() {
        selectedItems.clear()
        adapter.multiSelectMode = false
        adapter.notifyDataSetChanged()
        mainMenu!!.findItem(R.id.edit_mode).isVisible = true
        mainMenu!!.findItem(R.id.move_items).isVisible = false
    }

    private fun getDisplayCreateHistory() {
        // Show loading indicator
        startLoading(context)

        // Observe the ViewModel's LiveData for QR Code history
        appViewModel.allCreateQRCodeHistory(selectedFolder).observe(this@CategoryQrCodesActivity) { list ->
            // Dismiss loading indicator
            dismiss()

            // Update the UI based on the data received
            if (list.isNotEmpty()) {
                qrCodeHistoryList.clear()
                qrCodeHistoryList.addAll(list)
                adapter.notifyDataSetChanged()

                // Show RecyclerView and hide empty view
                binding.qrCodeHistoryRecyclerview.visibility = View.VISIBLE
                binding.emptyView.visibility = View.GONE
            } else {
                // Show empty view and hide RecyclerView
                binding.qrCodeHistoryRecyclerview.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
            }
        }
    }

    private fun getCategories() {

        // Observe the ViewModel's LiveData for QR Code history
        appViewModel.allFolders().observe(this@CategoryQrCodesActivity) { list ->

            // Update the UI based on the data received
            if (list.isNotEmpty()) {
                categoriesList.clear()
                categoriesList.addAll(list)
                categoriesList.removeAll { it.name.lowercase() == selectedFolder }
                categoriesList.add(FolderWithCount(0,"Reset",0,0))
            }
        }
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

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = selectedFolder
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        mainMenu = menu
        menuInflater.inflate(R.menu.main_menu,mainMenu)
        menu!!.findItem(R.id.create).isVisible = true
        menu.findItem(R.id.edit_mode).isVisible = true
        menu.findItem(R.id.history).isVisible = false
        menu.findItem(R.id.analytics).isVisible = true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            R.id.create -> {
                startActivity(Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                true
            }

            R.id.history -> {
                startActivity(Intent(context, BarcodeHistoryActivity::class.java))
                true
            }
            R.id.edit_mode->{
                mainMenu!!.findItem(R.id.edit_mode).isVisible = false
                mainMenu!!.findItem(R.id.move_items).isVisible = true
                adapter.multiSelectMode = true
                adapter.notifyDataSetChanged()
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

    private fun moveSelectedToFolder(selectedItems: List<Int>, folderName: String) {
        startLoading(context)
        selectedItems.forEach { position ->
            val item = qrCodeHistoryList[position]
            item.folder = if(folderName == "reset"){null}else{folderName}
            appViewModel.updateHistory(item)
        }
        clearSelections()
        dismiss()
        adapter.notifyDataSetChanged()
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

