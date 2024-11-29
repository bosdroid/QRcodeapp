package com.expert.qrgenerator.ui.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentTimeAnalyticsBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter


class TimeAnalyticsFragment : Fragment() {
    private lateinit var binding:FragmentTimeAnalyticsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentTimeAnalyticsBinding.inflate(inflater, container, false)

        // Sample timestamps (replace this with your data from the server or database)
        val scanTimestamps = listOf(
            "2024-11-20 00:15:30", "2024-11-20 01:30:00", "2024-11-20 12:45:10",
            "2024-11-20 14:00:00", "2024-11-20 14:15:00", "2024-11-20 20:30:00",
            "2024-11-20 23:45:10"
        )

        // Prepare data for the histogram
        val scanEntries = prepareHistogramData(scanTimestamps)

        // Set up the histogram chart
        setupHistogram(binding.barChart, scanEntries)

        return binding.root
    }

    // Function to prepare data for the histogram
    private fun prepareHistogramData(scanTimestamps: List<String>): List<BarEntry> {
        val bins = IntArray(24) // Array to hold scan counts for each hour

        // Process timestamps and increment corresponding hour bins
        for (timestamp in scanTimestamps) {
            val hour = timestamp.substring(11, 13).toInt() // Extract hour from timestamp
            bins[hour]++
        }

        // Convert bins to BarEntry list
        return bins.mapIndexed { hour, count -> BarEntry(hour.toFloat(), count.toFloat()) }
    }

    // Function to set up the histogram chart
    private fun setupHistogram(barChart: BarChart, scanEntries: List<BarEntry>) {
        val barDataSet = BarDataSet(scanEntries, "Scans by Hour").apply {
            colors = generateColors(scanEntries) // Generate color gradient
            valueTextSize = 12f
        }

        val barData = BarData(barDataSet)
        barChart.apply {
            data = barData
            description.text = "Scan Distribution by Time of Day"
            setFitBars(true)

            // X-axis settings
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f // Ensure labels are displayed for every hour
                valueFormatter = IndexAxisValueFormatter((0..23).map { it.toString() })
                textColor = Color.BLACK
            }

            // Y-axis settings
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false // Disable right axis
            axisLeft.textColor = Color.BLACK

            legend.isEnabled = true
            animateY(1000) // Optional: Add animation
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

}