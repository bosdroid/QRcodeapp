package com.expert.qrgenerator.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.expert.qrgenerator.ui.fragments.CreateFragment
import com.expert.qrgenerator.ui.fragments.ScanFragment


class ViewPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    /**
     * Creates a new Fragment instance based on the position.
     *
     * @param position The position of the fragment to be created.
     * @return The Fragment instance to be displayed at the given position.
     */
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> CreateFragment() // Return CreateFragment for position 1
            else -> ScanFragment() // Return ScanFragment for all other positions
        }
    }

    /**
     * Returns the total number of fragments in the adapter.
     *
     * @return The total number of fragments.
     */
    override fun getItemCount(): Int {
        return 2 // Total number of fragments
    }
}
