package com.expert.qrgenerator.ui.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentConversionRateBinding
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter


class ConversionRateFragment : Fragment() {

    private lateinit var binding:FragmentConversionRateBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentConversionRateBinding.inflate(inflater, container, false)

// Manually entered conversion data for QR codes
        val qrCodeConversions = mapOf(
            "QR Code 1" to 45,  // 45%
            "QR Code 2" to 30,  // 30%
            "QR Code 3" to 60,  // 60%
            "QR Code 4" to 20   // 20%
        )

        // Calculate the overall conversion rate
        val overallRate = calculateOverallConversionRate(qrCodeConversions)

        // Display the overall conversion rate
        binding.overallConversionRate.text = "Overall Conversion Rate: $overallRate%"

        // Display conversion rates in a bar chart
        displayConversionRates(qrCodeConversions)
      return binding.root
    }

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

        val dataSet = BarDataSet(entries, "Conversion Rates").apply {
            colors = listOf(Color.BLUE, Color.GREEN, Color.RED, Color.MAGENTA)
            valueTextSize = 12f
        }

        val barData = BarData(dataSet)
        binding.barChart.apply {
            data = barData
            description.text = "Conversion Rates by QR Code"
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(conversions.keys.toList())
                granularity = 1f
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                textColor = Color.BLACK
            }
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false
            legend.isEnabled = true
            animateY(1000)
        }
    }
}