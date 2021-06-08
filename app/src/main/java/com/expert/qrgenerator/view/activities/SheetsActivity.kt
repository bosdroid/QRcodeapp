package com.expert.qrgenerator.view.activities

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.SheetAdapter
import com.expert.qrgenerator.model.Sheet
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SheetsActivity : BaseActivity(), SheetAdapter.OnItemClickListener {

    private var account: GoogleSignInAccount? = null
    private var signInOptions: GoogleSignInOptions? = null
    private lateinit var context: Context
    private lateinit var toolbar: Toolbar
    private lateinit var sheetsRecyclerview: RecyclerView
    private var sheetsList = mutableListOf<Sheet>()
    var credential: GoogleAccountCredential? = null
    var client: GoogleSignInClient? = null
    var mService: Drive? = null
    private val scopes = mutableListOf<String>()
    private val transport: HttpTransport? = AndroidHttp.newCompatibleTransport()
    private val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()
    private lateinit var adapter: SheetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sheets)

        initViews()
        setUpToolbar()
        requestSignIn()

        try {
            getAllSheets()
        } catch (availabilityException: GooglePlayServicesAvailabilityIOException) {
            Log.e("Play Services", "GPS unavailable")
        } catch (userRecoverableException: UserRecoverableAuthIOException) {
            Log.e("Recoverable Auth", "user recoverable")
            googleLauncher.launch(userRecoverableException.intent)
        } catch (e: Exception) {
            Log.e("gd", e.message + "----")
        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.sheets)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.black))
    }

    // THIS FUNCTION WILL HANDLE THE ON BACK ARROW CLICK EVENT
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun requestSignIn() {
        scopes.add(DriveScopes.DRIVE_METADATA_READONLY)
       try {
           signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
               .requestIdToken(getString(R.string.default_web_client_id))
               .requestEmail()
               .build()
           client = GoogleSignIn.getClient(this, signInOptions!!)

           account = GoogleSignIn.getLastSignedInAccount(this)

           if (account != null){
               credential = GoogleAccountCredential.usingOAuth2(
                   applicationContext, scopes
               )
                   .setBackOff(ExponentialBackOff())
                   .setSelectedAccount(Account(account!!.email, context.packageName))
               mService = Drive.Builder(
                   transport, jsonFactory, credential
               )
                   .setApplicationName(getString(R.string.app_name))
                   .build()
           }
           else{
               val signInIntent = client!!.signInIntent
               googleLauncher.launch(signInIntent)
           }

       }catch (ex: Exception){
           Log.e("Signing In", ex.localizedMessage!!)
       }

    }

    // THIS GOOGLE LAUNCHER WILL HANDLE RESULT
    private var googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                chooseAccount()
        }

    private fun chooseAccount() {
        chooseAccount.launch(credential!!.newChooseAccountIntent())
    }

    private var chooseAccount =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null && result.data!!.extras != null) {
                val accountName: String? = result.data!!.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)!!
                if (accountName != null) {
                    credential!!.selectedAccountName = accountName
                    dismiss()
                    getAllSheets()
                }

            } else if (result.resultCode == RESULT_CANCELED) {
                Log.e("gd", "in cancelled")
                dismiss()
            }
        }

    private fun initViews() {
        context = this

        toolbar = findViewById(R.id.toolbar)
        sheetsRecyclerview = findViewById(R.id.sheets_recyclerview)
        sheetsRecyclerview.layoutManager = LinearLayoutManager(context)
        sheetsRecyclerview.hasFixedSize()
        adapter = SheetAdapter(context, sheetsList as ArrayList<Sheet>)
        sheetsRecyclerview.adapter = adapter
        adapter.setOnItemClickListener(this)

    }


    private fun getAllSheets() {
        startLoading(context)

        CoroutineScope(Dispatchers.IO).launch {
              try {
                  val result: FileList = mService!!.files().list()
                      .setQ("mimeType='application/vnd.google-apps.spreadsheet'")
                      .execute()

                  val files = result.files

                  if (files != null) {
                      for (file in files) {
                          sheetsList.add(Sheet(file.id, file.name))
                      }

                      CoroutineScope(Dispatchers.Main).launch {
                          if (sheetsList.isNotEmpty()) {
                              adapter.notifyDataSetChanged()
                          }
                          dismiss()
                      }
                  }
              }
              catch (userRecoverableException:UserRecoverableAuthIOException){
                  googleLauncher.launch(userRecoverableException.intent)
              }
        }
    }

    override fun onItemClick(position: Int) {
        val sheet = sheetsList[position]
        showAlert(context, "${sheet.name}:${sheet.id}")
    }
}