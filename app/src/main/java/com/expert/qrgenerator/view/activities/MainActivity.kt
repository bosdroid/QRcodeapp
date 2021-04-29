package com.expert.qrgenerator.view.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.expert.qrgenerator.R
import com.expert.qrgenerator.interfaces.OnCompleteAction
import com.expert.qrgenerator.model.QRHistory
import com.expert.qrgenerator.view.fragments.GeneratorFragment
import com.expert.qrgenerator.view.fragments.ScannerFragment
import com.expert.qrgenerator.viewmodel.MainActivityViewModel
import com.expert.qrgenerator.viewmodelfactory.ViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textview.MaterialTextView


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,
    OnCompleteAction {

    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var mDrawer: DrawerLayout
    private lateinit var mNavigation: NavigationView
    private lateinit var bottomNavigation: BottomNavigationView
    private var encodedTextData: String = " "
    private lateinit var viewModel: MainActivityViewModel

    companion object {
        lateinit var nextStepTextView: MaterialTextView
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        initViews()
        setUpToolbar()

    }

    // THIS FUNCTION WILL INITIALIZE ALL THE VIEWS AND REFERENCE OF OBJECTS
    private fun initViews() {
        context = this
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(MainActivityViewModel()).createFor()
        )[MainActivityViewModel::class.java]
        toolbar = findViewById(R.id.toolbar)
        mDrawer = findViewById(R.id.drawer)
        mNavigation = findViewById(R.id.navigation)
        nextStepTextView = findViewById(R.id.next_step_btn)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_scanner -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ScannerFragment(), "scanner")
                        .addToBackStack("scanner")
                        .commit()
                    nextStepTextView.visibility = View.GONE
                }
                R.id.bottom_generator -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, GeneratorFragment(), "generator")
                        .addToBackStack("generator")
                        .commit()
                    nextStepTextView.visibility = View.VISIBLE
                }
                else -> {

                }
            }

            true
        }

        supportFragmentManager.beginTransaction().add(R.id.fragment_container,ScannerFragment(),"scanner")
            .addToBackStack("scanner")
            .commit()
    }

    // THIS FUNCTION WILL RENDER THE ACTION BAR/TOOLBAR
    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.app_name)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))

        val toggle = ActionBarDrawerToggle(this, mDrawer, toolbar, 0, 0)
        mDrawer.addDrawerListener(toggle)
        toggle.syncState()
        mNavigation.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.barcode_history -> {
                startActivity(Intent(context, BarcodeHistoryActivity::class.java))
            }
            else -> {
            }
        }
        mDrawer.closeDrawer(GravityCompat.START)
        return true
    }


    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentByTag("scanner")
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START)
        }
        else if(fragment!=null && fragment.isVisible){
           finish()
        }
        else {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container,ScannerFragment(),"scanner")
                .addToBackStack("scanner")
                .commit()
        }
    }

    // THIS METHOD WILL CALL AFTER SELECT THE QR TYPE WITH INPUT DATA
    override fun onTypeSelected(data: String, position: Int,type:String) {
        var url = ""
        val hashMap = hashMapOf<String, String>()
        hashMap["login"] = "sattar"
        hashMap["qrId"] = System.currentTimeMillis().toString()
        hashMap["userType"] = "free"
        if (position == 2) {


            hashMap["data"] = data

            startLoading(context)
            viewModel.createDynamicQrCode(context, hashMap)
            viewModel.getDynamicQrCode().observe(this, Observer { response ->
                dismiss()
                if (response != null) {
                    url = response.get("generatedUrl").asString
                    url = if (url.contains(":8990")) {
                        url.replace(":8990", "")
                    } else {
                        url
                    }
                    val qrHistory = QRHistory(
                        hashMap["login"]!!,
                        hashMap["qrId"]!!,
                        hashMap["data"]!!,
                        type,
                        hashMap["userType"]!!,
                        "",
                        1,
                        url,
                        System.currentTimeMillis()
                    )

                    val intent = Intent(context, DesignActivity::class.java)
                    intent.putExtra("ENCODED_TEXT", url)
                    intent.putExtra("QR_HISTORY",qrHistory)
                    startActivity(intent)
                } else {
                    showAlert(context, "Something went wrong, please try again!")
                }
            })
        } else {
            encodedTextData = data

            val qrHistory = QRHistory(
                hashMap["login"]!!,
                hashMap["qrId"]!!,
                encodedTextData,
                type,
                hashMap["userType"]!!,
                "",
                0,
                "",
                System.currentTimeMillis()
            )
            val intent = Intent(context, DesignActivity::class.java)
            intent.putExtra("ENCODED_TEXT", encodedTextData)
            intent.putExtra("QR_HISTORY",qrHistory)
            startActivity(intent)
        }

    }

}