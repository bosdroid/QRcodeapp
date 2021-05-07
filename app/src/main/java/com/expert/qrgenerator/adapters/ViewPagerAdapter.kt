package com.expert.qrgenerator.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.expert.qrgenerator.view.fragments.CreateFragment
import com.expert.qrgenerator.view.fragments.ScanFragment


class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager,lifecycle){

    override fun createFragment(position: Int): Fragment {
        when (position) {
            1 -> return CreateFragment()
        }
        return ScanFragment()
    }

    override fun getItemCount(): Int {
        return 2
    }

}