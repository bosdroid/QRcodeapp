package com.expert.qrgenerator.view.activities

import android.content.Context
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout


class BarcodeHistoryActivity : BaseActivity() {

    private lateinit var context:Context
    private lateinit var toolbar: Toolbar
    var tabLayout: TabLayout? = null
    var pager2: ViewPager2? = null
    var viewPagerAdapter: ViewPagerAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_history)

        initViews()
        setUpToolbar()
        setUpViewPager()
    }


    private fun initViews(){
        context = this
        toolbar = findViewById(R.id.toolbar)
        tabLayout = findViewById(R.id.tab_layout)
        pager2 = findViewById(R.id.viewpager)
    }

    private fun setUpViewPager(){
        val fm: FragmentManager = supportFragmentManager
        viewPagerAdapter = ViewPagerAdapter(fm, lifecycle)
        pager2!!.adapter = viewPagerAdapter

        tabLayout!!.addTab(tabLayout!!.newTab().setText(getString(R.string.scan)))
        tabLayout!!.addTab(tabLayout!!.newTab().setText(getString(R.string.create)))

        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                pager2!!.currentItem = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        pager2!!.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tabLayout!!.selectTab(tabLayout!!.getTabAt(position))
            }
        })

    }

    private fun setUpToolbar(){
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.qr_code_history)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

}