package com.expert.qrgenerator.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.databinding.SheetItemRowBinding
import com.expert.qrgenerator.model.Sheet

class SheetAdapter(private val sheetItems: ArrayList<Sheet>) :
    RecyclerView.Adapter<SheetAdapter.ItemViewHolder>() {

    // Interface for handling item clicks
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    // Listener for item clicks
    private var mListener: OnItemClickListener? = null

    // Set the click listener for items
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    // ViewHolder class to hold item views
    class ItemViewHolder(
        private val binding: SheetItemRowBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        // Bind data to views and set click listener
        fun bindData(sheet: Sheet) {
            binding.sheetItemName.text = sheet.name
            itemView.setOnClickListener {
                mListener.onItemClick(layoutPosition)
            }
        }
    }

    // Create a new ViewHolder instance
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val sheetItemRowBinding = SheetItemRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        // Ensure mListener is set before creating ViewHolder
        return ItemViewHolder(sheetItemRowBinding, mListener ?:
        throw IllegalStateException("OnItemClickListener not set"))
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = sheetItems[position]
        holder.bindData(item)
    }

    // Return the size of the item list
    override fun getItemCount(): Int = sheetItems.size
}
