package com.expert.qrgenerator.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.QrCodeHistoryAdapter
import com.expert.qrgenerator.databinding.ActivityBarcodeHistoryBinding
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.utils.Constants
import com.google.firebase.analytics.FirebaseAnalytics
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

//        logCustomEvent(eventName = "screen_history_opened")

        binding = ActivityBarcodeHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views and setup components
        initViews()
        setUpToolbar()

    }

    override fun onResume() {
        super.onResume()
        logEvent()
        getDisplayCreateHistory()
    }

    private fun logEvent() {
        val mainAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        // Log the custom event
        mainAnalytics.logEvent("screen_history_opened", bundle)
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
        startLoading(context)

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.create->{
                startActivity(Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                true
            }
            R.id.history->{
                startActivity(Intent(context, BarcodeHistoryActivity::class.java))
                true
            }
            R.id.compare->{
                startActivity(Intent(context, CodeComparisonActivity::class.java))
                true
            }
            else -> {
                // Pass the event to the superclass to handle other menu items
                super.onOptionsItemSelected(item)
            }
        }
    }

}
