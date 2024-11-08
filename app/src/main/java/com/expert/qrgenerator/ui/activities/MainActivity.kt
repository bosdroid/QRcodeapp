package com.expert.qrgenerator.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.ActivityMainBinding
import com.expert.qrgenerator.databinding.ContentMainBinding
import com.expert.qrgenerator.interfaces.LoginCallback
import com.expert.qrgenerator.interfaces.OnFragmentReplaceListener
import com.expert.qrgenerator.model.User
import com.expert.qrgenerator.repository.DataRepository
import com.expert.qrgenerator.ui.fragments.CalendarFragment
import com.expert.qrgenerator.ui.fragments.ChooseTypeFragment
import com.expert.qrgenerator.ui.fragments.ContactFragment
import com.expert.qrgenerator.ui.fragments.CryptoPaymentFragment
import com.expert.qrgenerator.ui.fragments.DynamicFragment
import com.expert.qrgenerator.ui.fragments.EmailFragment
import com.expert.qrgenerator.ui.fragments.EtsyFragment
import com.expert.qrgenerator.ui.fragments.FacebookFragment
import com.expert.qrgenerator.ui.fragments.GeneratorFragment
import com.expert.qrgenerator.ui.fragments.GoogleDocsFragment
import com.expert.qrgenerator.ui.fragments.GoogleFormsFragment
import com.expert.qrgenerator.ui.fragments.GoogleReviewFragment
import com.expert.qrgenerator.ui.fragments.GoogleSheetsFragment
import com.expert.qrgenerator.ui.fragments.InstagramFragment
import com.expert.qrgenerator.ui.fragments.LinkedinFragment
import com.expert.qrgenerator.ui.fragments.MapFragment
import com.expert.qrgenerator.ui.fragments.Office365Fragment
import com.expert.qrgenerator.ui.fragments.PaymentFragment
import com.expert.qrgenerator.ui.fragments.PaypalFragment
import com.expert.qrgenerator.ui.fragments.PhoneFragment
import com.expert.qrgenerator.ui.fragments.PlayMarketAppStoreFragment
import com.expert.qrgenerator.ui.fragments.RedditFragment
import com.expert.qrgenerator.ui.fragments.ScannerFragment
import com.expert.qrgenerator.ui.fragments.ShapedFragment
import com.expert.qrgenerator.ui.fragments.SmsFragment
import com.expert.qrgenerator.ui.fragments.SnapchatFragment
import com.expert.qrgenerator.ui.fragments.SocialMediaFragment
import com.expert.qrgenerator.ui.fragments.SpotifyFragment
import com.expert.qrgenerator.ui.fragments.StaticLinkFragment
import com.expert.qrgenerator.ui.fragments.TelegramFragment
import com.expert.qrgenerator.ui.fragments.TextFragment
import com.expert.qrgenerator.ui.fragments.TikTokFragment
import com.expert.qrgenerator.ui.fragments.TrackableFragment
import com.expert.qrgenerator.ui.fragments.TwitterFragment
import com.expert.qrgenerator.ui.fragments.UtmBuilderFragment
import com.expert.qrgenerator.ui.fragments.VCardFragment
import com.expert.qrgenerator.ui.fragments.WhatsappFragment
import com.expert.qrgenerator.ui.fragments.WifiFragment
import com.expert.qrgenerator.ui.fragments.YoutubeFragment
import com.expert.qrgenerator.utils.AppSettings
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.Constants.Companion.PRIVACY_POLICY_URL
import com.expert.qrgenerator.viewmodel.MainActivityViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.sheets.v4.Sheets
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener,
     ScannerFragment.ScannerInterface,OnFragmentReplaceListener {

    // Binding for ActivityMain layout
    private lateinit var binding: ActivityMainBinding

    // Encoded text data for internal use
    private var encodedTextData: String = " "

    // ViewModel for MainActivity
    private val viewModel: MainActivityViewModel by viewModels()

    // AppSettings instance for application settings
    private lateinit var appSettings: AppSettings

    // Google Sign-In client for authentication
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    // Drive and Sheets service instances for Google API access
    private var mService: Drive? = null
    private var sheetService: Sheets? = null

    // Firebase Auth instance for authentication
    private lateinit var auth: FirebaseAuth

    // Scopes for Google API access
    private val scopes = mutableListOf<String>()

    // HttpTransport and JsonFactory instances for network communication
    private val httpTransport: HttpTransport = NetHttpTransport()
    private val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()
    private val jacksonFactory: JsonFactory = JacksonFactory.getDefaultInstance()

    // User instance to hold current user data
    private var user: User? = null

    // Request login string for authentication
    private var requestLogin: String? = null

    // Fragment for scanning functionality
    private var scannerFragment: ScannerFragment? = null

    // Callback for login results
    private var callback: LoginCallback? = null

    // Context of the activity, initialized using lazy delegation
    private val context: Context by lazy { this }

    // Google Account Credential for accessing Google APIs
    var credential: GoogleAccountCredential? = null

    // Binding for ContentMain layout
    lateinit var contentBinding: ContentMainBinding

    private val REQUEST_CODE_GET_ACCOUNTS = 0

    private val fragments = listOf(
        TextFragment(),
        StaticLinkFragment(),
        ContactFragment(),
        WifiFragment(),
        PhoneFragment(),
        SmsFragment(),
        InstagramFragment(),
        WhatsappFragment(),
        MapFragment(),
        FacebookFragment(),
        YoutubeFragment(),
        TelegramFragment(),
        EmailFragment(),
        TikTokFragment(),
        GoogleFormsFragment(),
        TwitterFragment(),
        SnapchatFragment(),
        SpotifyFragment(),
        GoogleDocsFragment(),
        GoogleReviewFragment(),
        GoogleSheetsFragment(),
        PaymentFragment(),
        Office365Fragment(),
        ShapedFragment(),
        PaypalFragment(),
        EtsyFragment(),
        LinkedinFragment(),
        CryptoPaymentFragment(),
        CalendarFragment(),
        SocialMediaFragment(),
        RedditFragment(),
        PlayMarketAppStoreFragment(),
        VCardFragment(),
        TrackableFragment(),
        UtmBuilderFragment(),
        DynamicFragment()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Inflate the main activity layout and bind it to the ActivityMainBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Bind the content layout inside the main layout
        contentBinding = ContentMainBinding.bind(binding.includedLayout.root)

        // Configure StrictMode to permit all thread operations (not recommended for production)
//        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
//        StrictMode.setThreadPolicy(policy)

        // Initialize views, set up toolbar, and configure Google login parameters
        initViews()
        setUpToolbar()
//        getAccountsPermission()
        initializeGoogleLoginParameters()

        // Check if tips are enabled in app settings
//        if (appSettings.getBoolean(getString(R.string.key_tips))) {
//            // Retrieve the last shown tip timestamp
//            val lastTipTimestamp = appSettings.getLong("tt1")
//
//            // Check if the tip needs to be shown (if it hasn't been shown for over a day)
//            if (lastTipTimestamp == 0L || System.currentTimeMillis() - lastTipTimestamp > TimeUnit.DAYS.toMillis(1)) {
//                // Build and show the tooltip
//                SimpleTooltip.Builder(this)
//                    .anchorView(contentBinding.bottomNavigation)
//                    .text(getString(R.string.bottom_navigation_tip_text))
//                    .gravity(Gravity.TOP)
//                    .animated(true)
//                    .transparentOverlay(false)
//                    .onDismissListener { tooltip ->
//                        // Update the timestamp when the tooltip is dismissed
//                        appSettings.putLong("tt1", System.currentTimeMillis())
//
//                        // Show table select tip in the ScannerFragment
//                        val fragment = supportFragmentManager.findFragmentByTag("scanner") as? ScannerFragment
//                        fragment?.showTableSelectTip()
//                    }
//                    .build()
//                    .show()
//            }
//        }
    }


    // THIS FUNCTION WILL INITIALIZE ALL THE VIEWS AND REFERENCE OF OBJECTS
    private fun initViews() {

        appSettings = AppSettings(context)
        auth = Firebase.auth

        DataRepository.getChatGptApiKey()
        DataRepository.getAiPrompts()
        // Initialize fragments
        val scannerFragment = ScannerFragment()
        val generatorFragment = GeneratorFragment()

        // Set up history button click listener
//        contentBinding.historyBtn.setOnClickListener {
//            startActivity(Intent(context, BarcodeHistoryActivity::class.java))
//        }

        // Set up privacy policy view with clickable link
        binding.privacyPolicyView.apply {
            movementMethod = LinkMovementMethod.getInstance()
            paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener {
                binding.drawer.closeDrawer(GravityCompat.START)
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL))
                startActivity(browserIntent)
            }
        }

        // Set up bottom navigation item selection listener
        contentBinding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_scanner -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, scannerFragment, "scanner")
                        .addToBackStack("scanner")
                        .commit()
                }
                R.id.bottom_generator -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, generatorFragment, "generator")
                        .addToBackStack("generator")
                        .commit()
                }
                else -> false
            }
            true
        }

        // Handle initial fragment setup based on intent extras
//        if (intent?.getStringExtra("KEY") == "generator") {
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, GeneratorFragment(), "generator")
//                .addToBackStack("generator")
//                .commit()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ChooseTypeFragment(), "choose-type")
            .commit()

//        } else {
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, ScannerFragment(), "scanner")
//                .addToBackStack("scanner")
//                .commit()
//        }


    }


    private fun getAccountsPermission() {
        // Check if the GET_ACCOUNTS permission is already granted
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.GET_ACCOUNTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If permission is not granted, check if we need to show an explanation
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity,
                    Manifest.permission.GET_ACCOUNTS
                )
            ) {
                // Permission was denied previously, show rationale and initialize Google login parameters
                Log.e("Accounts", "Permission rationale needed")
                initializeGoogleLoginParameters()
            } else {
                // No rationale needed or this is the first time the permission is being requested
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.GET_ACCOUNTS),
                    REQUEST_CODE_GET_ACCOUNTS
                )
            }
        }
    }

    // THIS FUNCTION WILL RENDER THE ACTION BAR/TOOLBAR
    private fun setUpToolbar() {
        // Set the toolbar as the app's action bar
        setSupportActionBar(contentBinding.toolbar)

        // Configure the action bar with the app name as the title and enable the home button
        supportActionBar?.apply {
            title = getString(R.string.app_name)
            setDisplayHomeAsUpEnabled(true)
        }

        // Set the title text color of the toolbar
        contentBinding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.black))

        // Initialize and set up the ActionBarDrawerToggle for the navigation drawer
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawer,
            contentBinding.toolbar,
            R.string.navigation_drawer_open, // Use string resource for accessibility
            R.string.navigation_drawer_close // Use string resource for accessibility
        )
        binding.drawer.addDrawerListener(toggle)
        toggle.syncState() // Synchronize the state of the drawer toggle

        // Set up the navigation item selected listener
        binding.navigation.setNavigationItemSelectedListener(this)

        // Handle toolbar navigation click to open/close the navigation drawer
        contentBinding.toolbar.setNavigationOnClickListener {
            hideSoftKeyboard(this, binding.drawer)
            if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
                binding.drawer.closeDrawer(GravityCompat.START)
            } else {
                binding.drawer.openDrawer(GravityCompat.START)
            }
        }

        supportFragmentManager.addOnBackStackChangedListener {
            updateToolbar(toggle)
        }
    }

    private fun updateToolbar(toggle:ActionBarDrawerToggle) {
        val backStackEntryCount = supportFragmentManager.backStackEntryCount
        if (backStackEntryCount > 0) {
            // Show back arrow
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toggle.isDrawerIndicatorEnabled = false
            contentBinding.toolbar.setNavigationOnClickListener {
                hideKeyboard(context,this@MainActivity)
                onBackPressed()
            }
        } else {
            // Show hamburger menu
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            toggle.isDrawerIndicatorEnabled = true
            toggle.syncState()
            contentBinding.toolbar.setNavigationOnClickListener {
                binding.drawer.openDrawer(GravityCompat.START)
            }
        }
    }

    // THIS FUNCTION WILL INITIALIZE THE GOOGLE LOGIN PARAMETERS
    private fun initializeGoogleLoginParameters() {
        // Define the required scopes for Google Drive and Sheets API access
//        val scopes = mutableListOf(
//            DriveScopes.DRIVE_METADATA_READONLY,
//            SheetsScopes.SPREADSHEETS_READONLY,
//            SheetsScopes.DRIVE,
//            SheetsScopes.SPREADSHEETS,
//            DriveScopes.DRIVE,
//            DriveScopes.DRIVE_APPDATA
//        )

        // Build GoogleSignInOptions for authentication with the specified scopes
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
//            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
//            .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
//            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, signInOptions)

        // Get the last signed-in Google account
//        val acct: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
//        if (acct != null) {
//            // Initialize GoogleAccountCredential with the required scopes
//            credential = GoogleAccountCredential.usingOAuth2(applicationContext, scopes)
//                .setBackOff(ExponentialBackOff())
//                .setSelectedAccount(acct.account)
//
//            // Build the Drive service with the configured credentials
////            mService = Drive.Builder(httpTransport, jsonFactory, credential)
////                .setHttpRequestInitializer { request ->
////                    credential!!.initialize(request)
////                    request.connectTimeout = 300 * 60000  // Set connect timeout to 300 minutes
////                    request.readTimeout = 300 * 60000     // Set read timeout to 300 minutes
////                }
////                .setApplicationName(getString(R.string.app_name))
////                .build()
//
//            // Build the Sheets service with the configured credentials
////            try {
////                sheetService = Sheets.Builder(httpTransport, jacksonFactory, credential)
////                    .setApplicationName(getString(R.string.app_name))
////                    .build()
////            } catch (e: Exception) {
////                e.printStackTrace() // Log any exceptions encountered during Sheets service creation
////            }
//
//            // Save instances of Drive and Sheets services for later use
////            DriveService.saveDriveInstance(mService!!)
////            SheetService.saveGoogleSheetInstance(sheetService!!)
//            saveUserUpdatedDetail(acct, "last")
//        }

        // Check if the intent contains a request to login and start the login process if needed
        if (intent?.hasExtra("REQUEST") == true && intent.getStringExtra("REQUEST") == "login") {
            requestLogin = "login"
            startLogin()
        }
    }

    private fun saveUserUpdatedDetail(acct: GoogleSignInAccount?, isLastSignUser: String) {
        try {
            // Check if the account is not null and the display name is empty
            if (acct == null) {
                // Handle case where account is null, if needed
                return
            }

            // Proceed if displayName is not empty
            if (acct.displayName.isNullOrEmpty()) {
                startLogin()
            } else {
                // Extract user details from the account
                val user = User(
                    acct.displayName ?: "",
                    acct.givenName ?: "",
                    acct.familyName ?: "",
                    acct.email ?: "",
                    acct.id ?: "",
                    acct.photoUrl?.toString() ?: ""
                )

                // Save user details in app settings
                appSettings.putUser(Constants.user, user)
                Constants.userData = user

                // Notify the callback or restart the fragment
                callback?.onSuccess() ?: (supportFragmentManager.findFragmentById(R.id.fragment_container) as? ScannerFragment)?.restart()

                // Handle new user sign-in
//                if (isLastSignUser == "new") {
                    appSettings.putBoolean(Constants.isLogin, true)
                    Toast.makeText(
                        context,
                        getString(R.string.user_signin_success_text),
                        Toast.LENGTH_SHORT
                    ).show()
//                }

                // Start TablesActivity if requestLogin is "login"
                if (requestLogin == "login") {
                    startActivity(Intent(context, TablesActivity::class.java))
                }
            }
        } catch (e: Exception) {
            // Handle exceptions appropriately (e.g., log error)
            e.printStackTrace()
        }

        // Check user login status
        checkUserLoginStatus()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Helper function to start activities
        fun startActivity(activityClass: Class<*>) {
            startActivity(Intent(context, activityClass))
        }

        when (item.itemId) {
//            R.id.dynamic_links -> startActivity(DynamicQrActivity::class.java)
//
//            R.id.code_comparison -> startActivity(CodeComparisonActivity::class.java)

            R.id.sheets -> {
                if (appSettings.getBoolean(Constants.isLogin)) {
                    startActivity(SheetsActivity::class.java)
                } else {
                    startLogin()
                }
            }

            R.id.nav_setting -> startActivity(SettingsActivity::class.java)

            R.id.tables -> startActivity(TablesActivity::class.java)

            R.id.tables_data -> startActivity(TablesDataActivity::class.java)

            R.id.nav_rateUs -> rateUs(this)

            R.id.nav_recommend -> shareApp()

            R.id.nav_contact_support -> contactSupport(this)

            R.id.login -> startLogin()

            R.id.field_list -> startActivity(FieldListsActivity::class.java)

            R.id.profile -> startActivity(ProfileActivity::class.java)

            R.id.logout -> {
                MaterialAlertDialogBuilder(context)
                    .setTitle(getString(R.string.logout))
                    .setMessage(getString(R.string.logout_warning_text))
                    .setNegativeButton(getString(R.string.cancel_text)) { dialog, _ -> dialog.dismiss() }
                    .setPositiveButton(getString(R.string.logout)) { dialog, _ ->
                        startLoading(context)
                        signOut()
                    }
                    .create().show()
            }

            // Handle any other menu items
            else -> {
                // Optionally, log or handle unknown items here
            }
        }

        // Close the navigation drawer
        binding.drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        menu!!.findItem(R.id.create).isVisible = false
        menu.findItem(R.id.compare).isVisible = true
        menu.findItem(R.id.history).isVisible = true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Handle the home button press
                // Optionally, hide the soft keyboard if needed
                // hideSoftKeyboard(context, mDrawer)
                true
            }
            R.id.create->{
                startActivity(Intent(context, MainActivity::class.java)).apply {
                    finish()
                }
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

    private fun startLogin() {
        // Obtain the sign-in intent from the GoogleSignInClient
        val signInIntent = mGoogleSignInClient.signInIntent

        // Launch the sign-in activity
        googleLauncher.launch(signInIntent)
    }

    /**
     * Launches an intent to share the app with a predefined message and the app's Play Store link.
     */
    private fun shareApp() {
        // Create a new intent for sharing content
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            // Set the MIME type to plain text
            type = "text/plain"
            // Compose the share message with the app's Play Store URL
            putExtra(
                Intent.EXTRA_TEXT,
                "${getString(R.string.share_app_message)} https://play.google.com/store/apps/details?id=$packageName"
            )
        }

        // Start the activity with the sharing intent
        startActivity(Intent.createChooser(shareIntent, null))
    }

    private fun signOut() {
        // Revoke access from Google account
//        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this) { revokeTask ->
//            // Check if revoking access was successful
//            if (revokeTask.isSuccessful) {
                // Sign out from Google account
                mGoogleSignInClient.signOut().addOnCompleteListener(this) { signOutTask ->
                    if (signOutTask.isSuccessful) {
                        dismiss()
                        // Clear local settings and user data
                        appSettings.remove(Constants.isLogin)
                        appSettings.remove(Constants.user)
                        Constants.userData = null
//                        Constants.sheetService = null
//                        Constants.mService = null

                        // Show success message
                        Toast.makeText(context, getString(R.string.logout_success_text), Toast.LENGTH_SHORT).show()

                        // Restart ScannerFragment
                        val scannerFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? ScannerFragment
                        scannerFragment?.restart()

                        // Check if user is logged in or not
                        checkUserLoginStatus()
                    } else {
                        // Handle Google sign-out failure
                        dismiss()
                        Toast.makeText(context, getString(R.string.logout_failure_text), Toast.LENGTH_SHORT).show()
                    }
                }
//            } else {
//                // Handle Google access revoke failure
//                Toast.makeText(context, getString(R.string.logout_failure_text), Toast.LENGTH_SHORT).show()
//            }
//        }
    }

    // THIS GOOGLE LAUNCHER WILL HANDLE RESULT
    private var googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // Check if the result is successful
            if (result.resultCode == Activity.RESULT_OK) {

                // Retrieve the GoogleSignInAccount from the result intent
//                GoogleSignIn.getSignedInAccountFromIntent(result.data)
//                    .addOnSuccessListener { googleSignInAccount ->
//                        // Ensure the GoogleSignInAccount is not null
//                        googleSignInAccount?.let {
//                            // Initialize GoogleAccountCredential with OAuth2
//                            credential = GoogleAccountCredential.usingOAuth2(
//                                context,
//                                scopes
//                            ).apply {
//                                // Configure the backoff strategy
//                                setBackOff(ExponentialBackOff())
//                                // Set the selected Google account
//                                setSelectedAccount(it.account)
//                            }
//
//                            // Build the Drive service
////                            mService = Drive.Builder(
////                                httpTransport, jsonFactory, credential
////                            ).setHttpRequestInitializer { request ->
////                                credential!!.initialize(request)
////                                // Set connect and read timeouts to 300 minutes
////                                request.connectTimeout = 300 * 60 * 1000  // 300 minutes connect timeout
////                                request.readTimeout = 300 * 60 * 1000     // 300 minutes read timeout
////                            }
////                                .setApplicationName(getString(R.string.app_name))
////                                .build()
//
//                            // Build the Sheets service
////                            try {
////                                sheetService = Sheets.Builder(
////                                    httpTransport,
////                                    jacksonFactory,
////                                    credential
////                                )
////                                    .setApplicationName(getString(R.string.app_name))
////                                    .build()
////                            } catch (e: Exception) {
////                                // Print the exception stack trace if building Sheets service fails
////                                e.printStackTrace()
////                            }
//
//                            // Save the instances for later use
////                            DriveService.saveDriveInstance(mService!!)
////                            SheetService.saveGoogleSheetInstance(sheetService!!)
//
//                            // Handle the sign-in result
//                            handleSignInResult(it)
//                        }
//                    }
//                    .addOnFailureListener { exception ->
//                        // Show an alert if the sign-in fails
//                        showAlert(context, exception.localizedMessage ?: "Unknown error occurred")
//                    }
                val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)
            }
        }

    // Handle the result of the sign-in
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            account?.let {
                handleSignInResult(account)
                //Toast.makeText(this, "Signed in as: ${it.displayName}", Toast.LENGTH_SHORT).show()
                // Proceed with further logic (e.g., send token to server, etc.)
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Sign in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSignInResult(acct: GoogleSignInAccount) {
        try {
            // Attempt to save user details with the provided GoogleSignInAccount.
            saveUserUpdatedDetail(acct, "new")
        } catch (e: ApiException) {
            // Handle the ApiException, which indicates the reason for the failure.
            // You can log the exception or display an appropriate message to the user.
            Log.e("SignInError", "Google Sign-In failed with status code: ${e.statusCode}", e)
            // Optionally, display a user-friendly message
//             showError("Sign-in failed. Please try again.")
        }
    }



    override fun onBackPressed() {
        // Check if the drawer is open and close it if necessary
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawer(GravityCompat.START)
            return
        }

        // Find the ScannerFragment by its tag
//        val scannerFragment = supportFragmentManager.findFragmentByTag("choose-type")

        // Check if the ScannerFragment is visible and finish the activity if it is
//        if (scannerFragment != null && scannerFragment.isVisible) {
//            finish()
//            return
//        }

        // If none of the above conditions were met, replace the current fragment with ScannerFragment
//        contentBinding.bottomNavigation.selectedItemId = R.id.bottom_scanner
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container, ChooseTypeFragment(), "choose-type")
//            .addToBackStack("choose-type")
//            .commit()
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            finish()
        }
    }


    // THIS METHOD WILL CALL AFTER SELECT THE QR TYPE WITH INPUT DATA
//    override fun onTypeSelected(data: String, position: Int, type: String) {
//        var url = ""
//        val hashMap = hashMapOf<String, String>()
//        hashMap["login"] = "qrmagicapp"
//        hashMap["qrId"] = System.currentTimeMillis().toString()
//        hashMap["userType"] = "free"
//        if (position == 2) {
//
//
//            hashMap["userUrl"] = data
//
//            startLoading(context)
//            lifecycleScope.launch{
//                viewModel.createDynamicQrCode(hashMap)
//            }
//            viewModel.dynamicQrCodeResponse.observe(this, Observer { response ->
//                dismiss()
//                if (response != null) {
//                    url = response.get("generatedUrl").asString
//                    url = if (url.contains(":8990")) {
//                        url.replace(":8990", "")
//                    } else {
//                        url
//                    }
//                    val qrHistory = CodeHistory(
//                        hashMap["login"]!!,
//                        hashMap["qrId"]!!,
//                        hashMap["userUrl"]!!,
//                        type,
//                        hashMap["userType"]!!,
//                        "qr",
//                        "create",
//                        "",
//                        "1",
//                        url,
//                        System.currentTimeMillis().toString(),
//                        ""
//                    )
//
//                    val intent = Intent(context, DesignActivity::class.java)
//                    intent.putExtra("ENCODED_TEXT", url)
//                    intent.putExtra("QR_HISTORY", qrHistory)
//                    startActivity(intent)
//                } else {
//                    showAlert(context, getString(R.string.something_wrong_error))
//                }
//            })
//        } else {
//            encodedTextData = data
//
//            val qrHistory = CodeHistory(
//                hashMap["login"]!!,
//                hashMap["qrId"]!!,
//                encodedTextData,
//                type,
//                hashMap["userType"]!!,
//                "qr",
//                "create",
//                "",
//                "0",
//                "",
//                System.currentTimeMillis().toString(),
//                ""
//            )
//            val intent = Intent(context, DesignActivity::class.java)
//            intent.putExtra("ENCODED_TEXT", encodedTextData)
//            intent.putExtra("QR_HISTORY", qrHistory)
//            startActivity(intent)
//        }
//
//    } Ok good. now your branch has my commit with analytics + some changes you had before. Now

    override fun onResume() {
        super.onResume()
        checkUserLoginStatus()
        if(Constants.isOpenVcardScreen){
            Constants.isOpenVcardScreen = false
            replaceFragment(32)
        }

//        try {//now make the same try catch with all logCustomEvent that we have total 6 places then
//            logCustomEvent(eventName = "screen_main_opened")
//        }catch (exception: Exception){
//            Log.e("errors","logging error: $exception")
//        }
        logMainEvent()

    }

    private fun logMainEvent() {
        val mainAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        // Log the custom event
        mainAnalytics.logEvent("screen_main_opened", bundle)
    }

    /**
     * Checks the user's login status and updates the visibility of menu items accordingly.
     */
    private fun checkUserLoginStatus() {
        // Retrieve the user's login status from app settings
        val isLoggedIn = appSettings.getBoolean(Constants.isLogin)

        // Update menu item visibility based on login status
        binding.navigation.menu.apply {
            // Define menu item visibility for logged-in users
            findItem(R.id.login).isVisible = !isLoggedIn
            findItem(R.id.logout).isVisible = isLoggedIn
            findItem(R.id.profile).isVisible = isLoggedIn
//            findItem(R.id.tables).isVisible = isLoggedIn
//            findItem(R.id.field_list).isVisible = isLoggedIn
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Check if the request code matches the expected one
        when (requestCode) {
            // Case for Google login permissions
            0 -> {
                // Check if the permission request was granted
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Initialize Google login parameters
                    initializeGoogleLoginParameters()
                } else {
                    // Handle the case where permission is denied
                    // For example, show a message to the user or disable related functionality
                }
            }
            else -> {
                // Handle unexpected request codes if necessary
            }
        }
    }


    override fun login(callback: LoginCallback) {
        // Set the provided callback for later use
        this.callback = callback

        // Start the login process
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

    override fun replaceFragment(position: Int) {
//        if(position == 33){
//          GeneratorManager.generateQRCode(context,"${Constants.BASE_URL}track.php?id=${System.currentTimeMillis()}","trackable")
//        }
//        else{
            val fragment = fragments[position]
            val isLoggedIn = appSettings.getBoolean(Constants.isLogin)
            if (fragment is VCardFragment && !isLoggedIn){
                startLogin()
            }
            else {
                val fragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragment_container, fragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            }
//        }

    }

}