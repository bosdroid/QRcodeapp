package com.expert.qrgenerator.ui.activities

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.ROIAdapter
import com.expert.qrgenerator.adapters.ViewPagerAnalyticsAdapter
import com.expert.qrgenerator.databinding.ActivityAnalyticsBinding
import com.expert.qrgenerator.interfaces.TrackableScansCallback
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.TargetData
import com.expert.qrgenerator.model.TrackableScan
import com.expert.qrgenerator.repository.DataRepository
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.ui.fragments.CodeComparisonAnalyticsFragment
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.TapTargetHelper
import com.expert.qrgenerator.viewmodel.CodeDetailViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class AnalyticsActivity : BaseActivity() {

    private lateinit var binding: ActivityAnalyticsBinding
    private lateinit var context: Context

    // ViewModel for handling data operations
    private val appViewModel: AppViewModel by viewModels()

    private var qrCodesList = mutableListOf<CodeHistory>()

    private val handler = Handler(Looper.getMainLooper())
    private val fetchInterval: Long = 10_000 // 10 seconds

    private var selectedQrCodesForAnalytics = mutableListOf<CodeHistory>()

    private lateinit var tapTargetHelper:TapTargetHelper

    private val viewModel: CodeDetailViewModel by viewModels()

    // Register the ActivityResultLauncher
    private val selectedQrCodesResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Get the returned data
                val selectedQrCodes =
                    result.data?.getSerializableExtra("SELECTED_QR_CODES") as? ArrayList<CodeHistory>
                if (selectedQrCodes != null) {
                    if (selectedQrCodes.size > 0) {
//                        if(selectedQrCodes.size == 1){
//                            handler.removeCallbacks(dataFetchRunnable)
//                        }
//                        else{
//                            handler.post(dataFetchRunnable)
//                        }
                        selectedQrCodesForAnalytics = mutableListOf()
                        selectedQrCodesForAnalytics.addAll(selectedQrCodes)
                        binding.qrCodesDataLayout.invalidate()
                        showAllData()
                    }
                }

            } else {
                Toast.makeText(this, "No result received", Toast.LENGTH_SHORT).show()
            }
        }

    private val dataFetchRunnable = object : Runnable {
        override fun run() {
            // Fetch updated data from the server
            startAnalytics()
            // Schedule the runnable again after the interval
            handler.postDelayed(this, fetchInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this

        setUpToolbar()

        tapTargetHelper = TapTargetHelper(this)
        startLoading(context)
        Handler(Looper.getMainLooper()).postDelayed({
            startAnalytics()
//            handler.postDelayed(dataFetchRunnable,2000)
        }, 2000)
        // Start fetching data


    }

    private fun startTapTargetSequence(){
        tapTargetHelper.showSequence(
            "walk_through",
            binding.nestedScrollView,
            listOf(
                TargetData(
                    binding.chooseQrCodesBtn,
                    "Choose QR Codes",
                    "User this button to choose other Qr Codes for comparison analytics"
                ),
                TargetData(
                    binding.qrCodeImage1,
                    "QR Codes Metrics",
                    "Here each QR code display the metrics and quality score."
                ),
                TargetData(
                    binding.scanCountAnalyticsLayout,
                    "QR Codes Scan Count",
                    "Here analyze the selected Qr codes scans count with time period"
                ),
                TargetData(
                    binding.timeAnalyticsLayout,
                    "QR Codes Times",
                    "Here each QR code display the metrics and overall quality score."
                ),
                TargetData(
                    binding.conversionRateLayout,
                    "Selected QR Codes Conversion",
                    "Here display the comparison result with each other and overall conversion rate"
                ),
                TargetData(
                    binding.revenueAnalyticsLayout,
                    "Selected QR Codes Revenue",
                    "Here display the selected QR Codes revenue behaviour in bar chart"
                ),
                TargetData(
                    binding.roiAnalyticsLayout,
                    "Selected QR Codes ROI",
                    "Here display the return on investment percentage for each selected qr code"
                ),
                TargetData(
                    binding.codeComparisonAnalyticsLayout,
                    "Selected QR Codes Comparison",
                    "Here display the bar chart that show the comparison each qr code based on scan count and conversion rate"
                ),
            )
        )
    }


    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(dataFetchRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(dataFetchRunnable)
    }

    private fun startAnalytics() {

        appViewModel.allTrackableQRCodes.observe(this@AnalyticsActivity, Observer { list ->
            if (list.isEmpty()) {
                // No QR codes, dismiss loading and exit
                dismiss()
                showTopTwoQrCodes()
                return@Observer
            }

            // Clear and add all QR codes
            qrCodesList.clear()
            qrCodesList.addAll(list)

            var completedRequests = 0
            val totalRequests = qrCodesList.size

            // Track progress for completing all requests
            qrCodesList.forEach { qrCode ->
                DataRepository.getAllLiveScanHistory(
                    qrCode.qrId,
                    object : TrackableScansCallback {
                        override fun onTrackableScansLoaded(trackableScans: List<TrackableScan>) {
                            // Update QR code with scan data
                            qrCode.totalScans = trackableScans.size
                            qrCode.scanDateList = trackableScans.mapNotNull { scan ->
                                scan.timestamp?.let { getDateTimeFromTimeStamp1(it * 1000) }
                            }
                            qrCode.scanDateTimeStampList.addAll(trackableScans.mapNotNull { scan ->
                                scan.timestamp?.let { it * 1000 }
                            })

                            // Increment completed requests and check completion
                            handleRequestCompletion(++completedRequests, totalRequests)
                        }

                        override fun onTrackableScansError() {
                            // Log error and increment completed requests
                            Log.e("LoadAllScanData", "Error loading data for qrCodeId: ${qrCode.qrId}")
                            handleRequestCompletion(++completedRequests, totalRequests)
                        }
                    }
                )
            }
        })
    }

    // Function to handle the completion of each request
    fun handleRequestCompletion(completed: Int, total: Int) {
        if (completed == total) {
            // Filter QR codes with at least one scan
            qrCodesList = qrCodesList.filter { it.totalScans > 0 }.toMutableList()

            // Select the top 1 or 2 QR codes for analytics
            selectedQrCodesForAnalytics.clear()
            selectedQrCodesForAnalytics.addAll(qrCodesList.take(2))

            // Show all data
            showAllData()
//            Handler(Looper.getMainLooper()).postDelayed({startTapTargetSequence()},1000)
            // Dismiss loading indicator
            dismiss()
        }
    }

    private fun showAllData() {
        Log.d("TEST1000", selectedQrCodesForAnalytics.toString())

        // HANDLE TOP TWO QR CODES
        showTopTwoQrCodes()
        // SHOW OVERALL SCORE QUALITY
//        showOverallQualityScore()
        // SHOW SCAN COUNT ANALYTICS
        showScanCountAnalytics()
        // SHOW TIME ANALYTICS
        showTimeAnalytics()
        // SHOW CONVERSION RATE ANALYTICS
        showConversionRateAnalytics()
        // SHOW REVENUE ANALYTICS
        showRevenueAnalytics()

        // SHOW EXPENSES/REVENUE ANALYTICS
        showExpensesRevenueAnalytics()


    }

    private fun showTopTwoQrCodes() {

        if (selectedQrCodesForAnalytics.isNotEmpty()) {
            binding.qrCodesDataLayout.visibility = View.VISIBLE
            binding.qrCodesEmptyLayout.visibility = View.GONE

            showTwoSelectedQrCodesData()
                // SHOW ROI (RETURN ON INVESTMENT) ANALYTICS
//            showReturnOnInvestmentAnalytics()
        } else {
            binding.qrCodesEmptyLayout.visibility = View.VISIBLE
            binding.qrCodesDataLayout.visibility = View.GONE
        }
        binding.createQrCodeBtn.setOnClickListener {
            finish()
        }

        binding.chooseQrCodesBtn.setOnClickListener {
            selectedQrCodesResultLauncher.launch(
                Intent(
                    context,
                    CodeComparisonActivity::class.java
                ).apply {
                    putExtra("FROM", "code_comparison")
                })
        }

        binding.aiCompareBtn.setOnClickListener {
            if (selectedQrCodesForAnalytics.size > 1){
                if(hasInsufficientScans(selectedQrCodesForAnalytics)){
                    showAlert(context,"AI comparison not start if any selected Qr code Scans less then 10!")
                }else{
                    val finalPrompt = buildComparisonPrompt(selectedQrCodesForAnalytics)
                    startLoading(context)
                    binding.aiCompareBtn.text = getString(R.string.please_wait)
                    binding.aiCompareBtn.isEnabled = false
                    lifecycleScope.launch {
                        viewModel.callAiRecommendationRequest(finalPrompt) { result ->
                            binding.aiCompareBtn.text = getString(R.string.ai_compare)
                            binding.aiCompareBtn.isEnabled = true
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

    private fun hasInsufficientScans(qrCodes: List<CodeHistory>): Boolean {
        return qrCodes.any { it.totalScans < 10 }
    }

    private fun showTwoSelectedQrCodesData() {
        if (selectedQrCodesForAnalytics.isNotEmpty()) {
            // SHOW CODE COMPARISON ANALYTICS
//            showCodeComparisonAnalytics()
        }

        for (i in 0 until selectedQrCodesForAnalytics.size) {
            val item = selectedQrCodesForAnalytics[i]
            if (selectedQrCodesForAnalytics.size == 1) {
                binding.qrCodeImage2.setImageResource(0)
                binding.qrCodeName1.text = ""
                binding.qrCodeScans1.text = ""
                binding.qrCodeExpenses1.text = ""
                binding.qrCodeConversion1.text = ""
                binding.qrCodeRevenue1.text = ""
                binding.qrCodeQualityScore1.text = ""
                binding.qrCodeRoi1.text = ""
            }
            if (i == 0) {
                Glide.with(context)
                    .load(item.localImagePath)
                    .into(binding.qrCodeImage1)
                binding.qrCodeName.text = "QR 1"
                binding.qrCodeScans.text = "Scans: ${item.scanDateList.size}"
                binding.qrCodeExpenses.text = "Expenses: ${if(item.expenses.isNotEmpty()){item.expenses}else{0}}"
                binding.qrCodeConversion.text = "Conversion: ${if(item.conversion.isNotEmpty()){item.conversion}else{0}}%"
                binding.qrCodeRevenue.text = "Revenue: ${if(item.revenue.isNotEmpty()){item.revenue}else{0}}"
                val maxScanCount = qrCodesList.maxOfOrNull { it.totalScans } ?: 0
                val maxRevenue = qrCodesList.maxOfOrNull {
                    it.revenue.toIntOrNull() ?: 0
                } ?: 0
                // Calculate ROI
                val qrCodeRoi = if (maxRevenue != 0) {
                    (((item.revenue.toIntOrNull() ?: 0) - maxRevenue) / maxRevenue) * 100
                } else {
                    0 // Default value if maxRevenue is zero
                }

                val qrCodeNs = if (maxScanCount != 0) {
                    item.scanDateList.size / maxScanCount
                } else {
                    0 // Default value if maxScanCount is zero
                }

                val qrCodeNc = if ((item.conversion.toIntOrNull() ?: 0) != 0) {
                    (item.conversion.toIntOrNull() ?: 0) / 100
                } else {
                    0 // Default value if conversion is zero
                }

                val qrCodeNr = if (maxRevenue != 0) {
                    (item.revenue.toIntOrNull() ?: 0) / maxRevenue
                } else {
                    0 // Default value if maxRevenue is zero
                }

                val qrCodeNRoi = if (maxRevenue != 0) {
                    qrCodeRoi / 100
                } else {
                    0 // Default value if maxRevenue is zero
                }

                val qrCodeOverQualityScore = (0.3 * qrCodeNs) + (0.3 * qrCodeNc) + (0.2 * qrCodeNr) + (0.2 * qrCodeNRoi)
                val revenue = item.revenue.toIntOrNull() ?: 0
                val expenses = item.expenses.toIntOrNull() ?: 0

                val roi = if (expenses != 0) {
                    ((revenue - expenses).toDouble() / expenses) * 100
                } else {
                    0.0 // Avoid division by zero
                }
                binding.qrCodeQualityScore.text =
                    "Quality: ${Math.round(qrCodeOverQualityScore * 100)}%"
                binding.qrCodeRoi.text = "ROI: ${roi.toInt()}%"
            } else if (i == 1) {
                Glide.with(context)
                    .load(item.localImagePath)
                    .into(binding.qrCodeImage2)
                binding.qrCodeName1.text = "QR 2"
                binding.qrCodeScans1.text = "Scans: ${item.scanDateList.size}"
                binding.qrCodeExpenses1.text = "Expenses: ${if(item.expenses.isNotEmpty()){item.expenses}else{0}}"
                binding.qrCodeConversion1.text = "Conversion: ${if(item.conversion.isNotEmpty()){item.conversion}else{0}}%"
                binding.qrCodeRevenue1.text = "Revenue: ${if(item.revenue.isNotEmpty()){item.revenue}else{0}}"

                val maxScanCount = qrCodesList.maxOfOrNull { it.totalScans } ?: 0
                val maxRevenue = qrCodesList.maxOfOrNull {
                    // Check if revenue is empty or null, then treat it as "0"
                    it.revenue.toIntOrNull() ?: 0
                } ?: 0
                Log.d("TEST10000", "MAX SCANS COUNTS:${maxScanCount}, MAX REVENUE:${maxRevenue}")
                // Calculate ROI
                val qrCodeRoi = if (maxRevenue != 0) {
                    (((item.revenue.toIntOrNull() ?: 0) - maxRevenue) / maxRevenue) * 100
                } else {
                    0 // Default value if maxRevenue is zero
                }

                val qrCodeNs = if (maxScanCount != 0) {
                    item.scanDateList.size / maxScanCount
                } else {
                    0 // Default value if maxScanCount is zero
                }

                val qrCodeNc = if ((item.conversion.toIntOrNull() ?: 0) != 0) {
                    (item.conversion.toIntOrNull() ?: 0) / 100
                } else {
                    0 // Default value if conversion is zero
                }

                val qrCodeNr = if (maxRevenue != 0) {
                    (item.revenue.toIntOrNull() ?: 0) / maxRevenue
                } else {
                    0 // Default value if maxRevenue is zero
                }

                val qrCodeNRoi = if (maxRevenue != 0) {
                    qrCodeRoi / 100
                } else {
                    0 // Default value if maxRevenue is zero
                }

                val qrCodeOverQualityScore = (0.3 * qrCodeNs) + (0.3 * qrCodeNc) + (0.2 * qrCodeNr) + (0.2 * qrCodeNRoi)
                val revenue = item.revenue.toIntOrNull() ?: 0
                val expenses = item.expenses.toIntOrNull() ?: 0

                val roi = if (expenses != 0) {
                    ((revenue - expenses).toDouble() / expenses) * 100
                } else {
                    0.0 // Avoid division by zero
                }
                binding.qrCodeQualityScore1.text =
                    "Quality: ${Math.round(qrCodeOverQualityScore * 100)}%"
                binding.qrCodeRoi1.text = "ROI: ${roi.toInt()}%"
            } else {
                break
            }
        }
    }

    private fun showScanCountAnalytics() {
        // Flatten the list of scan timestamps from all selected QR codes
        val qrCodes = selectedQrCodesForAnalytics

        // Log to check if we have data
        Log.d("Analytics", "QR Codes: $qrCodes")

        // Prepare the chart data with each QR code's data
        prepareChartData(qrCodes, "daily", binding.lineChart)

        // Set up the spinner to change the period (daily, weekly, monthly)
        binding.periodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val period = when (position) {
                    0 -> "daily"  // Daily
                    1 -> "weekly" // Weekly
                    2 -> "monthly" // Monthly
                    else -> "daily"
                }
                // Update the chart based on the selected period
                prepareChartData(qrCodes, period, binding.lineChart)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Default to "daily" if nothing is selected
                prepareChartData(qrCodes, "daily", binding.lineChart)
            }
        }
    }

    private fun prepareChartData(
        qrCodes: List<CodeHistory>, // List of QR codes to process
        period: String, // "daily", "weekly", or "monthly"
        chart: LineChart
    ) {
        // Initialize the chart data
        val lineDataSets = mutableListOf<ILineDataSet>() // MutableList<ILineDataSet> instead of MutableList<LineDataSet>

        // Loop through each QR code and prepare its individual data
        qrCodes.mapIndexed {pIndex, qrCode ->
            // Group data by the selected period for the current QR code
            val groupedData = groupDataByPeriod(qrCode.scanDateTimeStampList, period) // Group data inside the loop


            // Prepare entries for the chart
            val entries = groupedData.entries.mapIndexed { index, entry ->
                val totalScans = entry.value.size // Count how many scans occurred in each period
                Entry(index.toFloat(), totalScans.toFloat()) // x: index, y: total scans
            }

            // Create a dataset for this QR code and add it to the list of datasets
            val dataSet = LineDataSet(entries, "QR Code ${pIndex+1} - Time ($period)").apply {
                color = if (pIndex == 0) Color.BLUE else Color.RED
                valueTextColor = Color.BLACK
                lineWidth = 2f
                setCircleColor(if (pIndex == 0) Color.BLUE else Color.RED)
                circleRadius = 5f
            }

            // Cast to ILineDataSet and add to the list
            lineDataSets.add(dataSet as ILineDataSet)
        }

        // Configure the chart with all datasets
        chart.apply {
            data = LineData(lineDataSets) // Add all the datasets for the QR codes

            // Get x-axis labels from the first QR code's data
            val firstGroupedData = groupDataByPeriod(qrCodes.first().scanDateTimeStampList, period)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                valueFormatter = IndexAxisValueFormatter(firstGroupedData.keys.toList()) // Label x-axis
            }

            axisRight.isEnabled = false
            description.isEnabled = false
            invalidate() // Refresh the chart
        }
    }





    private fun groupDataByPeriod(
        timestamps: List<Long>,
        period: String // "daily", "weekly", or "monthly"
    ): Map<String, List<Long>> {
        val calendar = Calendar.getInstance()

        return when (period) {
            "daily" -> groupByDay(timestamps, calendar)
            "weekly" -> groupByWeek(timestamps, calendar)
            "monthly" -> groupByMonth(timestamps, calendar)
            else -> throw IllegalArgumentException("Invalid period: $period")
        }
    }

    private fun groupByDay(timestamps: List<Long>, calendar: Calendar): Map<String, List<Long>> {
        val dateFormat = SimpleDateFormat("EEEE", Locale.getDefault()) // Format time as HH:mm
        return timestamps.groupBy {
            calendar.timeInMillis = it
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            dateFormat.format(it) // Return formatted time for the x-axis
        }
    }

    private fun groupByWeek(timestamps: List<Long>, calendar: Calendar): Map<String, List<Long>> {
        val dateFormat = SimpleDateFormat("'Week of' dd MMM", Locale.getDefault()) // Format as day and month
        return timestamps.groupBy {
            calendar.timeInMillis = it
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            dateFormat.format(it) // Format for weekly x-axis
        }
    }

    private fun groupByMonth(timestamps: List<Long>, calendar: Calendar): Map<String, List<Long>> {
        val dateFormat =
            SimpleDateFormat("MMM yyyy", Locale.getDefault()) // Format as month and year
        return timestamps.groupBy {
            calendar.timeInMillis = it
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            dateFormat.format(it) // Format for monthly x-axis
        }
    }


    private fun showTimeAnalytics() {
        // List of scan timestamps for each QR code from your QR code list
        val qrCodeData = selectedQrCodesForAnalytics.map { qrCode ->
            qrCode.scanDateList // A list of timestamps for each QR code
        }

        // Prepare data for the line chart for each QR code
        val (entriesList, timeLabels) = prepareTimeLineData(qrCodeData)

        // Set up the line chart with data for each QR code
        setupLineChart(entriesList, timeLabels)
    }

    // Function to prepare line chart data for each QR code
    private fun prepareTimeLineData(qrCodeData: List<List<String>>): Pair<List<List<Entry>>, List<String>> {
        val timeFormat = SimpleDateFormat("hh:mm a dd MMM yyyy", Locale.getDefault())

        // This will hold entries for each QR code
        val entriesList = mutableListOf<List<Entry>>()

        // Group by time for each QR code and generate entries for the line chart
        val groupedByTimeList = qrCodeData.map { scanTimestamps ->
            scanTimestamps.groupingBy { timestamp ->
                val date = timeFormat.parse(timestamp)
                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date!!) // Extract time
            }.eachCount()
        }

        // Create entries and time labels for the chart
        val timeLabels = groupedByTimeList.flatMap { groupedByTime ->
            groupedByTime.keys.toList()
        }.distinct()

        // Map each QR code data to its corresponding entries
        groupedByTimeList.forEach { groupedByTime ->
            val entries = timeLabels.mapIndexed { index, timeLabel ->
                val count = groupedByTime[timeLabel] ?: 0
                Entry(index.toFloat(), count.toFloat())
            }
            entriesList.add(entries)
        }

        return Pair(entriesList, timeLabels)
    }

    // Function to set up the LineChart
    private fun setupLineChart(entriesList: List<List<Entry>>, timeLabels: List<String>) {
        // Create a LineDataSet for each QR code with different colors
        val lineDataSets = entriesList.mapIndexed { index, entries ->
            LineDataSet(entries, "QR Code ${index + 1}").apply {
                color = if (index == 0) Color.BLUE else Color.RED // Assign different colors for each QR code
                valueTextColor = Color.BLACK
                lineWidth = 2f
                setCircleColor(if (index == 0) Color.BLUE else Color.RED)
                circleRadius = 5f
            }
        }

        // Combine the datasets into one LineData
        val lineData = LineData(lineDataSets)

        // Configure the chart
        binding.timeLineChart.apply {
            data = lineData
            description.isEnabled = false // Disable the description
            setDrawGridBackground(false)

            // Configure X-axis to display time
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                valueFormatter = IndexAxisValueFormatter(timeLabels) // Set time labels
                textColor = Color.BLACK
                granularity = 1f // Ensure labels are displayed for each unique time
            }

            // Configure Y-axis
            axisRight.isEnabled = false // Disable right axis
            axisLeft.apply {
                axisMinimum = 0f // Start Y-axis at 0
                textColor = Color.BLACK
            }

            // Enable the legend and animation
            legend.isEnabled = true
            animateY(1000) // Optional: Add animation for Y-axis

            // Refresh the chart
            invalidate()
        }
    }




    // Function to prepare histogram data grouped by date
//    private fun prepareDateHistogramData(scanTimestamps: List<String>): Pair<List<BarEntry>, List<String>> {
//        // Parse and group timestamps by date
//        val dateFormat = SimpleDateFormat("hh:mm a dd MMM yyyy", Locale.getDefault())
//        val groupedByDate = scanTimestamps.groupingBy { timestamp ->
//            val date = dateFormat.parse(timestamp)
//            SimpleDateFormat("dd MMM", Locale.getDefault()).format(date!!)
//        }.eachCount()
//
//        // Create BarEntry list for each date and keep track of the labels
//        val dateLabels = groupedByDate.keys.toList()
//        val barEntries = groupedByDate.entries.mapIndexed { index, entry ->
//            BarEntry(index.toFloat(), entry.value.toFloat())
//        }
//
//        return Pair(barEntries, dateLabels)
//    }

    // Function to set up the histogram chart
//    private fun setupHistogram(
//        lineChart: LineChart,
//        scanEntries: List<BarEntry>,
//        dateLabels: List<String>
//    ) {
//        val barDataSet = BarDataSet(scanEntries, "Scans by Date").apply {
//            colors = generateColors(scanEntries) // Generate color gradient
//            valueTextSize = 12f
//        }
//
//        val barData = BarData(barDataSet)
//        barChart.apply {
//            data = barData
//            description.text = "Scan Distribution by Date"
//            setFitBars(true)
//
//            // X-axis settings
//            xAxis.apply {
//                position = XAxis.XAxisPosition.BOTTOM
//                granularity = 1f // Ensure labels are displayed for each date
//                valueFormatter = IndexAxisValueFormatter(dateLabels) // Set date labels
//                textColor = Color.BLACK
//            }
//
//            // Y-axis settings
//            axisLeft.axisMinimum = 0f
//            axisRight.isEnabled = false // Disable right axis
//            axisLeft.textColor = Color.BLACK
//
//            legend.isEnabled = true
//            animateY(1000) // Optional: Add animation
//        }
//    }


    private fun showConversionRateAnalytics() {

        val qrCodeConversions = selectedQrCodesForAnalytics.mapIndexed { index, qrCode ->
            "QR Code ${index + 1}" to (qrCode.conversion.toIntOrNull()?:0)
        }.toMap()

        // Calculate the overall conversion rate
        val overallRate = calculateOverallConversionRate(qrCodeConversions)

        // Display the overall conversion rate
        binding.overallConversionRate.text = "Overall Conversion Rate: $overallRate%"

        // Display conversion rates in a bar chart
        displayConversionRates(qrCodeConversions)
    }

    private fun showRevenueAnalytics() {
        // Example data: revenue for each QR code
        val qrCodeRevenues = selectedQrCodesForAnalytics.mapIndexed { index, qrCode ->
            "QR Code ${index + 1}" to (qrCode.revenue.toIntOrNull() ?:0)
        }.toMap()
//        val revenue = selectedQrCodesForAnalytics.map { it.revenue } // List of revenue values

        setupRevenueBarChart(qrCodeRevenues)
    }

    private fun setupRevenueBarChart(revenues: Map<String, Int>) {
        val entries = revenues.values.mapIndexed { index, rate ->
            BarEntry(index.toFloat(), rate.toFloat())
        }

// Ensure each QR Code has a unique legend label and alternate colors
        val dataSets = revenues.values.mapIndexed { index, rate ->
            BarDataSet(listOf(BarEntry(index.toFloat(), rate.toFloat())), "QR Code ${index + 1}").apply {
                // Assign colors alternately: Blue for odd-indexed QR Codes, Red for even-indexed
                color = if (index % 2 == 0) Color.BLUE else Color.RED
                valueTextSize = 12f
            }
        }

// Combine all data sets into BarData
        val barData = BarData().apply {
            dataSets.forEach { addDataSet(it) }
        }

// Configure the chart
        binding.revenueBarChart.apply {
            data = barData
            description.isEnabled = false
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(revenues.keys.toList()) // Display QR Code names
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                textColor = Color.BLACK
            }
            axisLeft.apply {
                axisMinimum = 0f
                textColor = Color.BLACK
            }
            axisRight.isEnabled = false
            legend.isEnabled = true
            animateY(1000)
            invalidate() // Refresh the chart
        }

    }



    private fun showExpensesRevenueAnalytics() {
        val qrCodes = List(selectedQrCodesForAnalytics.size) { index -> "QR ${index + 1}" }
        val revenue = selectedQrCodesForAnalytics.map { it.revenue.toFloatOrNull() ?:0F } // List of revenue values
        val expenses = selectedQrCodesForAnalytics.map { it.expenses.toFloatOrNull() ?:0F } // List of expense values


        setupExpensesRevenueBarChart(qrCodes, revenue,expenses)
    }

    private fun setupExpensesRevenueBarChart(qrCodes: List<String>, revenue: List<Float>, expenses: List<Float>) {
        // Create BarEntries for revenue and expenses
        val revenueEntries = revenue.mapIndexed { index, value -> BarEntry(index.toFloat() + 0.4f, value) } // Shift revenue bars to the right
        val expensesEntries = expenses.mapIndexed { index, value -> BarEntry(index.toFloat(), value) } // Keep expenses bars on the left

        // Create data sets
        val revenueDataSet = BarDataSet(revenueEntries, "Revenue").apply {
            color = Color.RED // Red for revenue (right)
            valueTextColor = Color.BLACK
            valueTextSize = 10f
        }
        val expensesDataSet = BarDataSet(expensesEntries, "Expenses").apply {
            color = Color.BLUE // Blue for expenses (left)
            valueTextColor = Color.BLACK
            valueTextSize = 10f
        }

        // Combine data sets into BarData
        val barData = BarData(expensesDataSet, revenueDataSet).apply {
            barWidth = 0.3f // Adjust bar width
        }

        // Configure the chart
        binding.codeExpenseRevenueBarChart.apply {
            data = barData
            description.isEnabled = false
            setFitBars(false) // Do not auto-fit bars; we'll group them manually

            axisLeft.apply {
                axisMinimum = 0f // Start y-axis at zero
                granularity = 1f // Prevent irregular y-axis scaling
                textColor = Color.BLACK
            }
            axisRight.isEnabled = false // Disable right y-axis
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(qrCodes) // Display QR codes on x-axis
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                textColor = Color.BLACK
                axisMinimum = -0.5f // Adjust to fit grouped bars
                axisMaximum = qrCodes.size.toFloat() // Ensure all bars fit within the chart
            }

            legend.apply {
                isEnabled = true // Enable the chart legend
                textColor = Color.BLACK
            }

            animateY(1000) // Optional: Animation for the Y-axis
        }

        // Group bars together (groupSpace, barSpace, barWidth)
        val groupSpace = 0.2f
        val barSpace = 0.05f
        barData.groupBars(0f, groupSpace, barSpace)

        // Refresh the chart
        binding.codeExpenseRevenueBarChart.invalidate()
    }

    private fun showReturnOnInvestmentAnalytics() {
// Example data: Revenue and Expenses
        val qrCodes = List(selectedQrCodesForAnalytics.size) { index ->
            "QR ${index + 1}"
        }
        val revenue = selectedQrCodesForAnalytics.map { it.revenue.toFloatOrNull() ?:0F } // listOf(2000f, 2500f, 3000f, 4000f)
        val expenses = selectedQrCodesForAnalytics.map { it.expenses.toFloatOrNull() ?:0F } // listOf(1500f, 1800f, 2200f, 2800f)

// Calculate ROI and cap it at 100%
        val roiList = revenue.mapIndexed { index, rev ->
            val exp = expenses[index]
            if (exp != 0f) {
                val roi = ((rev - exp) / exp) * 100
                minOf(roi, 100f) // Cap ROI at 100%
            } else {
                0f // Avoid division by zero
            }
        }


//        // Set up RecyclerView
//        binding.roiRecyclerView.layoutManager = LinearLayoutManager(this)
//        binding.roiRecyclerView.adapter = ROIAdapter(qrCodes, roiList)
    }

//    private fun showCodeComparisonAnalytics() {
//
//        // Extract data from CodeHistory model
//        val qrCodes = selectedQrCodesForAnalytics.map { it.qrId } // Get QR Code IDs
//        val scanCounts =
//            selectedQrCodesForAnalytics.map { it.totalScans.toFloat() } // Get total scans
//        val conversionRates =
//            selectedQrCodesForAnalytics.map { it.conversion } // Get conversion rates
//
//        // Prepare Data
//        val scanEntries = ArrayList<BarEntry>()
//        val conversionEntries = ArrayList<BarEntry>()
//        for (i in qrCodes.indices) {
//            scanEntries.add(BarEntry(i.toFloat(), scanCounts[i]))
//            conversionEntries.add(BarEntry(i.toFloat(), conversionRates[i]))
//        }
//
//        // Create DataSets
//        val scanDataSet = BarDataSet(scanEntries, "Scan Count").apply {
//            color = ColorTemplate.COLORFUL_COLORS[0] // Assign first color
//        }
//        val conversionDataSet = BarDataSet(conversionEntries, "Conversion Rate (%)").apply {
//            color = ColorTemplate.COLORFUL_COLORS[1] // Assign second color
//        }
//
//        // Create BarData
//        val barData = BarData(scanDataSet, conversionDataSet).apply {
//            barWidth = 0.4f // Width of each bar
//        }
//
//        // Configure BarChart
//        binding.codeComparisonBarChart.apply {
//            data = barData
//            description.isEnabled = false // Disable description
//            setDrawGridBackground(false) // Remove grid background
//            axisLeft.axisMinimum = 0f // Start y-axis from zero
//            xAxis.apply {
//                isGranularityEnabled = true // Ensure granularity
//                labelCount = qrCodes.size
//                valueFormatter = IndexAxisValueFormatter(qrCodes) // Set QR Code labels
//            }
//            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
//            groupBars(0f, 0.2f, 0.02f) // Group bars with appropriate spacing
//            invalidate() // Refresh chart
//        }
//    }




    // Function to calculate the overall conversion rate
    private fun calculateOverallConversionRate(conversions: Map<String, Int>): Int {
        val totalRate = conversions.values.sum()
        return if (conversions.isNotEmpty()) totalRate / conversions.size else 0
    }

    // Function to display conversion rates in a bar chart
    private fun displayConversionRates(conversions: Map<String, Int>) {
        val entries = conversions.values.mapIndexed { index, rate ->
            BarEntry(index.toFloat(), rate.toFloat())
        }

// Ensure each QR Code has a unique legend label and alternate colors
        val dataSets = conversions.values.mapIndexed { index, rate ->
            BarDataSet(listOf(BarEntry(index.toFloat(), rate.toFloat())), "QR Code ${index + 1}").apply {
                // Assign colors alternately: Blue for odd-indexed QR Codes, Red for even-indexed
                color = if (index % 2 == 0) Color.BLUE else Color.RED
                valueTextSize = 12f
            }
        }

// Combine all data sets into BarData
        val barData = BarData().apply {
            dataSets.forEach { addDataSet(it) }
        }

// Configure the chart
        binding.conversionRateBarChart.apply {
            data = barData
            description.isEnabled = false
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(conversions.keys.toList()) // Display QR Code names
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                textColor = Color.BLACK
            }
            axisLeft.apply {
                axisMinimum = 0f
                textColor = Color.BLACK
            }
            axisRight.isEnabled = false
            legend.isEnabled = true
            animateY(1000)
            invalidate() // Refresh the chart
        }

    }


    // Function to generate a color gradient for the bars
    private fun generateColors(scanEntries: List<BarEntry>): List<Int> {
        val maxCount = scanEntries.maxOfOrNull { it.y } ?: 1f
        return scanEntries.map { entry ->
            val intensity = (entry.y / maxCount * 255).toInt()
            Color.rgb(255, 255 - intensity, 255 - intensity) // Gradient from pink to white
        }
    }

    private fun prepareChartData(
        timestamps: List<Long>,
        scanCounts: List<Int>,
        period: String, // "daily", "weekly", or "monthly"
        chart: LineChart
    ) {
        // Group data by the selected period
        val groupedData = groupDataByPeriod(timestamps, scanCounts, period)

        // Prepare entries for the chart
        val entries = groupedData.entries.mapIndexed { index, entry ->
            val totalScans = entry.value.sumOf { it.second }
            Entry(index.toFloat(), totalScans.toFloat()) // x: index, y: total scans
        }

        // Create a dataset
        val dataSet = LineDataSet(entries, "Scans Over Time ($period)").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            lineWidth = 2f
            setCircleColor(Color.RED)
            circleRadius = 5f
        }

        // Configure the chart
        chart.apply {
            data = LineData(dataSet)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                valueFormatter = IndexAxisValueFormatter(groupedData.keys.toList()) // Label x-axis
            }
            axisRight.isEnabled = false
            description.isEnabled = false
            invalidate() // Refresh the chart
        }
    }

    private fun groupDataByPeriod(
        timestamps: List<Long>,
        scanCounts: List<Int>,
        period: String // "daily", "weekly", or "monthly"
    ): Map<String, List<Pair<Long, Int>>> {
        val formatter = when (period) {
            "daily" -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Day-based grouping
            "weekly" -> SimpleDateFormat("yyyy-'W'ww", Locale.getDefault()) // Week-based grouping
            "monthly" -> SimpleDateFormat("yyyy-MM", Locale.getDefault()) // Month-based grouping
            else -> throw IllegalArgumentException("Invalid period: $period")
        }

        // Group data by the formatted date string based on the selected period
        return timestamps.zip(scanCounts).groupBy { (timestamp, _) ->
            formatter.format(Date(timestamp))
        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.analytics)
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    private fun updateQualityScore(previousScore: Int, currentScore: Int) {
        // Set numeric score text
        binding.qualityScore.text = "${getString(R.string.quality_score)} $currentScore"

        // Determine trend direction and set arrow icon
        if (currentScore > previousScore) {
            binding.trendArrow.setImageResource(R.drawable.ic_arrow_upward)
            binding.trendArrow.setColorFilter(Color.GREEN)
        } else if (currentScore < previousScore) {
            binding.trendArrow.setImageResource(R.drawable.ic_arrow_downward)
            binding.trendArrow.setColorFilter(Color.RED)
        } else {
            binding.trendArrow.setImageResource(R.drawable.ic_arrow_right)
            binding.trendArrow.setColorFilter(Color.GRAY)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        menu!!.findItem(R.id.create).isVisible = true
//        menu.findItem(R.id.compare).isVisible = true
        menu.findItem(R.id.history).isVisible = true
        menu.findItem(R.id.analytics).isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            R.id.create -> {
                startActivity(Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                true
            }

            R.id.history -> {
                startActivity(Intent(context, BarcodeHistoryActivity::class.java))
                true
            }

//            R.id.compare -> {
//                startActivity(Intent(context, CodeComparisonActivity::class.java))
//                true
//            }

            else -> {
                // Pass the event to the superclass to handle other menu items
                super.onOptionsItemSelected(item)
            }
        }
    }

    class IndexAxisValueFormatter(private val labels: List<String>) : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val index = value.toInt()
            return if (index in labels.indices) labels[index] else ""
        }
    }
}