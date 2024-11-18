package com.expert.qrgenerator.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentScanCountAnalyticsBinding


class ScanCountAnalyticsFragment : Fragment() {
    private lateinit var binding:FragmentScanCountAnalyticsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentScanCountAnalyticsBinding.inflate(layoutInflater, container, false)


        return binding.root
    }

}