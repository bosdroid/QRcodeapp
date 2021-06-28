package com.expert.qrgenerator.view.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.TablesAdapter
import com.expert.qrgenerator.model.Table
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.TableGenerator
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class TablesActivity : BaseActivity(),TablesAdapter.OnItemClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var tableRecyclerView: RecyclerView
    private lateinit var tableGenerator: TableGenerator
    private var tableList = mutableListOf<String>()
    private lateinit var adapter: TablesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tables)

        initViews()
        setUpToolbar()

    }


    private fun initViews() {
        context = this
        tableGenerator = TableGenerator(context)
        toolbar = findViewById(R.id.toolbar)
        tableRecyclerView = findViewById(R.id.tables_recycler_view)
        tableRecyclerView.layoutManager = LinearLayoutManager(context)
        tableRecyclerView.hasFixedSize()
        adapter = TablesAdapter(context, tableList as ArrayList<String>)
        tableRecyclerView.adapter = adapter
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.tables)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
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
        if (Constants.userData != null){
            addTableDialog()
        }
        else{
            //showAlert(context,"You can not create dynamic table without account login!")
            MaterialAlertDialogBuilder(context)
                    .setTitle("Alert!")
                    .setMessage("You can not create dynamic table without account login!")
                    .setNegativeButton("LATER"){dialog,which->
                        dialog.dismiss()
                    }
                    .setPositiveButton("LOGIN"){dialog,which->
                        dialog.dismiss()
                        val intent = Intent(context,MainActivity::class.java)
                        intent.putExtra("REQUEST","login")
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }
                    .create().show()
        }

    }

    override fun onResume() {
        super.onResume()
        displayTableList()
    }

    private fun addTableDialog(){
        val tableCreateLayout = LayoutInflater.from(context).inflate(R.layout.add_table_layout,null)
        val textInputBox = tableCreateLayout.findViewById<TextInputEditText>(R.id.add_table_text_input_field)
        val tableCreateBtn = tableCreateLayout.findViewById<MaterialButton>(R.id.add_table_btn)
        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(tableCreateLayout)
        val alert = builder.create()
        alert.show()
        tableCreateBtn.setOnClickListener {
            if(textInputBox.text.toString().isNotEmpty()){
                val tableName = textInputBox.text.toString().trim()
                tableGenerator.generateTable(tableName)
                Toast.makeText(context,"Table has been created successfully!",Toast.LENGTH_SHORT).show()
                alert.dismiss()
                //displayTableList()
                val intent = Intent(context,CreateTableActivity::class.java)
                intent.putExtra("TABLE_NAME",tableName)
                startActivity(intent)
            }
            else{
                showAlert(context,"Please enter the table name!")
            }
        }
    }
}