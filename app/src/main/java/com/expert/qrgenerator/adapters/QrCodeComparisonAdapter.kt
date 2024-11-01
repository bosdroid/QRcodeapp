package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.QrCodeComparisonItemDesignBinding
import com.expert.qrgenerator.databinding.QrCodeHistoryItemDesignBinding
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.ui.activities.BaseActivity

class QrCodeComparisonAdapter(
    private val context: Context,
    private val qrCodeHistoryList: ArrayList<CodeHistory>
) : RecyclerView.Adapter<QrCodeComparisonAdapter.ItemViewHolder>() {

    private var checkboxListener: OnCheckboxChangeListener? = null

    // Interface to handle checkbox change events
    interface OnCheckboxChangeListener {
        fun onCheckboxChanged(position: Int, isChecked: Boolean)
    }

    // Function to set the OnCheckboxChangeListener
    fun setOnCheckboxChangeListener(mListener: OnCheckboxChangeListener) {
        checkboxListener = mListener
    }

    // ViewHolder class to represent each item in the RecyclerView
    class ItemViewHolder(
        private val binding: QrCodeComparisonItemDesignBinding,
        private val checkboxListener: OnCheckboxChangeListener?
    ) : RecyclerView.ViewHolder(binding.root) {

        // Function to bind data to the views
        fun bindData(qrHistory: CodeHistory, context: Context) {

            // Set QR code data and formatted date
            binding.qrCodeItemId.text = "QR ID: ${qrHistory.qrId}"
            binding.qrCodeItemText.text = qrHistory.data
            binding.qrCodeItemCreatedDate.text =
                BaseActivity.getFormattedDate(context, qrHistory.createdAt.toLong())


            // Handle checkbox change event
            binding.qrCodeItemCheckbox.setOnCheckedChangeListener(null) // Clear previous listener
//            binding.qrCodeItemCheckbox.isChecked = qrHistory.isChecked // Set checkbox state based on data

            binding.qrCodeItemCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
                checkboxListener?.onCheckboxChanged(position, isChecked)
            }
        }
    }

    // Inflates the item layout and returns the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = QrCodeComparisonItemDesignBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        // Ensure the listener is set before returning the ViewHolder
        return ItemViewHolder(binding, checkboxListener ?: throw IllegalStateException("setOnCheckedChangeListener not set"))
    }

    // Binds data to the ViewHolder at the given position
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindData(qrCodeHistoryList[position], context)
    }

    // Returns the total number of items in the list
    override fun getItemCount(): Int = qrCodeHistoryList.size
}
