package com.expert.qrgenerator.ui.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.QrCodeComparisonAdapter
import com.expert.qrgenerator.databinding.ActivityCodeComparisonBinding
import com.expert.qrgenerator.databinding.ConversionParametersDialogLayoutBinding
import com.expert.qrgenerator.interfaces.TrackableScansCallback
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.TrackableScan
import com.expert.qrgenerator.repository.DataRepository
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.viewmodel.CodeDetailViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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

    private var selectedItems = mutableListOf<CodeHistory>()

    private val viewModel: CodeDetailViewModel by viewModels()

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
            override fun onClickEdit(position: Int) {
                openConversionParametersDialog(position)
            }

            override fun onCheckboxChanged(position: Int, isChecked: Boolean,item:CodeHistory) {
                if (isChecked) {
                    selectedItems.add(item) // Add to selected items
                } else {
                    selectedItems.remove(item) // Remove from selected items
                }
                Log.d("TESTSELECTEDITEMS",selectedItems.toString())
            }

        })

        binding.compareImg.setOnClickListener {

          if(selectedItems.size > 0){
              if (selectedItems.size > 1){
                  val finalPrompt = buildComparisonPrompt(selectedItems)
                  startLoading(context)
                  lifecycleScope.launch {
                      viewModel.callAiRecommendationRequest(finalPrompt) { result ->
                          dismiss()
                          showAlert(context,result)
                      }
                  }
              }
              else{
                  showAlert(context,getString(R.string.qr_codes_selected_size_error))
              }
          }
          else{
              showAlert(context,getString(R.string.qr_codes_empty_list_error))
          }
        }
    }

    private fun buildComparisonPrompt(qrCodes: List<CodeHistory>): String {
        val promptBuilder = StringBuilder()
        promptBuilder.append("Compare the following QR codes and provide insights on which performs better in terms of conversions and profits (limit response to 500 characters). Return the result as a readable list:\n\n")

        qrCodes.filter { it.totalScans >= 10 }
            .forEachIndexed { index, qrCode ->
                promptBuilder.append("QR Code ${index + 1}: ID: ${qrCode.id}, Scans: ${qrCode.totalScans}")

                if (qrCode.scanDateList.isNotEmpty()) {
                    promptBuilder.append(", Scan Dates: ${qrCode.scanDateList.joinToString(", ")}")
                }
                if (qrCode.conversion != 0) {
                    promptBuilder.append(", Conversions: ${qrCode.conversion}")
                }
                if (qrCode.revenue != 0) {
                    promptBuilder.append(", Revenue: ${qrCode.revenue}")
                }
                if (qrCode.expenses != 0) {
                    promptBuilder.append(", Expenses: ${qrCode.expenses}")
                }

                promptBuilder.append("\n\n")
            }

        promptBuilder.append("Focus on key performance differences and brief recommendations.")
        return promptBuilder.toString()
    }



    private fun openConversionParametersDialog(position: Int) {
        val dialogBinding = ConversionParametersDialogLayoutBinding.inflate(LayoutInflater.from(context))

        val builder = MaterialAlertDialogBuilder(context)
            .setView(dialogBinding.root)
            .setCancelable(false)

        val alertdialog = builder.create()
        alertdialog.show()

        dialogBinding.apply {
             saveBtn.setOnClickListener {
                 val conversion = conversionInputField.text.toString()
                 val revenue = revenueInputField.text.toString()
                 val expenses = expensesInputField.text.toString()

                 if(conversion.isNotEmpty()){
                     qrCodeList[position].conversion = conversion.toInt()
                 }
                 if(revenue.isNotEmpty()){
                     qrCodeList[position].revenue = revenue.toInt()
                 }
                 if(expenses.isNotEmpty()){
                     qrCodeList[position].expenses = expenses.toInt()
                 }
                 adapter.notifyItemChanged(position)
                 alertdialog.dismiss()
             }

            cancelBtn.setOnClickListener {
                alertdialog.dismiss()
            }
        }
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
                getQrScansList()
            } else {
                // Show empty view and hide RecyclerView
                binding.qrCodesRecyclerview.visibility = View.GONE
//                binding.emptyView.visibility = View.VISIBLE
            }
        }
    }

    private fun getQrScansList() {
        startLoading(context)
        for (i in qrCodeList.indices) {
            val item = qrCodeList[i]
            DataRepository.getAllScanHistory(item.qrId, object : TrackableScansCallback {
                override fun onTrackableScansLoaded(trackableScans: List<TrackableScan>) {
                    qrCodeList[i].totalScans = trackableScans.size

                    // Map the trackableScans to a list of formatted date-time strings
                    val formattedDates = trackableScans.mapNotNull { scan ->
                        scan.timestamp?.let { getDateTimeFromTimeStamp1(it) }
                    }

                    qrCodeList[i].scanDateList = formattedDates
                    adapter.notifyItemChanged(i)
                }

                override fun onTrackableScansError() {
                    // Handle error
                }
            })
        }
        dismiss()
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