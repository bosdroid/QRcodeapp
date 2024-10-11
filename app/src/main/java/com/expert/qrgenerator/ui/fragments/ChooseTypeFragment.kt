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

    private lateinit var binding: FragmentChooseTypeBinding
    private lateinit var adapter: QRTypesAdapter

    private var fragmentReplaceListener: OnFragmentReplaceListener? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)

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
        adapter = QRTypesAdapter(Constants.getQRTypes(requireActivity())) { qrType, position ->
            fragmentReplaceListener?.replaceFragment(position)
        }
        binding.chooseTypesRecyclerView.adapter = adapter

        return binding.root
    }


}