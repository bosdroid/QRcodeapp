package com.expert.qrgenerator.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentCodeComparisonAnalyticsBinding
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate


class CodeComparisonAnalyticsFragment : Fragment() {

    private lateinit var binding:FragmentCodeComparisonAnalyticsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCodeComparisonAnalyticsBinding.inflate(inflater, container, false)
        // Sample QR Code Metrics
        val qrCodes = listOf("QR1", "QR2", "QR3", "QR4")
        val scanCounts = listOf(150, 200, 120, 300) // Example scan counts
        val conversionRates = listOf(20f, 25f, 18f, 30f) // Example conversion rates

        // Prepare Data
        val scanEntries = ArrayList<BarEntry>()
        val conversionEntries = ArrayList<BarEntry>()
        for (i in qrCodes.indices) {
            scanEntries.add(BarEntry(i.toFloat(), scanCounts[i].toFloat()))
            conversionEntries.add(BarEntry(i.toFloat(), conversionRates[i]))
        }

        // Create DataSets
        val scanDataSet = BarDataSet(scanEntries, "Scan Count")
        scanDataSet.color = ColorTemplate.COLORFUL_COLORS[0] // First color
        val conversionDataSet = BarDataSet(conversionEntries, "Conversion Rate (%)")
        conversionDataSet.color = ColorTemplate.COLORFUL_COLORS[1] // Second color

        // Create BarData
        val barData = BarData(scanDataSet, conversionDataSet)
        barData.barWidth = 0.4f // Width of each bar
        binding.barChart.data = barData

        // Configure BarChart
        binding.barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            axisLeft.axisMinimum = 0f // Start y-axis from zero
            xAxis.isGranularityEnabled = true
            xAxis.labelCount = qrCodes.size
            xAxis.valueFormatter = IndexAxisValueFormatter(qrCodes) // Labels for QR Codes
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            groupBars(0f, 0.2f, 0.02f) // Group bars
            invalidate() // Refresh chart
        }
     return binding.root
    }

    class IndexAxisValueFormatter(private val labels: List<String>) : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val index = value.toInt()
            return if (index in labels.indices) labels[index] else ""
        }
    }

}