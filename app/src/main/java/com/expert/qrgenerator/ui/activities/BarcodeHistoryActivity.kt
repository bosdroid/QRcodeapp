package com.expert.qrgenerator.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.QrCodeHistoryAdapter
import com.expert.qrgenerator.adapters.ViewPagerAdapter
import com.expert.qrgenerator.databinding.ActivityBarcodeHistoryBinding
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.utils.Constants
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BarcodeHistoryActivity : BaseActivity() {

    // ViewBinding for the activity layout
    private lateinit var binding: ActivityBarcodeHistoryBinding

    // List to hold QR Code history items
    private var qrCodeHistoryList = mutableListOf<CodeHistory>()

    // Adapter for displaying QR Code history
    private lateinit var adapter: QrCodeHistoryAdapter

    // ViewModel for handling data operations
    private val appViewModel: AppViewModel by viewModels()


    // Context of the activity, initialized using lazy delegation
    private val context: Context by lazy { this }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        logCustomEvent(eventName = "screen_history_opened")

        binding = ActivityBarcodeHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views and setup components
        initViews()
        setUpToolbar()
        getDisplayCreateHistory()
    }

    /**
     * Initialize any views or variables.
     */
    private fun initViews() {
        // This is where you can initialize any other views or components if needed
        // Set up the RecyclerView with LinearLayoutManager and adapter
        binding.qrCodeHistoryRecyclerview.layoutManager = LinearLayoutManager(context)
        binding.qrCodeHistoryRecyclerview.setHasFixedSize(true) // Improve performance with fixed-size
        adapter = QrCodeHistoryAdapter(context, qrCodeHistoryList as ArrayList<CodeHistory>)
        binding.qrCodeHistoryRecyclerview.adapter = adapter

        // Set up the click listener for RecyclerView items
        adapter.setOnClickListener(object : QrCodeHistoryAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                // Handle item click event
                val historyItem = qrCodeHistoryList[position]
                if(historyItem.type == "vcard"){
//                    val intent = Intent(context, UpdateVCardActivity::class.java)
//                    startActivity(intent)
                    Constants.isOpenVcardScreen = true
                    finish()
                }
                else{
                    val intent = Intent(context, CodeDetailActivity::class.java)
                    intent.putExtra("HISTORY_ITEM", historyItem)
                    startActivity(intent)
                }
            }
        })
    }


    /**
     * Set up the toolbar with title and home button.
     */
    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.qr_code_history)
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    /**
     * Fetches and displays QR Code history data.
     */
    private fun getDisplayCreateHistory() {
        // Show loading indicator
        BaseActivity.startLoading(context)

        // Observe the ViewModel's LiveData for QR Code history
        appViewModel.allCreateQRCodeHistory.observe(this@BarcodeHistoryActivity, Observer { list ->
            // Dismiss loading indicator
            dismiss()

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
