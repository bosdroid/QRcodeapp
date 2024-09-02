package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.TableDetailRowDesignBinding
import com.expert.qrgenerator.model.TableObject
import com.google.android.material.textview.MaterialTextView

class TableDetailAdapter(
    private val tableDetailList: List<TableObject> // Use immutable List instead of mutable ArrayList
) : RecyclerView.Adapter<TableDetailAdapter.ItemViewHolder>() {

    // Interface for item click listener
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var onItemClickListener: OnItemClickListener? = null

    // Set the item click listener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    // ViewHolder class to hold and bind data to the view
    class ItemViewHolder(
        private val binding: TableDetailRowDesignBinding,
        private val onItemClickListener: OnItemClickListener? // Nullable listener
    ) : RecyclerView.ViewHolder(binding.root) {

        // Bind data to the views and handle item click
        fun bindData(tableObject: TableObject) {
            // Set data to views
            binding.tableIdView.text = tableObject.id.toString()
            binding.tableCodeDataView.text = tableObject.code_data
            binding.tableDateView.text = tableObject.date

            // Handle item click
            itemView.setOnClickListener {
                // Check if listener is not null before calling
                onItemClickListener?.onItemClick(adapterPosition)
            }
        }
    }

    // Inflate the item view and create the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        // Inflate layout using ViewBinding
        val binding = TableDetailRowDesignBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        // Create and return the ViewHolder with binding and listener
        return ItemViewHolder(binding, onItemClickListener)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val tableObject = tableDetailList[position]
        holder.bindData(tableObject)
    }

    // Return the total number of items
    override fun getItemCount(): Int = tableDetailList.size
}
