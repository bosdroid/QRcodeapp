package com.expert.qrgenerator.view.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.FieldListsAdapter
import com.expert.qrgenerator.model.ListItem
import com.expert.qrgenerator.model.ListValue
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.utils.TableGenerator
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import java.util.*
import kotlin.collections.ArrayList

class CreateTableActivity : BaseActivity(), View.OnClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var tableGenerator: TableGenerator
    private var tableName: String = ""
    private lateinit var tableColumnsView: MaterialTextView
    private lateinit var addNewFieldBtn: AppCompatButton
    private lateinit var addNewFieldLayoutWrapper: LinearLayout
    private lateinit var tableNewFieldNameTInput: TextInputEditText
    private lateinit var nonChangeableCheckBoxRadioButton: MaterialRadioButton
    private lateinit var listWithValuesFieldRadioButton: MaterialRadioButton
    private lateinit var fieldValueTypesRadioGroup: RadioGroup
    private lateinit var defaultValueFieldTInput: TextInputEditText
    private var isNonChangeableCheckBox = false
    private lateinit var submitBtnView: AppCompatButton
    private var defaultColumnValue: String = ""
    private lateinit var tableColumnsDetailLayout: LinearLayout
    private lateinit var listWithFieldsBtn: MaterialButton
    private lateinit var appViewModel: AppViewModel
    private var fieldType:String = "none"

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
        if (intent != null && intent.hasExtra("TABLE_NAME")) {
            tableName = intent.getStringExtra("TABLE_NAME")!!
        }
        tableColumnsView = findViewById(R.id.table_columns_detail)
        addNewFieldBtn = findViewById(R.id.add_new_field_btn)
        addNewFieldBtn.setOnClickListener(this)
        addNewFieldLayoutWrapper = findViewById(R.id.add_field_layout_wrapper)
        tableNewFieldNameTInput = findViewById(R.id.table_new_field_text_input)
        nonChangeableCheckBoxRadioButton = findViewById(R.id.non_changeable_radio_btn)
        listWithValuesFieldRadioButton = findViewById(R.id.list_with_values_radio_btn)
        fieldValueTypesRadioGroup = findViewById(R.id.value_types_radio_group)
        defaultValueFieldTInput = findViewById(R.id.table_non_changeable_default_text_input)
        submitBtnView = findViewById(R.id.field_submit_btn)
        submitBtnView.setOnClickListener(this)
        tableColumnsDetailLayout = findViewById(R.id.table_columns_detail_layout)
        listWithFieldsBtn = findViewById(R.id.list_with_fields_btn)
        listWithFieldsBtn.setOnClickListener(this)

        // fieldValueTypesRadioGroup RADIO GROUP LISTENER
        fieldValueTypesRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.none_radio_btn ->{
                    fieldType = "none"
                    defaultValueFieldTInput.visibility = View.GONE
                    listWithFieldsBtn.visibility = View.GONE
                }
                R.id.non_changeable_radio_btn -> {
                    isNonChangeableCheckBox = true
                    defaultValueFieldTInput.visibility = View.VISIBLE
                    listWithFieldsBtn.visibility = View.GONE
                    fieldType = "nonChangeable"
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
                            .inflate(R.layout.table_column_item_row, null)
                        val columnNameView =
                            layout.findViewById<MaterialTextView>(R.id.table_column_name)
                        when (columns[i]) {
                            "id" -> {
                                columnNameView.text = "# of the barcode"
                            }
                            "code_data" -> {
                                columnNameView.text = "Barcode data"
                            }
                            "date" -> {
                                columnNameView.text = "Date of scanning"
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
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.add_new_field_btn -> {
//                if (Constants.userData != null) {
                    addNewFieldBtn.visibility = View.GONE
                    addNewFieldLayoutWrapper.visibility = View.VISIBLE
//                } else {
//                    showAlert(context, "You can not create dynamic table without account login!")
//                }
            }
            R.id.list_with_fields_btn -> {
                openListWithFieldsDialog()
            }
            R.id.field_submit_btn -> {
                if (validation()) {
                    val fieldName = tableNewFieldNameTInput.text.toString().trim().toLowerCase(
                        Locale.ENGLISH
                    ).replace(" ","_")
                    if (fieldType == "none"){
                        tableGenerator.addNewColumn(
                            tableName,
                            Pair(fieldName, "TEXT"),
                            ""
                        )
                    }
                    else if (fieldType == "nonChangeable"){
                        tableGenerator.addNewColumn(
                            tableName,
                            Pair(fieldName, "TEXT"),
                            defaultColumnValue
                        )
                    }
                    else if(fieldType == "listWithValues"){
                        tableGenerator.addNewColumn(
                            tableName,
                            Pair(fieldName, "TEXT"),
                            ""
                        )
                        val listOptions:String = tableGenerator.getListValues(listId!!)
                        tableGenerator.insertFieldList(fieldName,tableName,listOptions)
                    }
                    addNewFieldLayoutWrapper.visibility = View.GONE
                    addNewFieldBtn.visibility = View.VISIBLE

                    Handler(Looper.myLooper()!!).postDelayed({
                        val layout =
                            LayoutInflater.from(context).inflate(R.layout.table_column_item_row, null)
                        val columnNameView =
                            layout.findViewById<MaterialTextView>(R.id.table_column_name)
                        columnNameView.text = tableNewFieldNameTInput.text.toString().trim()
                        tableColumnsDetailLayout.addView(layout)
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
//        appViewModel.getAllListValues().observe(this, Observer { list->
//            if (list.isNotEmpty()){
//                listItems.clear()
//                listItems.addAll(list)
//                adapter.notifyDataSetChanged()
//            }
//            else{
//                adapter.notifyDataSetChanged()
//            }
//        })

        adapter.setOnItemClickListener(object : FieldListsAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                val listValue = listItems[position]
                listId = listValue.id
                alert.dismiss()
            }

            override fun onAddItemClick(position: Int) {
                alert.dismiss()
                startActivity(Intent(context,FieldListsActivity::class.java))
            }
        })
    }

    private fun addTableDialog(){
        val listValueLayout = LayoutInflater.from(context).inflate(R.layout.add_list_value_layout,null)
        val listValueInputBox = listValueLayout.findViewById<TextInputEditText>(R.id.add_list_value_input_field)
        val listValueAddBtn = listValueLayout.findViewById<MaterialButton>(R.id.add_list_value_btn)
        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(listValueLayout)
        val alert = builder.create()
        alert.show()
        listValueAddBtn.setOnClickListener {
            if(listValueInputBox.text.toString().isNotEmpty()){
               val value = listValueInputBox.text.toString().trim()
                appViewModel.insertListValue(ListValue(value))
                alert.dismiss()
            }
            else{
                showAlert(context,"Please enter the list value!")
            }
        }
    }

    private fun validation(): Boolean {
        if(tableNewFieldNameTInput.text.toString().isEmpty()){
            showAlert(context, "Please enter the field/column name!")
            return false
        }
        else if (isNonChangeableCheckBox && defaultValueFieldTInput.text.toString().isEmpty()) {
            showAlert(context, "Please set the default value of field/column!")
            return false
        }
        else if (fieldType == "listWithValues" && listId == null){
            showAlert(context, "You can't use this type because field list is empty!")
            return false
        }
        return true
    }
}