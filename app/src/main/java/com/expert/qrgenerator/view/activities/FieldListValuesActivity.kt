package com.expert.qrgenerator.view.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.FieldListAdapter
import com.expert.qrgenerator.adapters.FieldListValuesAdapter
import com.expert.qrgenerator.model.ListItem
import com.expert.qrgenerator.utils.TableGenerator
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import java.util.*
import kotlin.collections.ArrayList

class FieldListValuesActivity : BaseActivity(), FieldListValuesAdapter.OnItemClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var fieldListValuesRecyclerView: RecyclerView
    private var listValues = mutableListOf<String>()
    private lateinit var tableGenerator: TableGenerator
    private lateinit var adapter: FieldListValuesAdapter
    private var listItem:ListItem?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_field_list_values)

        initViews()
        setUpToolbar()
        getListValues()
    }

    private fun initViews() {
        context = this
        if (intent!= null && intent.hasExtra("LIST_ITEM")){
            listItem = intent.getSerializableExtra("LIST_ITEM") as ListItem
        }
        tableGenerator = TableGenerator(context)
        toolbar = findViewById(R.id.toolbar)
        fieldListValuesRecyclerView = findViewById(R.id.field_list_values_recyclerview)
        fieldListValuesRecyclerView.layoutManager = LinearLayoutManager(context)
        fieldListValuesRecyclerView.hasFixedSize()
        adapter = FieldListValuesAdapter(context, listValues as ArrayList<String>)
        fieldListValuesRecyclerView.adapter = adapter
        adapter.setOnItemClickListener(this)
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = listItem!!.value
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }


    private fun getListValues(){
        val tempList = tableGenerator.getFieldListValues(listItem!!.id)
        if (tempList.isNotEmpty()){
            listValues.clear()
            listValues.addAll(tempList)
            adapter.notifyDataSetChanged()
        }
        else{
            adapter.notifyDataSetChanged()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(position: Int) {

    }

    override fun onAddItemClick(position: Int) {
        addListItemDialog(listItem!!.id)
    }

    private fun addListItemDialog(id:Int) {
        val listCreateLayout =
            LayoutInflater.from(context).inflate(R.layout.add_list_value_layout, null)
        val dialogHeading = listCreateLayout.findViewById<MaterialTextView>(R.id.dialog_heading)
        dialogHeading.text = "Enter the List value"
        val textInputBox =
            listCreateLayout.findViewById<TextInputEditText>(R.id.add_list_value_input_field)
        val listItemCreateBtn = listCreateLayout.findViewById<MaterialButton>(R.id.add_list_value_btn)
        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(listCreateLayout)
        val alert = builder.create()
        alert.show()
        listItemCreateBtn.setOnClickListener {
            if (textInputBox.text.toString().isNotEmpty()) {

                    tableGenerator.insertListValue(id,textInputBox.text.toString().trim().toLowerCase(
                        Locale.ENGLISH))
                    Toast.makeText(context, "List Item has been added successfully!", Toast.LENGTH_SHORT)
                        .show()
                    alert.dismiss()

                getListValues()
            } else {
                showAlert(context, "Please enter the list name!")
            }
        }
    }
}