package com.expert.qrgenerator.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.databinding.SocialNetworksListItemDesignBinding
import com.expert.qrgenerator.model.SocialNetwork
import com.google.android.material.checkbox.MaterialCheckBox

class SocialNetworkAdapter(private val socialNetworkList: ArrayList<SocialNetwork>) : RecyclerView.Adapter<SocialNetworkAdapter.ItemViewHolder>() {

    // Interface to handle item click events
    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onItemCheckClick(position: Int, isChecked: Boolean)
        fun onItemEditIconClick(position: Int, checkBox: MaterialCheckBox)
    }

    // Listener to handle click events
    private var mListener: OnItemClickListener? = null

    // Method to set the OnItemClickListener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    // ViewHolder class to bind data to views
    inner class ItemViewHolder(
        private val binding: SocialNetworksListItemDesignBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // Bind data to the views
        fun bindData(item: SocialNetwork) {
            // Set view alpha and checkbox state based on item status
            itemView.alpha = if (item.isActive == 0) 0.2f else 0.7f
            binding.snItemCheckbox.isChecked = item.isActive != 0

            // Set image resource and text for the views
            binding.snItemLogo.setImageResource(item.icon)
            binding.snItemHeading.text = item.title
            binding.snItemTagline.text = item.url

            // Handle checkbox change events
            binding.snItemCheckbox.setOnCheckedChangeListener { _, isChecked ->
                mListener?.onItemCheckClick(layoutPosition, isChecked)
            }

            // Handle edit icon click events
            binding.snItemEditIcon.setOnClickListener {
                mListener?.onItemClick(layoutPosition)
            }
        }
    }

    // Inflate the item view and create ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = SocialNetworksListItemDesignBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindData(socialNetworkList[position])
    }

    // Return the total item count
    override fun getItemCount(): Int = socialNetworkList.size
}
