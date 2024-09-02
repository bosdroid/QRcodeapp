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

    // View Binding for the Fragment
    private lateinit var binding: FragmentCreateBinding

    // List to hold QR Code history items
    private var qrCodeHistoryList = mutableListOf<CodeHistory>()

    // Adapter for displaying QR Code history
    private lateinit var adapter: QrCodeHistoryAdapter

    // ViewModel for handling data operations
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment and initialize views
        binding = FragmentCreateBinding.inflate(inflater, container, false)
        initViews()
        getDisplayCreateHistory()
        return binding.root
    }

    /**
     * Initializes the views and sets up the RecyclerView with its adapter.
     */
    private fun initViews() {
        // Set up the RecyclerView with LinearLayoutManager and adapter
        binding.qrCodeHistoryRecyclerview.layoutManager = LinearLayoutManager(context)
        binding.qrCodeHistoryRecyclerview.setHasFixedSize(true) // Improve performance with fixed-size
        adapter = QrCodeHistoryAdapter(requireActivity(), qrCodeHistoryList as ArrayList<CodeHistory>)
        binding.qrCodeHistoryRecyclerview.adapter = adapter

        // Set up the click listener for RecyclerView items
        adapter.setOnClickListener(object : QrCodeHistoryAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                // Handle item click event
                val historyItem = qrCodeHistoryList[position]
                val intent = Intent(context, CodeDetailActivity::class.java)
                intent.putExtra("HISTORY_ITEM", historyItem)
                requireActivity().startActivity(intent)
            }
        })
    }

    /**
     * Fetches and displays QR Code history data.
     */
    private fun getDisplayCreateHistory() {
        // Show loading indicator
        BaseActivity.startLoading(requireActivity())

        // Observe the ViewModel's LiveData for QR Code history
        appViewModel.allCreateQRCodeHistory.observe(viewLifecycleOwner, Observer { list ->
            // Dismiss loading indicator
            BaseActivity.dismiss()

            // Update the UI based on the data received
            if (list.isNotEmpty()) {
                qrCodeHistoryList.clear()
                qrCodeHistoryList.addAll(list)
                adapter.notifyDataSetChanged()

                // Show RecyclerView and hide empty view
                binding.qrCodeHistoryRecyclerview.visibility = View.VISIBLE
                binding.emptyView.visibility = View.GONE
            } else {
                // Show empty view and hide RecyclerView
                binding.qrCodeHistoryRecyclerview.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
            }
        })
    }
}
