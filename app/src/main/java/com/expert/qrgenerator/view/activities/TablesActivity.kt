package com.expert.qrgenerator.view.activities

import android.app.Activity
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
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.TablesAdapter
import com.expert.qrgenerator.model.Table
import com.expert.qrgenerator.utils.AppSettings
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.FileUtil
import com.expert.qrgenerator.utils.TableGenerator
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.opencsv.CSVReader
import java.io.FileReader
import java.io.IOException

class TablesActivity : BaseActivity(), TablesAdapter.OnItemClickListener, View.OnClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var tableRecyclerView: RecyclerView
    private lateinit var tableGenerator: TableGenerator
    private var tableList = mutableListOf<String>()
    private lateinit var adapter: TablesAdapter
    private lateinit var appSettings: AppSettings
    private lateinit var importCsvView: AppCompatImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tables)

        initViews()
        setUpToolbar()

    }


    private fun initViews() {
        context = this
        appSettings = AppSettings(context)
        tableGenerator = TableGenerator(context)
        toolbar = findViewById(R.id.toolbar)
        tableRecyclerView = findViewById(R.id.tables_recycler_view)
        importCsvView = findViewById(R.id.import_csv_view)
        importCsvView.setOnClickListener(this)
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
        val intent = Intent(context, CreateTableActivity::class.java)
        intent.putExtra("TABLE_NAME", table)
        startActivity(intent)
    }

    override fun onAddItemClick(position: Int) {
          if (Constants.userData != null){
        addTableDialog()
         }
        else{
            //showAlert(context,"You can not create dynamic table without account login!")
            MaterialAlertDialogBuilder(context)
                    .setTitle(getString(R.string.alert_text))
                    .setMessage(getString(R.string.login_error_text))
                    .setNegativeButton(getString(R.string.later_text)){dialog,which->
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.login_text)){dialog,which->
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

    private fun addTableDialog() {
        val tableCreateLayout =
            LayoutInflater.from(context).inflate(R.layout.add_table_layout, null)
        val textInputBox =
            tableCreateLayout.findViewById<TextInputEditText>(R.id.add_table_text_input_field)
        val tableCreateBtn = tableCreateLayout.findViewById<MaterialButton>(R.id.add_table_btn)
        textInputBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var newStr = s.toString()
                newStr = newStr.replace("[^a-zA-Z ]*".toRegex(), "")
                if (s.toString() != newStr) {
                    Toast.makeText(
                        context,
                        getString(R.string.characters_special_error_text),
                        Toast.LENGTH_SHORT
                    ).show()
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
            if (textInputBox.text.toString().isNotEmpty()) {
                val tableName = textInputBox.text.toString().trim()
                tableGenerator.generateTable(tableName)
                Toast.makeText(
                    context,
                    getString(R.string.table_create_success_text),
                    Toast.LENGTH_SHORT
                ).show()
                alert.dismiss()
                //displayTableList()
                val intent = Intent(context, CreateTableActivity::class.java)
                intent.putExtra("TABLE_NAME", tableName)
                startActivity(intent)
            } else {
                showAlert(context, getString(R.string.table_name_empty_error_text))
            }
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.import_csv_view -> {
                if (Constants.userData != null){
                    importCsv()
                }
                else{
                    //showAlert(context,"You can not create dynamic table without account login!")
                    MaterialAlertDialogBuilder(context)
                        .setTitle(getString(R.string.alert_text))
                        .setMessage(getString(R.string.login_error_text))
                        .setNegativeButton(getString(R.string.later_text)){dialog,which->
                            dialog.dismiss()
                        }
                        .setPositiveButton(getString(R.string.login_text)){dialog,which->
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
            else -> {

            }
        }
    }

    private fun importCsv() {
        openFilePicker()
    }


    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "text/*"
        fileResultLauncher.launch(intent)
    }

    private var fileResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val filePath = result.data!!.data
                try {
                    val file = FileUtil.from(context, filePath!!)
                    val ext = file.name.substring(file.name.lastIndexOf(".")+1)
                    val fileName = file.name.substring(0,file.name.lastIndexOf("."))
                    if (ext != "csv") {
                        showAlert(context, getString(R.string.csv_file_chooser_error_message_text))
                    } else {
                        try {
                            val reader = CSVReader(FileReader(file))
                            var nextLine: Array<String>
                            var counter = 0
                            val columnsList = mutableListOf<String>()
                            var tableData = mutableListOf<Pair<String, String>>()
                            val listRecord = mutableListOf<List<Pair<String,String>>>()
                            val tableName =
                                fileName.replace(" ", "_").replace("[-+.^:,]", "").trim()
                            startLoading(context)
                            while (reader.readNext().also { nextLine = it } != null) {
                                // nextLine[] is an array of values from the line
                                if (counter == 0) {
                                    for (i in nextLine.indices) {
                                        columnsList.add(nextLine[i].replace(" ","_").trim())
                                    }
                                    counter += 1
                                    continue
                                }
                                if (nextLine.isNotEmpty() && columnsList.size == nextLine.size) {
                                    for (i in nextLine.indices) {
                                        tableData.add(Pair(columnsList[i], nextLine[i]))
                                    }
                                    listRecord.add(tableData)
                                    tableData = mutableListOf()
                                    counter += 1
                                } else {
                                    break
                                }
                                if (reader.readNext() == null) {
                                    break
                                }
                            }

                            if (tableName.isNotEmpty() && listRecord.isNotEmpty()) {
                                val isFound = tableGenerator.tableExists(tableName)
                                if (isFound) {
                                    dismiss()
                                    showAlert(
                                        context,
                                        getString(R.string.table_already_exist_message)
                                    )
                                } else {

                                    tableGenerator.createTable(
                                        tableName,
                                        columnsList as ArrayList<String>
                                    )

                                    Handler(Looper.myLooper()!!).postDelayed({

                                        val isExist = tableGenerator.tableExists(tableName)
                                        if (isExist) {
                                            displayTableList()
                                            for (j in 0 until listRecord.size){
                                                tableGenerator.insertData(tableName, listRecord[j])
                                            }
                                            dismiss()
                                            showAlert(
                                                context,
                                                getString(R.string.table_created_success_message)
                                            )
                                        } else {
                                            dismiss()
                                            showAlert(
                                                context,
                                                getString(R.string.table_created_failed_message)
                                            )
                                        }
                                    }, 5000)
                                }
                            } else {
                                dismiss()
                                showAlert(
                                    context,
                                    getString(R.string.table_csv_import_error_message)
                                )
                            }

                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }

                } catch (e: Exception) {
                    showAlert(
                        context,
                        getString(R.string.table_csv_import_error_message)
                    )
                    e.printStackTrace()
                }

            }
        }
}