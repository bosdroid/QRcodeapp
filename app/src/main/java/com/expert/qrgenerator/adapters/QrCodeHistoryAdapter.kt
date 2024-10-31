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

class QrCodeHistoryAdapter(
    private val context: Context,
    private val qrCodeHistoryList: ArrayList<CodeHistory>
) : RecyclerView.Adapter<QrCodeHistoryAdapter.ItemViewHolder>() {

    private var listener: OnItemClickListener? = null

    // Interface to handle item click events
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    // Function to set the OnItemClickListener
    fun setOnClickListener(mListener: OnItemClickListener) {
        listener = mListener
    }

    // ViewHolder class to represent each item in the RecyclerView
    class ItemViewHolder(
        private val binding: QrCodeHistoryItemDesignBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        // Function to bind data to the views
        fun bindData(qrHistory: CodeHistory, context: Context) {
            // Set the appropriate icon based on the QR code type
            binding.qrCodeHistoryItemTypeIcon.setImageResource(
                when (qrHistory.type) {
                    context.getString(R.string.text) -> R.drawable.ic_text
                    context.getString(R.string.link) -> R.drawable.ic_link
                    context.getString(R.string.contact) -> R.drawable.ic_person
                    context.getString(R.string.wifi) -> R.drawable.ic_wifi
                    context.getString(R.string.phonee) -> R.drawable.ic_phone
                    context.getString(R.string.code) -> R.drawable.ic_code
                    context.getString(R.string.sms) -> R.drawable.ic_sms
                    context.getString(R.string.instagram) -> R.drawable.instagram
                    context.getString(R.string.whatsapp) -> R.drawable.whatsapp
                    context.getString(R.string.coupon) -> R.drawable.ic_coupon
                    context.getString(R.string.feedback) -> R.drawable.ic_feedback
                    context.getString(R.string.sn) -> R.drawable.ic_social_networks
                    context.getString(R.string.vcard)->R.drawable.application
                    context.getString(R.string.trackable)->R.drawable.ic_link
                    else -> R.mipmap.ic_launcher // Fallback icon
                }
            )

            // Set QR code data and formatted date
            binding.qrCodeHistoryItemId.text = "QR ID: ${qrHistory.qrId}"
            binding.qrCodeHistoryItemText.text = qrHistory.data
            binding.qrCodeHistoryItemCreatedDate.text =
                BaseActivity.getFormattedDate(context, qrHistory.createdAt.toLong())

            // Display notes if available, otherwise hide the notes section
            if (qrHistory.notes.isNotEmpty()) {
                binding.qrCodeHistoryItemNotesText.visibility = View.VISIBLE
                binding.qrCodeHistoryItemNotesText.text = if (qrHistory.notes.length >= 110) {
                    "${qrHistory.notes.substring(0, 107)}..."
                } else {
                    qrHistory.notes
                }
            } else {
                binding.qrCodeHistoryItemNotesText.visibility = View.GONE
            }

            // Handle item click event
            itemView.setOnClickListener {
                mListener.onItemClick(layoutPosition)
            }
        }
    }

    // Inflates the item layout and returns the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = QrCodeHistoryItemDesignBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        // Ensure the listener is set before returning the ViewHolder
        return ItemViewHolder(binding, listener ?: throw IllegalStateException("OnItemClickListener not set"))
    }

    // Binds data to the ViewHolder at the given position
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindData(qrCodeHistoryList[position], context)
    }

    // Returns the total number of items in the list
    override fun getItemCount(): Int = qrCodeHistoryList.size
}
