package com.expert.qrgenerator.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.databinding.TableItemRowBinding

class TablesDataAdapter(private val tableList: ArrayList<String>) :
    RecyclerView.Adapter<TablesDataAdapter.ItemViewHolder>() {

    // Interface for handling item clicks
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    // Listener for item click events
    private var mListener: OnItemClickListener? = null

    // Set the item click listener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    // ViewHolder class for binding table data to views
    class ItemViewHolder(
        private val binding: TableItemRowBinding,
        private val mListener: OnItemClickListener?
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            // Set up click listener for the item view
            itemView.setOnClickListener {
                mListener?.let { listener ->
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position)
                    }
                }
            }
        }

        // Bind data to the views
        fun bindData(table: String) {
            binding.tableItemName.text = table
        }
    }

    // Create a new ViewHolder for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        // Inflate the layout for the table item row
        val tableItemRowBinding = TableItemRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(tableItemRowBinding, mListener)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val table = tableList[position]
        holder.bindData(table)
    }

    // Return the total number of items
    override fun getItemCount(): Int = tableList.size
}
