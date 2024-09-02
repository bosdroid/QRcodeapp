package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FontFamilyItemRowBinding
import com.expert.qrgenerator.model.Fonts

class FontAdapter(
    private val context: Context,
    private val fontList: List<Fonts>
) : RecyclerView.Adapter<FontAdapter.ItemViewHolder>() {

    // Interface for handling item click events
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    // Listener for click events
    private var mListener: OnItemClickListener? = null
    // Flag to indicate if the icon should be updated
    private var isIconUpdate: Boolean = false
    // Tracks the selected item's position
    private var selectedPosition = -1

    // Method to set the item click listener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    // Method to update the icon visibility flag
    fun updateIcon(flag: Boolean) {
        isIconUpdate = flag
    }

    // ViewHolder class to hold and bind item views
    class ItemViewHolder(
        private val binding: FontFamilyItemRowBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        // Method to bind data to the item view
        fun bindData(
            position: Int,
            font: Fonts,
            context: Context,
            isIconUpdate: Boolean,
            adapter: FontAdapter,
            selectedPosition: Int
        ) {
            // Load the font image into the ImageView using Glide
            Glide.with(context).load(font.fontImage).into(binding.fontItem)

            // Update the visibility of the selected icon based on conditions
            binding.selectedIcon.visibility = if (selectedPosition == position && isIconUpdate) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }

            // Set the click listener for the font item
            binding.fontItem.setOnClickListener {
                val previousItem = selectedPosition
                // Update the selected position and notify changes
                adapter.updateSelectedPosition(position)
                adapter.notifyItemChanged(previousItem)
                adapter.notifyItemChanged(position)

                // Trigger the click event through the listener
                mListener.onItemClick(position)
            }
        }
    }

    // Inflates the item view and creates a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = FontFamilyItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding, mListener ?: throw IllegalStateException("OnItemClickListener not set"))
    }

    // Binds data to the ViewHolder for a given position
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val font = fontList[position]
        holder.bindData(position, font, context, isIconUpdate, this, selectedPosition)
    }

    // Updates the selected position and notifies the adapter of data changes
    private fun updateSelectedPosition(newPosition: Int) {
        selectedPosition = newPosition
        // Only update the items that have changed
        notifyItemChanged(newPosition)
    }

    // Returns the total count of items
    override fun getItemCount(): Int = fontList.size
}
