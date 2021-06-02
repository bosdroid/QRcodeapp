package com.expert.qrgenerator.view.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.QrCodeHistoryAdapter
import com.expert.qrgenerator.adapters.TablesAdapter
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.TableGenerator
import com.expert.qrgenerator.view.activities.BaseActivity
import com.expert.qrgenerator.view.activities.CodeDetailActivity
import com.expert.qrgenerator.view.activities.CreateTableActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView


class ScanFragment : Fragment(),TablesAdapter.OnItemClickListener {

//    private lateinit var qrCodeHistoryRecyclerView: RecyclerView
//    private lateinit var emptyView: MaterialTextView
//    private var qrCodeHistoryList = mutableListOf<CodeHistory>()
//    private lateinit var adapter: QrCodeHistoryAdapter
//    private lateinit var appViewModel: AppViewModel
    private lateinit var tableRecyclerView: RecyclerView
    private lateinit var tableGenerator: TableGenerator
    private var tableList = mutableListOf<String>()
    private lateinit var adapter: TablesAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
//        appViewModel = ViewModelProvider(
//            this,
//            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
//        ).get(AppViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_scan, container, false)

        initViews(v)
//        getDisplayScanHistory()
        displayTableList()
        return v
    }

    private fun initViews(view:View){
        tableGenerator = TableGenerator(requireActivity())
        tableRecyclerView = view.findViewById(R.id.tables_recycler_view)
        tableRecyclerView.layoutManager = LinearLayoutManager(context)
        tableRecyclerView.hasFixedSize()
        adapter = TablesAdapter(requireActivity(), tableList as ArrayList<String>)
        tableRecyclerView.adapter = adapter
//        emptyView = view.findViewById(R.id.emptyView)
//        qrCodeHistoryRecyclerView = view.findViewById(R.id.qr_code_history_recyclerview)
//        qrCodeHistoryRecyclerView.layoutManager = LinearLayoutManager(context)
//        qrCodeHistoryRecyclerView.hasFixedSize()
//        adapter = QrCodeHistoryAdapter(requireActivity(), qrCodeHistoryList as ArrayList<CodeHistory>)
//        qrCodeHistoryRecyclerView.adapter = adapter
//        adapter.setOnClickListener(object : QrCodeHistoryAdapter.OnItemClickListener{
//            override fun onItemClick(position: Int) {
//                val historyItem = qrCodeHistoryList[position]
////                showAlert(context,historyItem.toString())
//                val intent = Intent(context, CodeDetailActivity::class.java)
//                intent.putExtra("HISTORY_ITEM",historyItem)
//                startActivity(intent)
//            }
//        })
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
        val intent = Intent(requireActivity(), CreateTableActivity::class.java)
        intent.putExtra("TABLE_NAME",table)
        requireActivity().startActivity(intent)
    }

    override fun onAddItemClick(position: Int) {
        if (Constants.userData != null){
            addTableDialog()
        }
        else{
            BaseActivity.showAlert(requireActivity(),"You can not create dynamic table without account login!")
        }

    }


    private fun addTableDialog(){
        val tableCreateLayout = LayoutInflater.from(context).inflate(R.layout.add_table_layout,null)
        val textInputBox = tableCreateLayout.findViewById<TextInputEditText>(R.id.add_table_text_input_field)
        val tableCreateBtn = tableCreateLayout.findViewById<MaterialButton>(R.id.add_table_btn)
        val builder = MaterialAlertDialogBuilder(requireActivity())
        builder.setView(tableCreateLayout)
        val alert = builder.create()
        alert.show()
        tableCreateBtn.setOnClickListener {
            if(textInputBox.text.toString().isNotEmpty()){
                tableGenerator.generateTable(textInputBox.text.toString().trim())
                Toast.makeText(requireActivity(),"Table has been created successfully!", Toast.LENGTH_SHORT).show()
                alert.dismiss()
                displayTableList()
            }
            else{
                BaseActivity.showAlert(requireActivity(), "Please enter the table name!")
            }
        }
    }
}