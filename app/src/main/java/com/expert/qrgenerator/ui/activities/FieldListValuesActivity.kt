package com.expert.qrgenerator.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.FieldListValuesAdapter
import com.expert.qrgenerator.databinding.ActivityFieldListValuesBinding
import com.expert.qrgenerator.model.ListItem
import com.expert.qrgenerator.utils.TableGenerator
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class FieldListValuesActivity : BaseActivity(), FieldListValuesAdapter.OnItemClickListener {

    private lateinit var binding: ActivityFieldListValuesBinding
    private lateinit var context: Context
    private var listValues = mutableListOf<String>()
    private lateinit var tableGenerator: TableGenerator
    private lateinit var adapter: FieldListValuesAdapter
    private var listItem: ListItem? = null
    private var tableName = ""
    private var flag = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFieldListValuesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setUpToolbar()
        getListValues()
    }

    private fun initViews() {
        context = this
        if (intent != null && intent.hasExtra("LIST_ITEM")) {
            listItem = intent.getSerializableExtra("LIST_ITEM") as ListItem
        }
        if (intent != null && intent.hasExtra("TABLE_NAME")) {
            tableName = intent.getStringExtra("TABLE_NAME")!!
        }
        if (intent != null && intent.hasExtra("FLAG")) {
            flag = intent.getStringExtra("FLAG")!!
        }
        tableGenerator = TableGenerator(context)

        binding.fieldListValuesRecyclerview.layoutManager = LinearLayoutManager(context)
        binding.fieldListValuesRecyclerview.hasFixedSize()
        adapter = FieldListValuesAdapter(context, listValues as ArrayList<String>)
        binding.fieldListValuesRecyclerview.adapter = adapter
        adapter.setOnItemClickListener(this)
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = listItem!!.value
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }


    private fun getListValues() {
        val tempList = tableGenerator.getFieldListValues(listItem!!.id)
        if (tempList.isNotEmpty()) {
            listValues.clear()
            listValues.addAll(tempList)
            adapter.notifyDataSetChanged()
        } else {
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

    override fun onFinishItemClick() {
        if (tableName.isNotEmpty() && flag.isNotEmpty()) {
            val intent = Intent(context,CreateTableActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("TABLE_NAME",tableName)
            startActivity(intent)
            finish()
        } else {
            finish()
        }
    }

    private fun addListItemDialog(id: Int) {
        val listCreateLayout =
            LayoutInflater.from(context).inflate(R.layout.add_list_value_layout, null)
        val dialogHeading = listCreateLayout.findViewById<MaterialTextView>(R.id.dialog_heading)
        dialogHeading.text = getString(R.string.list_value_hint_text)
        val textInputBox =
            listCreateLayout.findViewById<TextInputEditText>(R.id.add_list_value_input_field)
        val listItemCreateBtn =
            listCreateLayout.findViewById<MaterialButton>(R.id.add_list_value_btn)
        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(listCreateLayout)
        val alert = builder.create()
        alert.show()
        listItemCreateBtn.setOnClickListener {
            if (textInputBox.text.toString().isNotEmpty()) {

                tableGenerator.insertListValue(
                    id, textInputBox.text.toString().trim().toLowerCase(
                        Locale.ENGLISH
                    )
                )
                Toast.makeText(
                    context,
                    getString(R.string.list_item_success_text),
                    Toast.LENGTH_SHORT
                )
                    .show()
                alert.dismiss()

                getListValues()
            } else {
                showAlert(context, getString(R.string.list_name_empty_error_text))
            }
        }
    }
}