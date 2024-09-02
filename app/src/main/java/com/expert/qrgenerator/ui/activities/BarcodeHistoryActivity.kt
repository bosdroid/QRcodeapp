package com.expert.qrgenerator.ui.activities

import android.content.Context
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.ViewPagerAdapter
import com.expert.qrgenerator.databinding.ActivityBarcodeHistoryBinding
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BarcodeHistoryActivity : BaseActivity() {

    // ViewBinding for the activity layout
    private lateinit var binding: ActivityBarcodeHistoryBinding

    // Adapter for ViewPager
    private var viewPagerAdapter: ViewPagerAdapter? = null

    // Context of the activity, initialized using lazy delegation
    private val context: Context by lazy { this }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views and setup components
        initViews()
        setUpToolbar()
        setUpViewPager()
    }

    /**
     * Initialize any views or variables.
     */
    private fun initViews() {
        // This is where you can initialize any other views or components if needed
    }

    /**
     * Set up the ViewPager with the adapter and tabs.
     */
    private fun setUpViewPager() {
        val fragmentManager: FragmentManager = supportFragmentManager
        viewPagerAdapter = ViewPagerAdapter(fragmentManager, lifecycle)
        binding.viewpager.adapter = viewPagerAdapter

        // Adding tabs to TabLayout
        binding.tabLayout.apply {
            addTab(newTab().setText(getString(R.string.tables)))
            addTab(newTab().setText(getString(R.string.create)))

            // Set up TabSelectedListener to switch ViewPager pages
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    binding.viewpager.currentItem = tab?.position ?: 0
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    // Handle tab unselected if needed
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    // Handle tab reselected if needed
                }
            })
        }

        // Sync TabLayout with ViewPager page changes
        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })
    }

    /**
     * Set up the toolbar with title and home button.
     */
    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.qr_code_history)
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }
}
