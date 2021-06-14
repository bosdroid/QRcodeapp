package com.expert.qrgenerator.view.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.expert.qrgenerator.R
import com.expert.qrgenerator.singleton.DriveService
import com.expert.qrgenerator.singleton.SheetService
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.ImageManager
import com.expert.qrgenerator.utils.VolleySingleton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.URLEncoder
import java.util.*


class PostSheetDataActivity : BaseActivity() {

    private lateinit var context: Context
    private lateinit var Submit: Button
    private lateinit var chooseFile: Button
    private lateinit var path: TextView
    var values: List<Any>? = null
    var values_String = arrayOfNulls<String>(1000)
    var allEds = mutableListOf<EditText>()
    var id: String? = null
//    private var service: Drive? = null
//    private var mSheetService: Sheets? = null
    var sheetName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_sheet_data)

        context = this
        Submit = findViewById(R.id.submit)
        path = findViewById(R.id.filePath)
        chooseFile = findViewById(R.id.chooseFile)

        chooseFile.setOnClickListener { getImageFromLocalStorage() }

        Submit.setOnClickListener {
            if (path.text.toString().isNotEmpty()) {
                sendRequest()
            } else {
                Toast.makeText(applicationContext, "Attach a file", Toast.LENGTH_LONG).show()
            }
        }

        id = intent.getStringExtra("id")
//        if (Constants.mService != null) {
//            service = Constants.mService!!
//        }
//
//        if (Constants.sheetService != null) {
//            mSheetService = Constants.sheetService!!
//        }
        getSheetName(id!!)
        fetchSheetColumns()
    }

    private fun sendRequest() {
        for (j in values!!.indices) {
            values_String[j] = allEds[j].text.toString()
        }
        startLoading(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fileMetadata = File()
                fileMetadata.name = "Image_${System.currentTimeMillis()}.jpg"
                val filePath = File(path.text.toString())
                val mediaContent = FileContent("image/jpeg", filePath)
                val file: File = DriveService.instance!!.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
                Log.e("File ID: ", file.id)
                val url = "https://script.google.com/macros/s/AKfycbw8aAiqlJbquiRYbiYyZOh36IC_0DEtp18qNkowZvltCJ-BEdbRYola2Dv1wLxAFF9X/exec"//URL("https://script.google.com/macros/s/AKfycbzTa84_2VmwTN2usH6MqzfiD7b4aNSYHAKy6k_vniR-uy5a_W9N/exec")
                val values_JSON = JSONArray()
                for (j in values!!.indices) values_JSON.put(values_String[j])

                val sr: StringRequest = object : StringRequest(
                    Method.POST,
                    url,
                    object : Response.Listener<String?> {
                        override fun onResponse(response: String?) {
                            CoroutineScope(Dispatchers.Main).launch {
                                dismiss()
                                if (response!!.toLowerCase(Locale.ENGLISH).contains("success")) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Data has been inserted successfully",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    val permissionDeniedLayout = LayoutInflater.from(context)
                                        .inflate(R.layout.spreadsheet_permission_failed_dialog, null)
                                    val builder = MaterialAlertDialogBuilder(context)
                                    builder.setCancelable(false)
                                    builder.setView(permissionDeniedLayout)
                                    builder.setPositiveButton("Ok") { dialog, which ->
                                        dialog.dismiss()
                                    }
                                    val alert = builder.create()
                                    alert.show()
                                }

                            }
                        }
                    },
                    object : Response.ErrorListener {
                        override fun onErrorResponse(error: VolleyError?) {
                            Toast.makeText(context,error!!.toString(),Toast.LENGTH_SHORT).show()
                            dismiss()
                        }
                    }) {
                    override fun getParams(): Map<String, String> {
                        val params: MutableMap<String, String> = HashMap()
                        params["sheetName"] = sheetName
                        params["number"] = "${values!!.size}"
                        params["id"] = "$id"
                        params["value"] = "${values_JSON}"
                        params["drive"] = "https://drive.google.com/file/d/" + file.id + "/view?usp=sharing"
                        return params
                    }

                }
                VolleySingleton(context).addToRequestQueue(sr)

            } catch (e: UserRecoverableAuthIOException) {
                e.printStackTrace()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getSheetName(id: String){
        CoroutineScope(Dispatchers.IO).launch {
            val response1: Spreadsheet =
                SheetService.instance!!.spreadsheets().get(id).setIncludeGridData(false)
                    .execute()
            sheetName = response1.sheets[0].properties.title
        }
    }

//    @Throws(java.lang.Exception::class)
//    fun getPostDataString(params: JSONObject): String {
//        val result = StringBuilder()
//        var first = true
//        val itr = params.keys()
//        while (itr.hasNext()) {
//            val key = itr.next()
//            val value = params[key]
//            if (first) first = false else result.append("&")
//            result.append(URLEncoder.encode(key, "UTF-8"))
//            result.append("=")
//            result.append(URLEncoder.encode(value.toString(), "UTF-8"))
//        }
//        return result.toString()
//    }

    // THIS FUNCTION WILL CALL THE IMAGE INTENT
    private fun getImageFromLocalStorage() {
        val fileIntent = Intent(Intent.ACTION_PICK)
        fileIntent.type = "image/*"
        resultLauncher.launch(fileIntent)
    }

    // THIS RESULT LAUNCHER WILL CALL THE ACTION PICK FROM FILES FOR BACKGROUND AND LOGO IMAGE
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // THIS LINE OF CODE WILL CHECK THE IMAGE HAS BEEN SELECTED OR NOT
            if (result.resultCode == Activity.RESULT_OK) {

                val data: Intent? = result.data

                //couponHeaderImage = ImageManager.convertImageToBase64(context, data.data!!)
                val paths = ImageManager.getRealPathFromUri(this, data!!.data!!)
                path.text = paths
            }
        }

    private fun fetchSheetColumns() {
        CoroutineScope(Dispatchers.IO).launch {
            val range = "A:Z"
            var response: ValueRange? = null
            try {
                val request = SheetService.instance!!.spreadsheets().values().get(id, range)
                response = request.execute()
            } catch (e: UserRecoverableAuthIOException) {
                googleLauncher.launch(e.intent)
                //Toast.makeText(getApplicationContext(), "Can't Fetch columns of your sheet", Toast.LENGTH_LONG).show();
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (response != null) {
                values = response.getValues()[0]
                CoroutineScope(Dispatchers.Main).launch {
                    dynamicallyGenerateEditext()
                }
            }
        }
    }

    // THIS GOOGLE LAUNCHER WILL HANDLE RESULT
    private var googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                    fetchSheetColumns()
//                val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(
//                    result.data
//                )
//                handleSignInResult(task)
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    //firebaseAuthWithGoogle(account)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.w("TAG", "Google sign in failed", e)
                }
            }
        }


    private fun dynamicallyGenerateEditext() {
        try {
            val parentLinear = findViewById<View>(R.id.parentLinear) as LinearLayout
            val l = LinearLayout(this)
            l.orientation = LinearLayout.VERTICAL
            for (j in values!!.indices) {
                val et = EditText(this)
                val lp = LinearLayout.LayoutParams(800, 140)
                lp.setMargins(10, 10, 10, 10)
                et.id = j
                allEds.add(et)
                et.setBackgroundResource(R.drawable.editext_back)
                et.setPadding(20, 0, 0, 0)
                et.hint = values!![j].toString()
                et.setTextColor(resources.getColor(R.color.white))
                l.addView(et, lp)
            }
            parentLinear.addView(l)
        } catch (e: Exception) {
            Log.e("Sheet Mismatch", e.message!!)
            Toast.makeText(this, "Sheet Format Mismatch", Toast.LENGTH_LONG).show()
        }
    }
}