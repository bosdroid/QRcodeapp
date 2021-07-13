package com.expert.qrgenerator.view.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.expert.qrgenerator.R
import com.expert.qrgenerator.interfaces.OnCompleteAction
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.User
import com.expert.qrgenerator.singleton.DriveService
import com.expert.qrgenerator.singleton.SheetService
import com.expert.qrgenerator.utils.AppSettings
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.view.fragments.GeneratorFragment
import com.expert.qrgenerator.view.fragments.ScannerFragment
import com.expert.qrgenerator.viewmodel.MainActivityViewModel
import com.expert.qrgenerator.viewmodelfactory.ViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textview.MaterialTextView
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,
    OnCompleteAction, ScannerFragment.ScannerInterface {


    private lateinit var toolbar: Toolbar
    private lateinit var mDrawer: DrawerLayout
    private lateinit var mNavigation: NavigationView
    private lateinit var privacyPolicy: MaterialTextView
    private lateinit var bottomNavigation: BottomNavigationView
    private var encodedTextData: String = " "
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var appSettings: AppSettings
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    var mService: Drive? = null
    var sheetService: Sheets? = null
    private lateinit var auth: FirebaseAuth
    private val scopes = mutableListOf<String>()
    private val transport: HttpTransport? = AndroidHttp.newCompatibleTransport()
    private val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()
    private val httpTransport = NetHttpTransport()
    private val jacksonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
    private var user: User? = null
    private var requestLogin: String? = null
    private var scannerFragment: ScannerFragment? = null



    companion object {
        lateinit var context: Context
        lateinit var historyBtn: MaterialTextView
        var credential: GoogleAccountCredential? = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        initViews()
        setUpToolbar()
        getAccountsPermission()
        initializeGoogleLoginParameters()

        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            val duration = appSettings.getLong("tt1")
            if (duration.compareTo(0) == 0 || System.currentTimeMillis()-duration > TimeUnit.DAYS.toMillis(1) ) {

                SimpleTooltip.Builder(this)
                    .anchorView(bottomNavigation)
                    .text(getString(R.string.bottom_navigation_tip_text))
                    .gravity(Gravity.TOP)
                    .animated(true)
                    .transparentOverlay(false)
                    .onDismissListener { tooltip ->
                        tooltip.dismiss()
                        appSettings.putLong("tt1",System.currentTimeMillis())
                        val fragment =
                            supportFragmentManager.findFragmentByTag("scanner") as ScannerFragment
                        fragment.showTableSelectTip()
                    }
                    .build()
                    .show()
            }
        }
    }

    // THIS FUNCTION WILL INITIALIZE ALL THE VIEWS AND REFERENCE OF OBJECTS
    private fun initViews() {
        context = this
        appSettings = AppSettings(context)
        scannerFragment = ScannerFragment()
        auth = Firebase.auth
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(MainActivityViewModel()).createFor()
        )[MainActivityViewModel::class.java]
        toolbar = findViewById(R.id.toolbar)
        mDrawer = findViewById(R.id.drawer)
        historyBtn = findViewById(R.id.history_btn)
        historyBtn.setOnClickListener {
            startActivity(Intent(context, BarcodeHistoryActivity::class.java))
        }
        privacyPolicy = findViewById(R.id.privacy_policy_view)
        privacyPolicy.movementMethod = LinkMovementMethod.getInstance()
        privacyPolicy.paintFlags = privacyPolicy.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        privacyPolicy.setOnClickListener {
            mDrawer.closeDrawer(GravityCompat.START)
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://qrmagicapp.com/privacy-policy-2/")
            )
            startActivity(browserIntent)
        }
        mNavigation = findViewById(R.id.navigation)
//        nextStepTextView = findViewById(R.id.next_step_btn)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_scanner -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ScannerFragment(), "scanner")
                        .addToBackStack("scanner")
                        .commit()
//                    nextStepTextView.visibility = View.GONE
//                    historyBtn.visibility = View.VISIBLE
                }
                R.id.bottom_generator -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, GeneratorFragment(), "generator")
                        .addToBackStack("generator")
                        .commit()
//                    historyBtn.visibility = View.GONE
//                    nextStepTextView.visibility = View.VISIBLE
                }
                else -> {

                }
            }

            true
        }

        if (intent != null && intent.hasExtra("KEY") && intent.getStringExtra("KEY") == "generator") {
            bottomNavigation.selectedItemId = R.id.bottom_generator
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, GeneratorFragment(), "generator")
                .addToBackStack("generator")
                .commit()
            //historyBtn.visibility = View.GONE
//            nextStepTextView.visibility = View.VISIBLE
        } else {
//            nextStepTextView.visibility = View.GONE
            //historyBtn.visibility = View.VISIBLE
            supportFragmentManager.beginTransaction().add(
                R.id.fragment_container,
                ScannerFragment(),
                "scanner"
            )
                .addToBackStack("scanner")
                .commit()
        }
//        Constants.tipsValue = appSettings.getBoolean(getString(R.string.key_tips))

    }

    private fun getAccountsPermission() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.GET_ACCOUNTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity,
                    Manifest.permission.GET_ACCOUNTS
                )
            ) {
                Log.e("Accounts", "Permission Granted")
                initializeGoogleLoginParameters()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.GET_ACCOUNTS),
                    0
                )
            }
        }
    }

    // THIS FUNCTION WILL RENDER THE ACTION BAR/TOOLBAR
    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.app_name)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))

        val toggle = ActionBarDrawerToggle(this, mDrawer, toolbar, 0, 0)
        mDrawer.addDrawerListener(toggle)
        toggle.syncState()
        mNavigation.setNavigationItemSelectedListener(this)

        toolbar.setNavigationOnClickListener {
            hideSoftKeyboard(context, mDrawer)
            if (mDrawer.isDrawerOpen(GravityCompat.START)) {
                mDrawer.closeDrawer(GravityCompat.START)
            } else {
                mDrawer.openDrawer(GravityCompat.START)
            }
        }
    }

    // THIS FUNCTION WILL INITIALIZE THE GOOGLE LOGIN PARAMETERS
    private fun initializeGoogleLoginParameters() {
        scopes.add(DriveScopes.DRIVE_METADATA_READONLY)
        scopes.add(SheetsScopes.SPREADSHEETS_READONLY)
        scopes.add(SheetsScopes.DRIVE)
        scopes.add(SheetsScopes.SPREADSHEETS)
        scopes.add(DriveScopes.DRIVE)
        scopes.add(DriveScopes.DRIVE_APPDATA)

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, signInOptions)

        val acct: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)
        if (acct != null) {

            credential = GoogleAccountCredential.usingOAuth2(
                applicationContext, scopes
            )
                .setSelectedAccount(acct.account)

            mService = Drive.Builder(
                transport, jsonFactory, credential
            ).setHttpRequestInitializer { request ->
                credential!!.initialize(request)
                request!!.connectTimeout = 300 * 60000  // 300 minutes connect timeout
                request.readTimeout = 300 * 60000  // 300 minutes read timeout
            }
                .setApplicationName(getString(R.string.app_name))
                .build()

            try {
                sheetService = Sheets.Builder(
                    httpTransport,
                    jacksonFactory,
                    credential
                )
                    .setApplicationName(getString(R.string.app_name))
                    .build()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            DriveService.saveDriveInstance(mService!!)
            SheetService.saveGoogleSheetInstance(sheetService!!)
            saveUserUpdatedDetail(acct, "last")
        }

        if (intent != null && intent.hasExtra("REQUEST") && intent.getStringExtra("REQUEST") == "login") {
            requestLogin = "login"
            startLogin()
        }
    }


    private fun saveUserUpdatedDetail(acct: GoogleSignInAccount?, isLastSignUser: String) {
        try {

            // IF PART WILL RUN IF USER LOGGED AND ACCOUNT DETAIL NOT EMPTY
            if (acct != null && acct.displayName.isNullOrEmpty()) {
                startLogin()
            } else if (acct != null) {
                val personName = acct.displayName
                val personGivenName = acct.givenName
                val personFamilyName = acct.familyName
                val personEmail = acct.email
                val personId = acct.id
                val personPhoto: Uri? = acct.photoUrl
                val user = User(
                    personName!!,
                    personGivenName!!,
                    personFamilyName!!,
                    personEmail!!,
                    personId!!,
                    personPhoto!!.toString()
                )
                appSettings.putUser(Constants.user, user)
                Constants.userData = user
                if (isLastSignUser == "new") {
                    appSettings.putBoolean(Constants.isLogin, true)
                    Toast.makeText(
                        context,
                        getString(R.string.user_signin_success_text),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (requestLogin!!.isNotEmpty() && requestLogin == "login") {
                    startActivity(Intent(context, TablesActivity::class.java))
                }
            }
            // ELSE PART WILL WORK WHEN USER LOGGED BUT ACCOUNT DETAIL EMPTY
            // AND IN CASE ACCOUNT DETAIL IS EMPTY THEN APP FETCH THE ACCOUNT DETAIL FROM PREFERENCE FOR AVOID NULL ANC CRASH THE APP
//            else {
//                val userDetail = appSettings.getUser(Constants.user)
//                val user = User(
//                    userDetail.personName,
//                    userDetail.personGivenName,
//                    userDetail.personFamilyName,
//                    userDetail.personEmail,
//                    userDetail.personId,
//                    userDetail.personPhoto
//                )
//                appSettings.putUser(Constants.user, user)
//                Constants.userData = user
//            }
        } catch (e: Exception) {

        }
        checkUserLoginStatus()

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.dynamic_links -> {
                startActivity(Intent(context, DynamicQrActivity::class.java))
            }
            R.id.sheets -> {
                if (appSettings.getBoolean(Constants.isLogin)) {
                    startActivity(Intent(context, SheetsActivity::class.java))
                } else {
                    startLogin()
                }

            }
            R.id.nav_setting -> {
                startActivity(Intent(context, SettingsActivity::class.java))
            }
            R.id.tables -> {
                startActivity(Intent(context, TablesActivity::class.java))
            }
            R.id.tables_data -> {
                startActivity(Intent(context, TablesDataActivity::class.java))
            }
            R.id.nav_rateUs -> {
                rateUs(this)
            }
            R.id.nav_recommend -> {
                shareApp()
            }
            R.id.nav_contact_support -> {
                contactSupport(this)
            }
            R.id.login -> {
                startLogin()
            }
            R.id.field_list -> {
                startActivity(Intent(context, FieldListsActivity::class.java))
            }
            R.id.profile -> {
                startActivity(Intent(context, ProfileActivity::class.java))
            }
            R.id.logout -> {
                MaterialAlertDialogBuilder(context)
                    .setTitle(getString(R.string.logout))
                    .setMessage(getString(R.string.logout_warning_text))
                    .setNegativeButton(getString(R.string.cancel_text)) { dialog, which -> dialog.dismiss() }
                    .setPositiveButton(getString(R.string.logout)) { dialog, which ->
                        startLoading(context)
                        signOut()
                    }
                    .create().show()
            }
            else -> {
            }
        }
        mDrawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                //hideSoftKeyboard(context, mDrawer)
                return true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    }

    private fun startLogin() {
        val signInIntent = mGoogleSignInClient.signInIntent
        googleLauncher.launch(signInIntent)
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            getString(R.string.share_app_message) + "https://play.google.com/store/apps/details?id=" + packageName
        )
        startActivity(shareIntent)

    }

    private fun signOut() {

        // Google sign out
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this) {

            mGoogleSignInClient.signOut().addOnCompleteListener(this) {
                dismiss()
                appSettings.remove(Constants.isLogin)
                appSettings.remove(Constants.user)
                Toast.makeText(context, getString(R.string.logout_success_text), Toast.LENGTH_SHORT)
                    .show()
                Constants.userData = null
                Constants.sheetService = null
                Constants.mService = null
                checkUserLoginStatus()
            }

        }

    }

    // THIS GOOGLE LAUNCHER WILL HANDLE RESULT
    private var googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

                GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    .addOnSuccessListener(object : OnSuccessListener<GoogleSignInAccount> {
                        override fun onSuccess(googleSignInAccount: GoogleSignInAccount?) {
                            credential = GoogleAccountCredential.usingOAuth2(
                                context,
                                scopes
                            )
                                .setSelectedAccount(googleSignInAccount!!.account)
//                            if (googleSignInAccount != null) {
//                                credential.selectedAccount = googleSignInAccount.account
//                            }

                            mService = Drive.Builder(
                                transport, jsonFactory, credential
                            ).setHttpRequestInitializer { request ->
                                credential!!.initialize(request)
                                request!!.connectTimeout =
                                    300 * 60000  // 300 minutes connect timeout
                                request.readTimeout = 300 * 60000  // 300 minutes read timeout
                            }
                                .setApplicationName(getString(R.string.app_name))
                                .build()

                            try {
                                sheetService = Sheets.Builder(
                                    httpTransport,
                                    jacksonFactory,
                                    credential
                                )
                                    .setApplicationName(getString(R.string.app_name))
                                    .build()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            DriveService.saveDriveInstance(mService!!)
                            SheetService.saveGoogleSheetInstance(sheetService!!)
                            if (googleSignInAccount != null) {
                                handleSignInResult(googleSignInAccount)
                            }
                        }
                    }).addOnFailureListener(object : OnFailureListener {
                        override fun onFailure(p0: java.lang.Exception) {
                            showAlert(context, p0.localizedMessage!!)
                        }

                    })
            }
        }

    private fun handleSignInResult(acct: GoogleSignInAccount) {
        try {

            startLoading(context)
            val hashMap = hashMapOf<String, String>()
            hashMap["personName"] = acct.displayName.toString()
            hashMap["personGivenName"] = acct.givenName.toString()
            hashMap["personFamilyName"] = acct.familyName.toString()
            hashMap["personEmail"] = acct.email.toString()
            hashMap["personId"] = acct.id.toString()
            hashMap["personPhoto"] = acct.photoUrl.toString()

            viewModel.signUp(context, hashMap)
            viewModel.getSignUp().observe(this, { response ->
                dismiss()
                if (response != null) {
                    if (response.has("errorMessage")) {

                    } else {
                        saveUserUpdatedDetail(acct, "new")
                    }
                } else {
                    showAlert(context, getString(R.string.something_wrong_error))
                }
            })

        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
        }
    }


    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentByTag("scanner")
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START)
        } else if (fragment != null && fragment.isVisible) {
            finish()
        } else {
            bottomNavigation.selectedItemId = R.id.bottom_scanner
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                ScannerFragment(),
                "scanner"
            )
                .addToBackStack("scanner")
                .commit()
        }
    }

    // THIS METHOD WILL CALL AFTER SELECT THE QR TYPE WITH INPUT DATA
    override fun onTypeSelected(data: String, position: Int, type: String) {
        var url = ""
        val hashMap = hashMapOf<String, String>()
        hashMap["login"] = "qrmagicapp"
        hashMap["qrId"] = System.currentTimeMillis().toString()
        hashMap["userType"] = "free"
        if (position == 2) {


            hashMap["userUrl"] = data

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
                    val qrHistory = CodeHistory(
                        hashMap["login"]!!,
                        hashMap["qrId"]!!,
                        hashMap["userUrl"]!!,
                        type,
                        hashMap["userType"]!!,
                        "qr",
                        "create",
                        "",
                        "1",
                        url,
                        System.currentTimeMillis().toString()
                    )

                    val intent = Intent(context, DesignActivity::class.java)
                    intent.putExtra("ENCODED_TEXT", url)
                    intent.putExtra("QR_HISTORY", qrHistory)
                    startActivity(intent)
                } else {
                    showAlert(context, getString(R.string.something_wrong_error))
                }
            })
        } else {
            encodedTextData = data

            val qrHistory = CodeHistory(
                hashMap["login"]!!,
                hashMap["qrId"]!!,
                encodedTextData,
                type,
                hashMap["userType"]!!,
                "qr",
                "create",
                "",
                "0",
                "",
                System.currentTimeMillis().toString()
            )
            val intent = Intent(context, DesignActivity::class.java)
            intent.putExtra("ENCODED_TEXT", encodedTextData)
            intent.putExtra("QR_HISTORY", qrHistory)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        checkUserLoginStatus()
    }

    private fun checkUserLoginStatus() {
        if (appSettings.getBoolean(Constants.isLogin)) {
            mNavigation.menu.findItem(R.id.login).isVisible = false
            mNavigation.menu.findItem(R.id.logout).isVisible = true
            mNavigation.menu.findItem(R.id.profile).isVisible = true
            mNavigation.menu.findItem(R.id.tables).isVisible = true
            mNavigation.menu.findItem(R.id.field_list).isVisible = true
//            mNavigation.menu.findItem(R.id.dynamic_links).isVisible = true


        } else {
            mNavigation.menu.findItem(R.id.login).isVisible = true
            mNavigation.menu.findItem(R.id.logout).isVisible = false
            mNavigation.menu.findItem(R.id.profile).isVisible = false
            mNavigation.menu.findItem(R.id.tables).isVisible = false
            mNavigation.menu.findItem(R.id.field_list).isVisible = false
//            mNavigation.menu.findItem(R.id.dynamic_links).isVisible = false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeGoogleLoginParameters()
            }
        } else if (requestCode == 100) {

//                saveToDriveAppFolder();

        }
    }

    override fun login() {
        startLogin()
    }

    override fun onPause() {
        hideKeyboard(context,this)
        super.onPause()
    }

    override fun onDestroy() {
        hideKeyboard(context,this)
        super.onDestroy()

    }

}