package com.expert.qrgenerator.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.ActivityPostSheetDataBinding
import com.expert.qrgenerator.singleton.DriveService
import com.expert.qrgenerator.singleton.SheetService
import com.expert.qrgenerator.utils.ImageManager
import com.expert.qrgenerator.utils.VolleySingleton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.services.drive.model.File
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.ValueRange
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.*


@AndroidEntryPoint
class PostSheetDataActivity : BaseActivity() {

    // Declare the binding object for ActivityPostSheetDataBinding
    private lateinit var binding: ActivityPostSheetDataBinding

    // Context of the activity, initialized using lazy delegation
    private val context: Context by lazy { this }

    // Declare a list to hold values of any type
    var values: List<Any>? = null

    // Initialize an array to hold string values with a default size of 1000
// Consider using a more dynamic collection like ArrayList if you need to adjust the size
    var valuesString = Array<String?>(1000) { null }

    // Use a mutable list to keep track of EditText views
    var allEditTexts = mutableListOf<EditText>()

    // Optional variable to hold an ID as a string
    var id: String? = null

    // Variable to store the name of the sheet
    var sheetName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize ViewBinding
        binding = ActivityPostSheetDataBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Set up click listener for choosing a file
        binding.chooseFile.setOnClickListener { getImageFromLocalStorage() }

        // Set up click listener for submitting the file
        binding.submit.setOnClickListener {
            // Check if the file path field is not empty before sending the request
            val filePath = binding.filePath.text.toString()
            if (filePath.isNotEmpty()) {
                sendRequest()
            } else {
                // Show a toast message if no file is attached
                Toast.makeText(applicationContext, "Attach a file", Toast.LENGTH_LONG).show()
            }
        }

        // Retrieve the ID from the intent extras
        id = intent.getStringExtra("id")

        // Ensure ID is not null before making requests
        id?.let {
            getSheetName(it)
            fetchSheetColumns()
        } ?: run {
            // Handle the case where ID is null (optional, add specific logic as needed)
            Toast.makeText(this, "Invalid ID", Toast.LENGTH_SHORT).show()
        }
    }


    private fun sendRequest() {
        // Convert EditText values to a list of strings
        val valuesString = Array(values!!.size) { index -> allEditTexts[index].text.toString() }

        // Start loading indicator
        startLoading(context)

        // Launch a coroutine to handle background operations
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Prepare file metadata and content
                val fileMetadata = File().apply {
                    name = "Image_${System.currentTimeMillis()}.jpg"
                }
                val filePath = File(binding.filePath.text.toString())
                val mediaContent = FileContent("image/jpeg", filePath)

                // Upload file to Google Drive
                val file = DriveService.getDriveInstance()!!
                    .files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute()

                // Log the file ID
                Log.e("File ID: ", file.id)

                // Prepare data to be sent in the request
                val url = "https://script.google.com/macros/s/AKfycbw8aAiqlJbquiRYbiYyZOh36IC_0DEtp18qNkowZvltCJ-BEdbRYola2Dv1wLxAFF9X/exec"
                val values_JSON = JSONArray().apply {
                    valuesString.forEach { put(it) }
                }

                // Create and send a Volley request
                val stringRequest = object : StringRequest(
                    Method.POST,
                    url,
                    Response.Listener<String?> { response ->
                        CoroutineScope(Dispatchers.Main).launch {
                            // Dismiss loading indicator
                            dismiss()

                            // Handle response
                            if (response!!.contains("success", ignoreCase = true)) {
                                Toast.makeText(applicationContext, "Data has been inserted successfully", Toast.LENGTH_LONG).show()
                            } else {
                                showPermissionDeniedDialog()
                            }
                        }
                    },
                    Response.ErrorListener { error ->
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, error!!.toString(), Toast.LENGTH_SHORT).show()
                            dismiss()
                        }
                    }
                ) {
                    override fun getParams(): Map<String, String> {
                        return mapOf(
                            "sheetName" to sheetName,
                            "number" to "${values!!.size}",
                            "id" to "$id",
                            "value" to values_JSON.toString(),
                            "drive" to "https://drive.google.com/file/d/${file.id}/view?usp=sharing"
                        )
                    }
                }

                // Add request to Volley request queue
                VolleySingleton.getInstance(context).addToRequestQueue(stringRequest)

            } catch (e: UserRecoverableAuthIOException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Helper function to show permission denied dialog
    private fun showPermissionDeniedDialog() {
        val permissionDeniedLayout = LayoutInflater.from(context)
            .inflate(R.layout.spreadsheet_permission_failed_dialog, null)
        val builder = MaterialAlertDialogBuilder(context).apply {
            setCancelable(false)
            setView(permissionDeniedLayout)
            setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
        }
        builder.create().show()
    }


    private fun getSheetName(id: String): String? {
        return try {
            // Execute the network request to get the spreadsheet details.
            val response: Spreadsheet = SheetService.getInstance()?.spreadsheets()
                ?.get(id)
                ?.setIncludeGridData(false)
                ?.execute() ?: return null

            // Extract and return the name of the first sheet.
            response.sheets.firstOrNull()?.properties?.title
        } catch (e: IOException) {
            // Handle any exceptions that occur during the network request.
            e.printStackTrace() // Log the exception for debugging.
            null // Return null if an exception occurs.
        }
    }

    // THIS FUNCTION WILL CALL THE IMAGE INTENT
    private fun getImageFromLocalStorage() {
        // Create an intent to pick an image from the device's local storage
        val fileIntent = Intent(Intent.ACTION_PICK).apply {
            // Set the type of file to be picked as an image
            type = "image/*"
        }

        // Launch the intent using resultLauncher to handle the result
        resultLauncher.launch(fileIntent)
    }

    // THIS RESULT LAUNCHER WILL CALL THE ACTION PICK FROM FILES FOR BACKGROUND AND LOGO IMAGE
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // Check if the result code indicates a successful result
            if (result.resultCode == Activity.RESULT_OK) {

                // Retrieve the data from the result intent
                val data: Intent? = result.data

                // Ensure that data and its Uri are not null
                data?.data?.let { uri ->

                    // Get the real file path from the Uri
                    val paths = ImageManager.getRealPathFromUri(this, uri)

                    // Update the UI with the obtained file path
                    binding.filePath.text = paths
                }
            }
        }


    /**
     * Fetches column data from a Google Sheets spreadsheet and updates the UI.
     * This function runs on a background thread and updates the UI on the main thread.
     */
    private fun fetchSheetColumns() {
        // Launch a coroutine on the IO dispatcher for network operations
        CoroutineScope(Dispatchers.IO).launch {
            // Define the range of columns to fetch from the spreadsheet
            val range = "A:Z"
            var response: ValueRange? = null

            try {
                // Create a request to fetch the values from the specified range
                val request = SheetService.getInstance()?.spreadsheets()?.values()?.get(id, range)
                // Execute the request and store the response
                response = request?.execute()
            } catch (e: UserRecoverableAuthIOException) {
                // Handle the case where user authorization is required
                googleLauncher.launch(e.intent)
            } catch (e: IOException) {
                // Print stack trace for general IO errors
                e.printStackTrace()
            }

            // If the response is not null, proceed to update the UI
            if (response != null) {
                // Extract column values from the response
                values = response.getValues().firstOrNull() ?: emptyList()

                // Switch to the main dispatcher to update the UI
                withContext(Dispatchers.Main) {
                    dynamicallyGenerateEdittext()
                }
            }
        }
    }


    // THIS GOOGLE LAUNCHER WILL HANDLE RESULT
    // Register an activity result launcher for Google Sign-In
    private var googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // Check if the result code indicates a successful sign-in
            if (result.resultCode == Activity.RESULT_OK) {
                // Fetch sheet columns after successful sign-in
                fetchSheetColumns()

                // Get the Google Sign-In account information from the result intent
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

                try {
                    // Google Sign-In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    // Uncomment the line below to authenticate with Firebase
                    // firebaseAuthWithGoogle(account)

                } catch (e: ApiException) {
                    // Google Sign-In failed, log the error for debugging
                    Log.w("TAG", "Google sign-in failed", e)
                }
            }
        }

    private fun dynamicallyGenerateEdittext() {
        // Try-catch block to handle any exceptions that may occur
        try {
            // Access the parent LinearLayout from the binding
            val parentLinear = binding.parentLinear as LinearLayout

            // Create a new LinearLayout to hold the EditText views, with vertical orientation
            val verticalLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }

            // Iterate through the values to create EditText views dynamically
            values?.forEachIndexed { index, value ->
                // Create a new EditText view
                val editText = EditText(this).apply {
                    id = index  // Set unique ID for each EditText
                    setBackgroundResource(R.drawable.editext_back)  // Set background drawable
                    setPadding(20, 0, 0, 0)  // Set padding
                    hint = value.toString()  // Set hint text
                    setTextColor(resources.getColor(R.color.white))  // Set text color
                }

                // Define layout parameters for the EditText
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,  // Match parent width
                    140  // Fixed height
                ).apply {
                    setMargins(10, 10, 10, 10)  // Set margins
                }

                // Add the EditText view to the vertical LinearLayout
                verticalLayout.addView(editText, layoutParams)
            }

            // Add the vertical LinearLayout to the parent LinearLayout
            parentLinear.addView(verticalLayout)

        } catch (e: Exception) {
            // Log the error and show a toast message if an exception occurs
            Log.e("Sheet Mismatch", e.message.orEmpty())
            Toast.makeText(this, "Sheet Format Mismatch", Toast.LENGTH_LONG).show()
        }
    }

}