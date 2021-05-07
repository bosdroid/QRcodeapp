package com.expert.qrgenerator.view.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.QrCodeHistoryAdapter
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.view.activities.BaseActivity
import com.expert.qrgenerator.view.activities.CodeDetailActivity
import com.google.android.material.textview.MaterialTextView


class ScanFragment : Fragment() {

    private lateinit var qrCodeHistoryRecyclerView: RecyclerView
    private lateinit var emptyView: MaterialTextView
    private var qrCodeHistoryList = mutableListOf<CodeHistory>()
    private lateinit var adapter: QrCodeHistoryAdapter
    private lateinit var appViewModel: AppViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(AppViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_scan, container, false)

        initViews(v)
        getDisplayScanHistory()
        return v
    }

    private fun initViews(view:View){
        emptyView = view.findViewById(R.id.emptyView)
        qrCodeHistoryRecyclerView = view.findViewById(R.id.qr_code_history_recyclerview)
        qrCodeHistoryRecyclerView.layoutManager = LinearLayoutManager(context)
        qrCodeHistoryRecyclerView.hasFixedSize()
        adapter = QrCodeHistoryAdapter(requireActivity(), qrCodeHistoryList as ArrayList<CodeHistory>)
        qrCodeHistoryRecyclerView.adapter = adapter
        adapter.setOnClickListener(object : QrCodeHistoryAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                val historyItem = qrCodeHistoryList[position]
//                showAlert(context,historyItem.toString())
                val intent = Intent(context, CodeDetailActivity::class.java)
                intent.putExtra("HISTORY_ITEM",historyItem)
                startActivity(intent)
            }
        })
    }

    private fun getDisplayScanHistory(){
        BaseActivity.startLoading(requireActivity())
        appViewModel.getAllScanQRCodeHistory().observe(this, Observer { list ->
            BaseActivity.dismiss()
            if (list.isNotEmpty()){
                qrCodeHistoryList.clear()
                emptyView.visibility = View.GONE
                qrCodeHistoryRecyclerView.visibility = View.VISIBLE
                qrCodeHistoryList.addAll(list)
                adapter.notifyDataSetChanged()
            }
            else
            {
                qrCodeHistoryRecyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            }
        })
    }
}