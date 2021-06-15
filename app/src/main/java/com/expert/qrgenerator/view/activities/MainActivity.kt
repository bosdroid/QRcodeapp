package com.expert.qrgenerator.view.activities

import android.Manifest
import android.accounts.Account
import android.accounts.AccountManager
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
import android.view.MenuItem
import android.view.View
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
import com.google.android.gms.drive.Drive.SCOPE_APPFOLDER
import com.google.android.gms.drive.Drive.SCOPE_FILE
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
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,
    OnCompleteAction {

    private lateinit var context: Context
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
    var credential: GoogleAccountCredential? = null
    private lateinit var auth: FirebaseAuth
    private val scopes = mutableListOf<String>()
    private val transport: HttpTransport? = AndroidHttp.newCompatibleTransport()
    private val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()
    private val httpTransport = NetHttpTransport()
    private val jacksonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
    private var user:User?=null

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
        getAccountsPermission()
        initializeGoogleLoginParameters()

    }

    // THIS FUNCTION WILL INITIALIZE ALL THE VIEWS AND REFERENCE OF OBJECTS
    private fun initViews() {
        context = this
        appSettings = AppSettings(context)
        auth = Firebase.auth
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(MainActivityViewModel()).createFor()
        )[MainActivityViewModel::class.java]
        toolbar = findViewById(R.id.toolbar)
        mDrawer = findViewById(R.id.drawer)
        privacyPolicy = findViewById(R.id.privacy_policy_view)
        privacyPolicy.movementMethod = LinkMovementMethod.getInstance()
        privacyPolicy.setPaintFlags(privacyPolicy.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)
        privacyPolicy.setOnClickListener {
            mDrawer.closeDrawer(GravityCompat.START)
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://qrmagicapp.com/privacy-policy-2/")
            )
            startActivity(browserIntent)
        }
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

        supportFragmentManager.beginTransaction().add(
            R.id.fragment_container,
            ScannerFragment(),
            "scanner"
        )
            .addToBackStack("scanner")
            .commit()
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
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))

        val toggle = ActionBarDrawerToggle(this, mDrawer, toolbar, 0, 0)
        mDrawer.addDrawerListener(toggle)
        toggle.syncState()
        mNavigation.setNavigationItemSelectedListener(this)
    }

    // THIS FUNCTION WILL INITIALIZE THE GOOGLE LOGIN PARAMETERS
    private fun initializeGoogleLoginParameters() {
        scopes.add(DriveScopes.DRIVE_METADATA_READONLY)
        scopes.add(SheetsScopes.SPREADSHEETS_READONLY)
        scopes.add(SheetsScopes.DRIVE)
        scopes.add(SheetsScopes.SPREADSHEETS)
        scopes.add(DriveScopes.DRIVE)
        scopes.add(DriveScopes.DRIVE_APPDATA)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        val acct = GoogleSignIn.getLastSignedInAccount(context)
        if (acct != null) {

            credential = GoogleAccountCredential.usingOAuth2(
                applicationContext, scopes
            )
                .setBackOff(ExponentialBackOff())
                .setSelectedAccount(Account(acct.email!!, AccountManager.KEY_ACCOUNT_TYPE))

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


    }

    private fun saveUserUpdatedDetail(acct: GoogleSignInAccount?, isLastSignUser: String) {
        if (acct != null) {
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
                showAlert(context, "User has been signIn successfully!")
            }

            if(!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), Scope(DriveScopes.DRIVE_APPDATA))) {
                GoogleSignIn.requestPermissions(
                    this,
                    100,
                    GoogleSignIn.getLastSignedInAccount(context),
                    Scope(DriveScopes.DRIVE_APPDATA))
            }
        }
        checkUserLoginStatus()

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.barcode_history -> {
                startActivity(Intent(context, BarcodeHistoryActivity::class.java))
            }
            R.id.sheets -> {
                if (appSettings.getBoolean(Constants.isLogin)) {
                    startActivity(Intent(context, SheetsActivity::class.java))
                } else {
                    startLogin()
                }

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
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setNegativeButton("Cancel") { dialog, which -> dialog.dismiss() }
                    .setPositiveButton("Logout") { dialog, which ->
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
        // Firebase sign out
        auth.signOut()
        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
            dismiss()
            appSettings.remove(Constants.isLogin)
            appSettings.remove(Constants.user)
            Toast.makeText(context, "User signout successfully!", Toast.LENGTH_SHORT)
                .show()
            checkUserLoginStatus()
        }


    }

    // THIS GOOGLE LAUNCHER WILL HANDLE RESULT
    private var googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {

//                val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(
//                    result.data
//                )
//                handleSignInResult(task)
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    handleSignInResult(account)
//                    firebaseAuthWithGoogle(account)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.w("TAG", "Google sign in failed", e)
                }
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
                    showAlert(context, "Something went wrong, please try again!")
                }
            })

        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    handleSignInResult(account)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "signInWithCredential:failure", task.exception)
                }
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
                    val qrHistory = CodeHistory(
                        hashMap["login"]!!,
                        hashMap["qrId"]!!,
                        hashMap["data"]!!,
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
                    showAlert(context, "Something went wrong, please try again!")
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
//            mNavigation.menu.findItem(R.id.sheets).isVisible = true

        } else {
            mNavigation.menu.findItem(R.id.login).isVisible = true
            mNavigation.menu.findItem(R.id.logout).isVisible = false
            mNavigation.menu.findItem(R.id.profile).isVisible = false
            mNavigation.menu.findItem(R.id.tables).isVisible = false
            mNavigation.menu.findItem(R.id.field_list).isVisible = false
//            mNavigation.menu.findItem(R.id.sheets).isVisible = false
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
        }
        else if (requestCode == 100){

//                saveToDriveAppFolder();

        }
    }

}