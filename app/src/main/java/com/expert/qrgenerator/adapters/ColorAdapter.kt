package com.expert.qrgenerator.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.databinding.AddColorItemRowBinding
import com.expert.qrgenerator.databinding.ColorItemRowBinding

class ColorAdapter(private val colorList: List<String>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Interface for handling item click events
    interface OnItemClickListener {
        fun onItemClick(position: Int)  // Click on a color item
        fun onAddItemClick(position: Int)  // Click on the add item button
    }

    private var mListener: OnItemClickListener? = null
    private var isIconUpdate: Boolean = false  // Flag for showing/hiding the selected icon
    private var selectedPosition = -1  // Tracks the currently selected position

    // Method to set the click listener from outside the adapter
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    // ViewHolder for color items
    class ItemViewHolder(
        private val binding: ColorItemRowBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(
            color: String,
            position: Int,
            isIconUpdate: Boolean,
            adapter: ColorAdapter,
            selectedPosition: Int
        ) {
            // Set background color of the item
            binding.colorItem.setBackgroundColor(Color.parseColor("#$color"))

            // Show or hide the selected icon based on the selection status
            binding.selectedIcon.visibility =
                if (selectedPosition == position && isIconUpdate) View.VISIBLE else View.INVISIBLE

            // Handle color item click event
            binding.colorItem.setOnClickListener {
                val previousItem = selectedPosition
                adapter.updateSelectedPosition(position)
                adapter.notifyItemChanged(previousItem)
                adapter.notifyItemChanged(position)
                mListener.onItemClick(position - 1)  // Pass the correct position to listener
            }
        }
    }

    // ViewHolder for the add color item
    class AddItemViewHolder(
        private val binding: AddColorItemRowBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(position: Int) {
            // Handle add item click event
            binding.addCardView.setOnClickListener {
                mListener.onAddItemClick(position)
            }
        }
    }

    // Updates the selected position and notifies the adapter
    private fun updateSelectedPosition(newPosition: Int) {
        selectedPosition = newPosition
    }

    // Adds a new color item to the adapter
    fun updateAdapter(position: Int) {
        selectedPosition = position
        notifyItemInserted(position)
    }

    // Updates the flag to show/hide the selected icon
    fun updateIcon(flag: Boolean) {
        isIconUpdate = flag
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val binding = AddColorItemRowBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            AddItemViewHolder(binding, mListener ?: throw IllegalStateException("OnItemClickListener not set"))
        } else {
            val binding = ColorItemRowBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            ItemViewHolder(binding, mListener ?: throw IllegalStateException("OnItemClickListener not set"))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) 0 else 1  // First item is the add button
    }

    override fun getItemCount(): Int = colorList.size + 1  // Include add button item

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> (holder as AddItemViewHolder).bindData(position)
            else -> {
                val color = colorList[position - 1]  // Adjust for the add button item
                (holder as ItemViewHolder).bindData(color, position, isIconUpdate, this, selectedPosition)
            }
        }
    }
}
