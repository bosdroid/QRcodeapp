package com.expert.qrgenerator.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.TablesAdapter
import com.expert.qrgenerator.databinding.ActivityTablesBinding
import com.expert.qrgenerator.databinding.AddTableLayoutBinding
import com.expert.qrgenerator.utils.AppSettings
import com.expert.qrgenerator.utils.TableGenerator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TablesActivity : BaseActivity(),TablesAdapter.OnItemClickListener {

    // Define the binding object to access views in the layout file
    private lateinit var binding: ActivityTablesBinding

    // Context of the activity, initialized using lazy delegation
    private val context: Context by lazy { this }

    // Define the table generator to create tables
    private lateinit var tableGenerator: TableGenerator

    // List to hold table names or identifiers
    private var tableList = mutableListOf<String>()

    // Adapter for displaying tables in a RecyclerView or similar component
    private lateinit var adapter: TablesAdapter

    // Object to store and retrieve application settings
    private lateinit var appSettings: AppSettings


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout and initialize ViewBinding
        binding = ActivityTablesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views and set up toolbar
        initViews()
        setUpToolbar()
    }



    private fun initViews() {

        appSettings = AppSettings(context)
        tableGenerator = TableGenerator(context)

        // Set up the RecyclerView for displaying tables

            // Use LinearLayoutManager for vertical list layout
            binding.tablesRecyclerView.layoutManager = LinearLayoutManager(context)

            // Optimize RecyclerView performance by setting fixed size
            binding.tablesRecyclerView.setHasFixedSize(true)

            // Initialize and set the adapter with the table list
            adapter = TablesAdapter(context, tableList as ArrayList<String>)
            binding.tablesRecyclerView.adapter = adapter

    }


    private fun setUpToolbar() {
        // Set the toolbar as the support action bar
        setSupportActionBar(binding.toolbar)

        // Get the support action bar
        val actionBar = supportActionBar
        actionBar?.apply {
            // Set the title of the toolbar
            title = getString(R.string.tables)

            // Enable the home button for the toolbar
            setDisplayHomeAsUpEnabled(true)
        }

        // Set the title text color of the toolbar
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.black))
    }


    private fun displayTableList() {
        // Retrieve the list of all database tables
        val list = tableGenerator.getAllDatabaseTables()

        // Check if the list is not empty before clearing the existing table list
        if (list.isNotEmpty()) {
            tableList.clear() // Clear the existing list to avoid stale data
        }

        // Add the new table data to the list
        tableList.addAll(list)

        // Notify the adapter that the data has changed, so the UI can be updated
        adapter.notifyDataSetChanged()

        // Set the item click listener for the adapter
        adapter.setOnItemClickListener(this)
    }

    // THIS FUNCTION WILL HANDLE THE ON BACK ARROW CLICK EVENT
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            android.R.id.home -> {
                // Handle the 'home' button press
                onBackPressed() // Navigate back
                true
            }
            else -> super.onOptionsItemSelected(item) // Handle other menu items
        }
    }


    override fun onItemClick(position: Int) {
        // Retrieve the table name from the list based on the clicked position
        val tableName = tableList[position]

        // Create an intent to start the CreateTableActivity
        val intent = Intent(context, CreateTableActivity::class.java).apply {
            // Pass the table name as an extra to the intent
            putExtra("TABLE_NAME", tableName)
        }

        // Start the CreateTableActivity with the provided intent
        startActivity(intent)
    }

    override fun onAddItemClick(position: Int) {
            addTableDialog()
    }

    override fun onResume() {
        super.onResume()
        displayTableList()
    }

    private fun addTableDialog() {
        // Inflate the layout using ViewBinding
        val dialogBinding = AddTableLayoutBinding.inflate(LayoutInflater.from(context))

        // Get references to views using the ViewBinding object
        val textInputBox = dialogBinding.addTableTextInputField
        val tableCreateBtn = dialogBinding.addTableBtn

        // Set up a TextWatcher to filter out special characters
        textInputBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed before text change
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    // Remove any non-alphabetic characters and update the text
                    var newStr = it.toString().replace("[^a-zA-Z ]*".toRegex(), "")
                    if (it.toString() != newStr) {
                        // Show a toast and set the cleaned text
                        Toast.makeText(context, getString(R.string.characters_special_error_text), Toast.LENGTH_SHORT).show()
                        textInputBox.setText(newStr)
                        textInputBox.setSelection(newStr.length)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // No action needed after text change
            }
        })

        // Create and show the dialog using ViewBinding
        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(dialogBinding.root)
        val alert = builder.create()
        alert.show()

        // Set up the button click listener
        tableCreateBtn.setOnClickListener {
            val tableName = textInputBox.text.toString().trim()

            if (tableName.isNotEmpty()) {
                // Generate table and show success message
                tableGenerator.generateTable(tableName)
                Toast.makeText(context, getString(R.string.table_create_success_text), Toast.LENGTH_SHORT).show()
                alert.dismiss()

                // Start CreateTableActivity with the table name
                val intent = Intent(context, CreateTableActivity::class.java).apply {
                    putExtra("TABLE_NAME", tableName)
                }
                startActivity(intent)
            } else {
                // Show error message if table name is empty
                showAlert(context, getString(R.string.table_name_empty_error_text))
            }
        }
    }

}