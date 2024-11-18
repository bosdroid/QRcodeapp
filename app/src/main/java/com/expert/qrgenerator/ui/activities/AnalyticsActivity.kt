package com.expert.qrgenerator.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.ViewPagerAnalyticsAdapter
import com.expert.qrgenerator.databinding.ActivityAnalyticsBinding
import com.google.android.material.tabs.TabLayoutMediator

class AnalyticsActivity : BaseActivity() {

    private lateinit var binding:ActivityAnalyticsBinding
    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this

        setUpToolbar()
        setUpTabs()
    }

    private fun setUpTabs(){
        val adapter = ViewPagerAnalyticsAdapter(this)
        binding.viewPager.adapter = adapter

        // Connect TabLayout and ViewPager
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Scan Count"
                1 -> "Time"
                2 -> "Revenue"
                else -> "Tab ${position + 1}"
            }
        }.attach()
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.analytics)
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        menu!!.findItem(R.id.create).isVisible = true
        menu.findItem(R.id.compare).isVisible = true
        menu.findItem(R.id.history).isVisible = true
        menu.findItem(R.id.analytics).isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.create->{
                startActivity(Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                true
            }
            R.id.history->{
                startActivity(Intent(context, BarcodeHistoryActivity::class.java))
                true
            }
            R.id.compare->{
                startActivity(Intent(context, CodeComparisonActivity::class.java))
                true
            }
            else -> {
                // Pass the event to the superclass to handle other menu items
                super.onOptionsItemSelected(item)
            }
        }
    }
}