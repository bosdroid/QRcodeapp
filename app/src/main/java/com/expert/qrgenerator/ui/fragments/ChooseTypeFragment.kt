package com.expert.qrgenerator.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.QRTypesAdapter
import com.expert.qrgenerator.adapters.QrCodeComparisonAdapter
import com.expert.qrgenerator.databinding.FragmentChooseTypeBinding
import com.expert.qrgenerator.interfaces.LoginCallback
import com.expert.qrgenerator.interfaces.OnFragmentReplaceListener
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.ui.activities.BaseActivity
import com.expert.qrgenerator.ui.activities.BaseActivity.Companion.dismiss
import com.expert.qrgenerator.ui.activities.BaseActivity.Companion.showAlert
import com.expert.qrgenerator.ui.activities.BaseActivity.Companion.startLoading
import com.expert.qrgenerator.ui.activities.DesignActivity
import com.expert.qrgenerator.ui.fragments.ScannerFragment.ScannerInterface
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.GeneratorManager
import com.expert.qrgenerator.viewmodel.DynamicQrViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.regex.Pattern

@AndroidEntryPoint
class ChooseTypeFragment : Fragment() {

    private lateinit var binding:FragmentChooseTypeBinding
    private lateinit var adapter: QRTypesAdapter

    private var fragmentReplaceListener: OnFragmentReplaceListener? = null
    private val viewModel: DynamicQrViewModel by viewModels()
    private val appViewModel: AppViewModel by viewModels()
    var selectedProtocol = "https://"

    var selectedQrType = "advance"

    var isUpdating = false

    // Variable to hold the encoded data for QR code generation
    private var encodedData: String = ""
    private var listener: ChooseTypeInterface? = null

    interface ChooseTypeInterface {
        fun login(callback: LoginCallback)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
//        (requireActivity() as? BaseActivity)?.logCustomEvent(
//            eventName = "screen_choose_type_opened"
//        )

        try {
            fragmentReplaceListener = context as OnFragmentReplaceListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnFragmentReplaceListener")
        }

        if (context is ChooseTypeInterface) {
            listener = context
        } else {
            throw ClassCastException("$context must implement ChooseTypeInterface")
        }
    }

    override fun onDetach() {
        super.onDetach()
        fragmentReplaceListener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChooseTypeBinding.inflate(inflater, container, false)

        val layoutManager = GridLayoutManager(requireActivity(), 2) // 2 columns

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    QRTypesAdapter.VIEW_TYPE_HEADER -> 2 // Header takes full width (2 columns)
                    QRTypesAdapter.VIEW_TYPE_ITEM -> {
                        if (position < 5) 2 else 1  // First 4 items also take full width, rest take 1 column
                    }   // Items take one column each
                    else -> 1
                }
            }
        }
        binding.chooseTypesRecyclerView.layoutManager = layoutManager
        adapter = QRTypesAdapter(Constants.getQRTypes(requireActivity())){ type,position ->
            BaseActivity.logCustomEvent(requireActivity(),"qr_type_chosen","type",type)
            fragmentReplaceListener?.replaceFragment(position)
        }
        binding.chooseTypesRecyclerView.adapter = adapter

        binding.infoImageView.setOnClickListener {
//            TooltipCompat.setTooltipText(binding.infoImageView, getString(R.string.static_link_hint_message))
//            binding.infoImageView.performLongClick()
            val dialog = YouTubeDialogFragment("Jh1AnV5opZA")
            dialog.show(childFragmentManager, "YouTubeDialogFragment")
        }


// Set up a listener for changes in the protocol selection
        binding.staticTypeGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.advance_type_rb -> {
                    selectedQrType = "advance"
                }
                R.id.regular_type_rb -> {
                    selectedQrType = "link"
                }
                else -> {
                    // Handle other cases if necessary
                }
            }
        }

        // Set up a listener for changes in the protocol selection
        binding.httpProtocolGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.http_protocol_rb -> {
                    selectedProtocol = "http://"
                }
                R.id.https_protocol_rb -> {
                    selectedProtocol = "https://"
                }
                else -> {
                    // Handle other cases if necessary
                }
            }
        }

        binding.staticLinkLayoutInputField.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (!isUpdating) {
                    isUpdating = true // Set the flag to true to prevent recursion
                    binding.staticLinkLayoutInputField.setText(s.toString().lowercase())
                    binding.staticLinkLayoutInputField.setSelection(binding.staticLinkLayoutInputField.text.toString().length) // Move the cursor to the end
                    isUpdating = false // Reset the flag
                }
            }
        })

        binding.staticLayoutContinueBtn.setOnClickListener {
            val value = binding.staticLinkLayoutInputField.text.toString().trim().lowercase()

            // Check if a protocol is selected
            if (selectedProtocol.isEmpty()) {
                BaseActivity.hideSoftKeyboard(requireActivity(), binding.staticLinkLayoutInputField.rootView)
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.protocol_error)
                )
            }
            // Check if the input field is empty
            else if (value.isEmpty()) {
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.required_data_input_error)
                )
            }
            // Check if the input contains 'http://' or 'https://'
            else if (value.contains("http://") || value.contains("https://")) {
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.without_protocol_error)
                )
            }
            // Validate the URL format using a regular expression
            else if (!Pattern.compile("^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?\$").matcher(value).find()) {
                BaseActivity.showAlert(
                    requireActivity(),
                    requireActivity().resources.getString(R.string.valid_website_error)
                )
            }
            // If all validations pass, encode the data and generate a QR code
            else {

                if (selectedQrType == "link"){
                    encodedData = "$selectedProtocol$value"
                    binding.staticLinkLayoutInputField.setText("")
                    BaseActivity.hideSoftKeyboard(requireActivity(),binding.staticLinkLayoutInputField)
                    binding.staticLinkLayoutInputField.clearFocus()
                    GeneratorManager.generateQRCode(requireActivity(), encodedData, selectedQrType)
                }
                else{
                    if(Constants.userData != null) {

                        encodedData = "$selectedProtocol$value"
                        binding.staticLinkLayoutInputField.setText("")
                        BaseActivity.hideSoftKeyboard(
                            requireActivity(),
                            binding.staticLinkLayoutInputField
                        )
                        binding.staticLinkLayoutInputField.clearFocus()
                        val qrId = System.currentTimeMillis()
                        val userId = Constants.userData?.personId
                        val hashMap = hashMapOf<String, String>().apply {
                            put("login", "$userId")
                            put("qrId", "$qrId")
                            put("qrType", selectedQrType)
                            put("userUrl", encodedData)
                            put("userType", "free")
                        }

                        startLoading(requireActivity())
                        lifecycleScope.launch {
                            viewModel.createDynamicQrCode(hashMap)
                        }
                        viewModel.dynamicQrCodeResponse.observe(
                            requireActivity(),
                            Observer { response ->
                                dismiss()
                                response?.let {
                                    val genUrl = it.get("generatedUrl").asString
                                    val qrHistory = CodeHistory(
                                        "$userId",
                                        "$qrId",
                                        encodedData,
                                        selectedQrType,
                                        "free",
                                        "qr",
                                        "create",
                                        "",
                                        "1",
                                        genUrl,
                                        System.currentTimeMillis().toString(),
                                        ""
                                    )
//                                    val insertedId = appViewModel.insert(qrHistory)
//                                    qrHistory.id = insertedId.toInt()
                                    val intent = Intent(context, DesignActivity::class.java).apply {
                                        // Add encoded data and QR history to the intent extras
                                        putExtra("ENCODED_TEXT", genUrl)
                                        putExtra("QR_HISTORY", qrHistory)
                                    }

                                    // Start the DesignActivity with the intent
                                    startActivity(intent)
                                } ?: run {
                                    showAlert(
                                        requireActivity(),
                                        "Something went wrong, please try again!"
                                    )
                                }
                            })
                    }
                    else{
                        listener?.login(object : LoginCallback {
                            override fun onSuccess() {
                                onResume()
                            }
                        })
                    }
                }

            }
        }
//        BaseActivity.hideSoftKeyboard(requireActivity(), binding.staticLinkLayoutInputField.rootView)
//        binding.staticLinkLayoutInputField.requestFocus()
//        openKeyboard(requireActivity())

        return binding.root
    }


}