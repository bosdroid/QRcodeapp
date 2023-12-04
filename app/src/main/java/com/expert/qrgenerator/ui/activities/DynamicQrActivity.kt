package com.expert.qrgenerator.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.DynamicQrAdapter
import com.expert.qrgenerator.databinding.ActivityDynamicQrBinding
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.viewmodel.DynamicQrViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DynamicQrActivity : BaseActivity(), DynamicQrAdapter.OnItemClickListener {

    private lateinit var binding:ActivityDynamicQrBinding

    private lateinit var context: Context

    private lateinit var adapter: DynamicQrAdapter
    private var dynamicQrList = mutableListOf<CodeHistory>()

private val appViewModel: AppViewModel by viewModels()
    private val viewModel: DynamicQrViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDynamicQrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setUpToolbar()
        displayDynamicQrCodes()

    }

    // THIS FUNCTION WILL INITIALIZE ALL THE VIEWS AND REFERENCE OF OBJECTS
    private fun initViews() {
        context = this

        binding.dynamicQrRecyclerview.layoutManager = LinearLayoutManager(context)
        binding.dynamicQrRecyclerview.hasFixedSize()
        adapter = DynamicQrAdapter(context, dynamicQrList as ArrayList<CodeHistory>)
        binding.dynamicQrRecyclerview.adapter = adapter
        adapter.setOnClickListener(this)
    }

    // THIS FUNCTION WILL SET UP THE TOP ACTIONBAR
    private fun setUpToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Dynamic QR Codes"
        binding.toolbar.setTitleTextColor(Color.parseColor("#000000"))
    }

    // THIS FUNCTION WILL DISPLAY THE LIST OF CREATED DYNAMIC QR CODE
    private fun displayDynamicQrCodes() {
        appViewModel.getAllDynamicQrCodes().observe(this, Observer { list ->

            if (list != null && list.isEmpty()) {
                binding.dynamicQrRecyclerview.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
            } else {
                dynamicQrList.clear()
                binding.emptyView.visibility = View.GONE
                binding.dynamicQrRecyclerview.visibility = View.VISIBLE

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

    override fun onItemEditClick(position: Int) {
        val dynamicQr = dynamicQrList[position]
        updateDynamicQrUrl(dynamicQr)
    }

    // THIS FUNCTION WILL HANDLE THE DYNAMIC QR CODE LIST FOR UPDATE
    override fun onItemClick(position: Int) {
        val dynamicQr = dynamicQrList[position]
        val intent = Intent(context, CodeDetailActivity::class.java)
        intent.putExtra("HISTORY_ITEM", dynamicQr)
        startActivity(intent)
    }


    // THIS FUNCTION WILL POP UP WITH EXISTING URL FOR INPUT NEW UPDATED URL
    private fun updateDynamicQrUrl(selectedDynamicUrl: CodeHistory){
        var selectedProtocol = ""
        val dynamicUrlUpdateView = LayoutInflater.from(context).inflate(R.layout.update_dynamic_url_dialog_layout, null)
        val updateInputBox = dynamicUrlUpdateView!!.findViewById<TextInputEditText>(R.id.dynamic_url_update_input_field)
        val cancelBtn = dynamicUrlUpdateView.findViewById<MaterialButton>(R.id.dialog_cancel_btn)
        val updateBtn = dynamicUrlUpdateView.findViewById<MaterialButton>(R.id.dialog_update_btn)
        val protocolGroup = dynamicUrlUpdateView.findViewById<RadioGroup>(R.id.http_protocol_group)
        protocolGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.http_protocol_rb -> {
                    selectedProtocol = "http://"
                }
                R.id.https_protocol_rb -> {
                    selectedProtocol = "https://"
                }
                else -> {

                }
            }
        }
        if (selectedDynamicUrl.data.contains("http://"))
        {
            protocolGroup.check(R.id.http_protocol_rb)
            selectedProtocol = "http://"
            updateInputBox.setText(selectedDynamicUrl.data.removePrefix("http://"))
        } else if(selectedDynamicUrl.data.contains("https://")){
            selectedProtocol = "https://"
            protocolGroup.check(R.id.https_protocol_rb)
            updateInputBox.setText(selectedDynamicUrl.data.removePrefix("https://"))
        }
        else{
            updateInputBox.setText(selectedDynamicUrl.data)
        }



        val builder = MaterialAlertDialogBuilder(context)
        builder.setCancelable(false)
        builder.setView(dynamicUrlUpdateView)

        val alert = builder.create()
        alert.show()


        cancelBtn.setOnClickListener {
            alert.dismiss()
        }

        updateBtn.setOnClickListener {
            val value = updateInputBox.text.toString().trim()
            if (selectedProtocol.isEmpty()) {
                showAlert(
                    context,
                    "Please select the URL protocol!"
                )
            } else if (value.isEmpty()) {

                showAlert(
                    context,
                    "Please enter the required input data!"
                )

            } else if (value.contains("http://") || value.contains("https://")
            ) {
                showAlert(
                    context,
                    "Please enter the URL without http:// or https://"
                )
            } else if (!value.contains(".com")) {
                showAlert(
                    context,
                    "Please enter the valid URL"
                )
            }
            else{

                // THIS IS THE TESTING USER DATA FOR DYNAMIC QR CODE GENERATION
                val hashMap = hashMapOf<String, String>()
                hashMap["login"] = selectedDynamicUrl.login
                hashMap["qrId"] = selectedDynamicUrl.qrId
                hashMap["userUrl"] = "$selectedProtocol$value"
                hashMap["userType"] = selectedDynamicUrl.userType
                alert.dismiss()
                startLoading(context)
                viewModel.createDynamicQrCode(hashMap)
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

                        appViewModel.update("$selectedProtocol$value",url,selectedDynamicUrl.id)
                        showAlert(context,"Dynamic Url update Successfully!")
                    }
                    else{
                        showAlert(context,"Something went wrong, please try again!")
                    }
                })
            }
        }

    }
}