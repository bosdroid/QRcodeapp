package com.expert.qrgenerator.ui.activities

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.TableDetailAdapter
import com.expert.qrgenerator.databinding.ActivityTableViewBinding
import com.expert.qrgenerator.databinding.HeaderTableRowCellBinding
import com.expert.qrgenerator.databinding.QuickEditSingleLayoutBinding
import com.expert.qrgenerator.databinding.TableMoreOptionLayoutBinding
import com.expert.qrgenerator.databinding.TableRowCellBinding
import com.expert.qrgenerator.databinding.UpdateQuickEditTableLayoutBinding
import com.expert.qrgenerator.model.TableObject
import com.expert.qrgenerator.utils.TableGenerator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.Locale

@AndroidEntryPoint
class TableViewActivity : BaseActivity(), TableDetailAdapter.OnItemClickListener,
    View.OnClickListener{

    // Lateinit properties to be initialized later
    private lateinit var binding: ActivityTableViewBinding
    // Context of the activity, initialized using lazy delegation
    private val context: Context by lazy { this }

    // Instance for generating tables
    private lateinit var tableGenerator: TableGenerator

    // Layouts for displaying the table
//    private lateinit var tableMainLayout: TableLayout

    // Variables for table configuration
    private var tableName: String = ""
    private var dataList = mutableListOf<TableObject>() // List to hold table data
    private var sortingImages = mutableListOf<AppCompatImageView>() // List to hold sorting icons

    // Variables to manage table column sorting
    private var currentColumn: String = "" // Name of the currently sorted column
    private var currentOrder: String = "" // Sorting order (ascending/descending)
    private var quickEditFlag: Boolean = false // Flag to enable or disable quick edit mode

    // LayoutParams for table rows with weight of 2
    val layoutParams = TableRow.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        2f
    )

    // List to manage barcode editing with associated views and types
    private var barcodeEditList =
        mutableListOf<Triple<TextInputEditText, AppCompatImageView, String>>()

    // Counter for managing dynamic elements
    private var counter: Int = 0

    // List to hold details in key-value pairs
    private var detailList = mutableListOf<Pair<String, String>>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using ViewBinding
        binding = ActivityTableViewBinding.inflate(layoutInflater)

        // Set the content view to the root of the inflated layout
        setContentView(binding.root)

        // Initialize views and set up toolbar
        initViews()
        setUpToolbar()
    }


    override fun onResume() {
        super.onResume()
        getTableData(tableName, "", "")
    }

    private fun initViews() {

        tableGenerator = TableGenerator(context)

        // Retrieve the table name from the intent extras
        tableName = intent?.getStringExtra("TABLE_NAME") ?: return

        // Get the columns for the table
        val columns = tableGenerator.getTableColumns(tableName) ?: return

        // Create a new TableRow for table headers
        val tableHeaders = TableRow(context)

        // Inflate the header layout using ViewBinding
        for (i in 0 until columns.size + 1) {
            val headerBinding = HeaderTableRowCellBinding.inflate(LayoutInflater.from(context), tableHeaders, false)

            if (i == 0) {
                // First header cell (empty cell or title)
                headerBinding.sortImage.visibility = View.INVISIBLE
            } else {
                // Subsequent header cells for columns
                headerBinding.sortImage.apply {
                    visibility = View.VISIBLE
                    id = i
                    sortingImages.add(this)
                }
                headerBinding.headerCellName.text = columns[i - 1].toUpperCase(Locale.ENGLISH)
                headerBinding.root.id = i - 1
                headerBinding.root.tag = columns[i - 1].toLowerCase(Locale.ENGLISH)
                headerBinding.root.setOnClickListener(this)
            }

            // Set background color for header layout
            headerBinding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.purple_dark))

            tableHeaders.addView(headerBinding.root)
        }

        // Add the table headers to the main layout
        binding.tableMain.addView(tableHeaders)

        // Set up the export CSV button click listener
        binding.exportCsv.setOnClickListener {
            exportCsv(tableName)
            //importCsv(tableName) // Uncomment if import functionality is needed
        }

        // Set up the quick edit table checkbox listener
        binding.quickEditTableViewCheckbox.setOnCheckedChangeListener { _, isChecked ->
            quickEditFlag = isChecked
        }
    }


    /**
     * Sets up the toolbar for the activity.
     */
    private fun setUpToolbar() {
        // Ensure that the toolbar is not null before proceeding
        val toolbar = binding.toolbar

        // Set the toolbar as the ActionBar for this activity
        setSupportActionBar(toolbar)

        // Get the ActionBar instance
        val actionBar = supportActionBar

        // Check if the ActionBar is available
        if (actionBar != null) {
            // Set the title for the ActionBar
            actionBar.title = tableName

            // Enable the home button (back button) in the ActionBar
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        // Set the color of the toolbar title text
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.black))
    }


    // THIS FUNCTION WILL HANDLE THE ON BACK ARROW CLICK EVENT
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Check if the selected item is the home button
        return when (item.itemId) {
            android.R.id.home -> {
                // Call onBackPressed() to handle the back navigation
                onBackPressed()
                true
            }
            else -> {
                // Handle other item selections by calling the superclass method
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun getTableData(tName: String, column: String, order: String) {
        // Fetch table data based on the given table name, column, and order
        val tempList = tableGenerator.getTableData(tName, column, order)

        // Clear existing data and remove previous views
        if (tempList.isNotEmpty()) {
            dataList.clear()
        }
        if (binding.tableMain.childCount > 1) {
            binding.tableMain.removeViews(1, binding.tableMain.childCount - 1)
        }

        // Add new data to the list and set the weight sum for the layout
        dataList.addAll(tempList)
        binding.tableMain.weightSum = dataList.size * 2F

        // Proceed if there is data to display
        if (dataList.isNotEmpty()) {
            startLoading(context)
            for (j in dataList.indices) {
                // Inflate row and layout views using ViewBinding
                val moreOptionsBinding = TableMoreOptionLayoutBinding.inflate(LayoutInflater.from(context))
                val cellBinding = TableRowCellBinding.inflate(LayoutInflater.from(context))

                // Configure the table row
                val tableRow = TableRow(context).apply {
                    id = j
                    tag = "row"
                    setOnClickListener(this@TableViewActivity)
                    setBackgroundColor(if (j % 2 == 0) Color.parseColor("#EAEAF6") else Color.parseColor("#f2f2f2"))
                }

                // Configure the "More" button
                moreOptionsBinding.cellMoreImage.apply {
                    id = j
                    tag = "more"
                    setOnClickListener(this@TableViewActivity)
                }
                tableRow.addView(moreOptionsBinding.root)

                // Populate data for each row
                with(dataList[j]) {
                    cellBinding.cellValue.text = "$id"
                    tableRow.addView(cellBinding.root)

                    // Populate code data
                    val codeDataBinding = TableRowCellBinding.inflate(LayoutInflater.from(context))
                    codeDataBinding.cellValue.text = code_data
                    tableRow.addView(codeDataBinding.root)

                    // Populate date
                    val dateBinding = TableRowCellBinding.inflate(LayoutInflater.from(context))
                    dateBinding.cellValue.text = date
                    tableRow.addView(dateBinding.root)

                    // Populate image (truncated if necessary)
                    val imageBinding = TableRowCellBinding.inflate(LayoutInflater.from(context))
                    imageBinding.cellValue.text = if (image.length >= 20) image.substring(0, 20) else image
                    tableRow.addView(imageBinding.root)

                    // Populate quantity
                    val quantityBinding = TableRowCellBinding.inflate(LayoutInflater.from(context))
                    quantityBinding.cellValue.text = "$quantity"
                    tableRow.addView(quantityBinding.root)

                    // Add dynamic columns if present
                    dynamicColumns.forEach { item ->
                        val dynamicColumnBinding = TableRowCellBinding.inflate(LayoutInflater.from(context))
                        dynamicColumnBinding.cellValue.text = item.second
                        tableRow.addView(dynamicColumnBinding.root)
                    }
                }

                // Add the configured row to the layout
                binding.tableMain.addView(tableRow)
            }
            dismiss()
        }
    }

    // Function that handles item click events
    override fun onItemClick(position: Int) {
        // Retrieve the object from the data list based on the clicked position
        val tableObject = dataList[position]

        // Display the object's string representation in an alert dialog
        // Assume `showAlert` is a function that shows a dialog with a message
        showAlert(binding.root.context, tableObject.toString())
    }


    override fun onClick(v: View?) {
        // Safe call to avoid null pointer exception
        v?.let { view ->
            // Handle the case when the view tag is "row"
            when (view.tag) {
                "row" -> handleRowClick(view.id)
                "qe" -> handleQuickEdit(view.id)
                "more" -> showPopupMenu(view, view.id)
                else -> handleSorting(view)
            }
        }
    }

    // Function to handle row clicks
    private fun handleRowClick(position: Int) {
        val item = dataList[position]
        if (quickEditFlag) {
            openQuickEditDialog(item)
        } else {
            val intent = Intent(context, CodeDetailActivity::class.java).apply {
                putExtra("TABLE_NAME", tableName)
                putExtra("TABLE_ITEM", item)
            }
            startActivity(intent)
        }
    }

    // Function to handle quick edit
    private fun handleQuickEdit(position: Int) {
        val triple = barcodeEditList[position]
        triple.first.setText("")
    }

    // Function to display a popup menu
    private fun showPopupMenu(view: View, position: Int) {
        val itemDetail = dataList[position]
        val popup = PopupMenu(context, view).apply {
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.pp_remove -> {
                        removeItem(itemDetail.id, position)
                        true
                    }
                    R.id.pp_copy -> {
                        copyToClipboard(itemDetail.toString())
                        true
                    }
                    else -> false
                }
            }
            inflate(R.menu.table_pop_up_menu)
            show()
        }
    }

    // Function to handle sorting logic
    private fun handleSorting(view: View) {
        if (dataList.isNotEmpty()) {
            val tag = view.tag.toString().toLowerCase(Locale.ENGLISH)
            currentOrder = if (currentColumn == tag && currentOrder == "DESC") "ASC" else "DESC"
            currentColumn = tag

            val image = sortingImages[view.id]
            updateSortingImage(image, currentOrder)
            getTableData(tableName, currentColumn, currentOrder)
        }
    }


    private fun removeItem(id: Int, position: Int) {
        // Create and show an alert dialog to confirm item removal
        MaterialAlertDialogBuilder(context).apply {
            setMessage(getString(R.string.remove_item_alert_message_text))

            // Handle the "Cancel" button click
            setNegativeButton(getString(R.string.cancel_text)) { dialog, _ ->
                dialog.dismiss() // Dismiss the dialog if the user cancels
            }

            // Handle the "Remove" button click
            setPositiveButton(getString(R.string.remove_text)) { dialog, _ ->
                dialog.dismiss() // Dismiss the dialog once the user confirms removal
                val isSuccess = tableGenerator.removeItem(tableName, id)

                // Check if the item removal was successful
                if (isSuccess) {
                    dataList.removeAt(position) // Remove item from the list
                    Toast.makeText(
                        context,
                        getString(R.string.remove_item_success_text),
                        Toast.LENGTH_SHORT
                    ).show()

                    // Refresh the table data after removing the item
                    getTableData(tableName, "", "")
                }
            }
        }.create().show() // Create and show the dialog
    }


    private fun openQuickEditDialog(item: TableObject) {
        // Inflate the dialog layout using ViewBinding
        val dialogBinding = UpdateQuickEditTableLayoutBinding.inflate(LayoutInflater.from(context))
        val quickEditWrapperLayout = dialogBinding.quickEditParentLayout

        // Create a reusable function to set up each field
        fun setupField(
            value: String,
            fieldType: String,
            quickEditWrapperLayout: LinearLayout
        ): TextInputEditText {
            val layoutBinding = QuickEditSingleLayoutBinding.inflate(LayoutInflater.from(context), quickEditWrapperLayout, false)
            val textInputField = layoutBinding.quickEditBarcodeDetailTextInputField
            val clearBrushView = layoutBinding.quickEditBarcodeDetailCleaningTextView

            counter += 1
            clearBrushView.id = counter
            clearBrushView.tag = "qe"

            barcodeEditList.add(Triple(textInputField, clearBrushView, fieldType))
            clearBrushView.setOnClickListener(this)
            textInputField.setText(value)
            textInputField.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    dialogBinding.quickEditDialogUpdateBtn.isEnabled = true
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            quickEditWrapperLayout.addView(layoutBinding.root)
            return textInputField
        }

        // Set up the initial fields
        setupField(item.code_data, "code_data", quickEditWrapperLayout)
        setupField(item.date, "date", quickEditWrapperLayout)
        setupField(item.image, "image", quickEditWrapperLayout)

        // Set up dynamic columns
        item.dynamicColumns.forEach { (fieldType, value) ->
            setupField(value, fieldType, quickEditWrapperLayout)
        }
        counter = 0

        // Build and show the dialog
        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(binding.root)
        builder.setCancelable(false)
        val alert = builder.create()
        alert.show()

        // Handle cancel button click
        dialogBinding.quickEditDialogCancelBtn.setOnClickListener { alert.dismiss() }

        // Handle update button click
        dialogBinding.quickEditDialogUpdateBtn.setOnClickListener {
            startLoading(context)
            var isValid = true

            // Validate all fields
            for ((textInputField, _, fieldType) in barcodeEditList) {
                val value = textInputField.text.toString().trim()
                if (value.isEmpty()) {
                    isValid = false
                    detailList.clear()
                    break
                } else {
                    detailList.add(Pair(fieldType, value))
                }
            }

            // If all fields are valid, update the data
            if (isValid) {
                alert.dismiss()
                if (detailList.isNotEmpty()) {
                    val isSuccess = tableGenerator.updateData(tableName, detailList, item.id)
                    if (isSuccess) {
                        dismiss()
                        getTableData(tableName, "", "")
                    } else {
                        dismiss()
                        showAlert(context, getString(R.string.database_update_failed_error))
                    }
                }
            } else {
                dismiss()
                showAlert(context, getString(R.string.empty_text_error))
            }
        }
    }

    private fun updateSortingImage(imageView: AppCompatImageView, order: String) {
        // Loop through each sorting image in the list
        sortingImages.forEach { sImage ->
            // Check if the current image view matches the passed image view and order
            if (imageView.id == sImage.id && currentOrder.equals(order, ignoreCase = true)) {
                // Set the color filter to white and update the image resource based on the order
                sImage.setColorFilter(Color.WHITE)
                val imageResource = if (order.equals("asc", ignoreCase = true)) {
                    R.drawable.ic_sort_asc
                } else {
                    R.drawable.ic_sort_desc
                }
                sImage.setImageResource(imageResource)
            } else {
                // Set the color filter to gray for non-matching images
                sImage.setColorFilter(Color.parseColor("#808080"))
            }
        }
    }

    // Function to copy the provided content to the clipboard
    private fun copyToClipboard(content: String) {
        // Retrieve the clipboard service from the system
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Create a new ClipData object with the label "Barcode Detail" and the content to copy
        val clip = ClipData.newPlainText("Barcode Detail", content)

        // Set the ClipData as the primary clip in the clipboard
        clipboard.setPrimaryClip(clip)

        // Display a toast message to inform the user that the content has been copied
        Toast.makeText(this, "Copied", Toast.LENGTH_LONG).show()
    }

    private fun exportCsv(tableName: String) {
        // Check if dataList is not empty before proceeding with CSV export
        if (dataList.isNotEmpty()) {
            startLoading(context)

            // Retrieve columns for the specified table
            val columns = tableGenerator.getTableColumns(tableName)
            val builder = StringBuilder()

            // Append column headers to the CSV builder
            builder.append(columns?.joinToString(",") ?: "")

            // Iterate over each data entry in the dataList
            for (data in dataList) {
                // Handle image field, adding quotes if it contains a comma
                val image = if (data.image.contains(",")) {
                    "\"${data.image.replace(",", ", ")}\""
                } else {
                    data.image
                }

                // Append the fixed columns (id, code_data, date, and image) to the CSV builder
                builder.append("\n${data.id},${data.code_data},${data.date},$image")

                // Append dynamic columns, if any
                if (data.dynamicColumns.isNotEmpty()) {
                    builder.append(",")
                    val dynamicValues = data.dynamicColumns.joinToString(",") { it.second }
                    builder.append(dynamicValues)
                }
            }

            try {
                // Write the CSV content to a file in the private storage
                openFileOutput("$tableName.csv", Context.MODE_PRIVATE).use { out ->
                    out.write(builder.toString().toByteArray())
                }

                // Get the file URI and share the CSV using an Intent
                val file = File(filesDir, "$tableName.csv")
                val path = FileProvider.getUriForFile(
                    context,
                    "com.expert.qrgenerator.fileprovider",
                    file
                )
                dismiss()

                // Create and launch the share intent
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(Intent.EXTRA_STREAM, path)
                }
                startActivity(Intent.createChooser(intent, "Share with"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // Show an alert if dataList is empty and no CSV export can be performed
            showAlert(context, getString(R.string.table_export_error_text))
        }
    }


    private fun importCsv(tableName: String) {
        // Open the file picker to allow the user to select a CSV file
        openFilePicker()
    }

    private fun openFilePicker() {
        // Create an intent to open a file picker
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            // Set the MIME type to CSV files
            type = "text/csv"
            // Add category to ensure only files are shown
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        // Launch the file picker using the result launcher
        fileResultLauncher.launch(intent)
    }

    // Register an ActivityResultLauncher to handle the result of an activity
    private val fileResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        // Check if the result code is OK
        if (result.resultCode == Activity.RESULT_OK) {
            // Safely retrieve the file URI from the result data
            result.data?.data?.let { fileUri ->
                // Handle the file URI (e.g., use it to display or process the selected file)
                // For example: val filePath = fileUri.toString()
            } ?: run {
                // Handle the case where data is null
                Log.e("FileResult", "No file data available")
            }
        } else {
            // Handle the case where the result code is not OK
            Log.e("FileResult", "Result code is not OK: ${result.resultCode}")
        }
    }


}