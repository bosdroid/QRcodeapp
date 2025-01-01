package com.expert.qrgenerator.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
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
import com.expert.qrgenerator.model.Folder
import com.expert.qrgenerator.repository.DataRepository
import com.expert.qrgenerator.room.AppViewModel
import com.expert.qrgenerator.ui.activities.BaseActivity
import com.expert.qrgenerator.ui.activities.BaseActivity.Companion.dismiss
import com.expert.qrgenerator.ui.activities.BaseActivity.Companion.getDateTimeFromTimeStamp1
import com.expert.qrgenerator.ui.activities.BaseActivity.Companion.showAlert
import com.expert.qrgenerator.ui.activities.BaseActivity.Companion.startLoading
import com.expert.qrgenerator.ui.activities.DesignActivity
import com.expert.qrgenerator.ui.fragments.ScannerFragment.ScannerInterface
import com.expert.qrgenerator.utils.AppSettings
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.GeneratorManager
import com.expert.qrgenerator.viewmodel.DynamicQrViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.regex.Pattern

@AndroidEntryPoint
class ChooseTypeFragment : Fragment() {

    private lateinit var binding: FragmentChooseTypeBinding
    private lateinit var adapter: QRTypesAdapter

    private var fragmentReplaceListener: OnFragmentReplaceListener? = null
    private val viewModel: DynamicQrViewModel by viewModels()
    private val appViewModel: AppViewModel by viewModels()
    var selectedProtocol = "https://"

    var selectedQrType = "advance"

    var isUpdating = false

    val tipList = listOf("one", "two", "three", "four", "five")

    // Variable to hold the encoded data for QR code generation
    private var encodedData: String = ""
    private var listener: ChooseTypeInterface? = null
    private lateinit var appSettings: AppSettings

    companion object{
        lateinit var infoImageView3:AppCompatImageView
        lateinit var infoImageView4:AppCompatImageView
    }

    interface ChooseTypeInterface {
        fun login(callback: LoginCallback)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appSettings = AppSettings(context)

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        adapter = QRTypesAdapter(Constants.getQRTypes(requireActivity()))

        binding.chooseTypesRecyclerView.adapter = adapter
        adapter.setItemClickListener(object : QRTypesAdapter.OnItemClickListener {
            override fun itemClickListener(type: String, position: Int) {
                BaseActivity.logCustomEvent(requireActivity(), "qr_type_chosen", "type", type)
                fragmentReplaceListener?.replaceFragment(position)
            }

            override fun itemIconClickListener(position: Int) {
                if (position == 0) {
                    openTipDialog("four")
                } else {
                    openTipDialog("five")
                }
            }
        })

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

        binding.staticLinkLayoutInputField.addTextChangedListener(object : TextWatcher {
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

            if (value.isNotEmpty() && value == "preparetestdata") {
                generateFakeTestData()
                binding.staticLinkLayoutInputField.setText("")
                BaseActivity.hideSoftKeyboard(
                    requireActivity(),
                    binding.staticLinkLayoutInputField
                )
                binding.staticLinkLayoutInputField.clearFocus()
            } else {
                // Check if a protocol is selected
                if (selectedProtocol.isEmpty()) {
                    BaseActivity.hideSoftKeyboard(
                        requireActivity(),
                        binding.staticLinkLayoutInputField.rootView
                    )
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
                else if (!Pattern.compile("^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?\$")
                        .matcher(value).find()
                ) {
                    BaseActivity.showAlert(
                        requireActivity(),
                        requireActivity().resources.getString(R.string.valid_website_error)
                    )
                }
                // If all validations pass, encode the data and generate a QR code
                else {

                    if (selectedQrType == "link") {
                        encodedData = "$selectedProtocol$value"
                        binding.staticLinkLayoutInputField.setText("")
                        BaseActivity.hideSoftKeyboard(
                            requireActivity(),
                            binding.staticLinkLayoutInputField
                        )
                        binding.staticLinkLayoutInputField.clearFocus()
                        GeneratorManager.generateQRCode(
                            requireActivity(),
                            encodedData,
                            selectedQrType
                        )
                    } else {
                        if (Constants.userData != null) {

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
                                            "", "", "", ""
                                        )
//                                    val insertedId = appViewModel.insert(qrHistory)
//                                    qrHistory.id = insertedId.toInt()
                                        val intent =
                                            Intent(context, DesignActivity::class.java).apply {
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
                        } else {
                            listener?.login(object : LoginCallback {
                                override fun onSuccess() {
                                    onResume()
                                }
                            })
                        }
                    }

                }
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            manageTipsSequentially(tipList)
        },2000)

        appViewModel.allFolders().observe(requireActivity()) { list ->
            if (list.isNotEmpty()) {
                val hasFavouriteFolder = list.any { it.name.lowercase() == "favourite" }
                if (!hasFavouriteFolder) {
                    appViewModel.insertFolder(Folder(name = "favourite"))
                }
            } else {
                appViewModel.insertFolder(Folder(name = "favourite"))
            }
        }
    }

    private fun manageTipsSequentially(
        keys: List<String>
    ) {
        // Start with the first unhidden tip
        //        for (key in keys) {
//            if (!appSettings.getBoolean("${key}_status")) {
                // Show the tip for the current key
                when (val key = keys[currentTipIndex]) {
                    "one" -> showTip(binding.infoImageView, key)
                    "two" -> showTip(binding.infoImageView1, key)
                    "three" -> showTip(binding.infoImageView2, key)
                    "four" -> showTip(infoImageView3, key)
                    "five" -> showTip(infoImageView4, key)
                }
                return // Stop once a tip is shown
//            }
//        }
    }

    private var currentTipIndex = 0
    private fun showTip(view: AppCompatImageView, value: String) {
        view.setOnClickListener {
            if (currentTipIndex != tipList.size-1){
                currentTipIndex ++
            }
            Constants.clearShakeAnimation(view)
//            view.visibility = View.GONE
            appSettings.putBoolean("${value}_status", true) // Mark as hidden
            openTipDialog(value)

            // Trigger the next tip display
            manageTipsSequentially(tipList)
        }

        if (appSettings.getBoolean("${value}_status")) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.VISIBLE
            Constants.startShakeAnimation(view)
        }
    }


    private fun openTipDialog(key: String) {
        val tip = Constants.getTip(key)
        if (tip != null) {
            val dialog = YouTubeDialogFragment(tip)
            dialog.show(childFragmentManager, "YouTubeDialogFragment")
        }
    }

    private fun generateFakeTestData() {
        startLoading(requireActivity())
        CoroutineScope(Dispatchers.Main).launch {
            for (i in 0..2) { // Iterate through the cases
                when (i) {
                    0 -> {
                        val qrHistory = CodeHistory(
                            Constants.userData?.personId ?: "qrmagicapp",
                            System.currentTimeMillis().toString(),
                            "https://www.google.com",
                            "advance",
                            "free",
                            "qr",
                            "fake",
                            "",
                            "1",
                            "",
                            System.currentTimeMillis().toString(),
                            "", "5", "50", "20"
                        )
                        DataRepository.saveFakeTestData(qrHistory.qrId, 0)
                        appViewModel.insert(qrHistory)
                    }

                    1 -> {
                        val qrHistory = CodeHistory(
                            Constants.userData?.personId ?: "qrmagicapp",
                            System.currentTimeMillis().toString(),
                            "https://www.microsoft.com",
                            "advance",
                            "free",
                            "qr",
                            "fake",
                            "",
                            "1",
                            "",
                            System.currentTimeMillis().toString(),
                            "", "8", "80", "40"
                        )
                        DataRepository.saveFakeTestData(qrHistory.qrId, 1)
                        appViewModel.insert(qrHistory)
                    }

                    2 -> {
                        val qrHistory = CodeHistory(
                            Constants.userData?.personId ?: "qrmagicapp",
                            System.currentTimeMillis().toString(),
                            "https://www.bing.com",
                            "advance",
                            "free",
                            "qr",
                            "fake",
                            "",
                            "1",
                            "",
                            System.currentTimeMillis().toString(),
                            "", "12", "120", "60"
                        )
                        DataRepository.saveFakeTestData(qrHistory.qrId, 2)
                        appViewModel.insert(qrHistory)
                    }
                }
                delay(1000) // Wait for 1 second before proceeding to the next iteration
            }
            dismiss()
            Toast.makeText(
                requireActivity(),
                "Fake Test Data has been generated!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}