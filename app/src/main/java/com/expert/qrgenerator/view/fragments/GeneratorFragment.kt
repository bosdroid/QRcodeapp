package com.expert.qrgenerator.view.fragments

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
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
import com.expert.qrgenerator.utils.AppSettings
import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.view.activities.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip
import java.util.concurrent.TimeUnit


class GeneratorFragment : Fragment() {

    private lateinit var qrTypesRecyclerView: RecyclerView
    private lateinit var typesAdapter: TypesAdapter
    private var qrTypeList = mutableListOf<QRTypes>()
    private lateinit var layoutContainer: FrameLayout
    private lateinit var nextStepBtn: MaterialButton
    private lateinit var appSettings:AppSettings


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_generator, container, false)

        initViews(v)
        openQrTypeTooltip()
        return v
    }


    private fun initViews(view: View){
        appSettings = AppSettings(requireActivity())
        qrTypesRecyclerView = view.findViewById(R.id.types_recycler_view)
        layoutContainer = view.findViewById(R.id.layout_container)
        nextStepBtn = view.findViewById(R.id.next_step_btn)

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
                    Constants.getLayout(requireActivity(), position, layoutContainer,nextStepBtn)
                }

            }
        })

    }

    fun openQrTypeTooltip(){
            if (appSettings.getBoolean(getString(R.string.key_tips))) {
                val duration = appSettings.getLong("tt6")
                if (duration.compareTo(0) == 0 || System.currentTimeMillis()-duration > TimeUnit.DAYS.toMillis(1) ) {
                    SimpleTooltip.Builder(requireActivity())
                        .anchorView(qrTypesRecyclerView)
                        .text(getString(R.string.qr_types_tip_text))
                        .gravity(Gravity.BOTTOM)
                        .animated(true)
                        .transparentOverlay(false)
                        .onDismissListener { tooltip ->
                            appSettings.putLong("tt6",System.currentTimeMillis())
                            tooltip.dismiss()
                            openInsertBarcodeTooltip()
                        }
                        .build()
                        .show()
                }
            }
    }

    private fun openInsertBarcodeTooltip() {
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            val duration = appSettings.getLong("tt7")
            if (duration.compareTo(0) == 0 || System.currentTimeMillis()-duration > TimeUnit.DAYS.toMillis(1) ) {
                SimpleTooltip.Builder(requireActivity())
                    .anchorView(layoutContainer)
                    .text(getString(R.string.insert_barcode_data_tip_text))
                    .gravity(Gravity.BOTTOM)
                    .animated(true)
                    .transparentOverlay(false)
                    .onDismissListener { tooltip ->
                        appSettings.putLong("tt7",System.currentTimeMillis())
                        tooltip.dismiss()
                        openGeneratorBtnTooltip()
                    }
                    .build()
                    .show()
            }
        }
    }

    private fun openGeneratorBtnTooltip() {
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            val duration = appSettings.getLong("tt8")
            if (duration.compareTo(0) == 0 || System.currentTimeMillis()-duration > TimeUnit.DAYS.toMillis(1) ) {
                SimpleTooltip.Builder(requireActivity())
                    .anchorView(nextStepBtn)
                    .text(getString(R.string.next_btn_tip_text))
                    .gravity(Gravity.BOTTOM)
                    .animated(true)
                    .transparentOverlay(false)
                    .onDismissListener { tooltip ->
                        appSettings.putLong("tt8",System.currentTimeMillis())
                        tooltip.dismiss()
                        openHistoryBtnTip()
                    }
                    .build()
                    .show()
            }
        }
    }

    private fun openHistoryBtnTip(){
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            val duration = appSettings.getLong("tt9")
            if (duration.compareTo(0) == 0 || System.currentTimeMillis()-duration > TimeUnit.DAYS.toMillis(1) ) {
                SimpleTooltip.Builder(requireActivity())
                    .anchorView(MainActivity.historyBtn)
                    .text(getString(R.string.generate_history_btn_tip_text))
                    .gravity(Gravity.BOTTOM)
                    .animated(true)
                    .transparentOverlay(false)
                    .onDismissListener { tooltip ->
                        appSettings.putLong("tt9",System.currentTimeMillis())
                        tooltip.dismiss()
                    }
                    .build()
                    .show()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        Constants.getLayout(requireActivity(), 0, layoutContainer,nextStepBtn)
        renderQRTypesRecyclerview()
    }

}