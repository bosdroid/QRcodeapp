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
class ScanFragment : Fragment(),TablesDataAdapter.OnItemClickListener {

    private lateinit var binding: FragmentScanBinding

    private lateinit var tableGenerator: TableGenerator
    private var tableList = mutableListOf<String>()
    private lateinit var adapter: TablesDataAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentScanBinding.inflate(layoutInflater, container, false)

        initViews()
//        getDisplayScanHistory()
        displayTableList()
        return binding.root
    }

    private fun initViews(){
        tableGenerator = TableGenerator(requireActivity())
        binding.tablesDataRecyclerview.layoutManager = LinearLayoutManager(context)
        binding.tablesDataRecyclerview.hasFixedSize()
        adapter = TablesDataAdapter(requireActivity(), tableList as ArrayList<String>)
        binding.tablesDataRecyclerview.adapter = adapter

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

//    private fun getDisplayScanHistory(){
//        BaseActivity.startLoading(requireActivity())
//        appViewModel.getAllScanQRCodeHistory().observe(this, Observer { list ->
//            BaseActivity.dismiss()
//            if (list.isNotEmpty()){
//                qrCodeHistoryList.clear()
//                emptyView.visibility = View.GONE
//                qrCodeHistoryRecyclerView.visibility = View.VISIBLE
//                qrCodeHistoryList.addAll(list)
//                adapter.notifyDataSetChanged()
//            }
//            else
//            {
//                qrCodeHistoryRecyclerView.visibility = View.GONE
//                emptyView.visibility = View.VISIBLE
//            }
//        })
//    }

    override fun onItemClick(position: Int) {
        val table = tableList[position]
        val intent = Intent(requireActivity(), TableViewActivity::class.java)
        intent.putExtra("TABLE_NAME",table)
        requireActivity().startActivity(intent)
    }

}