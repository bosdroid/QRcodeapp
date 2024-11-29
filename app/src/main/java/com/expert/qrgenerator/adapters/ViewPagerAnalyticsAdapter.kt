package com.expert.qrgenerator.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.expert.qrgenerator.ui.fragments.CodeComparisonAnalyticsFragment
import com.expert.qrgenerator.ui.fragments.ConversionRateFragment
import com.expert.qrgenerator.ui.fragments.RevenueAnalyticsFragment
import com.expert.qrgenerator.ui.fragments.RoiAnalyticsFragment
import com.expert.qrgenerator.ui.fragments.ScanCountAnalyticsFragment
import com.expert.qrgenerator.ui.fragments.TimeAnalyticsFragment

class ViewPagerAnalyticsAdapter (fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 6 // Total number of tabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ScanCountAnalyticsFragment()
            1 -> TimeAnalyticsFragment()
            2 -> ConversionRateFragment()
            3 -> RevenueAnalyticsFragment()
            4 -> RoiAnalyticsFragment()
            5 -> CodeComparisonAnalyticsFragment()
            else -> ScanCountAnalyticsFragment()
        }
    }
}