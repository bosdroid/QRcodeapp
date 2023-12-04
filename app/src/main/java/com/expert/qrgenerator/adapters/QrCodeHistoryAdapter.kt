package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.QrCodeHistoryItemDesignBinding
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.ui.activities.BaseActivity
import com.google.android.material.textview.MaterialTextView

class QrCodeHistoryAdapter(val context: Context, var qrCodeHistoryList: ArrayList<CodeHistory>) :
    RecyclerView.Adapter<QrCodeHistoryAdapter.ItemViewHolder>() {

    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    public fun setOnClickListener(mListener: OnItemClickListener) {
        listener = mListener
    }

    class ItemViewHolder(private val binding:QrCodeHistoryItemDesignBinding,private val mListener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindData(qrHistory: CodeHistory, context: Context){
            when (qrHistory.type) {
                "text" -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.ic_text)
                }
                "link" -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.ic_link)
                }
                "contact" -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.ic_person)
                }
                "wifi" -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.ic_wifi)
                }
                "phone" -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.ic_phone)
                }
                "code" -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.ic_code)
                }
                "sms" -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.ic_sms)
                }
                "instagram" -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.instagram)
                }
                "whatsapp" -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.whatsapp)
                }
                "coupon" -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.ic_coupon)
                }
                "feedback" -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.ic_feedback)
                }
                "sn" -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.ic_social_networks)
                }
                else -> {

                }
            }

            binding.qrCodeHistoryItemText.text = qrHistory.data
            binding.qrCodeHistoryItemCreatedDate.text =
                BaseActivity.getFormattedDate(context, qrHistory.createdAt.toLong())
            if (qrHistory.notes.isNotEmpty()) {
                binding.qrCodeHistoryItemNotesText.visibility = View.VISIBLE
                val notesText = qrHistory.notes
                if (notesText.length >= 110) {
                    binding.qrCodeHistoryItemNotesText.text = "${notesText.substring(0, 107)}..."
                } else {
                    binding.qrCodeHistoryItemNotesText.text = notesText
                }
            } else {
                binding.qrCodeHistoryItemNotesText.visibility = View.GONE
            }
            itemView.setOnClickListener {
                mListener.onItemClick(layoutPosition)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val qrCodeHistoryItemDesignBinding = QrCodeHistoryItemDesignBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return ItemViewHolder(qrCodeHistoryItemDesignBinding, listener!!)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val qrHistory = qrCodeHistoryList[position]
        holder.bindData(qrHistory,context)
    }

    override fun getItemCount(): Int {
        return qrCodeHistoryList.size
    }


}