package com.expert.qrgenerator.ui.activities

import android.content.Context
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.ViewPagerAdapter
import com.expert.qrgenerator.databinding.ActivityBarcodeHistoryBinding
import com.google.android.material.tabs.TabLayout
import dagger.hilt.EntryPoint
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BarcodeHistoryActivity : BaseActivity() {

    private lateinit var binding: ActivityBarcodeHistoryBinding

    private lateinit var context:Context
    var viewPagerAdapter: ViewPagerAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setUpToolbar()
        setUpViewPager()
    }


    private fun initViews(){
        context = this

    }

    private fun setUpViewPager(){
        val fm: FragmentManager = supportFragmentManager
        viewPagerAdapter = ViewPagerAdapter(fm, lifecycle)
        binding.viewpager.adapter = viewPagerAdapter

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.tables)))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.create)))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.viewpager.currentItem = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })

    }

    private fun setUpToolbar(){
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = getString(R.string.qr_code_history)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

}