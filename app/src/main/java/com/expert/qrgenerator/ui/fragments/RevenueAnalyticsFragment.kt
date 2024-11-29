package com.expert.qrgenerator.ui.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentRevenueAnalyticsBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter


class RevenueAnalyticsFragment : Fragment() {

    private lateinit var binding:FragmentRevenueAnalyticsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRevenueAnalyticsBinding.inflate(inflater, container, false)

        // Example data: revenue and expenses for each QR code
        val qrCodes = listOf("QR1", "QR2", "QR3", "QR4")
        val revenue = listOf(2000f, 2500f, 3000f, 4000f)
        val expenses = listOf(1500f, 1800f, 2200f, 2800f)

        setupBarChart(qrCodes, revenue, expenses)

        return binding.root
    }

    private fun setupBarChart(qrCodes: List<String>, revenue: List<Float>, expenses: List<Float>) {
        // Create BarEntries for revenue and expenses
        val revenueEntries = revenue.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }
        val expensesEntries = expenses.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }

        // Create data sets
        val revenueDataSet = BarDataSet(revenueEntries, "Revenue").apply {
            color = Color.GREEN
        }
        val expensesDataSet = BarDataSet(expensesEntries, "Expenses").apply {
            color = Color.RED
        }

        // Combine data sets into BarData
        val barData = BarData(revenueDataSet, expensesDataSet).apply {
            barWidth = 0.4f // Set bar width
        }

        // Configure the chart
        binding.barChart.apply {
            data = barData
            description.isEnabled = false
            setFitBars(true)
            axisLeft.axisMinimum = 0f // Start y-axis at zero
            axisRight.isEnabled = false // Disable right y-axis

            // Customize x-axis
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(qrCodes)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
            }

            legend.isEnabled = true // Enable legend
            animateY(1000) // Animate the chart vertically
        }

        // Group bars together
        binding.barChart.groupBars(0f, 0.2f, 0.05f) // (groupSpace, barSpace, barWidth)
        binding.barChart.invalidate() // Refresh the chart
    }

}