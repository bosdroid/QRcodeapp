package com.expert.qrgenerator.view.activities

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.TableDetailAdapter
import com.expert.qrgenerator.model.TableObject
import com.expert.qrgenerator.utils.TableGenerator
import com.google.android.material.textview.MaterialTextView
import java.util.*
import kotlin.collections.ArrayList


class TableViewActivity : BaseActivity(),TableDetailAdapter.OnItemClickListener {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var tableGenerator: TableGenerator
    private lateinit var tableDetailRecyclerView: RecyclerView
    private var tableName: String = ""
    private var dataList = mutableListOf<TableObject>()
    private lateinit var adapter: TableDetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table_view)

        initViews()
        setUpToolbar()
        getTableData()
    }

    private fun initViews() {
        context = this
        tableGenerator = TableGenerator(context)
        toolbar = findViewById(R.id.toolbar)
        if (intent != null && intent.hasExtra("TABLE_NAME")) {
            tableName = intent.getStringExtra("TABLE_NAME")!!
        }
        tableDetailRecyclerView = findViewById(R.id.tables_detail_recyclerview)
        tableDetailRecyclerView.layoutManager = LinearLayoutManager(context)
        tableDetailRecyclerView.hasFixedSize()
        adapter = TableDetailAdapter(context, dataList as ArrayList<TableObject>)
        tableDetailRecyclerView.adapter = adapter
        adapter.setOnItemClickListener(this)
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

    private fun getTableData(){
        dataList.addAll(tableGenerator.getTableDate(tableName))
        adapter.notifyDataSetChanged()
    }

    override fun onItemClick(position: Int) {
          val tableObject = dataList[position]
          showAlert(context,tableObject.toString())
    }

}