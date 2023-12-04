package com.expert.qrgenerator.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.QrCodeHistoryAdapter
import com.expert.qrgenerator.databinding.FragmentCreateBinding
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.ui.activities.BaseActivity
import com.expert.qrgenerator.ui.activities.CodeDetailActivity
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateFragment : Fragment() {

    private lateinit var binding: FragmentCreateBinding
    private var qrCodeHistoryList = mutableListOf<CodeHistory>()
    private lateinit var adapter: QrCodeHistoryAdapter
    private val appViewModel: AppViewModel by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCreateBinding.inflate(layoutInflater, container, false)
        initViews()
        getDisplayCreateHistory()
        return binding.root
    }

    private fun initViews(){

        binding.qrCodeHistoryRecyclerview.layoutManager = LinearLayoutManager(context)
        binding.qrCodeHistoryRecyclerview.hasFixedSize()
        adapter = QrCodeHistoryAdapter(requireActivity(), qrCodeHistoryList as ArrayList<CodeHistory>)
        binding.qrCodeHistoryRecyclerview.adapter = adapter
        adapter.setOnClickListener(object : QrCodeHistoryAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                val historyItem = qrCodeHistoryList[position]
                val intent = Intent(context, CodeDetailActivity::class.java)
                intent.putExtra("HISTORY_ITEM",historyItem)
                requireActivity().startActivity(intent)
            }
        })
    }

    private fun getDisplayCreateHistory(){
        BaseActivity.startLoading(requireActivity())
        appViewModel.getAllCreateQRCodeHistory().observe(requireActivity(), Observer { list ->
            BaseActivity.dismiss()
            if (list.isNotEmpty()){
                qrCodeHistoryList.clear()
                binding.emptyView.visibility = View.GONE
                binding.qrCodeHistoryRecyclerview.visibility = View.VISIBLE
                qrCodeHistoryList.addAll(list)
                adapter.notifyDataSetChanged()
            }
            else
            {
                binding.qrCodeHistoryRecyclerview.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
            }
        })
    }

}