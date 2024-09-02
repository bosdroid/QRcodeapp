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

class LogoAdapter(private val context: Context, private val logoList: List<String>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Interface to handle item click events
    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onAddItemClick(position: Int)
    }

    // Listener to be set by the parent component
    private var mListener: OnItemClickListener? = null

    // Flag to check if the icon update is enabled
    private var isIconUpdate: Boolean = false

    // Position of the selected item
    private var selectedPosition = -1

    // Method to set the item click listener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    // ViewHolder class for displaying image items
    class ItemViewHolder(
        private val binding: ImageItemRowBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        // Bind data to the item view
        fun bindData(
            image: String,
            position: Int,
            context: Context,
            isIconUpdate: Boolean,
            adapter: LogoAdapter,
            selectedPosition: Int
        ) {
            // Load image using Glide or set image from URI
            if (image.contains(context.getString(R.string.http)) || image.contains(context.getString(R.string.https))) {
                Glide.with(context).load(image).into(binding.imageItem)
            } else {
                val uri: Uri = Uri.parse(image)
                binding.imageItem.setImageURI(uri)
            }

            // Show or hide the selected icon based on position
            binding.imageSelectedIcon.visibility =
                if (selectedPosition == position && isIconUpdate) View.VISIBLE else View.INVISIBLE

            // Handle image item click
            binding.imageItem.setOnClickListener {
                val previousItem: Int = selectedPosition
                adapter.updateSelectedPosition(position)
                adapter.notifyItemChanged(previousItem)
                adapter.notifyItemChanged(position)
                mListener.onItemClick(position - 1)
            }
        }
    }

    // ViewHolder class for displaying the "Add" item
    class AddItemViewHolder(
        private val binding: AddItemLayoutBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        // Bind data to the "Add" item view
        fun bindData(position: Int) {
            binding.addCardView.setOnClickListener {
                mListener.onAddItemClick(position)
            }
        }
    }

    // Create appropriate ViewHolder based on viewType
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val addItemLayoutBinding = AddItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            AddItemViewHolder(addItemLayoutBinding, mListener ?: throw IllegalStateException("OnItemClickListener not set"))
        } else {
            val imageItemRowBinding = ImageItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ItemViewHolder(imageItemRowBinding, mListener ?: throw IllegalStateException("OnItemClickListener not set"))
        }
    }

    // Update the icon update flag
    fun updateIcon(flag: Boolean) {
        isIconUpdate = flag
    }

    // Update the adapter with a new item
    fun updateAdapter(position: Int) {
        selectedPosition += 1
        notifyItemInserted(position)
        notifyDataSetChanged()
    }

    // Update the selected position and refresh the view
    private fun updateSelectedPosition(newPosition: Int) {
        selectedPosition = newPosition
        notifyDataSetChanged()
    }

    // Determine the view type based on the position
    override fun getItemViewType(position: Int): Int {
        return if (position == 0) 0 else 1
    }

    // Bind the ViewHolder with data
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> (holder as AddItemViewHolder).bindData(position)
            else -> {
                val image = logoList[position - 1]  // Adjusted for 0-index position
                (holder as ItemViewHolder).bindData(
                    image,
                    position,
                    context,
                    isIconUpdate,
                    this,
                    selectedPosition
                )
            }
        }
    }

    // Return the total item count, including the "Add" item
    override fun getItemCount(): Int = logoList.size + 1
}
