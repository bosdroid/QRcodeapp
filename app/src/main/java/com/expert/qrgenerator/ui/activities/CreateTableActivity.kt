package com.expert.qrgenerator.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.FieldListsAdapter
import com.expert.qrgenerator.databinding.ActivityCreateTableBinding
import com.expert.qrgenerator.databinding.AddListValueLayoutBinding
import com.expert.qrgenerator.databinding.ListWithFieldsValueLayoutBinding
import com.expert.qrgenerator.databinding.TableColumnItemRowBinding
import com.expert.qrgenerator.model.ListItem
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.utils.AppSettings
import com.expert.qrgenerator.utils.TableGenerator
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip
import java.util.Locale
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class CreateTableActivity : BaseActivity(), View.OnClickListener {

    // View binding instance for accessing the views in the layout
    private lateinit var binding: ActivityCreateTableBinding

    // Context of the activity, initialized using lazy delegation
    private val context: Context by lazy { this }

    // Instance of TableGenerator for generating tables
    private lateinit var tableGenerator: TableGenerator

    // Name of the table to be created
    private var tableName: String = ""

    // Flag to check if the checkbox should be non-changeable
    private var isNonChangeableCheckBox = false

    // Default value for the columns in the table
    private var defaultColumnValue: String = ""

    // ViewModel instance for managing UI-related data in a lifecycle-conscious way
    private val appViewModel: AppViewModel by viewModels()

    // Type of the field, initialized as "none"
    private var fieldType: String = "none"

    // App settings instance for accessing configuration settings
    private lateinit var appSettings: AppSettings

    // Adapter for handling the list of fields in the table
    private lateinit var adapter: FieldListsAdapter

    // Optional ID for the list, nullable since it may not always have a value
    private var listId: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding
        binding = ActivityCreateTableBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views and toolbar
        initViews()
        setUpToolbar()
    }

    private fun initViews() {

        appSettings = AppSettings(context)

        // Initialize the table generator
        tableGenerator = TableGenerator(context)

        // Retrieve the table name from the intent extras
        if (intent != null && intent.hasExtra("TABLE_NAME")) {
            tableName = intent.getStringExtra("TABLE_NAME")!!
        }

        // Set hint text for creating table fields
        binding.createTableFieldsHint.text = getString(R.string.create_table_fields_hint_text)

        // Add text change listener for table field input
        binding.tableNewFieldTextInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed before text changes
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var newStr = s.toString()
                newStr = newStr.replace("[^a-zA-Z ]*".toRegex(), "")
                if (s.toString() != newStr) {
                    // Show a toast message if special characters are entered
                    Toast.makeText(
                        context,
                        getString(R.string.characters_special_error_text),
                        Toast.LENGTH_SHORT
                    ).show()
                    // Update the text input to remove invalid characters
                    binding.tableNewFieldTextInput.setText(newStr)
                    binding.tableNewFieldTextInput.setSelection(binding.tableNewFieldTextInput.text!!.length)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // No action needed after text changes
            }
        })

        // Add text change listener for non-changeable default value input
        binding.tableNonChangeableDefaultTextInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed before text changes
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var newStr = s.toString()
                newStr = newStr.replace("[^a-zA-Z0-9 ]*".toRegex(), "")
                if (s.toString() != newStr) {
                    // Show a toast message if special characters are entered
                    Toast.makeText(
                        context,
                        getString(R.string.characters_special_error_text),
                        Toast.LENGTH_SHORT
                    ).show()
                    // Update the text input to remove invalid characters
                    binding.tableNonChangeableDefaultTextInput.setText(newStr)
                    binding.tableNonChangeableDefaultTextInput.setSelection(binding.tableNonChangeableDefaultTextInput.text!!.length)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // No action needed after text changes
            }
        })

        // Set click listeners for buttons
        binding.fieldSubmitBtn.setOnClickListener(this)
        binding.fieldFinishBtn.setOnClickListener(this)
        binding.listWithFieldsBtn.setOnClickListener(this)

        // Set listener for radio group to handle different field types
        binding.valueTypesRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.none_radio_btn -> {
                    // No specific field type selected
                    fieldType = "none"
                    binding.tableNonChangeableDefaultTextInput.visibility = View.GONE
                    binding.listWithFieldsBtn.visibility = View.GONE
                    isNonChangeableCheckBox = false
                }
                R.id.non_changeable_radio_btn -> {
                    // Non-changeable field type selected
                    isNonChangeableCheckBox = true
                    binding.tableNonChangeableDefaultTextInput.visibility = View.VISIBLE
                    binding.listWithFieldsBtn.visibility = View.GONE
                    fieldType = "nonChangeable"
                    // Scroll to the top and hide the soft keyboard
                    binding.scrollCreateTable.fullScroll(ScrollView.FOCUS_UP)
                    hideSoftKeyboard(context, binding.scrollCreateTable)
                    // Show tips dialog for default value
                    openDefaultValueTipsDialog(binding.tableNonChangeableDefaultTextInput)
                }
                R.id.list_with_values_radio_btn -> {
                    // List with values field type selected
                    isNonChangeableCheckBox = false
                    binding.tableNonChangeableDefaultTextInput.visibility = View.GONE
                    binding.listWithFieldsBtn.visibility = View.VISIBLE
                    fieldType = "listWithValues"
                    // Show tips dialog for attaching list values
                    openAttachListValuesTipsDialog(binding.listWithFieldsBtn)
                }
                else -> {
                    // No action for other cases
                }
            }
        }
    }

    private fun setUpToolbar() {
        // Set the toolbar as the activity's app bar
        setSupportActionBar(binding.toolbar)

        // Set the toolbar title to the name of the table
        supportActionBar?.apply {
            title = tableName
            setDisplayHomeAsUpEnabled(true) // Enable the back/home button in the toolbar
        }

        // Set the toolbar title color to black
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))

        // Scroll the content down, ensuring visibility of all elements
        scrollDown()
    }

    private fun scrollDown() {
        // Post a delayed task to scroll the ScrollView to the bottom after 500ms
        binding.scrollCreateTable.postDelayed({
            binding.scrollCreateTable.fullScroll(ScrollView.FOCUS_DOWN)
        }, 500) // Reduced delay time for faster response
    }

    // Handles the back arrow click event in the action bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Trigger the onBackPressed function when the back arrow is clicked
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onResume() {
        super.onResume()

        // Display the details of the columns when the activity resumes
        // This could be used to refresh UI components with the latest data
        displayColumnDetails()
    }


    private fun displayColumnDetails() {
        // Ensure tableName is not empty before proceeding
        if (tableName.isNotEmpty()) {
            // Check if the table exists in the database
            if (tableGenerator.tableExists(tableName)) {
                // Retrieve the list of columns for the specified table
                val columns = tableGenerator.getTableColumns(tableName)
                // Proceed only if the columns list is not null and has elements
                if (columns != null && columns.isNotEmpty()) {
                    // Clear any previous views from the layout
                    binding.tableColumnsDetailLayout.removeAllViews()

                    // Loop through each column and create a view for it
                    columns.forEach { column ->
                        // Inflate the layout using ViewBinding
                        val layoutBinding = TableColumnItemRowBinding.inflate(
                            LayoutInflater.from(context),
                            binding.tableColumnsDetailLayout,
                            false
                        )

                        // Bind views using ViewBinding
                        val columnNameView = layoutBinding.tableColumnName
                        val columnNameSubTitleView = layoutBinding.tableColumnSubTitle

                        // Set the column name and subtitle based on the column type
                        when (column) {
                            "id" -> {
                                columnNameView.text = getString(R.string.code_id_heading)
                                columnNameSubTitleView.text = getString(R.string.code_id_sub_heading)
                            }
                            "code_data" -> {
                                columnNameView.text = getString(R.string.code_data_heading)
                                columnNameSubTitleView.text = getString(R.string.code_data_sub_heading)
                            }
                            "date" -> {
                                columnNameView.text = getString(R.string.code_date_heading)
                                columnNameSubTitleView.text = getString(R.string.code_date_sub_heading)
                            }
                            "image" -> {
                                columnNameView.text = getString(R.string.code_image_heading)
                                columnNameSubTitleView.text = getString(R.string.code_image_sub_heading)
                            }
                            "quantity" -> {
                                columnNameView.text = getString(R.string.code_quantity_heading)
                                columnNameSubTitleView.text = getString(R.string.code_quantity_sub_heading)
                            }
                            "notes" -> {
                                columnNameView.text = getString(R.string.code_notes_heading)
                                columnNameSubTitleView.text = getString(R.string.code_notes_sub_heading)
                            }
                            else -> {
                                columnNameView.text = column
                                columnNameSubTitleView.visibility = View.GONE // Hide subtitle if not predefined
                            }
                        }
                        // Add the newly created view to the layout
                        binding.tableColumnsDetailLayout.addView(layoutBinding.root)
                    }

                    // Open a dialog to provide tips on default columns
                    openDefaultColumnsTipsDialog(binding.tableColumnsDetailLayout)
                }
            }
        }
    }


    /**
     * Opens a tooltip dialog with tips for default columns if certain conditions are met.
     *
     * @param tableColumnsDetailLayout The view to which the tooltip is anchored.
     */
    private fun openDefaultColumnsTipsDialog(tableColumnsDetailLayout: LinearLayout) {
        // Check if tips are enabled in app settings
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            // Retrieve the last shown time from settings
            val lastShownDuration = appSettings.getLong("tt13")

            // Check if the tooltip has never been shown or if it's been more than a day since it was last shown
            if (lastShownDuration == 0L || System.currentTimeMillis() - lastShownDuration > TimeUnit.DAYS.toMillis(1)) {
                // Build and show the tooltip
                SimpleTooltip.Builder(context)
                    .anchorView(tableColumnsDetailLayout) // Set the view to anchor the tooltip
                    .text(getString(R.string.tt13_tip_text)) // Set the tooltip text
                    .gravity(Gravity.BOTTOM) // Position the tooltip at the bottom of the anchor view
                    .animated(true) // Enable animation
                    .transparentOverlay(false) // Set transparent overlay to false
                    .onDismissListener { tooltip ->
                        // Update the timestamp in settings when the tooltip is dismissed
                        appSettings.putLong("tt13", System.currentTimeMillis())

                        // Open another tooltip for adding new fields
                        openAddNewFieldLayoutTipsDialog(binding.addFieldLayoutWrapper)

                        // Dismiss the current tooltip
                        tooltip.dismiss()
                    }
                    .build() // Build the tooltip
                    .show() // Show the tooltip
            }
        }
    }


    /**
     * Opens a tooltip dialog to provide tips on adding a new field layout if the tips are enabled
     * and the duration since the last tip display exceeds one day.
     *
     * @param addNewFieldLayoutWrapper The CardView to which the tooltip will be anchored.
     */
    private fun openAddNewFieldLayoutTipsDialog(addNewFieldLayoutWrapper: CardView) {
        // Check if tips are enabled in app settings
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            // Retrieve the last displayed time of this tooltip
            val lastDisplayedTime = appSettings.getLong("tt14")

            // Calculate the time elapsed since the last display
            val timeElapsed = System.currentTimeMillis() - lastDisplayedTime

            // Show the tooltip if it has been more than a day since the last display or if it's the first time
            if (lastDisplayedTime == 0L || timeElapsed > TimeUnit.DAYS.toMillis(1)) {
                SimpleTooltip.Builder(context)
                    .anchorView(addNewFieldLayoutWrapper) // Set the anchor view for the tooltip
                    .text(getString(R.string.tt14_tip_text)) // Set the tooltip text from resources
                    .gravity(Gravity.BOTTOM) // Set the position of the tooltip
                    .animated(true) // Enable animation for showing the tooltip
                    .transparentOverlay(false) // Disable overlay transparency
                    .onDismissListener { tooltip ->
                        // Update the last displayed time in app settings when the tooltip is dismissed
                        appSettings.putLong("tt14", System.currentTimeMillis())

                        // Optionally open another tooltip dialog
                        openInputFieldRadioTipsDialog(binding.noneRadioBtn)

                        // Dismiss the current tooltip
                        tooltip.dismiss()
                    }
                    .build()
                    .show() // Display the tooltip
            }
        }
    }


    /**
     * Opens a tooltip dialog to provide tips for the input field radio button.
     *
     * @param noneRadioBtn The radio button that serves as the anchor for the tooltip.
     */
    private fun openInputFieldRadioTipsDialog(noneRadioBtn: MaterialRadioButton) {
        // Check if tips are enabled in app settings
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            // Retrieve the timestamp of the last tooltip display
            val lastTooltipTime = appSettings.getLong("tt15")

            // Calculate the time elapsed since the last tooltip display
            val timeElapsed = System.currentTimeMillis() - lastTooltipTime

            // Show the tooltip if it has been more than a day since the last display
            if (lastTooltipTime == 0L || timeElapsed > TimeUnit.DAYS.toMillis(1)) {
                // Build and display the tooltip
                SimpleTooltip.Builder(context)
                    .anchorView(noneRadioBtn) // Set the anchor view for the tooltip
                    .text(getString(R.string.tt15_tip_text)) // Set the text for the tooltip
                    .gravity(Gravity.BOTTOM) // Position the tooltip below the anchor view
                    .animated(true) // Enable animation
                    .transparentOverlay(false) // Do not use a transparent overlay
                    .onDismissListener { tooltip ->
                        // Update the timestamp of the last tooltip display
                        appSettings.putLong("tt15", System.currentTimeMillis())

                        // Open predefined field radio tips dialog
                        openPredefinedFieldRadioTipsDialog(binding.nonChangeableRadioBtn)

                        // Dismiss the tooltip (optional, as it is already handled by the onDismissListener)
                        tooltip.dismiss()
                    }
                    .build()
                    .show()
            }
        }
    }


    /**
     * Opens a tooltip dialog with predefined tips for the given radio button if the conditions are met.
     *
     * @param nonChangeableCheckBoxRadioButton The radio button that will anchor the tooltip.
     */
    private fun openPredefinedFieldRadioTipsDialog(nonChangeableCheckBoxRadioButton: MaterialRadioButton) {
        // Check if tips are enabled in the app settings
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            // Retrieve the last displayed time for the tip
            val lastDisplayedTime = appSettings.getLong("tt16")

            // Check if the tip hasn't been shown recently (i.e., more than 1 day ago)
            if (lastDisplayedTime == 0L || System.currentTimeMillis() - lastDisplayedTime > TimeUnit.DAYS.toMillis(1)) {
                // Create and show the tooltip
                SimpleTooltip.Builder(context)
                    .anchorView(nonChangeableCheckBoxRadioButton)
                    .text(getString(R.string.tt16_tip_text))
                    .gravity(Gravity.BOTTOM)
                    .animated(true)
                    .transparentOverlay(false)
                    .onDismissListener { tooltip ->
                        // Update the timestamp when the tooltip is dismissed
                        appSettings.putLong("tt16", System.currentTimeMillis())

                        // Optionally open another dialog after the current one is dismissed
                        openDropDownListFieldRadioTipsDialog(binding.listWithValuesRadioBtn)

                        // Dismiss the tooltip
                        tooltip.dismiss()
                    }
                    .build()
                    .show()
            }
        }
    }


    /**
     * Opens a tooltip dialog with tips for the provided MaterialRadioButton if certain conditions are met.
     * @param listWithValuesFieldRadioButton The MaterialRadioButton to anchor the tooltip to.
     */
    private fun openDropDownListFieldRadioTipsDialog(listWithValuesFieldRadioButton: MaterialRadioButton) {
        // Check if tips are enabled in app settings
        if (appSettings.getBoolean(getString(R.string.key_tips))) {

            // Get the last tooltip display timestamp
            val lastDisplayedTimestamp = appSettings.getLong("tt17")

            // Check if tooltip needs to be shown (if never shown before or if last display was more than 1 day ago)
            val currentTime = System.currentTimeMillis()
            val oneDayInMillis = TimeUnit.DAYS.toMillis(1)
            if (lastDisplayedTimestamp == 0L || currentTime - lastDisplayedTimestamp > oneDayInMillis) {

                // Create and show the tooltip
                SimpleTooltip.Builder(context)
                    .anchorView(listWithValuesFieldRadioButton) // Anchor the tooltip to the provided radio button
                    .text(getString(R.string.tt17_tip_text))    // Set the tooltip text from resources
                    .gravity(Gravity.BOTTOM)                    // Position the tooltip at the bottom
                    .animated(true)                             // Enable animation
                    .transparentOverlay(false)                  // Disable transparent overlay
                    .onDismissListener { tooltip ->             // Action on tooltip dismiss
                        // Update the last displayed timestamp in app settings
                        appSettings.putLong("tt17", currentTime)

                        // Show another tooltip or dialog (assuming this is relevant to your flow)
                        openAddingAnotherFieldTipsDialog(binding.fieldSubmitBtn)

                        // Dismiss the current tooltip
                        tooltip.dismiss()
                    }
                    .build()
                    .show()
            }
        }
    }


    /**
     * Opens a tooltip dialog to provide tips on adding another field.
     *
     * @param submitBtnView The MaterialButton that serves as the anchor for the tooltip.
     */
    private fun openAddingAnotherFieldTipsDialog(submitBtnView: MaterialButton) {
        // Check if tips are enabled in app settings
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            // Retrieve the last displayed time of the tooltip
            val lastDisplayedTime = appSettings.getLong("tt18")

            // Check if the tooltip has not been shown for more than a day
            if (lastDisplayedTime == 0L || System.currentTimeMillis() - lastDisplayedTime > TimeUnit.DAYS.toMillis(1)) {
                // Create and show the tooltip
                SimpleTooltip.Builder(context)
                    .anchorView(submitBtnView) // Anchor the tooltip to the submit button
                    .text(getString(R.string.tt18_tip_text)) // Set the text for the tooltip
                    .gravity(Gravity.TOP) // Position the tooltip above the button
                    .animated(true) // Enable animation
                    .transparentOverlay(false) // No transparent overlay
                    .onDismissListener { tooltip ->
                        // Update the timestamp of the last displayed tooltip
                        appSettings.putLong("tt18", System.currentTimeMillis())

                        // Show the finish button tooltip
                        openFinishBtnTipsDialog(binding.fieldFinishBtn)

                        // Dismiss the current tooltip
                        tooltip.dismiss()
                    }
                    .build()
                    .show()
            }
        }
    }


    /**
     * Opens a tooltip dialog with tips when the finish button is clicked, based on app settings.
     *
     * @param finishBtnView The MaterialButton that triggers the tooltip.
     */
    private fun openFinishBtnTipsDialog(finishBtnView: MaterialButton) {
        // Check if tips are enabled in app settings
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            // Retrieve the last displayed tooltip time
            val lastTipTime = appSettings.getLong("tt19")

            // Determine if the tooltip should be shown (i.e., if 1 day has passed since the last display)
            val timeElapsed = System.currentTimeMillis() - lastTipTime
            val oneDayMillis = TimeUnit.DAYS.toMillis(1)

            if (lastTipTime == 0L || timeElapsed > oneDayMillis) {
                // Build and show the tooltip
                SimpleTooltip.Builder(context)
                    .anchorView(finishBtnView)
                    .text(getString(R.string.tt19_tip_text))
                    .gravity(Gravity.TOP)
                    .animated(true)
                    .transparentOverlay(false)
                    .onDismissListener { tooltip ->
                        // Update the last displayed time in settings when the tooltip is dismissed
                        appSettings.putLong("tt19", System.currentTimeMillis())
                        tooltip.dismiss()
                    }
                    .build()
                    .show()
            }
        }
    }


    /**
     * Displays a tooltip with a tip about default values if the relevant setting is enabled and the tip hasn't been shown recently.
     *
     * @param defaultValueFieldTInput The TextInputEditText view to anchor the tooltip to.
     */
    private fun openDefaultValueTipsDialog(defaultValueFieldTInput: TextInputEditText) {
        // Check if tips are enabled in app settings
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            // Retrieve the timestamp of the last tip display
            val lastTipTimestamp = appSettings.getLong("tt20")

            // Check if the tooltip should be shown (i.e., it's been more than a day since the last display)
            if (lastTipTimestamp == 0L || System.currentTimeMillis() - lastTipTimestamp > TimeUnit.DAYS.toMillis(1)) {
                // Create and show the tooltip
                SimpleTooltip.Builder(context)
                    .anchorView(defaultValueFieldTInput) // Anchor the tooltip to the specified view
                    .text(getString(R.string.tt20_tip_text)) // Set the text for the tooltip
                    .gravity(Gravity.TOP) // Position the tooltip at the top of the anchor view
                    .animated(true) // Enable animation for the tooltip
                    .transparentOverlay(false) // Disable transparent overlay behind the tooltip
                    .onDismissListener { tooltip ->
                        // Update the timestamp of the last tip display and dismiss the tooltip
                        appSettings.putLong("tt20", System.currentTimeMillis())
                        tooltip.dismiss()
                    }
                    .build()
                    .show()
            }
        }
    }


    /**
     * Displays a tooltip with tips if the settings allow it and the tooltip hasn't been shown in the last 24 hours.
     *
     * @param listWithFieldsBtn The button to anchor the tooltip.
     */
    private fun openAttachListValuesTipsDialog(listWithFieldsBtn: MaterialButton) {
        // Check if the tips feature is enabled in app settings
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            // Retrieve the last shown time of the tooltip
            val lastShownDuration = appSettings.getLong("tt21")

            // Determine if the tooltip should be shown based on the time elapsed
            if (lastShownDuration == 0L || System.currentTimeMillis() - lastShownDuration > TimeUnit.DAYS.toMillis(1)) {
                // Create and configure the tooltip
                SimpleTooltip.Builder(context)
                    .anchorView(listWithFieldsBtn) // Set the button as the anchor view for the tooltip
                    .text(getString(R.string.tt21_tip_text)) // Set the text to display in the tooltip
                    .gravity(Gravity.TOP) // Position the tooltip at the top of the anchor view
                    .animated(true) // Enable animations for the tooltip
                    .transparentOverlay(false) // Set overlay transparency to false
                    .onDismissListener { tooltip ->
                        // Update the last shown time of the tooltip when it is dismissed
                        appSettings.putLong("tt21", System.currentTimeMillis())
                        tooltip.dismiss() // Dismiss the tooltip
                    }
                    .build() // Build the tooltip
                    .show() // Show the tooltip
            }
        }
    }


    /**
     * Resets the views to their default states.
     */
    private fun resetViews() {
        // Clear the text input fields
        binding.tableNewFieldTextInput.setText("")
        binding.tableNonChangeableDefaultTextInput.setText("")

        // Set the default radio button to checked
        binding.valueTypesRadioGroup.findViewById<RadioButton>(R.id.none_radio_btn).isChecked = true

        // Hide the select list text view
        binding.selectListTextView.visibility = View.GONE
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.field_finish_btn -> {
                // Navigate to MainActivity and clear the task stack
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
                finish()
            }

            R.id.list_with_fields_btn -> {
                // Open a dialog to select fields
                openListWithFieldsDialog()
            }

            R.id.field_submit_btn -> {
                // Validate input before proceeding
                if (validation()) {
                    startLoading(context)

                    // Prepare field name
                    val fieldName = binding.tableNewFieldTextInput.text.toString().trim()
                        .toLowerCase(Locale.ENGLISH)
                        .replace(" ", "_")

                    // Handle different field types
                    when (fieldType) {
                        "none" -> {
                            tableGenerator.addNewColumn(
                                tableName,
                                Pair(fieldName, "TEXT"),
                                ""
                            )
                        }
                        "nonChangeable" -> {
                            // Get default column value and add column
                            defaultColumnValue = binding.tableNonChangeableDefaultTextInput.text.toString().trim()
                            tableGenerator.addNewColumn(
                                tableName,
                                Pair(fieldName, "TEXT"),
                                defaultColumnValue
                            )
                            tableGenerator.insertFieldList(
                                fieldName,
                                tableName,
                                defaultColumnValue,
                                "non_changeable"
                            )
                        }
                        "listWithValues" -> {
                            // Add column and insert field list with values
                            tableGenerator.addNewColumn(
                                tableName,
                                Pair(fieldName, "TEXT"),
                                ""
                            )
                            val listOptions = tableGenerator.getListValues(listId ?: return)
                            tableGenerator.insertFieldList(
                                fieldName,
                                tableName,
                                listOptions,
                                "listWithValues"
                            )
                        }
                    }

                    // Add a new column to the layout after a delay
                    Handler(Looper.myLooper()!!).postDelayed({
                        val layout = LayoutInflater.from(context).inflate(
                            R.layout.table_column_item_row,
                            binding.tableColumnsDetailLayout,
                            false
                        )
                        val columnNameView = layout.findViewById<MaterialTextView>(R.id.table_column_name)
                        columnNameView.text = binding.tableNewFieldTextInput.text.toString().trim()
                        binding.tableColumnsDetailLayout.addView(layout)

                        // Dismiss dialog and reset views
                        dismiss()
                        scrollDown()
                        resetViews()
                    }, 2000)
                }
            }

            else -> {
                // Handle other cases if necessary
            }
        }
    }


    private fun openListWithFieldsDialog() {
        // Create a list to hold the items
        val listItems = mutableListOf<ListItem>()

        // Inflate the custom layout for the dialog
        val dialogBinding = ListWithFieldsValueLayoutBinding.inflate(LayoutInflater.from(context))

        // Set up the RecyclerView
        dialogBinding.listWithFieldsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            hasFixedSize()
            adapter = FieldListsAdapter(context, listItems as ArrayList<ListItem>).also {
                this@CreateTableActivity.adapter = it
            }
        }

        // Build and show the dialog
        val alert = MaterialAlertDialogBuilder(context)
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()
        alert.show()

        // Update listItems with data from tableGenerator
        val tempList = tableGenerator.getList()
        if (tempList.isNotEmpty()) {
            listItems.clear()
            listItems.addAll(tempList)
        }
        // Notify the adapter of data changes
        adapter.notifyDataSetChanged()

        // Set the item click listener for the adapter
        adapter.setOnItemClickListener(object : FieldListsAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val listValue = listItems[position]
                listId = listValue.id
                val list = tableGenerator.getListValues(listId!!)

                if (list.isNotEmpty()) {
                    // Update the TextView with the selected value and dismiss the dialog
                    binding.selectListTextView.text = listValue.value
                    alert.dismiss()
                } else {
                    // Show a dialog if there are no list values
                    MaterialAlertDialogBuilder(context)
                        .setMessage(getString(R.string.field_list_value_empty_error_text))
                        .setNegativeButton(getString(R.string.cancel_text)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setPositiveButton(getString(R.string.add_text)) { dialog, _ ->
                            dialog.dismiss()
                            addTableDialog(listId!!)
                        }
                        .create().show()
                }
            }

            override fun onAddItemClick(position: Int) {
                alert.dismiss()
                // Start the activity to add a new item
                Intent(context, FieldListsActivity::class.java).apply {
                    putExtra("TABLE_NAME", tableName)
                    putExtra("FLAG", "yes")
                }.also { startActivity(it) }
            }
        })
    }


    private fun addTableDialog(id: Int) {
        // Inflate the layout using ViewBinding
        val dialogBinding = AddListValueLayoutBinding.inflate(LayoutInflater.from(context))

        // Set the heading text
        dialogBinding.dialogHeading.text = getString(R.string.list_value_hint_text)

        // Create and configure the dialog
        val builder = MaterialAlertDialogBuilder(context)
            .setView(dialogBinding.root)  // Set the custom view
        val alertDialog = builder.create()

        // Show the dialog
        alertDialog.show()

        // Handle the Add button click event
        dialogBinding.addListValueBtn.setOnClickListener {
            // Check if the input field is not empty
            val value = dialogBinding.addListValueInputField.text?.toString()?.trim()
            if (!value.isNullOrEmpty()) {
                // Insert the value into the table and dismiss the dialog
                tableGenerator.insertListValue(id, value)
                alertDialog.dismiss()
            } else {
                // Show an error message if the input field is empty
                showAlert(context, getString(R.string.add_list_value_error_text))
            }
        }
    }

    /**
     * Validates the form fields and returns true if all validations pass, otherwise false.
     *
     * @return Boolean indicating whether the validation passed or not.
     */
    private fun validation(): Boolean {
        // Check if the column name field is empty
        if (binding.tableNewFieldTextInput.text.toString().isEmpty()) {
            showAlert(context, getString(R.string.add_column_name_error_text))
            return false
        }

        // Check if the 'non-changeable' field is empty when applicable
        if (isNonChangeableCheckBox && binding.tableNonChangeableDefaultTextInput.text.toString().isEmpty()) {
            showAlert(context, getString(R.string.default_column_value_error_text))
            return false
        }

        // Validate 'listWithValues' type field to ensure listId is not null
        if (fieldType == "listWithValues" && listId == null) {
            showAlert(context, getString(R.string.field_type_error_text))
            return false
        }

        // All validations passed
        return true
    }



}