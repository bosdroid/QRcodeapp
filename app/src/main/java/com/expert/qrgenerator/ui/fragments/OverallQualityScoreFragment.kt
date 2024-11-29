package com.expert.qrgenerator.ui.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FragmentOverallQualityScoreBinding


class OverallQualityScoreFragment : Fragment() {

    private lateinit var binding:FragmentOverallQualityScoreBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOverallQualityScoreBinding.inflate(inflater, container, false)

        // Example scores
        val previousScore = 65
        val currentScore = 80

        // Update UI with the current quality score
        updateQualityScore(previousScore, currentScore)

        return binding.root
    }

    private fun updateQualityScore(previousScore: Int, currentScore: Int) {
        // Set numeric score text
        binding.qualityScore.text = "Quality Score: $currentScore"

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

        // Set color-coded quality indicator
        when {
            currentScore >= 75 -> binding.qualityIndicator.setBackgroundColor(Color.GREEN)  // High-quality
            currentScore in 50..74 -> binding.qualityIndicator.setBackgroundColor(Color.YELLOW)  // Medium-quality
            else -> binding.qualityIndicator.setBackgroundColor(Color.RED)  // Low-quality
        }
    }
}