package com.expert.qrgenerator.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.FieldListValuesAdapter
import com.expert.qrgenerator.databinding.ActivityFieldListValuesBinding
import com.expert.qrgenerator.databinding.AddListValueLayoutBinding
import com.expert.qrgenerator.model.ListItem
import com.expert.qrgenerator.utils.TableGenerator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class FieldListValuesActivity : BaseActivity(), FieldListValuesAdapter.OnItemClickListener {

    // Lateinit properties to be initialized later
    private lateinit var binding: ActivityFieldListValuesBinding
    // Context of the activity, initialized using lazy delegation
    private val context: Context by lazy { this }

    // Mutable list to hold string values
    private val listValues = mutableListOf<String>()

    // Dependencies and adapters
    private lateinit var tableGenerator: TableGenerator
    private lateinit var adapter: FieldListValuesAdapter

    // Optional properties
    private var listItem: ListItem? = null

    // Table and flag variables
    private var tableName = ""
    private var flag = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout and get the binding instance
        binding = ActivityFieldListValuesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views such as setting up adapters or listeners
        initViews()

        // Set up the toolbar or any other action bar related elements
        setUpToolbar()

        // Fetch the list values from the data source
        getListValues()
    }

    private fun initViews() {

        // Check and retrieve extras from the intent
        intent?.let {
            listItem = it.getSerializableExtra("LIST_ITEM") as? ListItem ?: return
            tableName = it.getStringExtra("TABLE_NAME") ?: return
            flag = it.getStringExtra("FLAG") ?: return
        }

        // Initialize TableGenerator
        tableGenerator = TableGenerator(context)

        // Set up the RecyclerView
        with(binding.fieldListValuesRecyclerview) {
            layoutManager = LinearLayoutManager(context)
            hasFixedSize() // Optimize RecyclerView for performance
            adapter = FieldListValuesAdapter(context, listValues as ArrayList<String>).apply {
                setOnItemClickListener(this@FieldListValuesActivity)
            }
        }
    }


    /**
     * Sets up the toolbar with the appropriate configurations.
     */
    private fun setUpToolbar() {
        // Initialize the toolbar as the ActionBar for this activity
        setSupportActionBar(binding.toolbar)

        // Ensure the ActionBar is not null before setting its properties
        supportActionBar?.apply {
            // Set the title of the ActionBar
            title =
                listItem?.value ?: "Default Title" // Provide a default title if listItem is null

            // Enable the "Up" button in the ActionBar
            setDisplayHomeAsUpEnabled(true)
        }

        // Set the text color of the toolbar title
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    /**
     * Retrieves and updates the list of values for the specified item.
     * Clears the current list and adds the new values if available.
     * Notifies the adapter to refresh the data.
     */
    private fun getListValues() {
        // Retrieve a list of values based on the item ID
        val tempList = tableGenerator.getFieldListValues(listItem?.id ?: return)

        // Update list values only if tempList is not empty
        if (tempList.isNotEmpty()) {
            listValues.clear() // Clear the existing list
            listValues.addAll(tempList) // Add new values to the list
        }

        // Notify the adapter to update the UI with the latest data
        adapter.notifyDataSetChanged()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Check if the selected item ID is the 'home' button (usually the up button)
        return when (item.itemId) {
            android.R.id.home -> {
                // Handle the 'home' button click by calling onBackPressed() to navigate back
                onBackPressed()
                true
            }

            else -> {
                // For other menu items, use the default implementation
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onItemClick(position: Int) {

    }

    override fun onAddItemClick(position: Int) {
        addListItemDialog(listItem!!.id)
    }

    /**
     * Handles the action when an item click event finishes.
     * Launches `CreateTableActivity` if `tableName` and `flag` are not empty,
     * otherwise just finishes the current activity.
     */
    override fun onFinishItemClick() {
        // Check if tableName and flag are not empty
        if (tableName.isNotEmpty() && flag.isNotEmpty()) {
            // Create an intent to start CreateTableActivity
            val intent = Intent(context, CreateTableActivity::class.java).apply {
                // Set flags to clear the activity stack and start a new task
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                // Add tableName as an extra to the intent
                putExtra("TABLE_NAME", tableName)
            }

            // Start CreateTableActivity
            startActivity(intent)
        }

        // Finish the current activity
        finish()
    }


    private fun addListItemDialog(id: Int) {
        // Inflate the custom dialog layout using ViewBinding
        val dialogBinding = AddListValueLayoutBinding.inflate(LayoutInflater.from(context))

        // Set the heading text for the dialog
        dialogBinding.dialogHeading.text = getString(R.string.list_value_hint_text)

        // Create the dialog builder
        val builder = MaterialAlertDialogBuilder(context)
            .setView(dialogBinding.root)

        // Create and show the dialog
        val alert = builder.create()
        alert.show()

        // Set up the click listener for the button
        dialogBinding.addListValueBtn.setOnClickListener {
            val inputText = dialogBinding.addListValueInputField.text.toString().trim()

            if (inputText.isNotEmpty()) {
                // Insert the list value into the database
                tableGenerator.insertListValue(
                    id, inputText.lowercase(Locale.ENGLISH)
                )

                // Show success toast message
                Toast.makeText(
                    context,
                    getString(R.string.list_item_success_text),
                    Toast.LENGTH_SHORT
                ).show()

                // Dismiss the dialog and refresh the list values
                alert.dismiss()
                getListValues()
            } else {
                // Show error message if input is empty
                showAlert(context, getString(R.string.list_name_empty_error_text))
            }
        }
    }

}