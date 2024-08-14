package com.expert.qrgenerator.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.expert.qrgenerator.ui.fragments.CreateFragment
import com.expert.qrgenerator.ui.fragments.ScanFragment


class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager,lifecycle){

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> CreateFragment()
            else -> ScanFragment()
        }
    }

    override fun getItemCount(): Int {
        return 2
    }

}