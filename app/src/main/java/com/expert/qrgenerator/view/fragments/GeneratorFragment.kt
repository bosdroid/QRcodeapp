package com.expert.qrgenerator.view.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.TypesAdapter
import com.expert.qrgenerator.model.QRTypes
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.view.activities.*


class GeneratorFragment : Fragment() {

    private lateinit var qrTypesRecyclerView: RecyclerView
    private lateinit var typesAdapter: TypesAdapter
    private var qrTypeList = mutableListOf<QRTypes>()
    private lateinit var layoutContainer: FrameLayout


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_generator, container, false)

        initViews(v)

        return v
    }


    private fun initViews(view: View){
        qrTypesRecyclerView = view.findViewById(R.id.types_recycler_view)
        layoutContainer = view.findViewById(R.id.layout_container)

    }

    // THIS FUNCTION WILL DISPLAY THE HORIZONTAL QR TYPES LIST
    private fun renderQRTypesRecyclerview() {
        // THIS LINE OF CODE WILL SET THE RECYCLERVIEW ORIENTATION (HORIZONTAL OR VERTICAL)
        qrTypesRecyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL,
            false
        )
        qrTypesRecyclerView.hasFixedSize()
        val tempList = Constants.getQRTypes(requireActivity())
        if (tempList.isNotEmpty()){
            qrTypeList.clear()
        }
        qrTypeList.addAll(tempList)
        typesAdapter = TypesAdapter(requireActivity(), qrTypeList)
        qrTypesRecyclerView.adapter = typesAdapter
        typesAdapter.updatePosition(0)
        typesAdapter.setOnItemClickListener(object : TypesAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val qrType = qrTypeList[position]
                if (position == 9) {
                    BaseActivity.hideSoftKeyboard(requireActivity(),layoutContainer)
                    requireActivity().startActivity(Intent(requireActivity(), CouponQrActivity::class.java))
                }
                else if (position == 10){
                    BaseActivity.hideSoftKeyboard(requireActivity(),layoutContainer)
                    requireActivity().startActivity(Intent(requireActivity(), FeedbackQrActivity::class.java))
                }
                else if (position == 11){
                    BaseActivity.hideSoftKeyboard(requireActivity(),layoutContainer)
                    requireActivity().startActivity(Intent(requireActivity(), SocialNetworksQrActivity::class.java))
                }
                else {
                    Constants.getLayout(requireActivity(), position, layoutContainer)
                }

            }
        })
    }

    override fun onResume() {
        super.onResume()
        Constants.getLayout(requireActivity(), 0, layoutContainer)
        renderQRTypesRecyclerview()
    }

}