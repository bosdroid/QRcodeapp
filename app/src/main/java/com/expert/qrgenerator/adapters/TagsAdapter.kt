package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.databinding.TagItemDesignBinding

class TagsAdapter(
    private val context: Context,
    private val tagsList: List<String>
) : RecyclerView.Adapter<TagsAdapter.ItemViewHolder>() {

    // Interface for handling item click events
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    // Listener for click events
    private var mListener: OnItemClickListener? = null

    // Method to set the item click listener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }


    // ViewHolder class to hold and bind item views
    class ItemViewHolder(
        private val binding: TagItemDesignBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        // Method to bind data to the item view
        fun bindData(
            position: Int,
            tag:String
        ) {
            binding.tagItemView.text = tag

            binding.root.setOnClickListener {
                mListener.onItemClick(position)
            }
        }
    }

    // Inflates the item view and creates a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = TagItemDesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding, mListener ?: throw IllegalStateException("OnItemClickListener not set"))
    }

    // Binds data to the ViewHolder for a given position
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val tag = tagsList[position]
        holder.bindData(position, tag)
    }

    // Returns the total count of items
    override fun getItemCount(): Int = tagsList.size
}
