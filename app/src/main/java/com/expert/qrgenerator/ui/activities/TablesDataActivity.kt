package com.expert.qrgenerator.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.TablesDataAdapter
import com.expert.qrgenerator.databinding.ActivityTablesDataBinding
import com.expert.qrgenerator.utils.TableGenerator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TablesDataActivity : BaseActivity(),TablesDataAdapter.OnItemClickListener {

    private lateinit var binding:ActivityTablesDataBinding
    private lateinit var context: Context
    private lateinit var tableGenerator: TableGenerator
    private var tableList = mutableListOf<String>()
    private lateinit var adapter: TablesDataAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTablesDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setUpToolbar()
        displayTableList()

    }

    private fun initViews() {
        context = this
        tableGenerator = TableGenerator(context)

        binding.tablesDataRecyclerview.layoutManager = LinearLayoutManager(context)
        binding.tablesDataRecyclerview.hasFixedSize()
        adapter = TablesDataAdapter(context, tableList as ArrayList<String>)
        binding.tablesDataRecyclerview.adapter = adapter
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
        val intent = Intent(context,TableViewActivity::class.java)
        intent.putExtra("TABLE_NAME",table)
        startActivity(intent)
    }

}