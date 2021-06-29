package com.expert.qrgenerator.view.activities

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.DynamicQrAdapter
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.QREntity
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.QRGenerator
import com.expert.qrgenerator.viewmodel.DynamicQrViewModel
import com.expert.qrgenerator.viewmodel.MainActivityViewModel
import com.expert.qrgenerator.viewmodelfactory.ViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView

class DynamicQrActivity : BaseActivity(), DynamicQrAdapter.OnItemClickListener {

    private lateinit var toolbar: Toolbar
    private lateinit var context: Context
    private lateinit var dynamicQrRecyclerView: RecyclerView
    private lateinit var adapter: DynamicQrAdapter
    private var dynamicQrList = mutableListOf<CodeHistory>()
    private lateinit var emptyView: MaterialTextView
    private lateinit var appViewModel: AppViewModel
    private lateinit var viewModel: DynamicQrViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dynamic_qr)

        initViews()
        setUpToolbar()
        displayDynamicQrCodes()

    }

    // THIS FUNCTION WILL INITIALIZE ALL THE VIEWS AND REFERENCE OF OBJECTS
    private fun initViews() {
        context = this
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(DynamicQrViewModel()).createFor()
        )[DynamicQrViewModel::class.java]
        appViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(this.application)
        ).get(AppViewModel::class.java)
        toolbar = findViewById(R.id.toolbar)
        emptyView = findViewById(R.id.emptyView)
        dynamicQrRecyclerView = findViewById(R.id.dynamic_qr_recyclerview)
        dynamicQrRecyclerView.layoutManager = LinearLayoutManager(context)
        dynamicQrRecyclerView.hasFixedSize()
        adapter = DynamicQrAdapter(context, dynamicQrList as ArrayList<CodeHistory>)
        dynamicQrRecyclerView.adapter = adapter
        adapter.setOnClickListener(this)
    }

    // THIS FUNCTION WILL SET UP THE TOP ACTIONBAR
    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Dynamic QR Codes"
        toolbar.setTitleTextColor(Color.parseColor("#000000"))
    }

    // THIS FUNCTION WILL DISPLAY THE LIST OF CREATED DYNAMIC QR CODE
    private fun displayDynamicQrCodes() {
        appViewModel.getAllDynamicQrCodes().observe(this, Observer { list ->

            if (list != null && list.isEmpty()) {
                dynamicQrRecyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            } else {
                dynamicQrList.clear()
                emptyView.visibility = View.GONE
                dynamicQrRecyclerView.visibility = View.VISIBLE

                dynamicQrList.addAll(list)
                adapter.notifyDataSetChanged()
            }
        })

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

    // THIS FUNCTION WILL HANDLE THE DYNAMIC QR CODE LIST FOR UPDATE
    override fun onItemClick(position: Int) {
        val dynamicQr = dynamicQrList[position]
        updateDynamicQrUrl(dynamicQr)
    }


    // THIS FUNCTION WILL POP UP WITH EXISTING URL FOR INPUT NEW UPDATED URL
    private fun updateDynamicQrUrl(selectedDynamicUrl: CodeHistory){

        val dynamicUrlUpdateView = LayoutInflater.from(context).inflate(R.layout.update_dynamic_url_dialog_layout, null)
        val updateInputBox = dynamicUrlUpdateView!!.findViewById<TextInputEditText>(R.id.dynamic_url_update_input_field)
        val cancelBtn = dynamicUrlUpdateView.findViewById<MaterialButton>(R.id.dialog_cancel_btn)
        val updateBtn = dynamicUrlUpdateView.findViewById<MaterialButton>(R.id.dialog_update_btn)
        updateInputBox.setText(selectedDynamicUrl.data)

        val builder = MaterialAlertDialogBuilder(context)
        builder.setCancelable(false)
        builder.setView(dynamicUrlUpdateView)

        val alert = builder.create()
        alert.show()


        cancelBtn.setOnClickListener {
            alert.dismiss()
        }

        updateBtn.setOnClickListener {
            if (updateInputBox.text.toString().contains("http") || updateInputBox.text.toString().contains("https")
                || updateInputBox.text.toString().contains("www")
            ) {

                val inputValue = updateInputBox.text.toString()

                // THIS IS THE TESTING USER DATA FOR DYNAMIC QR CODE GENERATION
                val hashMap = hashMapOf<String, String>()
                hashMap["login"] = selectedDynamicUrl.login
                hashMap["qrId"] = selectedDynamicUrl.qrId
                hashMap["userUrl"] = inputValue
                hashMap["userType"] = selectedDynamicUrl.userType
                alert.dismiss()
                startLoading(context)
                viewModel.createDynamicQrCode(context,hashMap)
                viewModel.getDynamicQrCode().observe(this, Observer { response ->
                    var url = ""
                    dismiss()
                    if (response != null){
                        url = response.get("generatedUrl").asString
                        url = if (url.contains(":8990")) {
                            url.replace(":8990","")
                        } else {
                            url
                        }

//                        appViewModel.update(inputValue,url,selectedDynamicUrl.id)
                        showAlert(context,"Dynamic Url update Successfully!")
                    }
                    else{
                        showAlert(context,"Something went wrong, please try again!")
                    }
                })

            } else {
               showAlert(
                    context,
                    "Please enter the correct format of url!"
                )
            }
        }

    }
}