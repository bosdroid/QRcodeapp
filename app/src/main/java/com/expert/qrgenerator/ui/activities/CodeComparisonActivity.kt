package com.expert.qrgenerator.ui.activities

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.QrCodeComparisonAdapter
import com.expert.qrgenerator.databinding.ActivityCodeComparisonBinding
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.room.AppViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CodeComparisonActivity : BaseActivity() {

    private lateinit var binding: ActivityCodeComparisonBinding

    private lateinit var context: Context

    // ViewModel for handling data operations
    private val appViewModel: AppViewModel by viewModels()

    // List to hold QR Code history items
    private var qrCodeList = mutableListOf<CodeHistory>()

    // Adapter for displaying QR Code history
    private lateinit var adapter: QrCodeComparisonAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCodeComparisonBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this
        setUpToolbar()
        initViews()


        getDisplayTrackableCodes()
    }

    private fun initViews() {
        // Set up the RecyclerView with LinearLayoutManager and adapter
        binding.qrCodesRecyclerview.layoutManager = LinearLayoutManager(context)
        binding.qrCodesRecyclerview.setHasFixedSize(true) // Improve performance with fixed-size
        adapter = QrCodeComparisonAdapter(context, qrCodeList as ArrayList<CodeHistory>)
        binding.qrCodesRecyclerview.adapter = adapter

        adapter.setOnCheckboxChangeListener(object : QrCodeComparisonAdapter.OnCheckboxChangeListener{
            override fun onCheckboxChanged(position: Int, isChecked: Boolean) {

            }

        })
    }

    /**
     * Fetches and displays QR Code only trackable type.
     */
    private fun getDisplayTrackableCodes() {
        // Show loading indicator
        startLoading(context)

        // Observe the ViewModel's LiveData for QR Code history
        appViewModel.allTrackableQRCodes.observe(this@CodeComparisonActivity) { list ->
            // Dismiss loading indicator
            dismiss()

            // Update the UI based on the data received
            if (list.isNotEmpty()) {
                qrCodeList.clear()
                qrCodeList.addAll(list)
                adapter.notifyDataSetChanged()

                // Show RecyclerView and hide empty view
                binding.qrCodesRecyclerview.visibility = View.VISIBLE
//                binding.emptyView.visibility = View.GONE
            } else {
                // Show empty view and hide RecyclerView
                binding.qrCodesRecyclerview.visibility = View.GONE
//                binding.emptyView.visibility = View.VISIBLE
            }
        }
    }


    // Sets up the ActionBar/Toolbar for the activity
    private fun setUpToolbar() {
        // Set the toolbar as the ActionBar
        setSupportActionBar(binding.toolbar)

        // Check if the ActionBar is not null
        supportActionBar?.let { actionBar ->
            // Set the title for the ActionBar
            actionBar.title = getString(R.string.code_comparison_text)

            // Enable the Up button to navigate back
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        // Set the title text color of the Toolbar
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    /**
     * Handles the item selection in the options menu.
     *
     * @param item The menu item that was selected.
     * @return True if the event was handled, false otherwise.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Check if the selected item is the home (back arrow) button
        return if (item.itemId == android.R.id.home) {
            // Handle the back arrow click event
            onBackPressed()
            true
        } else {
            // Pass the event to the superclass for handling other items
            super.onOptionsItemSelected(item)
        }
    }
}