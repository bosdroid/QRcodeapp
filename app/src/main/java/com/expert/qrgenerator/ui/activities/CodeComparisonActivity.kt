package com.expert.qrgenerator.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatCheckBox
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
import com.expert.qrgenerator.utils.Constants
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

    private var from = "ai_comparison"

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

            override fun onCheckboxChanged(position: Int, isChecked: Boolean,item:CodeHistory,buttonView:AppCompatCheckBox) {
                if (isChecked) {
                    if(from.isNotEmpty() && from != "ai_comparison" && item.totalScans == 0){
                        showAlert(context,"You can only select QR codes that have a scan count of at least 1!")
                        buttonView.isChecked = false
                    }
                    else if(from.isNotEmpty() && from != "ai_comparison" && selectedItems.size >=2){
                        showAlert(context,"You can only select 2 qr codes for comparison!")
                        buttonView.isChecked = false
                    }

                    else
                    {
                        selectedItems.add(item) // Add to selected items
                    }

                } else {
                    selectedItems.remove(item) // Remove from selected items
                }
                Log.d("TESTSELECTEDITEMS",selectedItems.toString())
            }

        })

        binding.aiAnalyzerBtn.setOnClickListener {
           if(from.isNotEmpty() && from != "ai_comparison"){
               val resultIntent = Intent()
               resultIntent.putExtra("SELECTED_QR_CODES", selectedItems as ArrayList<CodeHistory>)
               setResult(RESULT_OK, resultIntent)
               finish() // Close the second activity
               return@setOnClickListener
           }
            logCustomEvent(context,"comparison_screen","event","analyze code comparison")
          if(selectedItems.size > 0){
              if (selectedItems.size > 1){
                 if(hasInsufficientScans(selectedItems)){
                     showAlert(context,"AI comparison not start if any selected Qr code Scans less then 10!")
                 }else{
                     val finalPrompt = buildComparisonPrompt(selectedItems)
                     startLoading(context)
                     lifecycleScope.launch {
                         viewModel.callAiRecommendationRequest(finalPrompt) { result ->
                             dismiss()
                             showAlert(context,result)
                         }
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

        if (intent != null && intent.hasExtra("FROM")){
            from = intent.getStringExtra("FROM") as String
            binding.aiAnalyzerBtn.text = getString(R.string.done)
        }
    }

    override fun onResume() {
        super.onResume()
        logCustomEvent(context,"comparison_screen_open")
    }

    private fun hasInsufficientScans(qrCodes: List<CodeHistory>): Boolean {
        return qrCodes.any { it.totalScans < 10 }
    }

    private fun buildComparisonPrompt(qrCodes: List<CodeHistory>): String {
        // Build the message for each QR code
        val messageLines = mutableListOf<String>()
        qrCodes.filter { it.totalScans >= 10 }
            .forEachIndexed { index, qrCode ->
                val qrCodeDetails = mutableListOf<String>()
                qrCodeDetails.add("QR Code ${index + 1}: ID: ${qrCode.id}, Scans: ${qrCode.totalScans}")

                if (qrCode.scanDateList.isNotEmpty()) {
                    qrCodeDetails.add("Scan Dates: ${qrCode.scanDateList.joinToString(", ")}")
                }
                if (qrCode.conversion != "") {
                    qrCodeDetails.add("Conversions: ${qrCode.conversion}")
                }
                if (qrCode.revenue != "") {
                    qrCodeDetails.add("Revenue: ${qrCode.revenue}")
                }
                if (qrCode.expenses != "") {
                    qrCodeDetails.add("Expenses: ${qrCode.expenses}")
                }

                messageLines.add(qrCodeDetails.joinToString(", "))
            }

        // Convert the messageLines list to a string with line breaks
        val qrData = messageLines.joinToString("\n")

        // Template that will be dynamically updated
        val promptTemplate = """
        ${Constants.comparePrompt}
    """.trimIndent()

        // Replace placeholder with the actual message lines
        return promptTemplate.replace("{messageLines}", qrData)
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
                     qrCodeList[position].conversion = conversion
                 }
                 if(revenue.isNotEmpty()){
                     qrCodeList[position].revenue = revenue
                 }
                 if(expenses.isNotEmpty()){
                     qrCodeList[position].expenses = expenses
                 }

                 val item = qrCodeList[position]
                 item.conversion = conversion
                 item.revenue = revenue
                 item .expenses = expenses
                 appViewModel.updateHistory(item)
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
                        scan.timestamp?.let { getDateTimeFromTimeStamp1(it*1000) }
                    }

                    val timestampDates = trackableScans.mapNotNull { scan ->
                        scan.timestamp?.let { it * 1000 }
                    }

                    qrCodeList[i].scanDateList = formattedDates
                    qrCodeList[i].scanDateTimeStampList.addAll(timestampDates)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        menu!!.findItem(R.id.create).isVisible = true
//        menu.findItem(R.id.compare).isVisible = false
        menu.findItem(R.id.history).isVisible = true
        menu.findItem(R.id.analytics).isVisible = true
        return true
    }

    /**
     * Handles the item selection in the options menu.
     *
     * @param item The menu item that was selected.
     * @return True if the event was handled, false otherwise.
     */


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.analytics->{
                startActivity(Intent(context, AnalyticsActivity::class.java))
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