package com.expert.qrgenerator.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.expert.qrgenerator.databinding.FragmentUtmBuilderBinding


class UtmBuilderFragment : Fragment() {

    private lateinit var binding:FragmentUtmBuilderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUtmBuilderBinding.inflate(inflater, container, false)

        binding.nextStepBtn.setOnClickListener {

        }

       return binding.root
    }
}