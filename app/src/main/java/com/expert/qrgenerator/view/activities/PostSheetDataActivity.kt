package com.expert.qrgenerator.view.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import com.expert.qrgenerator.R
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.ImageManager
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection

class PostSheetDataActivity : BaseActivity() {

    private lateinit var context: Context
    private lateinit var Submit: Button
    private lateinit var chooseFile: Button
    private lateinit var path: TextView
    var values: List<Any>? = null
    var values_String = arrayOfNulls<String>(1000)
    var allEds = mutableListOf<EditText>()
    var id: String? = null
    private var service:Drive?=null
    private var mSheetService:Sheets?=null

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
        if (Constants.mService != null) {
            service = Constants.mService!!
        }

        if (Constants.sheetService != null) {
            mSheetService = Constants.sheetService!!
        }

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
                val file: File = service!!.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()
                Log.e("File ID: ", file.id)
                val url =
                    URL("https://script.google.com/macros/s/AKfycbw6_wNMBRdgxbfc49OcoF_t-ZwKjL5Idi9-xlD1tUXB1joobMLz7CmXDe3fL3lyKFd79g/exec")

                val postDataParams = JSONObject()
                val values_JSON = JSONArray()
                for (j in values!!.indices) values_JSON.put(values_String[j])
                postDataParams.put("number", values!!.size)
                postDataParams.put("id", id)
                postDataParams.put("value", values_JSON)
                postDataParams.put(
                    "drive",
                    "https://drive.google.com/file/d/" + file.id + "/view?usp=sharing"
                )

                val conn = url.openConnection() as HttpURLConnection
                conn.readTimeout = 15000
                conn.connectTimeout = 15000
                conn.requestMethod = "POST"
                conn.doInput = true
                conn.doOutput = true
                val os = conn.outputStream
                val writer = BufferedWriter(
                    OutputStreamWriter(os, "UTF-8")
                )
                writer.write(getPostDataString(postDataParams))
                writer.flush()
                writer.close()
                os.close()
                val responseCode = conn.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    val input = BufferedReader(InputStreamReader(conn.inputStream))
                    val sb = StringBuffer("")
                    var line: String? = ""
                    while (input.readLine().also { line = it } != null) {
                        sb.append(line)
                        break
                    }
                    input.close()
                    sb.toString()

                    CoroutineScope(Dispatchers.Main).launch {
                        dismiss()
                        Toast.makeText(
                            applicationContext,
                            "Data has been inserted successfully",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    //String("false : $responseCode")
                }
            } catch (e: UserRecoverableAuthIOException) {
                e.printStackTrace()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                //String("Exception: " + e.message)
            }
        }
    }

    @Throws(java.lang.Exception::class)
    fun getPostDataString(params: JSONObject): String {
        val result = StringBuilder()
        var first = true
        val itr = params.keys()
        while (itr.hasNext()) {
            val key = itr.next()
            val value = params[key]
            if (first) first = false else result.append("&")
            result.append(URLEncoder.encode(key, "UTF-8"))
            result.append("=")
            result.append(URLEncoder.encode(value.toString(), "UTF-8"))
        }
        return result.toString()
    }

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
                val request = mSheetService!!.spreadsheets().values().get(id, range)
                response = request.execute()
            } catch (e: UserRecoverableAuthIOException) {
                Log.d("TEST199",e.localizedMessage!!)
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