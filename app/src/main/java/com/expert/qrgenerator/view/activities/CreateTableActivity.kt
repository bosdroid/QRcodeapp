package com.expert.qrgenerator.view.activities

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.expert.qrgenerator.R
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.TableGenerator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView

class CreateTableActivity : BaseActivity(), View.OnClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var tableGenerator: TableGenerator
    private var tableName: String = ""
    private lateinit var tableColumnsView: MaterialTextView
    private lateinit var addNewFieldBtn: AppCompatButton
    private lateinit var addNewFieldLayoutWrapper: LinearLayout
    private lateinit var tableNewFieldNameTInput: TextInputEditText
    private lateinit var nonChangeableCheckBox: AppCompatCheckBox
    private lateinit var defaultValueFieldTInput: TextInputEditText
    private var isNonChangeableCheckBox = false
    private lateinit var submitBtnView: AppCompatButton
    private var defaultColumnValue: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_table)

        initViews()
        setUpToolbar()

    }

    private fun initViews() {
        context = this
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
        nonChangeableCheckBox = findViewById(R.id.non_changeable_check_box)
        defaultValueFieldTInput = findViewById(R.id.table_non_changeable_default_text_input)
        submitBtnView = findViewById(R.id.field_submit_btn)
        submitBtnView.setOnClickListener(this)

        nonChangeableCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                isNonChangeableCheckBox = true
                defaultValueFieldTInput.visibility = View.VISIBLE
            } else {
                isNonChangeableCheckBox = false
                defaultValueFieldTInput.visibility = View.GONE
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
        displayColumnDetails("")
    }

    private fun displayColumnDetails(newColumn:String) {
        var details = ""
        if (tableName.isNotEmpty()) {
            if (tableGenerator.tableExists(tableName)) {
                val columns = tableGenerator.getTableColumns(tableName)
                if (columns != null && columns.isNotEmpty()) {
                    for (i in columns.indices) {
                        details += "Column: ${columns[i]}\n"
                    }
                    tableColumnsView.text = details
                }
                if (newColumn.isNotEmpty()){
                    details += "Column: $newColumn\n"
                    tableColumnsView.text = details
                }
            }
        }
    }

    private fun resetViews() {
        displayColumnDetails(tableNewFieldNameTInput.text.toString().trim())
        tableNewFieldNameTInput.setText("")
        nonChangeableCheckBox.isChecked = false
        defaultValueFieldTInput.setText("")
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.add_new_field_btn -> {
                if (Constants.userData != null){
                    addNewFieldBtn.visibility = View.GONE
                    addNewFieldLayoutWrapper.visibility = View.VISIBLE
                }
                else{
                    showAlert(context,"You can not create dynamic table without account login!")
                }
            }
            R.id.field_submit_btn -> {
                if (validation()) {
                    defaultColumnValue = if (isNonChangeableCheckBox) {
                        defaultValueFieldTInput.text.toString().trim()
                    } else {
                        ""
                    }
                    tableGenerator.addNewColumn(
                        tableName,
                        Pair(tableNewFieldNameTInput.text.toString().trim(), "TEXT"),
                        defaultColumnValue
                    )
                    addNewFieldLayoutWrapper.visibility = View.GONE
                    addNewFieldBtn.visibility = View.VISIBLE
                    Handler(Looper.myLooper()!!).postDelayed({
                        resetViews()
                    },2000)
                }

            }
            else -> {

            }
        }
    }

    private fun validation(): Boolean {
        if (tableNewFieldNameTInput.text.toString().isEmpty()) {
            showAlert(context, "Please enter the field/column name!")
            return false
        } else if (isNonChangeableCheckBox && defaultValueFieldTInput.text.toString().isEmpty()) {
            showAlert(context, "Please set the default value of field/column!")
            return false
        }
        return true
    }
}