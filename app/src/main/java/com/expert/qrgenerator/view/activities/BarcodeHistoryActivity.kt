package com.expert.qrgenerator.view.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.QrCodeHistoryAdapter
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.room.AppViewModel
import com.google.android.material.textview.MaterialTextView

class BarcodeHistoryActivity : BaseActivity() {

    private lateinit var context:Context
    private lateinit var toolbar: Toolbar
    private lateinit var qrCodeHistoryRecyclerView: RecyclerView
    private lateinit var emptyView:MaterialTextView
    private var qrCodeHistoryList = mutableListOf<CodeHistory>()
    private lateinit var adapter:QrCodeHistoryAdapter
    private lateinit var appViewModel: AppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_history)

        initViews()
        setUpToolbar()
        getDisplayHistory()
    }


    private fun initViews(){
        context = this
        appViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(this.application)
        ).get(AppViewModel::class.java)
        toolbar = findViewById(R.id.toolbar)
        emptyView = findViewById(R.id.emptyView)
        qrCodeHistoryRecyclerView = findViewById(R.id.qr_code_history_recyclerview)
        qrCodeHistoryRecyclerView.layoutManager = LinearLayoutManager(context)
        qrCodeHistoryRecyclerView.hasFixedSize()
        adapter = QrCodeHistoryAdapter(context, qrCodeHistoryList as ArrayList<CodeHistory>)
        qrCodeHistoryRecyclerView.adapter = adapter
        adapter.setOnClickListener(object : QrCodeHistoryAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
              val historyItem = qrCodeHistoryList[position]
//                showAlert(context,historyItem.toString())
                val intent = Intent(context,CodeDetailActivity::class.java)
                intent.putExtra("HISTORY_ITEM",historyItem)
                startActivity(intent)
            }
        })
    }

    private fun setUpToolbar(){
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.qr_code_history)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    private fun getDisplayHistory(){
        startLoading(context)
       appViewModel.getAllQRCodeHistory().observe(this, Observer { list ->
           dismiss()
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