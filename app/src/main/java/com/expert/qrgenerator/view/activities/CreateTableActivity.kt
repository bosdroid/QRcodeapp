package com.expert.qrgenerator.view.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.FieldListsAdapter
import com.expert.qrgenerator.model.ListItem
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.utils.TableGenerator
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import java.util.*

class CreateTableActivity : BaseActivity(), View.OnClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var tableGenerator: TableGenerator
    private var tableName: String = ""
    private lateinit var createTableFieldHint: MaterialTextView
    private lateinit var selectedListTextView:MaterialTextView
//    private lateinit var addNewFieldBtn: AppCompatButton
    private lateinit var addNewFieldLayoutWrapper: CardView
    private lateinit var tableNewFieldNameTInput: TextInputEditText
    private lateinit var nonChangeableCheckBoxRadioButton: MaterialRadioButton
    private lateinit var listWithValuesFieldRadioButton: MaterialRadioButton
    private lateinit var fieldValueTypesRadioGroup: RadioGroup
    private lateinit var defaultValueFieldTInput: TextInputEditText
    private var isNonChangeableCheckBox = false
    private lateinit var submitBtnView: MaterialButton
    private lateinit var finishBtnView: MaterialButton
    private var defaultColumnValue: String = ""
    private lateinit var tableColumnsDetailLayout: LinearLayout
    private lateinit var listWithFieldsBtn: MaterialButton
    private lateinit var appViewModel: AppViewModel
    private var fieldType:String = "none"
    private lateinit var scrollCreateTable:ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_table)

        initViews()
        setUpToolbar()

    }

    private fun initViews() {
        context = this
        appViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(this.application)
        ).get(AppViewModel::class.java)
        tableGenerator = TableGenerator(context)
        toolbar = findViewById(R.id.toolbar)
        scrollCreateTable = findViewById(R.id.scroll_create_table)
        if (intent != null && intent.hasExtra("TABLE_NAME")) {
            tableName = intent.getStringExtra("TABLE_NAME")!!
        }
        createTableFieldHint = findViewById(R.id.create_table_fields_hint)
        createTableFieldHint.text = "${getString(R.string.create_table_fields_hint_text)}$tableName"
//        addNewFieldBtn = findViewById(R.id.add_new_field_btn)
//        addNewFieldBtn.setOnClickListener(this)
        addNewFieldLayoutWrapper = findViewById(R.id.add_field_layout_wrapper)
        tableNewFieldNameTInput = findViewById(R.id.table_new_field_text_input)
        tableNewFieldNameTInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var newStr = s.toString()
                newStr = newStr.replace("[^a-zA-Z ]*".toRegex(), "")
                if (s.toString() != newStr) {
                    Toast.makeText(context,getString(R.string.characters_special_error_text),Toast.LENGTH_SHORT).show()
                    tableNewFieldNameTInput.setText(newStr)
                    tableNewFieldNameTInput.setSelection(tableNewFieldNameTInput.text!!.length)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        nonChangeableCheckBoxRadioButton = findViewById(R.id.non_changeable_radio_btn)
        listWithValuesFieldRadioButton = findViewById(R.id.list_with_values_radio_btn)
        fieldValueTypesRadioGroup = findViewById(R.id.value_types_radio_group)
        defaultValueFieldTInput = findViewById(R.id.table_non_changeable_default_text_input)
        defaultValueFieldTInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var newStr = s.toString()
                newStr = newStr.replace("[^a-zA-Z0-9 ]*".toRegex(), "")
                if (s.toString() != newStr) {
                    Toast.makeText(context,getString(R.string.characters_special_error_text),Toast.LENGTH_SHORT).show()
                    defaultValueFieldTInput.setText(newStr)
                    defaultValueFieldTInput.setSelection(defaultValueFieldTInput.text!!.length)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        submitBtnView = findViewById(R.id.field_submit_btn)
        submitBtnView.setOnClickListener(this)
        finishBtnView = findViewById(R.id.field_finish_btn)
        finishBtnView.setOnClickListener(this)
        tableColumnsDetailLayout = findViewById(R.id.table_columns_detail_layout)
        listWithFieldsBtn = findViewById(R.id.list_with_fields_btn)
        listWithFieldsBtn.setOnClickListener(this)
        selectedListTextView = findViewById(R.id.select_list_text_view)

        // fieldValueTypesRadioGroup RADIO GROUP LISTENER
        fieldValueTypesRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.none_radio_btn -> {
                    fieldType = "none"
                    defaultValueFieldTInput.visibility = View.GONE
                    listWithFieldsBtn.visibility = View.GONE
                    isNonChangeableCheckBox = false
                }
                R.id.non_changeable_radio_btn -> {
                    isNonChangeableCheckBox = true
                    defaultValueFieldTInput.visibility = View.VISIBLE
                    listWithFieldsBtn.visibility = View.GONE
                    fieldType = "nonChangeable"
                    scrollCreateTable.fullScroll(ScrollView.FOCUS_UP)
                    hideSoftKeyboard(context, scrollCreateTable)
                }
                R.id.list_with_values_radio_btn -> {
                    isNonChangeableCheckBox = false
                    defaultValueFieldTInput.visibility = View.GONE
                    listWithFieldsBtn.visibility = View.VISIBLE
                    fieldType = "listWithValues"
                }
                else -> {

                }
            }
        }

    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = tableName
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
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

    override fun onResume() {
        super.onResume()
        displayColumnDetails()
    }

    private fun displayColumnDetails() {
        if (tableName.isNotEmpty()) {
            if (tableGenerator.tableExists(tableName)) {
                val columns = tableGenerator.getTableColumns(tableName)
                if (columns != null && columns.isNotEmpty()) {
                    if (tableColumnsDetailLayout.childCount > 0){
                        tableColumnsDetailLayout.removeAllViews()
                    }
                    for (i in columns.indices) {
                        val layout = LayoutInflater.from(context)
                            .inflate(
                                R.layout.table_column_item_row,
                                tableColumnsDetailLayout,
                                false
                            )
                        val columnNameView =
                            layout.findViewById<MaterialTextView>(R.id.table_column_name)
                        val columnNameSubTitleView =
                            layout.findViewById<MaterialTextView>(R.id.table_column_sub_title)
                        when (columns[i]) {
                            "id" -> {
                                columnNameView.text = getString(R.string.code_id_heading)
                                columnNameSubTitleView.text = getString(R.string.code_id_sub_heading)
                            }
                            "code_data" -> {
                                columnNameView.text = getString(R.string.code_data_heading)
                                columnNameSubTitleView.text =
                                    getString(R.string.code_data_sub_heading)
                            }
                            "date" -> {
                                columnNameView.text = getString(R.string.code_date_heading)
                                columnNameSubTitleView.text =
                                    getString(R.string.code_date_sub_heading)
                            }
                            "image" -> {
                                columnNameView.text = getString(R.string.code_image_heading)
                                columnNameSubTitleView.text = getString(R.string.code_image_sub_heading)
                            }
                            else ->{
                                    columnNameView.text = columns[i]
                            }
                        }
                        tableColumnsDetailLayout.addView(layout)
                    }
                }
            }

        }
    }

    private fun resetViews() {
        tableNewFieldNameTInput.setText("")
        defaultValueFieldTInput.setText("")
        fieldValueTypesRadioGroup.findViewById<RadioButton>(R.id.none_radio_btn).isChecked = true
        selectedListTextView.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.field_finish_btn -> {
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            R.id.list_with_fields_btn -> {
                openListWithFieldsDialog()
            }
            R.id.field_submit_btn -> {
                if (validation()) {
                    startLoading(context)
                    val fieldName = tableNewFieldNameTInput.text.toString().trim().toLowerCase(
                        Locale.ENGLISH
                    ).replace(" ", "_")
                    if (fieldType == "none") {
                        tableGenerator.addNewColumn(
                            tableName,
                            Pair(fieldName, "TEXT"),
                            ""
                        )
                    } else if (fieldType == "nonChangeable") {
                        defaultColumnValue = defaultValueFieldTInput.text.toString().trim()
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
                    } else if (fieldType == "listWithValues") {
                        tableGenerator.addNewColumn(
                            tableName,
                            Pair(fieldName, "TEXT"),
                            ""
                        )
                        val listOptions: String = tableGenerator.getListValues(listId!!)
                        tableGenerator.insertFieldList(
                            fieldName,
                            tableName,
                            listOptions,
                            "listWithValues"
                        )
                    }


                    Handler(Looper.myLooper()!!).postDelayed({
                        val layout =
                            LayoutInflater.from(context).inflate(
                                R.layout.table_column_item_row,
                                tableColumnsDetailLayout,
                                false
                            )
                        val columnNameView =
                            layout.findViewById<MaterialTextView>(R.id.table_column_name)
                        columnNameView.text = tableNewFieldNameTInput.text.toString().trim()
                        tableColumnsDetailLayout.addView(layout)
                        dismiss()
//                        addNewFieldLayoutWrapper.visibility = View.GONE
//                        addNewFieldBtn.visibility = View.VISIBLE
                        resetViews()

                    }, 2000)
                }

            }
            else -> {

            }
        }
    }

    private lateinit var adapter: FieldListsAdapter
    private var listId:Int?=null
    private fun openListWithFieldsDialog() {
        val listItems = mutableListOf<ListItem>()
        val layout =
            LayoutInflater.from(context).inflate(R.layout.list_with_fields_value_layout, null)
        val listWithFieldsValueRecyclerView =
            layout.findViewById<RecyclerView>(R.id.list_with_fields_recycler_view)
        listWithFieldsValueRecyclerView.layoutManager = LinearLayoutManager(context)
        listWithFieldsValueRecyclerView.hasFixedSize()
        adapter = FieldListsAdapter(context, listItems as ArrayList<ListItem>)
        listWithFieldsValueRecyclerView.adapter = adapter


        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(layout)
        builder.setCancelable(true)
        val alert = builder.create()
        alert.show()
        val tempList = tableGenerator.getList()
        if (tempList.isNotEmpty()){
            listItems.clear()
            listItems.addAll(tempList)
            adapter.notifyDataSetChanged()
        }
        else{
            adapter.notifyDataSetChanged()
        }


        adapter.setOnItemClickListener(object : FieldListsAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val listValue = listItems[position]
                listId = listValue.id
                val list = tableGenerator.getListValues(listId!!)
                if (list.isNotEmpty()) {
                    selectedListTextView.text = listValue.value
                    alert.dismiss()
                } else {
                    MaterialAlertDialogBuilder(context)
                        .setMessage(getString(R.string.field_list_value_empty_error_text))
                        .setNegativeButton(getString(R.string.cancel_text)) { dialog, which ->
                            dialog.dismiss()
                        }
                        .setPositiveButton(getString(R.string.add_text)) { dialog, which ->
                            dialog.dismiss()
                            addTableDialog(listId!!)
                        }
                        .create().show()
                }

            }

            override fun onAddItemClick(position: Int) {
                alert.dismiss()
                startActivity(Intent(context, FieldListsActivity::class.java))
            }
        })
    }

    private fun addTableDialog(id: Int){
        val listValueLayout = LayoutInflater.from(context).inflate(
            R.layout.add_list_value_layout,
            null
        )
        val heading = listValueLayout.findViewById<MaterialTextView>(R.id.dialog_heading)
        heading.text = getString(R.string.list_value_hint_text)
        val listValueInputBox = listValueLayout.findViewById<TextInputEditText>(R.id.add_list_value_input_field)
        val listValueAddBtn = listValueLayout.findViewById<MaterialButton>(R.id.add_list_value_btn)
        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(listValueLayout)
        val alert = builder.create()
        alert.show()
        listValueAddBtn.setOnClickListener {
            if(listValueInputBox.text.toString().isNotEmpty()){
               val value = listValueInputBox.text.toString().trim()
                tableGenerator.insertListValue(id, value)
                alert.dismiss()
            }
            else{
                showAlert(context, getString(R.string.add_list_value_error_text))
            }
        }
    }

    private fun validation(): Boolean {
        if(tableNewFieldNameTInput.text.toString().isEmpty()){
            showAlert(context, getString(R.string.add_column_name_error_text))
            return false
        }
        else if (isNonChangeableCheckBox && defaultValueFieldTInput.text.toString().isEmpty()) {
            showAlert(context, getString(R.string.default_column_value_error_text))
            return false
        }
        else if (fieldType == "listWithValues" && listId == null){
            showAlert(context, getString(R.string.field_type_error_text))
            return false
        }
        return true
    }
}