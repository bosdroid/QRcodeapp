package com.expert.qrgenerator.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.adapters.TypesAdapter
import com.expert.qrgenerator.databinding.FragmentGeneratorBinding
import com.expert.qrgenerator.model.QRItem
import com.expert.qrgenerator.model.QRTypes
import com.expert.qrgenerator.ui.activities.BaseActivity
import com.expert.qrgenerator.ui.activities.CouponQrActivity
import com.expert.qrgenerator.ui.activities.FeedbackQrActivity
import com.expert.qrgenerator.ui.activities.MainActivity
import com.expert.qrgenerator.ui.activities.SocialNetworksQrActivity
import com.expert.qrgenerator.utils.AppSettings
import com.expert.qrgenerator.utils.Constants
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class GeneratorFragment : Fragment() {

    private lateinit var binding: FragmentGeneratorBinding

    private lateinit var typesAdapter: TypesAdapter
    private var qrTypeList = mutableListOf<QRItem>()
    private lateinit var appSettings: AppSettings

    private val fragments = listOf(
        TextFragment(),
        StaticLinkFragment(),
        ContactFragment(),
        WifiFragment(),
        PhoneFragment(),
        SmsFragment(),
        InstagramFragment(),
        WhatsappFragment(),
        MapFragment(),
        FacebookFragment(),
        YoutubeFragment(),
        TelegramFragment(),
        EmailFragment(),
        TikTokFragment(),
        GoogleFormsFragment(),
        TwitterFragment(),
        SnapchatFragment(),
        SpotifyFragment(),
        GoogleDocsFragment(),
        GoogleReviewFragment(),
        GoogleSheetsFragment(),
        PaymentFragment(),
        Office365Fragment(),
        ShapedFragment(),
        PaypalFragment(),
        EtsyFragment(),
        LinkedinFragment(),
        CryptoPaymentFragment(),
        CalendarFragment(),
        SocialMediaFragment(),
        RedditFragment(),
        PlayMarketAppStoreFragment()
    )


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentGeneratorBinding.inflate(layoutInflater, container, false)

        initViews()
        openQrTypeTooltip()
        return binding.root
    }


    private fun initViews() {
        appSettings = AppSettings(requireActivity())
    }

    // THIS FUNCTION WILL DISPLAY THE HORIZONTAL QR TYPES LIST
    private fun renderQRTypesRecyclerview() {
        // THIS LINE OF CODE WILL SET THE RECYCLERVIEW ORIENTATION (HORIZONTAL OR VERTICAL)
        binding.typesRecyclerView.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL,
            false
        )
        binding.typesRecyclerView.hasFixedSize()
        val tempList = Constants.getQRTypes(requireActivity())
        if (tempList.isNotEmpty()) {
            qrTypeList.clear()
        }
        qrTypeList.addAll(tempList)
//        typesAdapter = TypesAdapter(requireActivity(), qrTypeList)
        binding.typesRecyclerView.adapter = typesAdapter
        typesAdapter.updatePosition(0)
        typesAdapter.setOnItemClickListener(object : TypesAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val qrType = qrTypeList[position]
                typesAdapter.updatePosition(position)
                replaceFragment(position)
//                when (position) {
//                    9 -> {
//                        BaseActivity.hideSoftKeyboard(requireActivity(), binding.layoutContainer)
//                        requireActivity().startActivity(
//                            Intent(
//                                requireActivity(),
//                                CouponQrActivity::class.java
//                            )
//                        )
//                    }
//
//                    10 -> {
//                        BaseActivity.hideSoftKeyboard(requireActivity(), binding.layoutContainer)
//                        requireActivity().startActivity(
//                            Intent(
//                                requireActivity(),
//                                FeedbackQrActivity::class.java
//                            )
//                        )
//                    }
//
//                    11 -> {
//                        BaseActivity.hideSoftKeyboard(requireActivity(), binding.layoutContainer)
//                        requireActivity().startActivity(
//                            Intent(
//                                requireActivity(),
//                                SocialNetworksQrActivity::class.java
//                            )
//                        )
//                    }
//
//                    else -> {
//                        typesAdapter.updatePosition(position)
//                        replaceFragment(position)
//                    }
//                }

            }
        })

    }


    private fun replaceFragment(position: Int) {
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.layout_container, fragments[position])
        fragmentTransaction.commit()
    }

    private fun openQrTypeTooltip() {
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            val duration = appSettings.getLong("tt6")
            if (duration.compareTo(0) == 0 || System.currentTimeMillis() - duration > TimeUnit.DAYS.toMillis(
                    1
                )
            ) {
                SimpleTooltip.Builder(requireActivity())
                    .anchorView(binding.typesRecyclerView)
                    .text(getString(R.string.qr_types_tip_text))
                    .gravity(Gravity.BOTTOM)
                    .animated(true)
                    .transparentOverlay(false)
                    .onDismissListener { tooltip ->
                        appSettings.putLong("tt6", System.currentTimeMillis())
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
            if (duration.compareTo(0) == 0 || System.currentTimeMillis() - duration > TimeUnit.DAYS.toMillis(
                    1
                )
            ) {
                SimpleTooltip.Builder(requireActivity())
                    .anchorView(binding.layoutContainer)
                    .text(getString(R.string.insert_barcode_data_tip_text))
                    .gravity(Gravity.BOTTOM)
                    .animated(true)
                    .transparentOverlay(false)
                    .onDismissListener { tooltip ->
                        appSettings.putLong("tt7", System.currentTimeMillis())
                        tooltip.dismiss()
                        openHistoryBtnTip()
                    }
                    .build()
                    .show()
            }
        }
    }

    private fun openGeneratorBtnTooltip(view:MaterialButton) {
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            val duration = appSettings.getLong("tt8")
            if (duration.compareTo(0) == 0 || System.currentTimeMillis() - duration > TimeUnit.DAYS.toMillis(
                    1
                )
            ) {
                SimpleTooltip.Builder(requireActivity())
                    .anchorView(view)
                    .text(getString(R.string.next_btn_tip_text))
                    .gravity(Gravity.BOTTOM)
                    .animated(true)
                    .transparentOverlay(false)
                    .onDismissListener { tooltip ->
                        appSettings.putLong("tt8", System.currentTimeMillis())
                        tooltip.dismiss()
//                        openHistoryBtnTip()
                    }
                    .build()
                    .show()
            }
        }
    }

    private fun openHistoryBtnTip() {
        if (appSettings.getBoolean(getString(R.string.key_tips))) {
            val duration = appSettings.getLong("tt9")
            if (duration.compareTo(0) == 0 || System.currentTimeMillis() - duration > TimeUnit.DAYS.toMillis(
                    1
                )
            ) {
                SimpleTooltip.Builder(requireActivity())
                    .anchorView((requireActivity() as MainActivity).contentBinding.historyBtn)
                    .text(getString(R.string.generate_history_btn_tip_text))
                    .gravity(Gravity.BOTTOM)
                    .animated(true)
                    .transparentOverlay(false)
                    .onDismissListener { tooltip ->
                        appSettings.putLong("tt9", System.currentTimeMillis())
                        tooltip.dismiss()
                    }
                    .build()
                    .show()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        replaceFragment(0)
        renderQRTypesRecyclerview()
    }

}