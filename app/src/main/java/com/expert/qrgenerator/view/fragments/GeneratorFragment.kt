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
import com.expert.qrgenerator.view.activities.CouponQrActivity
import com.expert.qrgenerator.view.activities.FeedbackQrActivity
import com.expert.qrgenerator.view.activities.MainActivity


class GeneratorFragment : Fragment() {

    private lateinit var qrTypesRecyclerView: RecyclerView
    private lateinit var typesAdapter: TypesAdapter
    private var qrTypeList = mutableListOf<QRTypes>()
    private lateinit var layoutContainer: FrameLayout


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_generator, container, false)

        initViews(v)
        renderQRTypesRecyclerview()
        return v
    }


    private fun initViews(view: View){
        qrTypesRecyclerView = view.findViewById(R.id.types_recycler_view)
        layoutContainer = view.findViewById(R.id.layout_container)
        Constants.getLayout(requireActivity(), 0, layoutContainer, MainActivity.nextStepTextView)
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
        qrTypeList.addAll(Constants.getQRTypes(requireActivity()))
        typesAdapter = TypesAdapter(requireActivity(), qrTypeList)
        qrTypesRecyclerView.adapter = typesAdapter
        typesAdapter.setOnItemClickListener(object : TypesAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val qrType = qrTypeList[position]
                if (position == 9) {
                    requireActivity().startActivity(Intent(requireActivity(), CouponQrActivity::class.java))
                }
                else if (position == 10){
                    requireActivity().startActivity(Intent(requireActivity(), FeedbackQrActivity::class.java))
                }
                else {
                    Constants.getLayout(requireActivity(), position, layoutContainer, MainActivity.nextStepTextView)
                }

            }
        })
    }


}