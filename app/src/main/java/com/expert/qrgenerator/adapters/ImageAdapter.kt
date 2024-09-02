package com.expert.qrgenerator.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.AddItemLayoutBinding
import com.expert.qrgenerator.databinding.ImageItemRowBinding

class ImageAdapter(private val context: Context, private val imageList: List<String>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Interface for handling item click events
    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onAddItemClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null
    private var selectedPosition = -1

    // Set the listener for click events
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    // ViewHolder for displaying an image item
    class ItemViewHolder(
        private val binding: ImageItemRowBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        // Bind data to the view holder
        fun bindData(image: String, position: Int, context: Context, adapter: ImageAdapter, selectedPosition: Int) {
            // Load image from URL or URI
            if (image.startsWith(context.getString(R.string.http)) || image.startsWith(context.getString(R.string.https))) {
                Glide.with(context).load(image).into(binding.imageItem)
            } else {
                val uri: Uri = Uri.parse(image)
                binding.imageItem.setImageURI(uri)
            }

            // Highlight the selected image
            binding.imageSelectedIcon.visibility = if (selectedPosition == position) View.VISIBLE else View.INVISIBLE

            // Handle image click event
            binding.imageItem.setOnClickListener {
                val previousItem: Int = adapter.selectedPosition
                adapter.updateSelectedPosition(position)
                adapter.notifyItemChanged(previousItem)
                adapter.notifyItemChanged(position)
                mListener.onItemClick(position - 1) // Notify the listener
            }
        }
    }

    // ViewHolder for the "Add" button
    class AddItemViewHolder(
        private val binding: AddItemLayoutBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        // Bind data to the view holder
        fun bindData(position: Int) {
            binding.addCardView.setOnClickListener {
                mListener.onAddItemClick(position) // Notify the listener
            }
        }
    }

    // Method to update the adapter when a new item is added
    fun updateAdapter(position: Int) {
        selectedPosition += 1
        notifyItemInserted(position)
        notifyDataSetChanged()
    }

    // Method to update the selected item position
    private fun updateSelectedPosition(newPosition: Int) {
        selectedPosition = newPosition
    }

    // Return the view type for each item
    override fun getItemViewType(position: Int): Int {
        return if (position == 0) 0 else 1
    }

    // Create the appropriate view holder based on the view type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val addItemLayoutBinding =
                AddItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            AddItemViewHolder(addItemLayoutBinding, mListener ?: throw IllegalStateException("OnItemClickListener not set"))
        } else {
            val imageItemRowBinding =
                ImageItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ItemViewHolder(imageItemRowBinding, mListener ?: throw IllegalStateException("OnItemClickListener not set"))
        }
    }

    // Bind the appropriate data to the view holder
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> {
                (holder as AddItemViewHolder).bindData(position)
            }
            else -> {
                val image = imageList[position - 1]
                (holder as ItemViewHolder).bindData(image, position, context, this, selectedPosition)
            }
        }
    }

    // Return the total number of items, including the "Add" button
    override fun getItemCount(): Int = imageList.size + 1
}
