package com.expert.qrgenerator.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.ROIAdapter
import com.expert.qrgenerator.databinding.FragmentRoiAnalyticsBinding


class RoiAnalyticsFragment : Fragment() {

    private lateinit var binding:FragmentRoiAnalyticsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRoiAnalyticsBinding.inflate(inflater, container, false)

        // Example data: Revenue and Expenses
        val qrCodes = listOf("QR1", "QR2", "QR3", "QR4")
        val revenue = listOf(2000f, 2500f, 3000f, 4000f)
        val expenses = listOf(1500f, 1800f, 2200f, 2800f)

        // Calculate ROI
        val roiList = revenue.mapIndexed { index, rev ->
            val exp = expenses[index]
            if (exp != 0f) ((rev - exp) / exp) * 100 else 0f // Avoid division by zero
        }

        // Set up RecyclerView
        binding.roiRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
        binding.roiRecyclerView.adapter = ROIAdapter(qrCodes, roiList)

       return binding.root
    }

}