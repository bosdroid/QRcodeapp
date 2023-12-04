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
import com.expert.qrgenerator.adapters.FieldListAdapter
import com.expert.qrgenerator.databinding.ActivityFieldListsBinding
import com.expert.qrgenerator.model.ListItem
import com.expert.qrgenerator.utils.TableGenerator
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class FieldListsActivity : BaseActivity() {

    private lateinit var binding:ActivityFieldListsBinding
    private lateinit var context: Context

    private var list = mutableListOf<ListItem>()
    private lateinit var adapter: FieldListAdapter
    private lateinit var tableGenerator: TableGenerator
    private var tableName = ""
    private var flag = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFieldListsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setUpToolbar()

    }

    private fun initViews() {
        context = this
        tableGenerator = TableGenerator(context)

        binding.fieldTypeListRecyclerview.layoutManager = LinearLayoutManager(context)
        binding.fieldTypeListRecyclerview.hasFixedSize()
        adapter = FieldListAdapter(context, list as ArrayList<ListItem>)
        binding.fieldTypeListRecyclerview.adapter = adapter

        if (intent != null && intent.hasExtra("TABLE_NAME")) {
            tableName = intent.getStringExtra("TABLE_NAME")!!
        }
        if (intent != null && intent.hasExtra("FLAG")) {
            flag = intent.getStringExtra("FLAG")!!
        }
    }

    private fun getList() {
        val tempList = tableGenerator.getList()
        if (tempList.isNotEmpty()) {
            list.clear()
            list.addAll(tempList)
            adapter.notifyDataSetChanged()
        } else {
            adapter.notifyDataSetChanged()
        }

        adapter.setOnItemClickListener(object : FieldListAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val list = list[position]
                val intent = Intent(context, FieldListValuesActivity::class.java)
                if (tableName.isNotEmpty() && flag.isNotEmpty()){
                    intent.putExtra("TABLE_NAME",tableName)
                    intent.putExtra("FLAG",flag)
                }
                intent.putExtra("LIST_ITEM", list)
                startActivity(intent)

            }

            override fun onAddItemClick(position: Int) {
                addListItemDialog()
            }
        })
    }

    private fun addListItemDialog() {
        val listCreateLayout =
            LayoutInflater.from(context).inflate(R.layout.add_list_value_layout, null)
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
                val listName = textInputBox.text.toString().trim().toLowerCase(Locale.ENGLISH)
                val id = tableGenerator.insertList(listName)
                Toast.makeText(context, getString(R.string.list_create_success_text), Toast.LENGTH_SHORT)
                    .show()
                alert.dismiss()
                val intent = Intent(context, FieldListValuesActivity::class.java)
                if (tableName.isNotEmpty() && flag.isNotEmpty()){
                    intent.putExtra("TABLE_NAME",tableName)
                    intent.putExtra("FLAG",flag)
                }
                intent.putExtra("LIST_ITEM", ListItem(id.toInt(),listName))
                startActivity(intent)
            } else {
                showAlert(context, getString(R.string.list_name_empty_error_text))
            }
        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = getString(R.string.field_type_list)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    override fun onResume() {
        super.onResume()
        getList()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}