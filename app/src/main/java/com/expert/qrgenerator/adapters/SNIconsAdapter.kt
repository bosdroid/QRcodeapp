package com.expert.qrgenerator.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.databinding.SnIconItemRowDesignBinding

class SNIconsAdapter(private val iconsList: List<Pair<String, Int>>) : RecyclerView.Adapter<SNIconsAdapter.ItemViewHolder>() {

    // Interface for item click listener
    interface OnItemClickListener {
        fun onItemClick(pos: Int)
    }

    // Variable to hold the item click listener
    private var mListener: OnItemClickListener? = null

    // Method to set the item click listener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    // ViewHolder class to hold and bind view data
    inner class ItemViewHolder(private val binding: SnIconItemRowDesignBinding) : RecyclerView.ViewHolder(binding.root) {

        // Method to bind data to views
        fun bindData(pair: Pair<String, Int>) {
            binding.snIconItemView.setImageResource(pair.second)
            binding.snIconItemView.setOnClickListener {
                mListener?.onItemClick(adapterPosition)
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = SnIconItemRowDesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val pair = iconsList[position]
        holder.bindData(pair)
    }

    // Return the size of the dataset
    override fun getItemCount(): Int = iconsList.size
}
