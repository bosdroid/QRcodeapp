package com.expert.qrgenerator.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.TablesDataAdapter
import com.expert.qrgenerator.databinding.FragmentScanBinding
import com.expert.qrgenerator.utils.TableGenerator
import com.expert.qrgenerator.ui.activities.TableViewActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScanFragment : Fragment(), TablesDataAdapter.OnItemClickListener {

    private lateinit var binding: FragmentScanBinding
    private lateinit var tableGenerator: TableGenerator
    private val tableList = mutableListOf<String>()
    private lateinit var adapter: TablesDataAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment and initialize views
        binding = FragmentScanBinding.inflate(inflater, container, false)
        initViews()
        displayTableList()
        return binding.root
    }

    /**
     * Initialize the views and set up the RecyclerView with its adapter.
     */
    private fun initViews() {
        tableGenerator = TableGenerator(requireActivity())

        // Set up RecyclerView
        binding.tablesDataRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            hasFixedSize() // Improve performance if RecyclerView size is fixed
        }

        adapter = TablesDataAdapter(tableList as ArrayList<String>)
        binding.tablesDataRecyclerview.adapter = adapter
        adapter.setOnItemClickListener(this)
    }

    /**
     * Fetch the list of tables from the TableGenerator and update the adapter.
     */
    private fun displayTableList() {
        val list = tableGenerator.getAllDatabaseTables()
        if (list.isNotEmpty()) {
            tableList.clear()
            tableList.addAll(list)
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * Handle item clicks in the RecyclerView.
     */
    override fun onItemClick(position: Int) {
        val table = tableList[position]
        val intent = Intent(requireActivity(), TableViewActivity::class.java).apply {
            putExtra("TABLE_NAME", table)
        }
        startActivity(intent)
    }
}
