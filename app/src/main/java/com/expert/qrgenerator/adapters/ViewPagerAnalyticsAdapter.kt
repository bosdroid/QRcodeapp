package com.expert.qrgenerator.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.expert.qrgenerator.ui.fragments.RevenueAnalyticsFragment
import com.expert.qrgenerator.ui.fragments.ScanCountAnalyticsFragment
import com.expert.qrgenerator.ui.fragments.TimeAnalyticsFragment

class ViewPagerAnalyticsAdapter (fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3 // Total number of tabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ScanCountAnalyticsFragment()
            1 -> TimeAnalyticsFragment()
            2 -> RevenueAnalyticsFragment()
            else -> ScanCountAnalyticsFragment()
        }
    }
}