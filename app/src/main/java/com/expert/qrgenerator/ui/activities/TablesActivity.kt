package com.expert.qrgenerator.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.TablesAdapter
import com.expert.qrgenerator.databinding.ActivityTablesBinding
import com.expert.qrgenerator.utils.AppSettings
import com.expert.qrgenerator.utils.TableGenerator
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TablesActivity : BaseActivity(),TablesAdapter.OnItemClickListener {

    private lateinit var binding:ActivityTablesBinding
    private lateinit var context: Context

    private lateinit var tableGenerator: TableGenerator
    private var tableList = mutableListOf<String>()
    private lateinit var adapter: TablesAdapter
    private lateinit var appSettings: AppSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityTablesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setUpToolbar()

    }


    private fun initViews() {
        context = this
        appSettings = AppSettings(context)
        tableGenerator = TableGenerator(context)

        binding.tablesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.tablesRecyclerView.hasFixedSize()
        adapter = TablesAdapter(context, tableList as ArrayList<String>)
        binding.tablesRecyclerView.adapter = adapter
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = getString(R.string.tables)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    private fun displayTableList() {
        val list = tableGenerator.getAllDatabaseTables()
        if (list.isNotEmpty()) {
            tableList.clear()
        }
        tableList.addAll(list)
        adapter.notifyDataSetChanged()
        adapter.setOnItemClickListener(this)
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

    override fun onItemClick(position: Int) {
        val table = tableList[position]
        val intent = Intent(context,CreateTableActivity::class.java)
        intent.putExtra("TABLE_NAME",table)
        startActivity(intent)
    }

    override fun onAddItemClick(position: Int) {

            addTableDialog()
    }

    override fun onResume() {
        super.onResume()
        displayTableList()
    }

    private fun addTableDialog(){
        val tableCreateLayout = LayoutInflater.from(context).inflate(R.layout.add_table_layout,null)
        val textInputBox = tableCreateLayout.findViewById<TextInputEditText>(R.id.add_table_text_input_field)
        val tableCreateBtn = tableCreateLayout.findViewById<MaterialButton>(R.id.add_table_btn)
        textInputBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var newStr = s.toString()
                newStr = newStr.replace("[^a-zA-Z ]*".toRegex(), "")
                if (s.toString() != newStr) {
                    Toast.makeText(context,getString(R.string.characters_special_error_text),Toast.LENGTH_SHORT).show()
                    textInputBox.setText(newStr)
                    textInputBox.setSelection(textInputBox.text!!.length)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(tableCreateLayout)
        val alert = builder.create()
        alert.show()
        tableCreateBtn.setOnClickListener {
            if(textInputBox.text.toString().isNotEmpty()){
                val tableName = textInputBox.text.toString().trim()
                tableGenerator.generateTable(tableName)
                Toast.makeText(context,getString(R.string.table_create_success_text),Toast.LENGTH_SHORT).show()
                alert.dismiss()
                //displayTableList()
                val intent = Intent(context,CreateTableActivity::class.java)
                intent.putExtra("TABLE_NAME",tableName)
                startActivity(intent)
            }
            else{
                showAlert(context,getString(R.string.table_name_empty_error_text))
            }
        }
    }
}