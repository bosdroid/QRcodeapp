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
import com.expert.qrgenerator.adapters.FieldListAdapter
import com.expert.qrgenerator.databinding.ActivityFieldListsBinding
import com.expert.qrgenerator.databinding.AddListValueLayoutBinding
import com.expert.qrgenerator.model.ListItem
import com.expert.qrgenerator.utils.TableGenerator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class FieldListsActivity : BaseActivity() {

    // Initialize ViewBinding
    private lateinit var binding: ActivityFieldListsBinding

    // Context of the activity, initialized using lazy delegation
    private val context: Context by lazy { this }

    // List to hold items for display
    private val list = mutableListOf<ListItem>()

    // Adapter for handling list items
    private lateinit var adapter: FieldListAdapter

    // Table generator for creating tables
    private lateinit var tableGenerator: TableGenerator

    // Name of the table being displayed
    private var tableName = ""

    // Flag to determine specific conditions or states
    private var flag = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using ViewBinding
        val binding = ActivityFieldListsBinding.inflate(layoutInflater)

        // Set the content view to the root of the inflated binding
        setContentView(binding.root)

        // Initialize the views
        initViews()

        // Set up the toolbar
        setUpToolbar()
    }


    private fun initViews() {

        tableGenerator = TableGenerator(context)

        // Initialize RecyclerView with ViewBinding
        with(binding.fieldTypeListRecyclerview) {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = FieldListAdapter(list as ArrayList<ListItem>)
        }

        // Check and retrieve extras from the intent safely
        intent?.let {
            tableName = it.getStringExtra("TABLE_NAME") ?: ""
            flag = it.getStringExtra("FLAG") ?: ""
        }
    }


    // Function to fetch and display the list of items
    private fun getList() {
        // Fetch the list from the table generator
        val tempList = tableGenerator.getList()

        // Check if the fetched list is not empty
        if (tempList.isNotEmpty()) {
            // Clear the existing list and add the new items
            list.clear()
            list.addAll(tempList)
        }

        // Notify the adapter of data changes
        adapter.notifyDataSetChanged()

        // Set item click listener for the adapter
        adapter.setOnItemClickListener(object : FieldListAdapter.OnItemClickListener {

            // Handle item click events
            override fun onItemClick(position: Int) {
                // Get the item at the clicked position
                val selectedItem = list[position]

                // Create an intent to start FieldListValuesActivity
                val intent = Intent(context, FieldListValuesActivity::class.java)

                // Pass optional extras if tableName and flag are not empty
                if (tableName.isNotEmpty() && flag.isNotEmpty()) {
                    intent.putExtra("TABLE_NAME", tableName)
                    intent.putExtra("FLAG", flag)
                }

                // Pass the selected item to the new activity
                intent.putExtra("LIST_ITEM", selectedItem)
                startActivity(intent)
            }

            // Handle add item click events
            override fun onAddItemClick(position: Int) {
                // Show dialog to add a new list item
                addListItemDialog()
            }
        })
    }


    // Function to show a dialog for adding a list item
    private fun addListItemDialog() {
        // Inflate the dialog layout using ViewBinding
        val dialogBinding = AddListValueLayoutBinding.inflate(LayoutInflater.from(context))

        // Create and configure the alert dialog
        val alert = MaterialAlertDialogBuilder(context)
            .setView(dialogBinding.root)
            .create()

        // Show the dialog
        alert.show()

        // Set click listener for the create button
        dialogBinding.addListValueBtn.setOnClickListener {
            val listName = dialogBinding.addListValueInputField.text.toString().trim().toLowerCase(Locale.ENGLISH)

            // Check if the input field is not empty
            if (listName.isNotEmpty()) {
                // Insert the new list item into the database and show a success message
                val id = tableGenerator.insertList(listName)
                Toast.makeText(context, getString(R.string.list_create_success_text), Toast.LENGTH_SHORT).show()
                alert.dismiss()

                // Prepare the intent to start the FieldListValuesActivity
                val intent = Intent(context, FieldListValuesActivity::class.java).apply {
                    if (tableName.isNotEmpty() && flag.isNotEmpty()) {
                        putExtra("TABLE_NAME", tableName)
                        putExtra("FLAG", flag)
                    }
                    putExtra("LIST_ITEM", ListItem(id.toInt(), listName))
                }
                startActivity(intent)
            } else {
                // Show an error message if the input field is empty
                showAlert(context, getString(R.string.list_name_empty_error_text))
            }
        }
    }


    private fun setUpToolbar() {
        // Set the toolbar as the support action bar
        setSupportActionBar(binding.toolbar)

        // Get the support action bar and configure its properties
        supportActionBar?.apply {
            title = getString(R.string.field_type_list) // Set the title of the action bar
            setDisplayHomeAsUpEnabled(true) // Enable the back button (up navigation)
        }

        // Set the color of the toolbar title text
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.black))
    }

    override fun onResume() {
        super.onResume()
        getList()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }
}