package com.expert.qrgenerator.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.databinding.DynamicQrSingleItemRowBinding
import com.expert.qrgenerator.model.CodeHistory

class DynamicQrAdapter(
    private val dynamicQrList: ArrayList<CodeHistory>
) : RecyclerView.Adapter<DynamicQrAdapter.ItemViewHolder>() {

    // Listener for handling item clicks
    private var listener: OnItemClickListener? = null

    // Interface to handle item click events
    interface OnItemClickListener {
        fun onItemEditClick(position: Int)
        fun onItemClick(position: Int)
    }

    // Method to set the listener
    fun setOnClickListener(mListener: OnItemClickListener) {
        listener = mListener
    }

    // ViewHolder class to hold and bind data to the item view
    class ItemViewHolder(
        private val binding: DynamicQrSingleItemRowBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        // Binds data to the views in the item layout
        fun bindData(codeHistory: CodeHistory) {
            // Set the text for the dynamic QR base URL item
            binding.dynamicQrBaseUrlItem.text = codeHistory.data

            // Set click listener for the entire item view
            itemView.setOnClickListener {
                mListener.onItemClick(adapterPosition)
            }

            // Set click listener for the edit button
            binding.editDynamicQr.setOnClickListener {
                mListener.onItemEditClick(adapterPosition)
            }
        }
    }

    // Inflates the item view and returns the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = DynamicQrSingleItemRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding, listener ?: throw IllegalStateException("OnItemClickListener not set"))
    }

    // Binds the data to the ViewHolder at the specified position
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindData(dynamicQrList[position])
    }

    // Returns the total number of items in the list
    override fun getItemCount(): Int = dynamicQrList.size
}
