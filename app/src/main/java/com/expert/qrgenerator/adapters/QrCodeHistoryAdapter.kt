package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.QrCodeHistoryItemDesignBinding
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.ui.activities.BaseActivity

class QrCodeHistoryAdapter(private val context: Context, private val qrCodeHistoryList: ArrayList<CodeHistory>) :
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
                context.getString(R.string.text) -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.ic_text)
                }
                context.getString(R.string.link) -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.ic_link)
                }
                context.getString(R.string.contact) -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.ic_person)
                }
                context.getString(R.string.wifi) -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.ic_wifi)
                }
                context.getString(R.string.phonee) -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.ic_phone)
                }
                context.getString(R.string.code) -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.ic_code)
                }
                context.getString(R.string.sms) -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.ic_sms)
                }
                context.getString(R.string.instagram) -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.instagram)
                }
                context.getString(R.string.whatsapp) -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.whatsapp)
                }
                context.getString(R.string.coupon) -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.ic_coupon)
                }
                context.getString(R.string.feedback) -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.ic_feedback)
                }
                context.getString(R.string.sn) -> {
                    binding.qrCodeHistoryItemTypeIcon.setImageResource(R.drawable.ic_social_networks)
                }
                else -> {

                }
            }

            binding.qrCodeHistoryItemText.text = qrHistory.data
            binding.qrCodeHistoryItemCreatedDate.text =
                BaseActivity.getFormattedDate(context, qrHistory.createdAt.toLong())

            // this condition check if qr code history have any detail like Notes then it display
            // otherwise hide it
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

        return ItemViewHolder(qrCodeHistoryItemDesignBinding, listener?: throw IllegalStateException("OnItemClickListener not set"))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val qrHistory = qrCodeHistoryList[position]
        holder.bindData(qrHistory,context)
    }

    override fun getItemCount(): Int = qrCodeHistoryList.size


}