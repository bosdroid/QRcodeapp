package com.expert.qrgenerator.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentScanCountAnalyticsBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ScanCountAnalyticsFragment : Fragment() {
    private lateinit var binding:FragmentScanCountAnalyticsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentScanCountAnalyticsBinding.inflate(layoutInflater, container, false)


        val timestamps = listOf(
            1697971200000, // Example timestamps in milliseconds
            1698057600000,
            1698144000000,
            1698316800000 // Add more timestamps as needed
        )
        val scanCounts = listOf(10, 15, 20, 25)


        prepareChartData(timestamps, scanCounts, "daily", binding.lineChart)

        // Set up the spinner
        binding.periodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val period = when (position) {
                    0 -> "daily"  // Daily
                    1 -> "weekly" // Weekly
                    2 -> "monthly" // Monthly
                    else -> "daily"
                }
                // Update the chart based on the selected period
                prepareChartData(timestamps, scanCounts, period, binding.lineChart)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Default to "daily" if nothing is selected
                prepareChartData(timestamps, scanCounts, "daily", binding.lineChart)
            }
        }

        return binding.root
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
            color = android.graphics.Color.BLUE
            valueTextColor = android.graphics.Color.BLACK
            lineWidth = 2f
            setCircleColor(android.graphics.Color.RED)
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


}