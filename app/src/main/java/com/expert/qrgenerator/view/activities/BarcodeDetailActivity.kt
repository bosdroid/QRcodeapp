package com.expert.qrgenerator.view.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.expert.qrgenerator.R
import com.expert.qrgenerator.model.TableObject
import com.google.android.material.textview.MaterialTextView

class BarcodeDetailActivity : BaseActivity() {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private var tableObject: TableObject?=null
    private var tableName:String = ""
    private lateinit var barcodeDetailParentLayout:LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_detail)

        initViews()
        setUpToolbar()
        displayBarcodeDetail()
    }

    private fun initViews(){
        context = this
        toolbar = findViewById(R.id.toolbar)
        barcodeDetailParentLayout = findViewById(R.id.barcode_detail_wrapper_layout)
        if (intent != null && intent.hasExtra("TABLE_ITEM")) {
            tableObject = intent.getSerializableExtra("TABLE_ITEM") as TableObject
        }
        if (intent != null && intent.hasExtra("TABLE_NAME")) {
            tableName = intent.getStringExtra("TABLE_NAME") as String
        }


    }

    // THIS FUNCTION WILL RENDER THE ACTION BAR/TOOLBAR
    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.barcode_detail_text)
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

    private fun displayBarcodeDetail(){
        if (tableObject != null){
            val codeDataLayout = LayoutInflater.from(context).inflate(R.layout.barcode_detail_item_row, barcodeDetailParentLayout,false)
            val codeDataColumnValue = codeDataLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_value)
            val codeDataColumnName = codeDataLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_name)
            val codeDataColumnEditView = codeDataLayout.findViewById<AppCompatImageView>(R.id.bcd_edit_view)

            codeDataColumnValue.text = tableObject!!.code_data
            codeDataColumnName.text = "code_data"
            barcodeDetailParentLayout.addView(codeDataLayout)
            val dateLayout = LayoutInflater.from(context).inflate(R.layout.barcode_detail_item_row, barcodeDetailParentLayout,false)
            val dateColumnValue = dateLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_value)
            val dateColumnName = dateLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_name)
            val dateColumnEditView = dateLayout.findViewById<AppCompatImageView>(R.id.bcd_edit_view)
            dateColumnValue.text = tableObject!!.date
            dateColumnName.text = "date"
            barcodeDetailParentLayout.addView(dateLayout)
            val imageLayout = LayoutInflater.from(context).inflate(R.layout.barcode_detail_item_row, barcodeDetailParentLayout,false)
            val imageColumnValue = imageLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_value)
            val imageColumnName = imageLayout.findViewById<MaterialTextView>(R.id.bcd_table_column_name)
            val imageColumnEditView = imageLayout.findViewById<AppCompatImageView>(R.id.bcd_edit_view)
            imageColumnValue.text = tableObject!!.image
            imageColumnName.text = "image"
            barcodeDetailParentLayout.addView(imageLayout)

            for (i in 0 until tableObject!!.dynamicColumns.size){
                val item = tableObject!!.dynamicColumns[i]
                val layout = LayoutInflater.from(context).inflate(R.layout.barcode_detail_item_row, barcodeDetailParentLayout,false)
                val columnValue = layout.findViewById<MaterialTextView>(R.id.bcd_table_column_value)
                val columnName = layout.findViewById<MaterialTextView>(R.id.bcd_table_column_name)
                val columnEditView = layout.findViewById<AppCompatImageView>(R.id.bcd_edit_view)
                columnValue.text = item.second
                columnName.text = item.first
                barcodeDetailParentLayout.addView(layout)
            }

        }
    }
}