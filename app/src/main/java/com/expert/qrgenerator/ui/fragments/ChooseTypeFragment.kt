package com.expert.qrgenerator.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.expert.qrgenerator.adapters.QRTypesAdapter
import com.expert.qrgenerator.databinding.FragmentChooseTypeBinding
import com.expert.qrgenerator.interfaces.OnFragmentReplaceListener
import com.expert.qrgenerator.ui.activities.BaseActivity
import com.expert.qrgenerator.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChooseTypeFragment : Fragment() {

    private lateinit var binding:FragmentChooseTypeBinding
    private lateinit var adapter: QRTypesAdapter

    private var fragmentReplaceListener: OnFragmentReplaceListener? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as? BaseActivity)?.logCustomEvent(
            eventName = "screen_choose_type_opened"
        )

        try {
            fragmentReplaceListener = context as OnFragmentReplaceListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnFragmentReplaceListener")
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
                        if (position < 6) 2 else 1  // First 4 items also take full width, rest take 1 column
                    }   // Items take one column each
                    else -> 1
                }
            }
        }
        binding.chooseTypesRecyclerView.layoutManager = layoutManager
        adapter = QRTypesAdapter(Constants.getQRTypes(requireActivity())){ qrType,position ->
            fragmentReplaceListener?.replaceFragment(position)
        }
        binding.chooseTypesRecyclerView.adapter = adapter

<<<<<<< Updated upstream
=======
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

        binding.staticLayoutContinueBtn.setOnClickListener {
            val value = binding.staticLinkLayoutInputField.text.toString().trim()

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
                encodedData = "$selectedProtocol$value"
                binding.staticLinkLayoutInputField.setText("")
                BaseActivity.hideSoftKeyboard(requireActivity(),binding.staticLinkLayoutInputField)
                binding.staticLinkLayoutInputField.clearFocus()
                GeneratorManager.generateQRCode(requireActivity(), encodedData, "trackable")
            }
        }


>>>>>>> Stashed changes
        return binding.root
    }


}