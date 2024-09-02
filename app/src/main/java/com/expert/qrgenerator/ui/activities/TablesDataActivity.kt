package com.expert.qrgenerator.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.TablesDataAdapter
import com.expert.qrgenerator.databinding.ActivityTablesDataBinding
import com.expert.qrgenerator.utils.TableGenerator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TablesDataActivity : BaseActivity(), TablesDataAdapter.OnItemClickListener {

    private lateinit var binding: ActivityTablesDataBinding
    private lateinit var tableGenerator: TableGenerator
    private var tableList = mutableListOf<String>()
    private lateinit var adapter: TablesDataAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize view binding
        binding = ActivityTablesDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views and set up toolbar
        initViews()
        setUpToolbar()

        // Display list of tables
        displayTableList()
    }

    private fun initViews() {
        // Initialize table generator
        tableGenerator = TableGenerator(this)

        // Set up RecyclerView
        binding.tablesDataRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@TablesDataActivity)
            setHasFixedSize(true)
            adapter = TablesDataAdapter(tableList as ArrayList<String>).apply {
                setOnItemClickListener(this@TablesDataActivity)
            }
        }
    }

    private fun setUpToolbar() {
        // Set up toolbar with title and back button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.tables)
            setDisplayHomeAsUpEnabled(true)
            binding.toolbar.setTitleTextColor(ContextCompat.getColor(this@TablesDataActivity, R.color.black))
        }
    }

    private fun displayTableList() {
        // Fetch and display table list from database
        val list = tableGenerator.getAllDatabaseTables()
        if (list.isNotEmpty()) {
            tableList.clear()
            tableList.addAll(list)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle toolbar back button click
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(position: Int) {
        // Handle item click event
        val table = tableList[position]
        val intent = Intent(this, TableViewActivity::class.java).apply {
            putExtra("TABLE_NAME", table)
        }
        startActivity(intent)
    }
}
